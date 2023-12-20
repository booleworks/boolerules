package com.booleworks.boolerules.computations.optimization

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OptimizationOnlyBooleanNoSlicesTest : TestWithConfig() {

    private val cut = OptimizationComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/real/automotive/automotive_simple_1.prl"))
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
        val requestMin = OptimizationRequest("any", mutableListOf(), listOf(), OptimizationType.MIN, weightings)
        val sbMin = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val responseMin = cut.computeResponse(requestMin, model, sbMin)
        val statusMin = sbMin.build()

        val requestMax = OptimizationRequest("any", mutableListOf(), listOf(), OptimizationType.MAX, weightings)
        val sbMax = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val responseMax = cut.computeResponse(requestMax, model, sbMax)

        assertThat(statusMin.success).isTrue
        assertThat(statusMin.statistics.numberOfSlices).isEqualTo(1)
        assertThat(statusMin.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(statusMin.errors).isEmpty()
        assertThat(statusMin.warnings).isEmpty()
        assertThat(statusMin.infos).isEmpty()

        assertThat(responseMin.merge).hasSize(1)
        assertThat(responseMin.merge[0].result).isEqualTo(-6)
        assertThat(responseMin.merge[0].slices).hasSize(1)

        assertThat(responseMin.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(responseMin.detailMap).hasSize(1)
        assertThat(responseMin.detailMap[1]).hasSize(1)

        assertThat(responseMin.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(responseMin.detailMap[1]!![0].detail.exampleConfiguration!!.features.size).isEqualTo(12)

        assertThat(responseMax.merge).hasSize(1)
        assertThat(responseMax.merge[0].result).isEqualTo(134)
        assertThat(responseMax.merge[0].slices).hasSize(1)

        assertThat(responseMax.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(responseMax.detailMap).hasSize(1)
        assertThat(responseMax.detailMap[1]).hasSize(1)

        assertThat(responseMax.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(responseMax.detailMap[1]!![0].detail.exampleConfiguration!!.features.size).isEqualTo(38)
    }
}
