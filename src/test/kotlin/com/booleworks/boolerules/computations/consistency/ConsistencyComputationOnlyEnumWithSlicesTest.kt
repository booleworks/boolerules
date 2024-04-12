package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.detailsForSlice
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.toDO
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.boolerules.rulefile.SlicingPropertyDO
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.mergeSlices
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ConsistencyComputationOnlyEnumWithSlicesTest : TestWithConfig() {

    private val cut = ConsistencyComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(5)
    }

    @Test
    fun testComputeForSliceWithoutExplanation() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())

        val request = ConsistencyRequest("any", mutableListOf(), listOf())
        val info = modelTranslation[0].info
        val mergedInfo = mergeSlices(cf, modelTranslation.computations).info
        val resultTrue = cut.computeForSlice(
            request,
            Slice.empty(),
            info,
            model,
            f,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val resultFalse = cut.computeForSlice(
            request,
            Slice.empty(),
            mergedInfo,
            model,
            f,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )

        assertThat(resultTrue.consistent).isTrue()
        assertThat(resultTrue.explanation).isNull()
        assertThat(resultTrue.example).isNotNull
        assertThat(resultTrue.slice).isEqualTo(Slice.empty())

        assertThat(resultFalse.consistent).isFalse()
        assertThat(resultFalse.explanation).isNull()
        assertThat(resultFalse.example).isNull()
        assertThat(resultFalse.slice).isEqualTo(Slice.empty())
    }

    @Test
    fun testComputeForSliceWithExplanation() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())

        val request = ConsistencyRequest("any", mutableListOf(), listOf(), true)
        val info = modelTranslation[0].info
        val mergedInfo = mergeSlices(cf, modelTranslation.computations).info
        val resultTrue = cut.computeForSlice(
            request,
            Slice.empty(),
            info,
            model,
            f,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val resultFalse = cut.computeForSlice(
            request,
            Slice.empty(),
            mergedInfo,
            model,
            f,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )

        assertThat(resultTrue.consistent).isTrue()
        assertThat(resultTrue.explanation).isNull()
        assertThat(resultTrue.example).isNotNull
        assertThat(resultTrue.slice).isEqualTo(Slice.empty())

        assertThat(resultFalse.consistent).isFalse()
        assertThat(resultFalse.slice).isEqualTo(Slice.empty())
        assertThat(resultFalse.explanation).hasSize(4)
        assertThat(resultFalse.example).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSingleSplitSlice(tc: TestConfig) {
        setUp(tc)
        val slice = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult = result[slice]!!

        assertThat(result).hasSize(1)

        assertThat(sliceResult.slice).isEqualTo(slice)
        assertThat(sliceResult.consistent).isTrue
        assertThat(sliceResult.example!!.size).isEqualTo(4)
        assertThat(sliceResult.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.consistent).isTrue
        assertThat(sliceResult1.example!!.size).isEqualTo(4)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isTrue
        assertThat(sliceResult2.example!!.size).isEqualTo(5)
        assertThat(sliceResult2.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesAndVersionSplitSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )
        val slice3 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice4 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

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
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!
        val sliceResult3 = result[slice3]!!
        val sliceResult4 = result[slice4]!!

        assertThat(result).hasSize(4)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.consistent).isTrue
        assertThat(sliceResult1.example!!.size).isEqualTo(4)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isTrue
        assertThat(sliceResult2.example!!.size).isEqualTo(5)
        assertThat(sliceResult2.explanation).isNull()

        assertThat(sliceResult3.slice).isEqualTo(slice3)
        assertThat(sliceResult3.consistent).isTrue
        assertThat(sliceResult3.example!!.size).isEqualTo(4)
        assertThat(sliceResult3.explanation).isNull()

        assertThat(sliceResult4.slice).isEqualTo(slice4)
        assertThat(sliceResult4.consistent).isTrue
        assertThat(sliceResult4.example!!.size).isEqualTo(5)
        assertThat(sliceResult4.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersionAnySlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
        val anySlice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val anySlice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

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
                SliceTypeDO.ANY
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(anySlice1)
        assertThat(sliceResult1.consistent).isTrue
        assertThat(sliceResult1.example!!.size).isEqualTo(4)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(anySlice2)
        assertThat(sliceResult2.consistent).isTrue
        assertThat(sliceResult2.example!!.size).isEqualTo(5)
        assertThat(sliceResult2.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesAndVersionAnySlices(tc: TestConfig) {
        setUp(tc)
        val anySlice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
                Pair(EnumProperty("series", "S1"), SliceType.ANY)
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ANY
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2), SliceTypeDO.ANY)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult1 = result[Slice.empty()]!!

        assertThat(result).hasSize(1)

        assertThat(sliceResult1.slice).isEqualTo(anySlice1)
        assertThat(sliceResult1.consistent).isTrue
        assertThat(sliceResult1.example!!.size).isEqualTo(4)
        assertThat(sliceResult1.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersionAllSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))

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
                SliceTypeDO.ALL
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf(), true)

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.consistent).isFalse

        assertThat(sliceResult1.example).isNull()
        assertThat(sliceResult1.explanation).hasSize(7)

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isFalse

        assertThat(sliceResult2.example).isNull()
        assertThat(sliceResult2.explanation).hasSize(7)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesAndVersionAllSlices(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ALL
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.ALL
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf(), true)

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", SINGLE))
        val sliceResult1 = result[Slice.empty()]!!

        assertThat(result).hasSize(1)

        assertThat(sliceResult1.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult1.consistent).isFalse
        assertThat(sliceResult1.example).isNull()
        assertThat(sliceResult1.explanation).hasSize(4)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseAllSplitDiscrete(tc: TestConfig) {
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
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result).isTrue()
        assertThat(response.merge[0].slices).hasSize(1)

        assertThat(response.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1", "S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2)))
                )
            )
        )

        assertThat(response.detailMap).hasSize(1)
        assertThat(response.detailMap[1]).hasSize(4)

        var details = detailsForSlice(slice1, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.explanation).isNull()

        details = detailsForSlice(slice2, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.explanation).isNull()

        details = detailsForSlice(slice3, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.explanation).isNull()

        details = detailsForSlice(slice4, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseAllSplitContinous(tc: TestConfig) {
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
                PropertyRangeDO(intMin = 1, intMax = 2),
                SliceTypeDO.SPLIT
            )
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result).isTrue()
        assertThat(response.merge[0].slices).hasSize(1)

        assertThat(response.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1", "S2"))),
                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2))
                )
            )
        )

        assertThat(response.detailMap).hasSize(1)
        assertThat(response.detailMap[1]).hasSize(4)

        var details = detailsForSlice(slice1, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.explanation).isNull()

        details = detailsForSlice(slice2, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.explanation).isNull()

        details = detailsForSlice(slice3, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(details.detail.explanation).isNull()

        details = detailsForSlice(slice4, response.detailMap[1])
        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
        assertThat(details.detail.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseSeriesSplitVersionAnyContinous(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        ).toDO()
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
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
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2), SliceTypeDO.ANY)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result).isTrue()
        assertThat(response.merge[0].slices).hasSize(1)

        assertThat(response.merge[0].slices[0]).isEqualTo(
            SliceDO(
                listOf(
                    SlicingPropertyDO(
                        "series",
                        PropertyTypeDO.ENUM,
                        PropertyRangeDO(enumValues = setOf("S1", "S2"))
                    )
                )
            )
        )

        assertThat(response.detailMap).hasSize(1)
        assertThat(response.detailMap[1]).hasSize(2)

        //TODO fix details
