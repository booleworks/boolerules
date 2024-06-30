package com.booleworks.boolerules.algorithms.configuration

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
import com.booleworks.prl.transpiler.RuleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ConsistencyAlgorithmOnlyBooleanWithSlicesTest : TestWithConfig() {

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

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(9)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithExplanation(tc: TestConfig) {
        setUp(tc)
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2", "S3"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.interval(1, 3), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf(), listOf(), status)

        assertThat(result).hasSize(9)
        result.values.forEach {
            assertThat(it.consistent).isTrue
            assertThat(it.exampleConfiguration!!.features).hasSize(1)
        }
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.interval(1, 2), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf("-b"), listOf(), status)

        assertThat(result).hasSize(4)
        assertThat(result[s11]!!.consistent).isTrue
        assertThat(result[s21]!!.consistent).isTrue
        assertThat(result[s11]!!.exampleConfiguration!!.features).hasSize(2)
        assertThat(result[s21]!!.exampleConfiguration!!.features).hasSize(2)
        assertThat(result[s12]!!.consistent).isFalse
        assertThat(result[s22]!!.consistent).isFalse
        assertThat(result[s12]!!.exampleConfiguration).isNull()
        assertThat(result[s22]!!.exampleConfiguration).isNull()
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithExplanationWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf("-b", "-c"), listOf(), status)

        assertThat(result).hasSize(2)
        val sliceResult1 = result[s11]!!
        assertThat(sliceResult1.consistent).isFalse

        assertThat(sliceResult1.explanation!!.rules).hasSize(4)
        assertThat(sliceResult1.explanation.rules.filter { it.backpack().ruleType == RuleType.ORIGINAL_RULE })
            .hasSize(2)
        assertThat(sliceResult1.explanation.rules.filter { it.backpack().ruleType == RuleType.ADDITIONAL_RESTRICTION })
            .hasSize(2)

        val sliceResult2 = result[s21]!!
        assertThat(sliceResult2.consistent).isFalse
        assertThat(sliceResult2.explanation!!.rules.filter { it.backpack().ruleType == RuleType.ORIGINAL_RULE })
            .hasSize(2)
        assertThat(sliceResult2.explanation.rules.filter { it.backpack().ruleType == RuleType.ADDITIONAL_RESTRICTION })
            .hasSize(2)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutExplanationWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val algo = Consistency(false)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val sliceSelection = mutableListOf(
            EnumSliceSelection("series", EnumRange.list("S1", "S2"), SliceType.SPLIT),
            IntSliceSelection("version", IntRange.list(1), SliceType.SPLIT)
        )
        val result = exec.executeForModel(model, sliceSelection, listOf("-b", "-c"), listOf(), status)

        assertThat(result).hasSize(2)
        val sliceResult1 = result[s11]!!
        assertThat(sliceResult1.consistent).isFalse
        assertThat(sliceResult1.explanation).isNull()
        val sliceResult2 = result[s21]!!
        assertThat(sliceResult2.consistent).isFalse
        assertThat(sliceResult2.explanation).isNull()
    }
}
