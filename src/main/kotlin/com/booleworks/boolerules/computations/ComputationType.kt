// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations

import com.booleworks.boolerules.computations.backbone.BACKBONE
import com.booleworks.boolerules.computations.consistency.CONSISTENCY
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.ComputationRunner
import com.booleworks.boolerules.computations.minmaxconfig.MINMAXCONFIG
import com.booleworks.boolerules.computations.modelcount.MODELCOUNT
import com.booleworks.boolerules.computations.modelenumeration.MODELENUMERATION
import com.booleworks.boolerules.computations.optimization.OPTIMIZATION
import com.booleworks.kjobs.control.ComputationResult
import com.booleworks.kjobs.data.Job

interface ComputationType<REQUEST : ComputationRequest, RESPONSE, MAIN, DETAIL : ComputationDetail, ELEMENT> {
    val path: String
    val docs: ApiDocs

    val request: Class<REQUEST>
    val main: Class<MAIN>
    val detail: Class<DETAIL>
    val element: Class<ELEMENT>

    val computationFunction: (Job, REQUEST) -> ComputationResult<RESPONSE>
    val runner: ComputationRunner<REQUEST, *, DETAIL>
}

object NoElement
object NoComputationDetail : ComputationDetail

fun computationTypeFromPath(path: String) = when (path) {
    CONSISTENCY.path      -> CONSISTENCY
    BACKBONE.path         -> BACKBONE
    MODELCOUNT.path       -> MODELCOUNT
    MODELENUMERATION.path -> MODELENUMERATION
    MINMAXCONFIG.path     -> MINMAXCONFIG
    OPTIMIZATION.path     -> OPTIMIZATION
    else                  -> error("Unknown computation path $path")
}
