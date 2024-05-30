// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.model.constraints.And
import com.booleworks.prl.model.constraints.BooleanFeature
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

const val VERSION_FEATURE_PREFIX = "@VER"
private const val PREFIX_INSTALL = VERSION_FEATURE_PREFIX + "_i"
private const val PREFIX_INSTALL_GE = VERSION_FEATURE_PREFIX + "_ig"
private const val PREFIX_INSTALL_LE = VERSION_FEATURE_PREFIX + "_il"
private const val PREFIX_UNINSTALL_GE = VERSION_FEATURE_PREFIX + "_ug"
private const val PREFIX_UNINSTALL_LE = VERSION_FEATURE_PREFIX + "_ul"

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
): List<PrlProposition> {
    val props = mutableListOf<PrlProposition>()
    props.addAll(generateIntervalVariables(f, sliceSet, versionStore))
    props.addAll(generateVersionConstraints(f, sliceSet, versionStore))
    return props
}

fun translateVersionComparison(cf: CspFactory, constraint: VersionPredicate): Formula {
    val f = cf.formulaFactory()
    val fea = constraint.feature
    val ver = constraint.version
    return when (constraint.comparison) {
        EQ -> f.and(installed(f, fea, ver))
        NE -> f.or(installedLE(f, fea, ver - 1), installedGE(f, fea, ver + 1))
        LT -> installedLT(f, fea, ver)
        LE -> installedLE(f, fea, ver)
        GT -> installedGT(f, fea, ver)
        GE -> installedGT(f, fea, ver)
    }
}

fun generateIntervalVariables(f: FormulaFactory, sliceSet: SliceSet, versionStore: VersionStore): List<PrlProposition> {
    val props = mutableListOf<PrlProposition>()
    versionStore.usedValues.forEach { (fea, maxVer) ->
        for (it in 1..maxVer) {
            props.add(generateIntervalVariable(f, sliceSet, fea, it, maxVer))
        }
    }
    return props
}

fun generateIntervalVariable(
    f: FormulaFactory,
    sliceSet: SliceSet,
    fea: BooleanFeature,
    v: Int,
    maxVer: Int
): PrlProposition {
    val installed = installed(f, fea, v)
    val uninstalledGreater = uninstalledGE(f, fea, v)
    val uninstalledLess = uninstalledLE(f, fea, v)
    val installedGreaterPlusOne = if (maxVer >= v + 1) installedGT(f, fea, v) else f.falsum()
    val uninstalledGreaterPlusOne = uninstalledGT(f, fea, v)
    val installedLessMinusOne = if (v - 1 in 1..maxVer) installedLT(f, fea, v) else f.falsum()
    val uninstalledLessMinusOne = uninstalledLT(f, fea, v)
    val formula = f.cnf(
        f.or(installedGE(f, fea, v).negate(f), installed, installedGreaterPlusOne),
        f.or(uninstalledGreater.negate(f), installed.negate(f)),
        f.or(uninstalledGreater.negate(f), uninstalledGreaterPlusOne),
        f.or(installedLE(f, fea, v).negate(f), installed, installedLessMinusOne),
        f.or(uninstalledLess.negate(f), installed.negate(f)),
        f.or(uninstalledLess.negate(f), uninstalledLessMinusOne)
    )
    return PrlProposition(RuleInformation(RuleType.VERSION_INTERVAL_VARIABLE, sliceSet), formula)
}

private fun generateVersionConstraints(
    f: FormulaFactory,
    sliceSet: SliceSet,
    versionStore: VersionStore
): List<PrlProposition> {
    val propositions = mutableListOf<PrlProposition>()
    versionStore.usedValues.forEach { (fea, maxVer) ->
        val vars = (1..maxVer).map { v -> vin(f, fea, v) }
        val amo = f.amo(vars)
        val euqiv = f.equivalence(f.variable(fea.featureCode), f.or(vars))
        propositions.add(PrlProposition(RuleInformation(RuleType.VERSION_AMO_CONSTRAINT, sliceSet), amo))
        propositions.add(PrlProposition(RuleInformation(RuleType.VERSION_EQUIVALENCE, sliceSet), euqiv))
    }
    return propositions
}

internal fun installed(f: FormulaFactory, fea: BooleanFeature, v: Int) = vin(f, fea, v)
private fun installedGE(f: FormulaFactory, fea: BooleanFeature, v: Int) = vig(f, fea, v)
private fun installedGT(f: FormulaFactory, fea: BooleanFeature, v: Int) = vig(f, fea, v + 1)
private fun installedLE(f: FormulaFactory, fea: BooleanFeature, v: Int) = vil(f, fea, v)
private fun installedLT(f: FormulaFactory, fea: BooleanFeature, v: Int) = vil(f, fea, v - 1)
private fun uninstalledGE(f: FormulaFactory, fea: BooleanFeature, v: Int) = vug(f, fea, v)
private fun uninstalledGT(f: FormulaFactory, fea: BooleanFeature, v: Int) = vug(f, fea, v + 1)
private fun uninstalledLE(f: FormulaFactory, fea: BooleanFeature, v: Int) = vul(f, fea, v)
private fun uninstalledLT(f: FormulaFactory, fea: BooleanFeature, v: Int) = vul(f, fea, v - 1)

private fun vin(f: FormulaFactory, fea: BooleanFeature, v: Int) =
    f.variable(PREFIX_INSTALL + "_" + fea.featureCode + "_" + v)

private fun vig(f: FormulaFactory, fea: BooleanFeature, v: Int) =
    f.variable(PREFIX_INSTALL_GE + "_" + fea.featureCode + "_" + v)

private fun vil(f: FormulaFactory, fea: BooleanFeature, v: Int) =
    f.variable(PREFIX_INSTALL_LE + "_" + fea.featureCode + "_" + v)

private fun vug(f: FormulaFactory, fea: BooleanFeature, v: Int) =
    f.variable(PREFIX_UNINSTALL_GE + "_" + fea.featureCode + "_" + v)

private fun vul(f: FormulaFactory, fea: BooleanFeature, v: Int) =
    f.variable(PREFIX_UNINSTALL_LE + "_" + fea.featureCode + "_" + v)

data class VersionStore internal constructor(
    val usedValues: MutableMap<BooleanFeature, Int> = mutableMapOf()
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
