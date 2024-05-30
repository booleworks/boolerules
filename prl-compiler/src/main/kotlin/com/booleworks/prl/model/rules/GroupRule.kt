// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.ConstraintType
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Equivalence
import com.booleworks.prl.model.constraints.Exo
import com.booleworks.prl.model.constraints.FALSE
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_CONTAINS
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_GROUP
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_COMMA
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.groupTypeString
import java.util.Objects

enum class GroupType { OPTIONAL, MANDATORY }

class GroupRule(
    val type: GroupType,
    val group: BooleanFeature,
    val content: Set<BooleanFeature>,
    override val id: String = "",
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : Rule<GroupRule>(id, description, properties, lineNumber) {

    constructor(
        type: GroupType,
        group: BooleanFeature,
        content: Collection<BooleanFeature>,
        id: String = "",
        description: String = "",
        properties: Map<String, AnyProperty> = mapOf(),
        lineNumber: Int? = null
    ) : this(type, group, content.toSet(), id, description, properties, lineNumber)

    private constructor(
        rule: AnyRule,
        type: GroupType,
        group: BooleanFeature,
        content: Collection<BooleanFeature>,
    ) : this(type, group, content.toSet(), rule.id, rule.description, rule.properties, rule.lineNumber)

    override fun features() = setOf(group) + content
    override fun booleanFeatures() = setOf(group) + content
    override fun enumFeatures() = setOf<EnumFeature>()
    override fun enumValues() = mapOf<EnumFeature, Set<String>>()
    override fun intFeatures() = setOf<IntFeature>()
    override fun containsBooleanFeatures() = true
    override fun containsEnumFeatures() = false
    override fun containsIntFeatures() = false

    /**
     * Evaluates if the rule is true with the given assignment.
     *
     * For mandatory rules the group is always true. An optional rule becomes a mandatory rule
     * if the group is true.  If the group is false, all features of the groupContent must be false.
     * @param assignment the current assignment
     * @return the evaluation of this rule
     */
    override fun evaluate(assignment: FeatureAssignment) =
        if (type == GroupType.MANDATORY || group.evaluate(assignment)) {
            group.evaluate(assignment) && Exo(content).evaluate(assignment)
        } else {
            not(or(content)).evaluate(assignment)
        }

    override fun restrict(assignment: FeatureAssignment) =
        if (type == GroupType.MANDATORY) restrictMandatory(assignment) else restrictOptional(assignment)

    private fun restrictMandatory(assignment: FeatureAssignment): AnyRule {
        group.restrict(assignment).let {
            if (it.type === ConstraintType.TRUE) {
                return ConstraintRule(this, Exo(content).restrict(assignment))
            } else if (it.type === ConstraintType.FALSE) {
                return ConstraintRule(this, FALSE)
            }
        }
        return Exo(content).restrict(assignment).let {
            when {
                it.type === ConstraintType.TRUE -> ConstraintRule(this, group)
                it.type === ConstraintType.FALSE -> ConstraintRule(this, FALSE)
                it.type === ConstraintType.ATOM || it.type === ConstraintType.AND -> ConstraintRule(
                    this,
                    and(group, it)
                )
                else -> GroupRule(this, type, group, it.booleanFeatures())
            }
        }
    }

    private fun restrictOptional(assignment: FeatureAssignment): AnyRule {
        group.restrict(assignment).let {
            if (it.type === ConstraintType.TRUE) {
                return ConstraintRule(this, Exo(content).restrict(assignment))
            } else if (it.type === ConstraintType.FALSE) {
                return ConstraintRule(this, not(or(content).restrict(assignment)))
            }
        }
        return Exo(content).restrict(assignment).let {
            when {
                it.type === ConstraintType.TRUE -> ConstraintRule(this, group)
                it.type === ConstraintType.FALSE ->
                    if (or(content).restrict(assignment).type === ConstraintType.FALSE) {
                        ConstraintRule(this, not(group))
                    } else {
                        ConstraintRule(this, FALSE)
                    }
                it.type === ConstraintType.ATOM -> ConstraintRule(this, Equivalence(group, it))
                it.type === ConstraintType.AND -> ConstraintRule(this, and(group, it).syntacticSimplify())
                else -> GroupRule(this, type, group, it.booleanFeatures())
            }
        }
    }

    override fun syntacticSimplify() = when {
        content.isEmpty() -> ConstraintRule(this, if (type == GroupType.OPTIONAL) not(group) else FALSE)
        content.size == 1 -> ConstraintRule(
            this,
            if (type == GroupType.OPTIONAL) Equivalence(group, content.first()) else and(group, content.first())
        )
        else -> this
    }

    override fun rename(renaming: FeatureRenaming) =
        GroupRule(this, type, renaming.rename(group), content.map { renaming.rename(it) })

    override fun stripProperties() = GroupRule(type, group, content, id, description, mapOf(), lineNumber)
    override fun stripMetaInfo() = GroupRule(type, group, content, "", "", properties, lineNumber)
    override fun stripAll() = GroupRule(type, group, content, "", "", mapOf(), lineNumber)

    override fun headerLine() = "${groupTypeString(type)} $KEYWORD_GROUP $group " +
            "$KEYWORD_CONTAINS $SYMBOL_LSQB" + content.joinToString("$SYMBOL_COMMA ") { it.toString() } + SYMBOL_RSQB

    override fun hashCode() = Objects.hash(super.hashCode(), type, group, content)
    override fun equals(other: Any?) = super.equals(other) && hasEqualConstraint(other as AnyRule)
    private fun hasEqualConstraint(other: AnyRule) =
        other is GroupRule && type == other.type && group == other.group && content == other.content
}
