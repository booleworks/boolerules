// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.config

import com.booleworks.boolerules.service.ServiceEnv

data object ComputationConfig {
    var numThreads = 1
        private set

    fun setFromEnvironment(env: ServiceEnv) {
        env.numThreads?.toInt()?.let { numThreads = it }
    }

    fun setFromValues(numThreads: Int? = null) {
        numThreads?.let { ComputationConfig.numThreads = it }
    }

    override fun toString() = "ComputationConfig(numThreads=$numThreads)"
}
