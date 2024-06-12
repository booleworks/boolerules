// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.bomcheck

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.bomcheck.PositionValidationComputation.PositionInternalResult
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.NON_PT_CONFIG
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.extractModel
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranspilationInfo

val POSITION_VALIDATION = object : ComputationType<
        PositionValidationRequest,
        PositionValidationResponse,
        PositionValidationResult,
        PositionValidationDetail,
        NoElement> {
    override val path: String = "posval"
    override val docs: ApiDocs = computationDoc<PositionValidationRequest, PositionValidationResponse>(
        "BOM Position Validation",
        "Validates a position in a BOM",
        "Computes if a position in a bill of materials is unique (no configuration can select more " +
                "than one position variant), complete (all configurations select at least one position variant)," +
                "and has dead position variants that can never be seletcted by any configuration."
    )

    override val request = PositionValidationRequest::class.java
    override val main = PositionValidationResult::class.java
    override val detail = PositionValidationDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(PositionValidationComputation)
    override val computationFunction = runner::compute
}

internal object PositionValidationComputation : SingleComputation<
        PositionValidationRequest,
        PositionValidationResult,
        PositionValidationDetail,
        PositionInternalResult>(
    NON_CACHING_USE_FF
) {
    override fun allowedSliceTypes() = setOf(SliceTypeDO.SPLIT, SliceTypeDO.ALL)

    override fun mergeInternalResult(
        existingResult: PositionInternalResult?,
        newResult: PositionInternalResult
    ) = if (existingResult == null) {
        newResult
    } else {
        error("Only split slices are allowed, so this method should never be called")
    }

    override fun computeForSlice(
        request: PositionValidationRequest,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): PositionInternalResult {
        val f = cf.formulaFactory()
        val solver = satSolver(NON_PT_CONFIG, f, info, slice, status).also {
            if (!status.successful()) return PositionInternalResult(slice, request.position, listOf(), listOf(), null)
        }
        val pvConstraintMap = mutableMapOf<PositionVariant, Formula>()
        request.position.positionVariants.forEach {
            val pvConstraint = info.translateConstraint(f, it.constraint)
            if (pvConstraint == null) {
                status.addError("Could not translate the Position Variant constraint ${request.position.constraint}")
                return PositionInternalResult(slice, request.position, listOf(), listOf(), null)
            }
            pvConstraintMap[it] = pvConstraint.second
        }

        val positionConstraint = info.translateConstraint(f, request.position.constraint)
        if (positionConstraint == null) {
            status.addError("Could not translate the Position constraint ${request.position.constraint}")
            return PositionInternalResult(slice, request.position, listOf(), listOf(), null)
        }

        val deadPVs = checkDeadPVs(request, pvConstraintMap, solver)
        val nonComplete = checkCompleteness(request, pvConstraintMap, solver, info, f, positionConstraint.second)
        val nonUniquePVs = checkUniqueness(request, pvConstraintMap, solver, info)

        return PositionInternalResult(slice, request.position, deadPVs, nonUniquePVs, nonComplete)
    }


    private fun checkDeadPVs(
        request: PositionValidationRequest,
        pvConstraintMap: MutableMap<PositionVariant, Formula>,
        solver: SATSolver,
    ): List<PositionVariant> {
        val deadPVs = mutableListOf<PositionVariant>()
        if (BomCheckType.DEAD_PV in request.computationTypes) {
            pvConstraintMap.forEach { (pv, formula) ->
                solver.satCall().addFormulas(formula).solve().use { satCall ->
                    if (satCall.satResult != Tristate.TRUE) {
                        deadPVs.add(pv)
                    }
                }
            }
        }
        return deadPVs
    }

    private fun checkCompleteness(
        request: PositionValidationRequest,
        pvConstraintMap: MutableMap<PositionVariant, Formula>,
        solver: SATSolver,
        info: TranspilationInfo,
        f: FormulaFactory,
        pConstraint: Formula,
    ): FeatureModelDO? {
        if (BomCheckType.COMPLETENESS in request.computationTypes) {
            val allPvConstraintsNegatedConjunction = f.and(pvConstraintMap.values.map { it.negate(f) })
            solver.satCall().addFormulas(pConstraint, allPvConstraintsNegatedConjunction).solve()
                .use { satCall ->
                    if (satCall.satResult == Tristate.TRUE) {
                        val relevantVars = allPvConstraintsNegatedConjunction.variables(f)
                        return extractModel(satCall.model(info.knownVariables).positiveVariables(), info, relevantVars)
                    }
                }
        }
        return null
    }

    private fun checkUniqueness(
        request: PositionValidationRequest,
        pvConstraintMap: MutableMap<PositionVariant, Formula>,
        solver: SATSolver,
        info: TranspilationInfo,
    ): List<NonUniquePvsDetail> {
        val f = solver.factory()
        val nonUniquePVs = mutableListOf<NonUniquePvsDetail>()
        if (BomCheckType.UNIQUENESS in request.computationTypes) {
            val constraints = pvConstraintMap.keys.toTypedArray()
            for (i in constraints.indices) {
                val firstPv = pvConstraintMap[constraints[i]]
                for (j in i + 1 until constraints.size) {
                    val secondPv = pvConstraintMap[constraints[j]]
                    val formula = f.and(firstPv, secondPv)
                    solver.satCall().addFormulas(formula).solve().use { satCall ->
                        if (satCall.satResult == Tristate.TRUE) {
                            val example = extractModel(
                                satCall.model(info.knownVariables).positiveVariables(), info, formula.variables(f)
                            )
                            nonUniquePVs.add(NonUniquePvsDetail(constraints[i], constraints[j], example))
                        }
                    }
                }
            }
        }
        return nonUniquePVs
    }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranspilationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        cf: CspFactory
    ) = error("details are always computed in main computation")

    data class PositionInternalResult(
        override val slice: Slice,
        val position: Position,
        val deadPvs: List<PositionVariant>,
        val nonUniquePVs: List<NonUniquePvsDetail>,
        val nonComplete: FeatureModelDO?
    ) : InternalResult<PositionValidationResult, PositionValidationDetail>(slice) {
        override fun extractMainResult() = PositionValidationResult(
            position.positionId, position.description, position.constraint,
            nonComplete == null, nonUniquePVs.isNotEmpty(), deadPvs.isNotEmpty()
        )

        override fun extractDetails() = PositionValidationDetail(deadPvs, nonUniquePVs, nonComplete)
    }
}
