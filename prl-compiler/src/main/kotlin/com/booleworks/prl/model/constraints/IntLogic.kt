// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_IN
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_MUL
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_NOT_MINUS
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.range
import java.util.Objects

fun intFt(featureCode: String) = IntFeature(featureCode)
fun intVal(value: Int) = IntValue(value)

fun intMul(coefficient: Int, feature: IntFeature) = IntMul(coefficient, feature)
fun intMul(feature: IntFeature) = IntMul(1, feature)
fun intMul(feature: IntFeature, polarity: Boolean) = IntMul(if (polarity) 1 else -1, feature)
fun intSum(offset: Int, operands: Collection<IntMul>) = IntSum(operands.toList(), offset)
fun intSum(vararg operands: IntMul) = IntSum(operands.toList(), 0)
fun intSum(offset: Int, vararg operands: IntMul) = IntSum(operands.toList(), offset)

fun intEq(left: IntTerm, right: Int) = IntComparisonPredicate(left, right.toIntValue(), ComparisonOperator.EQ)
fun intEq(left: IntTerm, right: IntTerm) = IntComparisonPredicate(left, right, ComparisonOperator.EQ)
fun intNe(left: IntTerm, right: Int): IntComparisonPredicate =
    IntComparisonPredicate(left, right.toIntValue(), ComparisonOperator.NE)

fun intNe(left: IntTerm, right: IntTerm) = IntComparisonPredicate(left, right, ComparisonOperator.NE)
fun intLt(left: IntTerm, right: Int) = IntComparisonPredicate(left, right.toIntValue(), ComparisonOperator.LT)
fun intLt(left: IntTerm, right: IntTerm) = IntComparisonPredicate(left, right, ComparisonOperator.LT)
fun intLe(left: IntTerm, right: Int) = IntComparisonPredicate(left, right.toIntValue(), ComparisonOperator.LE)
fun intLe(left: IntTerm, right: IntTerm) = IntComparisonPredicate(left, right, ComparisonOperator.LE)
fun intGt(left: IntTerm, right: Int) = IntComparisonPredicate(left, right.toIntValue(), ComparisonOperator.GT)
fun intGt(left: IntTerm, right: IntTerm) = IntComparisonPredicate(left, right, ComparisonOperator.GT)
fun intGe(left: IntTerm, right: Int) = IntComparisonPredicate(left, right.toIntValue(), ComparisonOperator.GE)
fun intGe(left: IntTerm, right: IntTerm) = IntComparisonPredicate(left, right, ComparisonOperator.GE)
fun intIn(term: IntTerm, range: IntRange) = IntInPredicate(term, range)

fun intComparison(left: IntTerm, right: Int, comparison: ComparisonOperator) =
    IntComparisonPredicate(left, right.toIntValue(), comparison)

fun intComparison(left: IntTerm, right: IntTerm, comparison: ComparisonOperator) =
    IntComparisonPredicate(left, right, comparison)

sealed interface IntPredicate : Predicate {
    override fun booleanFeatures() = setOf<BooleanFeature>()
    override fun enumFeatures() = setOf<EnumFeature>()
    override fun enumValues() = mapOf<EnumFeature, Set<String>>()
}

sealed interface IntTerm {
    fun value(assignment: FeatureAssignment): Int
    fun normalize(): IntTerm
    fun restrict(assignment: FeatureAssignment): IntTerm
    fun rename(renaming: FeatureRenaming): IntTerm
    fun features(): Set<IntFeature>
}

class IntFeature internal constructor(override val featureCode: String) : Feature(featureCode), IntTerm {
    override fun value(assignment: FeatureAssignment) = assignment.getInt(this)
        ?: throw IllegalArgumentException("Integer Feature $featureCode is not assigned to any value")

    override fun normalize() = this
    override fun rename(renaming: FeatureRenaming) = renaming.rename(this)
    override fun features() = setOf(this)
    override fun restrict(assignment: FeatureAssignment) = assignment.getInt(this).let { it?.toIntValue() ?: this }

    override fun hashCode() = Objects.hash(featureCode)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as IntFeature
        return featureCode == other.featureCode
    }

}

data class IntValue internal constructor(val value: Int) : IntTerm {
    override fun value(assignment: FeatureAssignment) = value
    override fun normalize() = this
    override fun restrict(assignment: FeatureAssignment) = this
    override fun rename(renaming: FeatureRenaming) = this
    override fun features() = setOf<IntFeature>()
    override fun toString() = value.toString()
}

fun Int.toIntValue() = IntValue(this)

data class IntMul internal constructor(val coefficient: Int, val feature: IntFeature) : IntTerm {
    override fun value(assignment: FeatureAssignment) = coefficient * feature.value(assignment)
    override fun normalize() = if (coefficient == 1) feature else this
    override fun restrict(assignment: FeatureAssignment) = simplifiedTerm(feature.restrict(assignment))
    override fun rename(renaming: FeatureRenaming) = IntMul(coefficient, feature.rename(renaming))
    override fun features() = setOf(feature)

