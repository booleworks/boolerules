// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel.computations

import com.booleworks.boolerules.export.excel.SingleComputationExcelFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import kotlin.math.max

class ConsistencyExcelFormatter : SingleComputationExcelFormatter<Boolean>("Consistency") {
    private lateinit var mainStyle: CellStyle

    override fun writeMainResult(result: Boolean, cell: Cell) {
        if (!::mainStyle.isInitialized) mainStyle = boldStyle(cell)
        val mainResult = if (result) "consistent" else "inconsistent"
        mainMaxLength = max(mainResult.length, mainMaxLength)
        cell.setCellValue(mainResult)
        cell.cellStyle = mainStyle
    }
}
