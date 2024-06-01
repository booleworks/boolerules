// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.config.ApplicationConfig
import com.booleworks.boolerules.persistence.Persistence
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.boolerules.rulefile.SlicingPropertyDO
import com.booleworks.boolerules.rulefile.toDO
import com.booleworks.boolerules.service.ApplicationVersion
import com.booleworks.kjobs.api.JobFrameworkBuilder
import com.booleworks.prl.model.AnySlicingPropertyDefinition
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.BooleanSliceSelection
import com.booleworks.prl.model.slices.DateSliceSelection
import com.booleworks.prl.model.slices.EnumSliceSelection
import com.booleworks.prl.model.slices.IntSliceSelection
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Collections
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * A computation request.  The request must always contain a rule file ID.
 * This ID is used to fetch the rule file for this computation from the
 * internal storage.
 *
 * @property ruleFileId the ID of the rule file for this computation
 * @property sliceSelection a slice filter for the computation
 */
interface ComputationRequest {
    val ruleFileId: String
    val sliceSelection: MutableList<PropertySelectionDO>
    val additionalConstraints: List<String>

    fun validateAndAugmentSliceSelection(model: PrlModel, allowedSliceTypes: Set<SliceTypeDO>) {
        val sliceMap = sliceSelection.associateBy { it.property }
        for (def in model.propertyStore.allDefinitions()) {
            val propertySelection = sliceMap[def.name]
            if (propertySelection == null) {
                if (!allowedSliceTypes.contains(SliceTypeDO.ANY)) {
                    error(
                        "Property '${def.name}' has no selection and defaults to slice type " +
                                "ANY which is not allowed for this computation"
                    )
                }
                sliceSelection.add(PropertySelectionDO(def.name, def.propertyType.toDO(), rangeFromDef(def)))
            } else {
                if (!allowedSliceTypes.contains(propertySelection.sliceType)) {
                    error(
                        "Selection for property '${def.name}' has slice type ${propertySelection.sliceType} " +
                                "which is not allowed for this computation"
                    )
                }
                when (def) {
                    is SlicingBooleanPropertyDefinition -> if (propertySelection.range.booleanValues.isNullOrEmpty()) {
                        propertySelection.range.booleanValues = def.values
                    }
                    is SlicingEnumPropertyDefinition -> if (propertySelection.range.enumValues.isNullOrEmpty()) {
                        propertySelection.range.enumValues = def.values
                    }
                    is SlicingIntPropertyDefinition -> if (!propertySelection.range.intValues.isNullOrEmpty()) {
                        check(propertySelection.range.intMin == null && propertySelection.range.intMax == null) {
                            "Set values and a range of int property '${propertySelection.property}' at the same time"
                        }
                    } else {
                        if (propertySelection.range.intMin == null) {
                            propertySelection.range.intMin = def.min()
                        }
                        if (propertySelection.range.intMax == null) {
                            propertySelection.range.intMax = def.max()
                        }
                    }
                    is SlicingDatePropertyDefinition -> if (!propertySelection.range.dateValues.isNullOrEmpty()) {
                        check(propertySelection.range.dateMin == null && propertySelection.range.dateMax == null) {
                            "Set values and a range of date property '${propertySelection.property}' at the same time"
                        }
                    } else {
                        if (propertySelection.range.dateMin == null) {
                            propertySelection.range.dateMin = def.min()
                        }
                        if (propertySelection.range.dateMax == null) {
                            propertySelection.range.dateMax = def.max()
                        }
                    }
                }
            }
        }
    }

    fun rangeFromDef(def: AnySlicingPropertyDefinition) = when (def) {
        is SlicingBooleanPropertyDefinition -> PropertyRangeDO(booleanValues = def.values)
        is SlicingDatePropertyDefinition -> PropertyRangeDO(dateMin = def.min(), dateMax = def.max())
        is SlicingEnumPropertyDefinition -> PropertyRangeDO(enumValues = def.values)
        is SlicingIntPropertyDefinition -> PropertyRangeDO(intMin = def.min(), intMax = def.max())
    }

