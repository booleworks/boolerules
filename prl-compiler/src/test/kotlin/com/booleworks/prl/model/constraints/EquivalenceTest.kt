package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EquivalenceTest {
    private val f1 = boolFt("f1")
    private val f2 = boolFt("f2")
    private val f3 = boolFt("f3")
    private var a = boolFt("a")
    private var b = boolFt("b")
    private var c = boolFt("c")
    private var d = boolFt("d")
    private val and = and(f2, f3)

    @Test
    fun testCreation() {
        val equiv = equiv(f1, and)
        assertThat(equiv.type).isEqualTo(ConstraintType.EQUIV)
        assertThat(equiv.isAtom()).isFalse
        assertThat(equiv.left).isEqualTo(f1)
        assertThat(equiv.right).isEqualTo(and)
    }

    @Test
    fun testToString() {
        val equiv1 = equiv(f1, f2)
        val equiv2 = equiv(and(a, b), or(c, d))
        val equiv3 = equiv(a, equiv(b, c))
        val equiv4 = equiv(equiv(a, b), c)
        assertThat(equiv1.toString()).isEqualTo("f1 <=> f2")
        assertThat(equiv2.toString()).isEqualTo("a & b <=> c / d")
        assertThat(equiv3.toString()).isEqualTo("a <=> (b <=> c)")
        assertThat(equiv4.toString()).isEqualTo("(a <=> b) <=> c")
    }

    @Test
    fun testEquals() {
        val equiv1 = equiv(f1, f2)
        val equiv2 = equiv(f2, f1)
        val equiv3 = equiv(f1, and)
        assertThat(equiv1 == equiv(f1, f2)).isTrue
        assertThat(equiv3 == equiv(f1, and)).isTrue
        assertThat(equiv1 == equiv2).isTrue
        assertThat(equiv1 == equiv3).isFalse
    }

    @Test
    fun testFeatures() {
        val equiv1 = equiv(f1, or(f2, not(f3)))
        val equiv2 = equiv(
            a,
            equiv(
                or(
                    b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)
                ),
                equiv(
                    intGt(
                        intFt("x"), 3
                    ),
                    and(
                        b, enumEq(enumFt("desc"), "a")
                    )
                )
            )
        )
        val equiv3 = equiv(
            enumIn(
                enumFt("f1"), "a", "b"
            ),
            or(
                enumIn(
                    enumFt("f1"), "c"
                ),
                enumEq(enumFt("f1"), "a"),
                enumEq(enumFt("f2"), "c")
            )
        )

        assertThat(equiv1.features()).containsExactly(f1, f2, f3)
        assertThat(equiv1.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(equiv1.intFeatures()).isEmpty()
        assertThat(equiv1.enumFeatures()).isEmpty()
        assertThat(equiv2.features().map { it.featureCode }).containsExactly("a", "b", "i", "v", "x", "desc")
        assertThat(equiv2.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(equiv2.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(equiv2.enumFeatures().map { it.featureCode }).containsExactly("desc")
        assertThat(equiv1.containsBooleanFeatures()).isTrue
        assertThat(equiv1.containsEnumFeatures()).isFalse
        assertThat(equiv1.containsIntFeatures()).isFalse
        assertThat(equiv2.containsBooleanFeatures()).isTrue
        assertThat(equiv2.containsEnumFeatures()).isTrue
        assertThat(equiv2.containsIntFeatures()).isTrue
        assertThat(equiv2.enumValues()).hasSize(1)
        assertThat(equiv2.enumValues()[enumFt("desc")]).containsExactly("a")
        assertThat(equiv3.enumValues()).hasSize(2)
        assertThat(equiv3.enumValues()[enumFt("f1")]).containsExactly("a", "b", "c")
        assertThat(equiv3.enumValues()[enumFt("f2")]).containsExactly("c")
    }

    @Test
    fun testEvaluate() {
        val i = intFt("i")
        val ass = FeatureAssignment().assign(f1, true).assign(f2, false).assign(i, 7)
        assertThat(equiv(FALSE, intGt(i, 9)).evaluate(ass)).isTrue
        assertThat(equiv(intGt(i, 9), FALSE).evaluate(ass)).isTrue
        assertThat(equiv(f2, f1).evaluate(ass)).isFalse
        assertThat(equiv(and(f1, intLt(i, 3)), f2).evaluate(ass)).isTrue
    }

    @Test
    fun testRestrict() {
        val s1 = enumFt("s1")
        val ass1: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, true)
        val ass2: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, false).assign(s1, "text")
        assertThat(equiv(f1, f2).restrict(ass1)).isEqualTo(TRUE)
        assertThat(equiv(f1, f2).restrict(ass2)).isEqualTo(FALSE)
        assertThat(equiv(f2, enumNe(s1, "text")).restrict(ass2)).isEqualTo(TRUE)
        assertThat(equiv(enumNe(s1, "text"), TRUE).restrict(ass2)).isEqualTo(FALSE)
        assertThat(equiv(f1, f3).restrict(ass1)).isEqualTo(f3)
        assertThat(equiv(f3, f1).restrict(ass1)).isEqualTo(f3)
        assertThat(equiv(f2, f3).restrict(ass2)).isEqualTo(not(f3))
        assertThat(equiv(f3, f2).restrict(ass2)).isEqualTo(not(f3))
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(equiv(a, b).syntacticSimplify()).isEqualTo(equiv(a, b))
        assertThat(equiv(impl(a, a), b).syntacticSimplify()).isEqualTo(b)
        assertThat(equiv(FALSE, b).syntacticSimplify()).isEqualTo(not(b))
        assertThat(equiv(not(TRUE), TRUE).syntacticSimplify()).isEqualTo(FALSE)
        assertThat(equiv(b, b).syntacticSimplify()).isEqualTo(TRUE)
        assertThat(equiv(b, not(TRUE).syntacticSimplify()).syntacticSimplify()).isEqualTo(not(b))
    }

    @Test
    fun testRenaming() {
        val constraint = equiv(f2, f3)
        val r: FeatureRenaming = FeatureRenaming().add(f2, "a").add(f3, "b")
        assertThat(constraint.rename(r)).isEqualTo(equiv(a, b))
    }

    @Test
    fun testSerialization() {
        val equiv1 = equiv(f1, or(f2, not(f3)))
        val equiv2 = equiv(
            a,
            equiv(
                or(
                    b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)
                ),
                equiv(
                    intGt(
                        intFt("x"), 3
                    ),
                    and(
                        b, enumEq(enumFt("desc"), "a")
                    )
                )
            )
        )
        val equiv3 = equiv(
            enumIn(
                enumFt("f1"), "a", "b"
            ),
            or(
                enumIn(
                    enumFt("f1"), "c"
                ),
                enumEq(enumFt("f1"), "a"),
                enumEq(enumFt("f2"), "c")
            )
        )
        val maps1 = ftMap(equiv1)
        val maps2 = ftMap(equiv2)
        val maps3 = ftMap(equiv3)
        assertThat(deserialize(serialize(equiv1, maps1.first), maps1.second)).isEqualTo(equiv1)
        assertThat(deserialize(serialize(equiv2, maps2.first), maps2.second)).isEqualTo(equiv2)
        assertThat(deserialize(serialize(equiv3, maps3.first), maps3.second)).isEqualTo(equiv3)
    }
}
