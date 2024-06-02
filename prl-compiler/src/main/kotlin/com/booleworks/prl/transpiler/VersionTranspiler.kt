// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.constraints.And
import com.booleworks.prl.model.constraints.ComparisonOperator.EQ
import com.booleworks.prl.model.constraints.ComparisonOperator.GE
import com.booleworks.prl.model.constraints.ComparisonOperator.GT
import com.booleworks.prl.model.constraints.ComparisonOperator.LE
import com.booleworks.prl.model.constraints.ComparisonOperator.LT
import com.booleworks.prl.model.constraints.ComparisonOperator.NE
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.Equivalence
import com.booleworks.prl.model.constraints.Implication
import com.booleworks.prl.model.constraints.Not
import com.booleworks.prl.model.constraints.Or
import com.booleworks.prl.model.constraints.VersionPredicate
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.model.rules.DefinitionRule
import com.booleworks.prl.model.rules.ExclusionRule
import com.booleworks.prl.model.rules.ForbiddenFeatureRule
import com.booleworks.prl.model.rules.GroupRule
import com.booleworks.prl.model.rules.IfThenElseRule
import com.booleworks.prl.model.rules.InclusionRule
import com.booleworks.prl.model.rules.MandatoryFeatureRule
import com.booleworks.prl.model.slices.SliceSet
import java.util.SortedMap

const val VERSION_PREFIX = "@VER"

internal fun initVersionStore(f: FormulaFactory, rules: List<AnyRule>): Map<String, SortedMap<Int, Variable>> {
    val vs = mutableMapOf<String, SortedMap<Int, Variable>>()
    rules.forEach { addRuleToVersionStore(f, it, vs) }
    return vs
}

private fun addRuleToVersionStore(f: FormulaFactory, rule: AnyRule, vs: MutableMap<String, SortedMap<Int, Variable>>) {
    when (rule) {
        is ConstraintRule -> addContraintsToVersionStore(f, vs, rule.constraint)
        is DefinitionRule -> addContraintsToVersionStore(f, vs, rule.definition)
        is ExclusionRule -> addContraintsToVersionStore(f, vs, rule.ifConstraint, rule.thenNotConstraint)
        is ForbiddenFeatureRule -> addContraintsToVersionStore(f, vs, rule.constraint)
        is MandatoryFeatureRule -> addContraintsToVersionStore(f, vs, rule.constraint)
        is IfThenElseRule -> addContraintsToVersionStore(
            f, vs, rule.ifConstraint, rule.thenConstraint, rule.elseConstraint
        )
        is InclusionRule -> addContraintsToVersionStore(f, vs, rule.ifConstraint, rule.thenConstraint)
        is GroupRule -> {}
    }
}

private fun addContraintsToVersionStore(
    f: FormulaFactory,
    vs: MutableMap<String, SortedMap<Int, Variable>>,
    vararg constraints: Constraint
) {
    constraints.forEach {
        when (it) {
            is Not -> addContraintsToVersionStore(f, vs, it.operand)
            is Equivalence -> addContraintsToVersionStore(f, vs, it.left, it.right)
            is Implication -> addContraintsToVersionStore(f, vs, it.left, it.right)
            is And -> addContraintsToVersionStore(f, vs, *it.operands.toTypedArray<Constraint>())
            is Or -> addContraintsToVersionStore(f, vs, *it.operands.toTypedArray<Constraint>())
            is VersionPredicate -> addUsage(f, vs, it)
            else -> {}
        }
    }
}

fun versionPropositions(
    f: FormulaFactory,
    sliceSet: SliceSet,
    vs: Map<String, SortedMap<Int, Variable>>
): List<PrlProposition> = generateVersionConstraints(f, sliceSet, vs)

fun translateVersionComparison(
    f: FormulaFactory,
    constraint: VersionPredicate,
    vs: Map<String, SortedMap<Int, Variable>>
): Formula {
    val fea = constraint.feature
    val ver = constraint.version
    val versionMapping = vs[fea.featureCode]
    if (versionMapping == null) {
        return f.literal(fea.featureCode, false)
    } else {
        val maxValue = vs[fea.featureCode]!!.lastKey()
        return when (constraint.comparison) {
            EQ -> versionVar(f, fea, ver)
            NE -> versionVar(f, fea, ver).negate(f)
            LT -> if (ver == 1) {
                f.literal(fea.featureCode, false)
            } else {
                f.or((1..<ver).map { versionVar(f, fea, it) })
            }
            LE -> f.or((1..ver).map { versionVar(f, fea, it) })
            GT -> f.or((ver + 1..maxValue).map { versionVar(f, fea, it) })
            GE -> f.or((ver..maxValue).map { versionVar(f, fea, it) })
        }
    }
}

private fun generateVersionConstraints(
    f: FormulaFactory,
    sliceSet: SliceSet,
    vs: Map<String, SortedMap<Int, Variable>>
): List<PrlProposition> {
    val propositions = mutableListOf<PrlProposition>()
    vs.forEach { (fea, vers) ->
        val vars = vers.values
        val amo = f.amo(vars)
        val euqiv = f.equivalence(f.variable(fea), f.or(vars))
        propositions.add(PrlProposition(RuleInformation(RuleType.VERSION_AMO_CONSTRAINT, sliceSet), amo))
        propositions.add(PrlProposition(RuleInformation(RuleType.VERSION_EQUIVALENCE, sliceSet), euqiv))
    }
    return propositions
}

internal fun versionVar(f: FormulaFactory, fea: VersionedBooleanFeature, ver: Int) =
    f.variable("${VERSION_PREFIX}_${fea.featureCode}_$ver")


fun addUsage(f: FormulaFactory, vs: MutableMap<String, SortedMap<Int, Variable>>, predicate: VersionPredicate) {
    val feature = predicate.feature
    val code = predicate.feature.featureCode
    val ver = when (predicate.comparison) {
        in setOf(EQ, NE, GE, LE) -> predicate.version
        GT -> predicate.version + 1
        else -> predicate.version - 1
    }
    if (vs[code] == null) {
        if (ver >= 1) {
            vs[code] = sortedMapOf()
            for (it in 1..ver) {
                vs[code]!![it] = versionVar(f, feature, it)
            }
        }
    } else if (vs[code]!![ver] == null) {
        val currentMax = vs[code]!!.lastKey()
        for (it in currentMax + 1..ver) {
            vs[code]!![it] = versionVar(f, feature, it)
        }
    }
}
