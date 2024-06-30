package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.algorithms.OptimizationType
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MinMaxConfigurationOnlyBooleanNoSlicesTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    @Test
    fun testComputeSlice() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info

        val algoMin = MinMaxConfig(listOf(), OptimizationType.MIN)
        val algoMax = MinMaxConfig(listOf(), OptimizationType.MAX)
        val handler = BRTimeoutHandler.fromDuration(60)

        val resultMin = algoMin.executeForSlice(cf, model, info, Slice.empty(), handler)
        val resultMax = algoMax.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(resultMin.size).isEqualTo(9)
        assertThat(resultMin.featureModel!!.size).isEqualTo(9)
        assertThat(resultMax.size).isEqualTo(197)
        assertThat(resultMax.featureModel!!.size).isEqualTo(197)
    }
}
