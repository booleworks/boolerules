package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnumValueTest {

    @Test
    fun testCreation() {
        val v7 = enumVal("text")
        val v8 = "text".toEnumValue()
        assertThat(v7.value).isEqualTo("text")
        assertThat(v8.value).isEqualTo("text")
    }

    @Test
    fun testEquals() {
        val v1 = enumVal("text")
        assertThat(v1 == enumVal("text")).isTrue
        assertThat(v1 == enumVal("Text")).isFalse
    }

    @Test
    fun testValue() {
        val value = enumVal("text")
        assertThat(value.value).isEqualTo("text")
    }

    @Test
    fun testRenaming() {
        val feature1 = enumFt("f1")
        val feature2 = enumFt("f2")
        val value = enumVal("text")
        val r = FeatureRenaming().add(feature1, "text", "test")
        assertThat(value.rename(feature1, r)).isEqualTo(enumVal("test"))
        assertThat(value.rename(feature2, r)).isEqualTo(enumVal("text"))
    }
}
