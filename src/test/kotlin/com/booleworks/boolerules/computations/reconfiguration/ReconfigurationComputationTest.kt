package com.booleworks.boolerules.computations.reconfiguration

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ReconfigurationComputationTest : TestWithConfig() {

    private val cut = ReconfigurationComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/real/automotive/automotive_simple_1.prl"))

    @ParameterizedTest
    @MethodSource("configs")
    fun testEmptyOrder(tc: TestConfig) {
        setUp(tc)
        val request =
            ReconfigurationRequest("fileId", mutableListOf(), listOf(), listOf(), ReconfigurationAlgorithm.MAX_COV)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result.featuresToRemove).isEmpty()
        assertThat(response.merge[0].result.featuresToAdd).hasSize(9)
        assertThat(response.merge[0].slices).hasSize(1)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testMaxCov(tc: TestConfig) {
        setUp(tc)
        val order = listOf(
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
            "xpx"
        )
        val request =
            ReconfigurationRequest("fileId", mutableListOf(), listOf(), order, ReconfigurationAlgorithm.MAX_COV)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.featuresToRemove).isSubsetOf(order).hasSize(4)
        assertThat(response.merge[0].result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(15)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testMinDiff(tc: TestConfig) {
        setUp(tc)
        val order = listOf(
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
            "xpx"
        )
        val request =
            ReconfigurationRequest("fileId", mutableListOf(), listOf(), order, ReconfigurationAlgorithm.MIN_DIFF)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.featuresToRemove).isSubsetOf(order).hasSize(7)
        assertThat(response.merge[0].result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(12)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testUnknownCodes(tc: TestConfig) {
        setUp(tc)
        val order = listOf(
            "YYYY",
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
            "XXXX"
        )
        val request =
            ReconfigurationRequest("fileId", mutableListOf(), listOf(), order, ReconfigurationAlgorithm.MAX_COV)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).containsExactly(
            "The order contains invalid features which must always be removed: YYYY, XXXX"
        )
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.featuresToRemove).isSubsetOf(order).contains("YYYY", "XXXX").hasSize(6)
        assertThat(response.merge[0].result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(15)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testAdditionalRestrictionsWithMaxCov(tc: TestConfig) {
        setUp(tc)
        val order = listOf(
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
            "xpx"
        )
        val request = ReconfigurationRequest(
            "fileId",
            mutableListOf(),
            listOf("qzb / zhd => zqm & vti", "xpx => blw / pzg"),
            order,
            ReconfigurationAlgorithm.MAX_COV
        )
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.featuresToRemove).isSubsetOf(order).hasSize(6)
        assertThat(response.merge[0].result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(16)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testAdditionalRestrictionsWithMinDiff(tc: TestConfig) {
        setUp(tc)
        val order = listOf(
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
            "xpx"
        )
        val request = ReconfigurationRequest(
            "fileId",
            mutableListOf(),
            listOf("qzb / zhd => zqm & vti", "xpx => blw / pzg"),
            order,
            ReconfigurationAlgorithm.MIN_DIFF
        )
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.featuresToRemove).isSubsetOf(order).hasSize(10)
        assertThat(response.merge[0].result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(11)
    }
}
