// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.backbone

import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureDO
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the backbone resource")
data class BackboneRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "The list of features for which the backbone should be computed")
    val features: List<String> = listOf(),
) : ComputationRequest

enum class BackboneType { MANDATORY, FORBIDDEN, OPTIONAL }

typealias BackboneResponse = ListComputationResponse<BackboneType, FeatureDO>
