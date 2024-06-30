package com.booleworks.boolerules.algorithms.insights

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigInteger

internal class ModelCountIntTest {
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge4.prl"))

    @Test
    fun testComputeForSlice() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info

        val algo = ModelCount
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)
        val result5 = algo.executeForSlice(cf, model, info5, Slice.empty(), handler)
        val result6 = algo.executeForSlice(cf, model, info6, Slice.empty(), handler)

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.count).isEqualTo(BigInteger.valueOf(19))
        Assertions.assertThat(result2.count).isEqualTo(BigInteger.valueOf(1))
        Assertions.assertThat(result3.count).isEqualTo(BigInteger.valueOf(22))
        Assertions.assertThat(result4.count).isEqualTo(BigInteger.valueOf(6))
        Assertions.assertThat(result5.count).isEqualTo(BigInteger.valueOf(1))
        Assertions.assertThat(result6.count).isEqualTo(BigInteger.valueOf(14))
    }

    @Test
    fun testComputeForSliceWithAdditionalConstraints() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val additionalConstraint = listOf("[a = 5]")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraint)
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info

        val algo = ModelCount
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)
        val result5 = algo.executeForSlice(cf, model, info5, Slice.empty(), handler)
        val result6 = algo.executeForSlice(cf, model, info6, Slice.empty(), handler)

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.count).isEqualTo(BigInteger.valueOf(5))
        Assertions.assertThat(result2.count).isEqualTo(BigInteger.valueOf(1))
        Assertions.assertThat(result3.count).isEqualTo(BigInteger.valueOf(3))
        Assertions.assertThat(result4.count).isEqualTo(BigInteger.valueOf(3))
        Assertions.assertThat(result5.count).isEqualTo(BigInteger.valueOf(0))
        Assertions.assertThat(result6.count).isEqualTo(BigInteger.valueOf(2))
    }
}
