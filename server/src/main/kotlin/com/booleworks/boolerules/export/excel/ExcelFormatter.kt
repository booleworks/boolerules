// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel

import com.booleworks.boolerules.computations.generic.ComputationElementResult
import com.booleworks.boolerules.computations.generic.ComputationStatus
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.booleworks.boolerules.computations.generic.SliceComputationResult
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO.BOOLEAN
import com.booleworks.boolerules.rulefile.PropertyTypeDO.DATE
import com.booleworks.boolerules.rulefile.PropertyTypeDO.ENUM
import com.booleworks.boolerules.rulefile.PropertyTypeDO.INT
import com.booleworks.boolerules.rulefile.SlicingPropertyDO
import com.booleworks.kjobs.data.Job
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

private val FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
internal const val ADD = 4
internal const val WIDTH = 256

class ExcelFormatter<MAIN> {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var resultSheet: XSSFSheet
    private lateinit var styles: Styles
    private lateinit var mainFormatter: ComputationExcelFormatter<MAIN>
    private lateinit var splitSliceHeaders: List<String>

    private var startMainResult: Int = 0

    fun generateSingleResult(
        result: SingleComputationResponse<MAIN>, job: Job, formatter: SingleComputationExcelFormatter<MAIN>
    ): XSSFWorkbook {
        initWorkbook(result.status, job)
        mainFormatter = formatter
        startMainResult = 0
        writeSingleResult(result.results)
        return workbook
    }

    fun <ELEMENT> generateListResult(
        result: ListComputationResponse<MAIN, ELEMENT>,
        job: Job,
        formatter: ListComputationExcelFormatter<MAIN, ELEMENT>
    ): XSSFWorkbook {
        initWorkbook(result.status, job)
        mainFormatter = formatter
        startMainResult = 1
        writeListResult(result.results, formatter)
        return workbook
    }

    private fun initWorkbook(status: ComputationStatus, job: Job) {
        workbook = XSSFWorkbook()
        styles = Styles(workbook)
        resultSheet = workbook.createSheet("Result")
        writeInfoSheet(workbook.createSheet("Computation Info"), status, job, styles)
    }

    private fun writeSingleResult(results: List<SliceComputationResult<MAIN>>) {
        writeResultHeader(results)
        writeMainResults(2, results)

        resultSheet.setZoom(150)
    }

    private fun <ELEMENT> writeListResult(
        elementResults: List<ComputationElementResult<MAIN, ELEMENT>>,
        listFormatter: ListComputationExcelFormatter<MAIN, ELEMENT>
    ) {
        if (elementResults.isNotEmpty()) {
            writeHeader(resultSheet, 0, 1, 0, 0, listFormatter.elementHeader, styles)
            writeResultHeader(elementResults[0].results)
            var currentRow = 2
            elementResults.forEach { elemResult ->
                listFormatter.writeElement(
                    elemResult.element.content,
                    getOrCreateRow(resultSheet, currentRow).createCell(0)
                )
                val nextRow = writeMainResults(currentRow, elemResult.results)
                val range = CellRangeAddress(currentRow, nextRow - 1, 0, 0)
                if (currentRow != nextRow - 1) resultSheet.addMergedRegion(range)
                val cellStyle = resultSheet.getRow(currentRow).getCell(0).cellStyle
                cellStyle.verticalAlignment = VerticalAlignment.CENTER
                currentRow = nextRow
            }
        }
        resultSheet.setColumnWidth(0, listFormatter.elementLength() * WIDTH)
        resultSheet.setZoom(150)
    }

    private fun writeResultHeader(results: List<SliceComputationResult<MAIN>>) {
        var currentCol = startMainResult
        writeHeader(resultSheet, 0, 1, currentCol, currentCol, mainFormatter.mainHeader, styles)
        splitSliceHeaders = getSplitSliceHeaders(results)
        val endCol = currentCol + splitSliceHeaders.size
        currentCol++
        if (splitSliceHeaders.isNotEmpty()) {
            writeHeader(resultSheet, 0, 0, currentCol, endCol, "Split Slice", styles)
            splitSliceHeaders.forEach {
                writeHeader(resultSheet, 1, 1, currentCol, currentCol++, it, styles)
            }
        }
    }

