package com.booleworks.boolerules.computations.modelcount

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import java.math.BigInteger
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class ModelCountIntTest : TestWithConfig() {
    private val cut = ModelCountComputation
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge4.prl"))

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
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result5 = cut.computeForSlice(
            request,
            Slice.empty(),
            info5,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result6 = cut.computeForSlice(
            request,
            Slice.empty(),
            info6,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )

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

        val request = ModelCountRequest("any", mutableListOf(), additionalConstraint)
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result5 = cut.computeForSlice(
            request,
            Slice.empty(),
            info5,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )
        val result6 = cut.computeForSlice(
            request,
            Slice.empty(),
            info6,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        )

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.count).isEqualTo(BigInteger.valueOf(5))
        Assertions.assertThat(result2.count).isEqualTo(BigInteger.valueOf(1))
        Assertions.assertThat(result3.count).isEqualTo(BigInteger.valueOf(3))
        Assertions.assertThat(result4.count).isEqualTo(BigInteger.valueOf(3))
        Assertions.assertThat(result5.count).isEqualTo(BigInteger.valueOf(0))
        Assertions.assertThat(result6.count).isEqualTo(BigInteger.valueOf(2))
    }
}
