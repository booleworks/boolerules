// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.reconfiguration

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT.MaxSATResult.OPTIMUM
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranslationInfo

val RECONFIGURATION = object : ComputationType<
        ReconfigurationRequest,
        ReconfigurationResponse,
        ReconfigurationResult,
        NoComputationDetail,
        NoElement> {
    override val path = "reconfiguration"
    override val docs: ApiDocs = computationDoc<ReconfigurationRequest, ReconfigurationResponse>(
        "Reconfiguration",
        "Reconfigure an inconstructible order",
        "Adjust an inconstructible configuration s.t. it is valid again"
    )

    override val request = ReconfigurationRequest::class.java
    override val main = ReconfigurationResult::class.java
    override val detail = NoComputationDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(ReconfigurationComputation)
    override val computationFunction = runner::compute
}

object ReconfigurationComputation :
    SingleComputation<ReconfigurationRequest, ReconfigurationResult, NoComputationDetail, ReconfigurationInternalResult>(
        NON_CACHING_USE_FF
    ) {
    override fun mergeInternalResult(
        existingResult: ReconfigurationInternalResult?,
        newResult: ReconfigurationInternalResult
    ) =
        if (existingResult == null) {
            newResult
        } else {
            error("Only split slices are allowed, so this method should never be called")
        }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranslationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        cf: CspFactory
    ) = error("details are always computed in main computation")

    override fun computeForSlice(
        request: ReconfigurationRequest,
        slice: Slice,
        translation: TranslationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder
    ): ReconfigurationInternalResult {
        val f = cf.formulaFactory()
        val (validFeatures, invalidFeatures) = request.configuration.map { f.variable(it) }
            .partition { it in translation.knownVariables }
        if (invalidFeatures.isNotEmpty()) {
            status.addWarning(
                "The order contains invalid features which must always be removed: ${
                    invalidFeatures.joinToString(
                        ", "
                    )
                }"
            )
        }
        val configuration = validFeatures.toSet()
        val notInConfiguration = translation.knownVariables - configuration

        val solver = maxSat(MaxSATConfig.builder().build(), MaxSATSolver::oll, cf, translation).also {
            if (!status.successful()) return ReconfigurationInternalResult(slice, emptyList(), emptyList())
        }
        val weightForRemoval = when (request.algorithm) {
            ReconfigurationAlgorithm.MAX_COV -> translation.knownVariables.size
            ReconfigurationAlgorithm.MIN_DIFF -> 1
        }
        configuration.forEach { solver.addSoftFormula(it, weightForRemoval) }
        notInConfiguration.forEach { solver.addSoftFormula(it.negate(f), 1) }
        return if (solver.solve() == OPTIMUM) {
            val reconfiguration = solver.model().positiveVariables()
            val removed = configuration - reconfiguration
            val added = notInConfiguration.intersect(reconfiguration)
            ReconfigurationInternalResult(
                slice,
                (invalidFeatures + removed).map { it.name() }.toList(),
                added.map { it.name() }.toList()
            )
        } else {
            status.addWarning(
                "Rule set for the slice $slice is inconsistent. Use the 'consistency' " +
                        "check to get an explanation, why."
            )
            ReconfigurationInternalResult(slice, emptyList(), emptyList())
        }
    }
}


data class ReconfigurationInternalResult(
    override val slice: Slice,
    val featuresToRemove: List<String>,
    val featuresToAdd: List<String>,
) : InternalResult<ReconfigurationResult, NoComputationDetail>(slice) {
    override fun extractMainResult() = ReconfigurationResult(featuresToRemove, featuresToAdd)
    override fun extractDetails() = NoComputationDetail
}
