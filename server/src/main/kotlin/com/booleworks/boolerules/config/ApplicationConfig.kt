// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.config

import com.booleworks.boolerules.service.ServiceEnv

data object ApplicationConfig {

    var instance = "INSTANCE"
        private set
    var jobCheckInterval = 250
        private set
    var computationTimeout = 15
        private set
    var maxRestarts = 0
        private set
    var syncCheckInterval = 200
        private set
    var syncMaxWaitingTime = 3600
        private set


    fun setFromEnvironment(env: ServiceEnv) {
        instance = env.instance
        env.jobCheckInterval?.toInt()?.let { jobCheckInterval = it }
        env.computationTimeout?.toInt()?.let { computationTimeout = it }
        env.maxRestarts?.toInt()?.let { maxRestarts = it }
        env.syncCheckInterval?.toInt()?.let { syncCheckInterval = it }
        env.syncMaxWaitingTime?.toInt()?.let { syncMaxWaitingTime = it }
    }

    fun setFromValues(
        jobCheckInterval: Int? = null,
        computationTimeout: Int? = null,
        maxRestarts: Int? = null,
        syncCheckInterval: Int? = null,
        syncMaxWaitingTime: Int? = null
    ) {
        jobCheckInterval?.let { ApplicationConfig.jobCheckInterval = it }
        computationTimeout?.let { ApplicationConfig.computationTimeout = it }
        maxRestarts?.let { ApplicationConfig.maxRestarts = it }
        syncCheckInterval?.let { ApplicationConfig.syncCheckInterval = it }
        syncMaxWaitingTime?.let { ApplicationConfig.syncMaxWaitingTime = it }
    }

    override fun toString(): String {
        return "ApplicationConfig(" +
                "jobCheckInterval=$jobCheckInterval, " +
                "computationTimeout=$computationTimeout, " +
                "maxRestarts=$maxRestarts, " +
                "syncCheckInterval=$syncCheckInterval, " +
                "syncMaxWaitingTime=$syncMaxWaitingTime" +
                ")"
    }
}
