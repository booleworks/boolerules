// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.datastructures

import com.booleworks.logicng.csp.CspAssignment
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.transpiler.TranspilationInfo

data class FeatureInstance(
    val feature: Feature,
    val intValue: Int? = null,
    val enumValue: String? = null
) : Comparable<FeatureInstance> {
    override fun compareTo(other: FeatureInstance) = feature.featureCode.compareTo(other.feature.featureCode)

    override fun toString() = when (feature) {
        is VersionedBooleanFeature -> "${feature.featureCode}=${intValue}"
        is BooleanFeature -> feature.featureCode
        is EnumFeature -> "${feature.featureCode}=${enumValue}"
        is IntFeature -> "${feature.featureCode}=${intValue}"
    }

    companion object {
        fun boolean(feature: String) = FeatureInstance(boolFt(feature))
        fun enum(feature: String, value: String) = FeatureInstance(enumFt(feature), enumValue = value)
        fun int(feature: String, value: Int) = FeatureInstance(intFt(feature), value)
    }
}

data class FeatureModel(
    val features: List<FeatureInstance>
) : Comparable<FeatureModel> {
    val size = features.size
    override fun compareTo(other: FeatureModel) = toString().compareTo(other.toString())
    override fun toString() = features.joinToString(", ")
}

internal fun extractModel(
    variables: Collection<Variable>,
    info: TranspilationInfo,
    relevant: Collection<Variable>? = null
): FeatureModel =
    FeatureModel(variables
        .filter { it in info.knownVariables }
        .filter { relevant == null || it in relevant }
        .map { variable -> extractFeature(variable, info) }.sorted()
    )

internal fun extractModel(
    variables: Collection<Variable>,
    integerAssignment: CspAssignment,
    info: TranspilationInfo
): FeatureModel =
    FeatureModel(variables
        .filter { it in info.knownVariables }
        .map { variable -> extractFeature(variable, info) }.sorted() +
            extractIntFeatures(integerAssignment, info)
    )

internal fun extractIntFeatures(integerAssignment: CspAssignment, info: TranspilationInfo) =
    integerAssignment.integerAssignments.map { (variable, value) ->
        val v = info.integerVariables.find { it.variable.name == variable.name }!!
        FeatureInstance(intFt(v.feature), value)
    }

internal fun extractFeature(variable: Variable, info: TranspilationInfo) =
    when (variable) {
        in info.booleanVariables -> FeatureInstance(boolFt(variable.name()))
        in info.versionVariables -> {
            val (feature, version) = info.getFeatureAndVersion(variable)!!
            FeatureInstance(versionFt(feature), version)
        }

        in info.enumVariables -> {
            val (feature, value) = info.getFeatureAndValue(variable)!!
            FeatureInstance(enumFt(feature), enumValue = value)
        }

        else -> error("Cannot extract unknown variable.")
    }