    private fun simplifiedTerm(restrictedTerm: IntTerm) = if (restrictedTerm is IntValue) {
        (coefficient * restrictedTerm.value).toIntValue()
    } else if (coefficient == 0) {
        0.toIntValue()
    } else if (restrictedTerm is IntFeature) {
        IntMul(coefficient, restrictedTerm)
    } else {
        throw IllegalArgumentException("Cannot create a multiplication with a " + restrictedTerm.javaClass)
    }

    override fun toString() = when (coefficient) {
        1 -> feature.toString()
        -1 -> SYMBOL_NOT_MINUS + feature.toString()
        else -> coefficient.toString() + SYMBOL_MUL + feature.toString()
    }
}

data class IntSum internal constructor(val operands: List<IntMul>, val offset: Int) : IntTerm {
    override fun value(assignment: FeatureAssignment) = operands.sumOf { it.value(assignment) } + offset
    override fun normalize() = if (offset == 0 && operands.size == 1) operands[0].normalize() else this
    override fun restrict(assignment: FeatureAssignment) = simplifyTerm(operands.map { it.restrict(assignment) })
    override fun rename(renaming: FeatureRenaming) = IntSum(operands.map { it.rename(renaming) }, offset)
    override fun features() = operands.map { it.feature }.toSet()

    private fun simplifyTerm(simplifiedTerms: List<IntTerm>): IntTerm {
        var newOffset = offset
        val newOps: MutableList<IntMul> = mutableListOf()
        for (term in simplifiedTerms) {
            when (term) {
                is IntValue -> newOffset += term.value
                is IntSum -> newOps.addAll(term.operands)
                is IntFeature -> newOps.add(intMul(term))
                is IntMul -> newOps.add(term)
            }
        }
        return if (newOps.isEmpty()) newOffset.toIntValue() else IntSum(newOps, newOffset)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val size = operands.size
        var last: IntMul? = null
        for ((count, op) in operands.withIndex()) {
            if (count + 1 == size) last = op else sb.append(op.toString()).append(" ")
                .append(PragmaticRuleLanguage.SYMBOL_ADD).append(" ")
        }
        if (last != null) sb.append(last.toString())
        if (offset != 0) {
            if (last != null) sb.append(" ").append(PragmaticRuleLanguage.SYMBOL_ADD).append(" ")
            sb.append(offset)
        }
        return sb.toString()
    }
}

data class IntInPredicate internal constructor(val term: IntTerm, val range: IntRange) : IntPredicate {
    override val type = ConstraintType.ATOM
    override fun features() = term.features()
    override fun intFeatures() = term.features()
    override fun containsIntFeatures() = true
    override fun evaluate(assignment: FeatureAssignment) = range.contains(term.value(assignment))
    override fun restrict(assignment: FeatureAssignment) = simplifiedConstraint(term.restrict(assignment))
    override fun rename(renaming: FeatureRenaming) = IntInPredicate(term.rename(renaming), range)
    override fun syntacticSimplify() = if (range.isEmpty()) FALSE else this

    private fun simplifiedConstraint(restricted: IntTerm) = if (restricted is IntFeature) {
        IntInPredicate(restricted, range)
    } else {
        Constant(range.contains((restricted as IntValue).value))
    }

    override fun toString() = "$SYMBOL_LSQB${term} $KEYWORD_IN ${range(range.toString())}$SYMBOL_RSQB"
}

data class IntComparisonPredicate internal constructor(
    val left: IntTerm,
    val right: IntTerm,
    val comparison: ComparisonOperator
) : IntPredicate {
    override val type = ConstraintType.ATOM
    override fun features() = left.features() + right.features()
    override fun intFeatures() = left.features() + right.features()
    override fun containsIntFeatures() = true
    override fun evaluate(assignment: FeatureAssignment) =
        comparison.evaluate(left.value(assignment), right.value(assignment))

    override fun restrict(assignment: FeatureAssignment) =
        simplifiedConstraint(left.restrict(assignment), right.restrict(assignment))

    override fun rename(renaming: FeatureRenaming) =
        IntComparisonPredicate(left.rename(renaming), right.rename(renaming), comparison)

    override fun syntacticSimplify() = this

    private fun simplifiedConstraint(restricted1: IntTerm, restricted2: IntTerm) =
        if (restricted1 is IntValue && restricted2 is IntValue) {
            Constant(comparison.evaluate(restricted1.value, restricted2.value))
        } else {
            IntComparisonPredicate(restricted1, restricted2, comparison)
        }

    fun hasEqualConstraint(that: IntComparisonPredicate) =
        comparison == that.comparison && left == that.left && right == that.right
                || comparison == that.comparison.reverse() && left == that.right && right == that.left

    override fun toString() = "$SYMBOL_LSQB$left ${comparison.symbol} $right$SYMBOL_RSQB"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IntComparisonPredicate
        return hasEqualConstraint(other)
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result += right.hashCode()
        result = 31 * result + comparison.hashCode()
        return result
    }
}
