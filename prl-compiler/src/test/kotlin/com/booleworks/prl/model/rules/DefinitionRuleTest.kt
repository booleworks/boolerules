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
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.intGt
import com.booleworks.prl.model.constraints.intLt
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.model.constraints.versionGt
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DefinitionRuleTest {
    private val properties = mapOf(
        Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))),
        Pair("p2", IntProperty("p2", IntRange.list(42)))
    )
    private val feature = boolFt("f")
    private val constraint = and(boolFt("a"), boolFt("b"))
    private val and = and(
        boolFt("a"),
        or(boolFt("b"), intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
        intGt(intFt("x"), 3),
        or(boolFt("b"), enumEq(enumFt("desc"), "a"))
    )

    @Test
    fun testDefaultsConstructor() {
        val rule = DefinitionRule(feature, constraint)
        assertThat(rule.feature).isEqualTo(feature)
        assertThat(rule.definition).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("rule f is a & b")
        val map = ftMap(constraint, feature)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testFullConstructor() {
        val rule = DefinitionRule(feature, constraint, "id string", "text text", properties)
        assertThat(rule.feature).isEqualTo(feature)
        assertThat(rule.definition).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("id string")
        assertThat(rule.description).isEqualTo("text text")
        assertThat(rule.properties).containsExactlyInAnyOrderEntriesOf(properties)
        assertThat(rule.toString()).isEqualTo(
            "rule f is a & b {" + System.lineSeparator() +
                    "  id \"id string\"" + System.lineSeparator() +
                    "  description \"text text\"" + System.lineSeparator() +
                    "  p1 \"text 1\"" + System.lineSeparator() +
                    "  p2 42" + System.lineSeparator() +
                    "}"
        )
        val map = ftMap(constraint, feature)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testEquals() {
        val rule = DefinitionRule(feature, constraint, "id string", "text text", properties)
        val rule1 = DefinitionRule(feature, constraint, "id string", "text text", properties)
        val rule2 = DefinitionRule(feature, constraint, "id string 2", "text text", properties)
        val rule3 = DefinitionRule(feature, constraint, "id string", "text text 2", properties)
        val rule4 =
            DefinitionRule(
                feature,
                constraint,
                "id string",
                "text text",
                mapOf(Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))))
            )
        val rule5 = DefinitionRule(boolFt("f2"), constraint, "id string", "text text", properties)
        val rule6 = DefinitionRule(feature, TRUE, "id string", "text text", properties)
        assertThat(rule == rule1).isTrue
        assertThat(rule == rule2).isFalse
        assertThat(rule == rule3).isFalse
        assertThat(rule == rule4).isFalse
        assertThat(rule == rule4).isFalse
        assertThat(rule == rule5).isFalse
        assertThat(rule == rule6).isFalse
    }

    @Test
    fun testFeatures() {
        val rule = DefinitionRule(boolFt("f1"), and)
        assertThat(rule.features().map { it.featureCode }).containsExactly("a", "b", "i", "v", "x", "desc", "f1")
        assertThat(rule.booleanFeatures().map { it.featureCode }).containsExactly("a", "b", "f1")
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
        val feature = boolFt("feat")
        val rule = DefinitionRule(feature, and)
        val ass = FeatureAssignment()
            .assign(feature, true)
            .assign(boolFt("a"), true)
            .assign(boolFt("b"), true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 42)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(boolFt("b"), false)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(feature, false)
        assertThat(rule.evaluate(ass)).isTrue
    }

    @Test
    fun testRestrict() {
        val rule = DefinitionRule(feature, constraint)
        val ass1: FeatureAssignment = FeatureAssignment().assign(feature, true)
        val ass2: FeatureAssignment = FeatureAssignment().assign(feature, false)
        val ass3: FeatureAssignment = FeatureAssignment().assign(boolFt("a"), true)
        val ass4: FeatureAssignment = FeatureAssignment().assign(boolFt("a"), true).assign(boolFt("b"), true)
        val ass5: FeatureAssignment = FeatureAssignment().assign(boolFt("a"), false)
        assertThat(rule.restrict(ass1)).isEqualTo(ConstraintRule(constraint))
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(not(constraint)))
        assertThat(rule.restrict(ass3)).isEqualTo(DefinitionRule(feature, boolFt("b")))
        assertThat(rule.restrict(ass4)).isEqualTo(ConstraintRule(boolFt("f")))
        assertThat(rule.restrict(ass5)).isEqualTo(ConstraintRule(not(boolFt("f"))))
    }

    @Test
    fun testSyntacticSimplify() {
        val rule1 = DefinitionRule(feature, feature)
        val rule2 = DefinitionRule(feature, or(boolFt("b"), boolFt("a"), TRUE))
        val rule3 = DefinitionRule(feature, FALSE)
        assertThat(rule1.syntacticSimplify()).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule2.syntacticSimplify()).isEqualTo(ConstraintRule(feature))
        assertThat(rule3.syntacticSimplify()).isEqualTo(ConstraintRule(not(feature)))
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(enumFt("name"), "name1").add(boolFt("f"), "f1")
        val definition = and(boolFt("a"), enumEq(enumFt("name"), "desc"))
        val rule = DefinitionRule(feature, definition)
        val renamed = rule.rename(r)
        assertThat(renamed.feature).isEqualTo(boolFt("f1"))
        assertThat(renamed.definition).isEqualTo(and(boolFt("a"), enumEq(enumFt("name1"), "desc")))
    }
}
