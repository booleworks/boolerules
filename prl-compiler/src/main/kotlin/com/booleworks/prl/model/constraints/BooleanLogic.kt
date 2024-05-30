// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.parser.PragmaticRuleLanguage
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_COMMA
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RSQB
import com.booleworks.prl.parser.PragmaticRuleLanguage.bracket
import com.booleworks.prl.parser.PragmaticRuleLanguage.ccString
import com.booleworks.prl.parser.PragmaticRuleLanguage.constantString

val TRUE = Constant(true)
val FALSE = Constant(false)

fun boolFt(featureCode: String) = BooleanFeature(featureCode)

fun constant(value: Boolean) = Constant(value)

fun not(operand: Constraint) = when (operand.type) {
    ConstraintType.FALSE -> TRUE
    ConstraintType.TRUE -> FALSE
    ConstraintType.NOT -> (operand as Not).operand
    else -> Not(operand)
}

fun impl(left: Constraint, right: Constraint) = Implication(left, right)
fun equiv(left: Constraint, right: Constraint) = Equivalence(left, right)

fun amo(vararg features: BooleanFeature) = Amo(features.toSet())
fun amo(features: Collection<BooleanFeature>) = Amo(features.toSet())
fun exo(vararg features: BooleanFeature) = Exo(features.toSet())
fun exo(features: Collection<BooleanFeature>) = Exo(features.toSet())

fun or(vararg operands: Constraint) = or(operands.asList())
fun or(operands: Collection<Constraint>) = condenseOperands(ConstraintType.OR, operands).let {
    when {
        it.isEmpty() -> FALSE
        it.size == 1 -> it.iterator().next()
        else -> Or(it)
    }
}

fun and(vararg operands: Constraint) = and(operands.asList())
fun and(operands: Collection<Constraint>) = condenseOperands(ConstraintType.AND, operands).let {
    when {
        it.isEmpty() -> TRUE
        it.size == 1 -> it.iterator().next()
        else -> And(it)
    }
}

private fun condenseOperands(type: ConstraintType, operands: Collection<Constraint>) =
    LinkedHashSet<Constraint>().apply {
        operands.forEach { if (type === it.type) addAll((it as NaryOperator).operands) else add(it) }
    }

enum class ConstraintType(val symbol: String, val precedence: Byte) {
    EQUIV(PragmaticRuleLanguage.SYMBOL_EQUIV, 0x88.toByte()),
    IMPL(PragmaticRuleLanguage.SYMBOL_IMPL, 0x99.toByte()),
    OR(PragmaticRuleLanguage.SYMBOL_OR, 0xaa.toByte()),
    AND(PragmaticRuleLanguage.SYMBOL_AND, 0xbb.toByte()),
    NOT(PragmaticRuleLanguage.SYMBOL_NOT_MINUS, 0xcc.toByte()),
    AMO(PragmaticRuleLanguage.KEYWORD_AMO, 0xdc.toByte()),
    EXO(PragmaticRuleLanguage.KEYWORD_EXO, 0xdd.toByte()),
    ATOM("", 0xee.toByte()),
    TRUE(PragmaticRuleLanguage.KEYWORD_TRUE, 0xfe.toByte()),
    FALSE(PragmaticRuleLanguage.KEYWORD_FALSE, 0xff.toByte())
}

sealed interface Constraint {
    val type: ConstraintType
    fun features(): Set<Feature>
    fun booleanFeatures(): Set<BooleanFeature>
    fun enumFeatures(): Set<EnumFeature>
    fun enumValues(): Map<EnumFeature, Set<String>>
    fun intFeatures(): Set<IntFeature>
    fun containsBooleanFeatures() = false
    fun containsEnumFeatures() = false
    fun containsIntFeatures() = false
    fun evaluate(assignment: FeatureAssignment): Boolean
    fun restrict(assignment: FeatureAssignment): Constraint
    fun rename(renaming: FeatureRenaming): Constraint
    fun syntacticSimplify(): Constraint
    fun isAtom(): Boolean
}

open class BooleanFeature(override val featureCode: String) : Feature(featureCode), AtomicConstraint {
    override val type = ConstraintType.ATOM
    override fun features() = setOf(this)
    override fun booleanFeatures() = setOf(this)
    override fun enumFeatures() = setOf<EnumFeature>()
    override fun enumValues() = mapOf<EnumFeature, Set<String>>()
    override fun intFeatures() = setOf<IntFeature>()
    override fun containsBooleanFeatures() = true
    override fun evaluate(assignment: FeatureAssignment) = assignment.getBool(this)
    override fun rename(renaming: FeatureRenaming) = renaming.rename(this)
    override fun syntacticSimplify() = this
    override fun restrict(assignment: FeatureAssignment) = when (assignment.getBoolWithoutDefault(this)) {
        null -> this
        true -> TRUE
        else -> FALSE
    }
}

