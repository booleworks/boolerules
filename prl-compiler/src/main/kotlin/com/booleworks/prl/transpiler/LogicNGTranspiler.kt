// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.compiler.CoCoException
import com.booleworks.prl.compiler.ConstraintCompiler
import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.PrlModel
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
import com.booleworks.prl.model.constraints.IntPredicate
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
import com.booleworks.prl.transpiler.RuleType.FEATURE_EQUIVALENCE_OVER_SLICES
import com.booleworks.prl.transpiler.RuleType.INTEGER_PREDICATE_DEFINITION
import com.booleworks.prl.transpiler.RuleType.INTEGER_VARIABLE
import com.booleworks.prl.transpiler.RuleType.UNKNOWN_FEATURE_IN_SLICE

const val S = "_"
const val SLICE_SELECTOR_PREFIX = "@SL"

fun transpileModel(
    cf: CspFactory,
    model: PrlModel,
    selectors: List<AnySliceSelection>,
    maxNumberOfSlices: Int = MAXIMUM_NUMBER_OF_SLICES,
    additionalConstraints: List<String> = emptyList()
): ModelTranslation {
    val allSlices = computeAllSlices(selectors, model.propertyStore.allDefinitions(), maxNumberOfSlices)
    val sliceSets = computeSliceSets(allSlices, model)
    val context = CspEncodingContext()
    val intVarDefinitions = encodeIntFeatures(context, cf, model.featureStore)
    val skippedConstraints = mutableListOf<String>()
    val globalContraints = additionalConstraints.mapNotNull { processConstraint(it, model, skippedConstraints) }
    return ModelTranslation(sliceSets.map {
        transpileSliceSet(context, cf, intVarDefinitions, globalContraints, it)
    }, skippedConstraints)
}

fun processConstraint(
    constraint: String,
    model: PrlModel,
    skippedConstraints: MutableList<String>,
): ConstraintRule? {
    val parsed = parseConstraint<PrlConstraint>(constraint)
    val theoryMap = model.featureStore.theoryMap
    try {
        val compiled = ConstraintCompiler().compileConstraint(parsed, theoryMap)
        return ConstraintRule(compiled, description = "Additional Constraint")
    } catch (_: CoCoException) {
        skippedConstraints.add(constraint)
        return null
    }
}

fun transpileConstraint(
    cf: CspFactory,
    constraint: Constraint,
    info: TranspilerCoreInfo,
    integerEncodings: IntegerEncodingStore
): Formula =
    when (constraint) {
        is Constant -> cf.formulaFactory().constant(constraint.value)
        is VersionedBooleanFeature ->
            if (info.booleanVariables.contains(cf.formulaFactory().variable(constraint.featureCode))) {
                cf.formulaFactory().variable(constraint.featureCode)
            } else {
                cf.formulaFactory().falsum()
            }
        is BooleanFeature ->
            if (info.booleanVariables.contains(cf.formulaFactory().variable(constraint.featureCode))) {
                cf.formulaFactory().variable(constraint.featureCode)
            } else {
                cf.formulaFactory().falsum()
            }
        is Not -> cf.formulaFactory().not(transpileConstraint(cf, constraint.operand, info, integerEncodings))
        is Implication -> cf.formulaFactory().implication(
            transpileConstraint(cf, constraint.left, info, integerEncodings),
            transpileConstraint(cf, constraint.right, info, integerEncodings)
        )
        is Equivalence -> cf.formulaFactory().equivalence(
            transpileConstraint(cf, constraint.left, info, integerEncodings),
            transpileConstraint(cf, constraint.right, info, integerEncodings)
        )
        is And -> cf.formulaFactory()
            .and(constraint.operands.map { transpileConstraint(cf, it, info, integerEncodings) })
        is Or -> cf.formulaFactory().or(constraint.operands.map { transpileConstraint(cf, it, info, integerEncodings) })
        is Amo -> cf.formulaFactory().amo(filterFeatures(cf.formulaFactory(), constraint.features, info))
        is Exo -> cf.formulaFactory().exo(filterFeatures(cf.formulaFactory(), constraint.features, info))
        is EnumComparisonPredicate -> translateEnumComparison(cf.formulaFactory(), info.enumMapping, constraint)
        is EnumInPredicate -> translateEnumIn(cf.formulaFactory(), info.enumMapping, constraint)
        is IntComparisonPredicate -> info.intPredicateMapping[constraint]
            ?: cf.formulaFactory().and( // TODO still necessary at the end?
                cf.encodeConstraint(
                    transpileIntComparisonPredicate(cf, integerEncodings, info.featureInstantiations, constraint),
                    info.encodingContext
                )
            )
        is IntInPredicate -> info.intPredicateMapping[constraint]
            ?: cf.formulaFactory().and( // TODO still necessary at the end?
                cf.encodeConstraint(
                    transpileIntInPredicate(cf, integerEncodings, info.featureInstantiations, constraint),
                    info.encodingContext,
                )
            )
        is VersionPredicate -> translateVersionComparison(cf, constraint)
    }

