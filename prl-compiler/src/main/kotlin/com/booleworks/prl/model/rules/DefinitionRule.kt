// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.ConstraintType
import com.booleworks.prl.model.constraints.TRUE
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_IS
import java.util.Objects

class DefinitionRule(
    val feature: BooleanFeature,
    val definition: Constraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : Rule<DefinitionRule>(id, description, properties, lineNumber) {

    init {
        require(feature !is VersionedBooleanFeature) { "A versioned feature cannot be defined" }
    }

    private constructor(rule: AnyRule, feature: BooleanFeature, definition: Constraint) : this(
        feature, definition, rule.id, rule.description, rule.properties, rule.lineNumber
    )

    override fun features() = definition.features() + feature
    override fun booleanFeatures() = definition.booleanFeatures() + feature
    override fun enumFeatures() = definition.enumFeatures()
    override fun enumValues() = definition.enumValues()
    override fun intFeatures() = definition.intFeatures()
    override fun containsBooleanFeatures() = true
    override fun containsEnumFeatures() = definition.containsEnumFeatures()
    override fun containsIntFeatures() = definition.containsIntFeatures()
    override fun evaluate(assignment: FeatureAssignment) =
        feature.evaluate(assignment) == definition.evaluate(assignment)

    override fun rename(renaming: FeatureRenaming) =
        DefinitionRule(this, renaming.rename(feature), definition.rename(renaming))

    override fun stripProperties() = DefinitionRule(feature, definition, id, description, mapOf(), lineNumber)

    override fun stripMetaInfo() = DefinitionRule(feature, definition, "", "", properties, lineNumber)
    override fun stripAll() = DefinitionRule(feature, definition, "", "", mapOf(), lineNumber)
    override fun headerLine() = "$feature $KEYWORD_IS $definition"

    override fun hashCode() = Objects.hash(super.hashCode(), feature, definition)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) =
        other is DefinitionRule && feature == other.feature && definition == other.definition

    override fun restrict(assignment: FeatureAssignment): AnyRule {
        val featureRestriction = feature.restrict(assignment)
        val definitionRestriction = definition.restrict(assignment)
        return when {
            featureRestriction.type === ConstraintType.TRUE -> ConstraintRule(this, definitionRestriction)
            featureRestriction.type === ConstraintType.FALSE -> ConstraintRule(this, not(definitionRestriction))
            definitionRestriction.type === ConstraintType.TRUE -> ConstraintRule(this, featureRestriction)
            definitionRestriction.type === ConstraintType.FALSE -> ConstraintRule(this, not(featureRestriction))
            else -> DefinitionRule(this, feature, definition.restrict(assignment))
        }
    }

    override fun syntacticSimplify() = definition.syntacticSimplify().let {
        when {
            it.type === ConstraintType.TRUE -> ConstraintRule(this, feature)
            it.type === ConstraintType.FALSE -> ConstraintRule(this, not(feature))
            feature == it -> ConstraintRule(this, TRUE)
            else -> this
        }
    }
}
