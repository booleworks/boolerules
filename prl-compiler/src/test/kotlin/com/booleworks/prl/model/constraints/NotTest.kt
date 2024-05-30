package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NotTest {
    private val f1 = boolFt("f1")
    private val f2 = boolFt("f2")
    private val f3 = boolFt("f3")
    private var a = boolFt("a")
    private var b = boolFt("b")
    private val implication = impl(f1, f2)

    @Test
    fun testCreationConstant() {
        val notFalse: Constraint = not(FALSE)
        val notTrue: Constraint = not(TRUE)
        assertThat(notFalse.type).isEqualTo(ConstraintType.TRUE)
        assertThat(notTrue.type).isEqualTo(ConstraintType.FALSE)
        assertThat(notFalse).isEqualTo(TRUE)
        assertThat(notTrue).isEqualTo(FALSE)
    }

    @Test
    fun testCreationFeature() {
        val notFeature = not(f1) as Not
        assertThat(notFeature.type).isEqualTo(ConstraintType.NOT)
        assertThat(notFeature.operand).isEqualTo(f1)
        assertThat(notFeature.isAtom()).isFalse
    }

    @Test
    fun testCreationImplication() {
        val notImplication = not(implication) as Not
        assertThat(notImplication.type).isEqualTo(ConstraintType.NOT)
        assertThat(notImplication.operand).isEqualTo(implication)
        assertThat(notImplication.isAtom()).isFalse
    }

    @Test
    fun testCreationNestedNot() {
        val not: Constraint = not(not(implication))
        assertThat(not).isEqualTo(implication)
        assertThat(not.type).isEqualTo(ConstraintType.IMPL)
        assertThat(not.isAtom()).isFalse
    }

    @Test
    fun testToString() {
        val not1 = not(f1)
        val not1b = not(not(not(f1)))
        val not2 = not(impl(f1, f2))
        val not3 = not(amo(f1, f2, f3))
        val not4 = not(and(f1, f2, not(f3)))
        val not5 = not(and(f1, not(or(f2, not(f3))), not(impl(f1, f2))))
        assertThat(not1.toString()).isEqualTo("-f1")
        assertThat(not1b.toString()).isEqualTo("-f1")
        assertThat(not2.toString()).isEqualTo("-(f1 => f2)")
        assertThat(not3.toString()).isEqualTo("-amo[f1, f2, f3]")
        assertThat(not4.toString()).isEqualTo("-(f1 & f2 & -f3)")
        assertThat(not5.toString()).isEqualTo("-(f1 & -(f2 / -f3) & -(f1 => f2))")
    }

    @Test
    fun testEquals() {
        val not1 = not(f1) as Not
        val not2 = not(implication) as Not
        val not3 = not(f2) as Not
        assertThat(not1 == not(f1)).isTrue
        assertThat(not2 == not(implication)).isTrue
        assertThat(not1 == not2).isFalse
        assertThat(not1 == not3).isFalse
    }

    @Test
    fun testFeatures() {
        val not1 = not(equiv(f1, or(f2, not(f3))))
        val not2 = not(
            equiv(
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
        )
        assertThat(not1.features()).containsExactly(f1, f2, f3)
        assertThat(not1.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(not1.intFeatures()).isEmpty()
        assertThat(not1.enumFeatures()).isEmpty()
        assertThat(not2.features().map { it.featureCode }).containsExactly("a", "b", "i", "v", "x", "desc")
        assertThat(not2.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(not2.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(not2.enumFeatures().map { it.featureCode }).containsExactly("desc")
        assertThat(not1.containsBooleanFeatures()).isTrue
        assertThat(not1.containsEnumFeatures()).isFalse
        assertThat(not1.containsIntFeatures()).isFalse
        assertThat(not2.containsBooleanFeatures()).isTrue
        assertThat(not2.containsEnumFeatures()).isTrue
        assertThat(not2.containsIntFeatures()).isTrue
        assertThat(not2.enumValues()).hasSize(1)
        assertThat(not2.enumValues()[enumFt("desc")]).containsExactly("a")
    }

    @Test
    fun testEvaluate() {
        val i1 = intFt("i1")
        val i2 = intFt("i2")
        val ass = FeatureAssignment().assign(i1, 1).assign(i2, 2)
        assertThat(not(intEq(i1, i2)).evaluate(ass)).isTrue
        assertThat(not(intNe(i1, i2)).evaluate(ass)).isFalse
    }

    @Test
    fun testRestrict() {
        val i1 = intFt("i1")
        val i2 = intFt("i2")
        val i3 = intFt("i3")
        val ass = FeatureAssignment().assign(i1, 3).assign(i2, 7)

        assertThat(not(intEq(i1, i2)).restrict(ass)).isEqualTo(TRUE)
        assertThat(not(intNe(i1, i2)).restrict(ass)).isEqualTo(FALSE)
        assertThat(not(intEq(i1, i3)).restrict(ass)).isEqualTo(not(intEq(i3, 3)))
        assertThat(not(not(f1)).restrict(ass)).isEqualTo(f1)
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(not(equiv(f1, f2)).syntacticSimplify()).isEqualTo(not(equiv(f1, f2)))
        assertThat(not(FALSE).syntacticSimplify()).isEqualTo(TRUE)
        assertThat(not(amo()).syntacticSimplify()).isEqualTo(FALSE)
    }

    @Test
    fun testRenaming() {
        val constraint = not(equiv(f2, f3))
        val r: FeatureRenaming = FeatureRenaming().add(f3, "a")
        assertThat(constraint.rename(r)).isEqualTo(not(equiv(f2, a)))
    }

    @Test
    fun testSerialization() {
        val not1 = not(equiv(f1, or(f2, not(f3))))
        val not2 = not(
            equiv(
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
        )
        val maps1 = ftMap(not1)
        val maps2 = ftMap(not2)
        assertThat(deserialize(serialize(not1, maps1.first), maps1.second)).isEqualTo(not1)
        assertThat(deserialize(serialize(not2, maps2.first), maps2.second)).isEqualTo(not2)
    }
}
