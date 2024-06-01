package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.PropertyDO
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.RuleDO
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.boolerules.rulefile.SlicingPropertyDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ConsistencyComputationOnlyBooleanWithSlicesTest : TestWithConfig() {

    private val cut = ConsistencyComputation
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

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(9)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithExplanation(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2", "S3")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intMin = 1, intMax = 3),
                SliceTypeDO.SPLIT
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        assertThat(result).hasSize(9)
        assertThat(result[s11]!!.example!!.features).hasSize(1)
        assertThat(result[s21]!!.example!!.features).hasSize(1)
        assertThat(result[s31]!!.example!!.features).hasSize(1)
        assertThat(result[s12]!!.example!!.features).hasSize(1)
        assertThat(result[s22]!!.example!!.features).hasSize(1)
        assertThat(result[s32]!!.example!!.features).hasSize(1)
        assertThat(result[s13]!!.example!!.features).hasSize(1)
        assertThat(result[s23]!!.example!!.features).hasSize(1)
        assertThat(result[s33]!!.example!!.features).hasSize(1)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intMin = 1, intMax = 2),
                SliceTypeDO.SPLIT
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf("-b"))

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        assertThat(result).hasSize(4)
        assertThat(result[s11]!!.example!!.features).hasSize(2)
        assertThat(result[s21]!!.example!!.features).hasSize(2)
        assertThat(result[s12]!!.example).isNull()
        assertThat(result[s22]!!.example).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithExplanationWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf("-b", "-c"), true)

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        assertThat(result).hasSize(2)
        val sliceResult1 = result[s11]!!
        assertThat(sliceResult1.consistent).isFalse

        assertThat(sliceResult1.explanation).containsExactlyInAnyOrder(
            RuleDO("if a then b / c", "", "", 43, listOf(PropertyDO("version", "1", true))),
            RuleDO("a / b", "", "", 42, listOf()),
            RuleDO("-c"),
            RuleDO("-b")
        )
        val sliceResult2 = result[s21]!!
        assertThat(sliceResult2.consistent).isFalse
        assertThat(sliceResult2.explanation).containsExactlyInAnyOrder(
            RuleDO("if a then b / c", "", "", 43, listOf(PropertyDO("version", "1", true))),
            RuleDO("a / b", "", "", 42, listOf()),
            RuleDO("-c"),
            RuleDO("-b")
        )
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf("-b", "-c"))

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        assertThat(result).hasSize(2)
        val sliceResult1 = result[s11]!!
        assertThat(sliceResult1.consistent).isFalse
        assertThat(sliceResult1.explanation).isNull()
        val sliceResult2 = result[s21]!!
        assertThat(sliceResult2.consistent).isFalse
        assertThat(sliceResult2.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponse(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2", "S3")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intMin = 1, intMax = 3),
                SliceTypeDO.SPLIT
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val respone = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(9)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(9)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(respone.merge).hasSize(1)
        assertThat(respone.merge[0].result).isTrue()
        assertThat(respone.merge[0].slices).hasSize(1)

        assertThat(respone.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO(
                        "series",
                        PropertyTypeDO.ENUM,
                        PropertyRangeDO(enumValues = setOf("S1", "S2", "S3"))
                    ),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 3))
                )
            )
        )
        assertThat(respone.detailMap).hasSize(1)
        assertThat(respone.detailMap[1]).hasSize(9)
        assertThat(respone.detailMap[1]!!.map { it.detail.exampleConfiguration }).hasSize(9)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseVersionAny(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2", "S3")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 3), SliceTypeDO.ANY)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val respone = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(9)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(9)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(respone.merge).hasSize(1)
        assertThat(respone.merge[0].result).isTrue()
        assertThat(respone.merge[0].slices).hasSize(1)

        assertThat(respone.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO(
                        "series",
                        PropertyTypeDO.ENUM,
                        PropertyRangeDO(enumValues = setOf("S1", "S2", "S3"))
                    )
                )
            )
        )

        assertThat(respone.detailMap).hasSize(1)
        assertThat(respone.detailMap[1]).hasSize(3)
        assertThat(respone.detailMap[1]!!.map { it.detail.exampleConfiguration }).hasSize(3)
    }
}
