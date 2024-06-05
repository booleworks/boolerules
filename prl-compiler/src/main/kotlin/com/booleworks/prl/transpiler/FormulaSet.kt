// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.propositions.ExtendedProposition
import com.booleworks.logicng.propositions.PropositionBackpack
import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.Theory
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
import java.util.SortedMap

typealias PrlProposition = ExtendedProposition<RuleInformation>

enum class RuleType(val description: String) {
    ORIGINAL_RULE("Original rule from the rule file"),
    UNKNOWN_FEATURE_IN_SLICE("Unknown feature in this slice"),
    FEATURE_EQUIVALENCE_OVER_SLICES("Feature equivalence for slice"),
    ENUM_FEATURE_CONSTRAINT("EXO constraint for enum feature values"),
    INTEGER_VARIABLE("Definition of an int feature"),
    INTEGER_PREDICATE_DEFINITION("Definition of predicate auxiliary variable"),
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

data class SliceTranslation(val sliceSet: SliceSet, val info: TranspilationInfo) {
    val propositions = info.propositions
    val knownVariables = info.knownVariables
    val booleanVariables = info.booleanVariables
    val enumVariables = info.enumVariables
    val integerVariables = info.integerVariables
    val versionVariables = info.versionVariables
    val versionMapping = info.versionMapping
    val enumMapping = info.enumMapping
    val unknownFeatures = info.unknownFeatures
}

data class MergedSliceTranslation(val sliceSelectors: Map<String, SliceTranslation>, val info: TranspilationInfo) {
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

data class TranspilationInfo(
    val theoryMap: Map<String, Theory>, // A mapping from each feature to its theory (model)
    val featureInstantiations: FeatureInstantiation, // A mapping from each feature to its definition (slice)
    val encodingContext: CspEncodingContext, // The context for CSP encodings (model)
    val integerStore: IntegerStore, // The store for integer variables (model)
    val intPredicateMapping: Map<IntPredicate, Variable>, // A mapping for all integer predicates (slice)
    val versionMapping: Map<String, SortedMap<Int, Variable>>, // All known enum variables and their values (slice)
    val booleanVariables: Set<Variable>, // All known boolean variables (slice)
    val enumMapping: Map<String, Map<String, Variable>>, // All known enum variables and their values (slice)
    val knownVariables: Set<Variable>, // All known problem variables (slice)
    val unknownFeatures: Set<Feature>, // All used but unknown features (slice)

    val propositions: MutableList<PrlProposition> = mutableListOf(),
) {
    val enumVariables: Set<Variable>
    val versionVariables: Set<Variable>
    val integerVariables: Set<LngIntVariable>

    private val var2enum = mutableMapOf<Variable, Pair<String, String>>()
    private val var2version = mutableMapOf<Variable, Pair<String, Int>>()

    init {
        enumVariables = mutableSetOf()
        enumMapping.forEach { (feature, vs) ->
            vs.forEach { (value, variable) ->
                enumVariables.add(variable)
                var2enum[variable] = Pair(feature, value)
            }
        }
        versionVariables = mutableSetOf()
        versionMapping.forEach { (feature, vs) ->
            vs.forEach { (ver, variable) ->
                versionVariables.add(variable)
                var2version[variable] = Pair(feature, ver)
            }
        }
        integerVariables = featureInstantiations.integerFeatures.values
            .map { integerStore.getVariable(featureInstantiations[it.feature]!!)!! }.toSet()
    }

    fun getFeatureAndValue(v: Variable) = var2enum[v]
    fun getFeatureAndVersion(v: Variable) = var2version[v]

    fun translateConstraint(f: FormulaFactory, input: String): Pair<Constraint, Formula>? {
        val processed = processConstraint(input, theoryMap) ?: return null
        val constraint = processed.constraint
        val formula = transpileConstraint(f, constraint, this)
        return Pair(constraint, formula)
    }
}
