package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogicNGTranspilerEnumNoSlicesTest {
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/compiler/enum_no_slices.prl"))
    private val f = FormulaFactory.caching()
    private val cf = CspFactory(f)
    private val modelTranslation = transpileModel(cf, model, listOf())

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(4)
    }

    @Test
    fun testModelTranslation() {
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
        assertThat(modelTranslation[0].propositions).hasSize(9)

        for (i in 0..3) {
            assertThat(modelTranslation[0].propositions[i].backpack().ruleType).isEqualTo(RuleType.ORIGINAL_RULE)
            assertThat(modelTranslation[0].propositions[i].backpack().rule).isEqualTo(model.rules[i])
        }
        for (i in 4..8) {
            assertThat(modelTranslation[0].propositions[i].backpack().ruleType).isEqualTo(RuleType.ENUM_FEATURE_CONSTRAINT)
        }

        assertThat(modelTranslation[0].propositions[0].formula()).isEqualTo(f.parse("@ENUM_a_a2"))
        assertThat(modelTranslation[0].propositions[1].formula()).isEqualTo(f.parse("@ENUM_a_a2 => @ENUM_b_b1"))
        assertThat(modelTranslation[0].propositions[2].formula()).isEqualTo(f.parse("@ENUM_p_p1 | @ENUM_p_p2"))
        assertThat(modelTranslation[0].propositions[3].formula()).isEqualTo(f.parse("@ENUM_q_r_q1 => @ENUM_p_p1"))

        assertThat(modelTranslation[0].propositions[4].formula()).isEqualTo(f.parse("@ENUM_a_a1 + @ENUM_a_a2 = 1"))
        assertThat(modelTranslation[0].propositions[5].formula()).isEqualTo(f.parse("@ENUM_b_b1 + @ENUM_b_b2 + @ENUM_b_b3 = 1"))
        assertThat(modelTranslation[0].propositions[6].formula()).isEqualTo(f.parse("@ENUM_c_c1 + @ENUM_c_c2 = 1"))
        assertThat(modelTranslation[0].propositions[7].formula()).isEqualTo(f.parse("@ENUM_p_p1 + @ENUM_p_p2 + @ENUM_p_p3 = 1"))
        assertThat(modelTranslation[0].propositions[8].formula()).isEqualTo(f.parse("@ENUM_q_r_q1 + @ENUM_q_r_q2 = 1"))

        assertThat(modelTranslation[0].unknownFeatures).isEmpty()
        assertThat(modelTranslation[0].knownVariables).containsExactlyInAnyOrderElementsOf(
            f.variables(
                "@ENUM_a_a1", "@ENUM_a_a2",
                "@ENUM_b_b1", "@ENUM_b_b2", "@ENUM_b_b3",
                "@ENUM_c_c1", "@ENUM_c_c2",
                "@ENUM_p_p1", "@ENUM_p_p2", "@ENUM_p_p3",
                "@ENUM_q_r_q1", "@ENUM_q_r_q2",
            )
        )
        assertThat(modelTranslation[0].enumMapping).hasSize(5)
        assertThat(modelTranslation[0].info.getFeatureAndValue(f.variable("@ENUM_p_p3"))).isEqualTo(Pair("p", "p3"))
    }
}
