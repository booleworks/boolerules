package com.booleworks.boolerules.computations.generic

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
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
            FeatureDO.enum("a", "a2"),
            FeatureDO.enum("c", "c1")
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
            FeatureDO.boolean("a", true),
            FeatureDO.boolean("x", true)
        )
    }
}
