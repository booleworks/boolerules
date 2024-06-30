package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.algorithms.AlgorithmExecutor
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.ExecutionStatusBuilder
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.RuleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ConsistencyAlgorithmOnlyBooleanNoSlicesTest : TestWithConfig() {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(339)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSatWithExplanation(tc: TestConfig) {
        setUp(tc)

        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val result = exec.executeForModel(model, listOf(), listOf(), listOf(), status)
        val sliceResult = result[Slice.empty()]!!

        assertThat(sliceResult.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult.consistent).isTrue
        assertThat(sliceResult.explanation).isNull()
        assertThat(sliceResult.exampleConfiguration!!.features).isNotEmpty
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeUnsatWithExplanation(tc: TestConfig) {
        setUp(tc)

        val algo = Consistency(true)
        val exec = AlgorithmExecutor(algo)
        val status = ExecutionStatusBuilder(BRTimeoutHandler.fromDuration(60))
        val result = exec.executeForModel(model, listOf(), listOf("hbe", "-(mje/cck)"), listOf(), status)
        val sliceResult = result[Slice.empty()]!!

        assertThat(sliceResult.slice).isEqualTo(Slice.empty())
        assertThat(sliceResult.consistent).isFalse
        assertThat(sliceResult.explanation).isNotNull()
        assertThat(sliceResult.explanation!!.rules).hasSize(3)
        assertThat(sliceResult.explanation.rules.filter { it.backpack().ruleType == RuleType.ADDITIONAL_RESTRICTION })
            .hasSize(2)
        assertThat(sliceResult.exampleConfiguration).isNull()
    }
}
