// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.IntegerDomain
import com.booleworks.logicng.csp.IntegerRangeDomain
import com.booleworks.logicng.csp.IntegerSetDomain
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.csp.predicates.ComparisonPredicate
import com.booleworks.logicng.csp.terms.IntegerVariable
import com.booleworks.logicng.csp.terms.Term
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.compiler.FeatureStore
import com.booleworks.prl.model.EmptyIntRange
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.IntInterval
import com.booleworks.prl.model.IntList
import com.booleworks.prl.model.PropertyRange
import com.booleworks.prl.model.constraints.Amo
import com.booleworks.prl.model.constraints.And
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.constraints.Constant
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.EnumComparisonPredicate
import com.booleworks.prl.model.constraints.EnumInPredicate
import com.booleworks.prl.model.constraints.Equivalence
import com.booleworks.prl.model.constraints.Exo
import com.booleworks.prl.model.constraints.Implication
import com.booleworks.prl.model.constraints.IntComparisonPredicate
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.IntInPredicate
import com.booleworks.prl.model.constraints.IntMul
import com.booleworks.prl.model.constraints.IntPredicate
import com.booleworks.prl.model.constraints.IntSum
import com.booleworks.prl.model.constraints.IntTerm
import com.booleworks.prl.model.constraints.IntValue
import com.booleworks.prl.model.constraints.Not
import com.booleworks.prl.model.constraints.Or
import com.booleworks.prl.model.constraints.VersionPredicate
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.model.rules.DefinitionRule
import com.booleworks.prl.model.rules.ExclusionRule
import com.booleworks.prl.model.rules.ForbiddenFeatureRule
import com.booleworks.prl.model.rules.GroupRule
import com.booleworks.prl.model.rules.IfThenElseRule
import com.booleworks.prl.model.rules.InclusionRule
import com.booleworks.prl.model.rules.MandatoryFeatureRule
import com.booleworks.prl.model.slices.SliceSet
import com.booleworks.prl.transpiler.RuleType.FEATURE_EQUIVALENCE_OVER_SLICES

const val PREDICATE_PREFIX = "@PREDICATE"
const val FEATURE_DEF_PREFIX = "@DEF"

fun encodeIntFeatures(
    context: CspEncodingContext,
    cf: CspFactory,
    store: FeatureStore
): IntegerEncodingStore = IntegerEncodingStore(store.intFeatures.values.associate { defs ->
    val reference = defs.first().code
    val map1 = defs.mapIndexed { index, d ->
        val def = (d as IntFeatureDefinition)
        Pair(
            def,
            LngIntVariable(
                def.feature.featureCode,
                cf.variable(
                    "$FEATURE_DEF_PREFIX$index$S${def.feature.featureCode}",
                    transpileIntDomain(def.domain)
                )
            )
        )
    }.toMap().toMutableMap()
    val map2 = map1.values.associateBy(LngIntVariable::variable) { v ->
        val clauses = cf.encodeVariable(v.variable, context)
        cf.formulaFactory().and(clauses)
    }.toMutableMap()
    Pair(reference, IntFeatureEncodingInfo(map1, map2))
})

fun createIntVariableEquivalence(
    sliceVariable: IntegerVariable,
    mergedVar: IntegerVariable,
    encodingContext: CspEncodingContext,
    f: FormulaFactory
): PrlProposition {
    val constraints = mutableListOf<Formula>();
    val mergedDomain = mergedVar.domain;
    val sliceDomain = sliceVariable.domain;
    var mergedCounter = 0
    var sliceCounter = 0
    var c = sliceDomain.lb();
    var lastGeneralVar: Variable? = null;
    while (c < sliceDomain.ub()) {
        if (sliceDomain.contains(c)) {
            val vEncodedVar = encodingContext.variableMap[sliceVariable]!![sliceCounter]!!
            if (c < mergedDomain.lb()) {
                constraints.add(vEncodedVar.negate(f))
            } else if (c >= mergedDomain.ub()) {
                constraints.add(vEncodedVar)
            } else if (mergedDomain.contains(c)) {
                val generalEncodedVar = encodingContext.variableMap[mergedVar]!![mergedCounter]!!
                constraints.add(f.equivalence(generalEncodedVar, vEncodedVar))
                lastGeneralVar = generalEncodedVar
                ++mergedCounter
            } else {
                if (lastGeneralVar == null) {
                    constraints.add(vEncodedVar.negate(f))
                } else {
                    constraints.add(f.implication(vEncodedVar, lastGeneralVar))
                }
            }
            ++sliceCounter
        }
        ++c
    }
    return PrlProposition(RuleInformation(FEATURE_EQUIVALENCE_OVER_SLICES), f.and(constraints))
}

