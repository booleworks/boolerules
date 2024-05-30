// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser

import com.booleworks.prl.model.constraints.ConstraintType
import com.booleworks.prl.model.rules.GroupType
import com.booleworks.prl.parser.internal.PragmaticRuleLanguageLexer
import java.util.regex.Pattern
import java.util.stream.Collectors

object PragmaticRuleLanguage {
    val VERSION = PrlVersion(1, 0)

    val KEYWORD_HEADER: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.HEADER)
    val KEYWORD_PRL_VERSION: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.PRL_VERSION)
    val KEYWORD_FEATURE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.FEATURE)
    val KEYWORD_VERSIONED: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.VERS)
    val KEYWORD_BOOL: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.BOOL)
    val KEYWORD_INT: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.INT)
    val KEYWORD_ENUM: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.ENUM)

    val KEYWORD_RULE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.RULE)
    val KEYWORD_SLICING: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.SLICING)
    val KEYWORD_PROPERTIES: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.PROPERTIES)
    val KEYWORD_DATE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.DATE)
    val KEYWORD_IS: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.IS)
    val KEYWORD_IF: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.IF)
    val KEYWORD_THEN: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.THEN)
    val KEYWORD_THEN_NOT: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.THEN_NOT)
    val KEYWORD_ELSE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.ELSE)
    val KEYWORD_MANDATORY: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.MANDATORY)
    val KEYWORD_OPTIONAL: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.OPTIONAL)
    val KEYWORD_FORBIDDEN: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.FORBIDDEN)
    val KEYWORD_GROUP: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.GROUP)
    val KEYWORD_CONTAINS: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.CONTAINS)
    val KEYWORD_ID: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.ID)
    val KEYWORD_DESCRIPTION: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.DESC)
    val KEYWORD_TRUE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.TRUE)
    val KEYWORD_FALSE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.FALSE)
    val KEYWORD_IN: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.IN)
    val KEYWORD_AMO: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.AMO)
    val KEYWORD_EXO: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.EXO)
    val SYMBOL_LPAR: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.LPAR)
    val SYMBOL_RPAR: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.RPAR)
    val SYMBOL_LBRA: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.LBRA)
    val SYMBOL_RBRA: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.RBRA)
    val SYMBOL_LSQB: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.LSQB)
    val SYMBOL_RSQB: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.RSQB)
    val SYMBOL_COMMA: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.COMMA)
    val SYMBOL_NOT_MINUS: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.NOT_MINUS)
    val SYMBOL_AND: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.AND)
    val SYMBOL_OR: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.OR)
    val SYMBOL_EQUIV: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.EQUIV)
    val SYMBOL_IMPL: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.IMPL)
    val SYMBOL_EQ: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.EQ)
    val SYMBOL_NE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.NE)
    val SYMBOL_LT: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.LT)
    val SYMBOL_LE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.LE)
    val SYMBOL_GT: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.GT)
    val SYMBOL_GE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.GE)
    val SYMBOL_ADD: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.ADD)
    val SYMBOL_MUL: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.MUL)
    val SYMBOL_QUOTE: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.QUOTE)
    val SYMBOL_BACKTICK: String = PragmaticRuleLanguageLexer.symbol(PragmaticRuleLanguageLexer.BACKTICK)

    const val INDENT: String = "  "
    val KEYWORDS = setOf(
        KEYWORD_HEADER, KEYWORD_PRL_VERSION,
        KEYWORD_FEATURE, KEYWORD_VERSIONED, KEYWORD_BOOL, KEYWORD_INT, KEYWORD_ENUM,
        KEYWORD_RULE, KEYWORD_IS, KEYWORD_IF, KEYWORD_THEN, KEYWORD_THEN_NOT, KEYWORD_ELSE,
        KEYWORD_MANDATORY, KEYWORD_OPTIONAL, KEYWORD_FORBIDDEN, KEYWORD_GROUP, KEYWORD_CONTAINS,
        KEYWORD_ID, KEYWORD_DESCRIPTION,
        KEYWORD_TRUE, KEYWORD_FALSE, KEYWORD_IN, KEYWORD_AMO, KEYWORD_EXO,
        KEYWORD_SLICING, KEYWORD_PROPERTIES, KEYWORD_DATE
    )

    /**
     * Regex Pattern for a featureCode that is a valid pure identifier in the rule file.
     * Otherwise, it is an identifier wrapped in backticks.
     */
    private val identifierRegexPattern: Pattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\-.]*")

    fun bracket(string: String?) = SYMBOL_LPAR + string + SYMBOL_RPAR
    fun identifier(code: String) =
        if (KEYWORDS.contains(code) || !identifierRegexPattern.matcher(code).matches()) SYMBOL_BACKTICK + code +
                SYMBOL_BACKTICK else code

    fun quote(string: String) = SYMBOL_QUOTE + string + SYMBOL_QUOTE
    fun quote(objects: Collection<*>): String = objects.stream().map { o: Any? -> SYMBOL_QUOTE + o + SYMBOL_QUOTE }
        .collect(Collectors.joining("$SYMBOL_COMMA ", SYMBOL_LSQB, SYMBOL_RSQB))

    fun groupTypeString(groupType: GroupType) =
        if (groupType == GroupType.MANDATORY) KEYWORD_MANDATORY else KEYWORD_OPTIONAL

    fun ccString(type: ConstraintType) = if (type == ConstraintType.AMO) KEYWORD_AMO else KEYWORD_EXO
    fun constantString(type: ConstraintType) = if (type == ConstraintType.TRUE) KEYWORD_TRUE else KEYWORD_FALSE
    fun range(string: String) = if (string.startsWith(SYMBOL_LSQB)) string else "$SYMBOL_LSQB$string$SYMBOL_RSQB"
}

data class PrlVersion(val major: Int, val minor: Int) {
    override fun toString() = "$major.$minor"
}
