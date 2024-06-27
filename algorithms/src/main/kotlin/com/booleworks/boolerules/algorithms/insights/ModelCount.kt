// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.insights

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.CACHING_IMPORT_FF
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.modelcounting.ModelCounter
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo
import java.math.BigInteger

data class ModelCountResult(
    override val slice: Slice,
    override val state: ComputationState,
    val count: BigInteger
) : SliceResult

object ModelCount : Algorithm<ModelCountResult> {
    override fun ffProvider() = CACHING_IMPORT_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): ModelCountResult {
        // TODO use handler
        val f = cf.formulaFactory()
        val allVariables = sortedSetOf<Variable>()
        val formulas = info.propositions.map {
            allVariables.addAll(it.formula().variables(f))
            it.formula()
        }
        allVariables.addAll(info.knownVariables)
        val count = ModelCounter.count(f, formulas, allVariables)
        return ModelCountResult(slice, ComputationState.success(), count)
    }

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: ModelCountResult?, newResult: ModelCountResult) = newResult
}
