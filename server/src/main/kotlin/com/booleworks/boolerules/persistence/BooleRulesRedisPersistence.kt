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
import com.booleworks.boolerules.computations.generic.ComputationVariant
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.booleworks.boolerules.computations.generic.SliceComputationResult
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SplitComputationDetail
import com.booleworks.boolerules.rulefile.UploadSummaryDO
import com.booleworks.kjobs.api.persistence.DataPersistence
import com.booleworks.kjobs.api.persistence.DataTransactionalPersistence
import com.booleworks.kjobs.api.persistence.redis.DefaultRedisConfig
import com.booleworks.kjobs.api.persistence.redis.RedisConfig
import com.booleworks.kjobs.api.persistence.redis.RedisDataPersistence
import com.booleworks.kjobs.api.persistence.redis.RedisDataTransactionalPersistence
import com.booleworks.kjobs.api.persistence.redis.RedisJobPersistence
import com.booleworks.kjobs.common.Either
import com.booleworks.kjobs.common.getOrElse
import com.booleworks.kjobs.data.Job
import com.booleworks.kjobs.data.PersistenceAccessResult
import com.booleworks.kjobs.data.internalError
import com.booleworks.kjobs.data.notFound
import com.booleworks.kjobs.data.result
import com.booleworks.kjobs.data.success
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Transaction
import kotlin.use

class RedisRulefilePersistence(private val pool: JedisPool) : RulefilePersistence {

    private val summaryDeserializer: (String) -> UploadSummaryDO =
        jacksonObjectMapper().registerModule(JavaTimeModule())::readValue

    private val summarySerializer: (UploadSummaryDO) -> String =
        jacksonObjectMapper().registerModule(JavaTimeModule())::writeValueAsString

    override fun storeRuleFile(ruleFileId: String, bytes: ByteArray, uploadSummary: UploadSummaryDO) {
        pool.resource.use { jedis ->
            jedis.set(ruleFileId.toByteArray(), bytes)
            jedis.set("$RULEFILE_PREFIX:$ruleFileId", summarySerializer(uploadSummary))
        }
    }

    override fun getBinaryRuleFile(ruleFileId: String): Result<ByteArray> = pool.resource.use { jedis ->
        val bytes = jedis.get(ruleFileId.toByteArray())
        return if (bytes != null) {
            Result.success(bytes)
        } else {
            Result.failure(PersistenceNotFoundException("No rule " + "file for ID $ruleFileId"))
        }
    }

    override fun deleteRuleFile(ruleFileId: String) = pool.resource.use { jedis ->
        jedis.del("$RULEFILE_PREFIX:$ruleFileId".toByteArray())
        jedis.del(ruleFileId.toByteArray()) > 0
    }

    override fun getAllSummaries() = pool.resource.use { jedis ->
        val keys = jedis.keys("$RULEFILE_PREFIX:$ANY")
        if (keys.isNotEmpty()) jedis.mget(*keys.toTypedArray()).map(summaryDeserializer) else listOf()
    }
}

class RedisComputationPersistence(private val pool: JedisPool) : ComputationPersistence {

    override fun fetchJobInfo(jobId: String): Result<Job> {
        val job = runBlocking { RedisJobPersistence(pool).fetchJob(jobId) }.getOrElse {
            return Result.failure(PersistenceNotFoundException("No Job for job ID $jobId"))
        }
        return Result.success(job)
    }

    override fun <REQUEST : ComputationRequest, RESPONSE, MAIN, ELEMENT> dataPersistence(
        computationType: ComputationType<REQUEST, RESPONSE, MAIN, *, ELEMENT>
    ): DataPersistence<REQUEST, ComputationResponse<MAIN>> =
        BooleRulesRedisPersistence(
            pool,
            jacksonObjectMapper().registerModule(JavaTimeModule())::writeValueAsBytes,
            { jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(it, computationType.request) },
            computationType,
            DefaultRedisConfig()
        )

    override fun <DETAIL : ComputationDetail> storeDetails(
        jobId: String,
        details: Map<Int, List<SplitComputationDetail<DETAIL>>>,
        sliceMap: Map<SliceDO, Int>,
        elementId: Int?
    ) {
        val detailSerializer: (SplitComputationDetail<DETAIL>) -> String =
            jacksonObjectMapper().registerModule(JavaTimeModule())::writeValueAsString
        pool.resource.use { jedis ->
            val keyValues = mutableListOf<String>()
            details.forEach { (id, ds) ->
                ds.forEach { d ->
                    d.resultId = id
                    d.elementId = elementId
                    val sliceGroup = sliceMap[d.slice] ?: 0
                    keyValues.add(detailKey(jobId, sliceGroup, elementId))
                    keyValues.add(detailSerializer(d))
                }
            }
            if (keyValues.isNotEmpty()) jedis.mset(*keyValues.toTypedArray())
        }
    }

