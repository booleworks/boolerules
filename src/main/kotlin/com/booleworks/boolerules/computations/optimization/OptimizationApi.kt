// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.optimization

import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the configuration optimization resource")
data class OptimizationRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "A flag whether a minimal or maximal weighted configuration should be computed")
    val computationType: OptimizationType,

    @field:Schema(description = "A list of weightings for constraints")
    val weightings: List<WeightPair>
) : ComputationRequest {
    override fun considerConstraints() = weightings.map { it.constraint }
}

@Schema(description = "A pair of constraint and it's weighting")
data class WeightPair(

    @field:Schema(description = "The constraint")
    val constraint: String,

    @field:Schema(description = "The weight to be added if the constraint is true")
    val weight: Int
)

@JsonInclude(Include.NON_NULL)
@Schema(description = "The details for a optimization computation")
data class OptimizationDetail(
    @field:Schema(description = "An example configuration of minimal/maximum weight")
    val exampleConfiguration: FeatureModelDO?,

    @field:Schema(description = "The weightings which were evaluated for the example configuration")
    val usedWeightings: List<WeightPair>
) : ComputationDetail

typealias OptimizationResponse = SingleComputationResponse<Int>