fun mergeSlices(cf: CspFactory, slices: List<SliceTranslation>): MergedSliceTranslation {
    val f = cf.formulaFactory()
    val knownVariables = slices.flatMap { it.knownVariables }.toSortedSet()
    val sliceSelectors = mutableMapOf<String, SliceTranslation>()
    val propositions = mutableListOf<PrlProposition>()
    val enumMapping = mutableMapOf<String, MutableMap<String, Variable>>()
    val unknownFeatures = slices[0].unknownFeatures.toMutableSet()
    val booleanVariables = mutableSetOf<Variable>()
    val intPredicateMapping = mutableMapOf<IntPredicate, Variable>()
    val encodingContext = CspEncodingContext(slices[0].info.encodingContext)
    val integerEncodings = slices[0].info.integerEncodings.clone()

    val instantiation = mergeFeatureInstantiations(slices)
    instantiation.integerFeatures.values.forEach {
        integerEncodings.getInfo(it.code)!!.addDefinition(it, encodingContext, cf)
    }
    val mergedVarMap = instantiation.integerFeatures.mapValues { (_, v) -> integerEncodings.getVariable(v)!! }
    val integerVariables = mergedVarMap.values.toSet()
    integerVariables.forEach {
        propositions.add(PrlProposition(RuleInformation(INTEGER_VARIABLE), integerEncodings.getEncoding(it)!!))
    }

    var count = 0
    slices.forEach { slice ->
        val selector = "$SLICE_SELECTOR_PREFIX${count++}"
        sliceSelectors[selector] = slice
        val substitution = Substitution()
        knownVariables.forEach { kVar ->
            val sVar = f.variable("${selector}_${kVar.name()}")
            propositions.add(
                PrlProposition(
                    RuleInformation(FEATURE_EQUIVALENCE_OVER_SLICES, slice.sliceSet),
                    f.equivalence(kVar, sVar)
                )
            )
            if (kVar in slice.knownVariables) {
                substitution.addMapping(kVar, sVar)
            } else {
                propositions.add(
                    PrlProposition(
                        RuleInformation(UNKNOWN_FEATURE_IN_SLICE, slice.sliceSet),
                        sVar.negate(f)
                    )
                )
            }
        }
        propositions += slice.integerVariables.map {
            createIntVariableEquivalence(
                it.variable,
                mergedVarMap[it.feature]!!.variable,
                encodingContext,
                f
            )
        }
        slice.info.intPredicateMapping.forEach { (predicate, variable) ->
            val newVar = intPredicateMapping.computeIfAbsent(predicate) {
                variable
            }
            if (newVar != variable) {
                substitution.addMapping(variable, newVar)
            }
        }
        booleanVariables.addAll(slice.info.booleanVariables)
        unknownFeatures.retainAll(slice.unknownFeatures)
        slice.enumMapping.forEach { (feature, varMap) ->
            enumMapping.computeIfAbsent(feature) { mutableMapOf() }.putAll(varMap)
        }
        slice.propositions.forEach { propositions.add(it.substitute(f, substitution)) }
    }

    return MergedSliceTranslation(
        sliceSelectors,
        TranslationInfo(
            propositions,
            knownVariables,
            integerEncodings,
            instantiation,
            booleanVariables,
            integerVariables,
            setOf(),  //TODO merged version features
            enumMapping,
            unknownFeatures,
            intPredicateMapping,
            encodingContext,
            mapOf() //TODO merged version features
        )
    )
}

