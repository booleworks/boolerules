// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.compiler.FeatureStore
import com.booleworks.prl.compiler.PropertyStore
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.evaluateProperties
import com.booleworks.prl.parser.PrlVersion

@Suppress("UNCHECKED_CAST")
data class PrlModel(
    val header: PrlModelHeader,
    val featureStore: FeatureStore,
    val rules: List<AnyRule>,
    val propertyStore: PropertyStore
) {
    private val featureMap: Map<String, Feature> by lazy {
        featureStore.allDefinitions().associateBy({ it.code }, { it.feature })
    }

    fun <T : Feature> getFeature(featureCode: String): T {
        val feature = featureMap[featureCode] ?: throw IllegalArgumentException("Could not find feature '$featureCode'")
        return feature as T
    }

    fun features() = booleanFeatures() + enumFeatures() + intFeatures()
    fun booleanFeatures() = featureStore.booleanFeatures()
    fun enumFeatures() = featureStore.enumFeatures()
    fun intFeatures() = featureStore.intFeatures()
    fun enumValues() = featureStore.enumDefinitions().associate { it.feature to it.values }
    fun containsBooleanFeatures() = featureStore.containsBooleanFeatures()
    fun containsEnumFeatures() = featureStore.containsEnumFeatures()
    fun containsIntFeatures() = featureStore.containsIntFeatures()
    fun evaluate(assignment: FeatureAssignment) = rules.all { it.evaluate(assignment) }
    fun evaluateEachRule(assignment: FeatureAssignment) = rules.associateWith { it.evaluate(assignment) }
    fun restrict(assignment: FeatureAssignment) = rules.map { it.restrict(assignment) }
    fun syntacticSimplify() = rules.map { it.syntacticSimplify() }
    fun rules(selections: List<AnySliceSelection>) = rules.filter { evaluateProperties(it.properties, selections) }
    fun rules(slice: Slice) = rules.filter { evaluateProperties(it.properties, slice.selector()) }
    fun featureDefinitions(selections: List<AnySliceSelection>) =
        featureStore.allDefinitions().filter { evaluateProperties(it.properties, selections) }

    fun featureDefinitions(slice: Slice) =
        featureStore.allDefinitions().filter { evaluateProperties(it.properties, slice.selector()) }

    fun propertyDefinition(name: String) = propertyStore.definition(name)

    fun toRuleFile() = RuleFile(header, toRuleSet(), propertyStore.slicingPropertyDefinitions)

    private fun toRuleSet(): RuleSet {
        val groups = featureStore.groups
        return RuleSet(featureStore.allDefinitions().filterNot { groups.contains(it.feature) }, rules)
    }
}

data class PrlModelHeader(val version: PrlVersion, val properties: Map<String, AnyProperty>) {
    fun stripProperties() = PrlModelHeader(version, mapOf())
}
