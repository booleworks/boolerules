// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.config.ComputationConfig
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.FormulaFactoryConfig
import com.booleworks.logicng.formulas.FormulaFactoryConfig.FormulaMergeStrategy.IMPORT
import com.booleworks.logicng.formulas.FormulaFactoryConfig.FormulaMergeStrategy.USE_BUT_NO_IMPORT
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.logicng.solvers.sat.SATSolverConfig
import com.booleworks.prl.model.FeatureDefinition
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.transpiler.ModelTranslation
import com.booleworks.prl.transpiler.SliceTranslation
import com.booleworks.prl.transpiler.TranspilationInfo
import com.booleworks.prl.transpiler.mergeSlices
import com.booleworks.prl.transpiler.transpileModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.TreeMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

val NON_CACHING_USE_FF: () -> FormulaFactory =
    { FormulaFactory.nonCaching(FormulaFactoryConfig.builder().formulaMergeStrategy(USE_BUT_NO_IMPORT).build()) }
val CACHING_IMPORT_FF: () -> FormulaFactory =
    { FormulaFactory.caching(FormulaFactoryConfig.builder().formulaMergeStrategy(IMPORT).build()) }

val PT_CONFIG: SATSolverConfig = SATSolverConfig.builder().proofGeneration(true).build()
val NON_PT_CONFIG: SATSolverConfig = SATSolverConfig.builder().proofGeneration(false).build()

/**
 * Super class for all internal computations.
 *
 * This class handles all generic computation parts like automatically
 * computing the relevant slices, computing for each relevant slice, and
 * merging the result in the end.
 *
 * @param REQUEST the type of the computation request
 * @param MAIN the type of the main result (must implement sensible
 *             hash/equals methods)
 * @param DETAIL the type pf the computation details (must implement sensible
 *               hash/equals methods)
 * @param INTRES the type of the internal result
 */
