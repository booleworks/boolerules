// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.compiler

import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.PrlModelHeader
import com.booleworks.prl.model.Theory
import com.booleworks.prl.model.compileProperties
import com.booleworks.prl.model.compilePropertiesToMap
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.model.rules.DefinitionRule
import com.booleworks.prl.model.rules.ExclusionRule
import com.booleworks.prl.model.rules.ForbiddenFeatureRule
import com.booleworks.prl.model.rules.GroupRule
import com.booleworks.prl.model.rules.IfThenElseRule
import com.booleworks.prl.model.rules.InclusionRule
import com.booleworks.prl.model.rules.MandatoryFeatureRule
import com.booleworks.prl.parser.PrlBooleanFeatureDefinition
import com.booleworks.prl.parser.PrlConstraintRule
import com.booleworks.prl.parser.PrlDefinitionRule
import com.booleworks.prl.parser.PrlExclusionRule
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlFeatureDefinition
import com.booleworks.prl.parser.PrlFeatureRule
import com.booleworks.prl.parser.PrlForbiddenFeatureRule
import com.booleworks.prl.parser.PrlGroupRule
import com.booleworks.prl.parser.PrlIfThenElseRule
import com.booleworks.prl.parser.PrlInclusionRule
import com.booleworks.prl.parser.PrlMandatoryFeatureRule
import com.booleworks.prl.parser.PrlRule
import com.booleworks.prl.parser.PrlRuleFile
import com.booleworks.prl.parser.PrlRuleSet
import com.booleworks.prl.parser.PrlVersion
import java.util.UUID.randomUUID

class PrlCompiler {
    private val state = CompilerState()
    private val cc = ConstraintCompiler()

    fun compile(ruleFile: PrlRuleFile): PrlModel {
        val version = PrlVersion(ruleFile.header.major, ruleFile.header.minor)
        val header = PrlModelHeader(version, compilePropertiesToMap(ruleFile.header.properties))
        val t1 = System.currentTimeMillis()
        val propertyStore = compileSlicingPropertyDefinitionsIntoStore(ruleFile)
        val t2 = System.currentTimeMillis()
        val featureStore = compileFeaturesIntoStore(ruleFile, propertyStore)
        val t3 = System.currentTimeMillis()
        val rules = compileRules(ruleFile, propertyStore, featureStore)
        val t4 = System.currentTimeMillis()
        val numDefs = featureStore.size()
        state.addInfo(
            "Compiled ${propertyStore.slicingPropertyDefinitions.size} slicing property definition " +
                    "${if (propertyStore.slicingPropertyDefinitions.size != 1) "s" else ""} in ${t2 - t1} ms."
        )
        state.addInfo("Compiled $numDefs feature definition${if (numDefs != 1) "s" else ""} in ${t3 - t2} ms.")
        state.addInfo("Compiled ${rules.size} rule${if (rules.size != 1) "s" else ""} in ${t4 - t3} ms.")

        return PrlModel(header, featureStore, rules, propertyStore)
    }

    fun errors() = state.errors
    fun warnings() = state.warnings
    fun infos() = state.infos
    fun hasErrors() = state.hasErrors()

    ////////////////////////////////
    // Compile Slicing Properties //
    ////////////////////////////////
    private fun compileSlicingPropertyDefinitionsIntoStore(ruleFile: PrlRuleFile): PropertyStore =
        PropertyStore().apply {
            ruleFile.slicingPropertyDefinitions.forEach { addSlicingPropertyDefinition(it, state) }
            state.context.clear()
        }

    //////////////////////
    // Compile Features //
    //////////////////////
    private fun compileFeaturesIntoStore(
        ruleFile: PrlRuleFile,
        propertyStore: PropertyStore,
    ) =
        FeatureStore().apply {
            compileFeaturesIntoStore(ruleFile.ruleSet.featureDefinitions, ruleFile.ruleSet.rules, propertyStore, this)
            state.context.clear()
        }

