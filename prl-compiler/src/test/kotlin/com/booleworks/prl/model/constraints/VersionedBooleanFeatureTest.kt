package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class VersionedBooleanFeatureTest {

    @Test
    fun testCreation() {
        val f1Def = BooleanFeatureDefinition("f1", true)
        val f1 = f1Def.feature
        val f2 = versionFt("f1")
        assertThat(f1 == f2).isTrue
        assertThat(f1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(f1.isAtom()).isTrue
        assertThat(f1.featureCode).isEqualTo("f1")
    }

    @Test
    fun testCreationBackticks() {
        val f1 = versionFt("Special feature &# x")
        assertThat(f1.featureCode).isEqualTo("Special feature &# x")
        assertThat(f1.isAtom()).isTrue
        assertThat(f1.type).isEqualTo(ConstraintType.ATOM)
    }

    @Test
    fun testToString() {
        val f1 = versionFt("Special feature &# x")
        val f2 = versionFt("f2")
        assertThat(f1.toString()).isEqualTo("`Special feature &# x`")
        assertThat(f2.toString()).isEqualTo("f2")
    }

    @Test
    fun testEquals() {
        val f1 = versionFt("f1")
        val f2 = versionFt("f2")
        assertThat(f1 == versionFt("f1")).isTrue
        assertThat(f1 == f2).isFalse
        assertThat(f1 == versionFt("g1")).isFalse
        assertThat(f1 == versionFt("f2")).isFalse
    }

    @Test
    fun testFeatures() {
        val feature = versionFt("f1")
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
        val feature = versionFt("f1")
        val ass1 = FeatureAssignment().assign(feature, 3)
        val ass2 = FeatureAssignment().assign(feature, false)
        val ass3 = FeatureAssignment().assign(feature, false)
        val ass4 = FeatureAssignment()
        val ass5 = FeatureAssignment()
        assertThat(feature.evaluate(ass1)).isTrue
        assertThat(feature.evaluate(ass2)).isFalse
        assertThat(feature.evaluate(ass3)).isFalse
        assertThatThrownBy { ass4.assign(feature, true) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Versioned features can't be assigned to true directly. Please assign a concrete version instead.")
        assertThat(feature.evaluate(ass5)).isFalse
    }

    @Test
    fun testRestrict() {
        val feature = versionFt("f1")
        val ass1 = FeatureAssignment().assign(feature, 3)
        val ass2 = FeatureAssignment().assign(feature, false)
        val ass3 = FeatureAssignment()
        assertThat(feature.restrict(ass1)).isEqualTo(TRUE)
        assertThat(feature.restrict(ass2)).isEqualTo(FALSE)
        assertThat(feature.restrict(ass3)).isEqualTo(feature)
    }

    @Test
    fun testSyntacticSimplify() {
        val f1 = versionFt("f1")
        assertThat(f1.syntacticSimplify()).isEqualTo(f1)
    }

    @Test
    fun testRenaming() {
        val feature = versionFt("f1")
        val r: FeatureRenaming = FeatureRenaming().add(feature, "f2")
        assertThat(feature.rename(r)).isEqualTo(versionFt("f2"))
    }
}