    fun modelSliceSelection(): List<AnySliceSelection> = sliceSelection.map { it.toModelDS() }

    fun splitProperties() = modelSliceSelection().filter { it.sliceType == SliceType.SPLIT }
        .map { it.property.name }
        .toSet()

    fun hasAnyOrAllSplits() = modelSliceSelection().any {
        it.sliceType == SliceType.ALL || it.sliceType == SliceType.ANY
    }
}

/**
 * Super class for computation responses of single and list computations.
 *
 * @param MAIN the type of the main result of this computation
 */
sealed interface ComputationResponse<MAIN>

/**
 * The response for a single computation request.
 * This response is used for computations which yield a single result for
 * a computation on a rule set, e.g. a consistency check or a model count.
 *
 * @param MAIN the type of the main result of this computation
 */
@Schema(description = "The response for a single computation request")
data class SingleComputationResponse<MAIN>(
    val status: ComputationStatus,

    @field:Schema(
        description = "The list of results.  " +
                "For each different main result there is exactly one result in this list"
    )
    val results: List<SliceComputationResult<MAIN>>
) : ComputationResponse<MAIN>

/**
 * The response for a list computation request.
 * This response is used for computations which yield a list of results for
 * a computation on a rule set, e.g. a backbone computation or a model
 * enumeration.
 *
 * @param MAIN the type of the main result of this computation
 * @param ELEMENT the type of the list elements
 */
data class ListComputationResponse<MAIN, ELEMENT>(
    val status: ComputationStatus,

    @field:Schema(
        description = "The list of results.  " +
                "For each result element there is exactly one result in this list"
    )
    val results: List<ComputationElementResult<MAIN, ELEMENT>>
) : ComputationResponse<MAIN>

/**
 * The result for a single element of a list computation request.
 *
 * @param MAIN the type of the main result of this computation
 * @param ELEMENT the type of the list elements
 */
data class ComputationElementResult<MAIN, ELEMENT>(
    val element: ComputationElement<ELEMENT>,

    @field:Schema(
        description = "The list of results.  " +
                "For each different main result there is exactly one result in this list"
    )
    val results: List<SliceComputationResult<MAIN>>
)

/**
 * A single element of a list computation request.
 *
 * @param ELEMENT the type of the list elements
 */
data class ComputationElement<ELEMENT>(
    @field:Schema(description = "The ID of this element")
    var id: Int,

    @field:Schema(description = "The element itself")
    val content: ELEMENT,
)

/**
 * A single result for a computation.  Such a result gathers all results
 * for a single computation with the same MAIN result.
 *
 * @param MAIN the type of the main result of this computation
 */
@Schema(description = "A single result for a computation")
data class SliceComputationResult<MAIN>(
    @field:Schema(description = "The ID of this result")
    var id: Int,

    @field:Schema(
        description = "The main result of the computation which " +
                "is used for merging the split results"
    )
    val result: MAIN,

    @field:Schema(
        description = "The list of split results for this result. " +
                "All these split results have the same main result"
    )
    val slices: List<SliceDO>
)

/**
 * Further computation details for a single sub-slice of a split result.
 *
 * @param DETAIL the type of the details for each result
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Further computation details for a single sub-slice of a split result")
data class SplitComputationDetail<DETAIL : ComputationDetail>(

    @field:Schema(description = "This detail's main result ID")
    var resultId: Int,

    val slice: SliceDO,

    @field:Schema(description = "The computation detail")
    val detail: DETAIL,

    @field:Schema(description = "This detail's element ID if it was a list computation, null otherwise")
    var elementId: Int? = null
) {
    constructor(internalResult: InternalResult<*, DETAIL>, splitSlices: Set<String>) :
            this(-1, internalResult.slice.toDO().project(splitSlices), internalResult.extractDetails(), null)
}

/**
 * An interface for computation details.  A class implementing this interface
 * can hold further details besides the MAIN result.  E.g. a consistency check
 * could have a satisfiable configuration or an explanation for a conflict in
 * its computation details.
 */
