// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel.computations

import com.booleworks.boolerules.export.excel.SingleComputationExcelFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import java.math.BigInteger

class ModelCountExcelFormatter : SingleComputationExcelFormatter<BigInteger>("Number of Configurations") {
    private var mainStyle: CellStyle? = null

    override fun writeMainResult(result: BigInteger, cell: Cell) {
        if (mainStyle == null) {
            val wb = cell.row.sheet.workbook
            mainStyle = wb.createCellStyle()
            mainStyle!!.alignment = HorizontalAlignment.LEFT
        }
        val mainResult = result.toString()
        if (mainResult.length > mainMaxLength) mainMaxLength = mainResult.length
        cell.setCellValue(result.toDouble())
        cell.cellStyle = mainStyle
    }
}
