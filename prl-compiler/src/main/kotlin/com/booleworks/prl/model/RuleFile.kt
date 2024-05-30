// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_HEADER
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_PRL_VERSION
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_PROPERTIES
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_SLICING
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LBRA
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RBRA
import java.util.Objects

class RuleFile(
    val header: PrlModelHeader,
    val ruleSet: RuleSet,
    val slicingProperties: Map<String, AnySlicingPropertyDefinition>,
    val fileName: String? = null
) {
    fun rename(renaming: FeatureRenaming) = RuleFile(header, ruleSet.rename(renaming), slicingProperties, fileName)
    fun stripProperties() = RuleFile(header.stripProperties(), ruleSet.stripProperties(), slicingProperties, fileName)
    fun stripMetaInfo() = RuleFile(header, ruleSet.stripMetaInfo(), slicingProperties, fileName)
    fun stripAll() = RuleFile(header.stripProperties(), ruleSet.stripAll(), slicingProperties, fileName)

    override fun toString() = StringBuilder().apply { appendString(this) }.toString()
    override fun hashCode() = Objects.hash(fileName, slicingProperties, ruleSet)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RuleFile
        if (ruleSet != other.ruleSet) return false
        if (slicingProperties != other.slicingProperties) return false
        return fileName == other.fileName
    }

    private fun appendString(appendable: Appendable) {
        appendable.append(KEYWORD_HEADER).append(" ").append(SYMBOL_LBRA).append(System.lineSeparator())
        appendable.append("  ").append(KEYWORD_PRL_VERSION).append(" ").append(header.version.toString())
            .append(System.lineSeparator())
        header.properties.forEach { appendable.append("  ").append(it.value.toString()).append(System.lineSeparator()) }
        appendable.append(SYMBOL_RBRA).append(System.lineSeparator()).append(System.lineSeparator())
        if (slicingProperties.isNotEmpty()) {
            appendable.append(KEYWORD_SLICING).append(" ").append(KEYWORD_PROPERTIES).append(" ").append(SYMBOL_LBRA)
                .append(System.lineSeparator())
            val slicingPropertyValues = slicingProperties.values.toList()
            slicingPropertyValues.forEach { it.appendString(appendable.append("  ")).append(System.lineSeparator()) }
            appendable.append(SYMBOL_RBRA).append(System.lineSeparator()).append(System.lineSeparator())
        }
        ruleSet.appendString(appendable).append(System.lineSeparator())
    }
}

class RuleSet(
    val featureDefinitions: List<AnyFeatureDef>,
    val rules: List<AnyRule>,
) {
    fun rename(renaming: FeatureRenaming) =
        RuleSet(
            featureDefinitions.map { it.rename(renaming) },
            rules.map { it.rename(renaming) },
        )

    fun stripProperties() = RuleSet(
        featureDefinitions.map { it.stripProperties() },
        rules.map { it.stripProperties() },
    )

    fun stripMetaInfo() = RuleSet(
        featureDefinitions.map { it.stripMetaInfo() },
        rules.map { it.stripMetaInfo() },
    )

    fun stripAll() = RuleSet(featureDefinitions.map { it.stripAll() }, rules.map { it.stripAll() })

    override fun hashCode() = Objects.hash(featureDefinitions, rules)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RuleSet
        if (featureDefinitions != other.featureDefinitions) return false
        if (rules != other.rules) return false
        return true
    }

    override fun toString() = StringBuilder().apply { appendString(this) }.toString()
    internal fun appendString(app: Appendable): Appendable {
        if (featureDefinitions.isNotEmpty()) {
            featureDefinitions.forEach { it.appendString(app, 1).append(System.lineSeparator()) }
            if (rules.isNotEmpty()) app.append(System.lineSeparator())
        }
        rules.forEach { it.appendString(app, 1).append(System.lineSeparator()) }
        return app
    }
}
