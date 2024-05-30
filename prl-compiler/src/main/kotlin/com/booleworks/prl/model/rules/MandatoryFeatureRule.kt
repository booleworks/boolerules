// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.constraints.enumEq
import com.booleworks.prl.model.constraints.enumVal
import com.booleworks.prl.model.constraints.intEq
import com.booleworks.prl.model.constraints.versionEq
import com.booleworks.prl.model.datastructures.FeatureRenaming
import java.util.Objects


class MandatoryFeatureRule internal constructor(
    override val feature: Feature,
    override val enumValue: String?,
    override val intValueOrVersion: Int?,
    override val id: String,
    override val description: String,
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : FeatureRule<MandatoryFeatureRule>(
    feature,
    enumValue,
    intValueOrVersion,
    id,
    description,
    properties,
    lineNumber
) {

    private constructor(rule: AnyRule, feature: Feature, enumValue: String?, intValueOrVersion: Int?)
            : this(feature, enumValue, intValueOrVersion, rule.id, rule.description, rule.properties, rule.lineNumber)

    constructor(
        feature: BooleanFeature,
        id: String = "",
        description: String = "",
        properties: Map<String, AnyProperty> = mapOf(),
        lineNumber: Int? = null
    ) : this(feature, null, null, id, description, properties, lineNumber)

    constructor(
        feature: BooleanFeature,
        version: Int,
        id: String = "",
        description: String = "",
        properties: Map<String, AnyProperty> = mapOf(),
        lineNumber: Int? = null
    ) : this(feature, null, version, id, description, properties, lineNumber)

    constructor(
        feature: IntFeature,
        value: Int,
        id: String = "",
        description: String = "",
        properties: Map<String, AnyProperty> = mapOf(),
        lineNumber: Int? = null
    ) : this(feature, null, value, id, description, properties, lineNumber)

    constructor(
        feature: EnumFeature,
        value: String,
        id: String = "",
        description: String = "",
        properties: Map<String, AnyProperty> = mapOf(),
        lineNumber: Int? = null
    ) : this(feature, value, null, id, description, properties, lineNumber)

    override fun rename(renaming: FeatureRenaming) =
        renameFeature(renaming).let {
            if (it != feature) MandatoryFeatureRule(
                this,
                it,
                enumValue,
                intValueOrVersion
            ) else this
        }

    override fun stripProperties() =
        MandatoryFeatureRule(feature, enumValue, intValueOrVersion, id, description, mapOf(), lineNumber)

    override fun stripMetaInfo() =
        MandatoryFeatureRule(feature, enumValue, intValueOrVersion, "", "", properties, lineNumber)

    override fun stripAll() =
        MandatoryFeatureRule(feature, enumValue, intValueOrVersion, "", "", mapOf(), lineNumber)

    override fun hashCode() = Objects.hash(super.hashCode(), feature, enumValue, intValueOrVersion)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) =
        other is MandatoryFeatureRule && feature == other.feature && enumValue == other.enumValue &&
                intValueOrVersion == other.intValueOrVersion

    override fun generateConstraint(feature: Feature, enumValue: String?, intValueOrVersion: Int?) = when (feature) {
        is VersionedBooleanFeature -> versionEq(feature, intValueOrVersion!!)
        is BooleanFeature -> feature
        is EnumFeature -> enumEq(feature, enumVal(enumValue!!))
        is IntFeature -> intEq(feature, intValueOrVersion!!)
    }
}