data class Constant internal constructor(val value: Boolean) : Constraint {
    override val type = if (value) ConstraintType.TRUE else ConstraintType.FALSE
    override fun features() = setOf<Feature>()
    override fun booleanFeatures() = setOf<BooleanFeature>()
    override fun enumFeatures() = setOf<EnumFeature>()
    override fun enumValues() = mapOf<EnumFeature, Set<String>>()
    override fun intFeatures() = setOf<IntFeature>()
    override fun evaluate(assignment: FeatureAssignment) = value
    override fun restrict(assignment: FeatureAssignment) = this
    override fun rename(renaming: FeatureRenaming) = this
    override fun syntacticSimplify() = this
    override fun isAtom() = true
    override fun toString() = constantString(type)
}

data class Not internal constructor(val operand: Constraint) : Constraint {
    override val type = ConstraintType.NOT
    override fun features() = operand.features()
    override fun booleanFeatures() = operand.booleanFeatures()
    override fun enumFeatures() = operand.enumFeatures()
    override fun enumValues() = operand.enumValues()
    override fun intFeatures() = operand.intFeatures()
    override fun containsBooleanFeatures() = operand.containsBooleanFeatures()
    override fun containsEnumFeatures() = operand.containsEnumFeatures()
    override fun containsIntFeatures() = operand.containsIntFeatures()
    override fun evaluate(assignment: FeatureAssignment) = !operand.evaluate(assignment)
    override fun restrict(assignment: FeatureAssignment) = not(operand.restrict(assignment))
    override fun rename(renaming: FeatureRenaming) = not(operand.rename(renaming))
    override fun syntacticSimplify() = not(operand.syntacticSimplify())
    override fun isAtom() = false
    override fun toString() = type.symbol +
            if (operand.type.precedence > ConstraintType.NOT.precedence) operand.toString() else
                bracket(operand.toString())
}

sealed class BinaryOperator(open val left: Constraint, open val right: Constraint) : Constraint {
    override fun isAtom() = false
    override fun features() = left.features() + right.features()
    override fun booleanFeatures() = left.booleanFeatures() + right.booleanFeatures()
    override fun enumFeatures() = left.enumFeatures() + right.enumFeatures()
    override fun intFeatures() = left.intFeatures() + right.intFeatures()
    override fun enumValues(): Map<EnumFeature, MutableSet<String>> {
        val result: MutableMap<EnumFeature, MutableSet<String>> = mutableMapOf()
        left.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        right.enumValues().forEach { (v, vs) -> result.computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        return result
    }

    override fun containsBooleanFeatures() = left.containsBooleanFeatures() || right.containsBooleanFeatures()
    override fun containsEnumFeatures() = left.containsEnumFeatures() || right.containsEnumFeatures()
    override fun containsIntFeatures() = left.containsIntFeatures() || right.containsIntFeatures()

    override fun restrict(assignment: FeatureAssignment) =
        simplifiedConstraint(left.restrict(assignment), right.restrict(assignment))

    override fun syntacticSimplify() = simplifiedConstraint(left.syntacticSimplify(), right.syntacticSimplify())
    protected abstract fun simplifiedConstraint(simpLeft: Constraint, simpRight: Constraint): Constraint

    override fun toString(): String {
        val precedence = type.precedence
        val leftString = if (precedence < left.type.precedence) left.toString() else bracket(left.toString())
        val rightString = if (precedence < right.type.precedence) right.toString() else bracket(right.toString())
        return leftString + " " + type.symbol + " " + rightString
    }
}

data class Implication internal constructor(override val left: Constraint, override val right: Constraint) :
    BinaryOperator(left, right) {
    override val type = ConstraintType.IMPL
    override fun evaluate(assignment: FeatureAssignment) = !left.evaluate(assignment) || right.evaluate(assignment)
    override fun rename(renaming: FeatureRenaming) = Implication(left.rename(renaming), right.rename(renaming))
    override fun toString() = super.toString()

    override fun simplifiedConstraint(simpLeft: Constraint, simpRight: Constraint): Constraint {
        return when {
            simpLeft.type === ConstraintType.FALSE || simpRight.type === ConstraintType.TRUE -> TRUE
            simpLeft.type === ConstraintType.TRUE -> simpRight
            simpRight.type === ConstraintType.FALSE -> not(simpLeft)
            simpLeft == simpRight -> TRUE
            else -> Implication(simpLeft, simpRight)
        }
    }
}

data class Equivalence internal constructor(override val left: Constraint, override val right: Constraint) :
    BinaryOperator(left, right) {
    override val type = ConstraintType.EQUIV
    override fun evaluate(assignment: FeatureAssignment) = left.evaluate(assignment) == right.evaluate(assignment)
    override fun rename(renaming: FeatureRenaming) = Equivalence(left.rename(renaming), right.rename(renaming))

    override fun simplifiedConstraint(simpLeft: Constraint, simpRight: Constraint) = when {
        simpLeft.type === ConstraintType.TRUE -> simpRight
        simpRight.type === ConstraintType.TRUE -> simpLeft
        simpLeft.type === ConstraintType.FALSE -> not(simpRight)
        simpRight.type === ConstraintType.FALSE -> not(simpLeft)
        simpLeft == simpRight -> TRUE
        else -> Equivalence(simpLeft, simpRight)
    }

    override fun toString() = super.toString()
    override fun hashCode() = left.hashCode() + right.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Equivalence
        return left == other.left && right == other.right || left == other.right && right == other.left
    }
}