fun mergeFeatureInstantiations(slices: List<SliceTranslation>): FeatureInstantiation {
    val booleanFeatureDefs = mutableMapOf<String, MutableList<BooleanFeatureDefinition>>()
    slices.flatMap { it.info.featureInstantiations.booleanFeatures.entries }
        .groupByTo(booleanFeatureDefs, { it.key }, { it.value })
    val booleanFeatureInstantiations =
        booleanFeatureDefs.mapValues { (_, v) -> BooleanFeatureDefinition.merge(v) }.toMutableMap()

    val enumFeatureDefs = mutableMapOf<String, MutableList<EnumFeatureDefinition>>()
    slices.flatMap { it.info.featureInstantiations.enumFeatures.entries }
        .groupByTo(enumFeatureDefs, { it.key }, { it.value })
    val enumFeatureInstantiations =
        enumFeatureDefs.mapValues { (_, v) -> EnumFeatureDefinition.merge(v) }.toMutableMap()

    val intFeatureDefs = mutableMapOf<String, MutableList<IntFeatureDefinition>>()
    slices.flatMap { it.info.featureInstantiations.integerFeatures.entries }
        .groupByTo(intFeatureDefs, { it.key }, { it.value })
    val intFeatureInstantiations = intFeatureDefs.mapValues { (_, v) -> IntFeatureDefinition.merge(v) }.toMutableMap()

    return FeatureInstantiation(booleanFeatureInstantiations, enumFeatureInstantiations, intFeatureInstantiations)
}

fun transpileSliceSet(
    context: CspEncodingContext,
    cf: CspFactory,
    integerEncodings: IntegerEncodingStore,
    constraints: List<ConstraintRule>,
    sliceSet: SliceSet
): SliceTranslation {
    val f = cf.formulaFactory()
    sliceSet.rules.addAll(constraints)
    sliceSet.additionalConstraints.addAll(constraints)
    val versionStore = if (sliceSet.hasVersionFeatures()) initVersionStore(sliceSet.rules) else null
    val state = initState(context, cf, sliceSet, integerEncodings, versionStore)
    val propositions = sliceSet.rules.map { transpileRule(cf, it, sliceSet, state, integerEncodings) }.toMutableList()
    propositions += state.enumMapping.values.map {
        PrlProposition(
            RuleInformation(ENUM_FEATURE_CONSTRAINT, sliceSet),
            f.exo(it.values)
        )
    }
    propositions += state.intPredicateMapping.entries.map {
        val lngPredicate = transpileIntPredicate(cf, integerEncodings, state.featureInstantiations, it.key)
        val clauses = cf.encodeConstraint(lngPredicate, context)
        val formula = f.equivalence(it.value, f.and(clauses))
        PrlProposition(RuleInformation(INTEGER_PREDICATE_DEFINITION, sliceSet), formula)
    }
    propositions += state.integerVariables.map {
        PrlProposition(
            RuleInformation(INTEGER_VARIABLE),
            integerEncodings.getEncoding(it)!!
        )
    }
    if (versionStore != null) {
        propositions += versionPropositions(f, sliceSet, versionStore)
    }
    return SliceTranslation(sliceSet, state.toTranslationInfo(propositions, integerEncodings))
}

private fun initState(
    context: CspEncodingContext,
    cf: CspFactory,
    sliceSet: SliceSet,
    integerEncodings: IntegerEncodingStore,
    versionStore: VersionStore?
) = TranspilerState(
    featureInstantiations = getFeatureInstantiations(sliceSet),
    intPredicateMapping = getAllIntPredicates(cf.formulaFactory(), sliceSet),
    encodingContext = context
).apply {
    sliceSet.rules.flatMap { it.features() }.filter { featureInstantiations[it] == null }
        .forEach { unknownFeatures.add(it) }
    booleanVariables.addAll(
        featureInstantiations.booleanFeatures.values.map { cf.formulaFactory().variable(it.feature.featureCode) })
    featureInstantiations.enumFeatures.values
        .forEach { def ->
            enumMapping[def.feature.featureCode] =
                def.values.associateWith { enumFeature(cf.formulaFactory(), def.feature.featureCode, it) }
        }
    integerVariables.addAll(
        featureInstantiations.integerFeatures.values
            .map { integerEncodings.getVariable(featureInstantiations[it.feature]!!)!! }
    )
    versionStore?.usedValues?.forEach { (fea, maxVer) ->
        versionMapping[fea.featureCode] = (1..maxVer).associateWith { installed(cf.formulaFactory(), fea, it) }
        versionVariables.addAll(versionMapping.flatMap { it.value.values })
    }
}