    private fun writeMainResults(initialRow: Int, results: List<SliceComputationResult<MAIN>>): Int {
        var currentRow = initialRow
        val splitSliceMaxLength = splitSliceHeaders.map { it.length }.toIntArray()
        results.forEach { sliceResult ->
            val mainResultCell = getOrCreateRow(resultSheet, currentRow).createCell(startMainResult)
            mainFormatter.writeMainResult(sliceResult.result, mainResultCell)
            sliceResult.slices.forEach { splitSlice ->
                writeSliceProperties(
                    splitSlice,
                    getOrCreateRow(resultSheet, currentRow),
                    splitSliceMaxLength,
                    startMainResult + 1
                )
                currentRow++
                for (it in startMainResult + 1..startMainResult + splitSliceHeaders.size) {
                    borderAndMergeRegion(currentRow, 1, it)
                }
            }
            val numResults = sliceResult.slices.size
            borderAndMergeRegion(currentRow, numResults, startMainResult)
        }
        setColumnWidths(resultSheet, mainFormatter, splitSliceMaxLength)
        return currentRow
    }

    private fun borderAndMergeRegion(row: Int, numResults: Int, col: Int) {
        val range = CellRangeAddress(row - numResults, row - 1, col, col)
        val cellStyle = resultSheet.getRow(range.firstRow).getCell(col).cellStyle
        cellStyle.verticalAlignment = VerticalAlignment.CENTER
        if (numResults > 1) resultSheet.addMergedRegion(range)
    }

    private fun setColumnWidths(
        sheet: XSSFSheet,
        formatter: ComputationExcelFormatter<MAIN>,
        splitSliceMaxLength: IntArray
    ) {
        sheet.setColumnWidth(startMainResult, formatter.mainLength() * WIDTH)
        splitSliceMaxLength.forEachIndexed { index, length ->
            sheet.setColumnWidth(
                index + startMainResult + 1,
                (length + ADD) * WIDTH
            )
        }
    }

    private fun writeSliceProperties(slice: SliceDO, row: XSSFRow, lengthArray: IntArray, initialCol: Int): Int {
        var currentCol = initialCol
        slice.content.forEachIndexed { index, property ->
            val splitCell = row.createCell(currentCol++)
            splitCell.setCellValue(property.toString())
            val length = writePropertyCell(splitCell, property, styles)
            lengthArray[index] = max(lengthArray[index], length)
        }
        return currentCol
    }

    private fun getSplitSliceHeaders(results: List<SliceComputationResult<*>>) =
        if (results.isEmpty() || results[0].slices.isEmpty()) {
            listOf()
        } else {
            results[0].slices[0].content.map { it.name }
        }

    private fun writePropertyCell(cell: XSSFCell, property: SlicingPropertyDO, styles: Styles): Int {
        var value: Any = ""
        var length = 0
        cell.cellStyle = styles.defaultStyle
        when (property.type) {
            BOOLEAN -> if (property.range.booleanValues!!.size == 1) {
                value = property.range.booleanValues!!.first()
                length = value.toString().length
            } else {
                value = property.range.booleanValues!!.joinToString(", ")
                length = value.length
            }
            INT -> if (property.range.isContinuous() && property.range.intMin != property.range.intMax) {
                value = "${property.range.intMin} - ${property.range.intMax}"
                length = value.length
            } else if (property.range.isContinuous()) {
                value = property.range.intMin!!
                length = value.toString().length
            } else if (property.range.intValues!!.size == 1) {
                value = property.range.intValues!!.first()
                length = value.toString().length
            } else {
                value = property.range.intValues!!.joinToString(", ")
                length = value.length
            }
            DATE -> if (property.range.isContinuous() && property.range.dateMin != property.range.dateMax) {
                value = "${property.range.dateMin!!.format(FORMATTER)} - ${property.range.dateMax!!.format(FORMATTER)}"
                length = value.length
                cell.cellStyle = styles.dateStyle
            } else if (property.range.isContinuous()) {
                value = property.range.dateMin!!
                length = 10
                cell.cellStyle = styles.dateStyle
            } else if (property.range.dateValues!!.size == 1) {
                value = property.range.dateValues!!.first()
                length = 10
                cell.cellStyle = styles.dateStyle
            } else {
                value = property.range.dateValues!!.joinToString(", ") { it.format(FORMATTER) }
                length = value.length
                cell.cellStyle = styles.dateStyle
            }
            ENUM -> {
                value = property.range.enumValues!!.joinToString(", ")
                length = value.length
            }
        }
        writeCellValue(cell, value, styles)
        return length
    }

