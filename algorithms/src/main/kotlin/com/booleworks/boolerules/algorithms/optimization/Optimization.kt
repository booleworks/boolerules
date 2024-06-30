// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.OptimizationType
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.boolerules.datastructures.extractModel
import com.booleworks.boolerules.helpers.maxSATSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.handlers.TimeoutHandler
import com.booleworks.logicng.handlers.TimeoutMaxSATHandler
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo
import kotlin.math.absoluteValue

data class WeightPair(
    val constraint: String,
    val weight: Int
)

data class OptimizationResult(
    override val slice: Slice,
    override val state: ComputationState,
    val weight: Int,
    val featureModel: FeatureModel?,
    val usedWeights: List<WeightPair>
) : SliceResult

class Optimization(
    private val weightings: List<WeightPair>,
    private val optimizationType: OptimizationType
) : Algorithm<OptimizationResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ANY, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): OptimizationResult {
        val f = cf.formulaFactory()
        val solver = maxSATSolver(MaxSATConfig.builder().build(), MaxSATSolver::oll, f, info)
        val mapping = mutableMapOf<Formula, Int>()
        val constraintMap = mutableMapOf<Formula, String>()
        weightings.forEach { wp ->
            val cPair = info.translateConstraint(f, wp.constraint)
            if (cPair == null) {
                val state = ComputationState.error("Could not process constraint ${wp.constraint}")
                return OptimizationResult(slice, state, -1, null, listOf())
            } else {
                val constraint = cPair.second
                val formula =
                    if (optimizationType == OptimizationType.MIN && wp.weight >= 0 ||
                        optimizationType == OptimizationType.MAX && wp.weight < 0
                    ) {
                        constraint.negate(f)
                    } else {
                        constraint
                    }
                mapping[constraint] = wp.weight
                constraintMap[constraint] = wp.constraint
                val weight = wp.weight.absoluteValue
                solver.addSoftFormula(formula, weight)
            }
        }

        val maxsatHandler = TimeoutMaxSATHandler(timeoutHandler.computationEnd, TimeoutHandler.TimerType.FIXED_END)
        val result = solver.solve(maxsatHandler)
        return when (result) {
            MaxSAT.MaxSATResult.OPTIMUM -> {
                val solverModel = solver.model()
                val evaluatedWeights = mapping.filter { (k, _) -> k.evaluate(solverModel) }
                val weight = evaluatedWeights.map { it.value }.sum()
                val integerAssignment =
                    cf.decode(solverModel, info.integerVariables.map(LngIntVariable::variable), info.encodingContext)
                val example = extractModel(solverModel.positiveVariables(), integerAssignment, info)
                val usedWeights = evaluatedWeights.map { WeightPair(constraintMap[it.key]!!, it.value) }
                OptimizationResult(slice, ComputationState.success(), weight, example, usedWeights)
            }

            MaxSAT.MaxSATResult.UNSATISFIABLE -> {
                OptimizationResult(slice, ComputationState.notBuildable(), -1, null, listOf())
            }

            else -> {
                timeoutHandler.shouldContinue()
                OptimizationResult(slice, ComputationState.aborted(), -1, null, listOf())
            }
        }
    }

    override fun mergeAnyResult(
        existingResult: OptimizationResult?,
        newResult: OptimizationResult
    ): OptimizationResult =
        if (existingResult == null) {
            newResult
        } else if (optimizationType == OptimizationType.MIN) {
            if (newResult.weight != -1 && newResult.weight < existingResult.weight) newResult else existingResult
        } else {
            if (newResult.weight > existingResult.weight) newResult else existingResult
        }
}