fun getFeatureInstantiations(sliceSet: SliceSet): FeatureInstantiation {
    val booleanMap = sliceSet.definitions.filterIsInstance<BooleanFeatureDefinition>().associateBy { it.code }
    val enumMap = sliceSet.definitions.filterIsInstance<EnumFeatureDefinition>().associateBy { it.code }
    val intMap = sliceSet.definitions.filterIsInstance<IntFeatureDefinition>().associateBy { it.code }
    return FeatureInstantiation(booleanMap, enumMap, intMap)
}

private fun transpileRule(
    cf: CspFactory,
    r: AnyRule,
    sliceSet: SliceSet,
    state: TranspilerState,
    integerEncodings: IntegerEncodingStore
): PrlProposition =
    when (r) {
        is ConstraintRule -> transpileConstraint(cf, r.constraint, state, integerEncodings)
        is DefinitionRule -> cf.formulaFactory().equivalence(
            transpileConstraint(cf, r.feature, state, integerEncodings),
            transpileConstraint(cf, r.definition, state, integerEncodings)
        )
        is ExclusionRule -> cf.formulaFactory().implication(
            transpileConstraint(cf, r.ifConstraint, state, integerEncodings),
            transpileConstraint(cf, r.thenNotConstraint, state, integerEncodings).negate(cf.formulaFactory())
        )
        is ForbiddenFeatureRule -> transpileConstraint(cf, r.constraint, state, integerEncodings)
        is MandatoryFeatureRule -> transpileConstraint(cf, r.constraint, state, integerEncodings)
        is GroupRule -> transpileGroupRule(cf.formulaFactory(), r, state)
        is InclusionRule -> cf.formulaFactory().implication(
            transpileConstraint(cf, r.ifConstraint, state, integerEncodings),
            transpileConstraint(cf, r.thenConstraint, state, integerEncodings)
        )
        is IfThenElseRule -> transpileConstraint(cf, r.ifConstraint, state, integerEncodings).let { ifPart ->
            cf.formulaFactory().or(
                cf.formulaFactory().and(ifPart, transpileConstraint(cf, r.thenConstraint, state, integerEncodings)),
                cf.formulaFactory().and(
                    ifPart.negate(cf.formulaFactory()),
                    transpileConstraint(cf, r.elseConstraint, state, integerEncodings)
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

private fun transpileGroupRule(f: FormulaFactory, rule: GroupRule, state: TranspilerState): Formula {
    val content = filterFeatures(f, rule.content, state)
    val group = if (state.featureInstantiations.booleanFeatures.containsKey(rule.group.featureCode)) {
        f.variable(rule.group.featureCode)
    } else {
        f.falsum()
    }
    val cc = if (rule.type == GroupType.MANDATORY) f.exo(content) else f.amo(content)
    return f.and(cc, f.equivalence(group, f.or(content)))
}

private fun filterFeatures(f: FormulaFactory, fs: Collection<BooleanFeature>, info: TranspilerCoreInfo) =
    fs.filter { info.booleanVariables.contains(f.variable(it.featureCode)) }.map { f.variable(it.featureCode) }

data class TranspilerState(
    override val featureInstantiations: FeatureInstantiation,
    override val unknownFeatures: MutableSet<Feature> = mutableSetOf(),
    override val booleanVariables: MutableSet<Variable> = mutableSetOf(),
    override val integerVariables: MutableSet<LngIntVariable> = mutableSetOf(),
    override val versionVariables: MutableSet<Variable> = mutableSetOf(),
    override val enumMapping: MutableMap<String, Map<String, Variable>> = mutableMapOf(),
    override val intPredicateMapping: MutableMap<IntPredicate, Variable> = mutableMapOf(),
    override val encodingContext: CspEncodingContext,
    override val versionMapping: MutableMap<String, Map<Int, Variable>> = mutableMapOf(),
) : TranspilerCoreInfo {
    private fun knownVariables() = (booleanVariables + enumMapping.values.flatMap { it.values }).toSortedSet()
    fun toTranslationInfo(propositions: List<PrlProposition>, integerEncodings: IntegerEncodingStore) =
        TranslationInfo(
            propositions,
            knownVariables(),
            integerEncodings,
            featureInstantiations,
            booleanVariables,
            integerVariables,
            versionVariables,
            enumMapping,
            unknownFeatures,
            intPredicateMapping,
            encodingContext,
            versionMapping
        )
}
