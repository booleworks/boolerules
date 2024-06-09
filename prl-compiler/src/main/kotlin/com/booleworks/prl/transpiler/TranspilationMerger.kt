// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.constraints.IntPredicate
import com.booleworks.prl.transpiler.RuleType.FEATURE_EQUIVALENCE_OVER_SLICES
import com.booleworks.prl.transpiler.RuleType.INTEGER_VARIABLE
import com.booleworks.prl.transpiler.RuleType.UNKNOWN_FEATURE_IN_SLICE

fun mergeSlices(cf: CspFactory, slices: List<SliceTranslation>): MergedSliceTranslation {
    //TODO version/int merging
    val f = cf.formulaFactory()
    val theoryMap = slices[0].info.theoryMap
    val knownVariables = slices.flatMap { it.info.knownVariables }.toSortedSet()
    val sliceSelectors = mutableMapOf<String, SliceTranslation>()
    val propositions = mutableListOf<PrlProposition>()
    val enumMapping = mutableMapOf<String, MutableMap<String, Variable>>()
    val unknownFeatures = slices[0].info.unknownFeatures.toMutableSet()
    val booleanVariables = mutableSetOf<Variable>()
    val intPredicateMapping = mutableMapOf<IntPredicate, Variable>()
    val encodingContext = CspEncodingContext(slices[0].info.encodingContext)
    val integerEncodings = slices[0].info.integerStore.clone()

    val instantiation = mergeFeatureInstantiations(slices)
    instantiation.integerFeatures.values.forEach {
        integerEncodings.getInfo(it.code)!!.addDefinition(it, encodingContext, cf)
    }
    val mergedVarMap = instantiation.integerFeatures.mapValues { (_, v) -> integerEncodings.getVariable(v)!! }
    val integerVariables = mergedVarMap.values.toMutableSet()
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
            if (kVar in slice.info.knownVariables) {
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
        propositions += slice.info.integerVariables.map {
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
        unknownFeatures.retainAll(slice.info.unknownFeatures)
        slice.info.enumMapping.forEach { (feature, varMap) ->
            enumMapping.computeIfAbsent(feature) { mutableMapOf() }.putAll(varMap)
        }
        slice.info.propositions.forEach { propositions.add(it.substitute(f, substitution)) }
    }

    val info = TranspilationInfo(
        theoryMap,
        instantiation,
        encodingContext,
        integerEncodings,
        intPredicateMapping,
        mapOf(),  //TODO merged version features
        booleanVariables,
        enumMapping,
        knownVariables,
        unknownFeatures,
        propositions,
    )
    return MergedSliceTranslation(sliceSelectors, info)
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

