package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VersionPredicateTest {
    private val f1 = versionFt("f1")
    private val f2 = versionFt("f2")

    @Test
    fun testCreation() {
        val f1Def = BooleanFeatureDefinition("f1", true)
        val vp1 = versionComparison(f1Def.feature as VersionedBooleanFeature, ComparisonOperator.EQ, 3)
        val vp2 = versionEq(versionFt("f1"), 3)
        assertThat(vp1 == vp2).isTrue
        assertThat(vp1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(vp1.isAtom()).isTrue
        assertThat(vp1.comparison).isEqualTo(ComparisonOperator.EQ)
        assertThat(vp1.version).isEqualTo(3)
    }

    @Test
    fun testCreationBackticks() {
        val vp1 = versionGt(versionFt("Special feature &# x"), 2)
        assertThat(vp1.feature.featureCode).isEqualTo("Special feature &# x")
        assertThat(vp1.version).isEqualTo(2)
        assertThat(vp1.comparison).isEqualTo(ComparisonOperator.GT)
        assertThat(vp1.type).isEqualTo(ConstraintType.ATOM)
        assertThat(vp1.isAtom()).isTrue
    }

    @Test
    fun testToString() {
        val vp1 = versionLe(versionFt("Special feature &# x"), 12)
        val vp2 = versionNe(f2, 18)
        assertThat(vp1.toString()).isEqualTo("`Special feature &# x`[<=12]")
        assertThat(vp2.toString()).isEqualTo("f2[!=18]")
    }

    @Test
    fun testEquals() {
        val vp1 = versionGt(f1, 2)
        val vp2 = versionEq(f2, 3)
        assertThat(vp1 == versionGt(f1, 2)).isTrue
        assertThat(vp1 == vp2).isFalse
        assertThat(vp1 == versionGt(versionFt("g1"), 2)).isFalse
        assertThat(vp1 == versionGe(f1, 2)).isFalse
        assertThat(vp1 == versionGt(f1, 3)).isFalse
    }

    @Test
    fun testFeatures() {
        val versionPredicate = versionGt(f1, 2)
        assertThat(versionPredicate.features()).containsExactly(f1)
        assertThat(versionPredicate.booleanFeatures()).isEmpty()
        assertThat(versionPredicate.intFeatures()).isEmpty()
        assertThat(versionPredicate.enumFeatures()).isEmpty()
        assertThat(versionPredicate.containsBooleanFeatures()).isFalse
        assertThat(versionPredicate.containsEnumFeatures()).isFalse
        assertThat(versionPredicate.containsIntFeatures()).isFalse
        assertThat(versionPredicate.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val versionPredicate = versionGt(f1, 2)
        val ass1 = FeatureAssignment().assign(f1, 3)
        val ass2 = FeatureAssignment().assign(f1, 1)
        val ass3 = FeatureAssignment().assign(f1, false)
        val ass4 = FeatureAssignment()
        assertThat(versionPredicate.evaluate(ass1)).isTrue
        assertThat(versionPredicate.evaluate(ass2)).isFalse
        assertThat(versionPredicate.evaluate(ass3)).isFalse
        assertThat(versionPredicate.evaluate(ass4)).isFalse
    }

    @Test
    fun testRestrict() {
        val versionPredicate = versionGt(f1, 2)
        val ass1 = FeatureAssignment().assign(f1, 3)
        val ass2 = FeatureAssignment().assign(f1, 1)
        val ass3 = FeatureAssignment().assign(f1, false)
        val ass4 = FeatureAssignment()
        assertThat(versionPredicate.restrict(ass1)).isEqualTo(TRUE)
        assertThat(versionPredicate.restrict(ass2)).isEqualTo(FALSE)
        assertThat(versionPredicate.restrict(ass3)).isEqualTo(FALSE)
        assertThat(versionPredicate.restrict(ass4)).isEqualTo(f1)
    }

    @Test
    fun testSyntacticSimplify() {
        val vp1 = versionGt(f1, 2)
        val vp2 = versionLt(f2, 0)
        assertThat(vp1.syntacticSimplify()).isEqualTo(vp1)
        assertThat(vp2.syntacticSimplify()).isEqualTo(FALSE)
    }

    @Test
    fun testRenaming() {
        val predicate = versionGt(f1, 2)
        val r: FeatureRenaming = FeatureRenaming().add(f1, "f2")
        assertThat(predicate.rename(r)).isEqualTo(versionGt(versionFt("f2"), 2))
    }

    @Test
    fun testSerialization() {
        val vp1 = versionGt(f1, 2)
        val vp2 = versionLt(f2, 0)
        val maps1 = ftMap(vp1)
        val maps2 = ftMap(vp2)
        assertThat(deserialize(serialize(vp1, maps1.first), maps1.second)).isEqualTo(vp1)
        assertThat(deserialize(serialize(vp2, maps2.first), maps2.second)).isEqualTo(vp2)
    }
}
