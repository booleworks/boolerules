// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.modelenumeration

import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the (projected) model enumeration check resource")
data class ModelEnumerationRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "A list of features used for the (projected) model enumeration")
    val features: List<String>
) : ComputationRequest

typealias ModelEnumerationResponse = ListComputationResponse<Boolean, FeatureModelDO>
