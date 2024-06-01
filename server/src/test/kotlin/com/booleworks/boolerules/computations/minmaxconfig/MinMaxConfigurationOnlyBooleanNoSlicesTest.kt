package com.booleworks.boolerules.computations.minmaxconfig

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MinMaxConfigurationOnlyBooleanNoSlicesTest : TestWithConfig() {

    private val cut = MinMaxConfigComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    @Test
    fun testComputeResponse() {
        val requestMin = MinMaxConfigRequest("any", mutableListOf(), listOf(), OptimizationType.MIN, listOf())
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val responseMin = cut.computeResponse(requestMin, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(responseMin.merge).hasSize(1)
        assertThat(responseMin.merge[0].result).isEqualTo(9)
        assertThat(responseMin.merge[0].slices).hasSize(1)

        assertThat(responseMin.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(responseMin.detailMap).hasSize(1)
        assertThat(responseMin.detailMap[1]).hasSize(1)

        assertThat(responseMin.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(responseMin.detailMap[1]!![0].detail.exampleConfiguration!!.features.size).isEqualTo(9)
    }
}
