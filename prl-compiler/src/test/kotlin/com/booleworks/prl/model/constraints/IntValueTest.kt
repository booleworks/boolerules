package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntValueTest {

    @Test
    fun testCreation() {
        val v7 = 7.toIntValue()
        assertThat(v7.value).isEqualTo(7)
    }

    @Test
    fun testEquals() {
        val v1 = intVal(7)
        assertThat(v1 == intVal(7)).isTrue
        assertThat(v1 == intVal(8)).isFalse
    }

    @Test
    fun testValue() {
        val value = 3.toIntValue()
        assertThat(value.value).isEqualTo(3)
    }

    @Test
    fun testRestrict() {
        val v1 = intVal(3)
        val ass1 = FeatureAssignment()
        assertThat(v1.restrict(ass1)).isEqualTo(v1)
    }

    @Test
    fun testRenaming() {
        val value = intVal(3)
        val r: FeatureRenaming = FeatureRenaming().add(intFt("f1"), "f2")
        assertThat(value.rename(r)).isEqualTo(value)
    }
}
