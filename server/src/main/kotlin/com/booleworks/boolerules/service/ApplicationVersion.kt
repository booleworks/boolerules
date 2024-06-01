// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.service

import java.util.*

data class ApplicationVersion(val version: String?, val buildDate: String?) {
    override fun toString() = "version=$version (buildDate=$buildDate)"

    companion object {
        fun get() = ApplicationVersion(BuildVersion.get().version, BuildVersion.get().buildDate).toString()
    }
}

data class BuildVersion(val version: String?, val buildDate: String?) {
    companion object {
        fun get(): BuildVersion {
            val props = BuildVersion::class.java.getResourceAsStream("version.txt").use {
                Properties().apply { load(it) }
            }
            return BuildVersion(props["version"] as? String, props["build.date"] as? String)
        }
    }
}
