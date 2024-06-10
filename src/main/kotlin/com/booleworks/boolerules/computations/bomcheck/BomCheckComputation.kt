// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.bomcheck

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.bomcheck.BomCheckComputation.BomCheckInternalResult
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.InternalListResult
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.ListComputation
import com.booleworks.boolerules.computations.generic.ListComputationRunner
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.NON_PT_CONFIG
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.extractModel
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.PrlProposition
import com.booleworks.prl.transpiler.TranslationInfo

val BOMCHECK = object : ComputationType<
        BomCheckRequest,
        BomCheckResponse,
        BomCheckAlgorithmsResult,
        BomCheckDetail,
        PositionElementDO> {
    override val path: String = "bomcheck"
    override val docs: ApiDocs = computationDoc<BomCheckRequest, BomCheckResponse>(
        "Bill of material check",
        "Computes either ",
        "Constraints can be given weightings. If the constraint is true, the " +
                "weighting will be considered, otherwise not. Configurations " +
                "of minimal and maximal weightings wrt. these weightings " +
                "can be computed."
    )

    override val request = BomCheckRequest::class.java
    override val main = BomCheckAlgorithmsResult::class.java
    override val detail = BomCheckDetail::class.java
    override val element = PositionElementDO::class.java

    override val runner = ListComputationRunner(BomCheckComputation)
    override val computationFunction = runner::compute
}

internal object BomCheckComputation :
    ListComputation<BomCheckRequest,
            Map<PositionElementDO, Slice>,
            BomCheckDetail,
            BomCheckAlgorithmsResult,
            BomCheckDetail,
            BomCheckInternalResult,
            BomCheckComputation.PositionElementResult,
            PositionElementDO>(NON_CACHING_USE_FF) {

    override fun mergeInternalResult(
        existingResult: BomCheckInternalResult?,
        newResult: BomCheckInternalResult
    ): BomCheckInternalResult =
        if (existingResult == null) {
            newResult
        } else {
            existingResult.positions.putAll(newResult.positions)
            existingResult
        }

    override fun computeForSlice(
        request: BomCheckRequest,
        slice: Slice,
        info: TranslationInfo,
        model: PrlModel,
        f: FormulaFactory,
        status: ComputationStatusBuilder,
    ): BomCheckInternalResult {
        val solver = miniSat(NON_PT_CONFIG, request, f, model, info, slice, status).also {
            if (!status.successful()) return BomCheckInternalResult(slice, mutableMapOf())
        }
        val positionElements: MutableMap<PositionElementDO, Triple<Slice, List<DeadPvDetail>, List<NonUniquePvsDetail>>> = mutableMapOf()
        // check if solver is sat with the rules from rule file
        request.positions.forEach { position ->
            val deadPVs: MutableList<DeadPvDetail> = mutableListOf()  // inconstructable pv = dead pv
            val nonUniquePVs: MutableList<NonUniquePvsDetail> = mutableListOf()
            var isComplete = true

            val pvConstraintMap =
                position.positionVariants.associateWith { processConstraint(f, it.constraint, model, info, status) }
                    .filterValues { it != null } as Map<PositionVariant, PrlProposition>
            // Search dead pvs
            pvConstraintMap.forEach { (pv, pvConstraint) ->
                solver.satCall().addPropositions(pvConstraint).solve().use { satCall ->
                    if (satCall.satResult != Tristate.TRUE) {
                        val deadPvModel = extractModel(satCall.model(info.knownVariables).positiveVariables(), info)
                        deadPVs.add(DeadPvDetail(pv, deadPvModel))
                    }

                }
            }

            // Check if position is complete
            val positionConstraint = processConstraint(f, position.constraint, model, info, status)
            val allPvConstraintsNegatedConjunction = f.and(pvConstraintMap.values.map { f.not(it.formula()) })
            solver.satCall().addFormulas(positionConstraint?.formula(), allPvConstraintsNegatedConjunction).solve().use { satCall ->
                if (satCall.satResult != Tristate.TRUE) {
                    // no model for empty hits
                    isComplete = false
                }
            }

            // Check if pv is unique
            val pvConstraintIiterator = pvConstraintMap.iterator()
            while (pvConstraintIiterator.hasNext()) {
                val currentPv = pvConstraintIiterator.next()
                pvConstraintIiterator.forEachRemaining {
                    solver.satCall().addPropositions(currentPv.value, it.value).solve().use { satCall ->
                        if (satCall.satResult == Tristate.TRUE) {
                            val nonUniquePvsModel = extractModel(satCall.model(info.knownVariables).positiveVariables(), info)
                            nonUniquePVs.add(NonUniquePvsDetail(currentPv.key, it.key, nonUniquePvsModel))
                        }
                    }
                }
            }

            // build result
            positionElements.put(
                PositionElementDO(
                    position.positionId,
                    position.description,
                    position.constraint,
                    isComplete,
                    nonUniquePVs.isNotEmpty(),
                    deadPVs.isNotEmpty()
                ),
                Triple(slice, deadPVs, nonUniquePVs)
            )
        }

        return BomCheckInternalResult(slice, positionElements)
    }

    override fun extractElements(internalResult: BomCheckInternalResult): Set<PositionElementDO> = internalResult.positions.keys

    override fun extractInternalResult(
        element: PositionElementDO,
        internalResult: BomCheckInternalResult
    ): PositionElementResult =
        internalResult.positions[element].let { triple ->
            if (triple == null) {
                PositionElementResult(
                    internalResult.slice,
                    BomCheckAlgorithmsResult(element.isComplete, element.hasNonUniquePVs, element.hasDeadPvs),
                    listOf(),
                    listOf()
                )
            } else {
                val (slice: Slice, deadPVs: List<DeadPvDetail>, nonUniquePvs: List<NonUniquePvsDetail>) = triple
                PositionElementResult(slice, BomCheckAlgorithmsResult(element.isComplete, element.hasNonUniquePVs, element.hasDeadPvs), deadPVs, nonUniquePvs)
            }
        }

    data class BomCheckInternalResult(
        override val slice: Slice,
        val positions: MutableMap<PositionElementDO, Triple<Slice, List<DeadPvDetail>, List<NonUniquePvsDetail>>>
    ) : InternalListResult<Map<PositionElementDO, Slice>, BomCheckDetail>(slice)

    data class PositionElementResult(
        override val slice: Slice,
        val result: BomCheckAlgorithmsResult,
        val deadPVs: List<DeadPvDetail>,
        val nonUniquePvs: List<NonUniquePvsDetail>
    ) :
        InternalResult<BomCheckAlgorithmsResult, BomCheckDetail>(slice) {
        override fun extractMainResult() = result
        override fun extractDetails() = BomCheckDetail(deadPVs, nonUniquePvs)
    }
}