    private fun writeInfoSheet(sheet: XSSFSheet, status: ComputationStatus, job: Job, styles: Styles) {
        val currentLine = writeComputationStatus(sheet, styles, status)
        writeJobInfo(sheet, styles, job, currentLine)

        sheet.setColumnWidth(0, 30 * WIDTH)
        sheet.setColumnWidth(1, 50 * WIDTH)
        sheet.setZoom(150)
    }

    private fun writeComputationStatus(sheet: XSSFSheet, styles: Styles, status: ComputationStatus): Int {
        var currentRow = 0
        writeHeader(sheet, currentRow++, 0, 0, 1, "BooleRules Computation Status", styles)
        writeKeyValue(sheet, currentRow++, "Computation Successful", status.success, styles)
        writeKeyValue(sheet, currentRow++, "BooleRules Version", status.version, styles)
        writeKeyValue(sheet, currentRow++, "Rule File ID", status.ruleFileId, styles)
        writeKeyValue(
            sheet,
            currentRow++,
            "Computation Time",
            status.statistics.computationTimeInMs.milliseconds,
            styles
        )
        writeKeyValue(sheet, currentRow++, "Number of Slices", status.statistics.numberOfSlices, styles)
        writeKeyValue(
            sheet,
            currentRow++,
            "Number of Slice Computations",
            status.statistics.numberOfSliceComputations,
            styles
        )
        writeKeyValue(
            sheet,
            currentRow++,
            "Average Slice Computation Time",
            status.statistics.averageSliceComputationTimeInMs.milliseconds,
            styles
        )

        if (status.errors.isNotEmpty() || status.warnings.isNotEmpty() || status.infos.isNotEmpty()) {
            currentRow++
            writeHeader(sheet, currentRow, currentRow++, 0, 1, "Messages", styles)
            status.errors.forEach { error -> writeKeyValue(sheet, currentRow++, "Error", error, styles) }
            status.warnings.forEach { warning -> writeKeyValue(sheet, currentRow++, "Warning", warning, styles) }
            status.infos.forEach { info -> writeKeyValue(sheet, currentRow++, "Info", info, styles) }
        }
        return currentRow
    }

    private fun writeJobInfo(sheet: XSSFSheet, styles: Styles, job: Job, initialLine: Int) {
        var currentLine = initialLine + 1
        writeHeader(sheet, currentLine, currentLine++, 0, 1, "KJobs Infos", styles)
        writeKeyValue(sheet, currentLine++, "Job UUID", job.uuid, styles)
        writeKeyValue(sheet, currentLine++, "Status", job.status, styles)
        writeKeyValue(sheet, currentLine++, "Type", job.type, styles)
        writeKeyValue(sheet, currentLine++, "Tags", job.tags.joinToString(", "), styles)
        writeKeyValue(sheet, currentLine++, "Priority", job.priority, styles)
        writeKeyValue(sheet, currentLine++, "Creating By", job.createdBy, styles)
        writeKeyValue(sheet, currentLine++, "Executed By", job.executingInstance, styles)
        writeKeyValue(sheet, currentLine++, "Creating At", job.createdAt, styles)
        writeKeyValue(sheet, currentLine++, "Started At", job.startedAt, styles)
        writeKeyValue(sheet, currentLine++, "Finished At", job.finishedAt, styles)
        writeKeyValue(sheet, currentLine, "Number of Restarts", job.numRestarts, styles)
    }

    private fun writeHeader(
        sheet: XSSFSheet,
        rowFrom: Int,
        rowTo: Int,
        colFrom: Int,
        colTo: Int,
        header: String,
        styles: Styles
    ) {
        val cellHeader = getOrCreateRow(sheet, rowFrom).createCell(colFrom)
        cellHeader.cellStyle = styles.mergedHeaderStyle
        cellHeader.setCellValue(header)
        val range = CellRangeAddress(rowFrom, rowTo, colFrom, colTo)
        if (rowFrom != rowTo || colFrom != colTo) {
            sheet.addMergedRegion(range)
        }
        addHeaderBorder(sheet, range)
    }

