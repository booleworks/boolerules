// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.ConstraintType
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_ELSE
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_IF
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_THEN
import java.util.Objects

class IfThenElseRule(
    val ifConstraint: Constraint,
    val thenConstraint: Constraint,
    val elseConstraint: Constraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : Rule<IfThenElseRule>(id, description, properties, lineNumber) {

    private constructor(
        rule: AnyRule,
        ifConstraint: Constraint,
        thenConstraint: Constraint,
        elseConstraint: Constraint
    ) : this(
        ifConstraint,
        thenConstraint,
        elseConstraint,
        rule.id,
        rule.description,
        rule.properties,
        rule.lineNumber
    )

    override fun features() = ifConstraint.features() + thenConstraint.features() + elseConstraint.features()
    override fun booleanFeatures() =
        ifConstraint.booleanFeatures() + thenConstraint.booleanFeatures() + elseConstraint.booleanFeatures()

    override fun enumFeatures() =
        ifConstraint.enumFeatures() + thenConstraint.enumFeatures() + elseConstraint.enumFeatures()

    override fun enumValues(): Map<EnumFeature, MutableSet<String>> {
        val result: MutableMap<EnumFeature, MutableSet<String>> = mutableMapOf()
        ifConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        thenConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        elseConstraint.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        return result
    }

    override fun intFeatures() =
        ifConstraint.intFeatures() + thenConstraint.intFeatures() + elseConstraint.intFeatures()

    override fun containsBooleanFeatures() =
        ifConstraint.containsBooleanFeatures() || thenConstraint.containsBooleanFeatures() ||
                elseConstraint.containsBooleanFeatures()

    override fun containsEnumFeatures() =
        ifConstraint.containsEnumFeatures() || thenConstraint.containsEnumFeatures() ||
                elseConstraint.containsEnumFeatures()

    override fun containsIntFeatures() =
        ifConstraint.containsIntFeatures() || thenConstraint.containsIntFeatures() ||
                elseConstraint.containsIntFeatures()

    override fun evaluate(assignment: FeatureAssignment) =
        if (ifConstraint.evaluate(assignment)) {
            thenConstraint.evaluate(assignment)
        } else {
            elseConstraint.evaluate(assignment)
        }

    override fun restrict(assignment: FeatureAssignment) =
        simplifiedRule(
            ifConstraint.restrict(assignment),
            thenConstraint.restrict(assignment),
            elseConstraint.restrict(assignment)
        )

    override fun syntacticSimplify() = simplifiedRule(
        ifConstraint.syntacticSimplify(),
        thenConstraint.syntacticSimplify(),
        elseConstraint.syntacticSimplify()
    )

    override fun rename(renaming: FeatureRenaming) =
        IfThenElseRule(
            this,
            ifConstraint.rename(renaming),
            thenConstraint.rename(renaming),
            elseConstraint.rename(renaming)
        )

    override fun stripProperties() =
        IfThenElseRule(ifConstraint, thenConstraint, elseConstraint, id, description, mapOf(), lineNumber)

    override fun stripMetaInfo() =
        IfThenElseRule(ifConstraint, thenConstraint, elseConstraint, "", "", properties, lineNumber)

    override fun stripAll() =
        IfThenElseRule(ifConstraint, thenConstraint, elseConstraint, "", "", mapOf(), lineNumber)

    override fun headerLine() = "$KEYWORD_IF $ifConstraint $KEYWORD_THEN $thenConstraint $KEYWORD_ELSE $elseConstraint"

    override fun hashCode() = Objects.hash(super.hashCode(), ifConstraint, thenConstraint, elseConstraint)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) =
        other is IfThenElseRule && ifConstraint == other.ifConstraint && thenConstraint == other.thenConstraint &&
                elseConstraint == other.elseConstraint

    private fun simplifiedRule(simpIf: Constraint, simpThen: Constraint, simpElse: Constraint) = when {
        simpIf == simpThen && simpThen == simpElse -> ConstraintRule(this, simpIf)
        simpIf.type === ConstraintType.TRUE -> ConstraintRule(this, simpThen)
        simpIf.type === ConstraintType.FALSE -> ConstraintRule(this, simpElse)
        simpThen.type === ConstraintType.TRUE || simpIf == simpThen -> InclusionRule(this, not(simpIf), simpElse)
        simpThen.type === ConstraintType.FALSE -> ConstraintRule(this, and(not(simpIf), simpElse))
        simpElse.type === ConstraintType.TRUE -> InclusionRule(this, simpIf, simpThen)
        simpElse.type === ConstraintType.FALSE || simpIf == simpElse -> ConstraintRule(this, and(simpIf, simpThen))
        else -> IfThenElseRule(this, simpIf, simpThen, simpElse)
    }
}
