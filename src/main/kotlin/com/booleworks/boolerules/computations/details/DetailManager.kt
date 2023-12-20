// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.details

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.consistency.CONSISTENCY
import com.booleworks.boolerules.computations.consistency.ConsistencyComputation
import com.booleworks.boolerules.computations.consistency.ConsistencyDetail
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.minmaxconfig.MINMAXCONFIG
import com.booleworks.boolerules.computations.minmaxconfig.MinMaxConfigDetail
import com.booleworks.boolerules.computations.optimization.OPTIMIZATION
import com.booleworks.boolerules.computations.optimization.OptimizationDetail
import com.booleworks.boolerules.persistence.Persistence
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.protobuf.ProtoBufModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

private val logger: Logger = LoggerFactory.getLogger("DetailManager")

fun computeDetailResponse(
    request: DetailRequest,
    computationType: ComputationType<*, *, *, *, *>
): DetailResponse<*, *, *> =
    when (computationType) {
        CONSISTENCY -> computeConsistencyDetail(request)
        MINMAXCONFIG -> computeMinMaxDetail(request)
        OPTIMIZATION -> computeOptimizationDetail(request)
        else -> error("Cannot compute details for computation without details")
    }

private fun computeConsistencyDetail(request: DetailRequest): DetailResponse<NoElement, Boolean, ConsistencyDetail> {
    var computationDetail = Persistence.computation.fetchDetail(
        request.jobId, SliceDO.fromSelection(request.sliceSelection), request.elementId, CONSISTENCY
    ).getOrThrow()

    val mainResult = Persistence.computation.fetchMainResult(
        request.jobId, computationDetail.resultId, CONSISTENCY
    ).getOrThrow()

    if (!mainResult.result && computationDetail.detail.explanation == null) {
        logger.info("Did not find consistency details for $request, computing...")
        val model = fetchModel(request.jobId)
        val sliceSelection = request.sliceSelection.map { it.toModelDS() }
        val consistencyReq = Persistence.computation.fetchConstraints(request.jobId, CONSISTENCY).getOrThrow()
        require(!consistencyReq.hasAnyOrAllSplits()) {
            "Currently explanations for ANY or ALL slices are not supported."
        }
        val constraints = consistencyReq.additionalConstraints
        val splitProperties = consistencyReq.splitProperties()
        computationDetail = ConsistencyComputation.computeDetail(model, sliceSelection, constraints, splitProperties)
    }
    return DetailResponse(null, mainResult, computationDetail)
}

private fun computeMinMaxDetail(request: DetailRequest): DetailResponse<NoElement, Int, MinMaxConfigDetail> {
    val computationDetail =
        Persistence.computation.fetchDetail(
            request.jobId,
            SliceDO.fromSelection(request.sliceSelection),
            request.elementId,
            MINMAXCONFIG
        ).getOrThrow()
    val mainResult =
        Persistence.computation.fetchMainResult(request.jobId, computationDetail.resultId, MINMAXCONFIG).getOrThrow()
    return DetailResponse(null, mainResult, computationDetail)
}

private fun computeOptimizationDetail(request: DetailRequest): DetailResponse<NoElement, Int, OptimizationDetail> {
    val computationDetail =
        Persistence.computation.fetchDetail(
            request.jobId,
            SliceDO.fromSelection(request.sliceSelection),
            request.elementId,
            OPTIMIZATION
        ).getOrThrow()
    val mainResult =
        Persistence.computation.fetchMainResult(request.jobId, computationDetail.resultId, OPTIMIZATION).getOrThrow()
    return DetailResponse(null, mainResult, computationDetail)
}

private fun fetchModel(jobId: String): PrlModel {
    val status = Persistence.computation.fetchStatus(jobId).getOrThrow()
    val binZipped = Persistence.rulefile.getBinaryRuleFile(status.ruleFileId)
    return deserialize(
        ProtoBufModel.PbModel.newBuilder().mergeFrom(GZIPInputStream(ByteArrayInputStream(binZipped.getOrThrow())))
            .build()
    )
}
