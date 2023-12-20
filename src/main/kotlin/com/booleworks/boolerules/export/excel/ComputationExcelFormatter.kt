// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel

import com.booleworks.boolerules.computations.generic.FeatureModelDO
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row

interface ComputationExcelFormatter<MAIN> {
    val mainHeader: String

    fun writeMainResult(result: MAIN, cell: Cell)

    fun mainLength(): Int

    fun boldStyle(cell: Cell): CellStyle = boldStyle(cell.row)
    fun boldStyle(row: Row): CellStyle {
        val wb = row.sheet.workbook
        val boldFont = wb.createFont()
        boldFont.bold = true
        val style = wb.createCellStyle()
        style.setFont(boldFont)
        return style
    }

    fun wrapStyle(cell: Cell): CellStyle = wrapStyle(cell.row)
    fun wrapStyle(row: Row): CellStyle {
        val wb = row.sheet.workbook
        val style = wb.createCellStyle()
        style.wrapText = true
        return style
    }

    fun writeFeatureModel(
        cell: Cell,
        style: CellStyle,
        model: FeatureModelDO,
        maxColumnWidth: Int,
        currrentLength: Int
    ): Int {
        val value = model.toString()
        cell.setCellValue(value)
        cell.cellStyle = style
        return if (value.length > maxColumnWidth) {
            maxColumnWidth
        } else if (value.length > currrentLength) {
            value.length
        } else {
            currrentLength
        }
    }
}

abstract class SingleComputationExcelFormatter<MAIN>(final override val mainHeader: String) :
    ComputationExcelFormatter<MAIN> {
    protected var mainMaxLength = mainHeader.length

    override fun mainLength() = mainMaxLength + ADD
}

abstract class ListComputationExcelFormatter<MAIN, ELEMENT>(
    val elementHeader: String,
    final override val mainHeader: String
) :
    ComputationExcelFormatter<MAIN> {
    protected var elementMaxLength = mainHeader.length
    protected var mainMaxLength = mainHeader.length

    abstract fun writeElement(element: ELEMENT, cell: Cell)

    fun elementLength() = elementMaxLength + ADD
    override fun mainLength() = mainMaxLength + ADD
}
