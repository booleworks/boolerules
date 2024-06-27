// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureInstance
import com.booleworks.boolerules.helpers.maxSATSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.handlers.TimeoutHandler
import com.booleworks.logicng.handlers.TimeoutMaxSATHandler
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo

enum class ReconfigurationAlgorithm { MAX_COV, MIN_DIFF, }

data class ReconfigurationResult(
    override val slice: Slice,
    override val state: ComputationState,
    val featuresToRemove: List<Feature>,
    val featuresToAdd: List<Feature>
) : SliceResult

class Reconfiguration(
    private val configuration: List<FeatureInstance>,
    private val algorithm: ReconfigurationAlgorithm
) : Algorithm<ReconfigurationResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): ReconfigurationResult {
        // TODO support other features than Boolean
        val f = cf.formulaFactory()
        val (validFeatures, invalidFeatures) = configuration.map { f.variable(it.feature.featureCode) }
            .partition { it in info.knownVariables }
        // TODO warnings
//        if (invalidFeatures.isNotEmpty()) {
//            status.addWarning(
//                "The order contains invalid features which must always be removed: ${
//                    invalidFeatures.joinToString(
//                        ", "
//                    )
//                }"
//            )
//        }
        val configuration = validFeatures.toSet()
        val notInConfiguration = info.knownVariables - configuration

        val solver = maxSATSolver(MaxSATConfig.builder().build(), MaxSATSolver::oll, f, info)
        val weightForRemoval = when (algorithm) {
            ReconfigurationAlgorithm.MAX_COV -> info.knownVariables.size
            ReconfigurationAlgorithm.MIN_DIFF -> 1
        }
        configuration.forEach { solver.addSoftFormula(it, weightForRemoval) }
        notInConfiguration.forEach { solver.addSoftFormula(it.negate(f), 1) }
        val maxsatHandler = TimeoutMaxSATHandler(timeoutHandler.computationEnd, TimeoutHandler.TimerType.FIXED_END)
        val result = solver.solve(maxsatHandler)

        return when (result) {
            MaxSAT.MaxSATResult.OPTIMUM -> {
                val reconfiguration = solver.model().positiveVariables()
                val removed = configuration - reconfiguration
                val added = notInConfiguration.intersect(reconfiguration)
                val featuresToRemove = (invalidFeatures + removed).map { boolFt(it.name()) }.toList()
                val featuresToAdd = added.map { boolFt(it.name()) }.toList()
                ReconfigurationResult(slice, ComputationState.success(), featuresToRemove, featuresToAdd)
            }

            MaxSAT.MaxSATResult.UNSATISFIABLE -> {
                ReconfigurationResult(slice, ComputationState.notBuildable(), listOf(), listOf())
            }

            else -> {
                timeoutHandler.shouldContinue()
                ReconfigurationResult(slice, ComputationState.aborted(), listOf(), listOf())
            }
        }
    }

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: ReconfigurationResult?, newResult: ReconfigurationResult) = newResult
}
