package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntMulTest {

    private val f: IntFeature = intFt("f1")
    private val pos1: IntMul = intMul(f)
    private val pos2: IntMul = intMul(f, true)
    private val neg: IntMul = intMul(f, false)
    private val mul1: IntMul = intMul(42, f)
    private val mul2: IntMul = intMul(-42, f)
    private val zero: IntMul = intMul(0, f)

    @Test
    fun testCreation() {
        assertThat(pos1.feature).isEqualTo(f)
        assertThat(pos1.coefficient).isEqualTo(1)
        assertThat(pos2.feature).isEqualTo(f)
        assertThat(pos2.coefficient).isEqualTo(1)
        assertThat(neg.feature).isEqualTo(f)
        assertThat(neg.coefficient).isEqualTo(-1)
        assertThat(mul1.feature).isEqualTo(f)
        assertThat(mul1.coefficient).isEqualTo(42)
        assertThat(mul2.feature).isEqualTo(f)
        assertThat(mul2.coefficient).isEqualTo(-42)
        assertThat(zero.feature).isEqualTo(f)
        assertThat(zero.coefficient).isEqualTo(0)
    }

    @Test
    fun testToString() {
        assertThat(pos1.toString()).isEqualTo("f1")
        assertThat(pos2.toString()).isEqualTo("f1")
        assertThat(neg.toString()).isEqualTo("-f1")
        assertThat(mul1.toString()).isEqualTo("42*f1")
        assertThat(mul2.toString()).isEqualTo("-42*f1")
        assertThat(zero.toString()).isEqualTo("0*f1")
    }

    @Test
    fun testEquals() {
        assertThat(pos1 == pos2).isTrue
        assertThat(pos1 == neg).isFalse
        assertThat(pos1 == intMul(1, intFt("f2"))).isFalse
    }

    @Test
    fun testRestrict() {
        val ass1: FeatureAssignment = FeatureAssignment().assign(f, 3)
        val ass2: FeatureAssignment = FeatureAssignment().assign(f, -3)
        val ass3 = FeatureAssignment()
        assertThat(pos1.restrict(ass1)).isEqualTo(3.toIntValue())
        assertThat(pos1.restrict(ass2)).isEqualTo((-3).toIntValue())
        assertThat(pos1.restrict(ass3)).isEqualTo(pos1)
        assertThat(neg.restrict(ass1)).isEqualTo((-3).toIntValue())
        assertThat(neg.restrict(ass2)).isEqualTo(3.toIntValue())
        assertThat(neg.restrict(ass3)).isEqualTo(neg)
        assertThat(mul1.restrict(ass1)).isEqualTo(126.toIntValue())
        assertThat(mul1.restrict(ass2)).isEqualTo((-126).toIntValue())
        assertThat(mul1.restrict(ass3)).isEqualTo(mul1)
        assertThat(mul2.restrict(ass1)).isEqualTo((-126).toIntValue())
        assertThat(mul2.restrict(ass2)).isEqualTo(126.toIntValue())
        assertThat(mul2.restrict(ass3)).isEqualTo(mul2)
        assertThat(zero.restrict(ass1)).isEqualTo(0.toIntValue())
        assertThat(zero.restrict(ass2)).isEqualTo(0.toIntValue())
        assertThat(zero.restrict(ass3)).isEqualTo(0.toIntValue())
    }

    @Test
    fun testRenaming() {
        val f2 = intFt("f2")
        val ren1: FeatureRenaming = FeatureRenaming().add(f, "f2")
        val ren2 = FeatureRenaming()
        assertThat(pos1.rename(ren1)).isEqualTo(intMul(f2))
        assertThat(pos1.rename(ren2)).isEqualTo(pos1)
        assertThat(neg.rename(ren1)).isEqualTo(intMul(f2, false))
        assertThat(neg.rename(ren2)).isEqualTo(neg)
        assertThat(mul1.rename(ren1)).isEqualTo(intMul(42, f2))
        assertThat(mul1.rename(ren2)).isEqualTo(mul1)
        assertThat(mul2.rename(ren1)).isEqualTo(intMul(-42, f2))
        assertThat(mul2.rename(ren2)).isEqualTo(mul2)
        assertThat(zero.rename(ren1)).isEqualTo(intMul(0, f2))
        assertThat(zero.rename(ren2)).isEqualTo(zero)
    }
}