    private fun writeKeyValue(sheet: XSSFSheet, row: Int, key: String, value: Any?, styles: Styles) {
        val cellKey = getOrCreateRow(sheet, row).createCell(0)
        cellKey.cellStyle = styles.infoKeyStyle
        cellKey.setCellValue(key)
        val cellValue = sheet.getRow(row).createCell(1)
        cellValue.cellStyle = styles.infoValueStyle
        writeCellValue(cellValue, value, styles)
    }

    private fun writeCellValue(cell: XSSFCell, value: Any?, styles: Styles) {
        when (value) {
            is String -> cell.setCellValue(value)
            is Int -> cell.setCellValue(value.toDouble())
            is Boolean -> cell.setCellValue(value)
            is LocalDateTime -> {
                cell.cellStyle = styles.dateTimeStyle
                cell.setCellValue(value)
            }
            is LocalDate -> {
                cell.cellStyle = styles.dateStyle
                cell.setCellValue(value)
            }
            null -> {}
            else -> cell.setCellValue(value.toString())
        }
    }

    private fun getOrCreateRow(sheet: XSSFSheet, row: Int) =
        if (sheet.lastRowNum < row) sheet.createRow(row) else sheet.getRow(row)

    private fun addHeaderBorder(sheet: XSSFSheet, region: CellRangeAddress) {
        addBorder(sheet, region, BorderStyle.MEDIUM, IndexedColors.BLACK.index)
    }

    private fun addBorder(sheet: XSSFSheet, region: CellRangeAddress, borderStyle: BorderStyle, color: Short) {
        // This is slow AS HELL
        RegionUtil.setBorderTop(borderStyle, region, sheet)
        RegionUtil.setBorderRight(borderStyle, region, sheet)
        RegionUtil.setBorderBottom(borderStyle, region, sheet)
        RegionUtil.setBorderLeft(borderStyle, region, sheet)
        RegionUtil.setTopBorderColor(color.toInt(), region, sheet)
        RegionUtil.setRightBorderColor(color.toInt(), region, sheet)
        RegionUtil.setBottomBorderColor(color.toInt(), region, sheet)
        RegionUtil.setLeftBorderColor(color.toInt(), region, sheet)
    }
}

private class Styles(workbook: XSSFWorkbook) {
    private val boldFont: Font = workbook.createFont()
    private val mergedHeaderFont: Font = workbook.createFont()

    private val yellowColor: XSSFColor =
        XSSFColor(ByteArray(3).apply { this[0] = 255.toByte(); this[1] = 195.toByte(); this[2] = 18.toByte() })

    private val dateFormat = workbook.creationHelper.createDataFormat().getFormat("mm.dd.yy")
    private val dateTimeFormat = workbook.creationHelper.createDataFormat().getFormat("mm.dd.yy h:mm:ss")

    val mergedHeaderStyle: XSSFCellStyle = workbook.createCellStyle()
    val infoKeyStyle: XSSFCellStyle = workbook.createCellStyle()
    val infoValueStyle: XSSFCellStyle = workbook.createCellStyle()
    val dateStyle: XSSFCellStyle = workbook.createCellStyle()
    val dateTimeStyle: XSSFCellStyle = workbook.createCellStyle()
    val defaultStyle: XSSFCellStyle = workbook.createCellStyle()

    init {
        boldFont.bold = true
        mergedHeaderFont.bold = true
        mergedHeaderFont.fontHeightInPoints = 14

        mergedHeaderStyle.setFont(mergedHeaderFont)
        mergedHeaderStyle.setFillForegroundColor(yellowColor)
        mergedHeaderStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        mergedHeaderStyle.alignment = HorizontalAlignment.CENTER
        mergedHeaderStyle.verticalAlignment = VerticalAlignment.CENTER

        infoKeyStyle.setFont(boldFont)
        infoValueStyle.alignment = HorizontalAlignment.LEFT

        dateStyle.dataFormat = dateFormat
        dateStyle.alignment = HorizontalAlignment.LEFT
        dateTimeStyle.dataFormat = dateTimeFormat
        dateTimeStyle.alignment = HorizontalAlignment.LEFT
        defaultStyle.alignment = HorizontalAlignment.LEFT
    }
}
