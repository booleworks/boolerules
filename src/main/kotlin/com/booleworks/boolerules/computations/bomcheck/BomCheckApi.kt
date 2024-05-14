// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.bomcheck

import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the bom check resource")
data class BomCheckRequest(
    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "A list of flags that defines whether non-unique, non-complete or dead position variants should be computed")
    val computationTypes: List<BomCheckType>,

    @field:Schema(description = "The list of positions")
    val positions: List<Position>
) : ComputationRequest

@Schema(description = "The position")
data class Position(
    @field:Schema(description = "The position ID")
    val positionId: String,

    @field:Schema(description = "The description of the position")
    val description: String,

    @field:Schema(description = "The formula for the position")
    val constraint: String,

    @field:Schema(description = "The position variants")
    val pvs: Set<PositionVariant>
)

@JsonInclude(Include.NON_NULL)
@Schema(description = "The position variants")
data class PositionVariant(
    @field:Schema(description = "The position variant ID")
    val pvId: String,

    @field:Schema(description = "The description of the position variant")
    val description: String,

    @field:Schema(description = "The formula of the position variant")
    val constraint: String
)

enum class BomCheckType { UNIQUENESS, COMPLETENESS, DEAD_PV }

typealias BomCheckResponse = ListComputationResponse<BomCheckAlgorithmsResult, PositionElementDO>

@Schema(description = "The position result")
data class PositionElementDO(
    @field:Schema(description = "The position ID")
    val positionId: String,

    @field:Schema(description = "The description of the position")
    val description: String,

    @field:Schema(description = "The formula for the position")
    val formula: String,

    @field:Schema(description = "A flag that indicates if the position is complete")
    val isComplete: Boolean,

    @field:Schema(description = "A flag that indicates if the position has position variants that are not unique")
    val hasNonUniquePVs: Boolean,

    @field:Schema(description = "A flag that indicates if the position has dead position variants")
    val hasDeadPvs: Boolean
) : Comparable<PositionElementDO> {
    override fun compareTo(other: PositionElementDO) = this.toString().compareTo(other.toString())
}

data class BomCheckAlgorithmsResult(val isComplete: Boolean, val hasNonUniquePVs: Boolean, val hasDeadPvs: Boolean) : Comparable<BomCheckAlgorithmsResult> {
    override fun compareTo(other: BomCheckAlgorithmsResult) = this.toString().compareTo(other.toString())
}

