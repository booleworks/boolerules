package com.booleworks.prl.model

import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntFeatureDefinitionTest {
    private val properties = mapOf(Pair("prop1", EnumProperty("prop1", EnumRange.list("val1"))))
    private val testDefinition = IntFeatureDefinition("ft1", IntRange.interval(1, 7), "desc 1", properties)

    @Test
    fun testDefaults() {
        val definition = IntFeatureDefinition("ft1", IntRange.interval(1, 7))
        assertThat(definition.code).isEqualTo("ft1")
        assertThat(definition.domain).isEqualTo(IntRange.interval(1, 7))
        assertThat(definition.description).isEqualTo("")
        assertThat(definition.properties).isEmpty()
    }

    @Test
    fun testBuilder() {
        assertThat(testDefinition.code).isEqualTo("ft1")
        assertThat(testDefinition.domain).isEqualTo(IntRange.interval(1, 7))
        assertThat(testDefinition.description).isEqualTo("desc 1")
        assertThat(testDefinition.properties).isEqualTo(properties)
    }

    @Test
    fun testToString() {
        assertThat(testDefinition.toString()).isEqualTo(
            "int feature ft1 [1 - 7] {" + System.lineSeparator() +
                    "  description \"desc 1\"" + System.lineSeparator() +
                    "  prop1 \"val1\"" + System.lineSeparator() +
                    "}"
        )
    }

    @Test
    fun testRenaming() {
        val f = testDefinition.rename(FeatureRenaming().add(testDefinition.feature, "x"))
        assertThat(f.code).isEqualTo("x")
        assertThat(f.domain).isEqualTo(IntRange.interval(1, 7))
        assertThat(f.description).isEqualTo("desc 1")
        assertThat(f.properties).isEqualTo(properties)
    }

    @Test
    fun testEquals() {
        val f1 = IntFeatureDefinition("ft1", IntRange.interval(1, 5))
        val f2 = IntFeatureDefinition("ft2", IntRange.interval(1, 10), "description", properties)
        assertThat(f1 == IntFeatureDefinition("ft1", IntRange.interval(1, 5))).isTrue
        assertThat(f1 == IntFeatureDefinition("ft1", IntRange.interval(1, 7))).isFalse
        assertThat(f1.equals(null)).isFalse
        assertThat(f1.equals("foo")).isFalse
        assertThat(f1 == f2).isFalse
        assertThat(f2 == f1).isFalse
        assertThat(f1 == IntFeatureDefinition("ft1", IntRange.interval(1, 4))).isFalse
        assertThat(f1 == IntFeatureDefinition("ft1", IntRange.interval(1, 5), "", properties)).isFalse
    }

    @Test
    fun testHashCode() {
        val f1 = IntFeatureDefinition("ft1", IntRange.interval(1, 5))
        val f2 = IntFeatureDefinition("ft2", IntRange.interval(1, 10), "description", properties)
        assertThat(f1).hasSameHashCodeAs(f1)
        assertThat(f1).hasSameHashCodeAs(IntFeatureDefinition("ft1", IntRange.interval(1, 5)))
        assertThat(f1).doesNotHaveSameHashCodeAs(f2)
        assertThat(f1).doesNotHaveSameHashCodeAs(IntFeatureDefinition("ft1", IntRange.interval(1, 4)))
        assertThat(f1).doesNotHaveSameHashCodeAs(IntFeatureDefinition("ft1", IntRange.interval(1, 5), "", properties))
    }
}
