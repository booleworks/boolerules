package com.booleworks.boolerules.computations.minmaxconfig

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.detailsForSlice
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.toDO
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MinMaxConfigurationOnlyEnumWithSlicesTest : TestWithConfig() {

    private val cut = MinMaxConfigComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/transpiler/merge3.prl"))

    private val s1v1 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    ).toDO()
    private val s2v1 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    ).toDO()
    private val s1v2 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    ).toDO()
    private val s2v2 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    ).toDO()

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscrete(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        ).toDO()
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        ).toDO()
        val slice3 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        ).toDO()
        val slice4 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        ).toDO()

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
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.SPLIT
            )
        )
        val request = MinMaxConfigRequest("any", sliceSelection, listOf(), OptimizationType.MIN, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        val res4 = result.merge.first { it.result == 4 }
        val res5 = result.merge.first { it.result == 5 }

        assertThat(result.merge).hasSize(2)
        assertThat(res4.result).isEqualTo(4)
        assertThat(res4.slices).hasSize(1)

        assertThat(res4.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap).hasSize(2)
        assertThat(result.detailMap[1]).hasSize(2)

        var details = detailsForSlice(slice1, result.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        details = detailsForSlice(slice3, result.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)

        assertThat(res5.result).isEqualTo(5)
        assertThat(res5.slices).hasSize(1)

        assertThat(res5.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap[2]).hasSize(2)
        details = detailsForSlice(slice2, result.detailMap[2])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        details = detailsForSlice(slice4, result.detailMap[2])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscreteWithAdditionalConstraints(tc: TestConfig) {
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
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.SPLIT
            )
        )
        val request = MinMaxConfigRequest("any", sliceSelection, listOf("[q = \"q2\"]"), OptimizationType.MIN, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).containsExactlyInAnyOrder(
            "Rule set for the slice Slice({version 1=SPLIT, series \"S1\"=SPLIT}) is inconsistent. Use the 'consistency' check to get an explanation, why.",
            "Rule set for the slice Slice({version 2=SPLIT, series \"S1\"=SPLIT}) is inconsistent. Use the 'consistency' check to get an explanation, why."
        )
        assertThat(status.infos).isEmpty()

        assertThat(result.merge).hasSize(2)
        assertThat(result.merge[0].result).isEqualTo(-1)
        assertThat(result.merge[0].slices).hasSize(1)

        assertThat(result.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap).hasSize(2)

        assertThat(result.detailMap[1]).hasSize(2)
        assertThat(result.detailMap[1]!![0].slice).isEqualTo(s1v1)
        assertThat(result.detailMap[1]!![0].detail.exampleConfiguration).isNull()
        assertThat(result.detailMap[1]!![1].slice).isEqualTo(s1v2)
        assertThat(result.detailMap[1]!![1].detail.exampleConfiguration).isNull()

        assertThat(result.merge[1].result).isEqualTo(5)
        assertThat(result.merge[1].slices).hasSize(1)

        assertThat(result.merge[1].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap[2]).hasSize(2)
        assertThat(result.detailMap[2]!![0].slice).isEqualTo(s2v1)
        assertThat(result.detailMap[2]!![0].detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(result.detailMap[2]!![1].slice).isEqualTo(s2v2)
        assertThat(result.detailMap[2]!![1].detail.exampleConfiguration!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMaxAllSplitDiscrete(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        ).toDO()
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        ).toDO()
        val slice3 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        ).toDO()
        val slice4 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        ).toDO()

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
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.SPLIT
            )
        )
        val request = MinMaxConfigRequest("any", sliceSelection, listOf(), OptimizationType.MAX, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(result.merge).hasSize(2)

        val res4 = result.merge.first { it.result == 4 }
        val res5 = result.merge.first { it.result == 5 }

        assertThat(res4.result).isEqualTo(4)
        assertThat(res4.slices).hasSize(1)

        assertThat(res4.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap).hasSize(2)

        assertThat(result.detailMap[1]).hasSize(2)
        var details = detailsForSlice(slice1, result.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        details = detailsForSlice(slice3, result.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)

        assertThat(res5.result).isEqualTo(5)
        assertThat(res5.slices).hasSize(1)

        assertThat(res5.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap[2]).hasSize(2)
        details = detailsForSlice(slice2, result.detailMap[2])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        details = detailsForSlice(slice4, result.detailMap[2])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMaxAllSplitDiscreteWithAdditionalConstraints(tc: TestConfig) {
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
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.SPLIT
            )
        )
        val request = MinMaxConfigRequest("any", sliceSelection, listOf("[q = \"q2\"]"), OptimizationType.MAX, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val result = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).containsExactlyInAnyOrder(
            "Rule set for the slice Slice({version 1=SPLIT, series \"S1\"=SPLIT}) is inconsistent. Use the 'consistency' check to get an explanation, why.",
            "Rule set for the slice Slice({version 2=SPLIT, series \"S1\"=SPLIT}) is inconsistent. Use the 'consistency' check to get an explanation, why."
        )
        assertThat(status.infos).isEmpty()

        assertThat(result.merge).hasSize(2)
        assertThat(result.merge[0].result).isEqualTo(-1)
        assertThat(result.merge[0].slices).hasSize(1)

        assertThat(result.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )
        assertThat(result.detailMap).hasSize(2)

        assertThat(result.detailMap[1]).hasSize(2)
        assertThat(result.detailMap[1]!![0].slice).isEqualTo(s1v1)
        assertThat(result.detailMap[1]!![0].detail.exampleConfiguration).isNull()
        assertThat(result.detailMap[1]!![1].slice).isEqualTo(s1v2)
        assertThat(result.detailMap[1]!![1].detail.exampleConfiguration).isNull()

        assertThat(result.merge[1].result).isEqualTo(5)
        assertThat(result.merge[1].slices).hasSize(1)

        assertThat(result.merge[1].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(result.detailMap[2]).hasSize(2)
        assertThat(result.detailMap[2]!![0].slice).isEqualTo(s2v1)
        assertThat(result.detailMap[2]!![0].detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(result.detailMap[2]!![1].slice).isEqualTo(s2v2)
        assertThat(result.detailMap[2]!![1].detail.exampleConfiguration!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinMaxAllAny(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ANY
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.ANY
            )
        )
        val requestMin = MinMaxConfigRequest("any", sliceSelection, listOf(), OptimizationType.MIN, listOf())
        val requestMax = MinMaxConfigRequest("any", sliceSelection, listOf(), OptimizationType.MAX, listOf())

        val sbMin = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val resultMin = cut.computeResponse(requestMin, model, sbMin)
        val statusMin = sbMin.build()
        val sbMax = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val resultMax = cut.computeResponse(requestMax, model, sbMax)
        val statusMax = sbMax.build()

        assertThat(statusMin.success).isTrue
        assertThat(statusMin.statistics.numberOfSlices).isEqualTo(4)
        assertThat(statusMin.statistics.numberOfSliceComputations).isEqualTo(4)

        assertThat(resultMin.merge).hasSize(1)
        assertThat(resultMin.merge[0].result).isEqualTo(4)
        assertThat(resultMin.merge[0].slices).hasSize(1)
        assertThat(resultMin.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(resultMin.detailMap).hasSize(1)
        assertThat(resultMin.detailMap[1]).hasSize(1)
        assertThat(resultMin.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(resultMin.detailMap[1]!![0].detail.exampleConfiguration!!.size).isEqualTo(4)

        assertThat(statusMax.success).isTrue
        assertThat(statusMax.statistics.numberOfSlices).isEqualTo(4)
        assertThat(statusMax.statistics.numberOfSliceComputations).isEqualTo(4)

        assertThat(resultMax.merge).hasSize(1)
        assertThat(resultMax.merge[0].result).isEqualTo(5)
        assertThat(resultMax.merge[0].slices).hasSize(1)
        assertThat(resultMax.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(resultMax.detailMap).hasSize(1)
        assertThat(resultMax.detailMap[1]).hasSize(1)
        assertThat(resultMax.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(resultMax.detailMap[1]!![0].detail.exampleConfiguration!!.size).isEqualTo(5)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinMaxAllAnyWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ANY
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.ANY
            )
        )
        val requestMin =
            MinMaxConfigRequest("any", sliceSelection, listOf("[c = \"c2\"]"), OptimizationType.MIN, listOf())
        val requestMax =
            MinMaxConfigRequest("any", sliceSelection, listOf("[c = \"c2\"]"), OptimizationType.MAX, listOf())

        val sbMin = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val resultMin = cut.computeResponse(requestMin, model, sbMin)
        val statusMin = sbMin.build()
        val sbMax = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val resultMax = cut.computeResponse(requestMax, model, sbMax)
        val statusMax = sbMax.build()

        assertThat(statusMin.success).isTrue
        assertThat(statusMin.statistics.numberOfSlices).isEqualTo(4)
        assertThat(statusMin.statistics.numberOfSliceComputations).isEqualTo(4)

        assertThat(resultMin.merge).hasSize(1)
        assertThat(resultMin.merge[0].result).isEqualTo(4)
        assertThat(resultMin.merge[0].slices).hasSize(1)
        assertThat(resultMin.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(resultMin.detailMap).hasSize(1)
        assertThat(resultMin.detailMap[1]).hasSize(1)
        assertThat(resultMin.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(resultMin.detailMap[1]!![0].detail.exampleConfiguration!!.size).isEqualTo(4)

        assertThat(statusMax.success).isTrue
        assertThat(statusMax.statistics.numberOfSlices).isEqualTo(4)
        assertThat(statusMax.statistics.numberOfSliceComputations).isEqualTo(4)

        assertThat(resultMax.merge).hasSize(1)
        assertThat(resultMax.merge[0].result).isEqualTo(5)
        assertThat(resultMax.merge[0].slices).hasSize(1)
        assertThat(resultMax.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(resultMax.detailMap).hasSize(1)
        assertThat(resultMax.detailMap[1]).hasSize(1)
        assertThat(resultMax.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(resultMax.detailMap[1]!![0].detail.exampleConfiguration!!.size).isEqualTo(5)
    }
}
