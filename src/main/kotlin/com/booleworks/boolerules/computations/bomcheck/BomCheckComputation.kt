// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.bomcheck

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
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
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.PrlProposition
import com.booleworks.prl.transpiler.TranslationInfo

val BOMCHECK = object : ComputationType<
        BomCheckRequest,
        BomCheckResponse,
        BomCheckAlgorithmsResult,
        NoComputationDetail,
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
    override val detail = NoComputationDetail::class.java
    override val element = PositionElementDO::class.java

    override val runner = ListComputationRunner(BomCheckComputation)
    override val computationFunction = runner::compute
}

internal object BomCheckComputation :
    ListComputation<BomCheckRequest,
            Map<PositionElementDO, Slice>,
            NoComputationDetail,
            BomCheckAlgorithmsResult,
            NoComputationDetail,
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
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): BomCheckInternalResult {
        val solver = miniSat(NON_PT_CONFIG, request, cf, model, info, slice, status).also {
            if (!status.successful()) return BomCheckInternalResult(slice, mutableMapOf())
        }
        val positionElements: MutableList<PositionElementDO> = mutableListOf()
        // check if solver is sat with the rules from rule file
        request.positions.forEach { position ->
            val positionConstraint = processConstraint(cf, position.constraint, model, info, status)
            solver.add(positionConstraint)
            if (!solver.sat()) {
                // TODO was tun?
                return@forEach
            }
            val deadPVs: MutableList<PositionVariant> = mutableListOf()  // inconstructable pv = dead pv
            val nonUniquePVs: MutableList<Pair<PositionVariant, PositionVariant>> = mutableListOf()
            var isComplete = true

            val pvConstraintMap =
                position.positionVariants.associateWith { processConstraint(cf, it.constraint, model, info, status) }
                    .filterValues { it != null } as Map<PositionVariant, PrlProposition>
            // Search dead pvs -> Detail: List of position variants that are dead
            pvConstraintMap.forEach { (pv, pvConstraint) ->
                solver.satCall().addPropositions(pvConstraint).solve().use { satCall ->
                    if (satCall.satResult != Tristate.TRUE) {
                        deadPVs.add(pv)
                    }
                }
                val initialState = solver.saveState()
                solver.add(pvConstraint)
                if (!solver.sat()) {
                    // Do something
                }
                solver.loadState(initialState)
            }

            // Check if position is complete -> (Detail with example that does not hit a pv?)
            val initialState = solver.saveState()
            pvConstraintMap.values.forEach {
                solver.add(cf.formulaFactory().not(it.formula()))
            }
            if (solver.sat()) {
                // TODO generate model as example
                isComplete = false
            }
            solver.loadState(initialState)

            // Check if pv is unique (?) -> List of pvs and a flag that indicates if it is unique
            // Add pv rule to solver and next rule to solver then check if satisfiable -> if yes, then the two rules are not unique


            // build result
            positionElements.add(
                PositionElementDO(
                    position.positionId,
                    position.description,
                    position.constraint,
                    isComplete,
                    nonUniquePVs.isEmpty(),
                    deadPVs.isEmpty()
                )
            )

        }

        TODO()
    }

    override fun extractElements(internalResult: BomCheckInternalResult): Set<PositionElementDO> =
        internalResult.positions.keys

    override fun extractInternalResult(
        element: PositionElementDO,
        internalResult: BomCheckInternalResult
    ): PositionElementResult =
        internalResult.positions[element].let { slice ->
            if (slice == null) {
                PositionElementResult(
                    internalResult.slice,
                    BomCheckAlgorithmsResult(element.isComplete, element.hasNonUniquePVs, element.hasDeadPvs)
                )
            } else {
                PositionElementResult(
                    slice,
                    BomCheckAlgorithmsResult(element.isComplete, element.hasNonUniquePVs, element.hasDeadPvs)
                )
            }
        }

    data class BomCheckInternalResult(
        override val slice: Slice,
        val positions: MutableMap<PositionElementDO, Slice>
    ) : InternalListResult<Map<PositionElementDO, Slice>, NoComputationDetail>(slice)

    data class PositionElementResult(override val slice: Slice, val result: BomCheckAlgorithmsResult) :
        InternalResult<BomCheckAlgorithmsResult, NoComputationDetail>(slice) {
        override fun extractMainResult() = result
        override fun extractDetails() = NoComputationDetail
    }
}
