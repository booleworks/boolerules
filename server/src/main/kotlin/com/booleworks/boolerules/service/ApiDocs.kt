// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.service

import io.github.smiley4.ktorswaggerui.dsl.PluginConfigDsl

fun PluginConfigDsl.initSwagger() {
    swagger {
        swaggerUrl = "docs"
        forwardRoot = true
    }
    info {
        title = "BooleRules API"
        version = BuildVersion.get().version
        description = "The public API of BooleRules Server"
    }
    server {
        url = "http://localhost:7070"
        description = "Development Server"
    }
    customSchemas {
        json("prl-file") {
            """
            {
              "type": "string",
              "format": "binary"
            }
            """.trimIndent()
        }
    }
}
