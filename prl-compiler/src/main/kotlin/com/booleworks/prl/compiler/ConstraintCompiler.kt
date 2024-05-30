// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.compiler

import com.booleworks.prl.model.Theory
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.IntMul
import com.booleworks.prl.model.constraints.IntTerm
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.constraints.amo
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.constant
import com.booleworks.prl.model.constraints.enumComparison
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.enumIn
import com.booleworks.prl.model.constraints.enumVal
import com.booleworks.prl.model.constraints.equiv
import com.booleworks.prl.model.constraints.exo
import com.booleworks.prl.model.constraints.impl
import com.booleworks.prl.model.constraints.intComparison
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.intIn
import com.booleworks.prl.model.constraints.intMul
import com.booleworks.prl.model.constraints.intSum
import com.booleworks.prl.model.constraints.intVal
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.constraints.versionComparison
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.parser.PrlAmo
import com.booleworks.prl.parser.PrlAnd
import com.booleworks.prl.parser.PrlComparisonPredicate
import com.booleworks.prl.parser.PrlConstant
import com.booleworks.prl.parser.PrlConstraint
import com.booleworks.prl.parser.PrlEnumValue
import com.booleworks.prl.parser.PrlEquivalence
import com.booleworks.prl.parser.PrlExo
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlImplication
import com.booleworks.prl.parser.PrlInEnumsPredicate
import com.booleworks.prl.parser.PrlInIntRangePredicate
import com.booleworks.prl.parser.PrlIntAddFunction
import com.booleworks.prl.parser.PrlIntMulFunction
import com.booleworks.prl.parser.PrlIntValue
import com.booleworks.prl.parser.PrlNot
import com.booleworks.prl.parser.PrlOr
import com.booleworks.prl.parser.PrlTerm

class CoCoException(message: String) : Exception(message)

class ConstraintCompiler {
    fun compileConstraint(constraint: PrlConstraint, theoryMap: Tmap): Constraint =
        when (constraint) {
            is PrlConstant -> constant(constraint.value)
            is PrlAmo -> amo(compileBooleanFeatures(constraint.features, theoryMap))
            is PrlAnd -> and(constraint.operands.map { compileConstraint(it, theoryMap) })
            is PrlComparisonPredicate -> compileComparison(constraint, theoryMap)
            is PrlEquivalence -> equiv(
                compileConstraint(constraint.left, theoryMap),
                compileConstraint(constraint.right, theoryMap)
            )
            is PrlExo -> exo(compileBooleanFeatures(constraint.features, theoryMap))
            is PrlFeature -> compileBooleanFeature(constraint, theoryMap)
            is PrlImplication -> impl(
                compileConstraint(constraint.left, theoryMap),
                compileConstraint(constraint.right, theoryMap)
            )
            is PrlInEnumsPredicate -> compileEnumInPredicate(constraint, theoryMap)
            is PrlInIntRangePredicate -> compileIntInPredicate(constraint, theoryMap)
            is PrlNot -> not(compileConstraint(constraint.operand, theoryMap))
            is PrlOr -> or(constraint.operands.map { compileConstraint(it, theoryMap) })
        }

    internal fun compileUnversionedBooleanFeature(feature: PrlFeature, theoryMap: Tmap) =
        compileBooleanFeature(feature, theoryMap).also {
            if (it is VersionedBooleanFeature) throw CoCoException("Feature '${feature.featureCode}' is a versioned feature")
        }

    private fun compileBooleanFeature(feature: PrlFeature, theoryMap: Tmap) =
        when (val theory = theoryMap[feature]) {
            null -> unknownFeature(feature)
            Theory.BOOL -> boolFt(feature.featureCode)
            Theory.VERSIONED_BOOL -> versionFt(feature.featureCode)
            else -> wrongFeatureTheory(feature, theory, Theory.BOOL)
        }

    internal fun compileBooleanFeatures(
        features: Collection<PrlFeature>,
        featuresMap: Tmap
    ): Collection<BooleanFeature> =
        mutableListOf<BooleanFeature>().apply {
            features.forEach {
                when (val theory = featuresMap[it]) {
                    null -> unknownFeature(it)
                    Theory.BOOL -> add(boolFt(it.featureCode))
                    else -> wrongFeatureTheory(it, theory, Theory.BOOL)
                }
            }
        }

    private fun compileIntFeature(feature: PrlFeature, theoryMap: Tmap): IntFeature =
        when (val theory = theoryMap[feature]) {
            null -> unknownFeature(feature)
            Theory.INT -> intFt(feature.featureCode)
            else -> wrongFeatureTheory(feature, theory, Theory.INT)
        }

    private fun compileEnumFeature(feature: PrlFeature, theoryMap: Tmap): EnumFeature =
        when (val theory = theoryMap[feature]) {
            null -> unknownFeature(feature)
            Theory.ENUM -> enumFt(feature.featureCode)
            else -> wrongFeatureTheory(feature, theory, Theory.ENUM)
        }

    private fun compileEnumInPredicate(predicate: PrlInEnumsPredicate, theoryMap: Tmap): Constraint {
        if (predicate.term !is PrlFeature) {
            throw CoCoException("Left-hand side of an enum 'in' predicate must be an enum feature")
        }
        return enumIn(compileEnumFeature(predicate.term, theoryMap), predicate.values)
    }

    private fun compileIntInPredicate(predicate: PrlInIntRangePredicate, theoryMap: Tmap): Constraint {
        return intIn(compileIntTerm(predicate.term, theoryMap), predicate.range)
    }

