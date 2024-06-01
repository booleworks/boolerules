// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.kjobs.api.ApiConfigBuilder
import io.github.smiley4.ktorswaggerui.dsl.BodyTypeDescriptor
import io.github.smiley4.ktorswaggerui.dsl.OpenApiRoute
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

interface ApiDocs {
    val computation: String
    val submitDocs: OpenApiRoute.() -> Unit
    val syncDocs: OpenApiRoute.() -> Unit
    val statusDocs: OpenApiRoute.() -> Unit
    val resultDocs: OpenApiRoute.() -> Unit
    val failureDocs: OpenApiRoute.() -> Unit
}

fun ApiConfigBuilder<*>.generateApiDocs(apiDocs: ApiDocs) {
    submitRoute = { block -> post("submit", apiDocs.submitDocs) { block() } }
    syncRoute = { block -> post("synchronous", apiDocs.syncDocs) { block() } }
    statusRoute = { block -> get("status/{uuid}", apiDocs.statusDocs) { block() } }
    resultRoute = { block -> get("result/{uuid}", apiDocs.resultDocs) { block() } }
    failureRoute = { block -> get("failure/{uuid}", apiDocs.failureDocs) { block() } }
}

internal inline fun <reified INPUT, reified OUTPUT> computationDoc(
    computation: String,
    summary: String,
    desc: String
) = object : ApiDocs {
    override val computation = computation
    override val submitDocs: OpenApiRoute.() -> Unit = submitResource<INPUT>(computation, summary, desc)
    override val statusDocs: OpenApiRoute.() -> Unit = statusResource(computation)
    override val failureDocs: OpenApiRoute.() -> Unit = failureResource(computation)
    override val resultDocs: OpenApiRoute.() -> Unit = resultResource<OUTPUT>(computation)
    override val syncDocs: OpenApiRoute.() -> Unit = syncResource<INPUT, OUTPUT>(computation, summary, desc)
}

private const val STATUS_SCHEMA_NAME = "status-schema"

private inline fun <reified INPUT> submitResource(computation: String, summaryDetail: String, desc: String)
        : OpenApiRoute.() -> Unit = resource(computation, "$computation-submit") {
    summary = summaryDetail
    description = desc
    request {
        body<INPUT> {
            description = "The computation request"
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "The job was successfully submitted."
            body<String> {
                description = "The UUID for the submitted job."
                mediaType(ContentType.Text.Plain)
            }
        }
        HttpStatusCode.BadRequest to { description = "The given input could not be parsed." }
        "5XX" to { description = "Server-side problem" }
    }
}

private fun statusResource(computation: String) = resource(computation, "$computation-status") {
    summary = "Get the status of a $computation computation"
    description = "Checks the status of the job with the given UUID."
    uuidPathParam()
    response {
        HttpStatusCode.OK to {
            description = "The job status was successfully returned."
            body(BodyTypeDescriptor.custom(STATUS_SCHEMA_NAME)) {
                mediaType(ContentType.Text.Plain)
            }
        }
        HttpStatusCode.BadRequest to { description = "The given UUID has an invalid format." }
        HttpStatusCode.NotFound to {
            description =
                "The given UUID could not be found. Either you passed a wrong UUID or the job with " +
                        "this UUID was already deleted after some days."
        }
        "5XX" to { description = "Server-side problem" }
    }
}

private inline fun <reified OUTPUT> resultResource(computation: String) = resource(computation, "$computation-result") {
    summary = "Get the result of a $computation computation"
    description =
        "Returns the result of the computation with the given UUID. This resource should only be called after " +
                "the status resource returned `SUCCESS` for this job."
    uuidPathParam()
    response {
        HttpStatusCode.OK to {
            description = "The job result was successfully returned."
            body<OUTPUT> {
                description = "The computation result."
                mediaType(ContentType.Application.Json)
            }
        }
        HttpStatusCode.BadRequest to {
            description = "The given UUID has an invalid format or the job is not in status `SUCCESS`."
        }
        HttpStatusCode.NotFound to {
            description =
                "The given UUID could not be found. Either you passed a wrong UUID or the job with this UUID " +
                        "was already deleted after some days."
        }
        "5XX" to { description = "Server-side problem" }
    }
}

private inline fun <reified INPUT, reified OUTPUT> syncResource(
    computation: String,
    summaryDetail: String,
    desc: String
) =
    resource(computation, "$computation-sync") {
        summary = summaryDetail
        description = desc
        request {
            body<INPUT> {
                description = "The computation request"
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "The job result was successfully returned."
                body<OUTPUT> {
                    description = "The computation result."
                    mediaType(ContentType.Application.Json)
                }
            }
            HttpStatusCode.BadRequest to {
                description = "The given UUID has an invalid format or the job is not in status `SUCCESS`."
            }
            HttpStatusCode.NotFound to {
                description =
                    "The given UUID could not be found. Either you passed a wrong UUID or the job with this " +
                            "UUID was already deleted after some days."
            }
            "5XX" to { description = "Server-side problem" }
        }
    }

private fun failureResource(computation: String) = resource(computation, "$computation-failure") {
    summary = "Get the failure of a $computation computation"
    description = "Checks the failure of the job with the given UUID."
    uuidPathParam()
    response {
        HttpStatusCode.OK to {
            description = "The job failure was successfully returned."
            body<String> {
                description = "The error message."
                mediaType(ContentType.Text.Plain)
            }
        }
        HttpStatusCode.BadRequest to {
            description = "The given UUID has an invalid format or the job is not in status `FAILURE`."
        }
        HttpStatusCode.NotFound to {
            description =
                "The given UUID could not be found. Either you passed a wrong UUID or the job with this UUID " +
                        "was already deleted after some days."
        }
        "5XX" to { description = "Server-side problem" }
    }
}

private fun OpenApiRoute.uuidPathParam() {
    request {
        pathParameter<String>("uuid") {
            required = true
            description = "The UUID of the job."
        }
    }
}

private fun resource(
    tag: String,
    operationId: String,
    block: OpenApiRoute.() -> Unit
): OpenApiRoute.() -> Unit = {
    block().also {
        tags = listOf(tag)
        this.operationId = operationId
    }
}
