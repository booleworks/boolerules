package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.algorithms.AlgorithmExecutor
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.configuration.BackboneType.*
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.ExecutionStatusBuilder
import com.booleworks.boolerules.datastructures.FeatureInstance
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.slices.EnumSliceSelection
import com.booleworks.prl.model.slices.IntSliceSelection
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class BackboneOnlyEnumWithSlicesTest : TestWithConfig() {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(5)
    }

    @Test
    fun testComputeForSlice() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())

        val info = modelTranslation[0].info
        val algo = Backbone(listOf())
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)


        assertThat(result.slice).isEqualTo(Slice.empty())
        assertThat(result.backbone).hasSize(9)
        assertThat(result.backbone[FeatureInstance.enum("a", "a1")]).isEqualTo(FORBIDDEN)
        assertThat(result.backbone[FeatureInstance.enum("a", "a2")]).isEqualTo(MANDATORY)
        assertThat(result.backbone[FeatureInstance.enum("b", "b1")]).isEqualTo(MANDATORY)
        assertThat(result.backbone[FeatureInstance.enum("b", "b2")]).isEqualTo(FORBIDDEN)
        assertThat(result.backbone[FeatureInstance.enum("b", "b3")]).isEqualTo(FORBIDDEN)
        assertThat(result.backbone[FeatureInstance.enum("c", "c1")]).isEqualTo(OPTIONAL)
        assertThat(result.backbone[FeatureInstance.enum("c", "c2")]).isEqualTo(OPTIONAL)
        assertThat(result.backbone[FeatureInstance.enum("p", "px")]).isEqualTo(FORBIDDEN)
        assertThat(result.backbone[FeatureInstance.enum("p", "p1")]).isEqualTo(MANDATORY)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitSlices(tc: TestConfig) {
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

        val algo = Backbone(listOf())
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(9)
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(11)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesAndVersionSplitSlicesWithRestrictedVars(tc: TestConfig) {
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

        val algo = Backbone(listOf(enumFt("a"), enumFt("b"), enumFt("c")))
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!
        val sliceResult3 = result[slice3]!!
        val sliceResult4 = result[slice4]!!

        assertThat(result).hasSize(4)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(7)
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(7)
        assertThat(sliceResult3.slice).isEqualTo(slice3)
        assertThat(sliceResult3.backbone).hasSize(8)
        assertThat(sliceResult4.slice).isEqualTo(slice4)
        assertThat(sliceResult4.backbone).hasSize(8)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitSlicesWithAdditionalConstraints(tc: TestConfig) {
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

        val algo = Backbone(listOf())
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf("[c = \"c1\"]"), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(9)
        assertThat(sliceResult1.backbone.keys).containsExactly(
            FeatureInstance.enum("a", "a2"),
            FeatureInstance.enum("b", "b1"),
            FeatureInstance.enum("c", "c1"),
            FeatureInstance.enum("p", "p1"),
            FeatureInstance.enum("a", "a1"),
            FeatureInstance.enum("b", "b2"),
            FeatureInstance.enum("b", "b3"),
            FeatureInstance.enum("c", "c2"),
            FeatureInstance.enum("p", "px")
        )
        assertThat(sliceResult1.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN
        )
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(11)
        assertThat(sliceResult2.backbone.keys).containsExactly(
            FeatureInstance.enum("a", "a2"),
            FeatureInstance.enum("b", "b1"),
            FeatureInstance.enum("c", "c1"),
            FeatureInstance.enum("p", "p2"),
            FeatureInstance.enum("a", "a1"),
            FeatureInstance.enum("b", "b2"),
            FeatureInstance.enum("b", "b3"),
            FeatureInstance.enum("c", "c2"),
            FeatureInstance.enum("p", "px"),
            FeatureInstance.enum("q", "q1"),
            FeatureInstance.enum("q", "q2")
        )
        assertThat(sliceResult2.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            OPTIONAL,
            OPTIONAL
        )
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitSlicesWithRestrictedVarsWithAdditionalConstraints(tc: TestConfig) {
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

        val algo = Backbone(listOf(enumFt("a"), enumFt("b"), enumFt("c")))
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf("[c = \"c1\"]"), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(7)
        assertThat(sliceResult1.backbone.keys).containsExactly(
            FeatureInstance.enum("a", "a2"),
            FeatureInstance.enum("b", "b1"),
            FeatureInstance.enum("c", "c1"),
            FeatureInstance.enum("a", "a1"),
            FeatureInstance.enum("b", "b2"),
            FeatureInstance.enum("b", "b3"),
            FeatureInstance.enum("c", "c2")
        )
        assertThat(sliceResult1.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN
        )
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(7)
        assertThat(sliceResult2.backbone.keys).containsExactly(
            FeatureInstance.enum("a", "a2"),
            FeatureInstance.enum("b", "b1"),
            FeatureInstance.enum("c", "c1"),
            FeatureInstance.enum("a", "a1"),
            FeatureInstance.enum("b", "b2"),
            FeatureInstance.enum("b", "b3"),
            FeatureInstance.enum("c", "c2")
        )
        assertThat(sliceResult2.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN
        )
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitVersionAllSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))

        val algo = Backbone(listOf())
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.interval(1, 2), SliceType.ALL)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.state).isEqualTo(ComputationState.notBuildable())
        assertThat(sliceResult1.backbone).isEmpty()
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.state).isEqualTo(ComputationState.notBuildable())
        assertThat(sliceResult2.backbone).isEmpty()
    }
}
