// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export

import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTimedValue

private val logger: Logger = LoggerFactory.getLogger("ExportApi")

fun Route.exportRoute() {
    get("/excel/{uuid}", {
        summary = "Export an existing result to Excel"
        description = "Generates a result Excel file for the given computation ID"
        tags = listOf("Result Export")
        request {
            pathParameter<String>("uuid") {
                required = true
                description = "The UUID of the job."
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Successful Request"
                body<ByteArray> {
                    mediaType(ContentType.Application.OctetStream)
                    description = "The Excel result file"
                }
            }
            HttpStatusCode.NotFound to { description = "Job ID not found" }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        val uuid = call.parameters["uuid"]
        measureTimedValue {
            if (uuid != null) getExcelExport(uuid) else null
        }.let {
            if (it.value != null) {
                call.respond(HttpStatusCode.OK, it.value!!)
                logger.info("Returned excel result for key '$uuid' (${it.duration})")
            } else {
                logger.error("No result for job ID '$uuid'")
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