    private fun compileFeaturesIntoStore(
        features: List<PrlFeatureDefinition>,
        rules: List<PrlRule>,
        propertyStore: PropertyStore,
        featureStore: FeatureStore
    ) {
        features.forEach {
            state.context.feature = it.code
            state.context.lineNumber = it.lineNumber
            if (hasInvalidFeatureName(it.code)) {
                state.addError("Feature name invalid: ${it.code}")
                return@forEach
            }
            propertyStore.addProperties(it, state)
            if (!state.hasErrors()) {
                featureStore.addDefinition(it, state, propertyStore.slicingPropertyDefinitions, false)
            }
        }
        rules.filterIsInstance<PrlGroupRule>().forEach {
            state.context.feature = it.group.featureCode
            state.context.lineNumber = it.lineNumber
            if (hasInvalidFeatureName(it.group.featureCode)) {
                state.addError("Rule name invalid: ${it.group.featureCode}")
                return@forEach
            }
            propertyStore.checkPropertiesCorrect(it.properties, compileProperties(it.propsMap()), state)
            if (!state.hasErrors()) {
                featureStore.addDefinition(
                    createFeatureDefinitionForGroup(it),
                    state,
                    propertyStore.slicingPropertyDefinitions,
                    true
                )
            }
        }
    }

    private fun hasInvalidFeatureName(featureName: String) = featureName.contains(".")


    private fun createFeatureDefinitionForGroup(rule: PrlGroupRule) =
        PrlBooleanFeatureDefinition(
            rule.group.featureCode,
            false,
            rule.description,
            rule.properties,
            rule.lineNumber
        )

    ///////////////////
    // Compile Rules //
    ///////////////////
    private fun compileRules(
        ruleFile: PrlRuleFile,
        propertyStore: PropertyStore,
        featureStore: FeatureStore,
    ) = if (!state.hasErrors()) {
        compileRules(ruleFile.ruleSet, propertyStore, featureStore)
    } else {
        emptyList()
    }


    private fun compileRules(
        ruleSet: PrlRuleSet,
        propertyStore: PropertyStore,
        featureStore: FeatureStore,
    ): List<AnyRule> {
        val theoryMap = featureStore.generateTheoryMap(ruleSet.features(), state)
        if (state.hasErrors()) return listOf()
        return ruleSet.rules.mapNotNull {
            state.context.ruleId = it.id.ifEmpty { randomUUID().toString() }
            propertyStore.addProperties(it, state)
            if (state.hasErrors()) {
                null
            } else {
                state.context.lineNumber = it.lineNumber
                compileRule(it, theoryMap)
            }
        }
    }

    internal fun compileRule(prlRule: PrlRule, map: Tmap): AnyRule? {
        return try {
            when (prlRule) {
                is PrlConstraintRule -> rule(prlRule, map)
                is PrlDefinitionRule -> rule(prlRule, map)
                is PrlExclusionRule -> rule(prlRule, map)
                is PrlForbiddenFeatureRule -> rule(prlRule, map)
                is PrlGroupRule -> rule(prlRule, map)
                is PrlIfThenElseRule -> rule(prlRule, map)
                is PrlInclusionRule -> rule(prlRule, map)
                is PrlMandatoryFeatureRule -> rule(prlRule, map)
                is PrlFeatureRule -> rule(prlRule, map)
            }
        } catch (e: CoCoException) {
            state.addError(e.message!!)
            null
        }
    }

    private fun rule(prl: PrlConstraintRule, map: Tmap) =
        ConstraintRule(
            cc.compileConstraint(prl.constraint, map),
            prl.id,
            prl.description,
            compileProperties(prl.propsMap()),
            prl.lineNumber
        )

    private fun rule(prl: PrlInclusionRule, map: Tmap) = InclusionRule(
        cc.compileConstraint(prl.ifPart, map),
        cc.compileConstraint(prl.thenPart, map),
        prl.id,
        prl.description,
        compileProperties(prl.propsMap()),
        prl.lineNumber
    )

    private fun rule(prl: PrlExclusionRule, map: Tmap) = ExclusionRule(
        cc.compileConstraint(prl.ifPart, map),
        cc.compileConstraint(prl.thenNotPart, map),
        prl.id,
        prl.description,
        compileProperties(prl.propsMap()),
        prl.lineNumber
    )

    private fun rule(prl: PrlDefinitionRule, map: Tmap) = DefinitionRule(
        cc.compileUnversionedBooleanFeature(prl.feature, map),
        cc.compileConstraint(prl.definition, map),
        prl.id,
        prl.description,
        compileProperties(prl.propsMap()),
        prl.lineNumber
    )

