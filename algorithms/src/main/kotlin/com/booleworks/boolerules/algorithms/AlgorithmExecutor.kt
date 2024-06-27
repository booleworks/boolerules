// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms

import com.booleworks.boolerules.config.ComputationConfig
import com.booleworks.boolerules.datastructures.ExecutionStatusBuilder
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.FormulaFactoryConfig
import com.booleworks.logicng.formulas.FormulaFactoryConfig.FormulaMergeStrategy.IMPORT
import com.booleworks.logicng.formulas.FormulaFactoryConfig.FormulaMergeStrategy.USE_BUT_NO_IMPORT
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.ModelTranslation
import com.booleworks.prl.transpiler.SliceTranslation
import com.booleworks.prl.transpiler.mergeSlices
import com.booleworks.prl.transpiler.transpileModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

val NON_CACHING_USE_FF: () -> FormulaFactory =
    { FormulaFactory.nonCaching(FormulaFactoryConfig.builder().formulaMergeStrategy(USE_BUT_NO_IMPORT).build()) }
val CACHING_IMPORT_FF: () -> FormulaFactory =
    { FormulaFactory.caching(FormulaFactoryConfig.builder().formulaMergeStrategy(IMPORT).build()) }

class AlgorithmExecutor<RESULT : SliceResult>(
    private val algorithm: Algorithm<RESULT>,
) {
    fun executeForModel(
        model: PrlModel,
        sliceSelection: List<AnySliceSelection>,
        additionalConstraints: List<String>,
        considerConstraints: List<String>,
        status: ExecutionStatusBuilder
    ): Map<Slice, RESULT> {
        val factory: FormulaFactory = FormulaFactory.caching()
        val cspFactory = CspFactory(factory)
        validateSliceSelection(sliceSelection, algorithm.allowedSliceTypes(), status)
        if (status.successful().not()) {
            return mapOf()
        }
        val modelTranslation = transpileModel(
            cspFactory, model, sliceSelection,
            additionalConstraints = additionalConstraints,
            considerConstraints = considerConstraints
        )
        status.numberOfSlices = modelTranslation.allSlices.size
        val slice2Translation = modelTranslation.sliceMap()
        val slicesToCompute = slice2Translation.values.flatMap { it.sliceSet.slices }.toSet()
        val sliceResults = Collections.synchronizedMap(TreeMap<Slice, RESULT>())
        val splitComputations = modelTranslation.allSplitSlices().ifEmpty { setOf(Slice.empty()) }
        val numExecs = AtomicInteger(0)

        if (ComputationConfig.numThreads == 1) {
            splitComputations.forEach { splitSlice ->
                execute(
                    cspFactory,
                    model,
                    modelTranslation,
                    splitSlice,
                    slice2Translation,
                    slicesToCompute,
                    sliceResults,
                    numExecs,
                    status
                )
            }
        } else {
            factory.readOnlyMode()
            Executors.newFixedThreadPool(ComputationConfig.numThreads).asCoroutineDispatcher().use { dispatcher ->
                runBlocking {
                    val jobs = splitComputations.map { splitSlice ->
                        launch(dispatcher) {
                            execute(
                                CspFactory(cspFactory, algorithm.ffProvider()()),
                                model,
                                modelTranslation,
                                splitSlice,
                                slice2Translation,
                                slicesToCompute,
                                sliceResults,
                                numExecs,
                                status
                            )
                        }
                    }
                    jobs.forEach { it.join() }
                }
            }
        }
        status.numberOfSliceComputations = numExecs.get()
        return sliceResults
    }

    private fun validateSliceSelection(
        sliceSelection: List<AnySliceSelection>,
        allowedSliceTypes: Set<SliceType>,
        status: ExecutionStatusBuilder
    ) {
        val notAllowed = sliceSelection.filter { it.sliceType !in allowedSliceTypes }
        if (notAllowed.isNotEmpty()) {
            status.addError("Not allowed slice types in request")
        }
    }

    private fun execute(
        cf: CspFactory,
        model: PrlModel,
        modelTranslation: ModelTranslation,
        splitSlice: Slice,
        slice2Translation: Map<Slice, SliceTranslation>,
        slicesToCompute: Set<Slice>,
        sliceResults: MutableMap<Slice, RESULT>,
        numExecs: AtomicInteger,
        status: ExecutionStatusBuilder,
    ) {
        for (anySlice in modelTranslation.allAnySlices(splitSlice).ifEmpty { setOf(splitSlice) }) {
            val allSlices = modelTranslation.allAllSlices(anySlice).ifEmpty { setOf(anySlice) }
            val sliceTranslations = allSlices.map { slice2Translation[it]!! }.distinct()
            if (sliceTranslations.size == 1) {
                val st = sliceTranslations[0]
                if (anySlice in slicesToCompute) {
                    val result = algorithm.executeForSlice(cf, model, st.info, anySlice, status.timeoutHandler)
                    numExecs.incrementAndGet()
                    val newResult = algorithm.mergeAnyResult(sliceResults[splitSlice], result)
                    sliceResults[splitSlice] = newResult
                }
            } else {
                val merged = mergeSlices(cf, sliceTranslations)
                val result = algorithm.executeForSlice(cf, model, merged.info, anySlice, status.timeoutHandler)
                numExecs.incrementAndGet()
                val newResult = algorithm.mergeAnyResult(sliceResults[splitSlice], result)
                sliceResults[splitSlice] = newResult
            }
        }
    }
}

