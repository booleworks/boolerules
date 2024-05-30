package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConstantTest {

    @Test
    fun testCreation() {
        val cTrue: Constant = constant(true)
        val cFalse: Constant = constant(false)
        assertThat(cTrue).isEqualTo(TRUE)
        assertThat(cFalse).isEqualTo(FALSE)
        assertThat(cTrue.type).isEqualTo(ConstraintType.TRUE)
        assertThat(cTrue.isAtom()).isTrue
        assertThat(cTrue.value).isEqualTo(true)
        assertThat(cFalse.type).isEqualTo(ConstraintType.FALSE)
        assertThat(cFalse.isAtom()).isTrue
        assertThat(cFalse.value).isEqualTo(false)
    }

    @Test
    fun testToString() {
        assertThat(TRUE.toString()).isEqualTo("true")
        assertThat(FALSE.toString()).isEqualTo("false")
    }

    @Test
    fun testEquals() {
        assertThat(TRUE == FALSE).isFalse
        assertThat(FALSE == TRUE).isFalse
    }

    @Test
    fun testFeatures() {
        val cTrue: Constant = TRUE
        val cFalse: Constant = FALSE
        assertThat(cTrue.features()).isEmpty()
        assertThat(cTrue.booleanFeatures()).isEmpty()
        assertThat(cTrue.intFeatures()).isEmpty()
        assertThat(cTrue.enumFeatures()).isEmpty()
        assertThat(cFalse.features()).isEmpty()
        assertThat(cFalse.booleanFeatures()).isEmpty()
        assertThat(cFalse.intFeatures()).isEmpty()
        assertThat(cFalse.enumFeatures()).isEmpty()
        assertThat(cTrue.containsBooleanFeatures()).isFalse
        assertThat(cTrue.containsEnumFeatures()).isFalse
        assertThat(cTrue.containsIntFeatures()).isFalse
        assertThat(cTrue.containsBooleanFeatures()).isFalse
        assertThat(cTrue.containsEnumFeatures()).isFalse
        assertThat(cTrue.containsIntFeatures()).isFalse
        assertThat(cFalse.enumValues()).isEmpty()
        assertThat(cTrue.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluate() {
        val ass = FeatureAssignment()
        assertThat(TRUE.evaluate(ass)).isTrue
        assertThat(FALSE.evaluate(ass)).isFalse
    }

    @Test
    fun testRestrict() {
        val ass = FeatureAssignment()
        assertThat(TRUE.restrict(ass)).isEqualTo(TRUE)
        assertThat(FALSE.restrict(ass)).isEqualTo(FALSE)
    }

    @Test
    fun testSyntacticSimplify() {
        assertThat(TRUE.syntacticSimplify()).isEqualTo(TRUE)
        assertThat(FALSE.syntacticSimplify()).isEqualTo(FALSE)
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(boolFt("y"), "x")
        assertThat(TRUE.rename(r)).isEqualTo(TRUE)
        assertThat(FALSE.rename(r)).isEqualTo(FALSE)
    }

    @Test
    fun testSerialization() {
        assertThat(deserialize(serialize(TRUE, mapOf()), mapOf())).isEqualTo(TRUE)
        assertThat(deserialize(serialize(FALSE, mapOf()), mapOf())).isEqualTo(FALSE)
    }
}