sealed class CardinalityConstraint(open val features: Set<BooleanFeature>) : Constraint {
    override fun features() = features.toSet()
    override fun booleanFeatures() = features.toSet()
    override fun enumFeatures() = setOf<EnumFeature>()
    override fun enumValues() = mapOf<EnumFeature, Set<String>>()
    override fun intFeatures() = setOf<IntFeature>()
    override fun containsBooleanFeatures() = features.isNotEmpty()
    override fun isAtom() = false

    protected fun count(assignment: FeatureAssignment): Int {
        var count = 0
        for (feature in features) {
            if (feature.evaluate(assignment)) count++
            if (count > 1) return -1
        }
        return count
    }

    protected fun restrictedFeatures(assignment: FeatureAssignment): FeatureAssignmentCC {
        val featureAssignment = FeatureAssignmentCC()
        for (feature in features) {
            feature.restrict(assignment).let {
                when (it.type) {
                    ConstraintType.TRUE -> featureAssignment.numTrueAssignedFeatures++
                    ConstraintType.FALSE -> {}
                    else -> featureAssignment.unassignedFeatures.add(it as BooleanFeature)
                }
            }
        }
        return featureAssignment
    }

    override fun toString() = ccString(type) + SYMBOL_LSQB +
            features.joinToString("$SYMBOL_COMMA ") { it.toString() } + SYMBOL_RSQB

    protected class FeatureAssignmentCC {
        var numTrueAssignedFeatures = 0
        val unassignedFeatures = ArrayList<BooleanFeature>()
    }
}

data class Amo internal constructor(override val features: Set<BooleanFeature>) : CardinalityConstraint(features) {
    override val type = ConstraintType.AMO
    override fun evaluate(assignment: FeatureAssignment) = count(assignment) != -1
    override fun rename(renaming: FeatureRenaming) = amo(features.map { renaming.rename(it) })
    override fun syntacticSimplify() = if (features.isEmpty() || features.size == 1) TRUE else this
    override fun toString() = super.toString()

    override fun restrict(assignment: FeatureAssignment): Constraint {
        val restrictedFeatures = restrictedFeatures(assignment)
        val numTrueAssignedFeatures = restrictedFeatures.numTrueAssignedFeatures
        val unassignedFeatures = restrictedFeatures.unassignedFeatures
        val tautology = unassignedFeatures.isEmpty() && numTrueAssignedFeatures <= 1 ||
                numTrueAssignedFeatures == 0 && unassignedFeatures.size == 1
        return when {
            tautology -> TRUE
            numTrueAssignedFeatures > 1 -> FALSE
            numTrueAssignedFeatures == 1 -> and(unassignedFeatures.map { not(it) })
            else -> amo(unassignedFeatures)
        }
    }
}

data class Exo internal constructor(override val features: Set<BooleanFeature>) : CardinalityConstraint(features) {
    override val type = ConstraintType.EXO
    override fun evaluate(assignment: FeatureAssignment) = count(assignment) == 1
    override fun rename(renaming: FeatureRenaming) = exo(features.map(renaming::rename))
    override fun toString() = super.toString()

    override fun syntacticSimplify() = when {
        features.isEmpty() -> FALSE
        features.size == 1 -> features.iterator().next()
        else -> this
    }

