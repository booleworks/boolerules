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
import com.booleworks.prl.model.constraints.equiv
import com.booleworks.prl.model.constraints.ftMap
import com.booleworks.prl.model.constraints.impl
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.intGe
import com.booleworks.prl.model.constraints.intGt
import com.booleworks.prl.model.constraints.intLt
import com.booleworks.prl.model.constraints.intNe
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.model.constraints.versionGt
import com.booleworks.prl.model.constraints.versionLt
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IfThenElseRuleTest {
    private val properties = mapOf(
        Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))),
        Pair("p2", IntProperty("p2", IntRange.list(42)))
    )
    private val feature = boolFt("f")
    private val constraint1 = and(boolFt("a"), boolFt("b"))
    private val constraint2 = or(not(boolFt("a")), boolFt("b"))

    @Test
    fun testDefaultsConstructor() {
        val rule = IfThenElseRule(feature, constraint1, constraint2)
        assertThat(rule.ifConstraint).isEqualTo(feature)
        assertThat(rule.thenConstraint).isEqualTo(constraint1)
        assertThat(rule.elseConstraint).isEqualTo(constraint2)
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("rule if f then a & b else -a / b")
        val map = ftMap(constraint1, feature, constraint2)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testFullConstructor() {
        val rule = IfThenElseRule(
            feature, constraint1, constraint2, "id string", "text text", properties
        )
        assertThat(rule.ifConstraint).isEqualTo(feature)
        assertThat(rule.thenConstraint).isEqualTo(constraint1)
        assertThat(rule.id).isEqualTo("id string")
        assertThat(rule.description).isEqualTo("text text")
        assertThat(rule.properties).containsExactlyInAnyOrderEntriesOf(properties)
        assertThat(rule.toString()).isEqualTo(
            "rule if f then a & b else -a / b {" + System.lineSeparator() +
                    "  id \"id string\"" + System.lineSeparator() +
                    "  description \"text text\"" + System.lineSeparator() +
                    "  p1 \"text 1\"" + System.lineSeparator() +
                    "  p2 42" + System.lineSeparator() +
                    "}"
        )
        val map = ftMap(constraint1, feature, constraint2)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testEquals() {
        val rule =
            IfThenElseRule(feature, constraint1, constraint2, "id string", "text text", properties)
        val rule1 =
            IfThenElseRule(feature, constraint1, constraint2, "id string", "text text", properties)
        val rule2 =
            IfThenElseRule(feature, constraint1, constraint2, "id string 2", "text text", properties)
        val rule3 =
            IfThenElseRule(feature, constraint1, constraint2, "id string", "text text 2", properties)
        val rule4 = IfThenElseRule(
            feature,
            constraint1,
            constraint2,
            "id string",
            "text text",
            mapOf(Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))))
        )
        val rule5 =
            IfThenElseRule(boolFt("f2"), constraint1, constraint2, "id string", "text text", properties)
        val rule6 = IfThenElseRule(feature, TRUE, constraint2, "id string", "text text", properties)
        val rule7 = IfThenElseRule(feature, constraint1, TRUE, "id string", "text text", properties)

        assertThat(rule == rule1).isTrue
        assertThat(rule == rule2).isFalse
        assertThat(rule == rule3).isFalse
        assertThat(rule == rule4).isFalse
        assertThat(rule == rule4).isFalse
        assertThat(rule == rule5).isFalse
        assertThat(rule == rule6).isFalse
        assertThat(rule == rule7).isFalse
    }

    @Test
    fun testFeatures() {
        val c1 = and(boolFt("a"), or(boolFt("b"), intLt(intFt("i"), 8), versionGt(versionFt("v"), 3)))
        val c2 = and(or(boolFt("b"), intLt(intFt("i"), 8)), intGt(intFt("x"), 3), boolFt("b"))
        val c3 = or(boolFt("b"), enumEq(enumFt("desc"), "a"))
        val rule = IfThenElseRule(c1, c2, c3)
        val rule2 = IfThenElseRule(
            enumIn(enumFt("f1"), "a", "b"),
            enumEq(enumFt("f1"), "c"),
            or(enumEq(enumFt("f1"), "a"), enumEq(enumFt("f2"), "c"))
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
        val c1 = and(
            boolFt("a"),
            enumEq(enumFt("name"), "desc"),
            or(boolFt("b"), intLt(intFt("i"), 8), versionLt(versionFt("v"), 8))
        )
        val c2 = and(intGt(intFt("x"), 3), or(boolFt("b"), enumEq(enumFt("desc"), "a")))
        val rule = IfThenElseRule(feature, c1, c2)
        val ass = FeatureAssignment()
            .assign(feature, true)
            .assign(boolFt("a"), true)
            .assign(boolFt("b"), true)
            .assign(enumFt("name"), "desc")
            .assign(enumFt("desc"), "text")
            .assign(intFt("i"), 12)
            .assign(intFt("x"), 0)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(feature, false)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(intFt("x"), 10)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(feature, true)
        ass.assign(boolFt("a"), false)
        assertThat(rule.evaluate(ass)).isFalse
    }

    @Test
    fun testRestrict() {
        val a = boolFt("a")
        val b = boolFt("b")
        val c = boolFt("c")
        val i = intFt("i")
        val constraint1 = and(boolFt("a"), not(boolFt("b")))
        val constraint2 = equiv(intGe(intFt("i"), 7), boolFt("a"))
        val constraint3 = or(boolFt("a"), boolFt("c"))
        val rule = IfThenElseRule(constraint1, constraint2, constraint3)
        val ass1: FeatureAssignment = FeatureAssignment().assign(b, false).assign(c, false).assign(i, 7)
        assertThat(rule.restrict(ass1)).isEqualTo(ConstraintRule(a))
        val ass2: FeatureAssignment = FeatureAssignment().assign(a, true).assign(b, false)
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(intGe(intFt("i"), 7)))
        val ass3: FeatureAssignment = FeatureAssignment().assign(a, false).assign(b, false)
        assertThat(rule.restrict(ass3)).isEqualTo(ConstraintRule(c))
        val ass4: FeatureAssignment = FeatureAssignment().assign(a, true).assign(i, 20)
        assertThat(rule.restrict(ass4)).isEqualTo(
            InclusionRule(not(constraint1.restrict(ass4)), constraint3.restrict(ass4))
        )
        val ass5: FeatureAssignment = FeatureAssignment().assign(b, false).assign(i, 27)
        assertThat(rule.restrict(ass5)).isEqualTo(InclusionRule(not(a), constraint3.restrict(ass5)))
        val ass6: FeatureAssignment = FeatureAssignment().assign(a, true).assign(i, 2)
        assertThat(rule.restrict(ass6)).isEqualTo(
            ConstraintRule(and(not(constraint1.restrict(ass6)), constraint3.restrict(ass6)))
        )
        val ass7: FeatureAssignment = FeatureAssignment().assign(c, true)
        assertThat(rule.restrict(ass7)).isEqualTo(
            InclusionRule(constraint1.restrict(ass7), constraint2.restrict(ass7))
        )
        val ass8: FeatureAssignment = FeatureAssignment().assign(a, false).assign(c, false)
        assertThat(rule.restrict(ass8)).isEqualTo(
            ConstraintRule(and(constraint1.restrict(ass8), constraint2.restrict(ass8)).syntacticSimplify())
        )
        val ass9: FeatureAssignment = FeatureAssignment().assign(b, false).assign(c, false)
        assertThat(rule.restrict(ass9)).isEqualTo(
            ConstraintRule(and(constraint1.restrict(ass9), constraint2.restrict(ass9)).syntacticSimplify())
        )
        val ass10: FeatureAssignment = FeatureAssignment().assign(b, false)
        assertThat(rule.restrict(ass10)).isEqualTo(
            IfThenElseRule(constraint1.restrict(ass10), constraint2, constraint3)
        )
    }

    @Test
    fun testSyntacticSimplify() {
        val a = boolFt("a")
        val b = boolFt("b")
        val c = boolFt("c")
        val s = enumFt("s")
        assertThat(IfThenElseRule(TRUE, b, c).syntacticSimplify()).isEqualTo(ConstraintRule(b))
        assertThat(IfThenElseRule(and(a, FALSE), b, c).syntacticSimplify()).isEqualTo(ConstraintRule(c))
        assertThat(IfThenElseRule(not(not(a)), a, a).syntacticSimplify()).isEqualTo(ConstraintRule(a))
        assertThat(IfThenElseRule(a, impl(a, TRUE), c).syntacticSimplify()).isEqualTo(InclusionRule(not(a), c))
        assertThat(IfThenElseRule(a, b, TRUE).syntacticSimplify()).isEqualTo(InclusionRule(a, b))
        assertThat(IfThenElseRule(a, equiv(not(a), FALSE), c).syntacticSimplify()).isEqualTo(InclusionRule(not(a), c))
        assertThat(IfThenElseRule(a, and(not(b), FALSE), c).syntacticSimplify()).isEqualTo(
            ConstraintRule(and(not(a), c))
        )
        assertThat(IfThenElseRule(a, b, FALSE).syntacticSimplify()).isEqualTo(ConstraintRule(and(a, b)))
        assertThat(IfThenElseRule(and(a, a), b, a).syntacticSimplify()).isEqualTo(ConstraintRule(and(a, b)))
        assertThat(
            IfThenElseRule(and(not(a), TRUE), and(b, b), enumEq(s, "name")).syntacticSimplify()
        ).isEqualTo(
            IfThenElseRule(not(a), b, enumEq(s, "name"))
        )
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(enumFt("name"), "name1").add(boolFt("b"), "b1").add(intFt("i"), "i1")
        val ifPart = and(boolFt("a"), enumEq(enumFt("name"), "desc"))
        val thenPart = or(boolFt("b"), intLt(intFt("i"), 8))
        val elsePart = intNe(intFt("i"), intFt("j"))
        val rule = IfThenElseRule(ifPart, thenPart, elsePart)
        val renamed = rule.rename(r)
        assertThat(renamed.ifConstraint).isEqualTo(and(boolFt("a"), enumEq(enumFt("name1"), "desc")))
        assertThat(renamed.thenConstraint).isEqualTo(or(boolFt("b1"), intLt(intFt("i1"), 8)))
        assertThat(renamed.elseConstraint).isEqualTo(intNe(intFt("i1"), intFt("j")))
    }
}
