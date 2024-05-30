// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import java.util.Objects

class ConstraintRule(
    val constraint: Constraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : Rule<ConstraintRule>(id, description, properties, lineNumber) {

    constructor(rule: AnyRule, constraint: Constraint) : this(
        constraint,
        rule.id,
        rule.description,
        rule.properties,
        rule.lineNumber
    )

    override fun features() = constraint.features()
    override fun booleanFeatures() = constraint.booleanFeatures()
    override fun enumFeatures() = constraint.enumFeatures()
    override fun enumValues() = constraint.enumValues()
    override fun intFeatures() = constraint.intFeatures()
    override fun containsBooleanFeatures() = constraint.containsBooleanFeatures()
    override fun containsEnumFeatures() = constraint.containsEnumFeatures()
    override fun containsIntFeatures() = constraint.containsIntFeatures()
    override fun evaluate(assignment: FeatureAssignment) = constraint.evaluate(assignment)
    override fun restrict(assignment: FeatureAssignment) = ConstraintRule(this, constraint.restrict(assignment))
    override fun syntacticSimplify() = ConstraintRule(this, constraint.syntacticSimplify())
    override fun rename(renaming: FeatureRenaming) = ConstraintRule(this, constraint.rename(renaming))
    override fun stripProperties() = ConstraintRule(constraint, id, description, mapOf(), lineNumber)

    override fun stripMetaInfo() = ConstraintRule(constraint, "", "", properties, lineNumber)
    override fun stripAll() = ConstraintRule(constraint, "", "", mapOf(), lineNumber)
    override fun headerLine() = constraint.toString()
    override fun hashCode() = Objects.hash(super.hashCode(), constraint)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) = other is ConstraintRule && constraint == other.constraint
}
