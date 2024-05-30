package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OrTest {
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
        val or = or(listOf(f1, f2, f3, implication)) as Or
        assertThat(or.type).isEqualTo(ConstraintType.OR)
        assertThat(or.isAtom()).isFalse
        assertThat(or.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testCreationVarargs() {
        val or = or(f1, f2, f3, implication) as Or
        assertThat(or.type).isEqualTo(ConstraintType.OR)
        assertThat(or.isAtom()).isFalse
        assertThat(or.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testDoubleOperands() {
        val or1 = or(f1, f2, f1, f3, implication, implication) as Or
        val or2 = or(listOf(f1, f2, f1, f3, implication, implication)) as Or
        assertThat(or1.operands).containsExactly(f1, f2, f3, implication)
        assertThat(or2.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testNestedOperators() {
        val or1 = or(f1, f2, or(f3, implication), f2, implication) as Or
        val or2 = or(listOf(f1, f2, or(f3, implication), f2, implication)) as Or
        assertThat(or1.operands).containsExactly(f1, f2, f3, implication)
        assertThat(or2.operands).containsExactly(f1, f2, f3, implication)
    }

    @Test
    fun testEmpty() {
        val or1: Constraint = or()
        val or2: Constraint = or(listOf())
        assertThat(or1).isEqualTo(FALSE)
        assertThat(or2).isEqualTo(FALSE)
    }

    @Test
    fun testOneOperand() {
        val or1: Constraint = or(f1)
        val or2: Constraint = or(listOf(f1))
        assertThat(or1).isEqualTo(f1)
        assertThat(or2).isEqualTo(f1)
    }

    @Test
    fun testConstants() {
        val or1 = or(f1, TRUE) as Or
        val or2 = or(f1, FALSE) as Or
        assertThat(or1.operands).containsExactly(f1, TRUE)
        assertThat(or2.operands).containsExactly(f1, FALSE)
    }

    @Test
    fun testToString() {
        val or1 = or(a, or(b, not(c), d))
        val or2 = or(a, impl(d, e), equiv(b, enumEq(enumFt("name"), "a")))
        val or3 = or(a, and(b, c), and(d, e))
        val or4 = and(or(a, b), or(c, d), e)
        assertThat(or1.toString()).isEqualTo("a / b / -c / d")
        assertThat(or2.toString()).isEqualTo("a / (d => e) / (b <=> [name = \"a\"])")
        assertThat(or3.toString()).isEqualTo("a / b & c / d & e")
        assertThat(or4.toString()).isEqualTo("(a / b) & (c / d) & e")
    }

    @Test
    fun testEquals() {
        val or1 = or(f1, f2) as Or
        val or2 = or(f1, f2, implication) as Or
        assertThat(or1 == or(f1, f2)).isTrue
        assertThat(or2 == or(f1, f2, implication)).isTrue
        assertThat(or1 == or(f2, f1)).isTrue
        assertThat(or2 == or(f1, implication, f2)).isTrue
        assertThat(or2 == or1).isFalse
    }

    @Test
    fun testFeatures() {
        val or1: Constraint = or(f1, implication)
        val or2 = or(
            a,
            enumEq(enumFt("name"), "desc"),
            and(b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
            intGt(intFt("x"), 3),
            and(b, enumEq(enumFt("desc"), "a"))
        )
        val or3 = or(
            enumIn(enumFt("f1"), "a", "b"),
            enumIn(enumFt("f1"), "c"),
            enumEq(enumFt("f1"), "a"),
            enumEq(enumFt("f2"), "c")
        )

        assertThat(or1.features()).containsExactly(f1, f2, f3)
        assertThat(or1.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(or1.intFeatures()).isEmpty()
        assertThat(or1.enumFeatures()).isEmpty()
        assertThat(or2.features().map { it.featureCode }).containsExactlyInAnyOrder(
            "a",
            "name",
            "desc",
            "b",
            "i",
            "v",
            "x"
        )
        assertThat(or2.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(or2.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(or2.enumFeatures().map { it.featureCode }).containsExactly("name", "desc")
        assertThat(or1.containsBooleanFeatures()).isTrue
        assertThat(or1.containsEnumFeatures()).isFalse
        assertThat(or1.containsIntFeatures()).isFalse
        assertThat(or2.containsBooleanFeatures()).isTrue
        assertThat(or2.containsEnumFeatures()).isTrue
        assertThat(or2.containsIntFeatures()).isTrue
        assertThat(or2.enumValues()).hasSize(2)
        assertThat(or2.enumValues()[enumFt("name")]).containsExactly("desc")
        assertThat(or2.enumValues()[enumFt("desc")]).containsExactly("a")
        assertThat(or3.enumValues()).hasSize(2)
        assertThat(or3.enumValues()[enumFt("f1")]).containsExactly("a", "b", "c")
        assertThat(or3.enumValues()[enumFt("f2")]).containsExactly("c")
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
        assertThat(or(a, b).evaluate(assEmpty)).isFalse
        assertThat(or(a, b).evaluate(ass)).isTrue
        assertThat(or(a, intEq(i1, 2), intLt(i2, 12)).evaluate(ass)).isTrue
        assertThat(or(not(a), b).evaluate(ass)).isFalse
        assertThat(or(b, intEq(i1, 2), intGt(i2, 12)).evaluate(ass)).isTrue
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
        ass1.assign(b3, false)
        ass1.assign(s1, "text")
        ass1.assign(bv1, 2)
        assertThat(or(b1, b2).restrict(assEmpty)).isEqualTo(or(b1, b2))
        assertThat(or(b1, b2).restrict(ass1)).isEqualTo(TRUE)
        assertThat(or(b2, b3).restrict(ass1)).isEqualTo(FALSE)
        assertThat(or(b1, not(b2)).restrict(ass1)).isEqualTo(TRUE)
        assertThat(or(b3, not(b1)).restrict(ass1)).isEqualTo(FALSE)
        assertThat(or(b1, enumEq(s1, "text")).restrict(ass1)).isEqualTo(TRUE)
        assertThat(or(b1, versionEq(bv1, 2)).restrict(ass1)).isEqualTo(TRUE)
        assertThat(or(b1).restrict(ass1)).isEqualTo(TRUE)
        assertThat(or(b3, enumEq(s3, "text")).restrict(ass1)).isEqualTo(enumEq(s3, enumVal("text")))
        assertThat(or(b2, bx).restrict(ass1)).isEqualTo(bx)
        assertThat(or(bx).restrict(ass1)).isEqualTo(bx)
        assertThat(or().restrict(ass1)).isEqualTo(FALSE)
    }

    @Test
    fun testSyntacticSimplify() {
        val eq = intEq(intFt("j"), 3)
        assertThat(or(a, b, eq).syntacticSimplify()).isEqualTo(or(a, b, eq))
        assertThat(or(a, b, or(TRUE, c)).syntacticSimplify()).isEqualTo(TRUE)
        assertThat(
            or(
                a, b,
                or(FALSE, c)
            ).syntacticSimplify()
        ).isEqualTo(or(a, b, c))
        assertThat(or(a, b, FALSE).syntacticSimplify()).isEqualTo(or(a, b))
        assertThat(or(FALSE, impl(a, a), not(FALSE)).syntacticSimplify()).isEqualTo(TRUE)
    }

    @Test
    fun testRenaming() {
        val constraint = or(f1, f2, and(a, f3))
        val r: FeatureRenaming = FeatureRenaming().add(f2, "b").add(f3, "c")
        assertThat(constraint.rename(r)).isEqualTo(or(f1, b, and(a, c)))
    }

    @Test
    fun testSerialization() {
        val or1: Constraint = or(f1, implication)
        val or2 = or(
            a,
            enumEq(enumFt("name"), "desc"),
            and(b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
            intGt(intFt("x"), 3),
            and(b, enumEq(enumFt("desc"), "a"))
        )
        val or3 = or(
            enumIn(enumFt("f1"), "a", "b"),
            enumIn(enumFt("f1"), "c"),
            enumEq(enumFt("f1"), "a"),
            enumEq(enumFt("f2"), "c")
        )
        val maps1 = ftMap(or1)
        val maps2 = ftMap(or2)
        val maps3 = ftMap(or3)
        assertThat(deserialize(serialize(or1, maps1.first), maps1.second)).isEqualTo(or1)
        assertThat(deserialize(serialize(or2, maps2.first), maps2.second)).isEqualTo(or2)
        assertThat(deserialize(serialize(or3, maps3.first), maps3.second)).isEqualTo(or3)
    }
}
