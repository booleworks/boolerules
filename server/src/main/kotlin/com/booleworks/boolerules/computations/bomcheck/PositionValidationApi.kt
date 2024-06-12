// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.bomcheck

import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the bom check resource")
data class PositionValidationRequest(
    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "A list of flags that defines whether non-unique, non-complete or dead position variants should be computed")
    val computationTypes: List<BomCheckType>,

    @field:Schema(description = "The position to validate")
    val position: Position
) : ComputationRequest {
    override fun considerConstraints() = mutableListOf<String>().apply {
        this.add(position.constraint)
        this.addAll(position.positionVariants.map { it.constraint })
    }
}

@Schema(description = "The position")
data class Position(
    @field:Schema(description = "The position ID")
    val positionId: String,

    @field:Schema(description = "The description of the position")
    val description: String,

    @field:Schema(description = "The formula for the position")
    val constraint: String,

    @field:Schema(description = "The position variants")
    val positionVariants: List<PositionVariant>
)

@JsonInclude(Include.NON_NULL)
@Schema(description = "The position variants")
data class PositionVariant(
    @field:Schema(description = "The position variant ID")
    val positionVariantId: String,

    @field:Schema(description = "The description of the position variant")
    val description: String,

    @field:Schema(description = "The formula of the position variant")
    val constraint: String
)

enum class BomCheckType { UNIQUENESS, COMPLETENESS, DEAD_PV }

typealias PositionValidationResponse = SingleComputationResponse<PositionValidationResult>

@Schema(description = "The position result")
data class PositionValidationResult(
    @field:Schema(description = "The position ID")
    val positionId: String,

    @field:Schema(description = "The description of the position")
    val description: String,

    @field:Schema(description = "The formula for the position")
    val constraint: String,

    @field:Schema(description = "A flag that indicates if the position is complete")
    val isComplete: Boolean,

    @field:Schema(description = "A flag that indicates if the position has position variants that are not unique")
    val hasNonUniquePVs: Boolean,

    @field:Schema(description = "A flag that indicates if the position has dead position variants")
    val hasDeadPvs: Boolean
) : Comparable<PositionValidationResult> {
    override fun compareTo(other: PositionValidationResult) = this.toString().compareTo(other.toString())
}

@JsonInclude(Include.NON_NULL)
@Schema(description = "The details for a bom check computation")
data class PositionValidationDetail(
    val deadPVs: List<PositionVariant>,
    val nonUniquePvs: List<NonUniquePvsDetail>,
    val nonComplete: FeatureModelDO?
) : ComputationDetail

@JsonInclude(Include.NON_NULL)
@Schema(description = "The details for a combination of two non unique position variants with example configuration")
data class NonUniquePvsDetail(
    @field:Schema(description = "First of the non unique position variants")
    val firstPositionVariant: PositionVariant?,

    @field:Schema(description = "Second of the non unique position variants")
    val secondPositionVariant: PositionVariant?,

    @field:Schema(description = "An example configuration for duplicate position variants")
    val exampleConfiguration: FeatureModelDO?,
)

