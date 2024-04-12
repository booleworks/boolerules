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
import com.booleworks.boolerules.computations.generic.computeRelevantVars
import com.booleworks.boolerules.computations.generic.extractModelWithInt
import com.booleworks.boolerules.computations.minmaxconfig.MinMaxConfigComputation.MinMaxInternalResult
import com.booleworks.logicng.csp.encodings.OrderDecoding
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT.MaxSATResult.OPTIMUM
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranslationInfo

val MINMAXCONFIG =
    object : ComputationType<MinMaxConfigRequest, MinMaxConfigResponse, Int, MinMaxConfigDetail, NoElement> {
        override val path: String = "minmaxconfig"
        override val docs: ApiDocs = computationDoc<MinMaxConfigRequest, MinMaxConfigResponse>(
            "Min/Max Configurations",
            "Compute a minimal or maximal configuration for a rule file",
            "A configuration is minimal if there is no other configuration with " +
                "fewer selected features, it is maximal if there is no " +
                "other configuration with more selected features. A list " +
                "of feautures can be provided over which the optimization " +
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
        info: TranslationInfo,
        model: PrlModel,
        f: FormulaFactory,
        status: ComputationStatusBuilder,
    ): MinMaxInternalResult {
        val relevantVars = computeRelevantVars(f, info, request.features)
        val relevantIntVars = if (request.features.isEmpty()) {
            info.integerVariables
        } else {
            info.integerVariables.filter { request.features.contains(it.feature) }
        }.map { it.variable }
        val solver = maxSat(MaxSATConfig.builder().build(), MaxSATSolver::incWBO, request, f, model, info, status)
        relevantVars.forEach { solver.addSoftFormula(f.literal(it.name(), request.computationType == MAX), 1) }
        return if (solver.solve() == OPTIMUM) {
            val integerAssignment = OrderDecoding.decode(solver.model(), relevantIntVars, info.encodingContext)
            val example = extractModelWithInt(solver.model().positiveVariables(), integerAssignment, info)
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
        info: TranslationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        f: FormulaFactory
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
