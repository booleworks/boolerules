// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

@file:Suppress("UNCHECKED_CAST")

package com.booleworks.prl.parser

import com.booleworks.prl.model.rules.GroupType
import org.antlr.v4.runtime.CharStreams


/**
 * Parses the given string to a PRL rule and casts it to the given type [R].
 */
fun <R : PrlRule> parseRule(string: String): R {
    val parser = PrlParser.prepareParser(CharStreams.fromString(string));
    return parser.rule as R;
}

sealed class PrlRule(
    open val id: String = "",
    open val description: String = "",
    open val properties: List<PrlProperty<*>> = listOf(),
    open val lineNumber: Int? = null
) {
    abstract fun features(): Set<PrlFeature>
    fun propsMap() = properties.associateBy { it.name }
}

data class PrlConstraintRule(
    val constraint: PrlConstraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = constraint.features()
}

data class PrlDefinitionRule(
    val feature: PrlFeature,
    val definition: PrlConstraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = definition.features() + feature
}

data class PrlExclusionRule(
    val ifPart: PrlConstraint,
    val thenNotPart: PrlConstraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = ifPart.features() + thenNotPart.features()
}

data class PrlGroupRule(
    val type: GroupType,
    val group: PrlFeature,
    val content: List<PrlFeature>,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = content.toSet() + group
}

data class PrlIfThenElseRule(
    val ifPart: PrlConstraint,
    val thenPart: PrlConstraint,
    val elsePart: PrlConstraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = ifPart.features() + thenPart.features() + elsePart.features()
}

data class PrlInclusionRule(
    val ifPart: PrlConstraint,
    val thenPart: PrlConstraint,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = ifPart.features() + thenPart.features()
}

sealed class PrlFeatureRule(
    open val feature: PrlFeature,
    open val enumValue: String?,
    open val intValueOrVersion: Int?,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlRule(id, description, properties, lineNumber) {
    override fun features() = setOf(feature)
}

data class PrlMandatoryFeatureRule(
    override val feature: PrlFeature,
    override val enumValue: String?,
    override val intValueOrVersion: Int?,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlFeatureRule(feature, enumValue, intValueOrVersion, id, description, properties, lineNumber)

data class PrlForbiddenFeatureRule(
    override val feature: PrlFeature,
    override val enumValue: String?,
    override val intValueOrVersion: Int?,
    override val id: String = "",
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = listOf(),
    override val lineNumber: Int? = null
) : PrlFeatureRule(feature, enumValue, intValueOrVersion, id, description, properties, lineNumber)
