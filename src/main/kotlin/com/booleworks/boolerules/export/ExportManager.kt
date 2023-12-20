// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export

import com.booleworks.boolerules.computations.backbone.BACKBONE
import com.booleworks.boolerules.computations.backbone.BackboneType
import com.booleworks.boolerules.computations.computationTypeFromPath
import com.booleworks.boolerules.computations.consistency.CONSISTENCY
import com.booleworks.boolerules.computations.minmaxconfig.MINMAXCONFIG
import com.booleworks.boolerules.computations.modelcount.MODELCOUNT
import com.booleworks.boolerules.computations.modelenumeration.MODELENUMERATION
import com.booleworks.boolerules.computations.optimization.OPTIMIZATION
import com.booleworks.boolerules.export.excel.ExcelFormatter
import com.booleworks.boolerules.export.excel.computations.BackboneExcelFormatter
import com.booleworks.boolerules.export.excel.computations.ConsistencyExcelFormatter
import com.booleworks.boolerules.export.excel.computations.MinMaxConfigExcelFormatter
import com.booleworks.boolerules.export.excel.computations.ModelCountExcelFormatter
import com.booleworks.boolerules.export.excel.computations.ModelEnumerationExcelFormatter
import com.booleworks.boolerules.export.excel.computations.OptimizationExcelFormatter
import com.booleworks.boolerules.persistence.Persistence
import com.booleworks.kjobs.data.Job
import io.ktor.util.logging.error
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.math.BigInteger

private val logger: Logger = LoggerFactory.getLogger("ExportManager")

fun getExcelExport(uuid: String): ByteArray? = try {
    val jobInfo = Persistence.computation.fetchJobInfo(uuid).getOrThrow()
    generateExcelResult(jobInfo)
} catch (e: Exception) {
    logger.error(e)
    null
}

private fun generateExcelResult(jobInfo: Job): ByteArray {
    val workbook = when (computationTypeFromPath(jobInfo.type)) {
        CONSISTENCY      -> {
            val response = Persistence.computation.fetchSingleResponse(jobInfo.uuid, CONSISTENCY).getOrThrow()
            ExcelFormatter<Boolean>().generateSingleResult(response, jobInfo, ConsistencyExcelFormatter())
        }
        BACKBONE         -> {
            val response = Persistence.computation.fetchListResponse(jobInfo.uuid, BACKBONE).getOrThrow()
            ExcelFormatter<BackboneType>().generateListResult(response, jobInfo, BackboneExcelFormatter())
        }
        MODELCOUNT       -> {
            val response = Persistence.computation.fetchSingleResponse(jobInfo.uuid, MODELCOUNT).getOrThrow()
            ExcelFormatter<BigInteger>().generateSingleResult(response, jobInfo, ModelCountExcelFormatter())
        }
        MODELENUMERATION -> {
            val response = Persistence.computation.fetchListResponse(jobInfo.uuid, MODELENUMERATION).getOrThrow()
            ExcelFormatter<Boolean>().generateListResult(response, jobInfo, ModelEnumerationExcelFormatter())
        }
        MINMAXCONFIG     -> {
            val response = Persistence.computation.fetchSingleResponse(jobInfo.uuid, MINMAXCONFIG).getOrThrow()
            ExcelFormatter<Int>().generateSingleResult(response, jobInfo, MinMaxConfigExcelFormatter())
        }
        OPTIMIZATION     -> {
            val response = Persistence.computation.fetchSingleResponse(jobInfo.uuid, OPTIMIZATION).getOrThrow()
            ExcelFormatter<Int>().generateSingleResult(response, jobInfo, OptimizationExcelFormatter())
        }
        else             -> error("No excel export possible")
    }
    val output = ByteArrayOutputStream()
    workbook.write(output)
    return output.toByteArray()
}
