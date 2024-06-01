// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.persistence.Persistence
import com.booleworks.kjobs.control.ComputationResult
import com.booleworks.kjobs.data.Job
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.protobuf.ProtoBufModel.PbModel
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import kotlin.time.measureTimedValue

sealed class ComputationRunner<REQUEST : ComputationRequest, MAIN, DETAIL : ComputationDetail>(
    open val computation: Computation<REQUEST, MAIN, DETAIL, *>
) {
    internal val logger = LoggerFactory.getLogger(this::class.java)
    internal fun loadRuleFile(ruleFileId: String, status: ComputationStatusBuilder): PrlModel? {
        val binZipped = Persistence.rulefile.getBinaryRuleFile(ruleFileId).getOrElse {
            status.addError("Could not find rulefile with ID $ruleFileId in store")
            return null
        }
        val bin = PbModel.newBuilder().mergeFrom(GZIPInputStream(ByteArrayInputStream(binZipped))).build()
        status.addInfo("Successfully loaded rulefile with ID $ruleFileId from store")
        return deserialize(bin)
    }
}

class SingleComputationRunner<REQUEST : ComputationRequest, MAIN, DETAIL : ComputationDetail>(
    override val computation: SingleComputation<REQUEST, MAIN, DETAIL, *>
) : ComputationRunner<REQUEST, MAIN, DETAIL>(computation) {

    fun compute(job: Job, request: REQUEST): ComputationResult<SingleComputationResponse<MAIN>> {
        val status = ComputationStatus(request.ruleFileId, job.uuid, ComputationVariant.SINGLE)
        computation.logger.info("Computing Job ID ${job.uuid} created ${job.createdAt}")
        val model = loadRuleFile(request.ruleFileId, status)
        return if (model == null) {
            ComputationResult.Success(SingleComputationResponse(status.build(), listOf()))
        } else {
            measureTimedValue {
                val response = try {
                    val res = computation.computeResponse(request, model, status)
                    val sliceMapping = Persistence.computation.storeSliceGroups(status.jobId, status.sliceSets)
                    Persistence.computation.storeDetails(status.jobId, res.detailMap, sliceMapping)
                    SingleComputationResponse(status.build(), res.merge)
                } catch (e: Exception) {
                    logger.error(e.stackTraceToString())
                    status.addError(e.message ?: e.toString())
                    SingleComputationResponse(status.build(), listOf())
                }
                ComputationResult.Success(response)
            }.let {
                status.addInfo("Computed response in ${it.duration}")
                it.value
            }
        }
    }
}

class ListComputationRunner<
        REQUEST : ComputationRequest,
        MAIN,
        DETAIL : ComputationDetail,
        ELEMMAIN : Comparable<ELEMMAIN>,
        ELEMDETAIL : ComputationDetail,
        ELEMENT : Comparable<ELEMENT>>(
    override val computation: ListComputation<REQUEST, MAIN, DETAIL, ELEMMAIN, ELEMDETAIL, *, *, ELEMENT>
) : ComputationRunner<REQUEST, MAIN, DETAIL>(computation) {

    fun compute(job: Job, request: REQUEST): ComputationResult<ListComputationResponse<ELEMMAIN, ELEMENT>> {
        val status = ComputationStatus(request.ruleFileId, job.uuid, ComputationVariant.LIST)
        computation.logger.info("Computing Job ID ${job.uuid} created ${job.createdAt}")
        val model = loadRuleFile(request.ruleFileId, status)
        return if (model == null) {
            ComputationResult.Success(ListComputationResponse(status.build(), listOf()))
        } else {
            measureTimedValue {
                val response = try {
                    val res = computation.computeResponse(request, model, status)
                    val sliceMapping = Persistence.computation.storeSliceGroups(status.jobId, status.sliceSets)
                    val resultPerElement = res.map { (k, v) -> ComputationElementResult(k, v.merge) }
                    res.forEach { (e, r) ->
                        Persistence.computation.storeDetails(status.jobId, r.detailMap, sliceMapping, e.id)
                    }
                    ListComputationResponse(status.build(), resultPerElement)
                } catch (e: Exception) {
                    logger.error(e.stackTraceToString())
                    status.addError(e.message ?: e.toString())
                    ListComputationResponse(status.build(), listOf())
                }
                ComputationResult.Success(response)
            }.let {
                status.addInfo("Computed response in ${it.duration}")
                it.value
            }
        }
    }
}
