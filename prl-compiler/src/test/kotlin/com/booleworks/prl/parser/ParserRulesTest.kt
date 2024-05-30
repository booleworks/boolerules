package com.booleworks.prl.parser

import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.rules.GroupType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ParserRulesTest {

    private val ruleFile = parseRuleFile("../test-files/prl/parser/rules.prl")
    private var rules = ruleFile.ruleSet.rules

    @Test
    fun testGeneral() {
        assertThat(rules.size).isEqualTo(28)
        assertThat(ruleFile.fileName).isEqualTo("rules.prl")
    }

    @Test
    fun testConstraintRule1() {
        val rule: PrlRule = rules[0]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        assertThat(rule.lineNumber).isEqualTo(13)
    }

    @Test
    fun testConstraintRule2() {
        val rule: PrlRule = rules[1]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        val constraint: PrlConstraint = constraintRule.constraint
        assertThat((constraint as PrlAnd).operands).containsExactly(
            PrlFeature("hitch2"),
            PrlOr(PrlFeature("rim1"), PrlFeature("rim3")),
            PrlComparisonPredicate(ComparisonOperator.EQ, PrlFeature("parking_sensors"), PrlIntValue(3))
        )
        assertThat(rule.lineNumber).isEqualTo(14)
    }

    @Test
    fun testInclusionRule() {
        val rule: PrlRule = rules[2]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlInclusionRule).isTrue
        val inclusionRule: PrlInclusionRule = rule as PrlInclusionRule
        assertThat(inclusionRule.ifPart).isEqualTo(PrlFeature("hitch2"))
        assertThat((inclusionRule.thenPart as PrlAnd).operands).containsExactly(
            PrlOr(PrlFeature("rim1"), PrlFeature("rim3")),
            PrlFeature("parking_sensors")
        )
        assertThat(rule.lineNumber).isEqualTo(15)
    }

    @Test
    fun testExclusionRule() {
        val rule: PrlRule = rules[3]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlExclusionRule).isTrue
        val exclusionRule: PrlExclusionRule = rule as PrlExclusionRule
        assertThat(exclusionRule.ifPart).isEqualTo(PrlFeature("hitch2"))
        assertThat((exclusionRule.thenNotPart as PrlAnd).operands).containsExactly(
            PrlOr(PrlFeature("rim1"), PrlFeature("rim3")),
            PrlComparisonPredicate(ComparisonOperator.GE, PrlFeature("parking_sensors"), PrlIntValue(2))
        )
        assertThat(rule.lineNumber).isEqualTo(16)
    }

    @Test
    fun testIfThenElseRule() {
        val rule: PrlRule = rules[4]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlIfThenElseRule).isTrue
        val ifThenElseRule: PrlIfThenElseRule = rule as PrlIfThenElseRule
        assertThat(ifThenElseRule.ifPart).isEqualTo(PrlFeature("hitch2"))
        assertThat((ifThenElseRule.thenPart as PrlOr).operands).containsExactly(PrlFeature("rim1"), PrlFeature("rim3"))
        assertThat(ifThenElseRule.elsePart).isEqualTo(PrlFeature("parking_sensors"))
        assertThat(rule.lineNumber).isEqualTo(17)
    }

    @Test
    fun testDefinitionRule() {
        val rule: PrlRule = rules[5]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlDefinitionRule).isTrue
        val definitionRule: PrlDefinitionRule = rule as PrlDefinitionRule
        assertThat(definitionRule.feature).isEqualTo(PrlFeature("hitch2"))
        assertThat((definitionRule.definition as PrlAnd).operands).containsExactly(
            PrlOr(PrlFeature("rim1"), PrlFeature("rim3")),
            PrlFeature("parking_sensors")
        )
        assertThat(rule.lineNumber).isEqualTo(18)
    }

    @Test
    fun testGroupRule1() {
        val rule: PrlRule = rules[6]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlGroupRule).isTrue
        val groupRule: PrlGroupRule = rule as PrlGroupRule
        assertThat(groupRule.type).isEqualTo(GroupType.OPTIONAL)
        assertThat(groupRule.group).isEqualTo(PrlFeature("hitches"))
        assertThat(groupRule.content).containsExactly(
            PrlFeature("h1"),
            PrlFeature("h2"),
            PrlFeature("h3"),
            PrlFeature("h4")
        )
        assertThat(rule.lineNumber).isEqualTo(20)
    }

    @Test
    fun testGroupRule2() {
        val rule: PrlRule = rules[7]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlGroupRule).isTrue
        val groupRule: PrlGroupRule = rule as PrlGroupRule
        assertThat(groupRule.type).isEqualTo(GroupType.MANDATORY)
        assertThat(groupRule.group).isEqualTo(PrlFeature("hitches"))
        assertThat(groupRule.content).containsExactly(
            PrlFeature("h1"),
            PrlFeature("h2"),
            PrlFeature("h3"),
            PrlFeature("h4")
        )
        assertThat(rule.lineNumber).isEqualTo(21)
    }

    @Test
    fun testConstraintRule3() {
        val rule: PrlRule = rules[8]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraint: PrlConstraint = (rule as PrlConstraintRule).constraint
        assertThat((constraint as PrlImplication).left).isEqualTo(
            PrlComparisonPredicate(
                ComparisonOperator.EQ,
                PrlFeature("name"),
                PrlEnumValue("text")
            )
        )
        assertThat(constraint.right).isEqualTo(
            PrlComparisonPredicate(
                ComparisonOperator.GT,
                PrlFeature("version"),
                PrlIntValue(3)
            )
        )
        assertThat(rule.lineNumber).isEqualTo(23)
    }

    @Test
    fun testConstraintRule4() {
        val rule: PrlRule = rules[9]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraint: PrlConstraint = (rule as PrlConstraintRule).constraint
        assertThat(constraint).isEqualTo(
            PrlComparisonPredicate(ComparisonOperator.NE, PrlFeature("name"), PrlEnumValue("not text"))
        )
        assertThat(rule.lineNumber).isEqualTo(24)
    }

    @Test
    fun testConstraintRule5() {
        val rule: PrlRule = rules[10]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraint: PrlConstraint = (rule as PrlConstraintRule).constraint
        assertThat(constraint).isEqualTo(
            PrlComparisonPredicate(ComparisonOperator.GT, PrlFeature("version_1"), PrlFeature("version_2"))
        )
        assertThat(rule.lineNumber).isEqualTo(25)
    }

    @Test
    fun testConstraintRule6() {
        val rule: PrlRule = rules[11]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraint: PrlConstraint = (rule as PrlConstraintRule).constraint
        assertThat(constraint).isEqualTo(
            PrlInEnumsPredicate(PrlFeature("name"), "a", "b", "c")
        )
        assertThat(rule.lineNumber).isEqualTo(26)
    }

    @Test
    fun testConstraintRule7() {
        val rule: PrlRule = rules[12]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraint: PrlConstraint = (rule as PrlConstraintRule).constraint
        assertThat(constraint).isEqualTo(
            PrlInIntRangePredicate(PrlFeature("version"), IntRange.interval(1, 7))
        )
        assertThat(rule.lineNumber).isEqualTo(27)
    }

    @Test
    fun testConstraintRule8() {
        val rule: PrlRule = rules[13]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraint: PrlConstraint = (rule as PrlConstraintRule).constraint
        assertThat(constraint).isEqualTo(
            PrlInIntRangePredicate(PrlFeature("version"), IntRange.list(1, 2, 3, 4, 5))
        )
        assertThat(rule.lineNumber).isEqualTo(28)
    }

    @Test
    fun testRuleWithProperties() {
        val rule: PrlRule = rules[14]
        assertThat(rule.description).isEqualTo("text description")
        assertThat(rule.id).isEqualTo("4711")
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        val implConstraint: PrlEquivalence = constraintRule.constraint as PrlEquivalence
        assertThat(rule.properties).containsExactly(
            PrlIntProperty("version", 4, 33),
            PrlDateProperty("validFrom", LocalDate.of(2010, 1, 1), 34),
            PrlDateProperty("validTo", LocalDate.of(2022, 12, 31), 35),
            PrlEnumProperty("text", "text text text", 37),
            PrlEnumProperty("releases", EnumRange.list("R1", "R2", "R3"), 38),
            PrlBooleanProperty("active", BooleanRange.list(false), 39),
            PrlBooleanProperty("cool", BooleanRange.list(true), 40)
        )
        assertThat(rule.lineNumber).isEqualTo(30)
        assertThat(implConstraint.left).isEqualTo(PrlFeature("a"))
        assertThat(implConstraint.right).isEqualTo(
            PrlImplication(
                PrlFeature("b"),
                PrlNot(PrlOr(PrlFeature("c"), PrlFeature("d")))
            )
        )
    }

    @Test
    fun testConstraintRule9() {
        val rule: PrlRule = rules[15]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        assertThat(constraintRule.constraint).isEqualTo(PrlAmo(PrlFeature("b1"), PrlFeature("b2"), PrlFeature("b3")))
        assertThat(rule.lineNumber).isEqualTo(43)
    }

    @Test
    fun testConstraintRule10() {
        val rule: PrlRule = rules[16]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        assertThat(constraintRule.constraint).isEqualTo(PrlExo(PrlFeature("c1")))
        assertThat(rule.lineNumber).isEqualTo(44)
    }

    @Test
    fun testConstraintRule11() {
        val rule: PrlRule = rules[17]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        assertThat(constraintRule.constraint).isEqualTo(PrlAmo(listOf()))
        assertThat(rule.lineNumber).isEqualTo(45)
    }

    @Test
    fun testIntNeq() {
        val rule: PrlRule = rules[18]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        assertThat(constraintRule.constraint).isEqualTo(
            PrlComparisonPredicate(
                ComparisonOperator.NE,
                PrlFeature("version_1"),
                PrlFeature("version_2")
            )
        )
        assertThat(rule.lineNumber).isEqualTo(47)
    }

    @Test
    fun testIntEq() {
        val rule: PrlRule = rules[19]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlConstraintRule).isTrue
        val constraintRule: PrlConstraintRule = rule as PrlConstraintRule
        assertThat(constraintRule.constraint).isEqualTo(
            PrlComparisonPredicate(
                ComparisonOperator.EQ,
                PrlFeature("version_1"),
                PrlFeature("version_2")
            )
        )
        assertThat(rule.lineNumber).isEqualTo(48)
    }

    @Test
    fun testFeatureRule1() {
        val rule: PrlRule = rules[20]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlForbiddenFeatureRule).isTrue
        val constraintRule: PrlForbiddenFeatureRule = rule as PrlForbiddenFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("bf1"))
        assertThat(constraintRule.enumValue).isNull()
        assertThat(constraintRule.intValueOrVersion).isNull()
        assertThat(rule.lineNumber).isEqualTo(50)
    }

    @Test
    fun testFeatureRule2() {
        val rule: PrlRule = rules[21]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlForbiddenFeatureRule).isTrue
        val constraintRule: PrlForbiddenFeatureRule = rule as PrlForbiddenFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("v"))
        assertThat(constraintRule.enumValue).isNull()
        assertThat(constraintRule.intValueOrVersion).isEqualTo(2)
        assertThat(rule.lineNumber).isEqualTo(51)
    }

    @Test
    fun testFeatureRule3() {
        val rule: PrlRule = rules[22]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlForbiddenFeatureRule).isTrue
        val constraintRule: PrlForbiddenFeatureRule = rule as PrlForbiddenFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("ii"))
        assertThat(constraintRule.enumValue).isNull()
        assertThat(constraintRule.intValueOrVersion).isEqualTo(2)
        assertThat(rule.lineNumber).isEqualTo(52)
    }

    @Test
    fun testFeatureRule4() {
        val rule: PrlRule = rules[23]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlForbiddenFeatureRule).isTrue
        val constraintRule: PrlForbiddenFeatureRule = rule as PrlForbiddenFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("st"))
        assertThat(constraintRule.enumValue).isEqualTo("te xt")
        assertThat(constraintRule.intValueOrVersion).isNull()
        assertThat(rule.lineNumber).isEqualTo(53)
    }

    @Test
    fun testFeatureRule5() {
        val rule: PrlRule = rules[24]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlMandatoryFeatureRule).isTrue
        val constraintRule: PrlMandatoryFeatureRule = rule as PrlMandatoryFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("bf1"))
        assertThat(constraintRule.enumValue).isNull()
        assertThat(constraintRule.intValueOrVersion).isNull()
        assertThat(rule.lineNumber).isEqualTo(55)
    }

    @Test
    fun testFeatureRule6() {
        val rule: PrlRule = rules[25]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlMandatoryFeatureRule).isTrue
        val constraintRule: PrlMandatoryFeatureRule = rule as PrlMandatoryFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("v"))
        assertThat(constraintRule.enumValue).isNull()
        assertThat(constraintRule.intValueOrVersion).isEqualTo(2)
        assertThat(rule.lineNumber).isEqualTo(56)
    }

    @Test
    fun testFeatureRule7() {
        val rule: PrlRule = rules[26]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).isEmpty()
        assertThat(rule is PrlMandatoryFeatureRule).isTrue
        val constraintRule: PrlMandatoryFeatureRule = rule as PrlMandatoryFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("ii"))
        assertThat(constraintRule.enumValue).isNull()
        assertThat(constraintRule.intValueOrVersion).isEqualTo(2)
        assertThat(rule.lineNumber).isEqualTo(57)
    }

    @Test
    fun testFeatureRule8() {
        val rule: PrlRule = rules[27]
        assertThat(rule.description).isEmpty()
        assertThat(rule.id).isEmpty()
        assertThat(rule.properties).containsExactly(PrlIntProperty("range", IntRange.list(1), 59))
        assertThat(rule is PrlMandatoryFeatureRule).isTrue
        val constraintRule: PrlMandatoryFeatureRule = rule as PrlMandatoryFeatureRule
        assertThat(constraintRule.feature).isEqualTo(PrlFeature("st"))
        assertThat(constraintRule.enumValue).isEqualTo("te xt")
        assertThat(constraintRule.intValueOrVersion).isNull()
        assertThat(rule.lineNumber).isEqualTo(58)
    }
}
