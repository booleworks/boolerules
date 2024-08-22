package com.booleworks.boolerules.computations.consistency

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
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

internal class ConsistencyComputationOnlyIntWithSlicesTest : TestWithConfig() {
    private val cut = ConsistencyComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge4.prl"))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(3)
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
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val resultFalse = cut.computeForSlice(
            request,
            Slice.empty(),
            mergedInfo,
            model,
            cf,
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
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )
        val resultFalse = cut.computeForSlice(
            request,
            Slice.empty(),
            mergedInfo,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", SINGLE)
        )

        assertThat(resultTrue.consistent).isTrue()
        assertThat(resultTrue.explanation).isNull()
        assertThat(resultTrue.example).isNotNull
        assertThat(resultTrue.slice).isEqualTo(Slice.empty())

        assertThat(resultFalse.consistent).isFalse()
        assertThat(resultFalse.slice).isEqualTo(Slice.empty())
        assertThat(resultFalse.explanation).isNotEmpty()
        assertThat(resultFalse.example).isNull()
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
                PropertyRangeDO(intValues = setOf(1, 2, 3)),
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
        assertThat(sliceResult1.example!!.size).isEqualTo(3)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(anySlice2)
        assertThat(sliceResult2.consistent).isTrue
        assertThat(sliceResult2.example!!.size).isEqualTo(3)
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
        assertThat(sliceResult1.example!!.size).isEqualTo(3)
        assertThat(sliceResult1.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersion12AllSlices(tc: TestConfig) {
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
        assertThat(sliceResult1.consistent).isTrue

        assertThat(sliceResult1.example!!.size).isEqualTo(3)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isTrue

        assertThat(sliceResult2.example!!.size).isEqualTo(3)
        assertThat(sliceResult2.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersion23AllSlices(tc: TestConfig) {
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
                PropertyRangeDO(intValues = setOf(2, 3)),
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
        assertThat(sliceResult1.explanation).isNotEmpty()

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isFalse

        assertThat(sliceResult2.example).isNull()
        assertThat(sliceResult2.explanation).isNotEmpty()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersion123AllSlices(tc: TestConfig) {
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
                PropertyRangeDO(intValues = setOf(1, 2, 3)),
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
        assertThat(sliceResult1.explanation).isNotEmpty()

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isFalse

        assertThat(sliceResult2.example).isNull()
        assertThat(sliceResult2.explanation).isNotEmpty()
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
                PropertyRangeDO(intValues = setOf(1, 2, 3)),
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
        assertThat(sliceResult1.explanation).isNotEmpty()
    }
}
