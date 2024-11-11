// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.minmaxconfig

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
import com.booleworks.boolerules.computations.generic.computeRelevantIntVars
import com.booleworks.boolerules.computations.generic.computeRelevantVars
import com.booleworks.boolerules.computations.generic.extractModel
import com.booleworks.boolerules.computations.minmaxconfig.MinMaxConfigComputation.MinMaxInternalResult
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSatConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo

val MINMAXCONFIG =
    object : ComputationType<MinMaxConfigRequest, MinMaxConfigResponse, Int, MinMaxConfigDetail, NoElement> {
        override val path: String = "minmaxconfig"
        override val docs: ApiDocs = computationDoc<MinMaxConfigRequest, MinMaxConfigResponse>(
            "Min/Max Configurations",
            "Compute a minimal or maximal configuration for a rule file",
            "A configuration is minimal if there is no other configuration with " +
                    "fewer selected features, it is maximal if there is no " +
                    "other configuration with more selected features. A list " +
                    "of features can be provided over which the optimization " +
                    "is computed."
        )

        override val request = MinMaxConfigRequest::class.java
        override val main = Int::class.java
        override val detail = MinMaxConfigDetail::class.java
        override val element = NoElement::class.java

        override val runner = SingleComputationRunner(MinMaxConfigComputation)
        override val computationFunction = runner::compute
    }

internal object MinMaxConfigComputation :
    SingleComputation<MinMaxConfigRequest, Int, MinMaxConfigDetail, MinMaxInternalResult>(NON_CACHING_USE_FF) {

    override fun mergeInternalResult(existingResult: MinMaxInternalResult?, newResult: MinMaxInternalResult) =
        if (existingResult == null) {
            newResult
        } else if (newResult.type == MIN) {
            if (existingResult.size == -1 || newResult.size != -1 && newResult.size < existingResult.size) {
                newResult
            } else {
                existingResult
            }
        } else {
            if (newResult.size > existingResult.size) newResult else existingResult
        }

    override fun computeForSlice(
        request: MinMaxConfigRequest,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): MinMaxInternalResult {
        val f = cf.formulaFactory
        val relevantVars = computeRelevantVars(f, info, request.features).filter(info.knownVariables::contains)
        val relevantIntVars = computeRelevantIntVars(info, request.features).map(LngIntVariable::variable)
        val solver = maxSat(MaxSatConfig.CONFIG_OLL, cf.formulaFactory, info)
        relevantVars.forEach { solver.addSoftFormula(f.literal(it.name, request.computationType == MAX), 1) }
        val solverResult = solver.solve()
        return if (solverResult.isSatisfiable) {
            val integerAssignment =
                cf.decode(solverResult.model.toAssignment(), relevantIntVars, relevantVars, info.encodingContext)
            val example = extractModel(integerAssignment, info)
            val numberOfFeatures = example.size
            MinMaxInternalResult(slice, request.computationType, numberOfFeatures, example)
        } else {
            status.addWarning(
                "Rule set for the slice $slice is inconsistent. Use the 'consistency' " +
                        "check to get an explanation, why."
            )
            MinMaxInternalResult(slice, request.computationType, -1, null)
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

    data class MinMaxInternalResult(
        override val slice: Slice,
        val type: OptimizationType,
        val size: Int,
        val example: FeatureModelDO?
    ) :
        InternalResult<Int, MinMaxConfigDetail>(slice) {
        override fun extractMainResult() = size
        override fun extractDetails() = MinMaxConfigDetail(example)
    }
}
