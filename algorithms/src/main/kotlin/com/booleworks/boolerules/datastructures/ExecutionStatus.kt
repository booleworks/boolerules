// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.datastructures

import java.util.*

data class ExecutionStatus(
    val success: Boolean,
    val statistics: ExecutionStatistics,
    val errors: List<String> = mutableListOf(),
    val warnings: List<String> = mutableListOf(),
    val infos: List<String> = mutableListOf(),
)

data class ExecutionStatistics(
    val computationTimeInMs: Long,
    val numberOfSlices: Int,
    val numberOfSliceComputations: Int,
    val averageSliceComputationTimeInMs: Long
)

@Suppress("FunctionName")
internal fun ExecutionStatus(timeoutHandler: BRTimeoutHandler) = ExecutionStatusBuilder(timeoutHandler)

class ExecutionStatusBuilder(
    internal val timeoutHandler: BRTimeoutHandler
) {
    private val startTime = System.currentTimeMillis()
    private val errors: MutableList<String> = Collections.synchronizedList(ArrayList())
    private val warnings: MutableList<String> = Collections.synchronizedList(ArrayList())
    private val infos: MutableList<String> = Collections.synchronizedList(ArrayList())

    internal var numberOfSlices: Int = 0
    internal var numberOfSliceComputations: Int = 0

    fun addError(message: String) {
        errors.add(message)
    }

    fun addWarning(message: String) {
        warnings.add(message)
    }

    fun addInfo(message: String) {
        infos.add(message)
    }

    fun successful() = errors.isEmpty()

    fun build(): ExecutionStatus {
        val computationTime = System.currentTimeMillis() - startTime
        val avgTime = if (numberOfSliceComputations > 0) computationTime / numberOfSliceComputations else 0
        val statistics = ExecutionStatistics(computationTime, numberOfSlices, numberOfSliceComputations, avgTime)
        return ExecutionStatus(
            errors.isEmpty(),
            statistics,
            errors,
            warnings,
            infos
        )
    }

    override fun toString() = "ComputationStatusBuilder(errors=$errors, warnings=$warnings, infos=$infos)"
}

