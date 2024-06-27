// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.bom

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.boolerules.datastructures.extractModel
import com.booleworks.boolerules.helpers.satSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo

enum class BomCheckType { UNIQUENESS, COMPLETENESS, DEAD_PV }

data class Position(
    val positionId: String,
    val description: String,
    val constraint: String,
    val positionVariants: List<PositionVariant>
)

data class PositionVariant(
    val positionVariantId: String,
    val description: String,
    val constraint: String
)

data class PositionValidationResult(
    override val slice: Slice,
    override val state: ComputationState,
    val position: Position,
    val deadPvs: List<PositionVariant>,
    val nonUniquePVs: List<NonUniquePvsDetail>,
    val nonComplete: FeatureModel?
) : SliceResult

data class NonUniquePvsDetail(
    val firstPositionVariant: PositionVariant?,
    val secondPositionVariant: PositionVariant?,
    val exampleConfiguration: FeatureModel?,
)

// TODO allow list of Pos again
class PositionValidity(
    private val computationTypes: List<BomCheckType>,
    private val position: Position
) : Algorithm<PositionValidationResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    //TODO would make sense for ANY
    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): PositionValidationResult {
        val f = cf.formulaFactory()
        val solver = satSolver(cf, false, info).also { solver ->
            if (!solver.sat()) return PositionValidationResult(
                slice, ComputationState.notBuildable(), position, listOf(), listOf(), null
            )
        }
        val pvConstraintMap = mutableMapOf<PositionVariant, Formula>()
        position.positionVariants.forEach {
            val pvConstraint = info.translateConstraint(f, it.constraint)
            if (pvConstraint == null) {
                val error = "Could not translate the Position Variant constraint ${position.constraint}"
                return PositionValidationResult(
                    slice, ComputationState.error(error), position, listOf(), listOf(), null
                )
            }
            pvConstraintMap[it] = pvConstraint.second
        }

        val positionConstraint = info.translateConstraint(f, position.constraint)
        if (positionConstraint == null) {
            val error = "Could not translate the Position constraint ${position.constraint}"
            return PositionValidationResult(
                slice, ComputationState.error(error), position, listOf(), listOf(), null
            )
        }

        val deadPVs = checkDeadPVs(pvConstraintMap, solver)
        val nonComplete = checkCompleteness(pvConstraintMap, solver, info, f, positionConstraint.second)
        val nonUniquePVs = checkUniqueness(pvConstraintMap, solver, info)

        return PositionValidationResult(slice, ComputationState.success(), position, deadPVs, nonUniquePVs, nonComplete)
    }


    private fun checkDeadPVs(
        pvConstraintMap: MutableMap<PositionVariant, Formula>,
        solver: SATSolver,
    ): List<PositionVariant> {
        val deadPVs = mutableListOf<PositionVariant>()
        if (BomCheckType.DEAD_PV in computationTypes) {
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
        pvConstraintMap: MutableMap<PositionVariant, Formula>,
        solver: SATSolver,
        info: TranspilationInfo,
        f: FormulaFactory,
        pConstraint: Formula,
    ): FeatureModel? {
        if (BomCheckType.COMPLETENESS in computationTypes) {
            val allPvConstraintsNegatedConjunction = f.and(pvConstraintMap.values.map { it.negate(f) })
            solver.satCall().addFormulas(pConstraint, allPvConstraintsNegatedConjunction).solve()
                .use { satCall ->
                    if (satCall.satResult == Tristate.TRUE) {
                        val relevantVars = allPvConstraintsNegatedConjunction.variables(f)
                        // TODO sensible handling of relevant Vars
                        return extractModel(satCall.model(info.knownVariables).positiveVariables(), info)
                    }
                }
        }
        return null
    }

    private fun checkUniqueness(
        pvConstraintMap: MutableMap<PositionVariant, Formula>,
        solver: SATSolver,
        info: TranspilationInfo,
    ): List<NonUniquePvsDetail> {
        val f = solver.factory()
        val nonUniquePVs = mutableListOf<NonUniquePvsDetail>()
        if (BomCheckType.UNIQUENESS in computationTypes) {
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

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: PositionValidationResult?, newResult: PositionValidationResult) =
        newResult
}