//        var details = detailsForSlice(SliceDO(listOf()), response.detailMap[0])
//        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(4)
//        assertThat(details.detail.explanation).isNull()
//
//        details = detailsForSlice(slice2, response.detailMap[1])
//        assertThat(details.detail.exampleConfiguration!!.size).isEqualTo(5)
//        assertThat(details.detail.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseSeriesAndVersionAnyContinous(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
                Pair(EnumProperty("series", "S1"), SliceType.ANY)
            )
        ).toDO()
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ANY
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2), SliceTypeDO.ANY)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result).isTrue()
        assertThat(response.merge[0].slices).hasSize(1)

        assertThat(response.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(response.detailMap).hasSize(1)
        assertThat(response.detailMap[1]).hasSize(1)

        assertThat(response.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(response.detailMap[1]!![0].detail.exampleConfiguration!!.size).isEqualTo(4)
        assertThat(response.detailMap[1]!![0].detail.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseSeriesAndVersionAllContinous(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ALL
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2), SliceTypeDO.ALL)
        )
        val request = ConsistencyRequest("any", sliceSelection, listOf(), true)

        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge).hasSize(1)
        assertThat(response.merge[0].result).isFalse()
        assertThat(response.merge[0].slices).hasSize(1)

        assertThat(response.merge[0].slices[0]).isEqualTo(SliceDO(listOf()))

        assertThat(response.detailMap).hasSize(1)
        assertThat(response.detailMap[1]).hasSize(1)

        assertThat(response.detailMap[1]!![0].slice).isEqualTo(SliceDO(listOf()))
        assertThat(response.detailMap[1]!![0].detail.exampleConfiguration).isNull()
        assertThat(response.detailMap[1]!![0].detail.explanation).hasSize(4)
    }
}
