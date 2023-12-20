package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ConsistencyComputationOnlyBooleanNoSlicesTest : TestWithConfig() {

    private val cut = ConsistencyComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/real/automotive/automotive_simple_1.prl"))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(339)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithExplanation(tc: TestConfig) {
        setUp(tc)
        val request = ConsistencyRequest("any", mutableListOf(), listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult = result[Slice.empty()]!!

        assertThat(sliceResult.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult.consistent).isTrue()
        assertThat(sliceResult.explanation).isNull()
        assertThat(sliceResult.example!!.features).isNotEmpty
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponse(tc: TestConfig) {
        setUp(tc)
        val request = ConsistencyRequest("any", mutableListOf(), listOf())
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val respone = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(respone.merge).hasSize(1)
        assertThat(respone.merge[0].result).isTrue()
        assertThat(respone.merge[0].slices).hasSize(1)

        assertThat(respone.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(respone.detailMap).hasSize(1)
        assertThat(respone.detailMap[1]).hasSize(1)
        assertThat(respone.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(respone.detailMap[1]!![0].detail.exampleConfiguration!!.features).isNotEmpty
        assertThat(respone.detailMap[1]!![0].detail.explanation).isNull()
    }
}
