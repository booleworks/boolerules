package com.booleworks.prl.model.rules

import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.FALSE
import com.booleworks.prl.model.constraints.TRUE
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumEq
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.ftMap
import com.booleworks.prl.model.constraints.impl
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.intGt
import com.booleworks.prl.model.constraints.intLt
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.constraints.toEnumValue
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.model.constraints.versionGt
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConstraintRuleTest {
    private val properties = mapOf(
        Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))),
        Pair("p2", IntProperty("p2", IntRange.list(42)))
    )
    private val constraint = impl(boolFt("a"), boolFt("b"))
    private val and = and(
        boolFt("a"),
        or(boolFt("b"), intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
        intGt(intFt("x"), 3),
        or(boolFt("b"), enumEq(enumFt("desc"), "a"))
    )

    @Test
    fun testDefaultsConstructor() {
        val rule = ConstraintRule(constraint)
        assertThat(rule.constraint).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("rule a => b")

        val map = ftMap(constraint)
        val deserialize = deserialize(serialize(rule, map.first), map.second)
        assertThat(deserialize).isEqualTo(rule)
    }

    @Test
    fun testFullConstructor() {
        val rule = ConstraintRule(constraint, "id string", "text text", properties)
        assertThat(rule.constraint).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("id string")
        assertThat(rule.description).isEqualTo("text text")
        assertThat(rule.properties).containsExactlyInAnyOrderEntriesOf(properties)
        assertThat(rule.toString()).isEqualTo(
            "rule a => b {" + System.lineSeparator() +
                    "  id \"id string\"" + System.lineSeparator() +
                    "  description \"text text\"" + System.lineSeparator() +
                    "  p1 \"text 1\"" + System.lineSeparator() +
                    "  p2 42" + System.lineSeparator() +
                    "}"
        )
        val map = ftMap(constraint)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testEquals() {
        val rule = ConstraintRule(constraint, "id string", "text text", properties)
        val rule1 = ConstraintRule(constraint, "id string", "text text", properties)
        val rule2 = ConstraintRule(constraint, "id string 2", "text text", properties)
        val rule3 = ConstraintRule(constraint, "id string", "text text 2", properties)
        val rule4 = ConstraintRule(
            constraint,
            "id string",
            "text text",
            mapOf(Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))))
        )
        val rule5 = ConstraintRule(TRUE, "id string", "text text", properties)
        assertThat(rule == rule1).isTrue
        assertThat(rule == rule2).isFalse
        assertThat(rule == rule3).isFalse
        assertThat(rule == rule4).isFalse
        assertThat(rule == rule4).isFalse
        assertThat(rule == rule5).isFalse
    }

    @Test
    fun testFeatures() {
        val rule = ConstraintRule(and)
        assertThat(rule.features().map { it.featureCode }).containsExactly("a", "b", "i", "v", "x", "desc")
        assertThat(rule.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(rule.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(rule.enumFeatures().map { it.featureCode }).containsExactly("desc")
        assertThat(rule.containsBooleanFeatures()).isTrue
        assertThat(rule.containsEnumFeatures()).isTrue
        assertThat(rule.containsIntFeatures()).isTrue
        assertThat(rule.enumValues()).hasSize(1)
        assertThat(rule.enumValues()[enumFt("desc")]).containsExactly("a")
    }

    @Test
    fun testEvaluate() {
        val rule = ConstraintRule(and)
        val ass: FeatureAssignment = FeatureAssignment()
            .assign(boolFt("a"), true)
            .assign(boolFt("b"), true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 42)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(boolFt("b"), false)
        assertThat(rule.evaluate(ass)).isFalse
    }

    @Test
    fun testRestriction() {
        val rule = ConstraintRule(and)
        val ass: FeatureAssignment = FeatureAssignment()
            .assign(boolFt("a"), true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 42)
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(and(or(boolFt("b"), versionFt("v")), boolFt("b"))))
        ass.assign(boolFt("b"), true)
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(TRUE))
        ass.assign(boolFt("b"), false)
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(FALSE))
    }

    @Test
    fun testSyntacticSimplify() {
        val or = or(and(enumEq(enumFt("name"), "text"), enumEq(enumFt("name"), "text"), TRUE), FALSE)
        val constraintRule = ConstraintRule(or)
        val simplifiedRule = constraintRule.syntacticSimplify()
        assertThat(simplifiedRule).isInstanceOf(ConstraintRule::class.java)
        assertThat(simplifiedRule.constraint).isEqualTo(enumEq(enumFt("name"), "text".toEnumValue()))
        assertThat(simplifiedRule.properties).isEmpty()
        assertThat(simplifiedRule.id).isEmpty()
        assertThat(simplifiedRule.description).isEmpty()
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(boolFt("a"), "a1").add(intFt("i"), "i1")
        val constraint = and(boolFt("a"), enumEq(enumFt("name"), "desc"), or(boolFt("b"), intLt(intFt("i"), 8)))
        val rule = ConstraintRule(constraint)
        assertThat(rule.rename(r).constraint).isEqualTo(
            and(
                boolFt("a1"),
                enumEq(enumFt("name"), "desc"),
                or(boolFt("b"), intLt(intFt("i1"), 8))
            )
        )
    }
}
