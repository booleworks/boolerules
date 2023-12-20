package com.booleworks.boolerules

import com.booleworks.boolerules.config.ComputationConfig

internal data class TestConfig(val numThreads: Int)

internal abstract class TestWithConfig {

    fun setUp(tc: TestConfig) {
        ComputationConfig.setFromValues(tc.numThreads)
    }

    companion object {
        @JvmStatic
        fun configs(): Collection<Array<Any>> = listOf(
            arrayOf(TestConfig(1)),
            arrayOf(TestConfig(2)),
            arrayOf(TestConfig(4))
        )
    }
}
