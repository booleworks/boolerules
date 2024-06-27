// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo

enum class ResultStatus {
    SUCCESS,
    ERROR,
    NOT_BUILDABLE,
    ABORTED
}

data class ComputationState(
    val status: ResultStatus,
    val error: String? = null
) {
    companion object {
        fun success() = ComputationState(ResultStatus.SUCCESS)
        fun aborted() = ComputationState(ResultStatus.ABORTED)
        fun notBuildable() = ComputationState(ResultStatus.NOT_BUILDABLE)
        fun error(message: String) = ComputationState(ResultStatus.ERROR, message)
    }
}

interface SliceResult {
    val slice: Slice
    val state: ComputationState
}

interface Algorithm<RESULT : SliceResult> {
    fun ffProvider(): () -> FormulaFactory

    fun allowedSliceTypes(): Set<SliceType>

    fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): RESULT

    fun mergeAnyResult(existingResult: RESULT?, newResult: RESULT): RESULT
}

enum class OptimizationType { MIN, MAX }