    override fun fetchStatus(jobId: String): Result<ComputationStatus> {
        val status = pool.resource.use { jedis -> jedis.get(statusKey(jobId)) }
        return if (status == null) {
            Result.failure(PersistenceNotFoundException("No status for job ID $jobId"))
        } else {
            Result.success(jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(status))
        }
    }

    override fun <DETAIL : ComputationDetail> fetchDetail(
        jobId: String,
        slice: SliceDO,
        elementId: Int?,
        computationType: ComputationType<*, *, *, DETAIL, *>
    ): Result<SplitComputationDetail<DETAIL>> {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val detailType =
            mapper.typeFactory.constructParametricType(SplitComputationDetail::class.java, computationType.detail)
        val sliceGroup = fetchSliceGroup(jobId, slice).getOrElse { return Result.failure(it) }
        val key = detailKey(jobId, sliceGroup, elementId)
        pool.resource.use { jedis ->
            val res = jedis.get(key)
                ?: return Result.failure(
                    PersistenceNotFoundException("No detail for job ID $jobId, slice $slice, and element ID $elementId")
                )
            return Result.success(mapper.readValue(res, detailType))
        }
    }

    override fun <REQUEST : ComputationRequest> fetchConstraints(
        jobId: String,
        computationType: ComputationType<REQUEST, *, *, *, *>
    ): Result<REQUEST> {
        val requestDeserializer: (String) -> REQUEST =
            { jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(it, computationType.request) }
        pool.resource.use {
            val request = it.get(requestKey(jobId))
                ?: return Result.failure(PersistenceNotFoundException("No request for job ID $jobId"))
            return Result.success(requestDeserializer(request))
        }
    }

    override fun <MAIN> fetchMainResult(
        jobId: String,
        resultId: Int,
        elementId: Int?,
        computationType: ComputationType<*, *, MAIN, *, *>
    ): Result<SliceComputationResult<MAIN>> {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val mainType =
            mapper.typeFactory.constructParametricType(SliceComputationResult::class.java, computationType.main)
        val key = if (elementId == null) singleResultKey(jobId, resultId) else listResultKey(jobId, elementId, resultId)
        pool.resource.use { jedis ->
            val res = jedis.get(key)
                ?: return Result.failure(
                    PersistenceNotFoundException(
                        "No detail for job ID $jobId, result ID $resultId, and element ID $elementId"
                    )
                )
            return Result.success(mapper.readValue(res, mainType))
        }
    }

    override fun <MAIN> fetchSingleResponse(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, *>
    ): Result<SingleComputationResponse<MAIN>> {
        val status = Persistence.computation.fetchStatus(jobId).getOrElse { return Result.failure(it) }
        val results = Persistence.computation.fetchSingleResults(jobId, computationType)
        return Result.success(SingleComputationResponse(status, results))
    }

    override fun <MAIN> fetchSingleResults(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, *>
    ): List<SliceComputationResult<MAIN>> {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val mainType =
            mapper.typeFactory.constructParametricType(SliceComputationResult::class.java, computationType.main)
        val results = mutableListOf<SliceComputationResult<MAIN>>()
        pool.resource.use { jedis ->
            val keys = jedis.keys(allSingleResultsKey(jobId)).toTypedArray()
            if (keys.isNotEmpty()) jedis.mget(*keys).forEach { value ->
                val main: SliceComputationResult<MAIN> = mapper.readValue(value, mainType)
                results.add(main)
            }
        }
        return results.sortedBy { it.id }
    }

    override fun <MAIN, ELEMENT> fetchListResults(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, ELEMENT>
    ):
            List<ComputationElementResult<MAIN, ELEMENT>> {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val elementType =
            mapper.typeFactory.constructParametricType(ComputationElement::class.java, computationType.element)
        val mainType =
            mapper.typeFactory.constructParametricType(SliceComputationResult::class.java, computationType.main)
        val results = mutableListOf<ComputationElementResult<MAIN, ELEMENT>>()
        pool.resource.use { jedis ->
            val elementKeys = jedis.keys(allElementsKey(jobId)).toTypedArray()
            if (elementKeys.isNotEmpty()) {
                jedis.mget(*elementKeys).forEach { elementValue ->
                    val elementResults = mutableListOf<SliceComputationResult<MAIN>>()
                    val element: ComputationElement<ELEMENT> = mapper.readValue(elementValue, elementType)
                    val keys = jedis.keys(allResultsForElementKey(jobId, element.id)).toTypedArray()
                    if (keys.isNotEmpty()) jedis.mget(*keys).forEach { value ->
                        val main: SliceComputationResult<MAIN> = mapper.readValue(value, mainType)
                        elementResults.add(main)
                    }
                    results.add(ComputationElementResult(element, elementResults.sortedBy { it.id }))
                }
            }
        }
        return results.sortedBy { it.element.id }
    }

