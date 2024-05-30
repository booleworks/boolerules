package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.RuleType.ADDITIONAL_RESTRICTION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogicNGTranspilerEnumTest {
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))
    private val f = FormulaFactory.caching()
    private val cf = CspFactory(f)
    private val modelTranslation = transpileModel(cf, model, listOf())

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(5)
    }

    @Test
    fun testModelTranslation() {
        assertThat(modelTranslation.numberOfComputations).isEqualTo(4)
    }

    @Test
    fun testModelTranslationSlice1() {
        // version 1, series S1
        assertThat(modelTranslation[0].unknownFeatures).isEmpty()
        assertThat(modelTranslation[0].knownVariables).containsExactlyInAnyOrderElementsOf(
            f.variables(
                "@ENUM_a_a1", "@ENUM_a_a2",
                "@ENUM_b_b1", "@ENUM_b_b2", "@ENUM_b_b3",
                "@ENUM_c_c1", "@ENUM_c_c2",
                "@ENUM_p_px", "@ENUM_p_p1"
            )
        )
        assertThat(modelTranslation[0].enumMapping).hasSize(4)
        assertThat(modelTranslation[0].info.getFeatureAndValue(f.variable("@ENUM_a_a2"))).isEqualTo(Pair("a", "a2"))
        assertThat(modelTranslation[0].propositions).hasSize(7)

        for (i in 0..2) {
            assertThat(modelTranslation[0].propositions[i].backpack().ruleType).isEqualTo(RuleType.ORIGINAL_RULE)
        }
        assertThat(modelTranslation[0].propositions[0].backpack().rule).isEqualTo(model.rules[0])
        assertThat(modelTranslation[0].propositions[1].backpack().rule).isEqualTo(model.rules[1])
        assertThat(modelTranslation[0].propositions[2].backpack().rule).isEqualTo(model.rules[3])
        for (i in 3..6) {
            assertThat(modelTranslation[0].propositions[i].backpack().ruleType).isEqualTo(RuleType.ENUM_FEATURE_CONSTRAINT)
        }

        assertThat(modelTranslation[0].propositions[0].formula()).isEqualTo(f.parse("@ENUM_a_a2"))
        assertThat(modelTranslation[0].propositions[1].formula()).isEqualTo(f.parse("@ENUM_a_a2 => @ENUM_b_b1"))
        assertThat(modelTranslation[0].propositions[2].formula()).isEqualTo(f.parse("@ENUM_p_p1"))

        assertThat(modelTranslation[0].propositions[3].formula()).isEqualTo(f.parse("@ENUM_a_a1 + @ENUM_a_a2 = 1"))
        assertThat(modelTranslation[0].propositions[4].formula()).isEqualTo(f.parse("@ENUM_b_b1 + @ENUM_b_b2 + @ENUM_b_b3 = 1"))
        assertThat(modelTranslation[0].propositions[5].formula()).isEqualTo(f.parse("@ENUM_c_c1 + @ENUM_c_c2 = 1"))
        assertThat(modelTranslation[0].propositions[6].formula()).isEqualTo(f.parse("@ENUM_p_px + @ENUM_p_p1 = 1"))
    }

    @Test
    fun testModelTranslationSlice2() {
        // version 1, series S2
        assertThat(modelTranslation[1].unknownFeatures).isEmpty()
        assertThat(modelTranslation[1].knownVariables).containsExactlyInAnyOrderElementsOf(
            f.variables(
                "@ENUM_a_a1", "@ENUM_a_a2",
                "@ENUM_b_b1", "@ENUM_b_b2", "@ENUM_b_b3",
                "@ENUM_c_c1", "@ENUM_c_c2",
                "@ENUM_p_px", "@ENUM_p_p2",
                "@ENUM_q_q1", "@ENUM_q_q2"
            )
        )
        assertThat(modelTranslation[1].enumMapping).hasSize(5)
        assertThat(modelTranslation[1].info.getFeatureAndValue(f.variable("@ENUM_a_a2"))).isEqualTo(Pair("a", "a2"))
        assertThat(modelTranslation[1].propositions).hasSize(8)

        for (i in 0..2) {
            assertThat(modelTranslation[1].propositions[i].backpack().ruleType).isEqualTo(RuleType.ORIGINAL_RULE)
        }
        assertThat(modelTranslation[1].propositions[0].backpack().rule).isEqualTo(model.rules[0])
        assertThat(modelTranslation[1].propositions[1].backpack().rule).isEqualTo(model.rules[1])
        assertThat(modelTranslation[1].propositions[2].backpack().rule).isEqualTo(model.rules[3])
        for (i in 3..7) {
            assertThat(modelTranslation[1].propositions[i].backpack().ruleType).isEqualTo(RuleType.ENUM_FEATURE_CONSTRAINT)
        }

        assertThat(modelTranslation[1].propositions[0].formula()).isEqualTo(f.parse("@ENUM_a_a2"))
        assertThat(modelTranslation[1].propositions[1].formula()).isEqualTo(f.parse("@ENUM_a_a2 => @ENUM_b_b1"))
        assertThat(modelTranslation[1].propositions[2].formula()).isEqualTo(f.parse("@ENUM_p_p2"))

        assertThat(modelTranslation[1].propositions[3].formula()).isEqualTo(f.parse("@ENUM_a_a1 + @ENUM_a_a2 = 1"))
        assertThat(modelTranslation[1].propositions[4].formula()).isEqualTo(f.parse("@ENUM_b_b1 + @ENUM_b_b2 + @ENUM_b_b3 = 1"))
        assertThat(modelTranslation[1].propositions[5].formula()).isEqualTo(f.parse("@ENUM_c_c1 + @ENUM_c_c2 = 1"))
        assertThat(modelTranslation[1].propositions[6].formula()).isEqualTo(f.parse("@ENUM_p_px + @ENUM_p_p2 = 1"))
        assertThat(modelTranslation[1].propositions[7].formula()).isEqualTo(f.parse("@ENUM_q_q1 + @ENUM_q_q2 = 1"))
    }

    @Test
    fun testModelTranslationSlice3() {
        // version 2, series S1
        assertThat(modelTranslation[2].unknownFeatures).containsExactly(model.getFeature("q"))
        assertThat(modelTranslation[2].knownVariables).containsExactlyInAnyOrderElementsOf(
            f.variables(
                "@ENUM_a_a1", "@ENUM_a_a2",
                "@ENUM_b_b1", "@ENUM_b_b2", "@ENUM_b_b3",
                "@ENUM_c_c1", "@ENUM_c_c2", "@ENUM_c_c3",
                "@ENUM_p_px", "@ENUM_p_p1"
            )
        )
        assertThat(modelTranslation[2].enumMapping).hasSize(4)
        assertThat(modelTranslation[2].info.getFeatureAndValue(f.variable("@ENUM_a_a2"))).isEqualTo(Pair("a", "a2"))
        assertThat(modelTranslation[2].propositions).hasSize(8)

        for (i in 0..3) {
            assertThat(modelTranslation[2].propositions[i].backpack().ruleType).isEqualTo(RuleType.ORIGINAL_RULE)
        }
        assertThat(modelTranslation[2].propositions[0].backpack().rule).isEqualTo(model.rules[0])
        assertThat(modelTranslation[2].propositions[1].backpack().rule).isEqualTo(model.rules[2])
        assertThat(modelTranslation[2].propositions[2].backpack().rule).isEqualTo(model.rules[3])
        assertThat(modelTranslation[2].propositions[3].backpack().rule).isEqualTo(model.rules[4])
        for (i in 4..7) {
            assertThat(modelTranslation[2].propositions[i].backpack().ruleType).isEqualTo(RuleType.ENUM_FEATURE_CONSTRAINT)
        }

        assertThat(modelTranslation[2].propositions[0].formula()).isEqualTo(f.parse("@ENUM_a_a2"))
        assertThat(modelTranslation[2].propositions[1].formula()).isEqualTo(f.parse("@ENUM_a_a2 => @ENUM_b_b2"))
        assertThat(modelTranslation[2].propositions[2].formula()).isEqualTo(f.parse("@ENUM_p_p1"))
        assertThat(modelTranslation[2].propositions[3].formula()).isEqualTo(f.verum())

        assertThat(modelTranslation[2].propositions[4].formula()).isEqualTo(f.parse("@ENUM_a_a1 + @ENUM_a_a2 = 1"))
        assertThat(modelTranslation[2].propositions[5].formula()).isEqualTo(f.parse("@ENUM_b_b1 + @ENUM_b_b2 + @ENUM_b_b3 = 1"))
        assertThat(modelTranslation[2].propositions[6].formula()).isEqualTo(f.parse("@ENUM_c_c1 + @ENUM_c_c2 + @ENUM_c_c3 = 1"))
        assertThat(modelTranslation[2].propositions[7].formula()).isEqualTo(f.parse("@ENUM_p_px + @ENUM_p_p1 = 1"))
    }

    @Test
    fun testModelTranslationSlice4() {
        // version 2, series S2
        assertThat(modelTranslation[3].unknownFeatures).isEmpty()
        assertThat(modelTranslation[3].knownVariables).containsExactlyInAnyOrderElementsOf(
            f.variables(
                "@ENUM_a_a1", "@ENUM_a_a2",
                "@ENUM_b_b1", "@ENUM_b_b2", "@ENUM_b_b3",
                "@ENUM_c_c1", "@ENUM_c_c2", "@ENUM_c_c3",
                "@ENUM_p_px", "@ENUM_p_p2",
                "@ENUM_q_q1", "@ENUM_q_q2"
            )
        )
        assertThat(modelTranslation[3].enumMapping).hasSize(5)
        assertThat(modelTranslation[3].info.getFeatureAndValue(f.variable("@ENUM_a_a2"))).isEqualTo(Pair("a", "a2"))
        assertThat(modelTranslation[3].propositions).hasSize(9)

        for (i in 0..3) {
            assertThat(modelTranslation[3].propositions[i].backpack().ruleType).isEqualTo(RuleType.ORIGINAL_RULE)
        }
        assertThat(modelTranslation[3].propositions[0].backpack().rule).isEqualTo(model.rules[0])
        assertThat(modelTranslation[3].propositions[1].backpack().rule).isEqualTo(model.rules[2])
        assertThat(modelTranslation[3].propositions[2].backpack().rule).isEqualTo(model.rules[3])
        assertThat(modelTranslation[3].propositions[3].backpack().rule).isEqualTo(model.rules[4])
        for (i in 4..8) {
            assertThat(modelTranslation[3].propositions[i].backpack().ruleType).isEqualTo(RuleType.ENUM_FEATURE_CONSTRAINT)
        }

        assertThat(modelTranslation[3].propositions[0].formula()).isEqualTo(f.parse("@ENUM_a_a2"))
        assertThat(modelTranslation[3].propositions[1].formula()).isEqualTo(f.parse("@ENUM_a_a2 => @ENUM_b_b2"))
        assertThat(modelTranslation[3].propositions[2].formula()).isEqualTo(f.parse("@ENUM_p_p2"))
        assertThat(modelTranslation[3].propositions[3].formula()).isEqualTo(f.parse("@ENUM_q_q1 | @ENUM_q_q2 => @ENUM_p_p2"))

        assertThat(modelTranslation[3].propositions[4].formula()).isEqualTo(f.parse("@ENUM_a_a1 + @ENUM_a_a2 = 1"))
        assertThat(modelTranslation[3].propositions[5].formula()).isEqualTo(f.parse("@ENUM_b_b1 + @ENUM_b_b2 + @ENUM_b_b3 = 1"))
        assertThat(modelTranslation[3].propositions[6].formula()).isEqualTo(f.parse("@ENUM_c_c1 + @ENUM_c_c2 + @ENUM_c_c3 = 1"))
        assertThat(modelTranslation[3].propositions[7].formula()).isEqualTo(f.parse("@ENUM_p_px + @ENUM_p_p2 = 1"))
        assertThat(modelTranslation[3].propositions[8].formula()).isEqualTo(f.parse("@ENUM_q_q1 + @ENUM_q_q2 = 1"))
    }

    @Test
    fun testAdditionalConstraints() {
        val mod = transpileModel(
            cf, model, listOf(),
            additionalConstraints = listOf("[b = \"b2\"]", "x", "[w = \"w1\"]")
        )
        assertThat(mod.skippedConstraints).containsExactly("x", "[w = \"w1\"]")

        assertThat(mod[0].propositions).hasSize(8)
        assertThat(mod[1].propositions).hasSize(9)
        assertThat(mod[2].propositions).hasSize(9)
        assertThat(mod[3].propositions).hasSize(10)
        assertThat(mod[0].propositions.filter { it.backpack().ruleType == ADDITIONAL_RESTRICTION }).hasSize(1)
        assertThat(mod[1].propositions.filter { it.backpack().ruleType == ADDITIONAL_RESTRICTION }).hasSize(1)
        assertThat(mod[2].propositions.filter { it.backpack().ruleType == ADDITIONAL_RESTRICTION }).hasSize(1)
        assertThat(mod[3].propositions.filter { it.backpack().ruleType == ADDITIONAL_RESTRICTION }).hasSize(1)
    }
}
