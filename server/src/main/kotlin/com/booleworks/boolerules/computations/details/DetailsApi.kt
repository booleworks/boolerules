// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.details

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationElement
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceComputationResult
import com.booleworks.boolerules.computations.generic.SplitComputationDetail
import com.fasterxml.jackson.annotation.JsonInclude
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.logging.error
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

private val logger: Logger = LoggerFactory.getLogger("DetailsApi")

data class DetailRequest(
    val jobId: String,
    val sliceSelection: List<PropertySelectionDO>,
    val elementId: Int?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DetailResponse<ELEMENT, MAIN, DETAIL : ComputationDetail>(
    val element: ComputationElement<ELEMENT>?,
    val mainResult: SliceComputationResult<MAIN>,
    val detail: SplitComputationDetail<DETAIL>
)

fun <ELEMENT, MAIN, DETAIL : ComputationDetail> Route.detailsRoute(
    computationType: ComputationType<*, *, MAIN, DETAIL, ELEMENT>
) {
    post(computationType.path + "/details", {
        summary = "Get the details for a ${computationType.path} computation"
        description = "Returns the details for a single computation and slice"
        tags = listOf(computationType.docs.computation)
        request {
            body<DetailRequest> {
                required = true
                description = "The request for details"
                mediaType(ContentType.Application.Json)
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Successful Request"
                body<DetailResponse<ELEMENT, MAIN, DETAIL>> {
                    mediaType(ContentType.Application.Json)
                    description = "The computation detail"
                }
            }
            HttpStatusCode.NotFound to { description = "Job ID not found" }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        val request = call.receive<DetailRequest>()
        measureTime {
            try {
                val result = computeDetailResponse(request, computationType)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                logger.error(e)
                call.respond(HttpStatusCode.InternalServerError, e.message!!)
            }
        }.let {
            logger.info("Returned detail result for request $request (${it})")
        }
    }
}
