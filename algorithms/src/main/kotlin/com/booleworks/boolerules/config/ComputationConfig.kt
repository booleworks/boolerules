// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.config

data object ComputationConfig {
    var numThreads = 1
        private set

    fun setFromValues(numThreads: Int? = null) {
        numThreads?.let { ComputationConfig.numThreads = it }
    }

    override fun toString() = "ComputationConfig(numThreads=$numThreads)"
}
