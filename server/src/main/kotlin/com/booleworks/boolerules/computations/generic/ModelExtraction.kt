// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.logicng.csp.CspAssignment
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.transpiler.TranspilationInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A single model of a ruleset.  A ruleset a buildable configuration.")
data class FeatureModelDO(
    @field:Schema(description = "The features with their assignments")
    val features: List<FeatureDO>
) : Comparable<FeatureModelDO> {
    @JsonIgnore
    val size = features.size

    override fun compareTo(other: FeatureModelDO) = toString().compareTo(other.toString())

    override fun toString() = features.joinToString(", ") {
        when (it.type) {
            FeatureTypeDO.BOOLEAN ->
                if (it.booleanValue != null && it.booleanValue) {
                    it.code
                } else {
                    "-${it.code}"
                }
            FeatureTypeDO.VERSIONED_BOOLEAN -> "${it.code}=${it.version}"
            FeatureTypeDO.ENUM -> "${it.code}=${it.enumValue}"
            FeatureTypeDO.INT -> "${it.code}=${it.intValue}"
        }
    }
}

/**
 * Extracts a model of a given set of variables (which are the positive
 * variables of the model) and returns a list of typed features.
 */
internal fun extractModel(variables: Collection<Variable>, info: TranspilationInfo): FeatureModelDO =
    FeatureModelDO(variables.filter { info.knownVariables.contains(it) }
        .map { variable -> extractFeature(variable, info) }.sorted())

internal fun extractModel(
    variables: Collection<Variable>,
    integerAssignment: CspAssignment,
    info: TranspilationInfo
): FeatureModelDO =
    FeatureModelDO(variables.filter { info.knownVariables.contains(it) }
        .map { variable -> extractFeature(variable, info) }.sorted() + extractIntFeatures(integerAssignment, info))


internal fun extractIntFeatures(integerAssignment: CspAssignment, info: TranspilationInfo): Collection<FeatureDO> =
    integerAssignment.integerAssignments.map { (variable, value) ->
        val v = info.integerVariables.find { it.variable.name == variable.name }!!
        FeatureDO.int(v.feature, value)
    }

internal fun extractFeature(variable: Variable, info: TranspilationInfo) =
    if (variable in info.booleanVariables) {
        FeatureDO.boolean(variable.name(), true)
    } else if (variable in info.versionVariables) {
        val (feature, version) = info.getFeatureAndVersion(variable)!!
        FeatureDO.boolean(feature, version)
    } else if (variable in info.enumVariables) {
        val (feature, value) = info.getFeatureAndValue(variable)!!
        FeatureDO.enum(feature, value)
    } else {
        error("Cannot extract unknown variable.")
    }

