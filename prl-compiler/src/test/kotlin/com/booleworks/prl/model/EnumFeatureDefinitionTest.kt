package com.booleworks.prl.model

import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnumFeatureDefinitionTest {
    private val properties = mapOf(Pair("prop1", EnumProperty("prop1", EnumRange.list("val1"))))
    private val testDefinition = EnumFeatureDefinition("ft1", setOf("v1", "v2"), "desc 1", properties)

    @Test
    fun testDefaults() {
        val definition = EnumFeatureDefinition("ft1", setOf("v1", "v2"))
        assertThat(definition.code).isEqualTo("ft1")
        assertThat(definition.values).isEqualTo(setOf("v1", "v2"))
        assertThat(definition.description).isEqualTo("")
        assertThat(definition.properties).isEmpty()
    }

    @Test
    fun testFull() {
        assertThat(testDefinition.code).isEqualTo("ft1")
        assertThat(testDefinition.values).isEqualTo(setOf("v1", "v2"))
        assertThat(testDefinition.description).isEqualTo("desc 1")
        assertThat(testDefinition.properties).isEqualTo(properties)
    }

    @Test
    fun testToString() {
        assertThat(testDefinition.toString()).isEqualTo(
            "enum feature ft1 [\"v1\", \"v2\"] {" + System.lineSeparator() +
                    "  description \"desc 1\"" + System.lineSeparator() +
                    "  prop1 \"val1\"" + System.lineSeparator() +
                    "}"
        )
    }

    @Test
    fun testRenaming() {
        val f = testDefinition.rename(FeatureRenaming().add(testDefinition.feature, "x"))
        assertThat(f.code).isEqualTo("x")
        assertThat(f.values).isEqualTo(setOf("v1", "v2"))
        assertThat(f.description).isEqualTo("desc 1")
        assertThat(f.properties).isEqualTo(properties)
    }

    @Test
    fun testEquals() {
        val f1 = EnumFeatureDefinition("ft1", setOf("a", "b"))
        val f2 = EnumFeatureDefinition("ft2", setOf("a", "b"), "description", properties)
        assertThat(f1 == EnumFeatureDefinition("ft1", setOf("a", "b"))).isTrue
        assertThat(f1.equals(null)).isFalse
        assertThat(f1.equals("foo")).isFalse
        assertThat(f1 == f2).isFalse
        assertThat(f2 == f1).isFalse
        assertThat(f1 == EnumFeatureDefinition("ft1", setOf("a", "b", "c"))).isFalse
        assertThat(f1 == EnumFeatureDefinition("ft1", setOf("a", "b"), "", properties)).isFalse
    }

    @Test
    fun testHashCode() {
        val f1 = EnumFeatureDefinition("ft1", setOf("a", "b"))
        val f2 = EnumFeatureDefinition("ft2", setOf("a", "b"), "description", properties)
        assertThat(f1).hasSameHashCodeAs(f1)
        assertThat(f1).hasSameHashCodeAs(EnumFeatureDefinition("ft1", setOf("a", "b")))
        assertThat(f1).doesNotHaveSameHashCodeAs(f2)
        assertThat(f1).doesNotHaveSameHashCodeAs(EnumFeatureDefinition("ft1", setOf("a", "b", "c")))
        assertThat(f1).doesNotHaveSameHashCodeAs(EnumFeatureDefinition("ft1", setOf("a", "b"), "", properties))
    }
}
