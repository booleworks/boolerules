package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntFeatureTest {

    @Test
    fun testCreation() {
        val f1Def = IntFeatureDefinition("f1", IntRange.interval(1, 7))
        val f1 = f1Def.feature
        val f2 = intFt("f2")
        assertThat(f1.featureCode).isEqualTo("f1")
        assertThat(f2.featureCode).isEqualTo("f2")
    }

    @Test
    fun testCreationBackticks() {
        val f1 = intFt("Special feature &# x")
        assertThat(f1.featureCode).isEqualTo("Special feature &# x")
    }

    @Test
    fun testToString() {
        val f1 = intFt("Special feature &# x")
        val f2 = intFt("f2")
        assertThat(f1.toString()).isEqualTo("`Special feature &# x`")
        assertThat(f2.toString()).isEqualTo("f2")
    }

    @Test
    fun testEquals() {
        val f1 = intFt("f1")
        val f2 = intFt("f2")
        assertThat(f1 == intFt("f1")).isTrue
        assertThat(f1 == f2).isFalse
    }

    @Test
    fun testRestrict() {
        val feature = intFt("f1")
        val ass1: FeatureAssignment = FeatureAssignment().assign(feature, 1)
        val assEmpty = FeatureAssignment()
        assertThat(feature.restrict(ass1)).isEqualTo(1.toIntValue())
        assertThat(feature.restrict(assEmpty)).isEqualTo(feature)
    }

    @Test
    fun testRenaming() {
        val feature = intFt("f1")
        val r: FeatureRenaming = FeatureRenaming().add(feature, "f2")
        assertThat(feature.rename(r)).isEqualTo(intFt("f2"))
    }
}
