// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.consistency.ConsistencyComputation.ConsistencyInternalResult
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatus
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.RuleDO
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SplitComputationDetail
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.computeExplanation
import com.booleworks.boolerules.computations.generic.extractModelWithInt
import com.booleworks.logicng.csp.encodings.OrderDecoding
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.MiniSat
import com.booleworks.logicng.solvers.sat.MiniSatConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.PrlProposition
import com.booleworks.prl.transpiler.RuleInformation
import com.booleworks.prl.transpiler.RuleType
import com.booleworks.prl.transpiler.TranslationInfo

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
        info: TranslationInfo,
        model: PrlModel,
        f: FormulaFactory,
        status: ComputationStatusBuilder,
    ): ConsistencyInternalResult {
        val solver = prepareSolver(f, request.computeAllDetails, info, request.additionalConstraints, model, status)
        if (!status.successful()) return ConsistencyInternalResult(slice, false, null, null)
        return if (solver.sat() == Tristate.TRUE) {
            val integerSatAssignment =
                OrderDecoding.decode(solver.model(info.encodingContext.relevantSatVariables), info.integerVariables.map(LngIntVariable::variable), info.encodingContext)
            val example = extractModelWithInt(solver.model(info.knownVariables).positiveVariables(), integerSatAssignment, info)
            ConsistencyInternalResult(slice, true, example, null)
        } else {
            //TODO beautify explanation
            ConsistencyInternalResult(
                slice,
                false,
                null,
                if (request.computeAllDetails) computeExplanation(
                    solver,
                    model.propertyStore.allDefinitions()
                ) else null
            )
        }
    }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranslationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        f: FormulaFactory
    ): SplitComputationDetail<ConsistencyDetail> {
        val solver = prepareSolver(f, true, info, additionalConstraints, model, ComputationStatus("", "", SINGLE))
        val sat = solver.sat()
        assert(sat == Tristate.FALSE) { "Detail computation should only be called for inconsistent slices" }
        val explanation = computeExplanation(solver, model.propertyStore.allDefinitions())
        val result = ConsistencyInternalResult(slice, false, null, explanation)
        return SplitComputationDetail(result, splitProperties)
    }

    private fun prepareSolver(
        f: FormulaFactory,
        proofTracing: Boolean,
        info: TranslationInfo,
        additionalConstraints: List<String>,
        model: PrlModel,
        status: ComputationStatusBuilder
    ): MiniSat {
        val solver = MiniSat.miniSat(f, MiniSatConfig.builder().proofGeneration(proofTracing).build()).apply {
            addPropositions(info.propositions)
        }
        if (solver.sat() == Tristate.TRUE) {
            val additionalFormulas =
                additionalConstraints.mapNotNull { constraint ->
                    processConstraint(f, constraint, model, info, status)
                }
            if (!status.successful()) return solver
            solver.addPropositions(additionalFormulas.map { it.first })
            solver.addPropositions(additionalFormulas
                .flatMap { it.second }
                .toSet()
                .map { PrlProposition(RuleInformation(RuleType.INTEGER_VARIABLE), info.integerEncodings.getEncoding(it)!!) }
            )
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
