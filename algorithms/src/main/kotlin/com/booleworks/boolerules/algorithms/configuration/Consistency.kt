// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.Explanation
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.boolerules.datastructures.computeExplanation
import com.booleworks.boolerules.datastructures.extractModel
import com.booleworks.boolerules.helpers.satSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.handlers.TimeoutHandler
import com.booleworks.logicng.handlers.TimeoutSATHandler
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo


data class ConsistencyResult(
    override val slice: Slice,
    override val state: ComputationState,
    val consistent: Boolean,
    val exampleConfiguration: FeatureModel?,
    val explanation: Explanation?
) : SliceResult

class Consistency(private val withDetails: Boolean) : Algorithm<ConsistencyResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ANY, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): ConsistencyResult {
        val solver = satSolver(cf, withDetails, info)
        val satHandler = TimeoutSATHandler(timeoutHandler.computationEnd, TimeoutHandler.TimerType.FIXED_END)
        return solver.satCall().handler(satHandler).solve().use { satCall ->
            when (satCall.satResult) {
                Tristate.TRUE -> {
                    val integerAssignment = cf.decode(
                        satCall.model(info.encodingContext.relevantSatVariables),
                        info.integerVariables.map(LngIntVariable::variable),
                        info.encodingContext
                    )
                    val example = if (!withDetails) {
                        null
                    } else {
                        extractModel(satCall.model(info.knownVariables).positiveVariables(), integerAssignment, info)
                    }
                    ConsistencyResult(slice, ComputationState.success(), true, example, null)
                }

                Tristate.FALSE -> {
                    val explanation = if (!withDetails) {
                        null
                    } else {
                        computeExplanation(satCall, cf)
                    }
                    ConsistencyResult(slice, ComputationState.success(), false, null, explanation)
                }

                else -> {
                    timeoutHandler.shouldContinue()
                    ConsistencyResult(slice, ComputationState.aborted(), false, null, null)
                }
            }
        }
    }

    override fun mergeAnyResult(existingResult: ConsistencyResult?, newResult: ConsistencyResult) =
        if (existingResult == null || !existingResult.consistent) {
            newResult
        } else {
            existingResult
        }
}
