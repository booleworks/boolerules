// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.datastructures

import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.versionFt

data class FeatureRenaming(
    private val booleanFeatures: MutableMap<BooleanFeature, BooleanFeature> = HashMap(),
    private val enumFeatures: MutableMap<EnumFeature, EnumFeature> = HashMap(),
    private val enumValues: MutableMap<EnumFeature, MutableMap<String, String>> = HashMap(),
    private val intFeatures: MutableMap<IntFeature, IntFeature> = HashMap()
) {
    fun add(feature: BooleanFeature, newName: String) = apply { booleanFeatures[feature] = boolFt(newName) }
    fun add(feature: VersionedBooleanFeature, newName: String) = apply { booleanFeatures[feature] = versionFt(newName) }
    fun add(feature: EnumFeature, newName: String) = apply { enumFeatures[feature] = enumFt(newName) }
    fun add(feature: EnumFeature, oldValue: String, newValues: String) =
        apply { enumValues.computeIfAbsent(feature) { mutableMapOf() }[oldValue] = newValues }

    fun add(feature: IntFeature, newName: String) = apply { intFeatures[feature] = intFt(newName) }

    fun rename(feature: BooleanFeature) = booleanFeatures.getOrDefault(feature, feature)
    fun rename(feature: VersionedBooleanFeature) =
        booleanFeatures.getOrDefault(feature, feature) as VersionedBooleanFeature

    fun rename(feature: EnumFeature) = enumFeatures.getOrDefault(feature, feature)
    fun rename(feature: EnumFeature, value: String) = enumValues[feature]?.get(value) ?: value
    fun rename(feature: IntFeature) = intFeatures.getOrDefault(feature, feature)

    operator fun contains(feature: BooleanFeature) = booleanFeatures.containsKey(feature)
    operator fun contains(feature: EnumFeature) = enumFeatures.containsKey(feature)
    operator fun contains(feature: IntFeature) = intFeatures.containsKey(feature)
    fun contains(feature: EnumFeature, value: String) = enumValues[feature].let { it != null && it.containsKey(value) }

    fun revert(): FeatureRenaming {
        val reverted = FeatureRenaming()
        booleanFeatures.forEach { (o, r) -> reverted.add(r, o.featureCode) }
        enumFeatures.forEach { (o, r) -> reverted.add(r, o.featureCode) }
        intFeatures.forEach { (o, r) -> reverted.add(r, o.featureCode) }
        enumValues.forEach { (o, vs) ->
            vs.forEach { (v1, v2) ->
                enumFeatures[o]?.let { reverted.add(it, v2, v1) }
            }
        }
        return reverted
    }
}
