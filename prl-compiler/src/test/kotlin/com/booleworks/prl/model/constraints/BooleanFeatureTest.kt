package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BooleanFeatureTest {
    @Test
    fun testCreation() {
        val f1Def = BooleanFeatureDefinition("f1")
        val f1 = f1Def.feature
        val f2 = boolFt("f2")
        assertThat(f1.featureCode).isEqualTo("f1")
        assertThat(f1.isAtom()).isTrue
        assertThat(f1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(f2.featureCode).isEqualTo("f2")
        assertThat(f2.isAtom()).isTrue
        assertThat(f2.type).isEqualTo(ConstraintType.ATOM)
    }

    @Test
    fun testCreationBackticks() {
        val f1 = boolFt("Special feature &# x")
        assertThat(f1.featureCode).isEqualTo("Special feature &# x")
        assertThat(f1.isAtom()).isTrue
        assertThat(f1.type).isEqualTo(ConstraintType.ATOM)
    }

    @Test
    fun testToString() {
        val f1 = boolFt("Special feature &# x")
        val f2 = boolFt("f2")
        assertThat(f1.toString()).isEqualTo("`Special feature &# x`")
        assertThat(f2.toString()).isEqualTo("f2")
    }

    @Test
    fun testEquals() {
        val f1 = boolFt("f1")
        val f2 = boolFt("f2")
        assertThat(f1 == boolFt("f1")).isTrue
        assertThat(f1 == f2).isFalse
    }

    @Test
    fun testFeatures() {
        val feature = boolFt("f1")
        assertThat(feature.features()).containsExactly(feature)
        assertThat(feature.booleanFeatures()).containsExactly(feature)
        assertThat(feature.intFeatures()).isEmpty()
        assertThat(feature.enumFeatures()).isEmpty()
        assertThat(feature.containsBooleanFeatures()).isTrue
        assertThat(feature.containsEnumFeatures()).isFalse
        assertThat(feature.containsIntFeatures()).isFalse
        assertThat(feature.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val feature = boolFt("f1")
        val ass1 = FeatureAssignment().assign(feature, true)
        val ass2 = FeatureAssignment().assign(feature, false)
        val ass3 = FeatureAssignment()
        assertThat(feature.evaluate(ass1)).isTrue
        assertThat(feature.evaluate(ass2)).isFalse
        assertThat(feature.evaluate(ass3)).isFalse
    }

    @Test
    fun testRestrict() {
        val feature = boolFt("f1")
        val ass1 = FeatureAssignment().assign(feature, true)
        val ass2 = FeatureAssignment().assign(feature, false)
        val ass3 = FeatureAssignment()
        assertThat(feature.restrict(ass1)).isEqualTo(TRUE)
        assertThat(feature.restrict(ass2)).isEqualTo(FALSE)
        assertThat(feature.restrict(ass3)).isEqualTo(feature)
    }

    @Test
    fun testSyntacticSimplify() {
        val feature = boolFt("f1")
        assertThat(feature.syntacticSimplify()).isEqualTo(feature)
    }

    @Test
    fun testRenaming() {
        val feature = boolFt("f1")
        val r: FeatureRenaming = FeatureRenaming().add(feature, "f2")
        assertThat(feature.rename(r)).isEqualTo(boolFt("f2"))
    }

    @Test
    fun testSerialization() {
        val feature = boolFt("f1")
        val maps = ftMap(feature)
        assertThat(deserialize(serialize(feature, maps.first), maps.second)).isEqualTo(feature)
    }
}
