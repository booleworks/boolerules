// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.ConstraintType
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.TRUE
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_IF
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_THEN
import java.util.Objects

class InclusionRule(
    val ifConstraint: Constraint,
    val thenConstraint: Constraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : Rule<InclusionRule>(id, description, properties, lineNumber) {

    internal constructor(rule: AnyRule, ifConstraint: Constraint, thenConstraint: Constraint) : this(
        ifConstraint, thenConstraint, rule.id, rule.description, rule.properties, rule.lineNumber
    )

    override fun features() = ifConstraint.features() + thenConstraint.features()
    override fun booleanFeatures() = ifConstraint.booleanFeatures() + thenConstraint.booleanFeatures()
    override fun enumFeatures() = ifConstraint.enumFeatures() + thenConstraint.enumFeatures()
    override fun intFeatures() = ifConstraint.intFeatures() + thenConstraint.intFeatures()
    override fun enumValues(): Map<EnumFeature, MutableSet<String>> {
        val result: MutableMap<EnumFeature, MutableSet<String>> = LinkedHashMap()
        ifConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        thenConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        return result
    }

    override fun containsBooleanFeatures() =
        ifConstraint.containsBooleanFeatures() || thenConstraint.containsBooleanFeatures()

    override fun containsEnumFeatures() = ifConstraint.containsEnumFeatures() || thenConstraint.containsEnumFeatures()
    override fun containsIntFeatures() = ifConstraint.containsIntFeatures() || thenConstraint.containsIntFeatures()

    override fun evaluate(assignment: FeatureAssignment) =
        !ifConstraint.evaluate(assignment) || thenConstraint.evaluate(assignment)

    override fun restrict(assignment: FeatureAssignment) =
        simplifiedRule(ifConstraint.restrict(assignment), thenConstraint.restrict(assignment))

    override fun syntacticSimplify() =
        simplifiedRule(ifConstraint.syntacticSimplify(), thenConstraint.syntacticSimplify())

    override fun rename(renaming: FeatureRenaming) =
        InclusionRule(this, ifConstraint.rename(renaming), thenConstraint.rename(renaming))

    override fun stripProperties() = InclusionRule(ifConstraint, thenConstraint, id, description, mapOf(), lineNumber)

    override fun stripMetaInfo() = InclusionRule(ifConstraint, thenConstraint, "", "", properties, lineNumber)
    override fun stripAll() = InclusionRule(ifConstraint, thenConstraint, "", "", mapOf(), lineNumber)
    override fun headerLine() = "$KEYWORD_IF $ifConstraint $KEYWORD_THEN $thenConstraint"

    override fun hashCode() = Objects.hash(super.hashCode(), ifConstraint, thenConstraint)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) =
        other is InclusionRule && ifConstraint == other.ifConstraint && thenConstraint == other.thenConstraint

    private fun simplifiedRule(ifConstraint: Constraint, thenConstraint: Constraint): AnyRule {
        val tautology = ifConstraint.type === ConstraintType.FALSE || thenConstraint.type === ConstraintType.TRUE ||
                ifConstraint == thenConstraint
        return when {
            tautology -> ConstraintRule(this, TRUE)
            ifConstraint.type === ConstraintType.TRUE -> ConstraintRule(this, thenConstraint)
            thenConstraint.type === ConstraintType.FALSE -> ConstraintRule(this, not(ifConstraint))
            else -> InclusionRule(this, ifConstraint, thenConstraint)
        }
    }
}
