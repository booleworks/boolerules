// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.service

import com.booleworks.boolerules.computations.backbone.BACKBONE
import com.booleworks.boolerules.computations.computationManagementRoute
import com.booleworks.boolerules.computations.consistency.CONSISTENCY
import com.booleworks.boolerules.computations.coverage.COVERAGE
import com.booleworks.boolerules.computations.details.detailsRoute
import com.booleworks.boolerules.computations.generic.addComputationApi
import com.booleworks.boolerules.computations.minmaxconfig.MINMAXCONFIG
import com.booleworks.boolerules.computations.modelcount.MODELCOUNT
import com.booleworks.boolerules.computations.modelenumeration.MODELENUMERATION
import com.booleworks.boolerules.computations.optimization.OPTIMIZATION
import com.booleworks.boolerules.config.ApplicationConfig
import com.booleworks.boolerules.config.ComputationConfig
import com.booleworks.boolerules.export.exportRoute
import com.booleworks.boolerules.persistence.Persistence
import com.booleworks.boolerules.rulefile.rulefileRoute
import com.booleworks.kjobs.api.JobFramework
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.EngineMain
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.milliseconds

const val PATH_VERSION: String = "version"
const val PATH_HEALTH: String = "healthz"
const val PATH_RULEFILE: String = "rulefile"
const val PATH_EXPORT: String = "export"
const val PATH_COMPUTATION: String = "computation"

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
        anyHost()
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())  // support java.time.* types
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(SwaggerUI) {
        initSwagger()
    }

    ServiceEnv.read { environment.config.propertyOrNull("ktor.environment.$it")?.getString() }.let {
        ApplicationConfig.setFromEnvironment(it)
        ComputationConfig.setFromEnvironment(it)
        Persistence.setFromEnvironment(it)
    }
    // Delete all existing computation info from persistence when
    // starting the application
    Persistence.computation.deleteAllComputations()

    log.info("Application Config: $ApplicationConfig")
    log.info("Computation Config: $ComputationConfig")

    JobFramework(ApplicationConfig.instance, Persistence.jobs) {
        maintenanceConfig { jobCheckInterval = ApplicationConfig.jobCheckInterval.milliseconds }

        routing {
            // Computations
            route(PATH_COMPUTATION) { computationManagementRoute() }

            route(PATH_COMPUTATION) { addComputationApi(CONSISTENCY, this@route) }
            route(PATH_COMPUTATION) { detailsRoute(CONSISTENCY) }

            route(PATH_COMPUTATION) { addComputationApi(MODELCOUNT, this@route) }
            route(PATH_COMPUTATION) { addComputationApi(MODELENUMERATION, this@route) }
            route(PATH_COMPUTATION) { addComputationApi(BACKBONE, this@route) }

            route(PATH_COMPUTATION) { addComputationApi(MINMAXCONFIG, this@route) }
            route(PATH_COMPUTATION) { detailsRoute(MINMAXCONFIG) }

            route(PATH_COMPUTATION) { addComputationApi(OPTIMIZATION, this@route) }
            route(PATH_COMPUTATION) { detailsRoute(OPTIMIZATION) }

            route(PATH_COMPUTATION) { addComputationApi(COVERAGE, this@route) }
            route(PATH_COMPUTATION) { detailsRoute(COVERAGE) }

            // Rulefile management
            route(PATH_RULEFILE) { rulefileRoute() }
            route(PATH_EXPORT) { exportRoute() }

            // Health and Version
            get(PATH_VERSION, { tags = listOf("Health") }) {
                call.respond(HttpStatusCode.OK, ApplicationVersion.get())
            }

            get(PATH_HEALTH, { tags = listOf("Health") }) {
                call.respond(HttpStatusCode.OK, "Healthy")
            }
        }
    }
}
