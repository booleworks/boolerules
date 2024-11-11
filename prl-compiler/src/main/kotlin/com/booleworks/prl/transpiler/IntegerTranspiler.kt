// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.datastructures.domains.IntegerDomain
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.csp.encodings.OrderEncodingContext
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

const val PREDICATE_PREFIX = "PREDICATE"
const val FEATURE_DEF_PREFIX = "DEF"
const val INT_IN_PREDICATE_PREFIX = "IIP"

fun initIntegerStore(
    context: OrderEncodingContext,
    cf: CspFactory,
    store: FeatureStore
): IntegerStore = IntegerStore(store.intFeatures.values.associate { defs ->
    val feature = defs.first().code
    val map1 = defs.mapIndexed { index, d ->
        val def = d as IntFeatureDefinition
        Pair(
            def,
            LngIntVariable(
                def.feature.featureCode,
                cf.auxVariable(
                    FEATURE_DEF_PREFIX,
                    def.feature.featureCode,
                    transpileIntDomain(def.domain)
                )
            )
        )
    }.toMap().toMutableMap()
    val map2 = map1.values.associateBy(LngIntVariable::variable) { v ->
        val clauses = cf.encodeVariable(v.variable, context)
        cf.formulaFactory.and(clauses)
    }.toMutableMap()
    Pair(feature, IntFeatureEncodingInfo(map1, map2))
})

fun createIntVariableEquivalence(
    sliceVariable: IntegerVariable,
    mergedVar: IntegerVariable,
    encodingContext: OrderEncodingContext,
    f: FormulaFactory
): PrlProposition {
    val constraints = mutableListOf<Formula>()
    val mergedDomain = mergedVar.domain
    val sliceDomain = sliceVariable.domain
    var mergedCounter = 0
    var sliceCounter = 0
    var c = sliceDomain.lb()
    var lastGeneralVar: Variable? = null
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
    sliceSet.allRules.forEach { rule ->
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
        is VersionedBooleanFeature -> {}
        is BooleanFeature -> {}
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
        is IntPredicate -> map.computeIfAbsent(constraint) { f.variable("${PREDICATE_PREFIX}_${map.size}") }
        is Amo, is Exo, is EnumComparisonPredicate, is EnumInPredicate -> {}
        is VersionPredicate -> {}
    }
}

fun transpileIntPredicate(
    cf: CspFactory,
    integerEncodings: IntegerStore,
    instantiation: FeatureInstantiation,
    predicate: IntPredicate
): ComparisonPredicate? =
    when (predicate) {
        is IntComparisonPredicate -> transpileIntComparisonPredicate(cf, integerEncodings, instantiation, predicate)
        is IntInPredicate -> transpileIntInPredicate(cf, integerEncodings, instantiation, predicate)
    }

fun transpileIntComparisonPredicate(
    cf: CspFactory,
    integerEncodings: IntegerStore,
    instantiation: FeatureInstantiation,
    predicate: IntComparisonPredicate
): ComparisonPredicate? {
    val left = transpileIntTerm(cf, integerEncodings, instantiation, predicate.left) ?: return null
    val right = transpileIntTerm(cf, integerEncodings, instantiation, predicate.right) ?: return null
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
    integerEncodings: IntegerStore,
    instantiation: FeatureInstantiation,
    predicate: IntInPredicate
): ComparisonPredicate? {
    val term = transpileIntTerm(cf, integerEncodings, instantiation, predicate.term) ?: return null
    val v = cf.auxVariable(INT_IN_PREDICATE_PREFIX, transpileIntDomain(predicate.range))
    return cf.eq(term, v)
}

fun transpileIntTerm(
    cf: CspFactory,
    integerEncodings: IntegerStore,
    instantiation: FeatureInstantiation,
    term: IntTerm
): Term? = when (term) {
    is IntValue -> cf.constant(term.value)
    is IntFeature -> transpileIntFeature(integerEncodings, instantiation, term)
    is IntMul -> transpileIntFeature(integerEncodings, instantiation, term.feature)?.let {
        cf.mul(
            term.coefficient,
            it
        )
    }

    is IntSum -> {
        val ops = term.operands.mapNotNull { transpileIntMul(cf, integerEncodings, instantiation, it) }
        if (ops.size == term.operands.size) {
            cf.add(cf.add(ops), cf.constant(term.offset))
        } else {
            null
        }
    }
}

fun transpileIntMul(
    cf: CspFactory,
    integerEncodings: IntegerStore,
    instantiation: FeatureInstantiation,
    feature: IntMul
): Term? =
    transpileIntFeature(integerEncodings, instantiation, feature.feature)?.let { cf.mul(feature.coefficient, it) }

fun transpileIntFeature(
    integerEncodings: IntegerStore,
    instantiation: FeatureInstantiation,
    feature: IntFeature
) = instantiation[feature]?.let { integerEncodings.getVariable(it)!!.variable }

fun transpileIntDomain(domain: PropertyRange<Int>): IntegerDomain = when (domain) {
    is IntList, EmptyIntRange -> IntegerDomain.of(domain.allValues())
    is IntInterval -> IntegerDomain.of(domain.first(), domain.last())
    else -> throw IllegalArgumentException("Invalid integer domain ${domain.javaClass}")
}

data class IntegerStore(val store: Map<String, IntFeatureEncodingInfo>) : Cloneable {

    fun getEncoding(variable: LngIntVariable) =
        getInfo(variable.feature)?.getEncoding(variable.variable)

    fun getVariable(feature: IntFeatureDefinition) =
        getInfo(feature.feature.featureCode)?.getVariable(feature)

    fun getInfo(feature: String) = store[feature]

    public override fun clone() = IntegerStore(store.mapValues { (_, info) -> info.clone() })

    companion object {
        fun empty() = IntegerStore(mutableMapOf())
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
            val domain = transpileIntDomain(definition.domain)
            val variable = LngIntVariable(definition.code, cf.auxVariable(FEATURE_DEF_PREFIX, definition.code, domain))
            val clauses = cf.encodeVariable(variable.variable, encodingContext)
            val encoded = cf.formulaFactory.and(clauses)
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
