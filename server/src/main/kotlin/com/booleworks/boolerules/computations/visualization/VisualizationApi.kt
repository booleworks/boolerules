// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.visualization

import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the visualization resource")
data class VisualizationRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,
) : ComputationRequest {
    override val additionalConstraints = emptyList<String>()
}

@Schema(description = "The result of the visualization, i.e. a graph in a format which can directly be used for tools like force-graph")
data class VisualizationResult(

    @field:Schema(description = "The nodes of the graph")
    val nodes: List<Node>,

    @field:Schema(description = "The edges of the graph")
    val links: List<Edge>
)

@Schema(description = "A node of the graph")
data class Node(

    @field:Schema(description = "The id of the node")
    val id: String,

    @field:Schema(description = "The name of the node")
    val name: String,
)


@Schema(description = "An edge of the graph. Although the fields are called 'source' and 'target' an edge is always considered undirected.")
data class Edge(

    @field:Schema(description = "One end of the edge")
    val source: String,

    @field:Schema(description = "The other end of the edge")
    val target: String,
)

typealias VisualizationResponse = SingleComputationResponse<VisualizationResult>
