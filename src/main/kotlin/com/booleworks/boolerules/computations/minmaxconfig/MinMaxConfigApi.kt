// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.minmaxconfig

import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the minimal/maximal configuration resource")
data class MinMaxConfigRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "A flag whether a minimal configuration should be computed")
    val computationType: OptimizationType,

    @field:Schema(
        description = "A list of features which should be used for the optimization " +
                "(all features are used if empty)"
    )
    val features: List<String>
) : ComputationRequest

@JsonInclude(Include.NON_NULL)
@Schema(description = "The details for a minimal/maximum configuration computation")
data class MinMaxConfigDetail(
    @field:Schema(description = "An example configuration of minimal/maximum length")
    val exampleConfiguration: FeatureModelDO?,
) : ComputationDetail

typealias MinMaxConfigResponse = SingleComputationResponse<Int>
