// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.consistency.ConsistencyComputation.ConsistencyInternalResult
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.RuleDO
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SplitComputationDetail
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.computeExplanation
import com.booleworks.boolerules.computations.generic.extractModel
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.sat.SATSolverConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranspilationInfo

val CONSISTENCY = object : ComputationType<
        ConsistencyRequest,
        ConsistencyResponse,
        Boolean,
        ConsistencyDetail,
        NoElement> {
    override val path = "consistency"
    override val docs: ApiDocs = computationDoc<ConsistencyRequest, ConsistencyResponse>(
        "Consistency",
        "Compute the consistency of a rule file",
        "A rule set is consistent if it has at least one solution satisfying " +
                "all rules. If it is inconsistent, an optional explanation " +
                "can be computed."
    )

    override val request = ConsistencyRequest::class.java
    override val main = Boolean::class.java
    override val detail = ConsistencyDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(ConsistencyComputation)
    override val computationFunction = runner::compute
}

internal object ConsistencyComputation :
    SingleComputation<ConsistencyRequest, Boolean, ConsistencyDetail, ConsistencyInternalResult>(NON_CACHING_USE_FF) {

    override fun mergeInternalResult(
        existingResult: ConsistencyInternalResult?,
        newResult: ConsistencyInternalResult
    ) =
        if (existingResult == null || !existingResult.consistent) {
            newResult
        } else {
            existingResult
        }

    override fun computeForSlice(
        request: ConsistencyRequest,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): ConsistencyInternalResult {
        val solver = prepareSolver(cf, request.computeAllDetails, info)
        if (!status.successful()) return ConsistencyInternalResult(slice, false, null, null)
        return solver.satCall().solve().use { satCall ->
            if (satCall.satResult == Tristate.TRUE) {
                val example = extractModel(satCall.model(info.knownVariables).positiveVariables(), info)
                ConsistencyInternalResult(slice, true, example, null)
            } else {
                //TODO beautify explanation
                ConsistencyInternalResult(
                    slice,
                    false,
                    null,
                    if (request.computeAllDetails) {
                        computeExplanation(satCall, model.propertyStore.allDefinitions(), cf)
                    } else {
                        null
                    }
                )
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
    ): SplitComputationDetail<ConsistencyDetail> {
        val solver = prepareSolver(cf, true, info)
        return solver.satCall().solve().use { satCall ->
            assert(satCall.satResult == Tristate.FALSE) { "Detail computation should only be called for inconsistent slices" }
            val explanation = computeExplanation(satCall, model.propertyStore.allDefinitions(), cf)
            val result = ConsistencyInternalResult(slice, false, null, explanation)
            SplitComputationDetail(result, splitProperties)
        }
    }

    private fun prepareSolver(
        cf: CspFactory,
        proofTracing: Boolean,
        info: TranspilationInfo,
    ): SATSolver {
        val f = cf.formulaFactory()
        val solver = SATSolver.newSolver(f, SATSolverConfig.builder().proofGeneration(proofTracing).build()).apply {
            addPropositions(info.propositions)
        }
        return solver
    }

    data class ConsistencyInternalResult(
        override val slice: Slice,
        val consistent: Boolean,
        val example: FeatureModelDO?,
        val explanation: List<RuleDO>?
    ) :
        InternalResult<Boolean, ConsistencyDetail>(slice) {
        override fun extractMainResult() = consistent
        override fun extractDetails() = ConsistencyDetail(example, explanation)
    }
}
