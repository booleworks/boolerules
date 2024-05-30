// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.datastructures

import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature

class FeatureAssignment(
    private val booleanFeatures: MutableMap<BooleanFeature, Boolean> = mutableMapOf(),
    private val versionedBooleanFeatures: MutableMap<BooleanFeature, Int> = mutableMapOf(),
    private val enumFeatures: MutableMap<EnumFeature, String> = mutableMapOf(),
    private val intFeatures: MutableMap<IntFeature, Int> = mutableMapOf()
) {
    fun assign(feature: BooleanFeature, value: Boolean) = apply {
        booleanFeatures[feature] = value
    }

    fun assign(feature: VersionedBooleanFeature, value: Boolean) = apply {
        require(!value) {
            "Versioned features can't be assigned to true directly. Please assign a concrete version instead."
        }
        versionedBooleanFeatures[feature] = -1
    }

    fun assign(feature: VersionedBooleanFeature, version: Int) = apply {
        versionedBooleanFeatures[feature] = version
    }

    fun assign(feature: EnumFeature, value: String) = apply { enumFeatures[feature] = value }
    fun assign(feature: IntFeature, value: Int) = apply { intFeatures[feature] = value }

    fun getBool(feature: BooleanFeature): Boolean {
        require(feature !is VersionedBooleanFeature) { "Cannot get boolean for a versioned feature" }
        return booleanFeatures.getOrDefault(feature, false)
    }

    fun getBoolWithoutDefault(feature: BooleanFeature): Boolean? {
        require(feature !is VersionedBooleanFeature) { "Cannot get boolean for a versioned feature" }
        return booleanFeatures[feature]
    }

    fun getVersion(feature: VersionedBooleanFeature): Int? = versionedBooleanFeatures[feature].let {
        if (it == null || it > -1) it else null
    }

    fun getVersionWithoutDefault(feature: VersionedBooleanFeature): Boolean? = versionedBooleanFeatures[feature].let {
        if (it == null) null else it != -1
    }

    fun getEnum(feature: EnumFeature) = enumFeatures[feature]
    fun getInt(feature: IntFeature) = intFeatures[feature]
}
