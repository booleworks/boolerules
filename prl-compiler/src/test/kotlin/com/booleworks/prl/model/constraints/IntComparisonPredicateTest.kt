package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.PrlComparisonPredicate
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlIntValue
import com.booleworks.prl.parser.parseConstraint
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class IntComparisonPredicateTest {
    private val feature: IntFeature = intFt("f1")
    private val term: IntTerm = 4.toIntValue()

    @Test
    fun testCreationEq() {
        val c1 = intEq(feature, 4)
        val c2 = intComparison(feature, 4, ComparisonOperator.EQ)
        assertThat(c1).isEqualTo(c2)
        assertThat(c1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c1.isAtom()).isTrue
        assertThat(c1.comparison).isEqualTo(ComparisonOperator.EQ)
        assertThat(c1.left).isEqualTo(feature)
        assertThat(c1.right).isEqualTo(term)
        assertThat(c1.toString()).isEqualTo("[f1 = 4]")
    }

    @Test
    fun testCreationNe() {
        val c1 = intNe(feature, 4)
        val c2 = intComparison(feature, 4, ComparisonOperator.NE)
        assertThat(c1).isEqualTo(c2)
        assertThat(c1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c1.isAtom()).isTrue
        assertThat(c1.comparison).isEqualTo(ComparisonOperator.NE)
        assertThat(c1.left).isEqualTo(feature)
        assertThat(c1.right).isEqualTo(term)
        assertThat(c1.toString()).isEqualTo("[f1 != 4]")
    }

    @Test
    fun testCreationLe() {
        val c1 = intLe(feature, 4)
        val c2 = intComparison(feature, 4, ComparisonOperator.LE)
        assertThat(c1).isEqualTo(c2)
        assertThat(c1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c1.isAtom()).isTrue
        assertThat(c1.comparison).isEqualTo(ComparisonOperator.LE)
        assertThat(c1.left).isEqualTo(feature)
        assertThat(c1.right).isEqualTo(term)
        assertThat(c1.toString()).isEqualTo("[f1 <= 4]")
    }

    @Test
    fun testCreationLt() {
        val c1 = intLt(feature, 4)
        val c2 = intComparison(feature, 4, ComparisonOperator.LT)
        assertThat(c1).isEqualTo(c2)
        assertThat(c1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c1.isAtom()).isTrue
        assertThat(c1.comparison).isEqualTo(ComparisonOperator.LT)
        assertThat(c1.left).isEqualTo(feature)
        assertThat(c1.right).isEqualTo(term)
        assertThat(c1.toString()).isEqualTo("[f1 < 4]")
    }

    @Test
    fun testCreationGe() {
        val c1 = intGe(feature, 4)
        val c2 = intComparison(feature, 4, ComparisonOperator.GE)
        assertThat(c1).isEqualTo(c2)
        assertThat(c1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c1.isAtom()).isTrue
        assertThat(c1.comparison).isEqualTo(ComparisonOperator.GE)
        assertThat(c1.left).isEqualTo(feature)
        assertThat(c1.right).isEqualTo(term)
        assertThat(c1.toString()).isEqualTo("[f1 >= 4]")
    }

    @Test
    fun testCreationGt() {
        val c1 = intGt(feature, 4)
        val c2 = intComparison(feature, 4, ComparisonOperator.GT)
        assertThat(c1).isEqualTo(c2)
        assertThat(c1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c1.isAtom()).isTrue
        assertThat(c1.comparison).isEqualTo(ComparisonOperator.GT)
        assertThat(c1.left).isEqualTo(feature)
        assertThat(c1.right).isEqualTo(term)
        assertThat(c1.toString()).isEqualTo("[f1 > 4]")
    }

    @Test
    fun testEquals() {
        val c1 = intGe(feature, 4)
        assertThat(c1 == intGe(feature, 4)).isTrue
        assertThat(unsafeParse("[a = b]") == unsafeParse("[b = a]")).isTrue
        assertThat(unsafeParse("[a != b]") == unsafeParse("[b != a]")).isTrue
        assertThat(unsafeParse("[a < b]") == unsafeParse("[b > a]")).isTrue
        assertThat(unsafeParse("[a <= b]") == unsafeParse("[b >= a]")).isTrue
        assertThat(unsafeParse("[a > b]") == unsafeParse("[b < a]")).isTrue
        assertThat(unsafeParse("[a >= b]") == unsafeParse("[b <= a]")).isTrue
        assertThat(c1 == intGt(feature, 4)).isFalse
    }

    @Test
    fun testFeatures() {
        val f1 = intFt("f1")
        val f2 = intFt("f2")
        val predicate = intGe(f1, f2)
        assertThat(predicate.features()).containsExactly(f1, f2)
        assertThat(predicate.booleanFeatures()).isEmpty()
        assertThat(predicate.intFeatures()).containsExactly(f1, f2)
        assertThat(predicate.enumFeatures()).isEmpty()
        assertThat(predicate.containsBooleanFeatures()).isFalse
        assertThat(predicate.containsEnumFeatures()).isFalse
        assertThat(predicate.containsIntFeatures()).isTrue
        assertThat(predicate.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val feature = intFt("i")
        val two = intFt("i2")
        val three = intFt("i3")
        val four = intFt("i4")
        val ass1 = FeatureAssignment()
        ass1.assign(feature, 3)
        ass1.assign(two, 2)
        ass1.assign(three, 3)
        ass1.assign(four, 4)
        assertThat(unsafeParse("[i = 3]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i = 4]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i != 3]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i != 4]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i < 3]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i < 4]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i <= 2]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i <= 3]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i > 3]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i > 2]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i >= 3]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i >= 4]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i = i3]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i = i4]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i != i3]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i != i4]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i < i3]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i < i4]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i <= i2]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i <= i3]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i > i3]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i > i2]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i >= i3]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i >= i4]").evaluate(ass1)).isFalse
        val ass2 = FeatureAssignment()
        assertThatThrownBy { unsafeParse("[i = 3]").evaluate(ass2) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Integer Feature i is not assigned to any value")
    }

    @Test
    fun testRestrict() {
        val feature = intFt("i")
        val ass1 = FeatureAssignment()
        ass1.assign(feature, 3)
        val two = intFt("i2")
        val three = intFt("i3")
        val four = intFt("i4")
        ass1.assign(two, 2)
        ass1.assign(three, 3)
        ass1.assign(four, 4)
        val unassignedFeature = intFt("ix")
        val unassignedFeature2 = intFt("iy")
        assertThat(unsafeParse("[i = 3]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i = 4]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i = ix]").restrict(ass1)).isEqualTo(intEq(unassignedFeature, 3))
        assertThat(unsafeParse("[i != 4]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i != 3]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i != ix]").restrict(ass1)).isEqualTo(intNe(unassignedFeature, 3))
        assertThat(unsafeParse("[i > 2]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i > 3]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i > ix]").restrict(ass1)).isEqualTo(intLt(unassignedFeature, 3))
        assertThat(unsafeParse("[i >= 3]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i >= 4]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i >= ix]").restrict(ass1)).isEqualTo(intLe(unassignedFeature, 3))
        assertThat(unsafeParse("[i < 4]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i < 3]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i < ix]").restrict(ass1)).isEqualTo(intGt(unassignedFeature, 3))
        assertThat(unsafeParse("[i <= 3]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i <= 2]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i <= ix]").restrict(ass1)).isEqualTo(intGe(unassignedFeature, 3))
        assertThat(unsafeParse("[ix = iy]").restrict(ass1)).isEqualTo(intEq(unassignedFeature, unassignedFeature2))
        assertThat(unsafeParse("[ix != iy]").restrict(ass1)).isEqualTo(intNe(unassignedFeature, unassignedFeature2))
        assertThat(unsafeParse("[ix > iy]").restrict(ass1)).isEqualTo(intGt(unassignedFeature, unassignedFeature2))
        assertThat(unsafeParse("[ix >= iy]").restrict(ass1)).isEqualTo(intGe(unassignedFeature, unassignedFeature2))
        assertThat(unsafeParse("[ix < iy]").restrict(ass1)).isEqualTo(intLt(unassignedFeature, unassignedFeature2))
        assertThat(unsafeParse("[ix <= iy]").restrict(ass1)).isEqualTo(intLe(unassignedFeature, unassignedFeature2))
        assertThat(unsafeParse("[i = i3]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i = i2]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i != i4]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i != i3]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i > i2]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i > i3]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i >= i3]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i >= i4]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i < i4]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i < i3]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i <= i3]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i <= i2]").restrict(ass1)).isEqualTo(FALSE)
    }

    @Test
    fun testSerialization() {
        val m1: IntMul = intMul(intFt("f1"))
        val m2: IntMul = intMul(4, intFt("f2"))
        val m3: IntMul = intMul(-7, intFt("f3"))

        val sum1: IntSum = intSum(8)
        val sum2: IntSum = intSum(m1)
        val sum3: IntSum = intSum(m1, m2)
        val sum4: IntSum = intSum(8, m1, m2, m3)

        val c1 = intEq(intFt("f1"), 3)
        val c2 = intGt(intVal(17), intFt("f2"))
        val c3 = intGe(sum1, sum2)
        val c4 = intLt(sum2, m1)
        val c5 = intLe(sum3, m3)
        val c6 = intNe(sum4, sum4)

        val maps1 = ftMap(c1)
        val maps2 = ftMap(c2)
        val maps3 = ftMap(c3)
        val maps4 = ftMap(c4)
        val maps5 = ftMap(c5)
        val maps6 = ftMap(c6)

        assertThat(deserialize(serialize(c1, maps1.first), maps1.second)).isEqualTo(c1)
        assertThat(deserialize(serialize(c2, maps2.first), maps2.second)).isEqualTo(c2)
        assertThat(deserialize(serialize(c3, maps3.first), maps3.second)).isEqualTo(c3)
        assertThat(deserialize(serialize(c4, maps4.first), maps4.second)).isEqualTo(c4)
        assertThat(deserialize(serialize(c5, maps5.first), maps5.second)).isEqualTo(c5)
        assertThat(deserialize(serialize(c6, maps6.first), maps6.second)).isEqualTo(c6)
    }


    @Test
    fun testSyntacticSimplify() {
        assertThat(unsafeParse("[i <= j]").syntacticSimplify()).isEqualTo(unsafeParse("[i <= j]").syntacticSimplify())
        assertThat(unsafeParse("[i <= 4]").syntacticSimplify()).isEqualTo(unsafeParse("[i <= 4]").syntacticSimplify())
    }

    @Test
    fun testRenaming() {
        val predicate = unsafeParse("[a <= b]")
        val r: FeatureRenaming = FeatureRenaming().add(intFt("a"), "x")
        assertThat(predicate.rename(r)).isEqualTo(unsafeParse("[x <= b]"))
    }

    private fun unsafeParse(string: String): IntComparisonPredicate {
        val parsed = parseConstraint<PrlComparisonPredicate>(string)
        val left: IntTerm = if (parsed.left is PrlFeature)
            intFt((parsed.left as PrlFeature).featureCode)
        else
            (parsed.left as PrlIntValue).value.toIntValue()
        return if (parsed.right is PrlFeature) {
            intComparison(left, intFt((parsed.right as PrlFeature).featureCode), parsed.comparison)
        } else {
            intComparison(left, (parsed.right as PrlIntValue).value, parsed.comparison)
        }
    }
}
