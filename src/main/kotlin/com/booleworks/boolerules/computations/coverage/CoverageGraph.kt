// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.coverage

import com.booleworks.boolerules.computations.details.DetailRequest
import com.booleworks.boolerules.computations.details.fetchModel
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.persistence.Persistence
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.logging.error
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

private val logger: Logger = LoggerFactory.getLogger("CoverageGraphApi")

fun Route.coverageGraphRoute() {
    post(COVERAGE.path + "/graph", {
        summary = "Compute the coverage graph"
        description = "Computes the maximum number of constraints which can be covered per configuration-count"
        tags = listOf(COVERAGE.docs.computation)
        request {
            body<DetailRequest> {
                required = true
                description = "The request for coverage graph"
                mediaType(ContentType.Application.Json)
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Successful Request"
                body<CoverageGraphResponse> {
                    mediaType(ContentType.Application.Json)
                    description = "The coverage graph response"
                }
            }
            HttpStatusCode.NotFound to { description = "Job ID not found" }
            "5XX" to { description = "Server-side problem" }
        }
    }) {
        val request = call.receive<DetailRequest>()
        measureTime {
            try {
                val result = computeCoverageGraphResponse(request)
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

@Schema(description = "The content of a coverage graph")
data class CoverageGraphResponse(

    @field:Schema(description = "The coverable constraints per number of configurations")
    val coverableConstraints: List<CoverableConstraints>
)

@Schema(description = "The maximum coverable constraints for the given number of configurations")
data class CoverableConstraints(

    @field:Schema(description = "The number of configurations")
    val numberOfConfigurations: Int,
    @field:Schema(description = "The maximum number of coverable constraints for the given number of configurations")
    val maxCoverableConstraints: Int,
)

private fun computeCoverageGraphResponse(request: DetailRequest): CoverageGraphResponse {
    val computationDetail = Persistence.computation.fetchDetail(
        request.jobId, SliceDO.fromSelection(request.sliceSelection), request.elementId, COVERAGE
    ).getOrThrow()
    val model = fetchModel(request.jobId)
    val sliceSelection = request.sliceSelection.map { it.toModelDS() }
    val mainRequest = Persistence.computation.fetchConstraints(request.jobId, COVERAGE).getOrThrow()
    return CoverageComputation.computeCoverageGraph(
        model,
        sliceSelection,
        mainRequest,
        computationDetail.detail.configurations.size
    )
}
