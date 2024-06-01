// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations

import com.booleworks.boolerules.persistence.Persistence
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTimedValue

val logger: Logger = LoggerFactory.getLogger("computationManagement")

fun Route.computationManagementRoute() {
    delete("/{uuid}", {
        summary = "Delete stored info for a computation"
        description = "Deletes all stored information for a computation with the given ID"
        tags = listOf("Computation Management")
        request {
            pathParameter<String>("uuid") {
                required = true
                description = "The UUID of the computation job."
            }
        }
        response {
            HttpStatusCode.OK to {
                body<String> {
                    description = "The number of deleted entries for the computation"
                    mediaType(ContentType.Text.Plain)
                }
            }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        val uuid = call.parameters["uuid"]
        measureTimedValue {
            if (uuid == null) {
                0
            } else {
                val del = Persistence.computation.deleteComputation(uuid)
                call.respond(HttpStatusCode.OK, del.toString())
                del
            }
        }.let {
            logger.info(
                if (it.value > 0) "Deleted $it entries for computation " +
                        "'$uuid' (${it.duration})" else "Nothing to delete for computation '$uuid'"
            )
        }
    }

    delete("", {
        summary = "Delete stored info for all computations"
        description = "Deletes all stored information for all computations"
        tags = listOf("Computation Management")
        response {
            HttpStatusCode.OK to {
                body<String> {
                    description = "The number of deleted entries for all computations"
                    mediaType(ContentType.Text.Plain)
                }
            }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        measureTimedValue {
            val del = Persistence.computation.deleteAllComputations()
            call.respond(HttpStatusCode.OK, del.toString())
            del
        }.let {
            logger.info(
                if (it.value > 0) {
                    "Deleted ${it.value} entries for all computations (${it.duration})"
                } else {
                    "Nothing to delete for computations"
                }
            )
        }
    }
}
