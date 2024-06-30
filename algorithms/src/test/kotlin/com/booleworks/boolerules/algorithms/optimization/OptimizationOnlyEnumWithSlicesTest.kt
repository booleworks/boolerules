package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.algorithms.AlgorithmExecutor
import com.booleworks.boolerules.algorithms.OptimizationType
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

internal class OptimizationOnlyEnumWithSlicesTest : TestWithConfig() {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    private val slice1 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val slice2 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 1), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )
    private val slice3 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
        )
    )
    private val slice4 = Slice.of(
        mapOf(
            Pair(IntProperty("version", 2), SliceType.SPLIT),
            Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
        )
    )

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
        val algo = Optimization(weightings, OptimizationType.MIN)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(result).hasSize(4)

        val res25 = result.values.first { it.weight == 25 }
        assertThat(res25.weight).isEqualTo(25)
        assertThat(res25.slice).isEqualTo(slice1)
        assertThat(res25.featureModel!!.size).isEqualTo(4)
        assertThat(res25.featureModel.toString()).isEqualTo("a=a2, b=b1, c=c1, p=p1")

        val res23 = result.values.first { it.weight == 23 }
        assertThat(res23.weight).isEqualTo(23)
        assertThat(res23.slice).isEqualTo(slice2)
        assertThat(res23.featureModel!!.size).isEqualTo(5)
        assertThat(res23.featureModel.toString()).isEqualTo("a=a2, b=b1, c=c1, p=p2, q=q1")

        val res30 = result.values.first { it.weight == 30 }
        assertThat(res30.weight).isEqualTo(30)
        assertThat(res30.slice).isEqualTo(slice3)
        assertThat(res30.featureModel!!.size).isEqualTo(4)
        assertThat(res30.featureModel.toString()).isEqualTo("a=a2, b=b2, c=c1, p=p1")

        val res28 = result.values.first { it.weight == 28 }
        assertThat(res28.weight).isEqualTo(28)
        assertThat(res28.slice).isEqualTo(slice4)
        assertThat(res28.featureModel!!.size).isEqualTo(5)
        assertThat(res28.featureModel.toString()).isEqualTo("a=a2, b=b2, c=c1, p=p2, q=q1")
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllSplitDiscreteMax(tc: TestConfig) {
        setUp(tc)

        val algo = Optimization(weightings, OptimizationType.MAX)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(result).hasSize(4)

        val res27 = result.values.first { it.weight == 27 }
        assertThat(res27.weight).isEqualTo(27)
        assertThat(res27.slice).isEqualTo(slice1)
        assertThat(res27.featureModel!!.size).isEqualTo(4)
        assertThat(res27.featureModel.toString()).isEqualTo("a=a2, b=b1, c=c2, p=p1")

        val res25 = result.values.first { it.weight == 25 }
        assertThat(res25.weight).isEqualTo(25)
        assertThat(res25.slice).isEqualTo(slice2)
        assertThat(res25.featureModel!!.size).isEqualTo(5)
        assertThat(res25.featureModel.toString()).isEqualTo("a=a2, b=b1, c=c2, p=p2, q=q1")

        val res34 = result.values.first { it.weight == 34 }
        assertThat(res34.weight).isEqualTo(34)
        assertThat(res34.slice).isEqualTo(slice3)
        assertThat(res34.featureModel!!.size).isEqualTo(4)
        assertThat(res34.featureModel.toString()).isEqualTo("a=a2, b=b2, c=c3, p=p1")

        val res32 = result.values.first { it.weight == 32 }
        assertThat(res32.weight).isEqualTo(32)
        assertThat(res32.slice).isEqualTo(slice4)
        assertThat(res32.featureModel!!.size).isEqualTo(5)
        assertThat(res32.featureModel.toString()).isEqualTo("a=a2, b=b2, c=c3, p=p2, q=q1")
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllAnyMin(tc: TestConfig) {
        setUp(tc)
        val anySlice = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.ANY),
                Pair(EnumProperty("series", "S2"), SliceType.ANY)
            )
        )
        val algo = Optimization(weightings, OptimizationType.MIN)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.ANY),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.ANY)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(result).hasSize(1)
        val res23 = result.values.first { it.weight == 23 }
        assertThat(res23.weight).isEqualTo(23)
        assertThat(res23.slice).isEqualTo(anySlice)
        assertThat(res23.featureModel!!.size).isEqualTo(5)
        assertThat(res23.featureModel.toString()).isEqualTo("a=a2, b=b1, c=c1, p=p2, q=q1")
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseMinAllAnyMax(tc: TestConfig) {
        setUp(tc)
        val anySlice = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.ANY),
                Pair(EnumProperty("series", "S1"), SliceType.ANY)
            )
        )
        val algo = Optimization(weightings, OptimizationType.MAX)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.ANY),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.ANY)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(result).hasSize(1)
        val res34 = result.values.first { it.weight == 34 }
        assertThat(res34.weight).isEqualTo(34)
        assertThat(res34.slice).isEqualTo(anySlice)
        assertThat(res34.featureModel!!.size).isEqualTo(4)
        assertThat(res34.featureModel.toString()).isEqualTo("a=a2, b=b2, c=c3, p=p1")
    }
}
