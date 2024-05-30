package com.booleworks.prl.model.rules

import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.FALSE
import com.booleworks.prl.model.constraints.TRUE
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumEq
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.enumIn
import com.booleworks.prl.model.constraints.ftMap
import com.booleworks.prl.model.constraints.intEq
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

class ExclusionRuleTest {

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
        val rule = ExclusionRule(feature, constraint)
        assertThat(rule.ifConstraint).isEqualTo(feature)
        assertThat(rule.thenNotConstraint).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("rule if f thenNot a & b")
        val map = ftMap(constraint, feature)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testFullConstructor() {
        val rule = ExclusionRule(feature, constraint, "id string", "text text", properties)
        assertThat(rule.ifConstraint).isEqualTo(feature)
        assertThat(rule.thenNotConstraint).isEqualTo(constraint)
        assertThat(rule.id).isEqualTo("id string")
        assertThat(rule.description).isEqualTo("text text")
        assertThat(rule.properties).containsExactlyInAnyOrderEntriesOf(properties)
        assertThat(rule.toString()).isEqualTo(
            "rule if f thenNot a & b {" + System.lineSeparator() +
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
        val rule = ExclusionRule(feature, constraint, "id string", "text text", properties)
        val rule1 = ExclusionRule(feature, constraint, "id string", "text text", properties)
        val rule2 = ExclusionRule(feature, constraint, "id string 2", "text text", properties)
        val rule3 = ExclusionRule(feature, constraint, "id string", "text text 2", properties)
        val rule4 =
            ExclusionRule(
                feature,
                constraint,
                "id string",
                "text text",
                mapOf(Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))))
            )
        val rule5 = ExclusionRule(boolFt("f2"), constraint, "id string", "text text", properties)
        val rule6 = ExclusionRule(feature, TRUE, "id string", "text text", properties)
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
        val rule = ExclusionRule(c1, c2)
        val rule2 = ExclusionRule(
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
        val rule = ExclusionRule(feature, and)
        val ass = FeatureAssignment()
            .assign(feature, true)
            .assign(boolFt("a"), true)
            .assign(boolFt("b"), true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 42)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(boolFt("b"), false)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(feature, false)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(boolFt("b"), true)
        assertThat(rule.evaluate(ass)).isTrue
    }

    @Test
    fun testRestrict() {
        val feature: BooleanFeature = boolFt("feat")
        val a: BooleanFeature = boolFt("a")
        val b: BooleanFeature = boolFt("b")
        val rule = ExclusionRule(feature, and)
        val ass: FeatureAssignment = FeatureAssignment()
            .assign(feature, true)
            .assign(b, true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 42)
        // if true thenNot f2 -> ConstraintRule(-f2r) -> false / -f2 -> -f2
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(not(a)))
        ass.assign(b, false)
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(TRUE))
        // if false thenNot f2 -> ConstraintRUle(true) -> true / -f2 -> true
        ass.assign(feature, false)
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(b, true)
        assertThat(rule.restrict(ass)).isEqualTo(ConstraintRule(TRUE))
        assertThat(rule.evaluate(ass)).isTrue
        val ass2: FeatureAssignment = FeatureAssignment()
            .assign(a, true)
            .assign(b, true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 42)
        // if f1 thenNot true -> ConstraintRule(-f1r) -> -f1 / false -> -f1
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(not(feature)))
        // if f1 thenNot false -> ConstraintRule(true) -> -f1 / true -> true
        ass2.assign(a, false)
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(TRUE))
        val ass3: FeatureAssignment = FeatureAssignment().assign(a, true)
        // if f1 thenNot f1 -> ConstraintRule(-f1) -> -f1 / -f1 -> -(f1 & f1) -> -f1
        val constraint2 = and(boolFt("a"), boolFt("x"))
        val rule2 = ExclusionRule(constraint2, constraint2)
        assertThat(rule2.restrict(ass3)).isEqualTo(ConstraintRule(not(boolFt("x"))))

        // if f1 thenNot f2 -> ExclusionRule(f1r, f2r)
        val rule3 = ExclusionRule(and(boolFt("a"), boolFt("b")), and(boolFt("a"), boolFt("c")))
        assertThat(rule3.restrict(ass3)).isEqualTo(ExclusionRule(b, boolFt("c")))
    }

    @Test
    fun testSyntacticSimplify() {
        val constraint1 = and(boolFt("f"), intEq(intFt("i"), 2), TRUE)
        val rule1 = ExclusionRule(TRUE, constraint1)
        assertThat(rule1.syntacticSimplify()).isEqualTo(ConstraintRule(not(constraint1.syntacticSimplify())))
        val rule2 = ExclusionRule(and(feature, FALSE), constraint1)
        assertThat(rule2.syntacticSimplify()).isEqualTo(ConstraintRule(TRUE))
        val constraint3 = or(boolFt("b"), TRUE)
        val rule3 = ExclusionRule(feature, constraint3)
        assertThat(rule3.syntacticSimplify()).isEqualTo(ConstraintRule(not(feature)))
        val constraint4 = and(boolFt("f"), not(TRUE))
        val rule4 = ExclusionRule(feature, constraint4)
        assertThat(rule4.syntacticSimplify()).isEqualTo(ConstraintRule(TRUE))
        val rule5 = ExclusionRule(constraint1, constraint1)
        assertThat(rule5.syntacticSimplify()).isEqualTo(ConstraintRule(not(constraint1.syntacticSimplify())))

        // if f1 thenNot f2 -> ExclusionRule(f1r, f2r)
        val rule6 = ExclusionRule(feature, constraint1)
        assertThat(rule6.syntacticSimplify()).isEqualTo(ExclusionRule(feature, constraint1.syntacticSimplify()))
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(enumFt("name"), "name1").add(boolFt("b"), "b1")
        val ifPart = and(boolFt("a"), enumEq(enumFt("name"), "desc"))
        val thenNotPart = or(boolFt("b"), intLt(intFt("i"), 8))
        val rule = ExclusionRule(ifPart, thenNotPart)
        val renamed = rule.rename(r)
        assertThat(renamed.ifConstraint).isEqualTo(and(boolFt("a"), enumEq(enumFt("name1"), "desc")))
        assertThat(renamed.thenNotConstraint).isEqualTo(or(boolFt("b1"), intLt(intFt("i"), 8)))
    }
}