    internal fun compileIntTerm(term: PrlTerm, theoryMap: Tmap): IntTerm = when (term) {
        is PrlFeature -> compileIntFeature(term, theoryMap)
        is PrlIntValue -> intVal(term.value)
        is PrlIntMulFunction -> compileIntMulFunction(term, theoryMap)
        is PrlIntAddFunction -> compileIntAddFunction(term, theoryMap)
        else -> throw CoCoException("Unknown integer term type: ${term.javaClass.simpleName}")
    }

    private fun compileIntMulFunction(term: PrlIntMulFunction, theoryMap: Tmap): IntMul {
        if (term.left is PrlIntMulFunction || term.left is PrlIntAddFunction ||
            term.right is PrlIntMulFunction || term.right is PrlIntAddFunction ||
            term.left is PrlIntValue && term.right is PrlIntValue
        ) {
            throw CoCoException(
                "Integer multiplication is only allowed between a fixed coefficient and an integer feature"
            )
        }
        val feature = if (term.left is PrlFeature) term.left else term.right as PrlFeature
        val coefficient = if (term.left is PrlIntValue) term.left.value else (term.right as PrlIntValue).value
        return intMul(coefficient, compileIntFeature(feature, theoryMap))
    }

    private fun compileIntAddFunction(term: PrlIntAddFunction, theoryMap: Tmap): IntTerm {
        var coefficient = 0
        val condensed = mutableListOf<PrlTerm>().apply {
            term.operands.forEach { if (it is PrlIntAddFunction) addAll(it.operands) else add(it) }
        }
        val operands = mutableListOf<IntMul>()
        condensed.forEach {
            when (it) {
                is PrlFeature -> operands.add(intMul(compileIntFeature(it, theoryMap)))
                is PrlIntValue -> coefficient += it.value
                is PrlIntMulFunction -> operands.add(compileIntMulFunction(it, theoryMap))
                else -> throw CoCoException("Unknown integer term type ${term.javaClass}")
            }
        }
        return intSum(coefficient, operands)
    }

    private fun compileComparison(
        predicate: PrlComparisonPredicate,
        theoryMap: Tmap,
    ): Constraint =
        when (determineTheory(predicate, theoryMap)) {
            Theory.BOOL -> compileVersionPredicate(predicate, theoryMap)
            Theory.VERSIONED_BOOL -> compileVersionPredicate(predicate, theoryMap)
            Theory.ENUM -> compileEnumComparison(predicate, theoryMap)
            Theory.INT -> {
                intComparison(
                    compileIntTerm(predicate.left, theoryMap),
                    compileIntTerm(predicate.right, theoryMap),
                    predicate.comparison
                )
            }
        }

    private fun compileEnumComparison(
        predicate: PrlComparisonPredicate,
        theoryMap: Tmap
    ): Constraint {
        val value =
            if (predicate.left is PrlEnumValue) {
                predicate.left.value
            } else if (predicate.right is PrlEnumValue) {
                predicate.right.value
            } else {
                null
            }
        val feature =
            if (predicate.left is PrlFeature) {
                predicate.left
            } else if (predicate.right is PrlFeature) {
                predicate.right
            } else {
                null
            }
        if (value == null || feature == null) {
            throw CoCoException("Enum comparison must compare an enum feature with an enum value")
        }
        if (predicate.comparison != ComparisonOperator.EQ && predicate.comparison != ComparisonOperator.NE) {
            throw CoCoException("Only comparisons with = and != are allowed for enums")
        }
        return enumComparison(compileEnumFeature(feature, theoryMap), enumVal(value), predicate.comparison)
    }

    private fun compileVersionPredicate(
        predicate: PrlComparisonPredicate,
        theoryMap: Tmap
    ): Constraint {
        val version =
            if (predicate.left is PrlIntValue) {
                predicate.left.value
            } else if (predicate.right is PrlIntValue) {
                predicate.right.value
            } else {
                null
            }
        val feature =
            if (predicate.left is PrlFeature) {
                predicate.left
            } else if (predicate.right is PrlFeature) {
                predicate.right
            } else {
                null
            }
        if (version == null || feature == null) {
            throw CoCoException("Version predicate must compare a versioned boolean feature with a fixed version")
        }
        if (version <= 0) {
            throw CoCoException("Versions must be > 0")
        }
        val compiledFeature = compileBooleanFeature(feature, theoryMap)
        if (compiledFeature !is VersionedBooleanFeature) {
            throw CoCoException("Unversioned feature in version predicate: " + compiledFeature.featureCode)
        }
        return versionComparison(compiledFeature, predicate.comparison, version)
    }

    private fun determineTheory(
        predicate: PrlComparisonPredicate,
        theoryMap: Tmap
    ): Theory {
        var theory: Theory? = null
        predicate.features().forEach { feature ->
            theoryMap[feature].let { found ->
                when {
                    found == null -> unknownFeature(feature)
                    theory == null -> theory = found
                    theory != found -> throw CoCoException(
                        "Cannot determine theory of predicate, mixed features of theories $theory and $found"
                    )
                }
            }
        }
        return when {
            theory != null -> theory
            isInt(predicate.left) && isInt(predicate.right) -> Theory.INT
            isEnum(predicate.left) && isEnum(predicate.right) -> Theory.ENUM
            else -> throw CoCoException("Cannot determine theory of predicate")
        }
    }

    internal fun unknownFeature(feature: PrlFeature): Nothing =
        throw CoCoException("Unknown feature: '${feature.featureCode}'")

    private fun wrongFeatureTheory(feature: PrlFeature, theory: Theory, expected: Theory): Nothing =
        throw CoCoException("$theory feature '${feature.featureCode}' is used as $expected feature")

    private fun isInt(term: PrlTerm) = term is PrlIntValue || term is PrlIntMulFunction || term is PrlIntAddFunction
    private fun isEnum(term: PrlTerm) = term is PrlEnumValue
}
