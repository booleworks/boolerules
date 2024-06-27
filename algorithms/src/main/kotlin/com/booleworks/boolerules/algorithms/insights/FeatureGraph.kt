// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.insights

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.graphs.datastructures.Graph
import com.booleworks.logicng.graphs.generators.ConstraintGraphGenerator
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo

data class Node(
    val id: String,
    val name: String,
)

data class Edge(
    val source: String,
    val target: String,
)

data class FeatureGraphResult(
    override val slice: Slice,
    override val state: ComputationState,
    val nodes: List<Node>,
    val links: List<Edge>
) : SliceResult

object FeatureGraph : Algorithm<FeatureGraphResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): FeatureGraphResult {
        //TODO Handler?
        val f = cf.formulaFactory()
        val graph = ConstraintGraphGenerator.generateFromFormulas(f, info.propositions.map { it.formula() })
        val nodes = graph.nodes().map { Node(it.content().variable().name(), it.content().variable().name()) }
        val edges = graph.edges()
        return FeatureGraphResult(slice, ComputationState.success(), nodes, edges)
    }

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: FeatureGraphResult?, newResult: FeatureGraphResult) = newResult
}


fun Graph<Variable>.edges(): List<Edge> {
    val edges = mutableSetOf<Set<String>>()
    nodes().forEach { node ->
        val nodeName = node.content().name()
        node.neighbours().asSequence().map { it.content().name() }.filterNot { it == nodeName }
            .forEach { edges.add(setOf(nodeName, it)) }
    }
    return edges.map {
        it.toList().let { nodes -> Edge(nodes[0], nodes[1]) }
    }
}

