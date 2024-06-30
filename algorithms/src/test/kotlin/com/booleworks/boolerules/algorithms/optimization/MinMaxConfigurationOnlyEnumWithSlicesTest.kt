package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.algorithms.AlgorithmExecutor
import com.booleworks.boolerules.algorithms.OptimizationType
import com.booleworks.boolerules.algorithms.ResultStatus
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.ExecutionStatusBuilder
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.slices.EnumSliceSelection
import com.booleworks.prl.model.slices.IntSliceSelection
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MinMaxConfigurationOnlyEnumWithSlicesTest : TestWithConfig() {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    private val s1v1 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val s2v1 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )
    private val s1v2 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val s2v2 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscrete(tc: TestConfig) {
        setUp(tc)

        val algo = MinMaxConfig(listOf(), OptimizationType.MIN)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val res1 = result[s1v1]!!
        val res2 = result[s1v2]!!
        val res3 = result[s2v1]!!
        val res4 = result[s2v2]!!

        assertThat(res1.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res1.size).isEqualTo(4)
        assertThat(res1.featureModel!!.size).isEqualTo(4)
        assertThat(res2.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res2.size).isEqualTo(4)
        assertThat(res2.featureModel!!.size).isEqualTo(4)
        assertThat(res3.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res3.size).isEqualTo(5)
        assertThat(res3.featureModel!!.size).isEqualTo(5)
        assertThat(res4.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res4.size).isEqualTo(5)
        assertThat(res4.featureModel!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscreteWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val algo = MinMaxConfig(listOf(), OptimizationType.MIN)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf("[q = \"q2\"]"), listOf(), status)

        val res1 = result[s1v1]!!
        val res2 = result[s1v2]!!
        val res3 = result[s2v1]!!
        val res4 = result[s2v2]!!

        assertThat(res1.state.status).isEqualTo(ResultStatus.NOT_BUILDABLE)
        assertThat(res1.size).isEqualTo(-1)
        assertThat(res1.featureModel).isNull()
        assertThat(res2.state.status).isEqualTo(ResultStatus.NOT_BUILDABLE)
        assertThat(res2.size).isEqualTo(-1)
        assertThat(res2.featureModel).isNull()
        assertThat(res3.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res3.size).isEqualTo(5)
        assertThat(res3.featureModel!!.size).isEqualTo(5)
        assertThat(res4.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res4.size).isEqualTo(5)
        assertThat(res4.featureModel!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMaxAllSplitDiscrete(tc: TestConfig) {
        setUp(tc)

        val algo = MinMaxConfig(listOf(), OptimizationType.MAX)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val res1 = result[s1v1]!!
        val res2 = result[s1v2]!!
        val res3 = result[s2v1]!!
        val res4 = result[s2v2]!!

        assertThat(res1.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res1.size).isEqualTo(4)
        assertThat(res1.featureModel!!.size).isEqualTo(4)
        assertThat(res2.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res2.size).isEqualTo(4)
        assertThat(res2.featureModel!!.size).isEqualTo(4)
        assertThat(res3.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res3.size).isEqualTo(5)
        assertThat(res3.featureModel!!.size).isEqualTo(5)
        assertThat(res4.state.status).isEqualTo(ResultStatus.SUCCESS)
        assertThat(res4.size).isEqualTo(5)
        assertThat(res4.featureModel!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinMaxAllAny(tc: TestConfig) {
        setUp(tc)

        val algoMin = MinMaxConfig(listOf(), OptimizationType.MIN)
        val algoMax = MinMaxConfig(listOf(), OptimizationType.MAX)
        val execMin = AlgorithmExecutor(algoMin)
        val execMax = AlgorithmExecutor(algoMax)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.ANY),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.ANY)
        )
        val resultMin = execMin.executeForModel(model, sliceSelection, listOf(), listOf(), status)
        val resultMax = execMax.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(resultMin).hasSize(1)
        assertThat(resultMin.values.first().size).isEqualTo(4)
        assertThat(resultMin.values.first().featureModel!!.size).isEqualTo(4)
        assertThat(resultMax).hasSize(1)
        assertThat(resultMax.values.first().size).isEqualTo(5)
        assertThat(resultMax.values.first().featureModel!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinMaxAllAnyWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)

        val algoMin = MinMaxConfig(listOf(), OptimizationType.MIN)
        val algoMax = MinMaxConfig(listOf(), OptimizationType.MAX)
        val execMin = AlgorithmExecutor(algoMin)
        val execMax = AlgorithmExecutor(algoMax)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.ANY),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.ANY)
        )
        val resultMin = execMin.executeForModel(model, sliceSelection, listOf("[c = \"c2\"]"), listOf(), status)
        val resultMax = execMax.executeForModel(model, sliceSelection, listOf("[c = \"c2\"]"), listOf(), status)

        assertThat(resultMin).hasSize(1)
        assertThat(resultMin.values.first().size).isEqualTo(4)
        assertThat(resultMin.values.first().featureModel!!.size).isEqualTo(4)
        assertThat(resultMax).hasSize(1)
        assertThat(resultMax.values.first().size).isEqualTo(5)
        assertThat(resultMax.values.first().featureModel!!.size).isEqualTo(5)
    }
}
