package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExoTest {
    private val f1 = boolFt("f1")
    private val f2 = boolFt("f2")
    private val f3 = boolFt("f3")
    private val f4 = boolFt("f4")

    @Test
    fun testCreationWithCollection() {
        val exo = exo(listOf(f1, f2, f3))
        assertThat(exo.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo.features).containsExactly(f1, f2, f3)
        assertThat(exo.isAtom()).isFalse
    }

    @Test
    fun testCreationWithVarargs() {
        val exo = exo(f1, f2, f3)
        assertThat(exo.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo.features).containsExactly(f1, f2, f3)
        assertThat(exo.isAtom()).isFalse
    }

    @Test
    fun testFilterDoubleFeatures() {
        val exo1 = exo(f1, f1, f2)
        val exo2 = exo(listOf(f1, f1, f2))
        val exo3 = exo(listOf(f1, f1, f1))
        assertThat(exo1.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo2.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo3.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo1.features).containsExactly(f1, f2)
        assertThat(exo2.features).containsExactly(f1, f2)
        assertThat(exo3.features).containsExactly(f1)
    }

    @Test
    fun testEmpty() {
        val exo1 = exo()
        val exo2 = exo(listOf())
        assertThat(exo1.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo2.type).isEqualTo(ConstraintType.EXO)
        assertThat(exo1.features).isEmpty()
        assertThat(exo2.features).isEmpty()
    }

    @Test
    fun testToString() {
        val exo1 = exo()
        val exo2 = exo(f1)
        val exo3 = exo(f1, f2, f3)
        val exo4 = exo(f1, f1, f2, f2)
        assertThat(exo1.toString()).isEqualTo("exo[]")
        assertThat(exo2.toString()).isEqualTo("exo[f1]")
        assertThat(exo3.toString()).isEqualTo("exo[f1, f2, f3]")
        assertThat(exo4.toString()).isEqualTo("exo[f1, f2]")
    }

    @Test
    fun testEquals() {
        val exo1 = exo()
        val exo2 = exo(f1)
        val exo3 = exo(f1, f2, f3)
        val exo4 = exo(f1, f1, f2, f2)
        assertThat(exo1 == exo()).isTrue
        assertThat(exo2 == exo(f1)).isTrue
        assertThat(exo3 == exo(f1, f2, f3)).isTrue
        assertThat(exo4 == exo(f1, f2)).isTrue
        assertThat(exo3 == exo(f3, f2, f1)).isTrue
        assertThat(exo4 == exo(f2, f1)).isTrue
        assertThat(exo3 == exo(f1, f2)).isFalse
        assertThat(exo1.equals(null)).isFalse
    }

    @Test
    fun testFeatures() {
        val exo1 = exo()
        val exo2 = exo(f1, f2, f3)
        assertThat(exo1.features()).isEmpty()
        assertThat(exo1.booleanFeatures()).isEmpty()
        assertThat(exo1.intFeatures()).isEmpty()
        assertThat(exo1.enumFeatures()).isEmpty()
        assertThat(exo2.features()).containsExactly(f1, f2, f3)
        assertThat(exo2.booleanFeatures()).containsExactly(f1, f2, f3)
        assertThat(exo2.intFeatures()).isEmpty()
        assertThat(exo2.enumFeatures()).isEmpty()
        assertThat(exo1.containsBooleanFeatures()).isFalse
        assertThat(exo2.containsBooleanFeatures()).isTrue
        assertThat(exo2.containsEnumFeatures()).isFalse
        assertThat(exo2.containsIntFeatures()).isFalse
        assertThat(exo2.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val exo1 = exo()
        val exo2 = exo(f1, f2, f3)
        val ass1 = FeatureAssignment()
        val ass2 = FeatureAssignment()
        ass2.assign(f1, true)
        val ass3 = FeatureAssignment()
        ass3.assign(f1, true)
        ass3.assign(f2, true)
        assertThat(exo1.evaluate(ass1)).isFalse
        assertThat(exo1.evaluate(ass2)).isFalse
        assertThat(exo1.evaluate(ass3)).isFalse
        assertThat(exo2.evaluate(ass1)).isFalse
        assertThat(exo2.evaluate(ass2)).isTrue
        assertThat(exo2.evaluate(ass3)).isFalse
    }

    @Test
    fun testRestrict() {
        val exo1 = exo()
        val exo2 = exo(f1, f2, f3, f4)
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
        assertThat(exo1.restrict(ass1)).isEqualTo(FALSE)
        assertThat(exo1.restrict(ass2)).isEqualTo(FALSE)
        assertThat(exo1.restrict(ass3)).isEqualTo(FALSE)
        assertThat(exo1.restrict(ass4)).isEqualTo(FALSE)
        assertThat(exo2.restrict(ass1)).isEqualTo(exo2)
        assertThat(exo2.restrict(ass2)).isEqualTo(and(not(f2), not(f3), not(f4)))
        assertThat(exo2.restrict(ass3)).isEqualTo(FALSE)
        assertThat(exo2.restrict(ass4)).isEqualTo(and(not(f2), not(f3)))
        assertThat(exo2.restrict(ass5)).isEqualTo(exo(f2, f3, f4))
        assertThat(exo2.restrict(ass6)).isEqualTo(f1)
        assertThat(exo2.restrict(ass7)).isEqualTo(FALSE)
        assertThat(exo2.restrict(ass8)).isEqualTo(TRUE)
    }

    @Test
    fun testSyntacticSimplify() {
        val exo1 = exo()
        val exo2 = exo(f1)
        val exo3 = exo(f1, f2, f3)
        assertThat(exo1.syntacticSimplify()).isEqualTo(FALSE)
        assertThat(exo2.syntacticSimplify()).isEqualTo(f1)
        assertThat(exo3.syntacticSimplify()).isEqualTo(exo3)
    }


    @Test
    fun testRenaming() {
        val exo = exo(f1, f2, f3)
        val r: FeatureRenaming = FeatureRenaming().add(f1, "x").add(f3, "z")
        assertThat(exo.rename(r)).isEqualTo(exo(boolFt("x"), boolFt("f2"), boolFt("z")))
    }

    @Test
    fun testSerialization() {
        val exo1 = exo()
        val exo2 = exo(f1)
        val exo3 = exo(f1, f2, f3)
        val maps1 = ftMap(exo1)
        val maps2 = ftMap(exo2)
        val maps3 = ftMap(exo3)
        assertThat(deserialize(serialize(exo1, maps1.first), maps1.second)).isEqualTo(exo1)
        assertThat(deserialize(serialize(exo2, maps2.first), maps2.second)).isEqualTo(exo2)
        assertThat(deserialize(serialize(exo3, maps3.first), maps3.second)).isEqualTo(exo3)
    }
}
