// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.rulefile

import com.booleworks.boolerules.persistence.Persistence
import com.booleworks.boolerules.rulefile.PropertyTypeDO.BOOLEAN
import com.booleworks.boolerules.rulefile.PropertyTypeDO.DATE
import com.booleworks.boolerules.rulefile.PropertyTypeDO.ENUM
import com.booleworks.boolerules.rulefile.PropertyTypeDO.INT
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.parseRuleFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.UUID
import java.util.zip.GZIPOutputStream

internal fun storeRuleFile(fileName: String, bytes: ByteArray): UploadSummaryDO {
    val prlRuleFile = try {
        parseRuleFile(InputStreamReader(ByteArrayInputStream(bytes)), fileName)
    } catch (e: RuntimeException) {
        return parseError(fileName, "Parse Error ${e.cause}")
    }

    val compiler = PrlCompiler()
    val model = compiler.compile(prlRuleFile)
    return if (!compiler.hasErrors()) {
        val bin = serialize(model)
        ByteArrayOutputStream().use {
            val output = GZIPOutputStream(it)
            bin.writeTo(output)
            output.close()
            val uuid = UUID.randomUUID().toString()
            val byteArray = it.toByteArray()
            val uploadSummary = generateUploadSummary(uuid, fileName, byteArray.size, model, compiler)
            Persistence.rulefile.storeRuleFile(uuid, byteArray, uploadSummary)
            uploadSummary
        }
    } else {
        compilerError(fileName, compiler)
    }
}

internal fun getAllSummaries(): List<UploadSummaryDO> = Persistence.rulefile.getAllSummaries()
internal fun getRuleFile(ruleFileId: String): ByteArray? =
    Persistence.rulefile.getBinaryRuleFile(ruleFileId).getOrNull()

internal fun deleteRuleFile(ruleFileId: String): Boolean = Persistence.rulefile.deleteRuleFile(ruleFileId)

private fun generateUploadSummary(uuid: String, fileName: String, size: Int, model: PrlModel, compiler: PrlCompiler) =
    UploadSummaryDO(
        uuid,
        fileName,
        LocalDateTime.now(),
        size,
        model.moduleHierarchy.numberOfModules(),
        model.rules.size,
        model.features().size,
        model.booleanFeatures().isNotEmpty(),
        model.enumFeatures().isNotEmpty(),
        model.intFeatures().isNotEmpty(),
        generateProperties(model),
        generateFeatures(model),
        compiler.errors(),
        compiler.warnings(),
        compiler.infos()
    )

private fun parseError(fileName: String, message: String) = UploadSummaryDO(
    "",
    fileName,
    LocalDateTime.now(),
    0,
    0,
    0,
    0,
    hasBooleanFeatures = false,
    hasEnumFeatures = false,
    hasIntFeatures = false,
    listOf(),
    setOf(),
    errors = listOf("Parse Error: $message")
)

private fun compilerError(fileName: String, compiler: PrlCompiler) = UploadSummaryDO(
    "",
    fileName,
    LocalDateTime.now(),
    0,
    0,
    0,
    0,
    hasBooleanFeatures = false,
    hasEnumFeatures = false,
    hasIntFeatures = false,
    listOf(),
    setOf(),
    compiler.errors(),
    compiler.warnings(),
    compiler.infos()
)

private fun generateProperties(model: PrlModel) = model.propertyStore.allDefinitions().map { def ->
    when (def) {
        is SlicingBooleanPropertyDefinition -> SlicingPropertyDO(def.name, BOOLEAN, PropertyRangeDO(def))
        is SlicingDatePropertyDefinition -> SlicingPropertyDO(def.name, DATE, PropertyRangeDO(def))
        is SlicingEnumPropertyDefinition -> SlicingPropertyDO(def.name, ENUM, PropertyRangeDO(def))
        is SlicingIntPropertyDefinition -> SlicingPropertyDO(def.name, INT, PropertyRangeDO(def))
    }
}

internal fun generateFeatures(model: PrlModel) = model.features().map {
    val uniqueName = if (model.featureStore.nonUniqueFeatures().contains(it.featureCode)) {
        it.fullName
    } else {
        it.featureCode
    }
    FeatureNameDO(uniqueName, it.fullName)
}.toSortedSet(compareBy { it.uniqueName })
