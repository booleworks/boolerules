// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.OptimizationType
import com.booleworks.boolerules.algorithms.OptimizationType.MAX
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.boolerules.datastructures.extractModel
import com.booleworks.boolerules.helpers.computeRelevantIntVars
import com.booleworks.boolerules.helpers.computeRelevantVars
import com.booleworks.boolerules.helpers.maxSATSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.handlers.TimeoutHandler
import com.booleworks.logicng.handlers.TimeoutMaxSATHandler
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo

data class MinMaxResult(
    override val slice: Slice,
    override val state: ComputationState,
    val size: Int,
    val featureModel: FeatureModel?,
) : SliceResult

class MinMaxConfig(
    private val features: List<Feature>,
    private val optimizationType: OptimizationType
) : Algorithm<MinMaxResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ANY, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): MinMaxResult {
        val f = cf.formulaFactory()
        val relevantVars = computeRelevantVars(f, info, features)
        val relevantIntVars = computeRelevantIntVars(info, features).map(LngIntVariable::variable)
        val maxsatHandler = TimeoutMaxSATHandler(timeoutHandler.computationEnd, TimeoutHandler.TimerType.FIXED_END)
        val solver = maxSATSolver(MaxSATConfig.builder().build(), MaxSATSolver::oll, cf.formulaFactory(), info)
        relevantVars.forEach { solver.addSoftFormula(f.literal(it.name(), optimizationType == MAX), 1) }
        val result = solver.solve(maxsatHandler)
        return when (result) {
            MaxSAT.MaxSATResult.OPTIMUM -> {
                val integerAssignment = cf.decode(solver.model(), relevantIntVars, info.encodingContext)
                val example = extractModel(solver.model().positiveVariables(), integerAssignment, info)
                val numberOfFeatures = example.size
                MinMaxResult(slice, ComputationState.success(), numberOfFeatures, example)
            }

            MaxSAT.MaxSATResult.UNSATISFIABLE -> MinMaxResult(slice, ComputationState.notBuildable(), -1, null)

            else -> {
                timeoutHandler.shouldContinue()
                MinMaxResult(slice, ComputationState.aborted(), -1, null)
            }
        }
    }

    override fun mergeAnyResult(existingResult: MinMaxResult?, newResult: MinMaxResult): MinMaxResult =
        if (existingResult == null) {
            newResult
        } else if (optimizationType == OptimizationType.MIN) {
            if (existingResult.size == -1 || newResult.size != -1 && newResult.size < existingResult.size) {
                newResult
            } else {
                existingResult
            }
        } else {
            if (newResult.size > existingResult.size) newResult else existingResult
        }
}
