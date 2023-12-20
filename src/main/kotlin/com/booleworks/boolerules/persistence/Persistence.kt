// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.persistence

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.ComputationElement
import com.booleworks.boolerules.computations.generic.ComputationElementResult
import com.booleworks.boolerules.computations.generic.ComputationRequest
import com.booleworks.boolerules.computations.generic.ComputationResponse
import com.booleworks.boolerules.computations.generic.ComputationStatus
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.booleworks.boolerules.computations.generic.SliceComputationResult
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SplitComputationDetail
import com.booleworks.boolerules.persistence.PersistenceType.REDIS
import com.booleworks.boolerules.rulefile.UploadSummaryDO
import com.booleworks.boolerules.service.ServiceEnv
import com.booleworks.kjobs.api.persistence.DataPersistence
import com.booleworks.kjobs.api.persistence.JobPersistence
import com.booleworks.kjobs.api.persistence.redis.RedisJobPersistence
import com.booleworks.kjobs.data.Job
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/**
 * Different persistent implementations.
 */
enum class PersistenceType { REDIS }

data class PersistenceNotFoundException(override val message: String) : Exception(message)

/**
 * Persistence layer for rulefile management
 */
interface RulefilePersistence {
    /**
     * Store a given binary rulefile and its summary under the given ID.
     */
    fun storeRuleFile(ruleFileId: String, bytes: ByteArray, uploadSummary: UploadSummaryDO)

    /**
     * Retrieves the binary rulefile for the given ID.
     */
    fun getBinaryRuleFile(ruleFileId: String): Result<ByteArray>

    /**
     * Deletes the rulefile and summary for the given ID.
     */
    fun deleteRuleFile(ruleFileId: String): Boolean

    /**
     * Returns all rulefile summaries.
     */
    fun getAllSummaries(): List<UploadSummaryDO>
}

/**
 * Persistence layer for computation data management
 */
interface ComputationPersistence {
    /**
     * Returns a data persistence object for a single computation with the
     * given input/output types.
     * @param computationType the computation type
     */
    fun <REQUEST : ComputationRequest, RESPONSE, MAIN, ELEMENT> dataPersistence(
        computationType: ComputationType<REQUEST, RESPONSE, MAIN, *, ELEMENT>
    ): DataPersistence<REQUEST, ComputationResponse<MAIN>>

    /**
     * Returns the job info for a single Kjobs computation job.
     */
    fun fetchJobInfo(jobId: String): Result<Job>

    /**
     * Returns the status for a given computation job.
     */
    fun fetchStatus(jobId: String): Result<ComputationStatus>

    /**
     * Returns the user-provided additional constraints for a given
     * computation request.
     * @param computationType the computation type
     */
    fun <REQUEST : ComputationRequest> fetchConstraints(
        jobId: String,
        computationType: ComputationType<REQUEST, *, *, *, *>
    ): Result<REQUEST>

    /**
     * Store the given computation details for a given computation job.
     */
    fun <DETAIL : ComputationDetail> storeDetails(
        jobId: String,
        details: Map<Int, List<SplitComputationDetail<DETAIL>>>,
        sliceMap: Map<SliceDO, Int>
    ) =
        storeDetails(jobId, details, sliceMap, null)

    /**
     * Store the given computation details for a given computation job and
     * element ID.
     */
    fun <DETAIL : ComputationDetail> storeDetails(
        jobId: String,
        details: Map<Int, List<SplitComputationDetail<DETAIL>>>,
        sliceMap: Map<SliceDO, Int>,
        elementId: Int?
    )

    /**
     * Fetch the computation details for a given computation job.
     */
    fun <DETAIL : ComputationDetail> fetchDetail(
        jobId: String,
        slice: SliceDO,
        elementId: Int?,
        computationType: ComputationType<*, *, *, DETAIL, *>
    ): Result<SplitComputationDetail<DETAIL>>

    /**
     * Fetch the main result for a given computation job and result ID.
     */
    fun <MAIN> fetchMainResult(
        jobId: String,
        resultId: Int,
        computationType: ComputationType<*, *, MAIN, *, *>
    ) = fetchMainResult(jobId, resultId, null, computationType)

    /**
     * Fetch the main result for a given computation job, result ID, and
     * element ID.
     */
    fun <MAIN> fetchMainResult(
        jobId: String,
        resultId: Int,
        elementId: Int?,
        computationType: ComputationType<*, *, MAIN, *, *>
    ):
            Result<SliceComputationResult<MAIN>>

    /**
     * Returns the computation response for a given single computation job.
     */
    fun <MAIN> fetchSingleResponse(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, *>
    ): Result<SingleComputationResponse<MAIN>>

    /**
     * Returns the computation results for a given single computation job.
     */
    fun <MAIN> fetchSingleResults(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, *>
    ): List<SliceComputationResult<MAIN>>

    /**
     * Returns the computation response for a given list computation job.
     */
    fun <MAIN, ELEMENT> fetchListResponse(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, ELEMENT>
    ): Result<ListComputationResponse<MAIN, ELEMENT>>

    /**
     * Returns the computation results for a given list computation job.
     */
    fun <MAIN, ELEMENT> fetchListResults(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, ELEMENT>
    ): List<ComputationElementResult<MAIN, ELEMENT>>

    /**
     * Returns the element for a given computation job and element ID.
     */
    fun <ELEMENT> fetchElement(
        jobId: String,
        elementId: Int,
        computationType: ComputationType<*, *, *, *, ELEMENT>
    ): Result<ComputationElement<ELEMENT>>

    /**
     * Stores the given slice groups and returns a mapping from slice to
     * group ID.
     */
    fun storeSliceGroups(jobId: String, sliceSets: List<List<SliceDO>>): Map<SliceDO, Int>

    /**
     * Returns the slice group ID for a given slice
     */
    fun fetchSliceGroup(jobId: String, slice: SliceDO): Result<Int>

    /**
     * Deletes all stored information related to the given job ID and return the
     * number of deleted entries.
     */
    fun deleteComputation(jobId: String): Long

    /**
     * Deletes all stored information for all computations and returns
     * the number of deleted entries.
     */
    fun deleteAllComputations(): Long
}

object Persistence {
    lateinit var rulefile: RulefilePersistence
        private set
    lateinit var computation: ComputationPersistence
        private set
    lateinit var jobs: JobPersistence
        private set

    fun setFromEnvironment(env: ServiceEnv) {
        val persistenceType = env.persistenceType ?: "redis"
        when (PersistenceType.valueOf(persistenceType.uppercase())) {
            REDIS -> initRedis(env)
        }
    }

    private fun initRedis(env: ServiceEnv) {
        check(env.redisUrl != null) { "Redis URL must be provided for Redis persistence type" }
        val redisMaxWait = env.redisMaxWait?.toLong() ?: 10L
        val pool = JedisPool(JedisPoolConfig(), env.redisUrl).apply {
            setMaxWait(java.time.Duration.ofSeconds(redisMaxWait))
        }

        rulefile = RedisRulefilePersistence(pool)
        computation = RedisComputationPersistence(pool)
        jobs = RedisJobPersistence(pool)
    }
}
