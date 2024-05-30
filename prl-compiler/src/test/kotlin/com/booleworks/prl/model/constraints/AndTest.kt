package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AndTest {
    private var f1 = boolFt("f1")
    private var f2 = boolFt("f2")
    private var f3 = boolFt("f3")
    private var a = boolFt("a")
    private var b = boolFt("b")
    private var c = boolFt("c")
    private var d = boolFt("d")
    private var e = boolFt("e")
    private var implication = impl(f2, f3)

    @Test
    fun testCreationCollection() {
        val and = and(listOf(f1, f2, f3, implication)) as And
        assertThat(and.type).isEqualTo(ConstraintType.AND)
        assertThat(and.isAtom()).isFalse
        assertThat(and.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testCreationVarargs() {
        val and = and(f1, f2, f3, implication) as And
        assertThat(and.type).isEqualTo(ConstraintType.AND)
        assertThat(and.isAtom()).isFalse
        assertThat(and.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testDoubleOperands() {
        val and1 = and(f1, f2, f1, f3, implication, implication) as And
        val and2 = and(listOf(f1, f2, f1, f3, implication, implication)) as And
        assertThat(and1.operands).containsExactly(f1, f2, f3, implication)
        assertThat(and2.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testNestedOperators() {
        val and1 = and(f1, f2, and(f3, implication), f2, implication) as And
        val and2 = and(listOf(f1, f2, and(f3, implication), f2, implication)) as And
        assertThat(and1.operands).containsExactly(f1, f2, f3, implication)
        assertThat(and2.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testEmpty() {
        val and1: Constraint = and()
        val and2: Constraint = and(listOf())
        assertThat(and1).isEqualTo(TRUE)
        assertThat(and2).isEqualTo(TRUE)
    }

    @Test
    fun testOneOperand() {
        val and1: Constraint = and(f1)
        val and2: Constraint = and(listOf(f1))
        assertThat(and1).isEqualTo(f1)
        assertThat(and2).isEqualTo(f1)
    }

    @Test
    fun testConstants() {
        val and1 = and(f1, TRUE) as And
        val and2 = and(f1, FALSE) as And
        assertThat(and1.operands).containsExactly(f1, TRUE)
        assertThat(and2.operands).containsExactly(f1, FALSE)
    }

    @Test
    fun testToString() {
        val and1 = and(a, and(b, not(c), d))
        val and2 = and(a, impl(d, e), equiv(b, enumEq(enumFt("name"), "a")))
        val and3 = and(a, or(b, c), or(d, e))
        val and4 = or(and(a, b), and(c, d), e)
        assertThat(and1.toString()).isEqualTo("a & b & -c & d")
        assertThat(and2.toString()).isEqualTo("a & (d => e) & (b <=> [name = \"a\"])")
        assertThat(and3.toString()).isEqualTo("a & (b / c) & (d / e)")
        assertThat(and4.toString()).isEqualTo("a & b / c & d / e")
    }

    @Test
    fun testEquals() {
        val and1 = and(f1, f2) as And
        val and2 = and(f1, f2, implication) as And
        assertThat(and1 == and(f1, f2)).isTrue
        assertThat(and2 == and(f1, f2, implication)).isTrue
        assertThat(and1 == and(f2, f1)).isTrue
        assertThat(and2 == and(f1, implication, f2)).isTrue
        assertThat(and2 == and1).isFalse
    }

    @Test
    fun testFeatures() {
        val and1: Constraint = and(f1, implication)
        val and2 = and(
            a,
            enumEq(enumFt("name"), "desc"),
            or(b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
            intGt(intFt("x"), 3),
            or(b, enumEq(enumFt("desc"), "a"))
        )
        val and3 = and(
            enumIn(enumFt("f1"), "a", "b"),
            enumIn(enumFt("f1"), "c"),
            enumEq(enumFt("f1"), "a"),
            enumEq(enumFt("f2"), "c")
        )

        assertThat(and1.features()).containsExactly(f1, f2, f3)
        assertThat(and1.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(and1.intFeatures()).isEmpty()
        assertThat(and1.enumFeatures()).isEmpty()
        assertThat(and2.features().map { it.featureCode }).containsExactlyInAnyOrder(
            "a",
            "name",
            "desc",
            "b",
            "i",
            "v",
            "x"
        )
        assertThat(and2.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(and2.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(and2.enumFeatures().map { it.featureCode }).containsExactly("name", "desc")
        assertThat(and1.containsBooleanFeatures()).isTrue
        assertThat(and1.containsEnumFeatures()).isFalse
        assertThat(and1.containsIntFeatures()).isFalse
        assertThat(and2.containsBooleanFeatures()).isTrue
        assertThat(and2.containsEnumFeatures()).isTrue
        assertThat(and2.containsIntFeatures()).isTrue
        assertThat(and2.enumValues()).hasSize(2)
        assertThat(and2.enumValues()[enumFt("name")]).containsExactly("desc")
        assertThat(and2.enumValues()[enumFt("desc")]).containsExactly("a")
        assertThat(and3.enumValues()).hasSize(2)
        assertThat(and3.enumValues()[enumFt("f1")]).containsExactly("a", "b", "c")
        assertThat(and3.enumValues()[enumFt("f2")]).containsExactly("c")
    }

    @Test
    fun testEvaluate() {
        val i1 = intFt("i1")
        val i2 = intFt("i2")
        val assEmpty = FeatureAssignment()
        val ass = FeatureAssignment()
        ass.assign(a, true)
        ass.assign(b, false)
        ass.assign(i1, 2)
        ass.assign(i2, 7)
        assertThat(and(a, b).evaluate(assEmpty)).isFalse
        assertThat(and(a, b).evaluate(ass)).isFalse
        assertThat(and(a, intEq(i1, 2), intLt(i2, 12)).evaluate(ass)).isTrue
        assertThat(and(a, not(b)).evaluate(ass)).isTrue
        assertThat(and(a, intEq(i1, 2), intGt(i2, 12)).evaluate(ass)).isFalse
    }

    @Test
    fun testRestrict() {
        val b1 = boolFt("b1")
        val b2 = boolFt("b2")
        val b3 = boolFt("b3")
        val s1 = enumFt("s1")
        val s3 = enumFt("s3")
        val bx = boolFt("bx")
        val assEmpty = FeatureAssignment()
        val ass1 = FeatureAssignment()
        val bv1 = versionFt("bv1")
        ass1.assign(b1, true)
        ass1.assign(b2, false)
        ass1.assign(b3, true)
        ass1.assign(s1, "text")
        ass1.assign(bv1, 2)
        assertThat(and(b1, b2).restrict(assEmpty)).isEqualTo(and(b1, b2))
        assertThat(and(b1, b2).restrict(ass1)).isEqualTo(FALSE)
        assertThat(and(b1, b3).restrict(ass1)).isEqualTo(TRUE)
        assertThat(and(b1, not(b2)).restrict(ass1)).isEqualTo(TRUE)
        assertThat(and(b1, not(b3)).restrict(ass1)).isEqualTo(FALSE)
        assertThat(and(b1, enumEq(s1, "text")).restrict(ass1)).isEqualTo(TRUE)
        assertThat(and(b1, versionEq(bv1, 2)).restrict(ass1)).isEqualTo(TRUE)
        assertThat(and(b1).restrict(ass1)).isEqualTo(TRUE)
        assertThat(and(b1, enumEq(s3, "text")).restrict(ass1)).isEqualTo(enumEq(s3, enumVal("text")))
        assertThat(and(b1, bx).restrict(ass1)).isEqualTo(bx)
        assertThat(and(bx).restrict(ass1)).isEqualTo(bx)
        assertThat(and().restrict(ass1)).isEqualTo(TRUE)
    }

    @Test
    fun testSyntacticSimplify() {
        val eq = intEq(intFt("j"), 3)
        assertThat(and(a, b, eq).syntacticSimplify()).isEqualTo(and(a, b, eq))
        assertThat(and(a, b, or(TRUE, c)).syntacticSimplify()).isEqualTo(and(a, b))
        assertThat(
            and(a, b, or(FALSE, c)).syntacticSimplify()
        ).isEqualTo(and(a, b, c))
        assertThat(and(a, b, FALSE).syntacticSimplify()).isEqualTo(FALSE)
        assertThat(and(TRUE, impl(a, a), not(FALSE)).syntacticSimplify()).isEqualTo(TRUE)
    }

    @Test
    fun testRenaming() {
        val constraint = and(f1, f2, or(a, f3))
        val r: FeatureRenaming = FeatureRenaming().add(f2, "b").add(f3, "c")
        assertThat(constraint.rename(r)).isEqualTo(and(f1, b, or(a, c)))
    }

    @Test
    fun testSerialization() {
        val and1: Constraint = and(f1, implication)
        val and2 = and(
            a,
            enumEq(enumFt("name"), "desc"),
            or(b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
            intGt(intFt("x"), 3),
            or(b, enumEq(enumFt("desc"), "a"))
        )
        val and3 = and(
            enumIn(enumFt("f1"), "a", "b"),
            enumIn(enumFt("f1"), "c"),
            enumEq(enumFt("f1"), "a"),
            enumEq(enumFt("f2"), "c")
        )
        val maps1 = ftMap(and1)
        val maps2 = ftMap(and2)
        val maps3 = ftMap(and3)
        assertThat(deserialize(serialize(and1, maps1.first), maps1.second)).isEqualTo(and1)
        assertThat(deserialize(serialize(and2, maps2.first), maps2.second)).isEqualTo(and2)
        assertThat(deserialize(serialize(and3, maps3.first), maps3.second)).isEqualTo(and3)
    }
}
