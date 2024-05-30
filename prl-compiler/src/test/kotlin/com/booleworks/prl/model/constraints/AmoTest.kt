package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AmoTest {
    private val f1 = boolFt("f1")
    private val f2 = boolFt("f2")
    private val f3 = boolFt("f3")
    private val f4 = boolFt("f4")

    @Test
    fun testCreationWithCollection() {
        val amo = amo(listOf(f1, f2, f3))
        assertThat(amo.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo.features).containsExactly(f1, f2, f3)
        assertThat(amo.isAtom()).isFalse
    }

    @Test
    fun testCreationWithVarargs() {
        val amo = amo(f1, f2, f3)
        assertThat(amo.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo.features).containsExactly(f1, f2, f3)
        assertThat(amo.isAtom()).isFalse
    }

    @Test
    fun testFilterDoubleFeatures() {
        val amo1 = amo(f1, f1, f2)
        val amo2 = amo(listOf(f1, f1, f2))
        val amo3 = amo(listOf(f1, f1, f1))
        assertThat(amo1.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo2.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo3.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo1.features).containsExactly(f1, f2)
        assertThat(amo2.features).containsExactly(f1, f2)
        assertThat(amo3.features).containsExactly(f1)
    }

    @Test
    fun testEmpty() {
        val amo1 = amo()
        val amo2 = amo(listOf())
        assertThat(amo1.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo2.type).isEqualTo(ConstraintType.AMO)
        assertThat(amo1.features).isEmpty()
        assertThat(amo2.features).isEmpty()
    }

    @Test
    fun testToString() {
        val amo1 = amo()
        val amo2 = amo(f1)
        val amo3 = amo(f1, f2, f3)
        val amo4 = amo(f1, f1, f2, f2)
        assertThat(amo1.toString()).isEqualTo("amo[]")
        assertThat(amo2.toString()).isEqualTo("amo[f1]")
        assertThat(amo3.toString()).isEqualTo("amo[f1, f2, f3]")
        assertThat(amo4.toString()).isEqualTo("amo[f1, f2]")
    }

    @Test
    fun testEquals() {
        val amo1 = amo()
        val amo2 = amo(f1)
        val amo3 = amo(f1, f2, f3)
        val amo4 = amo(f1, f1, f2, f2)
        assertThat(amo1 == amo()).isTrue
        assertThat(amo2 == amo(f1)).isTrue
        assertThat(amo3 == amo(f1, f2, f3)).isTrue
        assertThat(amo4 == amo(f1, f2)).isTrue
        assertThat(amo3 == amo(f3, f2, f1)).isTrue
        assertThat(amo4 == amo(f2, f1)).isTrue
        assertThat(amo3 == amo(f1, f2)).isFalse
    }

    @Test
    fun testFeatures() {
        val amo1 = amo()
        val amo2 = amo(f1, f2, f3)
        assertThat(amo1.features()).isEmpty()
        assertThat(amo1.booleanFeatures()).isEmpty()
        assertThat(amo1.intFeatures()).isEmpty()
        assertThat(amo1.enumFeatures()).isEmpty()
        assertThat(amo2.features()).containsExactly(f1, f2, f3)
        assertThat(amo2.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(amo2.intFeatures()).isEmpty()
        assertThat(amo2.enumFeatures()).isEmpty()
        assertThat(amo1.containsBooleanFeatures()).isFalse
        assertThat(amo2.containsBooleanFeatures()).isTrue
        assertThat(amo2.containsEnumFeatures()).isFalse
        assertThat(amo2.containsIntFeatures()).isFalse
        assertThat(amo2.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val amo1 = amo()
        val amo2 = amo(f1, f2, f3)
        val ass1 = FeatureAssignment()
        val ass2 = FeatureAssignment().assign(f1, true)
        val ass3 = FeatureAssignment().assign(f1, true).assign(f2, true)
        assertThat(amo1.evaluate(ass1)).isTrue
        assertThat(amo1.evaluate(ass2)).isTrue
        assertThat(amo1.evaluate(ass3)).isTrue
        assertThat(amo2.evaluate(ass1)).isTrue
        assertThat(amo2.evaluate(ass2)).isTrue
        assertThat(amo2.evaluate(ass3)).isFalse
    }

    @Test
    fun testRestrict() {
        val amo1 = amo()
        val amo2 = amo(f1, f2, f3, f4)
        val ass1 = FeatureAssignment()
        val ass2: FeatureAssignment = FeatureAssignment().assign(f1, true)
        val ass3: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, true)
        val ass4: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f4, false)
        val ass5: FeatureAssignment = FeatureAssignment().assign(f1, false)
        val ass6: FeatureAssignment = FeatureAssignment().assign(f2, false).assign(f3, false).assign(f4, false)
        val ass7: FeatureAssignment =
            FeatureAssignment().assign(f1, false).assign(f2, false).assign(f3, false).assign(f4, false)
        val ass8: FeatureAssignment =
            FeatureAssignment().assign(f1, false).assign(f2, false).assign(f3, true).assign(f4, false)
        assertThat(amo1.restrict(ass1)).isEqualTo(TRUE)
        assertThat(amo1.restrict(ass2)).isEqualTo(TRUE)
        assertThat(amo1.restrict(ass3)).isEqualTo(TRUE)
        assertThat(amo1.restrict(ass4)).isEqualTo(TRUE)
        assertThat(amo2.restrict(ass1)).isEqualTo(amo2)
        assertThat(amo2.restrict(ass2)).isEqualTo(and(not(f2), not(f3), not(f4)))
        assertThat(amo2.restrict(ass3)).isEqualTo(FALSE)
        assertThat(amo2.restrict(ass4)).isEqualTo(and(not(f2), not(f3)))
        assertThat(amo2.restrict(ass5)).isEqualTo(amo(f2, f3, f4))
        assertThat(amo2.restrict(ass6)).isEqualTo(TRUE)
        assertThat(amo2.restrict(ass7)).isEqualTo(TRUE)
        assertThat(amo2.restrict(ass8)).isEqualTo(TRUE)
    }

    @Test
    fun testSyntacticSimplify() {
        val amo1 = amo()
        val amo2 = amo(f1)
        val amo3 = amo(f1, f2, f3)
        assertThat(amo1.syntacticSimplify()).isEqualTo(TRUE)
        assertThat(amo2.syntacticSimplify()).isEqualTo(TRUE)
        assertThat(amo3.syntacticSimplify()).isEqualTo(amo3)
    }

    @Test
    fun testRenaming() {
        val amo = amo(f1, f2, f3)
        val r: FeatureRenaming = FeatureRenaming().add(f1, "x").add(f3, "z")
        assertThat(amo.rename(r)).isEqualTo(amo(boolFt("x"), boolFt("f2"), boolFt("z")))
    }

    @Test
    fun testSerialization() {
        val amo1 = amo()
        val amo2 = amo(f1)
        val amo3 = amo(f1, f2, f3)
        val maps1 = ftMap(amo1)
        val maps2 = ftMap(amo2)
        val maps3 = ftMap(amo3)
        assertThat(deserialize(serialize(amo1, maps1.first), maps1.second)).isEqualTo(amo1)
        assertThat(deserialize(serialize(amo2, maps2.first), maps2.second)).isEqualTo(amo2)
        assertThat(deserialize(serialize(amo3, maps3.first), maps3.second)).isEqualTo(amo3)
    }
}