    override fun restrict(assignment: FeatureAssignment): Constraint {
        val restrictedFeatures = restrictedFeatures(assignment)
        val numTrueAssignedFeatures = restrictedFeatures.numTrueAssignedFeatures
        val unassignedFeatures: List<BooleanFeature> = restrictedFeatures.unassignedFeatures
        val tautology = numTrueAssignedFeatures == 1 && unassignedFeatures.isEmpty()
        val contradiction = numTrueAssignedFeatures == 0 && unassignedFeatures.isEmpty() || numTrueAssignedFeatures > 1
        return when {
            tautology -> TRUE
            contradiction -> FALSE
            numTrueAssignedFeatures == 1 -> and(unassignedFeatures.map { not(it) })
            unassignedFeatures.size == 1 -> unassignedFeatures[0]
            else -> exo(unassignedFeatures)
        }
    }
}

sealed class NaryOperator(open val operands: Set<Constraint>) : Constraint {
    override fun isAtom() = false
    override fun features() = operands.flatMap { it.features() }.toSet()
    override fun booleanFeatures() = operands.flatMap { it.booleanFeatures() }.toSet()
    override fun enumFeatures() = operands.flatMap { it.enumFeatures() }.toSet()
    override fun intFeatures() = operands.flatMap { it.intFeatures() }.toSet()

    override fun enumValues() = mutableMapOf<EnumFeature, MutableSet<String>>().apply {
        operands.forEach {
            it.enumValues().forEach { (v, vs) -> computeIfAbsent(v) { mutableSetOf() }.addAll(vs) }
        }
    }

    override fun containsBooleanFeatures() = operands.any { obj: Constraint -> obj.containsBooleanFeatures() }
    override fun containsEnumFeatures() = operands.any { obj: Constraint -> obj.containsEnumFeatures() }
    override fun containsIntFeatures() = operands.any { obj: Constraint -> obj.containsIntFeatures() }

    override fun toString(): String {
        val sb = StringBuilder()
        val size = operands.size
        var last: Constraint? = null
        for ((count, op) in operands.withIndex()) {
            if (count + 1 == size) {
                last = op
            } else {
                sb.append(if (type.precedence < op.type.precedence) op.toString() else bracket(op.toString()))
                sb.append(" ").append(type.symbol).append(" ")
            }
        }
        if (last != null) {
            sb.append(if (type.precedence < last.type.precedence) last.toString() else bracket(last.toString()))
        }
        return sb.toString()
    }
}

data class And internal constructor(override val operands: Set<Constraint>) : NaryOperator(operands) {
    override val type = ConstraintType.AND
    override fun evaluate(assignment: FeatureAssignment) = operands.all { op: Constraint -> op.evaluate(assignment) }
    override fun restrict(assignment: FeatureAssignment) = simplifiedConstraint(assignment)
    override fun rename(renaming: FeatureRenaming) = and(operands.map { o: Constraint -> o.rename(renaming) }.toList())
    override fun syntacticSimplify() = simplifiedConstraint(null)
    override fun toString() = super.toString()

    private fun simplifiedConstraint(assignment: FeatureAssignment?): Constraint {
        val newOperands = LinkedHashSet<Constraint>()
        for (operand in operands) {
            val simplOp = (if (assignment == null) operand else operand.restrict(assignment)).syntacticSimplify()
            if (simplOp.type === ConstraintType.FALSE) {
                return FALSE
            } else if (simplOp.type !== ConstraintType.TRUE) {
                newOperands.add(simplOp)
            }
        }
        return and(newOperands)
    }
}

data class Or internal constructor(override val operands: Set<Constraint>) : NaryOperator(operands) {
    override val type = ConstraintType.OR
    override fun evaluate(assignment: FeatureAssignment) = operands.any { op: Constraint -> op.evaluate(assignment) }
    override fun rename(renaming: FeatureRenaming) = or(operands.map { o: Constraint -> o.rename(renaming) }.toList())
    override fun restrict(assignment: FeatureAssignment) = simplifiedConstraint(assignment)
    override fun syntacticSimplify() = simplifiedConstraint(null)
    override fun toString() = super.toString()

    private fun simplifiedConstraint(assignment: FeatureAssignment?): Constraint {
        val newOperands = LinkedHashSet<Constraint>()
        for (operand in operands) {
            val simplOp = (if (assignment == null) operand else operand.restrict(assignment)).syntacticSimplify()
            if (simplOp.type === ConstraintType.TRUE) {
                return TRUE
            } else if (simplOp.type !== ConstraintType.FALSE) {
                newOperands.add(simplOp)
            }
        }
        return or(newOperands)
    }
}
