// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser

import org.antlr.v4.runtime.CharStreams
import java.io.File
import java.io.Reader
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Parses the given string to a PRL rule file.
 */
fun parseRuleFile(reader: Reader, fileName: String): PrlRuleFile =
    PrlParser.prepareParser(CharStreams.fromReader(reader)).ruleFile(fileName)

/**
 * Parses the given file to a PRL rule file.
 */
fun parseRuleFile(fileName: String): PrlRuleFile = parseRuleFile(Paths.get(fileName))

/**
 * Parses the given file to a PRL rule file.
 */
fun parseRuleFile(file: Path): PrlRuleFile = parseRuleFile(file.toFile())

/**
 * Parses the given file to a PRL rule file.
 */
fun parseRuleFile(file: File): PrlRuleFile =
    PrlParser.prepareParser(CharStreams.fromFileName(file.absolutePath)).ruleFile(file.name)

data class PrlHeader(val major: Int, val minor: Int, val properties: List<PrlProperty<*>> = emptyList())

data class PrlRuleSet(
    val featureDefinitions: List<PrlFeatureDefinition> = listOf(),
    val rules: List<PrlRule> = listOf(),
    val lineNumber: Int? = null
) {
    fun features() = rules.flatMap { it.features() }.toSet()
}

data class PrlRuleFile(
    val header: PrlHeader,
    val ruleSet: PrlRuleSet,
    val slicingPropertyDefinitions: List<PrlSlicingPropertyDefinition> = mutableListOf(),
    val fileName: String? = null
) {
    fun features() = ruleSet.features()
}
