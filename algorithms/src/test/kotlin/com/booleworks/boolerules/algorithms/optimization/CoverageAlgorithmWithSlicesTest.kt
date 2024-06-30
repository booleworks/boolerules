package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.algorithms.AlgorithmExecutor
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

internal class CoverageAlgorithmWithSlicesTest : TestWithConfig() {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge2.prl"))

    private val s11 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val s21 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )
    private val s31 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S3"), SliceType.SPLIT)
        )
    )
    private val s12 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val s22 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )
    private val s32 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S3"), SliceType.SPLIT)
        )
    )
    private val s13 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 3), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val s23 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 3), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )
    private val s33 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 3), SliceType.SPLIT),
            Pair(EnumProperty("series", "S3"), SliceType.SPLIT)
        )
    )

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithConstraints(tc: TestConfig) {
        setUp(tc)
        val constraints = listOf(
            "a & b",
            "-a & -c",
            "-p / q",
            "-p & -z",
            "(-b / z) & (x / y / r)",
            "(r / x) & (-c / -q)"
        )
        val algo = Coverage(constraints, false)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2", "S3"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.interval(1, 3), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(result[s11]!!.result).hasSize(3)
        assertThat(result[s21]!!.result).hasSize(3)
        assertThat(result[s31]!!.result).hasSize(3)
        assertThat(result[s12]!!.result).hasSize(2)
        assertThat(result[s22]!!.result).hasSize(2)
        assertThat(result[s32]!!.result).hasSize(2)
        assertThat(result[s13]!!.result).hasSize(1)
        assertThat(result[s23]!!.result).hasSize(1)
        assertThat(result[s33]!!.result).hasSize(2)

        assertThat(result[s11]!!.uncoverableConstraints).isEqualTo(0)
        assertThat(result[s21]!!.uncoverableConstraints).isEqualTo(0)
        assertThat(result[s31]!!.uncoverableConstraints).isEqualTo(0)
        assertThat(result[s12]!!.uncoverableConstraints).isEqualTo(0)
        assertThat(result[s22]!!.uncoverableConstraints).isEqualTo(0)
        assertThat(result[s32]!!.uncoverableConstraints).isEqualTo(0)
        assertThat(result[s13]!!.uncoverableConstraints).isEqualTo(3)
        assertThat(result[s23]!!.uncoverableConstraints).isEqualTo(3)
        assertThat(result[s33]!!.uncoverableConstraints).isEqualTo(1)
    }
}
