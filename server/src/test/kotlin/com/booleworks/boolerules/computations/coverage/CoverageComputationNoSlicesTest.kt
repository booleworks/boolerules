package com.booleworks.boolerules.computations.coverage

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class CoverageComputationNoSlicesTest : TestWithConfig() {

    private val cut = CoverageComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithFeatures(tc: TestConfig) {
        setUp(tc)
        val buildable = listOf(
            "qzb",
            "zhd",
            "pow",
            "lmp",
            "xvo",
            "wyl",
            "zqm",
            "atu",
            "vpf",
            "vti",
            "suz",
            "gao",
            "csq",
            "emc",
            "eop",
            "jlt",
            "dxg",
            "xpx",
            "bhy",
            "fdj"
        )
        val notBuildable = listOf("oda", "aai", "zgg", "dez")

        val request = CoverageRequest("any", mutableListOf(), listOf(), buildable + notBuildable)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeForModel(request, model, sb)
        val status = sb.build()
        val sliceResult = result[Slice.empty()]!!

        assertThat(status.success).isTrue()
        assertThat(status.errors).isEmpty()
        assertThat(status.infos).isEmpty()
        assertThat(status.warnings).containsExactly(
            "For slice Slice({}) the following constraints were not buildable and are therefore not covered by the result: ${notBuildable.joinToString()}"
        )
        assertThat(sliceResult.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult.result).hasSize(3)
        assertThat(sliceResult.result.flatMap { it.coveredConstraints }).containsAll(buildable)
        assertThat(sliceResult.uncoverableConstraints).isEqualTo(4)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithFeaturesAndAdditionalRestriction(tc: TestConfig) {
        setUp(tc)
        val constraints =
            listOf("qzb & zhd", "-qzb / vti", "pow / lmp & -xvo & -bhy", "wyl & -zqm & emc", "-atu & xpx & bhy / oda")

        val request = CoverageRequest("any", mutableListOf(), listOf("vti => -zhd"), constraints)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeForModel(request, model, sb)
        val status = sb.build()
        val sliceResult = result[Slice.empty()]!!

        assertThat(status.success).isTrue()
        assertThat(status.errors).isEmpty()
        assertThat(status.infos).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(sliceResult.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult.result).hasSize(2)
        assertThat(sliceResult.result.flatMap { it.coveredConstraints }).containsAll(constraints)
        assertThat(sliceResult.result).allMatch { it.coveredConstraints.isNotEmpty() }
        assertThat(sliceResult.uncoverableConstraints).isZero()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithPairwiseCover(tc: TestConfig) {
        setUp(tc)
        val buildable = listOf("vti", "suz", "gao", "csq", "emc", "xpx", "bhy", "fdj")
        val buildableCombinations: List<Pair<String, String>> = buildable.indices
            .flatMap { i -> (i + 1 until buildable.size).map { j -> buildable[i] to buildable[j] } }
        val notBuildable = listOf("oda")

        val request = CoverageRequest("any", mutableListOf(), listOf(), buildable + notBuildable, true)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeForModel(request, model, sb)
        val status = sb.build()
        val sliceResult = result[Slice.empty()]!!

        assertThat(status.success).isTrue()
        assertThat(status.errors).isEmpty()
        assertThat(status.infos).isEmpty()
        assertThat(status.warnings).containsExactly(
            "For slice Slice({}) the following constraints were not buildable and are therefore not covered by the result: oda",
            "For slice Slice({}) the following constraint-combinations were not buildable and are therefore not covered by the result: [[suz, bhy], [csq, emc]]"
        )
        assertThat(sliceResult.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult.result).hasSize(4)
        assertThat(sliceResult.result.flatMap { it.coveredConstraints }).containsAll(buildableCombinations.map { "[${it.first}, ${it.second}]" } - listOf(
            "[suz, bhy]",
            "[csq, emc]"
        ))
        assertThat(sliceResult.uncoverableConstraints).isEqualTo(10)

        val request2 = CoverageRequest(
            "any",
            mutableListOf(),
            listOf(),
            buildableCombinations.map { "${it.first} & ${it.second}" },
            true
        )
        val result2 = cut.computeForModel(request2, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        assertThat(result2[Slice.empty()]!!.result).hasSameSizeAs(sliceResult.result)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponse(tc: TestConfig) {
        setUp(tc)
        val buildable = listOf(
            "qzb",
            "zhd",
            "pow",
            "lmp",
            "xvo",
            "wyl",
            "zqm",
            "atu",
            "vpf",
            "vti",
            "suz",
            "gao",
            "csq",
            "emc",
            "eop",
            "jlt",
            "dxg",
            "xpx",
            "bhy",
            "fdj"
        )
        val notBuildable = listOf("oda", "aai", "zgg", "dez")

        val request = CoverageRequest("any", mutableListOf(), listOf(), buildable + notBuildable)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).containsExactly(
            "For slice Slice({}) the following constraints were not buildable and are therefore not covered by the result: ${notBuildable.joinToString()}"
        )
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result).isEqualTo(CoverageMainResult(3, 4))
        assertThat(response.merge[0].slices).hasSize(1)
        assertThat(response.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(response.detailMap).hasSize(1)
        assertThat(response.detailMap[1]).hasSize(1)
        assertThat(response.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(response.detailMap[1]!![0].detail.configurations).hasSize(3)
            .allMatch { it.coveredConstraints.isNotEmpty() }
    }
}
