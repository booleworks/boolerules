package com.booleworks.prl.model.rules

import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.FALSE
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.equiv
import com.booleworks.prl.model.constraints.exo
import com.booleworks.prl.model.constraints.ftMap
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.datastructures.FeatureAssignment
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GroupRuleTest {
    private val properties = mapOf(
        Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))),
        Pair("p2", IntProperty("p2", IntRange.list(42)))
    )
    private val g1 = boolFt("g1")
    private val g2 = boolFt("g 2")
    private val f1 = boolFt("f1")
    private val f2 = boolFt("f2")
    private val f3 = boolFt("f3")
    private val f4 = boolFt("f#4")

    @Test
    fun testDefaultsConstructor() {
        val rule = GroupRule(GroupType.MANDATORY, g1, setOf(f1, f2, f3))
        assertThat(rule.type).isEqualTo(GroupType.MANDATORY)
        assertThat(rule.group).isEqualTo(g1)
        assertThat(rule.content).isEqualTo(setOf(f1, f2, f3))
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("mandatory group g1 contains [f1, f2, f3]")
        val map = ftMap(g1, f1, f2, f3)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testConstructor() {
        val rule = GroupRule(GroupType.OPTIONAL, g2, setOf(f1, f2, f3, f1, f2, f4))
        assertThat(rule.type).isEqualTo(GroupType.OPTIONAL)
        assertThat(rule.group).isEqualTo(g2)
        assertThat(rule.content).isEqualTo(setOf(f1, f2, f3, f4))
        assertThat(rule.id).isEqualTo("")
        assertThat(rule.description).isEqualTo("")
        assertThat(rule.properties).isEmpty()
        assertThat(rule.toString()).isEqualTo("optional group `g 2` contains [f1, f2, f3, `f#4`]")
        val map = ftMap(g2, f1, f2, f3, f4)
        assertThat(deserialize(serialize(rule, map.first), map.second)).isEqualTo(rule)
    }

    @Test
    fun testFullConstructor() {
        val rule = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3), "id string", "text text", properties)
        assertThat(rule.type).isEqualTo(GroupType.MANDATORY)
        assertThat(rule.group).isEqualTo(g1)
        assertThat(rule.content).isEqualTo(setOf(f1, f2, f3))
        assertThat(rule.id).isEqualTo("id string")
        assertThat(rule.description).isEqualTo("text text")
        assertThat(rule.properties).containsExactlyInAnyOrderEntriesOf(properties)
        assertThat(rule.toString()).isEqualTo(
            "mandatory group g1 contains [f1, f2, f3] {" + System.lineSeparator() +
                    "  id \"id string\"" + System.lineSeparator() +
                    "  description \"text text\"" + System.lineSeparator() +
                    "  p1 \"text 1\"" + System.lineSeparator() +
                    "  p2 42" + System.lineSeparator() +
                    "}"
        )
    }

    @Test
    fun testEquals() {
        val rule = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3), "id string", "text text", properties)
        val rule1 = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3, f1, f2), "id string", "text text", properties)
        val rule2 = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3), "id string 2", "text text", properties)
        val rule3 = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3), "id string", "text text 2", properties)
        val rule4 = GroupRule(
            GroupType.MANDATORY, g1, listOf(f1, f2, f3), "id string", "text text",
            mapOf(Pair("p1", EnumProperty("p1", EnumRange.list("text 1"))))
        )
        val rule5 = GroupRule(GroupType.OPTIONAL, g1, listOf(f1, f2, f3), "id string", "text text", properties)
        val rule6 = GroupRule(GroupType.MANDATORY, g2, listOf(f1, f2, f3), "id string", "text text", properties)
        val rule7 = GroupRule(GroupType.MANDATORY, g1, listOf(f2, f3), "id string", "text text", properties)

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
        val rule = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3))
        assertThat(rule.features().map { it.featureCode }).containsExactly("g1", "f1", "f2", "f3")
        assertThat(rule.booleanFeatures().map { it.featureCode }).containsExactly("g1", "f1", "f2", "f3")
        assertThat(rule.intFeatures().map { it.featureCode }).isEmpty()
        assertThat(rule.enumFeatures().map { it.featureCode }).isEmpty()
        assertThat(rule.containsBooleanFeatures()).isTrue
        assertThat(rule.containsEnumFeatures()).isFalse
        assertThat(rule.containsIntFeatures()).isFalse
        assertThat(rule.enumValues()).isEmpty()
    }

    @Test
    fun testEvaluateMandatory() {
        val g = boolFt("g")
        val f1 = boolFt("f1")
        val f2 = boolFt("f2")
        val f3 = boolFt("f3")
        val rule = GroupRule(GroupType.MANDATORY, g, setOf(f1, f2, f3))
        val ass = FeatureAssignment().assign(g, true).assign(f1, true).assign(f2, false).assign(f3, false)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(f1, false)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(f1, true)
        ass.assign(f2, true)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(g, false)
        assertThat(rule.evaluate(ass)).isFalse
    }

    @Test
    fun testEvaluateOptional() {
        val g = boolFt("g")
        val f1 = boolFt("f1")
        val f2 = boolFt("f2")
        val f3 = boolFt("f3")
        val rule = GroupRule(GroupType.OPTIONAL, g, setOf(f1, f2, f3))
        val ass: FeatureAssignment =
            FeatureAssignment().assign(g, true).assign(f1, true).assign(f2, false).assign(f3, false)
        assertThat(rule.evaluate(ass)).isTrue
        ass.assign(f1, false)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(f1, true)
        ass.assign(f2, true)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(g, false)
        ass.assign(f2, false)
        assertThat(rule.evaluate(ass)).isFalse
        ass.assign(f1, false)
        assertThat(rule.evaluate(ass)).isTrue
    }

    @Test
    fun testRestrictMandatory() {
        val g = boolFt("g")
        val f1 = boolFt("f1")
        val f2 = boolFt("f2")
        val f3 = boolFt("f3")
        val rule = GroupRule(GroupType.MANDATORY, g, setOf(f1, f2, f3))

        // mandatory group true contains [f1, f2, f3] -> exo(f1, f2, f3)
        val ass1: FeatureAssignment = FeatureAssignment().assign(g, true)
        assertThat(rule.restrict(ass1)).isEqualTo(ConstraintRule(exo(f1, f2, f3)))

        // mandatory group false contains [f1, f2, f3] -> false
        val ass2: FeatureAssignment = FeatureAssignment().assign(g, false)
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(FALSE))

        // mandatory group g contains [true, false, false] -> g
        val ass3: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, false).assign(f3, false)
        assertThat(rule.restrict(ass3)).isEqualTo(ConstraintRule(g))

        // mandatory group g contains [true, true, f3] -> false
        val ass4: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, true)
        assertThat(rule.restrict(ass4)).isEqualTo(ConstraintRule(FALSE))

        // mandatory group g contains [false, false, false] -> false
        val ass5: FeatureAssignment = FeatureAssignment().assign(f1, false).assign(f2, false).assign(f3, false)
        assertThat(rule.restrict(ass5)).isEqualTo(ConstraintRule(FALSE))

        // mandatory group g contains [false, false, f3] -> g & f3
        val ass6: FeatureAssignment = FeatureAssignment().assign(f1, false).assign(f2, false)
        assertThat(rule.restrict(ass6)).isEqualTo(ConstraintRule(and(g, f3)))

        // mandatory group g contains [true, f2, f3] -> g & -(f2 / f3) -> g & -f2 & -f3
        val ass7: FeatureAssignment = FeatureAssignment().assign(f1, true)
        assertThat(rule.restrict(ass7)).isEqualTo(ConstraintRule(and(g, not(f2), not(f3))))

        // mandatory group g contains [false, f2, f3] -> GroupRule(mandatory, g, [f2, f3])
        val ass8: FeatureAssignment = FeatureAssignment().assign(f1, false)
        assertThat(rule.restrict(ass8)).isEqualTo(GroupRule(GroupType.MANDATORY, g, listOf(f2, f3)))
    }

    @Test
    fun testRestrictOptional() {
        val g = boolFt("g")
        val f1 = boolFt("f1")
        val f2 = boolFt("f2")
        val f3 = boolFt("f3")
        val rule = GroupRule(GroupType.OPTIONAL, g, setOf(f1, f2, f3))

        // optional group true contains [f1, f2, f3] -> exo(f1, f2, f3)
        val ass1: FeatureAssignment = FeatureAssignment().assign(g, true)
        assertThat(rule.restrict(ass1)).isEqualTo(ConstraintRule(exo(f1, f2, f3)))

        // optional group false contains [f1, f2, f3] -> -(f1 / f2 / f3)
        val ass2: FeatureAssignment = FeatureAssignment().assign(g, false)
        assertThat(rule.restrict(ass2)).isEqualTo(ConstraintRule(not(or(f1, f2, f3))))

        // optional group g contains [true, false, false] -> g
        val ass3: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, false).assign(f3, false)
        assertThat(rule.restrict(ass3)).isEqualTo(ConstraintRule(g))

        // optional group g contains [true, true, f3] -> false
        val ass4: FeatureAssignment = FeatureAssignment().assign(f1, true).assign(f2, true)
        assertThat(rule.restrict(ass4)).isEqualTo(ConstraintRule(FALSE))

        // optional group g contains [false, false, false] -> -g
        val ass5: FeatureAssignment = FeatureAssignment().assign(f1, false).assign(f2, false).assign(f3, false)
        assertThat(rule.restrict(ass5)).isEqualTo(ConstraintRule(not(g)))

        // optional group g contains [false, false, f3] ->  g <=> f3
        val ass6: FeatureAssignment = FeatureAssignment().assign(f1, false).assign(f2, false)
        assertThat(rule.restrict(ass6)).isEqualTo(ConstraintRule(equiv(g, f3)))

        // optional group g contains [true, f2, f3] -> (-(f2 / f3) <=> f) & f -> ((-f2 & -f3) <=> f) & f
        val ass7: FeatureAssignment = FeatureAssignment().assign(f1, true)
        assertThat(rule.restrict(ass7)).isEqualTo(ConstraintRule(and(g, not(f2), not(f3))))

        // optional group g contains [false, f2, f3]  -> GroupRule(optional, f, [f2, f3])
        val ass8: FeatureAssignment = FeatureAssignment().assign(f1, false)
        assertThat(rule.restrict(ass8)).isEqualTo(GroupRule(GroupType.OPTIONAL, g, setOf(f2, f3)))
    }

    @Test
    fun testSyntacticSimplify() {
        val g = boolFt("g")
        val rule1 = GroupRule(GroupType.OPTIONAL, g, setOf())
        val rule2 = GroupRule(GroupType.OPTIONAL, g, setOf(f1))
        val rule3 = GroupRule(GroupType.OPTIONAL, g, setOf(f1, f2, f3))
        val rule4 = GroupRule(GroupType.MANDATORY, g, setOf())
        val rule5 = GroupRule(GroupType.MANDATORY, g, setOf(f1))
        val rule6 = GroupRule(GroupType.MANDATORY, g, setOf(f1, f2, f3))
        assertThat(rule1.syntacticSimplify()).isEqualTo(ConstraintRule(not(g)))
        assertThat(rule2.syntacticSimplify()).isEqualTo(ConstraintRule(equiv(g, f1)))
        assertThat(rule3.syntacticSimplify()).isEqualTo(rule3)
        assertThat(rule4.syntacticSimplify()).isEqualTo(ConstraintRule(FALSE))
        assertThat(rule5.syntacticSimplify()).isEqualTo(ConstraintRule(and(g, f1)))
        assertThat(rule6.syntacticSimplify()).isEqualTo(rule6)
    }

    @Test
    fun testRenaming() {
        val r = FeatureRenaming().add(g1, "group1").add(f2, "feature2")
        val rule = GroupRule(GroupType.MANDATORY, g1, listOf(f1, f2, f3))
        val renamed = rule.rename(r)
        assertThat(renamed.group).isEqualTo(boolFt("group1"))
        assertThat(renamed.content.map { it.featureCode }).containsExactlyInAnyOrder("f1", "feature2", "f3")
    }
}