    override fun <MAIN, ELEMENT> fetchListResponse(
        jobId: String,
        computationType: ComputationType<*, *, MAIN, *, ELEMENT>
    ):
            Result<ListComputationResponse<MAIN, ELEMENT>> {
        val status = Persistence.computation.fetchStatus(jobId).getOrElse { return Result.failure(it) }
        val results = Persistence.computation.fetchListResults(jobId, computationType)
        return Result.success(ListComputationResponse(status, results))
    }

    override fun <ELEMENT> fetchElement(
        jobId: String,
        elementId: Int,
        computationType: ComputationType<*, *, *, *, ELEMENT>
    ):
            Result<ComputationElement<ELEMENT>> {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val elementType =
            mapper.typeFactory.constructParametricType(ComputationElement::class.java, computationType.element)
        val key = elementKey(jobId, elementId)
        pool.resource.use { jedis ->
            val res = jedis.get(key)
                ?: return Result.failure(
                    PersistenceNotFoundException("No element for job ID $jobId and element ID $elementId")
                )
            return Result.success(mapper.readValue(res, elementType))
        }
    }

    override fun storeSliceGroups(jobId: String, sliceSets: List<List<SliceDO>>): Map<SliceDO, Int> {
        val sliceMap = mutableMapOf<SliceDO, Int>()
        pool.resource.use { jedis ->
            var sliceGroupId = 1
            sliceSets.forEach { set ->
                val keyValues = mutableListOf<String>()
                set.forEach { slice ->
                    keyValues.add(sliceKey(jobId, slice))
                    keyValues.add(sliceGroupId.toString())
                    sliceMap[slice] = sliceGroupId
                }
                jedis.mset(*keyValues.toTypedArray())
                sliceGroupId++
            }
        }
        return sliceMap
    }

    override fun fetchSliceGroup(jobId: String, slice: SliceDO): Result<Int> =
        pool.resource.use { jedis ->
            val keys = jedis.keys(sliceSearchKey(jobId, slice)).toTypedArray().sortedArray()
            if (keys.isEmpty()) {
                return Result.failure(
                    PersistenceNotFoundException("No slice group for job ID $jobId and slice $slice")
                )
            }
            val sliceGroup = jedis.get(keys[0])?.toInt()
                ?: return Result.failure(
                    PersistenceNotFoundException("No slice group for job ID $jobId and slice $slice")
                )
            return Result.success(sliceGroup)
        }

    override fun deleteComputation(jobId: String): Long {
        var deleted = 0L
        pool.resource.use { jedis ->
            deleted += jedis.del(requestKey(jobId))
            deleted += jedis.del(jobKey(jobId))
            val keys = jedis.keys(allResultKey(jobId)).toTypedArray()
            if (keys.isNotEmpty()) deleted += jedis.del(*keys)
        }
        return deleted
    }

    override fun deleteAllComputations(): Long {
        var deleted = 0L
        pool.resource.use { jedis ->
            jedis.keys(allStatusKey()).forEach { deleted += deleteComputation(extractIdFromStatusKey(it)) }
        }
        return deleted
    }
}

internal class BooleRulesRedisPersistence<
        MAIN,
        REQUEST : ComputationRequest,
        RESULT : ComputationResponse<MAIN>,
        ELEMENT>(
    pool: JedisPool,
    inputSerializer: (REQUEST) -> ByteArray,
    inputDeserializer: (ByteArray) -> REQUEST,
    private val computationType: ComputationType<REQUEST, *, MAIN, *, ELEMENT>,
    configuration: RedisConfig
) : RedisDataPersistence<REQUEST, RESULT>(
    pool,
    inputSerializer,
    { error("never call") },
    inputDeserializer,
    { error("never call") },
    configuration
) {
    private val logger = LoggerFactory.getLogger(BooleRulesRedisPersistence::class.java)

    override suspend fun <T> dataTransaction(
        block: suspend DataTransactionalPersistence<REQUEST, RESULT>.() -> T
    ): PersistenceAccessResult<T> =
        pool.resource.use { jedis ->
            jedis.multi().run {
                runCatching {
                    BooleRulesRedisDataTransactionalPersistence<MAIN, REQUEST, RESULT>(
                        this@run,
                        inputSerializer,
                        config
                    )
                        .run { block() }
                        .also { exec() }
                }.getOrElse { exception -> return handleTransactionException(exception) }
            }
        }.let { Either.Right(it) }

    @Suppress("UNCHECKED_CAST")
    override suspend fun fetchResult(uuid: String): PersistenceAccessResult<RESULT> {
        val status = Persistence.computation.fetchStatus(uuid).getOrElse {
            return PersistenceAccessResult.notFound()
        }
        return if (status.computationVariant == ComputationVariant.SINGLE) {
            val results = Persistence.computation.fetchSingleResults(uuid, computationType)
            PersistenceAccessResult.result(SingleComputationResponse(status, results) as RESULT)
        } else {
            val results = Persistence.computation.fetchListResults(uuid, computationType)
            PersistenceAccessResult.result(ListComputationResponse(status, results) as RESULT)
        }
    }

    private fun <T> Transaction.handleTransactionException(ex: Throwable): PersistenceAccessResult<T> {
        val message = ex.message ?: "Undefined error"
        logger.error("Jedis transaction failed with: $message", ex)
        val discardResult = discard()
        logger.error("Discarded the transaction with result: $discardResult")
        return PersistenceAccessResult.internalError(message)
    }
}

