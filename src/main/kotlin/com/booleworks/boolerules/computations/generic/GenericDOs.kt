// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.computations.generic.FeatureTypeDO.BOOLEAN
import com.booleworks.boolerules.computations.generic.FeatureTypeDO.ENUM
import com.booleworks.boolerules.computations.generic.FeatureTypeDO.INT
import com.booleworks.boolerules.computations.generic.FeatureTypeDO.VERSIONED_BOOLEAN
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.transpiler.RuleType
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A feature in a rule file")
data class FeatureDO(

    @field:Schema(description = "The feature code")
    val code: String,

    @field:Schema(description = "The full name of the feature including the module")
    val fullName: String,

    @field:Schema(description = "The feature type (BOOLEAN, VERSIONED_BOOLEAN, ENUM, INT)")
    val type: FeatureTypeDO,

    @field:Schema(description = "The value of a boolean feature", required = false)
    val booleanValue: Boolean? = null,

    @field:Schema(description = "The version of a versioned boolean feature", required = false)
    val version: Int? = null,

    @field:Schema(description = "The value of an enum feature", required = false)
    val enumValue: String? = null,

    @field:Schema(description = "The value of an int feature", required = false)
    val intValue: Int? = null
) : Comparable<FeatureDO> {
    override fun compareTo(other: FeatureDO) = this.type.compareTo(other.type).let {
        if (it != 0) {
            it
        } else {
            val fullNameComp = this.fullName.compareTo(other.fullName)
            if (fullNameComp != 0) {
                return fullNameComp
            } else {
                when (type) {
                    BOOLEAN -> 0
                    VERSIONED_BOOLEAN -> version!!.compareTo(other.version!!)
                    ENUM -> enumValue!!.compareTo(other.enumValue!!)
                    INT -> intValue!!.compareTo(other.intValue!!)
                }
            }
        }
    }

    override fun toString() = when (type) {
        BOOLEAN -> code
        VERSIONED_BOOLEAN -> "$code=$version"
        ENUM -> "$code=$enumValue"
        INT -> "$code=$intValue"
    }

    companion object {
        fun boolean(featureCode: String, fullName: String, value: Boolean) =
            FeatureDO(featureCode, fullName, BOOLEAN, booleanValue = value)

        fun boolean(featureCode: String, fullName: String, version: Int) =
            FeatureDO(featureCode, fullName, VERSIONED_BOOLEAN, version = version)

        fun enum(featureCode: String, fullName: String, value: String) =
            FeatureDO(featureCode, fullName, ENUM, enumValue = value)

        fun int(featureCode: String, fullName: String, value: Int) =
            FeatureDO(featureCode, fullName, INT, intValue = value)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A rule in a rule file")
data class RuleDO(

    @field:Schema(description = "The rule body as text")
    val rule: String,

    @field:Schema(description = "The module of the rule", required = false)
    val module: String? = null,

    @field:Schema(description = "The rule ID", required = false)
    val id: String? = null,

    @field:Schema(description = "The rule description", required = false)
    val description: String? = null,

    @field:Schema(description = "The line number in the rule file", required = false)
    val lineNumber: Int? = null,

    @field:Schema(description = "The list of properties of the rule", required = false)
    val properties: List<PropertyDO>? = null
) {
    companion object {
        fun fromRulesetRule(rule: AnyRule, slicingProperties: Collection<String>) = RuleDO(
            rule.headerLine(rule.module),
            rule.module.fullName,
            rule.id,
            rule.description,
            rule.lineNumber,
            rule.properties.values.map {
                PropertyDO(it.name, it.range.toString(), slicingProperties.contains(it.name))
            }
        )

        fun fromAdditionalConstraint(rule: ConstraintRule) = RuleDO(rule.headerLine(rule.module))

        fun fromInternalRule(ruleType: RuleType) = RuleDO(ruleType.description)
    }
}

@Schema(description = "A single property in a rule file")
data class PropertyDO(

    @field:Schema(description = "The name of the property")
    val property: String,

    @field:Schema(description = "The value of the property")
    val value: String,

    @field:Schema(description = "A flag whether the property is slicing or not")
    val slicing: Boolean
)

@Schema(description = "The feature type")
enum class FeatureTypeDO { BOOLEAN, VERSIONED_BOOLEAN, ENUM, INT }
