// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

@file:Suppress("UNCHECKED_CAST")

package com.booleworks.prl.parser

import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.parser.PrlParser.prepareParser
import org.antlr.v4.runtime.CharStreams

/**
 * Parses the given string to a PRL constraint and casts it to the given type [C].
 */
fun <C : PrlConstraint> parseConstraint(string: String): C {
    val parser = prepareParser(CharStreams.fromString(string));
    return parser.constraint as C;
}

// Boolean constraints
sealed interface PrlConstraint {
    fun features(): Set<PrlFeature>
}

data class PrlConstant(val value: Boolean) : PrlConstraint {
    override fun features() = setOf<PrlFeature>()
}

data class PrlNot(val operand: PrlConstraint) : PrlConstraint {
    override fun features() = operand.features()
}

data class PrlImplication(val left: PrlConstraint, val right: PrlConstraint) : PrlConstraint {
    override fun features() = left.features() + right.features()
}

data class PrlEquivalence(val left: PrlConstraint, val right: PrlConstraint) : PrlConstraint {
    override fun features() = left.features() + right.features()
}

data class PrlAmo(val features: Collection<PrlFeature>) : PrlConstraint {
    constructor(vararg features: PrlFeature) : this(features.toList())

    override fun features() = features.toSet()
}

data class PrlExo(val features: Collection<PrlFeature>) : PrlConstraint {
    constructor(vararg features: PrlFeature) : this(features.toList())

    override fun features() = features.toSet()
}

data class PrlAnd(val operands: Collection<PrlConstraint>) : PrlConstraint {
    constructor(vararg operands: PrlConstraint) : this(operands.toList())

    override fun features() = operands.flatMap { it.features() }.toSet()
}

data class PrlOr(val operands: Collection<PrlConstraint>) : PrlConstraint {
    constructor(vararg operands: PrlConstraint) : this(operands.toList())

    override fun features() = operands.flatMap { it.features() }.toSet()
}

// Atomic constraints
sealed interface PrlTerm {
    fun features(): Set<PrlFeature>
}

sealed interface PrlFunction : PrlTerm

data class PrlFeature(val featureCode: String) : PrlConstraint, PrlTerm {
    override fun features() = setOf(this)
}

data class PrlComparisonPredicate(val comparison: ComparisonOperator, val left: PrlTerm, val right: PrlTerm) :
    PrlConstraint {
    override fun features() = left.features() + right.features()
}

data class PrlIntValue(val value: Int) : PrlTerm {
    override fun features() = setOf<PrlFeature>()
}

data class PrlEnumValue(val value: String) : PrlTerm {
    override fun features() = setOf<PrlFeature>()
}

data class PrlIntMulFunction(val left: PrlTerm, val right: PrlTerm) : PrlFunction {
    override fun features() = left.features() + right.features()
}

data class PrlIntAddFunction(val operands: Collection<PrlTerm>) : PrlFunction {
    constructor(vararg operands: PrlTerm) : this(operands.toList())

    override fun features() = operands.flatMap { it.features() }.toSet()
}

data class PrlInIntRangePredicate(val term: PrlTerm, val range: IntRange) : PrlConstraint {
    override fun features() = term.features()
}

data class PrlInEnumsPredicate(val term: PrlTerm, val values: Collection<String>) : PrlConstraint {
    constructor(term: PrlTerm, vararg values: String) : this(term, values.toList())

    override fun features() = term.features()
}