internal class BooleRulesRedisDataTransactionalPersistence<MAIN, REQUEST, RESULT : ComputationResponse<MAIN>>(
    transaction: Transaction,
    inputSerializer: (REQUEST) -> ByteArray,
    config: RedisConfig
) : RedisDataTransactionalPersistence<REQUEST, RESULT>(transaction, inputSerializer, { error("never call") }, config) {
    private val serializer = jacksonObjectMapper().registerModule(JavaTimeModule())::writeValueAsBytes

    override suspend fun persistOrUpdateResult(job: Job, result: RESULT): PersistenceAccessResult<Unit> {
        when (result) {
            is SingleComputationResponse<*> -> {
                transaction.set(statusKey(job.uuid).toByteArray(), serializer(result.status))
                result.results.forEach {
                    transaction.set(
                        singleResultKey(job.uuid, it.id).toByteArray(),
                        serializer(it)
                    )
                }
            }
            is ListComputationResponse<*, *> -> {
                transaction.set(statusKey(job.uuid).toByteArray(), serializer(result.status))
                result.results.forEach { elementResult ->
                    transaction.set(
                        elementKey(job.uuid, elementResult.element.id).toByteArray(),
                        serializer(elementResult.element)
                    )
                    elementResult.results.forEach {
                        transaction.set(
                            listResultKey(job.uuid, elementResult.element.id, it.id).toByteArray(),
                            serializer(it)
                        )
                    }
                }
            }
        }
        return PersistenceAccessResult.success
    }
}


private const val RULEFILE_PREFIX = "rulefile"
private const val RESULT = "result"
private const val STATUS = "status"
private const val INPUT = "input"
private const val SINGLE = "single"
private const val LIST = "list"
private const val ELEMENT = "element"
private const val SLICE = "slice"
private const val DETAIL = "detail"
private const val JOB = "job"
private const val ANY = "*"

private fun statusKey(jobId: String) = "$RESULT:$jobId:$STATUS"
private fun requestKey(jobId: String) = "$INPUT:$jobId"

private fun singleResultKey(jobId: String, id: Int) = "$RESULT:$jobId:$SINGLE:$id"
private fun allSingleResultsKey(jobId: String) = "$RESULT:$jobId:$SINGLE:$ANY"

private fun listResultKey(jobId: String, elementId: Int, id: Int) = "$RESULT:$jobId:$LIST:$elementId:$id"
private fun elementKey(jobId: String, elementId: Int) = "$RESULT:$jobId:$ELEMENT:$elementId"
private fun allElementsKey(jobId: String) = "$RESULT:$jobId:$ELEMENT:$ANY"
private fun allResultsForElementKey(jobId: String, elementId: Int) = "$RESULT:$jobId:$LIST:$elementId:$ANY"

private fun sliceSearchKey(jobId: String, slice: SliceDO) = "$RESULT:$jobId:$SLICE:${slice.searchKey()}"
private fun sliceKey(jobId: String, slice: SliceDO) = "$RESULT:$jobId:$SLICE:${slice.uniqueKey()}"
private fun detailKey(jobId: String, sliceGroupId: Int, elementId: Int?) =
    "$RESULT:$jobId:$DETAIL:$sliceGroupId${if (elementId != null) ":$elementId" else ""}"

private fun jobKey(jobId: String) = "$JOB:$jobId"
private fun allResultKey(jobId: String) = "$RESULT:$jobId:$ANY"
private fun allStatusKey() = "$RESULT:$ANY:$STATUS"
private fun extractIdFromStatusKey(key: String) = key.substring(RESULT.length + 1, key.length - STATUS.length - 1)
