package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlInIntRangePredicate
import com.booleworks.prl.parser.parseConstraint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntInPredicateTest {
    private val f1: IntFeature = intFt("f1")

    @Test
    fun testCreation() {
        val c = intIn(f1, IntRange.interval(1, 42))
        assertThat(c.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c.isAtom()).isTrue
        assertThat(c.term).isEqualTo(f1)
        assertThat(c.range).isEqualTo(IntRange.interval(1, 42))
        assertThat(c.toString()).isEqualTo("[f1 in [1 - 42]]")
    }

    @Test
    fun testEquals() {
        val c = intIn(f1, IntRange.interval(1, 42))
        assertThat(c == intIn(f1, IntRange.interval(1, 42))).isTrue
        assertThat(c == intIn(f1, IntRange.interval(1, 41))).isFalse
        assertThat(c == intIn(intFt("f2"), IntRange.interval(1, 42))).isFalse
    }

    @Test
    fun testFeatures() {
        val f1 = intFt("f1")
        val predicate = intIn(f1, IntRange.interval(1, 42))
        assertThat(predicate.features()).containsExactly(f1)
        assertThat(predicate.booleanFeatures()).isEmpty()
        assertThat(predicate.intFeatures()).containsExactly(f1)
        assertThat(predicate.enumFeatures()).isEmpty()
        assertThat(predicate.containsBooleanFeatures()).isFalse
        assertThat(predicate.containsEnumFeatures()).isFalse
        assertThat(predicate.containsIntFeatures()).isTrue
        assertThat(predicate.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val feature: IntFeature = intFt("i")
        val ass1 = FeatureAssignment().assign(feature, 3)
        assertThat(unsafeParse("[i in [1-7]]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i in [1-2]]").evaluate(ass1)).isFalse
        assertThat(unsafeParse("[i in [1, 3, 5]]").evaluate(ass1)).isTrue
        assertThat(unsafeParse("[i in [1, 4, 6]]").evaluate(ass1)).isFalse
    }

    @Test
    fun testRestrict() {
        val feature = intFt("i")
        val ass1 = FeatureAssignment()
        ass1.assign(feature, 3)
        assertThat(unsafeParse("[i in [1-7]]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i in [1-2]]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[i in [1, 3, 5]]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[i in [1, 4, 6]]").restrict(ass1)).isEqualTo(FALSE)
        val assEmpty = FeatureAssignment()
        assertThat(unsafeParse("[i in [1-7]]").restrict(assEmpty)).isEqualTo(intIn(feature, IntRange.interval(1, 7)))
        assertThat(unsafeParse("[i in [1, 3, 5]]").restrict(assEmpty)).isEqualTo(intIn(feature, IntRange.list(1, 3, 5)))
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(unsafeParse("[i in [1-7]]").syntacticSimplify()).isEqualTo(unsafeParse("[i in [1-7]]"))
        assertThat(unsafeParse("[i in [1, 4, 7]]").syntacticSimplify()).isEqualTo(unsafeParse("[i in [1, 4, 7]]"))
    }

    @Test
    fun testRenaming() {
        val predicate = unsafeParse("[a in [1-7]]")
        val r = FeatureRenaming().add(intFt("a"), "x")
        assertThat(predicate.rename(r)).isEqualTo(unsafeParse("[x in [1-7]]"))
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

        val c1 = intIn(intFt("f1"), IntRange.list(1, 2, 3))
        val c2 = intIn(intVal(17), IntRange.list(1, 2, 3))
        val c3 = intIn(sum1, IntRange.interval(-1, 18))
        val c4 = intIn(sum2, IntRange.interval(0, 100))
        val c5 = intIn(sum3, IntRange.list(-1, -2, -3))
        val c6 = intIn(sum4, IntRange.list(1, 2, 3))

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

    private fun unsafeParse(string: String): IntInPredicate {
        val parsed = parseConstraint<PrlInIntRangePredicate>(string)
        val term: IntTerm = intFt((parsed.term as PrlFeature).featureCode)
        return intIn(term, parsed.range)
    }
}
