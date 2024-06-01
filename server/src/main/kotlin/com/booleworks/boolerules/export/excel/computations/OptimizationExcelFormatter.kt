// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel.computations

import com.booleworks.boolerules.export.excel.SingleComputationExcelFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment

class OptimizationExcelFormatter : SingleComputationExcelFormatter<Int>("Optimal Configuration") {
    private lateinit var mainStyle: CellStyle

    override fun writeMainResult(result: Int, cell: Cell) {
        if (!::mainStyle.isInitialized) mainStyle = boldStyle(cell)
        cell.cellStyle = mainStyle
        cell.cellStyle.alignment = HorizontalAlignment.LEFT
        cell.setCellValue(result.toDouble())
    }
}
