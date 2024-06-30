package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.algorithms.AlgorithmExecutor
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.ExecutionStatusBuilder
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
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
import com.booleworks.prl.transpiler.mergeSlices
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ConsistencyAlgorithmOnlyIntWithSlicesTest : TestWithConfig() {
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
        val info = modelTranslation[0].info
        val mergedInfo = mergeSlices(cf, modelTranslation.computations).info
        val algo = Consistency(false)
        val handler = BRTimeoutHandler.fromDuration(60)
        val resultTrue = algo.executeForSlice(cf, model, info, Slice.empty(), handler)
        val resultFalse = algo.executeForSlice(cf, model, mergedInfo, Slice.empty(), handler)

        assertThat(resultTrue.consistent).isTrue
        assertThat(resultTrue.explanation).isNull()
        assertThat(resultTrue.exampleConfiguration).isNull()
        assertThat(resultTrue.slice).isEqualTo(Slice.empty())

        assertThat(resultFalse.consistent).isFalse
        assertThat(resultFalse.explanation).isNull()
        assertThat(resultFalse.exampleConfiguration).isNull()
        assertThat(resultFalse.slice).isEqualTo(Slice.empty())
    }

    @Test
    fun testComputeForSliceWithExplanation() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info
        val mergedInfo = mergeSlices(cf, modelTranslation.computations).info
        val algo = Consistency(true)
        val handler = BRTimeoutHandler.fromDuration(60)
        val resultTrue = algo.executeForSlice(cf, model, info, Slice.empty(), handler)
        val resultFalse = algo.executeForSlice(cf, model, mergedInfo, Slice.empty(), handler)

        assertThat(resultTrue.consistent).isTrue()
        assertThat(resultTrue.explanation).isNull()
        assertThat(resultTrue.exampleConfiguration).isNotNull
        assertThat(resultTrue.slice).isEqualTo(Slice.empty())

        assertThat(resultFalse.consistent).isFalse()
        assertThat(resultFalse.slice).isEqualTo(Slice.empty())
        assertThat(resultFalse.explanation!!.rules).hasSize(4)
        assertThat(resultFalse.exampleConfiguration).isNull()
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

        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2, 3), SliceType.ANY)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(anySlice1)
        assertThat(sliceResult1.consistent).isTrue
        assertThat(sliceResult1.exampleConfiguration!!.size).isEqualTo(3)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(anySlice2)
        assertThat(sliceResult2.consistent).isTrue
        assertThat(sliceResult2.exampleConfiguration!!.size).isEqualTo(3)
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

        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.ANY),
            IntSliceSelection("version", IntRange.interval(1, 2), SliceType.ANY)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[Slice.empty()]!!
        assertThat(result).hasSize(1)
        assertThat(sliceResult1.slice).isEqualTo(anySlice1)
        assertThat(sliceResult1.consistent).isTrue
        assertThat(sliceResult1.exampleConfiguration!!.size).isEqualTo(3)
        assertThat(sliceResult1.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersion12AllSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.ALL)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.consistent).isTrue

        assertThat(sliceResult1.exampleConfiguration!!.size).isEqualTo(3)
        assertThat(sliceResult1.explanation).isNull()

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isTrue

        assertThat(sliceResult2.exampleConfiguration!!.size).isEqualTo(3)
        assertThat(sliceResult2.explanation).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersion23AllSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(2, 3), SliceType.ALL)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.consistent).isFalse

        assertThat(sliceResult1.exampleConfiguration).isNull()
        assertThat(sliceResult1.explanation!!.rules).hasSize(4)

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isFalse

        assertThat(sliceResult2.exampleConfiguration).isNull()
        assertThat(sliceResult2.explanation!!.rules).hasSize(3)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesSplitVersion123AllSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2, 3), SliceType.ALL)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.consistent).isFalse

        assertThat(sliceResult1.exampleConfiguration).isNull()
        assertThat(sliceResult1.explanation!!.rules).hasSize(4)

        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.consistent).isFalse

        assertThat(sliceResult2.exampleConfiguration).isNull()
        assertThat(sliceResult2.explanation!!.rules).hasSize(3)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationSeriesAndVersionAllSlices(tc: TestConfig) {
        setUp(tc)
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.ALL),
            IntSliceSelection("version", IntRange.list(1, 2, 3), SliceType.ALL)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)
        val sliceResult1 = result[Slice.empty()]!!

        assertThat(result).hasSize(1)

        assertThat(sliceResult1.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult1.consistent).isFalse
        assertThat(sliceResult1.exampleConfiguration).isNull()
        assertThat(sliceResult1.explanation!!.rules).hasSize(4)
    }
}
