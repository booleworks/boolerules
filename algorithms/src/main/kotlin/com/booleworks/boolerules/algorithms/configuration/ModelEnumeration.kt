// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.boolerules.datastructures.extractModel
import com.booleworks.boolerules.helpers.computeRelevantIntVars
import com.booleworks.boolerules.helpers.computeRelevantVars
import com.booleworks.boolerules.helpers.satSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo
import java.util.*

data class ModelEnumerationResult(
    override val slice: Slice,
    override val state: ComputationState,
    val models: MutableMap<FeatureModel, Slice>
) : SliceResult

class ModelEnumeration(
    private val features: List<Feature>
) : Algorithm<ModelEnumerationResult> {
    private val selTautology = "@SEL_TAUTOLOGY"

    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ANY, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): ModelEnumerationResult {
        //TODO Handler
        //TODO Fix All Slices
        val f = cf.formulaFactory()
        val relevantVars = computeRelevantVars(f, info, features)
        val relevantIntVars = computeRelevantIntVars(info, features).map(LngIntVariable::variable)

        val solver = satSolver(cf, false, info).also { solver ->
            if (!solver.sat()) return ModelEnumerationResult(slice, ComputationState.notBuildable(), mutableMapOf())
        }

        addTautologyClauses(solver, relevantVars)

        val models = solver.enumerateAllModels(relevantVars).map {
            val integerAssignment = cf.decode(it.assignment(), relevantIntVars, info.encodingContext)
            extractModel(it.positiveVariables(), integerAssignment, info)
        }.toSet()
        return ModelEnumerationResult(slice, ComputationState.success(), models.associateWith { slice }.toMutableMap())
    }

    private fun addTautologyClauses(solver: SATSolver, variables: SortedSet<Variable>) {
        val f = solver.factory()
        val selTautology = f.variable(selTautology)
        variables.forEach { solver.add(f.or(selTautology, it)) }
        solver.add(selTautology)
    }

    override fun mergeAnyResult(
        existingResult: ModelEnumerationResult?,
        newResult: ModelEnumerationResult
    ) = if (existingResult == null) {
        newResult
    } else {
        existingResult.models.putAll(newResult.models)
        existingResult
    }
}
