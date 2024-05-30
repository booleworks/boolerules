// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.propositions.ExtendedProposition
import com.booleworks.logicng.propositions.PropositionBackpack
import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.IntPredicate
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceSet
import com.booleworks.prl.model.slices.SliceType.ALL
import com.booleworks.prl.model.slices.SliceType.ANY
import com.booleworks.prl.model.slices.SliceType.SPLIT

typealias PrlProposition = ExtendedProposition<RuleInformation>

enum class RuleType(val description: String) {
    ORIGINAL_RULE("Original rule from the rule file"),
    UNKNOWN_FEATURE_IN_SLICE("Unknown feature in this slice"),
    FEATURE_EQUIVALENCE_OVER_SLICES("Feature equivalence for slice"),
    ENUM_FEATURE_CONSTRAINT("EXO constraint for enum feature values"),
    INTEGER_VARIABLE("Definition of an int feature"),
    INTEGER_PREDICATE_DEFINITION("Definition of predicate auxiliary variable"),
    VERSION_INTERVAL_VARIABLE("Interval variable for versioned feature"),
    VERSION_AMO_CONSTRAINT("AMO constraint for versioned feature"),
    VERSION_EQUIVALENCE("Equivalence constraint for versioned feature"),
    ADDITIONAL_RESTRICTION("Additional user-provided restriction")
}

data class RuleInformation(val ruleType: RuleType, val rule: AnyRule?, val sliceSet: SliceSet?) : PropositionBackpack {
    constructor(rule: AnyRule, sliceSet: SliceSet) : this(RuleType.ORIGINAL_RULE, rule, sliceSet)
    constructor(ruleType: RuleType, sliceSet: SliceSet) : this(ruleType, null, sliceSet)
    constructor(ruleType: RuleType) : this(ruleType, null, null)

    companion object {
        fun fromAdditionRestriction(constraint: Constraint) =
            RuleInformation(RuleType.ADDITIONAL_RESTRICTION, ConstraintRule(constraint), null)
    }
}

fun PrlProposition.substitute(f: FormulaFactory, substitution: Substitution) =
    PrlProposition(backpack(), formula().substitute(f, substitution))

data class SliceTranslation(val sliceSet: SliceSet, val info: TranslationInfo) {
    val propositions = info.propositions
    val knownVariables = info.knownVariables
    val booleanVariables = info.booleanVariables
    val enumVariables = info.enumVariables
    val integerVariables = info.integerVariables

    //TODO version variables
    val enumMapping = info.enumMapping
    val unknownFeatures = info.unknownFeatures
}

data class MergedSliceTranslation(val sliceSelectors: Map<String, SliceTranslation>, val info: TranslationInfo) {
    val propositions = info.propositions
    val knownVariables = info.knownVariables
    val booleanVariables = info.booleanVariables
    val enumVariables = info.enumVariables
    val integerVariables = info.integerVariables
    val enumMapping = info.enumMapping
    val unknownFeatures = info.unknownFeatures
}

data class ModelTranslation(
    val computations: List<SliceTranslation>,
    val skippedConstraints: List<String>
) : Iterable<SliceTranslation> {
    val numberOfComputations = computations.size
    val allSlices = LinkedHashSet(computations.flatMap { it.sliceSet.slices })

    fun sliceMap(): Map<Slice, SliceTranslation> =
        computations.flatMap { it.sliceSet.slices.map { slice -> Pair(slice, it) } }.toMap()

    fun allSplitSlices() = allSlices.map { it.filterProperties(setOf(SPLIT)) }.distinct()
    fun allAnySlices() = allSlices.map { it.filterProperties(setOf(ANY)) }.distinct()
    fun allAnySlices(slice: Slice) =
        allSlices.filter { it.matches(slice) }.map { it.filterProperties(setOf(SPLIT, ANY)) }.distinct()

    fun allAllSlices() = allSlices.map { it.filterProperties(setOf(ALL)) }.distinct()
    fun allAllSlices(slice: Slice) =
        allSlices.filter { it.matches(slice) }.map { it.filterProperties(setOf(SPLIT, ANY, ALL)) }.distinct()

    operator fun get(index: Int) = computations[index]
    override fun iterator() = computations.iterator()
}

data class FeatureInstantiation(
    val booleanFeatures: Map<String, BooleanFeatureDefinition>,
    val enumFeatures: Map<String, EnumFeatureDefinition>,
    val integerFeatures: Map<String, IntFeatureDefinition>,
) {
    operator fun get(feature: Feature) = when (feature) {
        is BooleanFeature -> booleanFeatures[feature.featureCode]
        is EnumFeature -> enumFeatures[feature.featureCode]
        is IntFeature -> integerFeatures[feature.featureCode]
    }

    operator fun get(feature: BooleanFeature) = booleanFeatures[feature.featureCode]
    operator fun get(feature: EnumFeature) = enumFeatures[feature.featureCode]
    operator fun get(feature: IntFeature) = integerFeatures[feature.featureCode]

    companion object {
        fun empty() = FeatureInstantiation(mapOf(), mapOf(), mapOf())
    }
}

interface TranspilerCoreInfo {
    val featureInstantiations: FeatureInstantiation
    val unknownFeatures: Set<Feature>
    val booleanVariables: Set<Variable>
    val integerVariables: Set<LngIntVariable>
    val enumMapping: Map<String, Map<String, Variable>>
    val intPredicateMapping: Map<IntPredicate, Variable>
    val encodingContext: CspEncodingContext
    val versionMapping: Map<String, Map<Int, Variable>>
}

data class TranslationInfo(
    val propositions: List<PrlProposition>,
    val knownVariables: Set<Variable>,
    val integerEncodings: IntegerEncodingStore,
    override val featureInstantiations: FeatureInstantiation,
    override val booleanVariables: Set<Variable>,
    override val integerVariables: Set<LngIntVariable>,
    override val enumMapping: Map<String, Map<String, Variable>>,
    override val unknownFeatures: Set<Feature>,
    override val intPredicateMapping: Map<IntPredicate, Variable>,
    override val encodingContext: CspEncodingContext,
    override val versionMapping: Map<String, Map<Int, Variable>>,
) : TranspilerCoreInfo {
    val enumVariables: Set<Variable> = enumMapping.values.flatMap { it.values }.toSet()
    private val var2enum = mutableMapOf<Variable, Pair<String, String>>()
    private val var2version = mutableMapOf<Variable, Pair<String, Int>>()

    init {
        enumMapping.forEach { (feature, vs) ->
            vs.forEach { (value, variable) ->
                var2enum[variable] = Pair(feature, value)
            }
        }
        versionMapping.forEach { (feature, vs) ->
            vs.forEach { (value, variable) ->
                var2version[variable] = Pair(feature, value)
            }
        }
    }

    fun getFeatureAndValue(v: Variable) = var2enum[v]
    fun getFeatureAndVersion(v: Variable) = var2version[v]
}
