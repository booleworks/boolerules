// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.packagesolving

import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the package solving resource")
data class PackageSolvingRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "The currently installed packages")
    val currentInstallation: List<String> = listOf(),

    @field:Schema(description = "The list of packages which should be installed")
    val install: List<String> = listOf(),

    @field:Schema(description = "The list of packages which should be removed")
    val remove: List<String> = listOf(),

    @field:Schema(description = "Update a maximum number of packages to their latest version")
    val update: Boolean = false,
) : ComputationRequest {
    override fun considerConstraints() = currentInstallation + install + remove
}

@Schema(description = "The result of the package solving request")
data class PackageSolvingResult(

    @field:Schema(description = "The removed features")
    val removedFeatures: List<VersionedFeature>,

    @field:Schema(description = "The new features")
    val newFeatures: List<VersionedFeature>,

    @field:Schema(description = "The changed features")
    val changedFeatures: List<VersionedFeature>,
)

@Schema(description = "A features which version was changed")
data class VersionedFeature(

    @field:Schema(description = "The name of the feature")
    val feature: String,

    @field:Schema(description = "The old version")
    val versionOld: Int,

    @field:Schema(description = "The new version")
    val versionNew: Int,
)

typealias PackageSolvingResponse = SingleComputationResponse<PackageSolvingResult>
