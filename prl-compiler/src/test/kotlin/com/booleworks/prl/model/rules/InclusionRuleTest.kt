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
import com.booleworks.prl.model.constraints.enumIn
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

class InclusionRuleTest {
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
        val rule = InclusionRule(feature, constraint)
        assertThat(rule.ifConstraint).isEqualTo(feature)
        assertThat(rule.thenConstraint).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("rule if f then a & b")
        val map = ftMap(constraint, feature)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testBuilder() {
        val rule = InclusionRule(feature, constraint, "id string", "text text", properties)
        assertThat(rule.ifConstraint).isEqualTo(feature)
        assertThat(rule.thenConstraint).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("id string")
        assertThat(rule.description).isEqualTo("text text")
        assertThat(rule.properties).containsExactlyInAnyOrderEntriesOf(properties)
        assertThat(rule.toString()).isEqualTo(
            "rule if f then a & b {" + System.lineSeparator() +
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
        val rule = InclusionRule(feature, constraint, "id string", "text text", properties)
        val rule1 = InclusionRule(feature, constraint, "id string", "text text", properties)
        val rule2 =
            InclusionRule(feature, constraint, "id string 2", "text text", properties)
        val rule3 =
            InclusionRule(feature, constraint, "id string", "text text 2", properties)
        val rule4 =
            InclusionRule(
                feature,
                constraint,
                "id string",
                "text text",
                mapOf(Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))))
            )
        val rule5 = InclusionRule(boolFt("f2"), constraint, "id string", "text text", properties)
        val rule6 = InclusionRule(feature, TRUE, "id string", "text text", properties)
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
        val c1 = and(boolFt("a"), or(boolFt("b"), intLt(intFt("i"), 8)), versionGt(versionFt("v"), 3))
        val c2 = and(
            or(boolFt("b"), intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)),
            intGt(intFt("x"), 3),
            or(boolFt("b"), enumEq(enumFt("desc"), "a"))
        )
        val rule = InclusionRule(c1, c2)
        val rule2 = InclusionRule(
            enumIn(enumFt("f1"), "a", "b"),
            or(enumEq(enumFt("f1"), "c"), enumEq(enumFt("f1"), "a"), enumEq(enumFt("f2"), "c"))
        )
        assertThat(rule.features().map { it.featureCode }).containsExactly("a", "b", "i", "v", "x", "desc")
        assertThat(rule.booleanFeatures().map { it.featureCode }).containsExactly("a", "b")
        assertThat(rule.intFeatures().map { it.featureCode }).containsExactly("i", "x")
        assertThat(rule.enumFeatures().map { it.featureCode }).containsExactly("desc")
        assertThat(rule.containsBooleanFeatures()).isTrue
        assertThat(rule.containsEnumFeatures()).isTrue
        assertThat(rule.containsIntFeatures()).isTrue
        assertThat(rule.enumValues()).hasSize(1)
        assertThat(rule.enumValues()[enumFt("desc")]).containsExactly("a")
        assertThat(rule2.enumValues()).hasSize(2)
        assertThat(rule2.enumValues()[enumFt("f1")]).containsExactly("a", "b", "c")
        assertThat(rule2.enumValues()[enumFt("f2")]).containsExactly("c")
    }

    @Test
    fun testEvaluate() {
        val feature = boolFt("feat")
        val rule = InclusionRule(feature, and)
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
        ass.assign(boolFt("b"), true)
        assertThat(rule.evaluate(ass)).isTrue
    }

    @Test
    fun testRestrict() {
        val rule = InclusionRule(feature, constraint)
        val a = boolFt("a")
        val b = boolFt("b")
        val ass1 = FeatureAssignment().assign(feature, true).assign(a, true)
        val ass2 = FeatureAssignment().assign(feature, false)
        val ass3 = FeatureAssignment().assign(a, true).assign(b, true)
        val ass4 = FeatureAssignment().assign(a, false).assign(b, true)
        val ass5 = FeatureAssignment().assign(a, true)
        assertThat(rule.restrict(ass1)).isEqualTo(ConstraintRule(b))
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule.restrict(ass3)).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule.restrict(ass4)).isEqualTo(ConstraintRule(not(feature)))
        assertThat(rule.restrict(ass5)).isEqualTo(InclusionRule(feature, b))
        val rule2 = InclusionRule(feature, feature)
        assertThat(rule2.restrict(ass5)).isEqualTo(ConstraintRule(TRUE))
    }

    @Test
    fun testSyntacticSimplify() {
        val a = boolFt("a")
        val rule1 = InclusionRule(TRUE, constraint)
        val rule2 = InclusionRule(FALSE, constraint)
        val rule3 = InclusionRule(feature, or(a, TRUE))
        val rule4 = InclusionRule(feature, and(intGt(intFt("i"), 2), FALSE))
        val constraint5 = enumEq(enumFt("name"), "text")
        val rule5 = InclusionRule(constraint5, constraint5)
        val rule6 = InclusionRule(feature, and(a, TRUE))
        assertThat(rule1.syntacticSimplify()).isEqualTo(ConstraintRule(constraint))
        assertThat(rule2.syntacticSimplify()).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule3.syntacticSimplify()).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule4.syntacticSimplify()).isEqualTo(ConstraintRule(not(feature)))
        assertThat(rule5.syntacticSimplify()).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule6.syntacticSimplify()).isEqualTo(InclusionRule(feature, a))
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(enumFt("name"), "name1").add(boolFt("b"), "b1")
        val ifPart = and(boolFt("a"), enumEq(enumFt("name"), "desc"))
        val thenPart = or(boolFt("b"), intLt(intFt("i"), 8))
        val rule = InclusionRule(ifPart, thenPart)
        val renamed = rule.rename(r)
        assertThat(renamed.ifConstraint).isEqualTo(and(boolFt("a"), enumEq(enumFt("name1"), "desc")))
        assertThat(renamed.thenConstraint).isEqualTo(or(boolFt("b1"), intLt(intFt("i"), 8)))
    }
}
