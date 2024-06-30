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

internal class OptimizationOnlyBooleanNoSlicesTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))
    private val weightings = listOf(
        WeightPair("qxz", 1),
        WeightPair("bci", 2),
        WeightPair("wnk", 3),
        WeightPair("jiq", 4),
        WeightPair("zke", 5),
        WeightPair("dql", 6),
        WeightPair("cce", 7),
        WeightPair("pis", 8),
        WeightPair("dca", 9),
        WeightPair("mje", 10),
        WeightPair("xjg", 11),
        WeightPair("uri", 12),
        WeightPair("ukp", 13),
        WeightPair("fvy", 14),
        WeightPair("ehj", 15),
        WeightPair("xqa", 16),
        WeightPair("cpx", 17),
        WeightPair("aji", -1),
        WeightPair("lsx", -2),
        WeightPair("wmw", -3),
    )

    @Test
    fun testComputeResponse() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info

        val algoMin = Optimization(weightings, OptimizationType.MIN)
        val algoMax = Optimization(weightings, OptimizationType.MAX)
        val handler = BRTimeoutHandler.fromDuration(60)
        val resultMin = algoMin.executeForSlice(cf, model, info, Slice.empty(), handler)
        val resultMax = algoMax.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(resultMin.weight).isEqualTo(-6)
        assertThat(resultMin.featureModel!!.size).isEqualTo(13)

        assertThat(resultMax.weight).isEqualTo(134)
        assertThat(resultMax.featureModel!!.size).isEqualTo(38)
    }
}
