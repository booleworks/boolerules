// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.rules

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.evaluateProperties
import com.booleworks.prl.parser.PragmaticRuleLanguage.INDENT
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_DESCRIPTION
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_ID
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_RULE
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LBRA
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RBRA
import com.booleworks.prl.parser.PragmaticRuleLanguage.quote
import java.util.Objects

typealias AnyRule = Rule<*>

sealed class Rule<R>(
    open val id: String,
    open val description: String,
    open val properties: Map<String, AnyProperty>,
    open val lineNumber: Int? = null
) {
    constructor(rule: Rule<R>) : this(rule.id, rule.description, rule.properties, rule.lineNumber)

    abstract fun features(): Set<Feature>
    abstract fun booleanFeatures(): Set<BooleanFeature>
    abstract fun enumFeatures(): Set<EnumFeature>
    abstract fun enumValues(): Map<EnumFeature, Set<String>>
    abstract fun intFeatures(): Set<IntFeature>
    abstract fun containsBooleanFeatures(): Boolean
    abstract fun containsEnumFeatures(): Boolean
    abstract fun containsIntFeatures(): Boolean
    abstract fun evaluate(assignment: FeatureAssignment): Boolean
    abstract fun restrict(assignment: FeatureAssignment): AnyRule
    abstract fun syntacticSimplify(): AnyRule
    abstract fun rename(renaming: FeatureRenaming): Rule<R>
    abstract fun stripProperties(): Rule<R>
    abstract fun stripMetaInfo(): Rule<R>
    abstract fun stripAll(): Rule<R>
    abstract fun headerLine(): String

    fun appendString(appendable: Appendable, depth: Int): Appendable {
        val i = INDENT.repeat(depth)
        val ii = i + INDENT
        val rulePrefix = if (this is GroupRule) "" else "$KEYWORD_RULE "
        if (description.isBlank() && id.isBlank() && properties.isEmpty()) {
            appendable.append(i).append(rulePrefix).append(headerLine())
        } else {
            appendable.append(i).append(rulePrefix).append(headerLine()).append(" ")
                .append(SYMBOL_LBRA).append(System.lineSeparator())
            if (id.isNotBlank()) {
                appendable.append(ii).append(KEYWORD_ID).append(" ").append(quote(id))
                    .append(System.lineSeparator())
            }
            if (description.isNotBlank()) {
                appendable.append(ii).append(KEYWORD_DESCRIPTION).append(" ").append(quote(description)).append(
                    System.lineSeparator()
                )
            }
            for (property in properties) {
                appendable.append(ii).append(property.value.toString()).append(System.lineSeparator())
            }
            appendable.append(i).append(SYMBOL_RBRA)
        }
        return appendable
    }

    fun filter(sliceSelections: List<AnySliceSelection>) = evaluateProperties(properties, sliceSelections)

    override fun toString() = StringBuilder().apply { appendString(this, 0) }.toString()
    override fun hashCode() = Objects.hash(id, description, properties)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AnyRule
        if (id != other.id) return false
        if (description != other.description) return false
        if (properties != other.properties) return false
        return true
    }
}
