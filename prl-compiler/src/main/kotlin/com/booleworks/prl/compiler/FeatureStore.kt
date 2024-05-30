// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.compiler

import com.booleworks.prl.compiler.PropertyStore.Companion.uniqueSlices
import com.booleworks.prl.model.AnyFeatureDef
import com.booleworks.prl.model.AnySlicingPropertyDefinition
import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.FeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.Theory
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.parser.PrlBooleanFeatureDefinition
import com.booleworks.prl.parser.PrlEnumFeatureDefinition
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlFeatureDefinition
import com.booleworks.prl.parser.PrlIntFeatureDefinition

data class FeatureStore internal constructor(
    internal val booleanFeatures: MutableMap<String, MutableList<AnyFeatureDef>> = mutableMapOf(),
    internal val intFeatures: MutableMap<String, MutableList<AnyFeatureDef>> = mutableMapOf(),
    internal val enumFeatures: MutableMap<String, MutableList<AnyFeatureDef>> = mutableMapOf(),
    internal val groups: MutableList<BooleanFeature> = mutableListOf(),
    internal val nonUniqueFeatures: MutableSet<String> = mutableSetOf()
) {
    /**
     * Adds a given feature definition to the feature store.
     * Is the definition already present, a compiler error is returned.
     *
     * A feature definition is uniquely determined by its name -
     * its feature type is not relevant for uniqueness.
     */
    internal fun addDefinition(
        definition: PrlFeatureDefinition,
        state: CompilerState,
        slicingProperties: MutableMap<String, AnySlicingPropertyDefinition> = mutableMapOf(),
        isGroup: Boolean = false
    ) {
        val map = mapForType(definition)
        addDefinitionToMap(definition, slicingProperties, map, isGroup, state)
    }

    internal fun generateTheoryMap(
        features: Collection<PrlFeature>,
        state: CompilerState
    ): Map<PrlFeature, Theory> {
        val map = mutableMapOf<PrlFeature, Theory>()
        for (feature in features) {
            val definitions = findMatchingDefinitions(feature.featureCode)
            if (definitions.isEmpty()) {
                state.addError("No feature definition found for ${feature.featureCode}")
            } else {
                map[feature] = when (val def = definitions.first()) {
                    is BooleanFeatureDefinition -> if (def.versioned) Theory.VERSIONED_BOOL else Theory.BOOL
                    is EnumFeatureDefinition -> Theory.ENUM
                    is IntFeatureDefinition -> Theory.INT
                }
            }
        }
        return map
    }

    internal fun size() = booleanFeatures.map { it.value.size }.sum() + enumFeatures.map { it.value.size }
        .sum() + intFeatures.map { it.value.size }.sum()

    fun findMatchingDefinitions(featureCode: String): List<AnyFeatureDef> {
        return booleanFeatures.getOrDefault(featureCode, listOf()) +
                enumFeatures.getOrDefault(featureCode, listOf()) +
                intFeatures.getOrDefault(featureCode, listOf())
    }

    fun allDefinitions() =
        booleanFeatures.values.flatten() + enumFeatures.values.flatten() + intFeatures.values.flatten()

    fun allDefinitionMaps(): Map<String, List<AnyFeatureDef>> = booleanFeatures + enumFeatures + intFeatures

    fun booleanFeatures() = booleanFeatures.flatMap { it.value }.map { it.feature as BooleanFeature }.toSortedSet()
    fun enumFeatures() = enumFeatures.flatMap { it.value }.map { it.feature as EnumFeature }.toSortedSet()
    fun intFeatures() = intFeatures.flatMap { it.value }.map { it.feature as IntFeature }.toSortedSet()
    fun nonUniqueFeatures() = nonUniqueFeatures.toSortedSet()
    fun enumDefinitions() = enumFeatures.flatMap { it.value }.map { it as EnumFeatureDefinition }
    fun containsBooleanFeatures() = booleanFeatures.isNotEmpty()
    fun containsEnumFeatures() = enumFeatures.isNotEmpty()
    fun containsIntFeatures() = intFeatures.isNotEmpty()
    fun containsVersionedBooleanFeatures() = booleanFeatures().any { it is VersionedBooleanFeature }

    private fun addDefinitionToMap(
        definition: PrlFeatureDefinition,
        slicingProperties: MutableMap<String, AnySlicingPropertyDefinition>,
        map: MutableMap<String, MutableList<AnyFeatureDef>>,
        isGroup: Boolean,
        state: CompilerState
    ) {
        val defToAdd = FeatureDefinition.fromPrlDef(definition)
        val existingDefinitions = findMatchingDefinitions(definition.code)
        val hasUniqueSlices = existingDefinitions.all { existing ->
            uniqueSlices(defToAdd.properties, existing.properties, slicingProperties.keys)
        }
        val added = hasUniqueSlices && addDefinition(
            defToAdd,
            map.computeIfAbsent(definition.code) { mutableListOf() },
            isGroup
        )
        if (!added) {
            state.addError("Duplicate feature definition")
        }
    }

    private fun addDefinition(definition: AnyFeatureDef, list: MutableList<AnyFeatureDef>, isGroup: Boolean): Boolean {
        if (isGroup) groups.add(definition.feature as BooleanFeature)
        return list.add(definition)
    }

    private fun mapForType(definition: PrlFeatureDefinition) = when (definition) {
        is PrlBooleanFeatureDefinition -> booleanFeatures
        is PrlEnumFeatureDefinition -> enumFeatures
        is PrlIntFeatureDefinition -> intFeatures
    }
}

