// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.service

import java.io.IOException

data class ServiceEnv(
    // Application config
    val instance: String,
    val jobCheckInterval: String?,
    val computationTimeout: String?,
    val maxRestarts: String?,
    val syncCheckInterval: String?,
    val syncMaxWaitingTime: String?,

    // Persistence config
    val persistenceType: String?,
    val redisUrl: String?,
    val redisMaxWait: String?,

    // Computation config
    val numThreads: String?,
) {
    companion object {
        fun read(propertyProvider: (String) -> String?) = ServiceEnv(
            propertyProvider("instance") ?: throw IOException("Missing system property: INSTANCE"),
            propertyProvider("jobCheckInterval"),
            propertyProvider("computationTimeout"),
            propertyProvider("maxRestarts"),
            propertyProvider("syncCheckInterval"),
            propertyProvider("syncMaxWaitingTime"),

            propertyProvider("persistenceType"),
            propertyProvider("redisUrl"),
            propertyProvider("redisMaxWait"),

            propertyProvider("numThreads")
        )
    }
}
