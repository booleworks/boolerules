package com.booleworks.boolerules.computations.modelcount

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

internal class ModelCountTest : TestWithConfig() {

    private val cut = ModelCountComputation
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testComputeForSlice() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())

        val request = ModelCountRequest("any", mutableListOf(), listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.count).isEqualTo(BigInteger.valueOf(2))
        assertThat(result2.count).isEqualTo(BigInteger.valueOf(4))
        assertThat(result3.count).isEqualTo(BigInteger.valueOf(3))
        assertThat(result4.count).isEqualTo(BigInteger.valueOf(6))
    }

    @Test
    fun testComputeForSliceWithAdditionalConstraints() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val additionalConstraint = listOf("[c = \"c1\"]")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraint)

        val request = ModelCountRequest("any", mutableListOf(), additionalConstraint)
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.count).isEqualTo(BigInteger.valueOf(1))
        assertThat(result2.count).isEqualTo(BigInteger.valueOf(2))
        assertThat(result3.count).isEqualTo(BigInteger.valueOf(1))
        assertThat(result4.count).isEqualTo(BigInteger.valueOf(2))
    }
}
