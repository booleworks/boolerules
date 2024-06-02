// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.compiler.CoCoException
import com.booleworks.prl.compiler.ConstraintCompiler
import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.Theory
import com.booleworks.prl.model.constraints.Amo
import com.booleworks.prl.model.constraints.And
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.Constant
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.EnumComparisonPredicate
import com.booleworks.prl.model.constraints.EnumInPredicate
import com.booleworks.prl.model.constraints.Equivalence
import com.booleworks.prl.model.constraints.Exo
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.Implication
import com.booleworks.prl.model.constraints.IntComparisonPredicate
import com.booleworks.prl.model.constraints.IntInPredicate
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
import com.booleworks.prl.model.rules.GroupType
import com.booleworks.prl.model.rules.IfThenElseRule
import com.booleworks.prl.model.rules.InclusionRule
import com.booleworks.prl.model.rules.MandatoryFeatureRule
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.MAXIMUM_NUMBER_OF_SLICES
import com.booleworks.prl.model.slices.SliceSet
import com.booleworks.prl.model.slices.computeAllSlices
import com.booleworks.prl.model.slices.computeSliceSets
import com.booleworks.prl.parser.PrlConstraint
import com.booleworks.prl.parser.parseConstraint
import com.booleworks.prl.transpiler.RuleType.ADDITIONAL_RESTRICTION
import com.booleworks.prl.transpiler.RuleType.ENUM_FEATURE_CONSTRAINT
import com.booleworks.prl.transpiler.RuleType.INTEGER_PREDICATE_DEFINITION
import com.booleworks.prl.transpiler.RuleType.INTEGER_VARIABLE
import java.util.TreeSet

const val S = "_"
const val SLICE_SELECTOR_PREFIX = "@SL"

fun transpileModel(
    cf: CspFactory,
    model: PrlModel,
    selectors: List<AnySliceSelection>,
    maxNumberOfSlices: Int = MAXIMUM_NUMBER_OF_SLICES,
    additionalConstraints: List<String> = emptyList(),
    considerConstraints: List<String> = emptyList(),
): ModelTranslation {
    val context = CspEncodingContext()
    val intStore = initIntegerStore(context, cf, model.featureStore)
    val skippedConstraints = mutableListOf<String>()
    val theoryMap = model.featureStore.theoryMap
    val globalContraints = additionalConstraints.mapNotNull { processConstraint(it, theoryMap, skippedConstraints) }
    val consider = considerConstraints.mapNotNull { processConstraint(it, theoryMap, skippedConstraints) }

    val allSlices = computeAllSlices(selectors, model.propertyStore.allDefinitions(), maxNumberOfSlices)
    val sliceSets = computeSliceSets(allSlices, model, globalContraints, consider)
    return ModelTranslation(
        sliceSets.map { transpileSliceSet(theoryMap, context, cf, intStore, it) },
        skippedConstraints
    )
}

fun transpileSliceSet(
    theoryMap: Map<String, Theory>,
    context: CspEncodingContext,
    cf: CspFactory,
    integerStore: IntegerStore,
    sliceSet: SliceSet
): SliceTranslation {
    val f = cf.formulaFactory()
    val state = initState(theoryMap, context, cf, sliceSet, integerStore)
    val propositions = sliceSet.rules.map { transpileRule(f, it, sliceSet, state) }.toMutableList()
    propositions += state.enumMapping.values.map {
        PrlProposition(
            RuleInformation(ENUM_FEATURE_CONSTRAINT, sliceSet),
            f.exo(it.values)
        )
    }
    propositions += state.intPredicateMapping.entries.map {
        val lngPredicate = transpileIntPredicate(cf, integerStore, state.featureInstantiations, it.key)
        val clauses = cf.encodeConstraint(lngPredicate, context)
        val formula = f.equivalence(it.value, f.and(clauses))
        PrlProposition(RuleInformation(INTEGER_PREDICATE_DEFINITION, sliceSet), formula)
    }
    propositions += state.integerVariables.map {
        PrlProposition(
            RuleInformation(INTEGER_VARIABLE),
            integerStore.getEncoding(it)!!
        )
    }
    propositions += versionPropositions(f, sliceSet, state.versionMapping)
    state.propositions.addAll(propositions)
    return SliceTranslation(sliceSet, state)
}

fun processConstraint(
    constraint: String,
    theoryMap: Map<String, Theory>,
    skippedConstraints: MutableList<String>? = null,
): ConstraintRule? {
    val parsed = parseConstraint<PrlConstraint>(constraint)
    try {
        val compiled = ConstraintCompiler().compileConstraint(parsed, theoryMap)
        return ConstraintRule(compiled, description = "Additional Constraint")
    } catch (_: CoCoException) {
        skippedConstraints?.add(constraint)
        return null
    }
}

