// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.Constant
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.constraints.versionEq
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_FEATURE
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_FORBIDDEN
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_MANDATORY
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_EQ
import com.booleworks.prl.parser.PragmaticRuleLanguage.quote

sealed class FeatureRule<F : FeatureRule<F>>(
    open val feature: Feature,
    open val enumValue: String?,
    open val intValueOrVersion: Int?,
    id: String,
    description: String,
    properties: Map<String, AnyProperty>,
    lineNumber: Int? = null
) : Rule<F>(id, description, properties, lineNumber) {

    val constraint by lazy {
        generateConstraint(feature, enumValue, intValueOrVersion)
    }

    val version by lazy {
        if (feature is VersionedBooleanFeature) intValueOrVersion else null
    }

    val intValue by lazy {
        if (feature is IntFeature) intValueOrVersion else null
    }

    override fun features() = setOf(feature)
    override fun booleanFeatures() = if (feature is BooleanFeature) setOf(feature as BooleanFeature) else setOf()
    override fun enumFeatures() = if (feature is EnumFeature) setOf(feature as EnumFeature) else setOf()
    override fun enumValues() = if (feature is EnumFeature) {
        val set: MutableSet<String> = mutableSetOf()
        enumValue?.let { set.add(it) }
        mapOf(feature as EnumFeature to set)
    } else {
        mapOf()
    }

    override fun intFeatures() = if (feature is IntFeature) setOf(feature as IntFeature) else setOf()
    override fun containsBooleanFeatures() = feature is BooleanFeature
    override fun containsEnumFeatures() = feature is EnumFeature
    override fun containsIntFeatures() = feature is IntFeature
    override fun evaluate(assignment: FeatureAssignment) = constraint.evaluate(assignment)
    override fun restrict(assignment: FeatureAssignment) =
        constraint.restrict(assignment).let { if (it is Constant) ConstraintRule(this, it) else this }

    override fun syntacticSimplify() = this
    protected fun renameFeature(renaming: FeatureRenaming) = when (feature) {
        is BooleanFeature -> renaming.rename(feature as BooleanFeature)
        is EnumFeature -> renaming.rename(feature as EnumFeature)
        is IntFeature -> renaming.rename(feature as IntFeature)
    }

    override fun headerLine(): String {
        val keyword: String = if (this is ForbiddenFeatureRule) KEYWORD_FORBIDDEN else KEYWORD_MANDATORY
        val constraintString: String = when (feature) {
            is VersionedBooleanFeature -> versionEq(feature as VersionedBooleanFeature, intValueOrVersion!!).toString()
            is BooleanFeature -> feature.toString()
            is EnumFeature -> feature.toString() + " " + SYMBOL_EQ + " " + quote(enumValue!!)
            is IntFeature -> "$feature $SYMBOL_EQ $intValueOrVersion"
        }
        return "$keyword $KEYWORD_FEATURE $constraintString"
    }

    protected abstract fun generateConstraint(feature: Feature, enumValue: String?, intValueOrVersion: Int?): Constraint
}