interface ComputationDetail

/**
 * The internal result of a single computation.  Since we want to decouple
 * the external API from the internal objects, a computation always has an
 * internal result type which is then translated in the external result
 * structure.
 *
 * It is expected that both, the MAIN and the DETAIL result can be compared
 * with `equals`.
 *
 * @param slice the slice for the internal result
 * @param MAIN the type of the main result of this computation
 * @param DETAIL the type of the details for each result
 */
abstract class InternalResult<MAIN, DETAIL>(open val slice: Slice) {
    abstract fun extractMainResult(): MAIN
    abstract fun extractDetails(): DETAIL
}

abstract class InternalListResult<MAIN, DETAIL>(override val slice: Slice) : InternalResult<MAIN, DETAIL>(slice) {
    override fun extractMainResult() =
        throw UnsupportedOperationException("Should never be called for a list computation")

    override fun extractDetails() =
        throw UnsupportedOperationException("Should never be called for a list computation")
}

@Schema(
    description = "A single slice. A slice consists of a list of slicing properties " +
            "with their assignment and their slice type."
)
data class SliceDO(
    @field:Schema(description = "The slicing properties of this slice")
    val content: List<SlicingPropertyDO>
) {
    fun uniqueKey() = content.sortedBy { it.name }.joinToString(":") { "${it.name}:${it.range.key()}" }
    fun searchKey() = content.sortedBy { it.name }.joinToString(":") { "*${it.name}:${it.range.key()}*" }
    fun project(splitProperties: Set<String>) = SliceDO(content.filter { it.name in splitProperties })

    companion object {
        fun fromSelection(propertySelection: List<PropertySelectionDO>) =
            SliceDO(propertySelection.map { SlicingPropertyDO(it.property, it.propertyType, it.range) })
    }
}

fun Slice.toDO() =
    SliceDO(this.allProperties().map { SlicingPropertyDO(it.name, it.propertyType.toDO(), it.range.toDO()) })

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A selection for a single slicing property")
data class PropertySelectionDO(

    @field:Schema(description = "The name of the slicing property")
    val property: String,

    @field:Schema(description = "The type of the slicing property")
    val propertyType: PropertyTypeDO,

    @field:Schema(description = "An optional filter for the property", required = false)
    val range: PropertyRangeDO,

    @field:Schema(description = "The slice type of the property", defaultValue = "ANY")
    val sliceType: SliceTypeDO = SliceTypeDO.ANY
) {
    fun toModelDS(): AnySliceSelection = when (propertyType) {
        PropertyTypeDO.BOOLEAN -> BooleanSliceSelection(property, range.toBooleanRange(), sliceType.toModelDS())
        PropertyTypeDO.INT -> IntSliceSelection(property, range.toIntRange(), sliceType.toModelDS())
        PropertyTypeDO.DATE -> DateSliceSelection(property, range.toDateRange(), sliceType.toModelDS())
        PropertyTypeDO.ENUM -> EnumSliceSelection(property, range.toEnumRange(), sliceType.toModelDS())
    }
}

@Schema(description = "The slice type")
enum class SliceTypeDO {
    ALL,
    ANY,
    SPLIT;

    fun toModelDS() = when (this) {
        ALL -> SliceType.ALL
        ANY -> SliceType.ANY
        SPLIT -> SliceType.SPLIT
    }
}

@Schema(description = "The status of a single computation.")
data class ComputationStatus(

    @field:Schema(description = "A flag whether the computation was successful or not")
    val success: Boolean,

    @field:Schema(description = "The Job ID within the kjobs framework")
    val jobId: String,

    @field:Schema(description = "The Rule file ID used for this computation")
    val ruleFileId: String,

    @field:Schema(description = "Statistics for this computation")
    val statistics: ComputationStatistics,

    @field:Schema(description = "The type of the computation")
    val computationVariant: ComputationVariant,

    @Transient
    val sliceSets: List<List<SliceDO>>,

    @field:Schema(description = "A (potential empty) list of error messages for this computation")
    val errors: List<String> = mutableListOf(),

    @field:Schema(description = "A (potential empty) list of warning messages for this computation")
    val warnings: List<String> = mutableListOf(),

    @field:Schema(description = "A (potential empty) list of info messages for this computation")
    val infos: List<String> = mutableListOf(),

    @field:Schema(description = "The application version of BooleRules")
    val version: String = ApplicationVersion.get(),
)

