// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.modelenumeration

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.InternalListResult
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.ListComputation
import com.booleworks.boolerules.computations.generic.ListComputationRunner
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.NON_PT_CONFIG
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.computeRelevantVars
import com.booleworks.boolerules.computations.generic.extractModelWithInt
import com.booleworks.boolerules.computations.modelenumeration.ModelEnumerationComputation.ModelEnumerationElementResult
import com.booleworks.boolerules.computations.modelenumeration.ModelEnumerationComputation.ModelEnumerationInternalResult
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranslationInfo
import java.util.SortedSet

val MODELENUMERATION = object : ComputationType<
        ModelEnumerationRequest,
        ModelEnumerationResponse,
        Boolean,
        NoComputationDetail,
        FeatureModelDO> {
    override val path: String = "modelenumeration"
    override val docs: ApiDocs = computationDoc<ModelEnumerationRequest, ModelEnumerationResponse>(
        "Buildable Configurations",
        "Compute a set of all buildable configuration wrt. a set of features",
        "All buildable configuration in the given features are computed.  " +
                "If this set of features is a sub-set of all features, " +
                "it is called projected model enumeration"
    )

    override val request = ModelEnumerationRequest::class.java
    override val main = Boolean::class.java
    override val detail = NoComputationDetail::class.java
    override val element = FeatureModelDO::class.java

    override val runner = ListComputationRunner(ModelEnumerationComputation)
    override val computationFunction = runner::compute
}

internal object ModelEnumerationComputation : ListComputation<
        ModelEnumerationRequest,
        Map<FeatureModelDO, Slice>,
        NoComputationDetail,
        Boolean,
        NoComputationDetail,
        ModelEnumerationInternalResult,
        ModelEnumerationElementResult,
        FeatureModelDO>(NON_CACHING_USE_FF) {

    private const val SEL_TAUTOLOGY = "@SEL_TAUTOLOGY"

    override fun mergeInternalResult(
        existingResult: ModelEnumerationInternalResult?,
        newResult: ModelEnumerationInternalResult
    ) =
        if (existingResult == null) {
            newResult
        } else {
            existingResult.models.putAll(newResult.models)
            existingResult
        }

    override fun computeForSlice(
        request: ModelEnumerationRequest,
        slice: Slice,
        info: TranslationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): ModelEnumerationInternalResult {
        val relevantVars = computeRelevantVars(cf.formulaFactory(), info, request.features)
        val relevantIntVars = if (request.features.isEmpty()) {
            info.integerVariables
        } else {
            info.integerVariables.filter { request.features.contains(it.feature.fullName) }
        }.map { it.variable }

        val solver = miniSat(NON_PT_CONFIG, request, cf, model, info, slice, status).also {
            if (!status.successful()) return ModelEnumerationInternalResult(slice, mutableMapOf())
        }
        addTautologyClauses(solver, relevantVars)

        val models = solver.enumerateAllModels(relevantVars).map {
            val integerSatAssignment = cf.decode(it.assignment(), relevantIntVars, info.encodingContext)
            extractModelWithInt(it.positiveVariables(), integerSatAssignment, info)
        }.toSet()
        return ModelEnumerationInternalResult(slice, models.associateWith { slice }.toMutableMap())
    }

    private fun addTautologyClauses(solver: SATSolver, variables: SortedSet<Variable>) {
        val f = solver.factory()
        val selTautology = f.variable(SEL_TAUTOLOGY)
        variables.forEach { solver.add(f.or(selTautology, it)) }
        solver.add(selTautology)
    }

    override fun extractElements(internalResult: ModelEnumerationInternalResult): Set<FeatureModelDO> =
        internalResult.models.keys

    override fun extractInternalResult(
        element: FeatureModelDO,
        internalResult: ModelEnumerationInternalResult
    ): ModelEnumerationElementResult =
        internalResult.models[element].let { slice ->
            if (slice == null) {
                ModelEnumerationElementResult(internalResult.slice, false)
            } else {
                ModelEnumerationElementResult(slice, true)
            }
        }

    data class ModelEnumerationInternalResult(
        override val slice: Slice,
        val models: MutableMap<FeatureModelDO, Slice>
    ) :
        InternalListResult<Map<FeatureModelDO, Slice>, NoComputationDetail>(slice)

    data class ModelEnumerationElementResult(override val slice: Slice, val isModel: Boolean) :
        InternalResult<Boolean, NoComputationDetail>(slice) {
        override fun extractMainResult() = isModel
        override fun extractDetails() = NoComputationDetail
    }
}
