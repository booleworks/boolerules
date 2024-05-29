package com.booleworks.boolerules.computations.coverage

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class CoverageComputationWithSlicesTest : TestWithConfig() {

    private val cut = CoverageComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/transpiler/merge2.prl"))

    private val s11 = Slice.of(mapOf(Pair(IntProperty("version", 1), SliceType.SPLIT), Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
    private val s21 = Slice.of(mapOf(Pair(IntProperty("version", 1), SliceType.SPLIT), Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
    private val s31 = Slice.of(mapOf(Pair(IntProperty("version", 1), SliceType.SPLIT), Pair(EnumProperty("series", "S3"), SliceType.SPLIT)))
    private val s12 = Slice.of(mapOf(Pair(IntProperty("version", 2), SliceType.SPLIT), Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
    private val s22 = Slice.of(mapOf(Pair(IntProperty("version", 2), SliceType.SPLIT), Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
    private val s32 = Slice.of(mapOf(Pair(IntProperty("version", 2), SliceType.SPLIT), Pair(EnumProperty("series", "S3"), SliceType.SPLIT)))
    private val s13 = Slice.of(mapOf(Pair(IntProperty("version", 3), SliceType.SPLIT), Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
    private val s23 = Slice.of(mapOf(Pair(IntProperty("version", 3), SliceType.SPLIT), Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
    private val s33 = Slice.of(mapOf(Pair(IntProperty("version", 3), SliceType.SPLIT), Pair(EnumProperty("series", "S3"), SliceType.SPLIT)))

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithConstraints(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1", "S2", "S3")), SliceTypeDO.SPLIT),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 3), SliceTypeDO.SPLIT)
        )

        val request = CoverageRequest("any", sliceSelection, listOf(), listOf("a & b", "-a & -c", "-p / q", "-p & -z", "(-b / z) & (x / y / r)", "(r / x) & (-c / -q)"))
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeForModel(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue()
        assertThat(status.errors).isEmpty()
        assertThat(status.infos).isEmpty()
        assertThat(status.warnings).containsExactlyInAnyOrder(
            """For slice Slice({version 3=SPLIT, series "S1"=SPLIT}) the following constraints were not buildable and are therefore not covered by the result: -a & -c, (-b / z) & (x / y / r), (r / x) & (-c / -q)""",
            """For slice Slice({version 3=SPLIT, series "S2"=SPLIT}) the following constraints were not buildable and are therefore not covered by the result: -a & -c, (-b / z) & (x / y / r), (r / x) & (-c / -q)""",
            """For slice Slice({version 3=SPLIT, series "S3"=SPLIT}) the following constraints were not buildable and are therefore not covered by the result: -a & -c""",
        )

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
