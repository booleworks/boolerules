// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.constraints.ComparisonOperator.EQ
import com.booleworks.prl.model.constraints.ComparisonOperator.NE
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_IN
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.quote
import java.util.Objects

fun enumFt(featureCode: String) = EnumFeature(featureCode)
fun enumVal(value: String) = EnumValue(value)
fun enumEq(feature: EnumFeature, value: EnumValue) = EnumComparisonPredicate(feature, value, EQ)
fun enumEq(feature: EnumFeature, value: String) = EnumComparisonPredicate(feature, EnumValue(value), EQ)
fun enumNe(feature: EnumFeature, value: EnumValue) = EnumComparisonPredicate(feature, value, NE)
fun enumNe(feature: EnumFeature, value: String) = EnumComparisonPredicate(feature, EnumValue(value), NE)
fun enumIn(feature: EnumFeature, values: Collection<String>) = EnumInPredicate(feature, values.toSortedSet())
fun enumIn(feature: EnumFeature, vararg values: String) = EnumInPredicate(feature, values.toSortedSet())
fun enumComparison(feature: EnumFeature, value: EnumValue, comparison: ComparisonOperator) =
    EnumComparisonPredicate(feature, value, comparison)

sealed interface EnumPredicate : Predicate {
    override fun booleanFeatures() = setOf<BooleanFeature>()
    override fun intFeatures() = setOf<IntFeature>()
}

sealed interface EnumTerm {
    fun value(assignment: FeatureAssignment): String
    fun restrict(assignment: FeatureAssignment): EnumTerm
    fun rename(feature: EnumFeature, renaming: FeatureRenaming): EnumTerm
}

class EnumFeature internal constructor(override val featureCode: String) : Feature(featureCode), EnumTerm {
    override fun value(assignment: FeatureAssignment) = assignment.getEnum(this)
        ?: throw IllegalArgumentException("Enum Feature $featureCode is not assigned to any value")

    override fun rename(feature: EnumFeature, renaming: FeatureRenaming) = renaming.rename(this)
    override fun restrict(assignment: FeatureAssignment) = assignment.getEnum(this).let { it?.toEnumValue() ?: this }

    override fun hashCode() = Objects.hash(featureCode)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as EnumFeature
        return (featureCode == other.featureCode)
    }
}

data class EnumValue internal constructor(val value: String) : EnumTerm {
    override fun value(assignment: FeatureAssignment) = value
    override fun restrict(assignment: FeatureAssignment) = this
    override fun rename(feature: EnumFeature, renaming: FeatureRenaming) =
        renaming.rename(feature, value).toEnumValue()

    override fun toString() = quote(value)
}

fun String.toEnumValue(): EnumValue = EnumValue(this)

data class EnumComparisonPredicate internal constructor(
    val feature: EnumFeature,
    val value: EnumValue,
    val comparison: ComparisonOperator
) : EnumPredicate {
    override val type = ConstraintType.ATOM
    fun hasEqualConstraint(that: EnumComparisonPredicate) =
        comparison == that.comparison && feature == that.feature && value == that.value

    override fun features() = setOf(feature)
    override fun enumFeatures() = setOf(feature)
    override fun enumValues() = mapOf(feature to setOf(value.value))
    override fun containsEnumFeatures() = true
    override fun restrict(assignment: FeatureAssignment) =
        simplifiedConstraint(feature.restrict(assignment), value.restrict(assignment))

    override fun rename(renaming: FeatureRenaming) =
        EnumComparisonPredicate(renaming.rename(feature), value.rename(feature, renaming), comparison)

    override fun syntacticSimplify() = this
    override fun evaluate(assignment: FeatureAssignment) = when (comparison) {
        EQ -> feature.value(assignment) == value.value(assignment)
        NE -> feature.value(assignment) != value.value(assignment)
        else -> false
    }

    private fun simplifiedConstraint(restrictedFeature: EnumTerm, value: EnumValue) =
        if (restrictedFeature is EnumFeature) {
            EnumComparisonPredicate(restrictedFeature, value, comparison)
        } else {
            Constant(comparison == EQ == ((restrictedFeature as EnumValue).value == value.value))
        }

    override fun toString() = "$SYMBOL_LSQB${feature} ${comparison.symbol} $value$SYMBOL_RSQB"
}

data class EnumInPredicate internal constructor(val feature: EnumFeature, val values: Set<String>) : EnumPredicate {
    override val type = ConstraintType.ATOM
    override fun features() = setOf(feature)
    override fun enumFeatures() = setOf(feature)
    override fun enumValues() = mapOf(feature to values.toSet())
    override fun containsEnumFeatures() = true
    override fun evaluate(assignment: FeatureAssignment) = values.contains(feature.value(assignment))
    override fun restrict(assignment: FeatureAssignment) = simplifiedConstraint(feature.restrict(assignment))
    override fun rename(renaming: FeatureRenaming) =
        enumIn(renaming.rename(feature), values.map { renaming.rename(feature, it) })

    override fun syntacticSimplify() = if (values.isEmpty()) Constant(false) else this

    private fun simplifiedConstraint(restricted: EnumTerm) = if (restricted is EnumFeature) {
        EnumInPredicate(restricted, values)
    } else {
        Constant(values.contains((restricted as EnumValue).value))
    }

    override fun toString() = "$SYMBOL_LSQB${feature} $KEYWORD_IN ${quote(values)}$SYMBOL_RSQB"
}
