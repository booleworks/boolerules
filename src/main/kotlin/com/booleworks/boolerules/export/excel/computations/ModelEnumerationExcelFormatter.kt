// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel.computations

import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.export.excel.ListComputationExcelFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import kotlin.math.max

class ModelEnumerationExcelFormatter :
    ListComputationExcelFormatter<Boolean, FeatureModelDO>("Feature Model", "Buildability") {
    private lateinit var elementStyle: CellStyle

    override fun writeElement(element: FeatureModelDO, cell: Cell) {
        if (!::elementStyle.isInitialized) elementStyle = boldStyle(cell)
        val value = element.toString()
        elementMaxLength = max(value.length, elementMaxLength)
        cell.setCellValue(element.toString())
        cell.cellStyle = elementStyle
    }

    override fun writeMainResult(result: Boolean, cell: Cell) {
        cell.setCellValue(result.toString().lowercase())
    }
}
