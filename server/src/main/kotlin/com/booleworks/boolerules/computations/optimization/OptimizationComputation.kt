// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.optimization

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.OptimizationType.MAX
import com.booleworks.boolerules.computations.generic.OptimizationType.MIN
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.extractModel
import com.booleworks.boolerules.computations.optimization.OptimizationComputation.OptimizationInternalResult
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSatConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo
import kotlin.math.absoluteValue

val OPTIMIZATION = object : ComputationType<
        OptimizationRequest,
        OptimizationResponse,
        Int,
        OptimizationDetail,
        NoElement> {
    override val path: String = "optimization"
    override val docs: ApiDocs = computationDoc<OptimizationRequest, OptimizationResponse>(
        "Configuration Optimization",
        "Compute a configuration of minimal or maximal weight for a given constraint weighting",
        "Constraints can be given weightings. If the constraint is true, the " +
                "weighting will be considered, otherwise not. Configurations " +
                "of minimal and maximal weightings wrt. these weightings " +
                "can be computed."
    )

    override val request = OptimizationRequest::class.java
    override val main = Int::class.java
    override val detail = OptimizationDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(OptimizationComputation)
    override val computationFunction = runner::compute
}

internal object OptimizationComputation :
    SingleComputation<OptimizationRequest, Int, OptimizationDetail, OptimizationInternalResult>(NON_CACHING_USE_FF) {

    override fun mergeInternalResult(
        existingResult: OptimizationInternalResult?,
        newResult: OptimizationInternalResult
    ) =
        if (existingResult == null) {
            newResult
        } else if (newResult.type == MIN) {
            if (newResult.weight != -1 && newResult.weight < existingResult.weight) newResult else existingResult
        } else {
            if (newResult.weight > existingResult.weight) newResult else existingResult
        }

    override fun computeForSlice(
        request: OptimizationRequest,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): OptimizationInternalResult {
        val f = cf.formulaFactory
        val solver = maxSat(MaxSatConfig.CONFIG_OLL, f, info)
        val mapping = mutableMapOf<Formula, Int>()
        val constraintMap = mutableMapOf<Formula, String>()
        request.weightings.forEach { wp ->
            val cPair = info.translateConstraint(f, wp.constraint)
            if (cPair == null) {
                status.addError("Could not process constraint ${wp.constraint}")
            } else {
                val constraint = cPair.second
                val formula =
                    if (request.computationType == MIN && wp.weight >= 0 ||
                        request.computationType == MAX && wp.weight < 0
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
        return if (!status.successful()) {
            OptimizationInternalResult(slice, request.computationType, -1, null, listOf())
        } else {
            val solverResult = solver.solve();
            if (solverResult.isSatisfiable) {
                val solverModel = solverResult.model.toAssignment()
                val evaluatedWeights = mapping.filter { (k, _) -> k.evaluate(solverModel) }
                val weight = evaluatedWeights.map { it.value }.sum()
                val integerAssignment = cf.decode(
                    solverModel,
                    info.integerVariables.map(LngIntVariable::variable),
                    info.knownVariables,
                    info.encodingContext
                )
                val example = extractModel(integerAssignment, info)
                val usedWeights = evaluatedWeights.map { WeightPair(constraintMap[it.key]!!, it.value) }
                OptimizationInternalResult(slice, request.computationType, weight, example, usedWeights)
            } else {
                status.addWarning(
                    "Rule set for the slice $slice is inconsistent. Use the 'consistency' " +
                            "check to get an explanation, why."
                )
                OptimizationInternalResult(slice, request.computationType, -1, null, listOf())
            }
        }
    }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranspilationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        cf: CspFactory
    ) = error("details are always computed in main computation")

    data class OptimizationInternalResult(
        override val slice: Slice,
        val type: OptimizationType,
        val weight: Int,
        val example: FeatureModelDO?,
        val weights: List<WeightPair>
    ) :
        InternalResult<Int, OptimizationDetail>(slice) {
        override fun extractMainResult() = weight
        override fun extractDetails() = OptimizationDetail(example, weights)
    }
}
