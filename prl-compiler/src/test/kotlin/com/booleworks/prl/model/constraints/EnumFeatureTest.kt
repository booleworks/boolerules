package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnumFeatureTest {

    @Test
    fun testCreation() {
        val f1Def = EnumFeatureDefinition("f1", setOf("a", "b", "c"))
        val f1 = f1Def.feature
        val f2 = enumFt("f2")
        assertThat(f1.featureCode).isEqualTo("f1")
        assertThat(f2.featureCode).isEqualTo("f2")
    }

    @Test
    fun testCreationBackticks() {
        val f1 = enumFt("Special feature &# x")
        assertThat(f1.featureCode).isEqualTo("Special feature &# x")
    }

    @Test
    fun testToString() {
        val f1 = enumFt("Special feature &# x")
        val f2 = enumFt("f2")
        assertThat(f1.toString()).isEqualTo("`Special feature &# x`")
        assertThat(f2.toString()).isEqualTo("f2")
    }

    @Test
    fun testEquals() {
        val f1 = enumFt("f1")
        val f2 = enumFt("f2")
        assertThat(f1 == enumFt("f1")).isTrue
        assertThat(f1 == f2).isFalse
    }

    @Test
    fun testRestrict() {
        val f1 = enumFt("f1")
        val ass1 = FeatureAssignment()
        ass1.assign(f1, "text")
        val assEmpty = FeatureAssignment()
        assertThat(f1.restrict(ass1)).isEqualTo("text".toEnumValue())
        assertThat(f1.restrict(assEmpty)).isEqualTo(f1)
    }

    @Test
    fun testRenaming() {
        val feature = enumFt("f1")
        val r: FeatureRenaming = FeatureRenaming().add(feature, "f2")
        assertThat(feature.rename(feature, r)).isEqualTo(enumFt("f2"))
    }
}
