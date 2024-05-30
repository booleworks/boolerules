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
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_THEN_NOT
import java.util.Objects

class ExclusionRule(
    val ifConstraint: Constraint,
    val thenNotConstraint: Constraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : Rule<ExclusionRule>(id, description, properties, lineNumber) {

    private constructor(rule: AnyRule, ifConstraint: Constraint, thenNotConstraint: Constraint) : this(
        ifConstraint, thenNotConstraint, rule.id, rule.description, rule.properties, rule.lineNumber
    )

    override fun features() = ifConstraint.features() + thenNotConstraint.features()
    override fun booleanFeatures() = ifConstraint.booleanFeatures() + thenNotConstraint.booleanFeatures()
    override fun enumFeatures() = ifConstraint.enumFeatures() + thenNotConstraint.enumFeatures()
    override fun intFeatures() = ifConstraint.intFeatures() + thenNotConstraint.intFeatures()
    override fun enumValues(): Map<EnumFeature, MutableSet<String>> {
        val result: MutableMap<EnumFeature, MutableSet<String>> = LinkedHashMap()
        ifConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        thenNotConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        return result
    }

    override fun containsBooleanFeatures() =
        ifConstraint.containsBooleanFeatures() || thenNotConstraint.containsBooleanFeatures()

    override fun containsEnumFeatures() =
        ifConstraint.containsEnumFeatures() || thenNotConstraint.containsEnumFeatures()

    override fun containsIntFeatures() = ifConstraint.containsIntFeatures() || thenNotConstraint.containsIntFeatures()
    override fun evaluate(assignment: FeatureAssignment) =
        !ifConstraint.evaluate(assignment) || !thenNotConstraint.evaluate(assignment)

    override fun restrict(assignment: FeatureAssignment) =
        simplifiedRule(ifConstraint.restrict(assignment), thenNotConstraint.restrict(assignment))

    override fun syntacticSimplify() =
        simplifiedRule(ifConstraint.syntacticSimplify(), thenNotConstraint.syntacticSimplify())

    override fun rename(renaming: FeatureRenaming) =
        ExclusionRule(this, ifConstraint.rename(renaming), thenNotConstraint.rename(renaming))

    override fun stripProperties() =
        ExclusionRule(ifConstraint, thenNotConstraint, id, description, mapOf(), lineNumber)

    override fun stripMetaInfo() =
        ExclusionRule(ifConstraint, thenNotConstraint, "", "", properties, lineNumber)

    override fun stripAll() = ExclusionRule(ifConstraint, thenNotConstraint, "", "", mapOf(), lineNumber)
    override fun headerLine() = "$KEYWORD_IF $ifConstraint $KEYWORD_THEN_NOT $thenNotConstraint"

    override fun hashCode() = Objects.hash(super.hashCode(), ifConstraint, thenNotConstraint)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) =
        other is ExclusionRule && ifConstraint == other.ifConstraint && thenNotConstraint == other.thenNotConstraint

    private fun simplifiedRule(ifConstraint: Constraint, thenNotConstraint: Constraint) = when {
        ifConstraint.type === ConstraintType.TRUE -> ConstraintRule(this, not(thenNotConstraint))
        ifConstraint.type === ConstraintType.FALSE ||
                thenNotConstraint.type === ConstraintType.FALSE -> ConstraintRule(this, TRUE)
        thenNotConstraint.type === ConstraintType.TRUE ||
                ifConstraint == thenNotConstraint -> ConstraintRule(this, not(ifConstraint))
        else -> ExclusionRule(this, ifConstraint, thenNotConstraint)
    }
}
