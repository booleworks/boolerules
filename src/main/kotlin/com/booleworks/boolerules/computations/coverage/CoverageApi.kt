package com.booleworks.boolerules.computations.coverage

import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A request to the coverage computation resource")
data class CoverageRequest(

    @field:Schema(description = "The ID of the rule file to use for the computation")
    override val ruleFileId: String,

    @field:Schema(description = "The list of slice filters for the computation")
    override val sliceSelection: MutableList<PropertySelectionDO>,

    @field:Schema(description = "The list of additional restrictions for the computation")
    override val additionalConstraints: List<String> = listOf(),

    @field:Schema(description = "The set of constraints which must be covered")
    val constraintsToCover: List<String>,

    @field:Schema(description = "By default every constraint most be covered (potentially together with a bunch of other constraints). Setting this value to true enforces, that all pairs of the given constraints must be covered. So for constraints A, B, and C, the combinations A+B, A+C, and B+C must be covered.")
    val pairwiseCover: Boolean = false
) : ComputationRequest

@Schema(description = "The details for a coverage computation")
data class CoverageDetail(

    @field:Schema(description = "The list of configurations covering the constraints")
    val configurations: List<CoveringConfiguration>,
) : ComputationDetail

@Schema(description = "A configuration and the constraints it covers")
data class CoveringConfiguration(

    @field:Schema(description = "The configuration")
    val configuration: FeatureModelDO,

    @field:Schema(description = "The constraints covered by the configuration")
    val coveredConstraints: List<String>
)

@Schema(description = "The main result of the coverage computation")
data class CoverageMainResult(

    @field:Schema(description = "The number of configurations required to cover all constraints (excluding the ones which cannot be covered at all)")
    val requiredConfigurations: Int,

    @field:Schema(description = "The number of constraints that cannot be covered since the constraint is not buildable")
    val uncoverableConstraints: Int,
)

typealias CoverageResponse = SingleComputationResponse<CoverageMainResult>
