// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser.internal

import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.rules.GroupType
import com.booleworks.prl.parser.PrlBooleanFeatureDefinition
import com.booleworks.prl.parser.PrlConstraint
import com.booleworks.prl.parser.PrlConstraintRule
import com.booleworks.prl.parser.PrlDefinitionRule
import com.booleworks.prl.parser.PrlEnumFeatureDefinition
import com.booleworks.prl.parser.PrlExclusionRule
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlFeatureDefinition
import com.booleworks.prl.parser.PrlForbiddenFeatureRule
import com.booleworks.prl.parser.PrlGroupRule
import com.booleworks.prl.parser.PrlIfThenElseRule
import com.booleworks.prl.parser.PrlInclusionRule
import com.booleworks.prl.parser.PrlIntFeatureDefinition
import com.booleworks.prl.parser.PrlMandatoryFeatureRule
import com.booleworks.prl.parser.PrlProperty
import com.booleworks.prl.parser.PrlRule
import com.booleworks.prl.parser.PrlRuleSet

internal class FeatureFactory {
    val map: MutableMap<String, PrlFeature> = HashMap()
    fun getOrAddFeature(featureCode: String) = map.computeIfAbsent(featureCode) { PrlFeature(featureCode) }
}

internal class PrlFeatureContent {
    var lnr = 0
    var vs = false
    var desc = ""
    private var props: MutableList<PrlProperty<*>> = ArrayList()

    fun addProperty(prop: PrlProperty<*>) {
        props.add(prop)
    }

    fun generateBoolFeature(code: String) = PrlBooleanFeatureDefinition(code, vs, desc, props, lnr)
    fun generateEnumFeature(code: String, values: List<String>) =
        PrlEnumFeatureDefinition(code, values, desc, props, lnr)

    fun generateIntFeature(code: String, intRange: IntRange) =
        PrlIntFeatureDefinition(code, intRange, desc, props, lnr)
}

internal class PrlRuleContent {
    var lnr = 0
    var id = ""
    var desc = ""
    private var props: MutableList<PrlProperty<*>> = ArrayList()

    fun addProperty(prop: PrlProperty<*>) {
        props.add(prop)
    }

    fun generateConstraintRule(constraint: PrlConstraint) = PrlConstraintRule(constraint, id, desc, props, lnr)
    fun generateInclusionRule(ifPart: PrlConstraint, thenPart: PrlConstraint) =
        PrlInclusionRule(ifPart, thenPart, id, desc, props, lnr)

    fun generateExclusionRule(ifPart: PrlConstraint, thenNotPart: PrlConstraint) =
        PrlExclusionRule(ifPart, thenNotPart, id, desc, props, lnr)

    fun generateDefinitionRule(feature: PrlFeature, definition: PrlConstraint) =
        PrlDefinitionRule(feature, definition, id, desc, props, lnr)

    fun generateGroupRule(type: GroupType, group: PrlFeature, content: List<PrlFeature>) =
        PrlGroupRule(type, group, content, id, desc, props, lnr)

    fun generateIfThenElseRule(ifPart: PrlConstraint, thenPart: PrlConstraint, elsePart: PrlConstraint) =
        PrlIfThenElseRule(ifPart, thenPart, elsePart, id, desc, props, lnr)

    fun generateForbiddenFeatureRule(feature: PrlFeature, enumValue: String?, intValueOrVersion: Int?) =
        PrlForbiddenFeatureRule(feature, enumValue, intValueOrVersion, id, desc, props, lnr)

    fun generateMandatoryFeatureRule(feature: PrlFeature, enumValue: String?, intValueOrVersion: Int?) =
        PrlMandatoryFeatureRule(feature, enumValue, intValueOrVersion, id, desc, props, lnr)
}

internal class PrlRuleSetContent {
    private var featureDefinitions: MutableList<PrlFeatureDefinition> = ArrayList()
    private var rules: MutableList<PrlRule> = ArrayList()

    fun addFeature(featureDefinition: PrlFeatureDefinition) {
        featureDefinitions.add(featureDefinition)
    }

    fun addRule(rule: PrlRule) {
        rules.add(rule)
    }

    fun generateRuleSet(lnr: Int) = PrlRuleSet(featureDefinitions, rules, lnr)
}
