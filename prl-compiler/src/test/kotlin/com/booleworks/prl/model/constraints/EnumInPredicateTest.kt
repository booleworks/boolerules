package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlInEnumsPredicate
import com.booleworks.prl.parser.parseConstraint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnumInPredicateTest {
    private val f1: EnumFeature = enumFt("f1")
    private val a = enumVal("a")
    private val b = enumVal("b")

    @Test
    fun testCreation() {
        val c = enumIn(f1, "a", "b")
        assertThat(c.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c.isAtom()).isTrue
        assertThat(c.feature).isEqualTo(f1)
        assertThat(c.values).containsExactly(a.value, b.value)
        assertThat(c.toString()).isEqualTo("[f1 in [\"a\", \"b\"]]")
    }

    @Test
    fun testEquals() {
        val c = enumIn(f1, listOf("a", "b"))
        assertThat(c == enumIn(f1, listOf("a", "b"))).isTrue
        assertThat(c == enumIn(f1, listOf("a"))).isFalse
        assertThat(c == enumIn(enumFt("f2"), listOf("a", "b"))).isFalse
    }

    @Test
    fun testFeatures() {
        val f1 = enumFt("f1")
        val predicate = enumIn(f1, listOf("a", "b"))
        assertThat(predicate.features()).containsExactly(f1)
        assertThat(predicate.booleanFeatures()).isEmpty()
        assertThat(predicate.intFeatures()).isEmpty()
        assertThat(predicate.enumFeatures()).containsExactly(f1)
        assertThat(predicate.containsBooleanFeatures()).isFalse
        assertThat(predicate.containsEnumFeatures()).isTrue
        assertThat(predicate.containsIntFeatures()).isFalse
        assertThat(predicate.enumValues()).hasSize(1)
        assertThat(predicate.enumValues()[f1]).contains(a.value, b.value)
    }

    @Test
    fun testEvaluate() {
        val feature = enumFt("s")
        val ass1 = FeatureAssignment().assign(feature, "text")
        assertThat(unsafeParse("[s in [\"text\", \"test\"]]").evaluate(ass1)).isTrue()
        assertThat(unsafeParse("[s in [\"texxt\", \"test\"]]").evaluate(ass1)).isFalse()
    }

    @Test
    fun testRestrict() {
        val feature = enumFt("s")
        val unassignedFeature = enumFt("sx")
        val ass1 = FeatureAssignment()
        ass1.assign(feature, "text")
        assertThat(unsafeParse("[s in [\"text\", \"test\"]]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[s in [\"texxt\", \"test\"]]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[sx in [\"text\", \"test\"]]").restrict(ass1)).isEqualTo(
            enumIn(
                unassignedFeature,
                listOf("text", "test")
            )
        )
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(unsafeParse("[s in [\"text\", \"test\"]]").syntacticSimplify()).isEqualTo(unsafeParse("[s in [\"text\", \"test\"]]"))
    }

    @Test
    fun testRenaming() {
        val feature = enumFt("a")
        val predicate = unsafeParse("[a in [\"text\", \"test\"]]")
        val r = FeatureRenaming()
            .add(feature, "x")
            .add(feature, "test", "te st")
            .add(enumFt("b"), "text", "te xt")
        assertThat(predicate.rename(r)).isEqualTo(unsafeParse("[x in [\"text\", \"te st\"]]"))
    }

    @Test
    fun testSerialization() {
        val c1 = unsafeParse("[a in [\"text\", \"test\"]]")
        val maps1 = ftMap(c1)
        assertThat(deserialize(serialize(c1, maps1.first), maps1.second)).isEqualTo(c1)
    }

    private fun unsafeParse(string: String): EnumInPredicate {
        val parsed = parseConstraint<PrlInEnumsPredicate>(string)
        val feature = enumFt((parsed.term as PrlFeature).featureCode)
        return enumIn(feature, parsed.values)
    }
}
