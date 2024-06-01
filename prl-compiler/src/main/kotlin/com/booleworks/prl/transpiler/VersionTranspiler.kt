// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
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

const val VERSION_PREFIX = "@VER"

internal fun initVersionStore(rules: List<AnyRule>): VersionStore {
    val versionStore = VersionStore()
    rules.forEach { addRuleToVersionStore(it, versionStore) }
    return versionStore
}

private fun addRuleToVersionStore(rule: AnyRule, versionStore: VersionStore) {
    when (rule) {
        is ConstraintRule -> addContraintsToVersionStore(versionStore, rule.constraint)
        is DefinitionRule -> addContraintsToVersionStore(versionStore, rule.definition)
        is ExclusionRule -> addContraintsToVersionStore(versionStore, rule.ifConstraint, rule.thenNotConstraint)
        is ForbiddenFeatureRule -> addContraintsToVersionStore(versionStore, rule.constraint)
        is MandatoryFeatureRule -> addContraintsToVersionStore(versionStore, rule.constraint)
        is IfThenElseRule -> addContraintsToVersionStore(
            versionStore, rule.ifConstraint, rule.thenConstraint, rule.elseConstraint
        )
        is InclusionRule -> addContraintsToVersionStore(versionStore, rule.ifConstraint, rule.thenConstraint)
        is GroupRule -> {}
    }
}

private fun addContraintsToVersionStore(versionStore: VersionStore, vararg constraints: Constraint) {
    constraints.forEach {
        when (it) {
            is Not -> addContraintsToVersionStore(versionStore, it.operand)
            is Equivalence -> addContraintsToVersionStore(versionStore, it.left, it.right)
            is Implication -> addContraintsToVersionStore(versionStore, it.left, it.right)
            is And -> addContraintsToVersionStore(versionStore, *it.operands.toTypedArray<Constraint>())
            is Or -> addContraintsToVersionStore(versionStore, *it.operands.toTypedArray<Constraint>())
            is VersionPredicate -> versionStore.addUsage(it)
            else -> {}
        }
    }
}

fun versionPropositions(
    f: FormulaFactory,
    sliceSet: SliceSet,
    versionStore: VersionStore,
): List<PrlProposition> = generateVersionConstraints(f, sliceSet, versionStore)

fun translateVersionComparison(cf: CspFactory, constraint: VersionPredicate, vs: VersionStore): Formula {
    val f = cf.formulaFactory()
    val fea = constraint.feature
    val ver = constraint.version
    val maxValue = vs.usedValues[fea]!!
    return when (constraint.comparison) {
        EQ -> versionVar(f, fea, ver)
        NE -> versionVar(f, fea, ver).negate(f)
        LT -> f.or((1..<ver).map { versionVar(f, fea, it) })
        LE -> f.or((1..ver).map { versionVar(f, fea, it) })
        GT -> f.or((ver + 1..maxValue).map { versionVar(f, fea, it) })
        GE -> f.or((ver..maxValue).map { versionVar(f, fea, it) })
    }
}

private fun generateVersionConstraints(
    f: FormulaFactory,
    sliceSet: SliceSet,
    versionStore: VersionStore
): List<PrlProposition> {
    val propositions = mutableListOf<PrlProposition>()
    versionStore.usedValues.forEach { (fea, maxVer) ->
        val vars = (1..maxVer).map { versionVar(f, fea, it) }
        val amo = f.amo(vars)
        val euqiv = f.equivalence(f.variable(fea.featureCode), f.or(vars))
        propositions.add(PrlProposition(RuleInformation(RuleType.VERSION_AMO_CONSTRAINT, sliceSet), amo))
        propositions.add(PrlProposition(RuleInformation(RuleType.VERSION_EQUIVALENCE, sliceSet), euqiv))
    }
    return propositions
}

internal fun versionVar(f: FormulaFactory, fea: VersionedBooleanFeature, ver: Int) =
    f.variable("${VERSION_PREFIX}_${fea.featureCode}_$ver")

data class VersionStore internal constructor(
    val usedValues: MutableMap<VersionedBooleanFeature, Int> = mutableMapOf()
) {
    fun addUsage(predicate: VersionPredicate) {
        val ver = when (predicate.comparison) {
            in setOf(EQ, NE, GE, LE) -> predicate.version
            GT -> predicate.version + 1
            else -> predicate.version - 1
        }
        usedValues.merge(predicate.feature, ver, Math::max)
    }
}
