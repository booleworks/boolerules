// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.export.excel.computations

import com.booleworks.boolerules.computations.backbone.BackboneType
import com.booleworks.boolerules.computations.generic.FeatureDO
import com.booleworks.boolerules.export.excel.ListComputationExcelFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import kotlin.math.max

class BackboneExcelFormatter : ListComputationExcelFormatter<BackboneType, FeatureDO>("Feature", "Backbone Type") {
    private lateinit var elementStyle: CellStyle

    override fun writeElement(element: FeatureDO, cell: Cell) {
        if (!::elementStyle.isInitialized) elementStyle = boldStyle(cell)
        val value = element.toString()
        elementMaxLength = max(value.length, elementMaxLength)
        cell.setCellValue(element.toString())
        cell.cellStyle = elementStyle
    }

    override fun writeMainResult(result: BackboneType, cell: Cell) {
        cell.setCellValue(result.toString().lowercase())
    }
}
