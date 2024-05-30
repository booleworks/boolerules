package com.booleworks.prl.model

import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BooleanFeatureDefinitionTest {
    private val properties = mapOf(Pair("prop1", EnumProperty("prop1", EnumRange.list("val1"))))
    private val testDefinition = BooleanFeatureDefinition("ft1", true, "desc 1", properties)

    @Test
    fun testDefaults() {
        val definition = BooleanFeatureDefinition("ft1")
        assertThat(definition.code).isEqualTo("ft1")
        assertThat(definition.description).isEqualTo("")
        assertThat(definition.versioned).isEqualTo(false)
        assertThat(definition.properties).isEmpty()
    }

    @Test
    fun testFull() {
        assertThat(testDefinition.code).isEqualTo("ft1")
        assertThat(testDefinition.description).isEqualTo("desc 1")
        assertThat(testDefinition.versioned).isEqualTo(true)
        assertThat(testDefinition.properties).isEqualTo(properties)
    }

    @Test
    fun testToString() {
        assertThat(testDefinition.toString()).isEqualTo(
            "versioned feature ft1 {" + System.lineSeparator() +
                    "  description \"desc 1\"" + System.lineSeparator() +
                    "  prop1 \"val1\"" + System.lineSeparator() +
                    "}"
        )
    }

    @Test
    fun testRenaming() {
        val f1 = testDefinition.rename(FeatureRenaming().add(testDefinition.feature, "x"))
        assertThat(f1.code).isEqualTo("x")
        assertThat(f1.versioned).isTrue
        assertThat(f1.description).isEqualTo("desc 1")
        assertThat(f1.versioned).isEqualTo(true)
        assertThat(f1.properties).isEqualTo(properties)

        val definition = BooleanFeatureDefinition("ft1")
        val f2 = definition.rename(FeatureRenaming().add(definition.feature, "x"))
        assertThat(f2.code).isEqualTo("x")
        assertThat(f2.versioned).isEqualTo(false)
    }

    @Test
    fun testEquals() {
        val f1 = BooleanFeatureDefinition("ft1", false)
        val f2 = BooleanFeatureDefinition("ft2", true, "description", properties)
        assertThat(f1 == BooleanFeatureDefinition("ft1", false)).isTrue
        assertThat(f1.equals(null)).isFalse
        assertThat(f1.equals("foo")).isFalse
        assertThat(f1 == f2).isFalse
        assertThat(f2 == f1).isFalse
        assertThat(f1 == BooleanFeatureDefinition("ft1", true)).isFalse
        assertThat(f1 == BooleanFeatureDefinition("ft1", false, "", properties)).isFalse
    }

    @Test
    fun testHashCode() {
        val f1 = BooleanFeatureDefinition("ft1", false)
        val f2 = BooleanFeatureDefinition("ft2", true, "description", properties)
        assertThat(f1).hasSameHashCodeAs(f1)
        assertThat(f1).hasSameHashCodeAs(BooleanFeatureDefinition("ft1", false))
        assertThat(f1).doesNotHaveSameHashCodeAs(f2)
        assertThat(f1).doesNotHaveSameHashCodeAs(BooleanFeatureDefinition("ft1", true))
        assertThat(f1).doesNotHaveSameHashCodeAs(BooleanFeatureDefinition("ft1", false, "", properties))
    }
}