fun getAllIntPredicates(f: FormulaFactory, sliceSet: SliceSet): MutableMap<IntPredicate, Variable> {
    val map = LinkedHashMap<IntPredicate, Variable>()
    sliceSet.rules.forEach { rule ->
        when (rule) {
            is ConstraintRule -> getAllIntPredicates(f, map, rule.constraint)
            is DefinitionRule -> {
                getAllIntPredicates(f, map, rule.feature)
                getAllIntPredicates(f, map, rule.definition)
            }
            is ExclusionRule -> {
                getAllIntPredicates(f, map, rule.ifConstraint)
                getAllIntPredicates(f, map, rule.thenNotConstraint)
            }
            is ForbiddenFeatureRule -> getAllIntPredicates(f, map, rule.constraint)
            is MandatoryFeatureRule -> getAllIntPredicates(f, map, rule.constraint)
            is GroupRule -> {}
            is InclusionRule -> {
                getAllIntPredicates(f, map, rule.ifConstraint)
                getAllIntPredicates(f, map, rule.thenConstraint)
            }
            is IfThenElseRule -> {
                getAllIntPredicates(f, map, rule.ifConstraint)
                getAllIntPredicates(f, map, rule.thenConstraint)
                getAllIntPredicates(f, map, rule.elseConstraint)
            }
        }
    }
    return map
}

fun getAllIntPredicates(f: FormulaFactory, map: MutableMap<IntPredicate, Variable>, constraint: Constraint) {
    when (constraint) {
        is Constant -> {}
        is BooleanFeature -> {}
        is VersionedBooleanFeature -> {}
        is Not -> getAllIntPredicates(f, map, constraint.operand)
        is Implication -> {
            getAllIntPredicates(f, map, constraint.left)
            getAllIntPredicates(f, map, constraint.right)
        }
        is Equivalence -> {
            getAllIntPredicates(f, map, constraint.left)
            getAllIntPredicates(f, map, constraint.right)
        }
        is And -> constraint.operands.forEach { getAllIntPredicates(f, map, it) }
        is Or -> constraint.operands.forEach { getAllIntPredicates(f, map, it) }
        is IntComparisonPredicate, is IntInPredicate -> map.computeIfAbsent(constraint) { _ ->
            f.variable("${PREDICATE_PREFIX}_${map.size}")
        }
        is Amo, is Exo, is EnumComparisonPredicate, is EnumInPredicate -> {}
        is VersionPredicate -> {}
    }
}

fun transpileIntPredicate(
    cf: CspFactory,
    integerEncodings: IntegerEncodingStore,
    instantiation: FeatureInstantiation,
    predicate: IntPredicate
): ComparisonPredicate =
    when (predicate) {
        is IntComparisonPredicate -> transpileIntComparisonPredicate(cf, integerEncodings, instantiation, predicate)
        is IntInPredicate -> transpileIntInPredicate(cf, integerEncodings, instantiation, predicate)
    }

fun transpileIntComparisonPredicate(
    cf: CspFactory,
    integerEncodings: IntegerEncodingStore,
    instantiation: FeatureInstantiation,
    predicate: IntComparisonPredicate
): ComparisonPredicate {
    val left = transpileIntTerm(cf, integerEncodings, instantiation, predicate.left)
    val right = transpileIntTerm(cf, integerEncodings, instantiation, predicate.right)
    return when (predicate.comparison) {
        ComparisonOperator.EQ -> cf.eq(left, right)
        ComparisonOperator.GE -> cf.ge(left, right)
        ComparisonOperator.NE -> cf.ne(left, right)
        ComparisonOperator.LT -> cf.lt(left, right)
        ComparisonOperator.LE -> cf.le(left, right)
        ComparisonOperator.GT -> cf.gt(left, right)
    }
}

