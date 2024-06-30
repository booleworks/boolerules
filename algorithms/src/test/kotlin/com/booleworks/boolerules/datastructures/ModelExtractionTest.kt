package com.booleworks.boolerules.datastructures

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ModelExtractionTest {

    @Test
    fun testEnumExtraction() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))
        val f = FormulaFactory.caching();
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info
        val extracted = extractModel(f.variables("@ENUM_a_a2", "@ENUM_c_c1"), info)

        assertThat(extracted.features).containsExactlyInAnyOrder(
            FeatureInstance(enumFt("a"), enumValue = "a2"),
            FeatureInstance(enumFt("c"), enumValue = "c1")
        )
    }

    @Test
    fun testBooleanExtraction() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge1.prl"))
        val f = FormulaFactory.caching();
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info
        val extracted = extractModel(f.variables("a", "x"), info)

        assertThat(extracted.features).containsExactlyInAnyOrder(
            FeatureInstance(boolFt("a")),
            FeatureInstance(boolFt("x"))
        )
    }
}
