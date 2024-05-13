package com.booleworks.boolerules.computations.optimization

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

internal class OptimizationOnlyEnumWithSlicesTest : TestWithConfig() {

    private val cut = OptimizationComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/transpiler/merge3.prl"))

    private val weightings = listOf(
        WeightPair("""[a in ["a1", "a2"]]""", 20), // always true
        WeightPair("""[b = "b1"]""", 10),          // always in version 1
        WeightPair("""[b = "b2"]""", 15),          // always in version 2
        WeightPair("""[b = "b3"]""", 1000),        // never true
        WeightPair("""[p = "px"]""", -1000),       // never true
        WeightPair("""[p = "p1"]""", -7),          // always in series 1
        WeightPair("""[p = "p2"]""", -9),          // always in series 2
        WeightPair("""[c = "c1"]""", 2),           // version 1 or 2
        WeightPair("""[c = "c2"]""", 4),           // version 1 or 2
        WeightPair("""[c = "c3"]""", 6),           // only in version 2
    )

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscreteMin(tc: TestConfig) {
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
        val request = OptimizationRequest("any", sliceSelection, listOf(), OptimizationType.MIN, weightings)

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

        assertThat(result.merge).hasSize(4)

        val res25 = result.merge.first { it.result == 25 }
        assertThat(res25.result).isEqualTo(25)
        assertThat(res25.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)))
                )
            )
        )
        var details = detailsForSlice(slice1, result.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b1, c=c1, p=p1")

        val res23 = result.merge.first { it.result == 23 }
        assertThat(res23.result).isEqualTo(23)
        assertThat(res23.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)))
                )
            )
        )
        details = detailsForSlice(slice2, result.detailMap[2])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b1, c=c1, p=p2, q=q1")

        val res30 = result.merge.first { it.result == 30 }
        assertThat(res30.result).isEqualTo(30)
        assertThat(res30.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(2)))
                )
            )
        )
        details = detailsForSlice(slice3, result.detailMap[3])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b2, c=c1, p=p1")

        val res28 = result.merge.first { it.result == 28 }
        assertThat(res28.result).isEqualTo(28)
        assertThat(res28.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(2)))
                )
            )
        )
        details = detailsForSlice(slice4, result.detailMap[4])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b2, c=c1, p=p2, q=q1")
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscreteMax(tc: TestConfig) {
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
        val request = OptimizationRequest("any", sliceSelection, listOf(), OptimizationType.MAX, weightings)

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

        assertThat(result.merge).hasSize(4)

        val res27 = result.merge.first { it.result == 27 }
        assertThat(res27.result).isEqualTo(27)
        assertThat(res27.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)))
                )
            )
        )
        var details = detailsForSlice(slice1, result.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b1, c=c2, p=p1")

        val res25 = result.merge.first { it.result == 25 }
        assertThat(res25.result).isEqualTo(25)
        assertThat(res25.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)))
                )
            )
        )
        details = detailsForSlice(slice2, result.detailMap[2])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b1, c=c2, p=p2, q=q1")

        val res34 = result.merge.first { it.result == 34 }
        assertThat(res34.result).isEqualTo(34)
        assertThat(res34.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(2)))
                )
            )
        )
        details = detailsForSlice(slice3, result.detailMap[3])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b2, c=c3, p=p1")

        val res32 = result.merge.first { it.result == 32 }
        assertThat(res32.result).isEqualTo(32)
        assertThat(res32.slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(2)))
                )
            )
        )
        details = detailsForSlice(slice4, result.detailMap[4])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.exampleConfiguration!!.toString()).isEqualTo("a=a2, b=b2, c=c3, p=p2, q=q1")
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllAnyMin(tc: TestConfig) {
        setUp(tc)
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        ).toDO()

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
        val request = OptimizationRequest("any", sliceSelection, listOf(), OptimizationType.MIN, weightings)

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

        assertThat(result.merge).hasSize(1)
        val res23 = result.merge[0]
        assertThat(res23.result).isEqualTo(23)
        assertThat(res23.slices[0]).isEqualTo(SliceDO(listOf()))

        //TODO fix details
//        val details = detailsForSlice(slice2, result.detailMap[1])
//        assertThat(details.slice).isEqualTo(
//            SliceDO(
//                listOf(
//                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1))),
//                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2")))
//                )
//            )
//        )
//        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
//        assertThat(details.detail.exampleConfiguration.toString()).isEqualTo("a=a2, b=b1, c=c1, p=p2, q=q1")
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllAnyMax(tc: TestConfig) {
        setUp(tc)
        val slice3 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        ).toDO()

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
        val request = OptimizationRequest("any", sliceSelection, listOf(), OptimizationType.MAX, weightings)

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

        assertThat(result.merge).hasSize(1)

        val res34 = result.merge[0]
        assertThat(res34.result).isEqualTo(34)
        assertThat(res34.slices[0]).isEqualTo(SliceDO(listOf()))

        //TODO fix details
//        val details = detailsForSlice(slice3, result.detailMap[1])
//        assertThat(details.slice).isEqualTo(
//            SliceDO(
//                listOf(
//                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(2))),
//                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1")))
//                )
//            )
//        )
//        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
//        assertThat(details.detail.exampleConfiguration.toString()).isEqualTo("a=a2, b=b2, c=c3, p=p1")
    }
}