fun transpileIntInPredicate(
    cf: CspFactory,
    integerEncodings: IntegerEncodingStore,
    instantiation: FeatureInstantiation,
    predicate: IntInPredicate
): ComparisonPredicate {
    val term = transpileIntTerm(cf, integerEncodings, instantiation, predicate.term)
    val v = cf.auxVariable(transpileIntDomain(predicate.range))
    return cf.eq(term, v)
}

fun transpileIntTerm(
    cf: CspFactory,
    integerEncodings: IntegerEncodingStore,
    instantiation: FeatureInstantiation,
    term: IntTerm
): Term = when (term) {
    is IntValue -> cf.constant(term.value)
    is IntFeature -> transpileIntFeature(integerEncodings, instantiation, term)
    is IntMul -> cf.mul(term.coefficient, transpileIntFeature(integerEncodings, instantiation, term.feature))
    is IntSum -> cf.add(
        cf.add(term.operands.map { transpileIntMul(cf, integerEncodings, instantiation, it) }),
        cf.constant(term.offset)
    )
}

fun transpileIntMul(
    cf: CspFactory,
    integerEncodings: IntegerEncodingStore,
    instantiation: FeatureInstantiation,
    feature: IntMul
): Term = cf.mul(feature.coefficient, transpileIntFeature(integerEncodings, instantiation, feature.feature))

fun transpileIntFeature(
    integerEncodings: IntegerEncodingStore,
    instantiation: FeatureInstantiation,
    feature: IntFeature
) = integerEncodings.getVariable(instantiation[feature]!!)!!.variable

fun transpileIntDomain(domain: PropertyRange<Int>): IntegerDomain = when (domain) {
    is IntList, EmptyIntRange -> IntegerSetDomain(domain.allValues())
    is IntInterval -> IntegerRangeDomain(domain.first(), domain.last())
    else -> throw IllegalArgumentException("Invalid integer domain ${domain.javaClass}")
}

data class IntegerEncodingStore(val store: Map<String, IntFeatureEncodingInfo>) : Cloneable {

    fun getEncoding(variable: LngIntVariable) =
        getInfo(variable.feature)?.getEncoding(variable.variable)

    fun getVariable(feature: IntFeatureDefinition) =
        getInfo(feature.feature.featureCode)?.getVariable(feature)

    fun getInfo(feature: String) = store[feature]

    public override fun clone() = IntegerEncodingStore(store.mapValues { (_, info) -> info.clone() })

    companion object {
        fun empty() = IntegerEncodingStore(mutableMapOf())
    }
}

data class IntFeatureEncodingInfo(
    private val featureToVar: MutableMap<IntFeatureDefinition, LngIntVariable>,
    private val encodedVars: MutableMap<IntegerVariable, Formula>,
) : Cloneable {
    fun contains(variable: IntegerVariable) = encodedVars.containsKey(variable)

    fun getEncoding(variable: IntegerVariable) = encodedVars[variable]

    fun getVariable(definition: IntFeatureDefinition) = featureToVar[definition]

    fun addDefinition(definition: IntFeatureDefinition, encodingContext: CspEncodingContext, cf: CspFactory) {
        if (!featureToVar.containsKey(definition)) {
            val varName = "$FEATURE_DEF_PREFIX${featureToVar.size}$S${definition.code}"
            val domain = transpileIntDomain(definition.domain)
            val variable = LngIntVariable(definition.code, cf.variable(varName, domain))
            val clauses = cf.encodeVariable(variable.variable, encodingContext)
            val encoded = cf.formulaFactory().and(clauses)
            featureToVar[definition] = variable
            encodedVars[variable.variable] = encoded
        }
    }

    public override fun clone() = IntFeatureEncodingInfo(HashMap(featureToVar), HashMap(encodedVars))

    companion object {
        fun empty() = IntFeatureEncodingInfo(mutableMapOf(), mutableMapOf())
    }
}

data class LngIntVariable(
    val feature: String,
    val variable: IntegerVariable,
)
