// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.rulefile

import io.github.smiley4.ktorswaggerui.dsl.BodyTypeDescriptor
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTimedValue

val logger: Logger = LoggerFactory.getLogger("rulefile")

fun Route.rulefileRoute() {
    get("", {
        summary = "Get all stored rule files"
        description = "Return all upload summaries of uploaded rule files"
        tags = listOf("Rule File Management")
        response {
            HttpStatusCode.OK to {
                description = "Successful Request"
                body<List<UploadSummaryDO>> { description = "The list of rule files summaries" }
            }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        measureTimedValue {
            getAllSummaries()
        }.let {
            call.respond(HttpStatusCode.OK, it.value)
            logger.info("Returned ${it.value.size} rule file summaries (${it.duration})")
        }
    }

    get("/{uuid}", {
        summary = "Get a stored compiled rule file"
        description = "Return the compiled rule file for the given ID"
        tags = listOf("Rule File Management")
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
                    description = "The compiled rule file"
                }
            }
            HttpStatusCode.NotFound to { description = "Rule file for ID not found" }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        val uuid = call.parameters["uuid"]
        measureTimedValue {
            if (uuid == null) null else getRuleFile(uuid)
        }.let {
            if (it.value != null) {
                call.respond(HttpStatusCode.OK, it.value!!)
                logger.info("Returned rule file for key '$uuid' (${it.duration})")
            } else {
                logger.error("No rule file for key '$uuid'")
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    post({
        summary = "Upload a PRL file"
        description = "Uploads a PRL file which will be compiled and stored in the application persistence layer"
        tags = listOf("Rule File Management")
        request {
            multipartBody {
                required = true
                description = "The PRL file"
                mediaType(ContentType.MultiPart.FormData)
                part("prl-file", BodyTypeDescriptor.custom("prl-file"))
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Successful Upload"
                body<UploadSummaryDO> { description = "The upload summary of the uploaded rule file" }
            }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        measureTimedValue {
            val multipartData = call.receiveMultipart()
            var summary: UploadSummaryDO? = null
            multipartData.forEachPart { part ->
                if (summary == null && part is PartData.FileItem) {
                    val us = storeRuleFile(part.originalFileName as String, part.streamProvider().readBytes())
                    call.respond(if (us.hasErrors()) HttpStatusCode.BadRequest else HttpStatusCode.OK, us)
                    summary = us
                }
                part.dispose()
            }
            summary
        }.let {
            logger.info("Uploaded PRL file '${it.value}' (${it.duration})")
        }
    }

    delete("/{uuid}", {
        summary = "Delete a rule file"
        description = "Deletes a rule file for the given ID"
        tags = listOf("Rule File Management")
        request {
            pathParameter<String>("uuid") {
                required = true
                description = "The UUID of the job."
            }
        }
        response {
            HttpStatusCode.OK to {
                body<String> {
                    description = "Flag whether the file was deleted or not"
                    mediaType(ContentType.Text.Plain)
                }
            }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        val uuid = call.parameters["uuid"]
        measureTimedValue {
            val del = if (uuid != null) deleteRuleFile(uuid) else false
            call.respond(HttpStatusCode.OK, del.toString())
            del
        }.let {
            logger.info(
                if (it.value) {
                    "Deleted rule file with key '$uuid' (${it.duration})"
                } else {
                    "Nothing to delete for key '$uuid'"
                }
            )
        }
    }
}
