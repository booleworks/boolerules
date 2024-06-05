// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.rulefile

import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.PropertyRange
import com.booleworks.prl.model.PropertyType
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "The upload summary for a rule file")
data class UploadSummaryDO(

    @field:Schema(description = "The generated rule file ID")
    val id: String,

    @field:Schema(description = "The file name of the uploaded rule file")
    val fileName: String,

    @field:Schema(description = "The upload timestamp")
    val timestamp: LocalDateTime,

    @field:Schema(description = "The size in the storage in bytes for the uploaded rule file")
    val size: Int,

    @field:Schema(description = "The number of rules of the uploaded rule file")
    val numberOfRules: Int,

    @field:Schema(description = "The number of features of the uploaded rule file")
    val numberOfFeatures: Int,

    @field:Schema(description = "A flag whether the uploaded rule file has boolean features")
    val hasBooleanFeatures: Boolean,

    @field:Schema(description = "A flag whether the uploaded rule file has enum features")
    val hasEnumFeatures: Boolean,

    @field:Schema(description = "A flag whether the uploaded rule file has int features")
    val hasIntFeatures: Boolean,

    @field:Schema(description = "The list of slicing properties of the uploaded rule file")
    val slicingProperties: List<SlicingPropertyDO>,

    @field:Schema(description = "The list of all full feature names in the uploaded rule file")
    val features: Set<String>,

    @field:Schema(
        description = "A (potential empty) list of error messages during uploading and " +
                "compiling the rule file"
    )
    val errors: List<String> = listOf(),

    @field:Schema(
        description = "A (potential empty) list of warning messages during uploading and " +
                "compiling the rule file"
    )
    val warnings: List<String> = listOf(),

    @field:Schema(
        description = "A (potential empty) list of info messages during uploading and " +
                "compiling the rule file"
    )
    val infos: List<String> = listOf(),
) {
    fun hasErrors() = errors.isNotEmpty()
}

@Schema(description = "A slicing property with its property type and its range")
data class SlicingPropertyDO(
    @field:Schema(description = "The name of this slicing property") val name: String,
    val type: PropertyTypeDO, val range: PropertyRangeDO
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "A property range.  A single range can be of type boolean, enum, int, or date and " +
            "can be a list of values or an interval."
)
data class PropertyRangeDO(

    @field:Schema(
        description = "A set of boolean values for a boolean property",
        required = false
    ) var booleanValues: Set<Boolean>? = null,

    @field:Schema(
        description = "A set of enum values for a enum property",
        required = false
    ) var enumValues: Set<String>? = null,

    @field:Schema(
        description = "A set of int values for an int property",
        required = false
    ) var intValues: Set<Int>? = null,

    @field:Schema(
        description = "The lower bound for an interval of an int property",
        required = false
    ) var intMin: Int? = null,

    @field:Schema(
        description = "The upper bound for an interval of an int property",
        required = false
    ) var intMax: Int? = null,

    @field:Schema(
        description = "A set of date values for a date property",
        required = false
    ) var dateValues: Set<LocalDate>? = null,

    @field:Schema(
        description = "The lower bound for an interval of a date property",
        required = false
    ) var dateMin: LocalDate? = null,

    @field:Schema(
        description = "The upper bound for an interval of a date property",
        required = false
    ) var dateMax: LocalDate? = null
) {
    constructor(def: SlicingBooleanPropertyDefinition) : this(booleanValues = def.values)
    constructor(def: SlicingIntPropertyDefinition) : this(
        intValues = def.computeRelevantValues(),
        intMin = def.min(),
        intMax = def.max()
    )

    constructor(def: SlicingDatePropertyDefinition) : this(
        dateValues = def.computeRelevantValues(),
        dateMin = def.min(),
        dateMax = def.max()
    )

    constructor(def: SlicingEnumPropertyDefinition) : this(enumValues = def.values.toSet())

    @JsonIgnore
    fun isContinuous(): Boolean =
        dateMin != null || dateMax != null || intMin != null || intMax != null

    fun toBooleanRange(): BooleanRange = if (booleanValues != null) {
        BooleanRange.list(booleanValues as Set<Boolean>)
    } else {
        BooleanRange.list()
    }

    fun toIntRange(): IntRange = if (intValues != null) {
        IntRange.list(intValues as Set<Int>)
    } else if (intMin != null || intMax != null) {
        IntRange.interval(intMin ?: Int.MIN_VALUE, intMax ?: Int.MAX_VALUE)
    } else {
        IntRange.list()
    }

    fun toDateRange(): DateRange = if (dateValues != null) {
        DateRange.list(dateValues as Set<LocalDate>)
    } else if (dateMin != null || dateMax != null) {
        DateRange.interval(dateMin ?: LocalDate.MIN, dateMax ?: LocalDate.MAX)
    } else {
        DateRange.list()
    }

    fun toEnumRange(): EnumRange = if (enumValues != null) {
        EnumRange.list(enumValues as Set<String>)
    } else {
        EnumRange.list()
    }

    fun key(): String = if (booleanValues != null) {
        booleanValues!!.first().toString()
    } else if (intValues != null) {
        intValues!!.first().toString()
    } else if (dateValues != null) {
        dateValues!!.first().toString()
    } else if (enumValues != null) {
        enumValues!!.first()
    } else {
        error("PropertyDO with no unique value: $this")
    }
}

fun PropertyRange<*>.toDO() = when (this) {
    is BooleanRange -> PropertyRangeDO(booleanValues = this.allValues())
    is EnumRange -> PropertyRangeDO(enumValues = this.allValues().toSet())
    is IntRange ->
        if (this.isDiscrete()) {
            PropertyRangeDO(intValues = this.allValues())
        } else {
            PropertyRangeDO(intMin = this.first(), intMax = this.last())
        }
    is DateRange ->
        if (this.isDiscrete()) {
            PropertyRangeDO(dateValues = this.allValues())
        } else {
            PropertyRangeDO(dateMin = this.first(), dateMax = this.last())
        }
}

@Schema(description = "The type of a property")
enum class PropertyTypeDO {
    BOOLEAN, INT, DATE, ENUM;
}

fun PropertyType.toDO(): PropertyTypeDO = when (this) {
    PropertyType.BOOL -> PropertyTypeDO.BOOLEAN
    PropertyType.INT -> PropertyTypeDO.INT
    PropertyType.DATE -> PropertyTypeDO.DATE
    PropertyType.ENUM -> PropertyTypeDO.ENUM
}
