package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogicNGTranspilerBooleanNoSlicesTest {
    private val model = PrlCompiler().compile(
        parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl")
    )
    private val f = FormulaFactory.caching()
    private val cf = CspFactory(f)
    private val modelTranslation = transpileModel(cf, model, listOf())

    @Test
    fun testModel() {
        assertThat(model.rules).hasSize(339)
    }

    @Test
    fun testModelTranslation() {
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
        assertThat(modelTranslation[0].propositions).hasSize(model.rules.size)
    }
}
