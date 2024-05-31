// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.reconfiguration

import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the reconfiguration resource")
data class ReconfigurationRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "The configuration to be fixed")
    val configuration: List<String>,

    @field:Schema(description = "The reconfiguration algorithm to use")
    val algorithm: ReconfigurationAlgorithm
) : ComputationRequest

@Schema(
    description = """
    The algorithm used for reconfiguration:
        - MAX_COV: Find a valid configuration by removing as few codes as possible (in the second step, the number of codes added is also minimized)
        - MIN_DIFF: Find a valid configuration by making as few changes (number of additions and removals of codes) as possible
"""
)
enum class ReconfigurationAlgorithm { MAX_COV, MIN_DIFF, }

@Schema(description = "The reconfiguration")
data class ReconfigurationResult(

    @field:Schema(description = "The features to remove s.t. the configuration is valid")
    val featuresToRemove: List<String>,

    @field:Schema(description = "The features to add s.t. the configuration is valid")
    val featuresToAdd: List<String>
)

typealias ReconfigurationResponse = SingleComputationResponse<ReconfigurationResult>
