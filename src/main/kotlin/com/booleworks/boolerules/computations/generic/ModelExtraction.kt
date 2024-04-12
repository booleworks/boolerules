// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.logicng.csp.CspAssignment
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.Module
import com.booleworks.prl.transpiler.FEATURE_DEF_PREFIX
import com.booleworks.prl.transpiler.S
import com.booleworks.prl.transpiler.TranslationInfo
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

internal fun extractModelWithInt(variables: Collection<Variable>, integerAssignment: CspAssignment, info: TranslationInfo): FeatureModelDO =
    FeatureModelDO(variables.filter { info.knownVariables.contains(it) }
        .map { variable -> extractFeature(variable, info) }.sorted() + extractIntFeatures(integerAssignment, info))

/**
 * Extracts a model of a given set of variables (which are the positive
 * variables of the model) and returns a list of typed features.
 */
internal fun extractModel(variables: Collection<Variable>, info: TranslationInfo): FeatureModelDO =
    FeatureModelDO(variables.filter { info.knownVariables.contains(it) }
        .map { variable -> extractFeature(variable, info) }.sorted())

internal fun extractIntFeatures(integerAssignment: CspAssignment, info: TranslationInfo): Collection<FeatureDO> =
    integerAssignment.integerAssignments.map { (variable, value) ->
        val v = info.integerVariables.find { it.variable.name == variable.name }!!
        FeatureDO.int(extractIntFeatureCode(variable.name), v.feature, value)
    }


internal fun extractFeature(variable: Variable, info: TranslationInfo) =
    if (info.booleanVariables.contains(variable)) {
        FeatureDO.boolean(extractFeatureCode(variable.name()), variable.name(), true)
    } else if (info.enumVariables.contains(variable)) {
        val (feature, value) = info.getFeatureAndValue(variable)!!
        FeatureDO.enum(extractFeatureCode(feature), feature, value)
    } else {
        error("Currently only boolean and enum features are supported.")
    }

private fun extractFeatureCode(fullName: String) =
    fullName.substring(fullName.lastIndexOf(Module.MODULE_SEPARATOR) + 1)

private fun extractIntFeatureCode(fullName: String) =
    if (fullName.startsWith(FEATURE_DEF_PREFIX)) {
        extractFeatureCode(fullName.substring(fullName.indexOf(S) + 1))
    } else {
        extractFeatureCode(fullName)
    }