sealed class Computation<
        REQUEST : ComputationRequest,
        MAIN,
        DETAIL : ComputationDetail,
        INTRES : InternalResult<MAIN, DETAIL>>(
    open val ffProvider: () -> FormulaFactory
) {
    internal val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * The allowed slice types.  Be default all slice types are allowed, but a
     * computation can forbid certain types if they are not clearly defined in
     * the context of its business logic.
     */
    open fun allowedSliceTypes(): Set<SliceTypeDO> = setOf(SliceTypeDO.SPLIT, SliceTypeDO.ANY, SliceTypeDO.ALL)

    /**
     * Merges a new result with an existing result for this computation.
     * @param existingResult the result already existing or null if none is
     *                       present
     * @param newResult the new result which should be merged with the existing
     *                  one
     * @return the new merged result
     */
    internal abstract fun mergeInternalResult(existingResult: INTRES?, newResult: INTRES): INTRES

    /**
     * Performs the computation for a single slice.  This is the main
     * computation function which has to be implemented for a new computation.
     * @param request the global request for the computation
     * @param slice the slice for the specific computation
     * @param info the translation info including propositions, variables and
     *             variable mappings
     * @param model the compiled PRL model
     * @param status the status builder
     * @return the internal result for the computation
     */
    internal abstract fun computeForSlice(
        request: REQUEST,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): INTRES

    /**
     * Computes the result for the whole PRL model and returns a mapping from
     * computed slice to internal result.
     */
    internal fun computeForModel(
        request: REQUEST,
        model: PrlModel,
        status: ComputationStatusBuilder
    ): Map<Slice, INTRES> {
        val factory: FormulaFactory = FormulaFactory.caching()
        val cspFactory = CspFactory(factory)
        request.validateAndAugmentSliceSelection(model, allowedSliceTypes())
        val modelTranslation = transpileModel(
            cspFactory, model, request.modelSliceSelection(),
            additionalConstraints = request.additionalConstraints,
            considerConstraints = request.considerConstraints()
        )
        status.numberOfSlices = modelTranslation.allSlices.size
        status.sliceSets = computeProjectedSliceSets(modelTranslation.computations, request)
        val slice2Translation = modelTranslation.sliceMap()
        //TODO slice simplification does currently not work with ANY/ALL slices
//        val slicesToCompute = slice2Translation.values.map { it.sliceSet.slices[0] }.toSet()
        val slicesToCompute = slice2Translation.values.flatMap { it.sliceSet.slices }.toSet()
        val sliceResults = Collections.synchronizedMap(TreeMap<Slice, INTRES>())
        val splitComputations = modelTranslation.allSplitSlices().ifEmpty { setOf(Slice.empty()) }
        val numExecs = AtomicInteger(0)

        if (ComputationConfig.numThreads == 1) {
            splitComputations.forEach { splitSlice ->
                execute(
                    request,
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
                                request,
                                CspFactory(cspFactory, ffProvider()),
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

    private fun computeProjectedSliceSets(computations: List<SliceTranslation>, req: REQUEST): List<List<SliceDO>> {
        val splitProperties = req.splitProperties()
        return computations.map { comp -> comp.sliceSet.slices.map { it.toDO().project(splitProperties) } }
    }

    private fun execute(
        request: REQUEST,
        cf: CspFactory,
        model: PrlModel,
        modelTranslation: ModelTranslation,
        splitSlice: Slice,
        slice2Translation: Map<Slice, SliceTranslation>,
        slicesToCompute: Set<Slice>,
        sliceResults: MutableMap<Slice, INTRES>,
        numExecs: AtomicInteger,
        status: ComputationStatusBuilder,
    ) {
        for (anySlice in modelTranslation.allAnySlices(splitSlice).ifEmpty { setOf(splitSlice) }) {
            val allSlices = modelTranslation.allAllSlices(anySlice).ifEmpty { setOf(anySlice) }
            val sliceTranslations = allSlices.map { slice2Translation[it]!! }.distinct()
            if (sliceTranslations.size == 1) {
                val sliceTranslation = sliceTranslations[0]
                if (anySlice in slicesToCompute) {
                    val result = computeForSlice(request, anySlice, sliceTranslation.info, model, cf, status)
                    numExecs.incrementAndGet()
                    val newResult = mergeInternalResult(sliceResults[splitSlice], result)
                    sliceResults[splitSlice] = newResult
                    //TODO slice simplification does currently not work with ANY/ALL slices
//                    sliceTranslation.sliceSet.slices.drop(1).forEach {
                    // set the result for the remaining slices of the set
//                        sliceResults[it.filterProperties(setOf(SliceType.SPLIT))] = newResult
//                    }
                }
            } else {
                val merged = mergeSlices(cf, sliceTranslations)
                val result = computeForSlice(request, anySlice, merged.info, model, cf, status)
                numExecs.incrementAndGet()
                val newResult = mergeInternalResult(sliceResults[splitSlice], result)
                sliceResults[splitSlice] = newResult
            }
        }
    }

    internal fun satSolver(
        config: SATSolverConfig,
        f: FormulaFactory,
        info: TranspilationInfo,
        slice: Slice,
        status: ComputationStatusBuilder
    ): SATSolver {
        val solver = SATSolver.newSolver(f, config)
        solver.addPropositions(info.propositions)
        if (!solver.sat()) {
            status.addWarning(
                "Original rule set for the slice $slice is inconsistent. " +
                        "Use the 'consistency' check to get an explanation, why."
            )
        }
        return solver
    }

    internal fun maxSat(
        config: MaxSATConfig,
        algo: (FormulaFactory, MaxSATConfig) -> MaxSATSolver,
        f: FormulaFactory,
        info: TranspilationInfo,
    ): MaxSATSolver {
        val solver = algo(f, config)
        info.propositions.forEach { solver.addHardFormula(it.formula()) }
        return solver
    }

    private fun errorResult(
        status: ComputationStatusBuilder,
        message: String
    ): Map<PrlFeature, FeatureDefinition<*, *>>? {
        status.addError(message)
        return null
    }
}

abstract class SingleComputation<
        REQUEST : ComputationRequest,
        MAIN,
        DETAIL : ComputationDetail,
        INTRES : InternalResult<MAIN, DETAIL>>(
    override val ffProvider: () -> FormulaFactory,
) : Computation<REQUEST, MAIN, DETAIL, INTRES>(ffProvider) {

    /**
     * Computes the result for a single computation request for the whole PRL
     * model and merges the slice results afterward.
     */
    internal fun computeResponse(
        request: REQUEST,
        model: PrlModel,
        status: ComputationStatusBuilder
    ): MergeResult<MAIN, DETAIL> =
        mergeMainResults(request, computeForModel(request, model, status))

    internal fun computeDetail(
        model: PrlModel,
        sliceSelection: List<AnySliceSelection>,
        additionalConstraints: List<String>,
        considerConstraints: List<String>,
        splitProperties: Set<String>
    ): SplitComputationDetail<DETAIL> {
        val f = FormulaFactory.caching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(
            cf,
            model,
            sliceSelection,
            additionalConstraints = additionalConstraints,
            considerConstraints = considerConstraints
        )
        assert(modelTranslation.computations.size == 1) { "Expected to get exactly one slice" }
        val slice = modelTranslation.allSlices.first()
        val translation = modelTranslation.computations.first().info
        return computeDetailForSlice(slice, model, translation, additionalConstraints, splitProperties, cf)
    }

    abstract fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranspilationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        cf: CspFactory
    ): SplitComputationDetail<DETAIL>
}

abstract class ListComputation<
        REQUEST : ComputationRequest,
        MAIN,
        DETAIL : ComputationDetail,
        ELEMMAIN : Comparable<ELEMMAIN>,
        ELEMDETAIL : ComputationDetail,
        INTRES : InternalResult<MAIN, DETAIL>,
        ELEMRES : InternalResult<ELEMMAIN, ELEMDETAIL>,
        ELEMENT : Comparable<ELEMENT>,
        >(
    override val ffProvider: () -> FormulaFactory
) : Computation<REQUEST, MAIN, DETAIL, INTRES>(ffProvider) {

    /**
     * Computes all different elements for the result.
     */
    internal abstract fun extractElements(internalResult: INTRES): Set<ELEMENT>

    /**
     * Extracts the internal result for a single element from the slice result
     * over all elements.
     */
    internal abstract fun extractInternalResult(element: ELEMENT, internalResult: INTRES): ELEMRES

    /**
     * Computes the result for a list computation request for the whole PRL
     * model and merges the slice results afterward.
     */
    internal fun computeResponse(request: REQUEST, model: PrlModel, status: ComputationStatusBuilder):
            Map<ComputationElement<ELEMENT>, MergeResult<ELEMMAIN, ELEMDETAIL>> {
        val computationResult = computeForModel(request, model, status)
        val allElements = computationResult.values.flatMap { extractElements(it) }.toSet()
        val elementMap = allElements.associateWith { resultMapForElement(it, computationResult) }.toSortedMap()
        var elementId = 1
        return elementMap
            .map { (element, elementRes) ->
                val mergeResult = mergeMainResults(request, elementRes)
                val res = Pair(ComputationElement(elementId, element), mergeResult)
                elementId++
                res
            }.toMap()
    }

    private fun resultMapForElement(element: ELEMENT, computationResult: Map<Slice, INTRES>): Map<Slice, ELEMRES> =
        computationResult.map { (slice, sliceRes) -> Pair(slice, extractInternalResult(element, sliceRes)) }.toMap()
}
