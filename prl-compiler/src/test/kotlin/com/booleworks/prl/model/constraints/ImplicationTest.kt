package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ImplicationTest {
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
        val impl = impl(f1, and)
        assertThat(impl.type).isEqualTo(ConstraintType.IMPL)
        assertThat(impl.isAtom()).isFalse
        assertThat(impl.left).isEqualTo(f1)
        assertThat(impl.right).isEqualTo(and)
    }

    @Test
    fun testToString() {
        val impl1 = impl(f1, f2)
        val impl2 = impl(and(a, b), or(c, d))
        val impl3 = impl(a, impl(b, c))
        val imipl4 = impl(impl(a, b), c)
        assertThat(impl1.toString()).isEqualTo("f1 => f2")
        assertThat(impl2.toString()).isEqualTo("a & b => c / d")
        assertThat(impl3.toString()).isEqualTo("a => (b => c)")
        assertThat(imipl4.toString()).isEqualTo("(a => b) => c")
    }

    @Test
    fun testEquals() {
        val impl1 = impl(f1, f2)
        val impl2 = impl(f2, f1)
        val impl3 = impl(f1, and)
        assertThat(impl1 == impl(f1, f2)).isTrue
        assertThat(impl3 == impl(f1, and)).isTrue
        assertThat(impl1 == impl2).isFalse
        assertThat(impl1 == impl3).isFalse
    }

    @Test
    fun testFeatures() {
        val impl1 = impl(f1, or(f2, not(f3)))
        val impl2 = impl(
            a,
            impl(
                or(
                    b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)
                ),
                impl(
                    intGt(
                        intFt("x"), 3
                    ),
                    and(
                        b, enumEq(enumFt("desc"), "a")
                    )
                )
            )
        )
        val impl3 = impl(
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

        assertThat(impl1.features()).containsExactly(f1, f2, f3)
        assertThat(impl1.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(impl1.intFeatures()).isEmpty()
        assertThat(impl1.enumFeatures()).isEmpty()
        assertThat(impl2.features().map { it.featureCode }).containsExactly("a", "b", "i", "v", "x", "desc")
        assertThat(impl2.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(impl2.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(impl2.enumFeatures().map { it.featureCode }).containsExactly("desc")
        assertThat(impl1.containsBooleanFeatures()).isTrue
        assertThat(impl1.containsEnumFeatures()).isFalse
        assertThat(impl1.containsIntFeatures()).isFalse
        assertThat(impl2.containsBooleanFeatures()).isTrue
        assertThat(impl2.containsEnumFeatures()).isTrue
        assertThat(impl2.containsIntFeatures()).isTrue
        assertThat(impl2.enumValues()).hasSize(1)
        assertThat(impl2.enumValues()[enumFt("desc")]).containsExactly("a")
        assertThat(impl3.enumValues()).hasSize(2)
        assertThat(impl3.enumValues()[enumFt("f1")]).containsExactly("a", "b", "c")
        assertThat(impl3.enumValues()[enumFt("f2")]).containsExactly("c")
    }

    @Test
    fun testEvaluate() {
        val i = intFt("i")
        val ass = FeatureAssignment().assign(f1, true).assign(f2, false).assign(i, 7)
        assertThat(impl(FALSE, intGt(i, 9)).evaluate(ass)).isTrue
        assertThat(impl(FALSE, intLt(i, 9)).evaluate(ass)).isTrue
        assertThat(impl(intGt(i, 9), FALSE).evaluate(ass)).isTrue
        assertThat(impl(intLt(i, 9), FALSE).evaluate(ass)).isFalse
        assertThat(impl(f1, f2).evaluate(ass)).isFalse
        assertThat(impl(and(f1, intLt(i, 3)), f2).evaluate(ass)).isTrue
    }

    @Test
    fun testRestrict() {
        val s1 = enumFt("s1")
        val ass1: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, true)
        val ass2: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, false).assign(s1, "text")
        assertThat(impl(f1, f2).restrict(ass1)).isEqualTo(TRUE)
        assertThat(impl(f1, f2).restrict(ass2)).isEqualTo(FALSE)
        assertThat(impl(f2, enumNe(s1, "text")).restrict(ass2)).isEqualTo(TRUE)
        assertThat(impl(enumNe(s1, "text"), TRUE).restrict(ass2)).isEqualTo(TRUE)
        assertThat(impl(f1, f3).restrict(ass1)).isEqualTo(f3)
        assertThat(impl(f3, f1).restrict(ass1)).isEqualTo(TRUE)
        assertThat(impl(f2, f3).restrict(ass2)).isEqualTo(TRUE)
        assertThat(impl(f3, f2).restrict(ass2)).isEqualTo(not(f3))
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(impl(a, b).syntacticSimplify()).isEqualTo(impl(a, b))
        assertThat(impl(equiv(a, a), b).syntacticSimplify()).isEqualTo(b)
        assertThat(impl(FALSE, b).syntacticSimplify()).isEqualTo(TRUE)
        assertThat(impl(not(TRUE), TRUE).syntacticSimplify()).isEqualTo(TRUE)
        assertThat(impl(b, b).syntacticSimplify()).isEqualTo(TRUE)
        assertThat(impl(b, not(TRUE).syntacticSimplify()).syntacticSimplify()).isEqualTo(not(b))
    }

    @Test
    fun testRenaming() {
        val constraint = impl(f2, f3)
        val r: FeatureRenaming = FeatureRenaming().add(f2, "a").add(f3, "b")
        assertThat(constraint.rename(r)).isEqualTo(impl(a, b))
    }

    @Test
    fun testSerialization() {
        val impl1 = impl(f1, or(f2, not(f3)))
        val impl2 = impl(
            a,
            impl(
                or(
                    b, intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)
                ),
                impl(
                    intGt(
                        intFt("x"), 3
                    ),
                    and(
                        b, enumEq(enumFt("desc"), "a")
                    )
                )
            )
        )
        val impl3 = impl(
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
        val maps1 = ftMap(impl1)
        val maps2 = ftMap(impl2)
        val maps3 = ftMap(impl3)
        assertThat(deserialize(serialize(impl1, maps1.first), maps1.second)).isEqualTo(impl1)
        assertThat(deserialize(serialize(impl2, maps2.first), maps2.second)).isEqualTo(impl2)
        assertThat(deserialize(serialize(impl3, maps3.first), maps3.second)).isEqualTo(impl3)
    }
}
