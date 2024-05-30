package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.PrlComparisonPredicate
import com.booleworks.prl.parser.PrlEnumValue
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.parseConstraint
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnumComparisonPredicateTest {
    private val feature = enumFt("f1")
    private val value = enumVal("text")

    @Test
    fun testCreationEq() {
        val c = enumEq(feature, value)
        assertThat(c.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c.isAtom()).isTrue
        assertThat(c.feature).isEqualTo(feature)
        assertThat(c.value).isEqualTo(value)
        assertThat(c.comparison).isEqualTo(ComparisonOperator.EQ)
        assertThat(c.toString()).isEqualTo("[f1 = \"text\"]")
    }

    @Test
    fun testCreationNe() {
        val c = enumNe(feature, value)
        assertThat(c.type).isEqualTo(ConstraintType.ATOM)
        assertThat(c.isAtom()).isTrue
        assertThat(c.feature).isEqualTo(feature)
        assertThat(c.value).isEqualTo(value)
        assertThat(c.comparison).isEqualTo(ComparisonOperator.NE)
        assertThat(c.toString()).isEqualTo("[f1 != \"text\"]")
    }

    @Test
    fun testEquals() {
        val c = enumEq(feature, value)
        assertThat(c == enumEq(feature, value)).isTrue
        assertThat(c == enumNe(feature, value)).isFalse
    }

    @Test
    fun testFeatures() {
        val f1 = enumFt("f1")
        val predicate = enumEq(f1, value)
        assertThat(predicate.features()).containsExactly(f1)
        assertThat(predicate.booleanFeatures()).isEmpty()
        assertThat(predicate.intFeatures()).isEmpty()
        assertThat(predicate.enumFeatures()).containsExactly(f1)
        assertThat(predicate.containsBooleanFeatures()).isFalse
        assertThat(predicate.containsEnumFeatures()).isTrue
        assertThat(predicate.containsIntFeatures()).isFalse
        assertThat(predicate.enumValues()).hasSize(1)
        assertThat(predicate.enumValues()[f1]).containsExactly(value.value)
    }

    @Test
    fun testEvaluate() {
        val feature = enumFt("s")
        val text = enumFt("sx")
        val test = enumFt("ss")
        val ass1 = FeatureAssignment().assign(feature, "text").assign(text, "text").assign(test, "test")
        assertThat(unsafeParse("[s = \"text\"]").evaluate(ass1)).isTrue()
        assertThat(unsafeParse("[s = \"test\"]").evaluate(ass1)).isFalse()
        assertThat(unsafeParse("[s != \"text\"]").evaluate(ass1)).isFalse()
        assertThat(unsafeParse("[s != \"test\"]").evaluate(ass1)).isTrue()
        val ass2 = FeatureAssignment()
        Assertions.assertThatThrownBy { unsafeParse("[s = \"test\"]").evaluate(ass2) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Enum Feature s is not assigned to any value")
    }

    @Test
    fun testRestrict() {
        val feature = enumFt("s")
        val text = enumFt("sx")
        val test = enumFt("ss")
        val ass1 = FeatureAssignment()
        ass1.assign(feature, "text")
        ass1.assign(text, "text")
        ass1.assign(test, "test")
        assertThat(unsafeParse("[s = \"text\"]").restrict(ass1)).isEqualTo(TRUE)
        assertThat(unsafeParse("[s = \"test\"]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[s != \"text\"]").restrict(ass1)).isEqualTo(FALSE)
        assertThat(unsafeParse("[s != \"test\"]").restrict(ass1)).isEqualTo(TRUE)
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(unsafeParse("[s1 = \"test\"]").syntacticSimplify()).isEqualTo(unsafeParse("[s1 = \"test\"]").syntacticSimplify())
        assertThat(unsafeParse("[s1 != \"test\"]").syntacticSimplify()).isEqualTo(unsafeParse("[s1 != \"test\"]").syntacticSimplify())
    }

    @Test
    fun testRenaming() {
        val predicate = unsafeParse("[a = \"text\"]")
        val feature = enumFt("a")
        val r = FeatureRenaming()
            .add(feature, "x")
            .add(feature, "text", "test")
            .add(enumFt("y"), "text", "te xt")
        assertThat(predicate.rename(r)).isEqualTo(unsafeParse("[x = \"test\"]"))
    }

    @Test
    fun testSerialization() {
        val f1 = enumFt("f1")
        val c1 = enumEq(f1, value)
        val c2 = enumNe(f1, value)
        val maps1 = ftMap(c1)
        val maps2 = ftMap(c2)
        assertThat(deserialize(serialize(c1, maps1.first), maps1.second)).isEqualTo(c1)
        assertThat(deserialize(serialize(c2, maps2.first), maps2.second)).isEqualTo(c2)
    }

    private fun unsafeParse(string: String): EnumComparisonPredicate {
        val parsed = parseConstraint<PrlComparisonPredicate>(string)
        val feature = enumFt((parsed.left as PrlFeature).featureCode)
        val value = (parsed.right as PrlEnumValue).value.toEnumValue()
        return enumComparison(feature, value, parsed.comparison)
    }
}
