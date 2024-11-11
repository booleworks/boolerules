// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.visualization

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.graphs.datastructures.Graph
import com.booleworks.logicng.graphs.generators.ConstraintGraphGenerator
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranspilationInfo

val VISUALIZATION = object : ComputationType<
        VisualizationRequest,
        VisualizationResponse,
        VisualizationResult,
        NoComputationDetail,
        NoElement> {
    override val path = "visualization"
    override val docs: ApiDocs = computationDoc<VisualizationRequest, VisualizationResponse>(
        "Visualization",
        "Visualize a Rule File",
        "Provides different visualizations"
    )

    override val request = VisualizationRequest::class.java
    override val main = VisualizationResult::class.java
    override val detail = NoComputationDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(VisualizationComputation)
    override val computationFunction = runner::compute
}

object VisualizationComputation :
    SingleComputation<VisualizationRequest, VisualizationResult, NoComputationDetail, VisualizationInternalResult>(
        NON_CACHING_USE_FF
    ) {

    override fun allowedSliceTypes() = setOf(SliceTypeDO.SPLIT)

    override fun mergeInternalResult(
        existingResult: VisualizationInternalResult?,
        newResult: VisualizationInternalResult
    ) =
        if (existingResult == null) {
            newResult
        } else {
            error("Only split slices are allowed, so this method should never be called")
        }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranspilationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        cf: CspFactory
    ) = error("details are always computed in main computation")

    override fun computeForSlice(
        request: VisualizationRequest,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder
    ): VisualizationInternalResult {
        val f = cf.formulaFactory
        return ConstraintGraphGenerator.generateFromFormulas(f, info.propositions.map { it.formula }).let { graph ->
            VisualizationInternalResult(
                slice,
                graph.nodes().map { Node(it.content.variable().name, it.content.variable().name) },
                graph.edges()
            )
        }
    }
}


data class VisualizationInternalResult(
    override val slice: Slice,
    val nodes: List<Node>,
    val links: List<Edge>
) : InternalResult<VisualizationResult, NoComputationDetail>(slice) {
    override fun extractMainResult() = VisualizationResult(nodes, links)
    override fun extractDetails() = NoComputationDetail
}

fun Graph<Variable>.edges(): List<Edge> {
    val edges = mutableSetOf<Set<String>>()
    nodes().forEach { node ->
        val nodeName = node.content.name
        node.neighbours.asSequence().map { it.content.name }.filterNot { it == nodeName }
            .forEach { edges.add(setOf(nodeName, it)) }
    }
    return edges.map {
        it.toList().let { nodes -> Edge(nodes[0], nodes[1]) }
    }
}
