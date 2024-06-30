package com.booleworks.boolerules.algorithms.insights

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

internal class ModelCountTest {

    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testComputeForSlice() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info

        val algo = ModelCount
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.count).isEqualTo(BigInteger.valueOf(2))
        assertThat(result2.count).isEqualTo(BigInteger.valueOf(4))
        assertThat(result3.count).isEqualTo(BigInteger.valueOf(3))
        assertThat(result4.count).isEqualTo(BigInteger.valueOf(6))
    }

    @Test
    fun testComputeForSliceWithAdditionalConstraints() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val additionalConstraint = listOf("[c = \"c1\"]")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraint)
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info

        val algo = ModelCount
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.count).isEqualTo(BigInteger.valueOf(1))
        assertThat(result2.count).isEqualTo(BigInteger.valueOf(2))
        assertThat(result3.count).isEqualTo(BigInteger.valueOf(1))
        assertThat(result4.count).isEqualTo(BigInteger.valueOf(2))
    }
}