fun transpileConstraint(
    f: FormulaFactory,
    constraint: Constraint,
    info: TranspilationInfo,
): Formula = when (constraint) {
    is Constant -> f.constant(constraint.value)
    is VersionedBooleanFeature ->
        if (info.booleanVariables.contains(f.variable(constraint.featureCode))) {
            f.variable(constraint.featureCode)
        } else {
            f.falsum()
        }
    is BooleanFeature ->
        if (info.booleanVariables.contains(f.variable(constraint.featureCode))) {
            f.variable(constraint.featureCode)
        } else {
            f.falsum()
        }
    is Not -> f.not(transpileConstraint(f, constraint.operand, info))
    is Implication -> f.implication(
        transpileConstraint(f, constraint.left, info),
        transpileConstraint(f, constraint.right, info)
    )
    is Equivalence -> f.equivalence(
        transpileConstraint(f, constraint.left, info),
        transpileConstraint(f, constraint.right, info)
    )
    is And -> f.and(constraint.operands.map { transpileConstraint(f, it, info) })
    is Or -> f.or(constraint.operands.map { transpileConstraint(f, it, info) })
    is Amo -> f.amo(filterFeatures(f, constraint.features, info))
    is Exo -> f.exo(filterFeatures(f, constraint.features, info))
    is EnumComparisonPredicate -> translateEnumComparison(f, info.enumMapping, constraint)
    is EnumInPredicate -> translateEnumIn(f, info.enumMapping, constraint)
    is IntComparisonPredicate -> info.intPredicateMapping[constraint]!!
    is IntInPredicate -> info.intPredicateMapping[constraint]!!
    is VersionPredicate -> translateVersionComparison(f, constraint, info.versionMapping)
}

private fun initState(
    theoryMap: Map<String, Theory>,
    context: CspEncodingContext,
    cf: CspFactory,
    sliceSet: SliceSet,
    integerStore: IntegerStore,
): TranspilationInfo {
    val f = cf.formulaFactory()
    val insts = getFeatureInstantiations(sliceSet)
    val versionMapping = if (sliceSet.hasVersionFeatures()) initVersionStore(f, sliceSet.allRules) else mapOf()
    val intPredicateMapping = getAllIntPredicates(f, sliceSet)
    val booleanVariables = insts.booleanFeatures.values.map { f.variable(it.feature.featureCode) }.toSortedSet()

    val enumMapping = mutableMapOf<String, Map<String, Variable>>()
    insts.enumFeatures.values
        .forEach { def ->
            enumMapping[def.feature.featureCode] =
                def.values.associateWith { enumFeature(f, def.feature.featureCode, it) }.toMutableMap()
        }
    val unknownFeatures = sortedSetOf<Feature>()
    sliceSet.allRules.forEach { unknownFeatures.addAll(it.features().filter { fea -> insts[fea] == null }) }


    val knownVariables = TreeSet(booleanVariables)
    knownVariables.addAll(enumMapping.values.flatMap { it.values })
    knownVariables.addAll(versionMapping.values.flatMap { it.values })
    return TranspilationInfo(
        cf, theoryMap, insts, context, integerStore, intPredicateMapping, versionMapping,
        booleanVariables, enumMapping, knownVariables, unknownFeatures
    )
}

fun getFeatureInstantiations(sliceSet: SliceSet): FeatureInstantiation {
    val booleanMap = sliceSet.definitions.filterIsInstance<BooleanFeatureDefinition>().associateBy { it.code }
    val enumMap = sliceSet.definitions.filterIsInstance<EnumFeatureDefinition>().associateBy { it.code }
    val intMap = sliceSet.definitions.filterIsInstance<IntFeatureDefinition>().associateBy { it.code }
    return FeatureInstantiation(booleanMap, enumMap, intMap)
}

private fun transpileRule(
    f: FormulaFactory,
    r: AnyRule,
    sliceSet: SliceSet,
    info: TranspilationInfo,
): PrlProposition =
    when (r) {
        is ConstraintRule -> transpileConstraint(f, r.constraint, info)
        is DefinitionRule -> f.equivalence(
            transpileConstraint(f, r.feature, info),
            transpileConstraint(f, r.definition, info)
        )
        is ExclusionRule -> f.implication(
            transpileConstraint(f, r.ifConstraint, info),
            transpileConstraint(f, r.thenNotConstraint, info).negate(f)
        )
        is ForbiddenFeatureRule -> transpileConstraint(f, r.constraint, info)
        is MandatoryFeatureRule -> transpileConstraint(f, r.constraint, info)
        is GroupRule -> transpileGroupRule(f, r, info)
        is InclusionRule -> f.implication(
            transpileConstraint(f, r.ifConstraint, info),
            transpileConstraint(f, r.thenConstraint, info)
        )
        is IfThenElseRule -> transpileConstraint(f, r.ifConstraint, info).let { ifPart ->
            f.or(
                f.and(ifPart, transpileConstraint(f, r.thenConstraint, info)),
                f.and(
                    ifPart.negate(f),
                    transpileConstraint(f, r.elseConstraint, info)
                )
            )
        }
    }.let {
        if (r in sliceSet.additionalConstraints) {
            PrlProposition(RuleInformation(ADDITIONAL_RESTRICTION, r, null), it)
        } else {
            PrlProposition(RuleInformation(r, sliceSet), it)
        }
    }

private fun transpileGroupRule(f: FormulaFactory, rule: GroupRule, info: TranspilationInfo): Formula {
    val content = filterFeatures(f, rule.content, info)
    val group = if (info.featureInstantiations.booleanFeatures.containsKey(rule.group.featureCode)) {
        f.variable(rule.group.featureCode)
    } else {
        f.falsum()
    }
    val cc = if (rule.type == GroupType.MANDATORY) f.exo(content) else f.amo(content)
    return f.and(cc, f.equivalence(group, f.or(content)))
}

private fun filterFeatures(f: FormulaFactory, fs: Collection<BooleanFeature>, info: TranspilationInfo) =
    fs.filter { info.booleanVariables.contains(f.variable(it.featureCode)) }.map { f.variable(it.featureCode) }
