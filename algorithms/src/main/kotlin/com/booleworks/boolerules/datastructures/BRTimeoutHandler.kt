// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.datastructures

data class BRTimeoutHandler(
    val computationEnd: Long
) {
    private var aborted = false

    fun shouldContinue(): Boolean {
        if (aborted) {
            return false
        }
        aborted = System.currentTimeMillis() > computationEnd
        return !aborted
    }

    companion object {
        fun fromDuration(durationInSeconds: Int) =
            BRTimeoutHandler(System.currentTimeMillis() + durationInSeconds * 1000)
    }
}