enum class ComputationVariant { SINGLE, LIST }

@Schema(description = "The statistics of a single computation.")
data class ComputationStatistics(

    @field:Schema(description = "The computation time in milliseconds")
    val computationTimeInMs: Long,

    @field:Schema(description = "The number of slices for the computation")
    val numberOfSlices: Int,

    @field:Schema(description = "The number of slice computations necessary for the computation")
    val numberOfSliceComputations: Int,

    @field:Schema(description = "The average computation time in milliseconds per slice")
    val averageSliceComputationTimeInMs: Long
)

enum class OptimizationType { MIN, MAX }

@Suppress("FunctionName")
internal fun ComputationStatus(ruleFileId: String, jobId: String, computationVariant: ComputationVariant) =
    ComputationStatusBuilder(ruleFileId, jobId, computationVariant)

internal class ComputationStatusBuilder(
    private val ruleFileId: String,
    val jobId: String,
    private val computationVariant: ComputationVariant
) {
    private val startTime = System.currentTimeMillis()
    private val errors: MutableList<String> = Collections.synchronizedList(ArrayList())
    private val warnings: MutableList<String> = Collections.synchronizedList(ArrayList())
    private val infos: MutableList<String> = Collections.synchronizedList(ArrayList())
    internal var sliceSets: List<List<SliceDO>> = mutableListOf()
    internal var numberOfSlices: Int = 0
    internal var numberOfSliceComputations: Int = 0

    fun addError(message: String) {
        errors.add(message)
    }

    fun addWarning(message: String) {
        warnings.add(message)
    }

    fun addInfo(message: String) {
        infos.add(message)
    }

    fun successful() = errors.isEmpty()

    fun build(): ComputationStatus {
        val computationTime = System.currentTimeMillis() - startTime
        val avgTime = if (numberOfSliceComputations > 0) computationTime / numberOfSliceComputations else 0
        val statistics = ComputationStatistics(computationTime, numberOfSlices, numberOfSliceComputations, avgTime)
        return ComputationStatus(
            errors.isEmpty(),
            jobId,
            ruleFileId,
            statistics,
            computationVariant,
            sliceSets,
            errors,
            warnings,
            infos
        )
    }

    override fun toString() = "ComputationStatusBuilder(errors=$errors, warnings=$warnings, infos=$infos)"
}

internal inline fun <
        reified MAIN,
        reified REQUEST : ComputationRequest,
        reified RESPONSE : ComputationResponse<MAIN>,
        reified ELEMENT
        > JobFrameworkBuilder.addComputationApi(
    computation: ComputationType<REQUEST, RESPONSE, MAIN, *, ELEMENT>,
    outerRoute: Route
) {
    addApi(
        computation.path,
        outerRoute.route(computation.path) {},
        Persistence.computation.dataPersistence(computation),
        { call.receive<REQUEST>() },
        { call.respond(it) },
        computation.computationFunction,
    ) {
        apiConfig {
            generateApiDocs(computation.docs)
            inputValidation = ::validateInput
        }
        jobConfig {
            timeoutComputation = { _, _ -> ApplicationConfig.computationTimeout.minutes }
            maxRestarts = ApplicationConfig.maxRestarts
        }
        synchronousResourceConfig {
            enabled = true
            checkInterval = ApplicationConfig.syncCheckInterval.milliseconds
            maxWaitingTime = ApplicationConfig.syncMaxWaitingTime.seconds
        }
    }
}

private fun <REQUEST : ComputationRequest> validateInput(input: REQUEST): List<String> =
    if (input.ruleFileId.isBlank()) listOf("Computation Request must include a rulefile ID")
    else listOf()
