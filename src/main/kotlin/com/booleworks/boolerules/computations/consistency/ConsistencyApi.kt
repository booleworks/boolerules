// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.RuleDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the consistency check resource")
data class ConsistencyRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "A flag whether all details should be computed eagerly.")
    val computeAllDetails: Boolean = false
) : ComputationRequest

@JsonInclude(Include.NON_NULL)
@Schema(description = "The details for a concistency computation")
data class ConsistencyDetail(

    @field:Schema(
        description = "If the rule file is consistent, an example configuration " +
                "which satisfies all rules is computed"
    )
    val exampleConfiguration: FeatureModelDO?,

    @field:Schema(
        description = "If the rule file is not consistent and an explanation was requested " +
                "in the request, a list of rules responsible for the inconsistency is computed"
    )
    val explanation: List<RuleDO>?
) : ComputationDetail

typealias ConsistencyResponse = SingleComputationResponse<Boolean>