    private fun rule(prl: PrlIfThenElseRule, map: Tmap) = IfThenElseRule(
        cc.compileConstraint(prl.ifPart, map),
        cc.compileConstraint(prl.thenPart, map),
        cc.compileConstraint(prl.elsePart, map),
        prl.id,
        prl.description,
        compileProperties(prl.propsMap()),
        prl.lineNumber
    )

    private fun rule(prl: PrlGroupRule, map: Tmap) = GroupRule(
        prl.type,
        cc.compileUnversionedBooleanFeature(prl.group, map),
        cc.compileBooleanFeatures(prl.content, map).toSet(),
        prl.id,
        prl.description,
        compileProperties(prl.propsMap()),
        prl.lineNumber
    )

    private fun rule(prl: PrlFeatureRule, map: Tmap): AnyRule {
        val feature = validateFeature(prl, map)
        return if (prl is PrlForbiddenFeatureRule) {
            ForbiddenFeatureRule(
                feature,
                prl.enumValue,
                prl.intValueOrVersion,
                prl.id,
                prl.description,
                compileProperties(prl.propsMap()),
                prl.lineNumber
            )
        } else {
            MandatoryFeatureRule(
                feature,
                prl.enumValue,
                prl.intValueOrVersion,
                prl.id,
                prl.description,
                compileProperties(prl.propsMap()),
                prl.lineNumber
            )
        }
    }

    private fun validateFeature(prl: PrlFeatureRule, map: Tmap): Feature {
        val theory = map[prl.feature]
        if (theory == Theory.BOOL && (prl.enumValue != null || prl.intValueOrVersion != null)) {
            return invalidBoolean()
        } else if (theory == Theory.VERSIONED_BOOL && prl.intValueOrVersion == null) {
            return invalidVersioned()
        }
        val featureCode = prl.feature.featureCode
        return when (theory) {
            Theory.VERSIONED_BOOL -> versionFt(featureCode)
            Theory.BOOL -> boolFt(featureCode)
            Theory.ENUM -> if (prl.enumValue == null) invalidEnum() else enumFt(featureCode)
            Theory.INT -> if (prl.intValueOrVersion == null) invalidInt() else intFt(featureCode)
            null -> cc.unknownFeature(prl.feature)
        }
    }

    private fun invalidBoolean(): Nothing =
        throw CoCoException("Cannot assign an unversioned boolean feature to an int or enum value")

    private fun invalidVersioned(): Nothing =
        throw CoCoException("Cannot assign a versioned boolean feature to anything else than an int version")

    private fun invalidInt(): Nothing =
        throw CoCoException("Cannot assign an int feature to anything else than an int value")

    private fun invalidEnum(): Nothing =
        throw CoCoException("Cannot assign an enum feature to anything else than an enum value")
}

internal class CompilerState {
    internal val errors = mutableListOf<String>()
    internal val warnings = mutableListOf<String>()
    internal val infos = mutableListOf<String>()
    internal var context: CompilerContext = CompilerContext()

    internal fun hasErrors() = errors.isNotEmpty()
    internal fun addError(message: String) = errors.add("${contextString()}$message")
    internal fun addWarning(message: String) = warnings.add("${contextString()}$message")
    internal fun addInfo(message: String) = infos.add("${contextString()}$message")
    private fun contextString() = if (context.isEmpty()) "" else "$context "
}

internal class CompilerContext(
    var feature: String? = null,
    var ruleId: String? = null,
    var lineNumber: Int? = null
) {
    fun clear() {
        feature = null
        ruleId = null
        lineNumber = null
    }

    fun isEmpty() = feature == null && ruleId == null && lineNumber == null

    override fun toString(): String {
        val featureString = if (feature == null) null else "feature=$feature"
        val ruleIdString = if (ruleId == null) null else "ruleId=$ruleId"
        val lineNumberString = if (lineNumber == null) null else "lineNumber=$lineNumber"
        return listOfNotNull(featureString, ruleIdString, lineNumberString).joinToString(", ", "[", "]")
    }
}

typealias Tmap = Map<PrlFeature, Theory>

