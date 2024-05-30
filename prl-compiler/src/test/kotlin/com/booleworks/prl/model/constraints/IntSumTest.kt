package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntSumTest {

    private val f1: IntFeature = intFt("f1")
    private val f2: IntFeature = intFt("f2")
    private val f3: IntFeature = intFt("f3")

    private val m1: IntMul = intMul(f1)
    private val m2: IntMul = intMul(4, f2)
    private val m3: IntMul = intMul(-7, f3)

    private val sum1: IntSum = intSum(8)
    private val sum2: IntSum = intSum(m1)
    private val sum3: IntSum = intSum(m1, m2)
    private val sum4: IntSum = intSum(8, m1, m2, m3)

    @Test
    fun testCreation() {
        assertThat(sum1.offset).isEqualTo(8)
        assertThat(sum1.operands).isEmpty()
        assertThat(sum2.offset).isEqualTo(0)
        assertThat(sum2.operands).containsExactly(m1)
        assertThat(sum3.offset).isEqualTo(0)
        assertThat(sum3.operands).containsExactly(m1, m2)
        assertThat(sum4.offset).isEqualTo(8)
        assertThat(sum4.operands).containsExactly(m1, m2, m3)
    }

    @Test
    fun testToString() {
        assertThat(sum1.toString()).isEqualTo("8")
        assertThat(sum2.toString()).isEqualTo("f1")
        assertThat(sum3.toString()).isEqualTo("f1 + 4*f2")
        assertThat(sum4.toString()).isEqualTo("f1 + 4*f2 + -7*f3 + 8")
    }

    @Test
    fun testEquals() {
        assertThat(sum4 == intSum(8, m1, m2, m3)).isTrue
        assertThat(sum4 == intSum(8, m3, m2, m1)).isFalse
        assertThat(sum3 == intSum(3, m1, m2, m3)).isFalse
        assertThat(sum3 == intSum(8, m1, m2)).isFalse
    }

    @Test
    fun testRestrict() {
        val ass1: FeatureAssignment = FeatureAssignment().assign(f1, 2)
        val ass2: FeatureAssignment = FeatureAssignment().assign(f1, 2).assign(f2, -3).assign(f3, 4)
        val ass3 = FeatureAssignment()
        assertThat(sum1.restrict(ass1)).isEqualTo(8.toIntValue())
        assertThat(sum1.restrict(ass2)).isEqualTo(8.toIntValue())
        assertThat(sum1.restrict(ass3)).isEqualTo(8.toIntValue())
        assertThat(sum2.restrict(ass1)).isEqualTo(2.toIntValue())
        assertThat(sum2.restrict(ass2)).isEqualTo(2.toIntValue())
        assertThat(sum2.restrict(ass3)).isEqualTo(sum2)
        assertThat(sum3.restrict(ass1)).isEqualTo(intSum(2, m2))
        assertThat(sum3.restrict(ass2)).isEqualTo(intVal(-10))
        assertThat(sum3.restrict(ass3)).isEqualTo(sum3)
        assertThat(sum4.restrict(ass1)).isEqualTo(intSum(10, m2, m3))
        assertThat(sum4.restrict(ass2)).isEqualTo((-30).toIntValue())
        assertThat(sum4.restrict(ass3)).isEqualTo(sum4)
    }

    @Test
    fun testRenaming() {
        val f22 = intFt("f22")
        val m22 = intMul(4, f22)
        val ren1: FeatureRenaming = FeatureRenaming().add(f2, "f22")
        val ren2 = FeatureRenaming()
        assertThat(sum1.rename(ren1)).isEqualTo(sum1)
        assertThat(sum1.rename(ren2)).isEqualTo(sum1)
        assertThat(sum2.rename(ren1)).isEqualTo(sum2)
        assertThat(sum2.rename(ren2)).isEqualTo(sum2)
        assertThat(sum3.rename(ren1)).isEqualTo(intSum(m1, m22))
        assertThat(sum3.rename(ren2)).isEqualTo(sum3)
        assertThat(sum4.rename(ren1)).isEqualTo(intSum(8, m1, m22, m3))
        assertThat(sum4.rename(ren2)).isEqualTo(sum4)
    }
}
