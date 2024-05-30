// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser

import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.PropertyRange
import com.booleworks.prl.model.PropertyType
import java.time.LocalDate

typealias AnyPrlProperty = PrlProperty<*>

sealed class PrlProperty<T : PropertyRange<*>>(
    open val name: String,
    open val value: T,
    val propertyType: PropertyType,
    open val lineNumber: Int? = null
)

data class PrlBooleanProperty(
    override val name: String,
    override val value: BooleanRange,
    override val lineNumber: Int? = null
) :
    PrlProperty<BooleanRange>(name, value, PropertyType.BOOL, lineNumber)

data class PrlDateProperty(
    override val name: String,
    override val value: DateRange,
    override val lineNumber: Int? = null
) :
    PrlProperty<DateRange>(name, value, PropertyType.DATE, lineNumber) {

    constructor(name: String, value: LocalDate, lineNumber: Int?) : this(name, DateRange.list(value), lineNumber)
}

data class PrlIntProperty(
    override val name: String,
    override val value: IntRange,
    override val lineNumber: Int? = null
) :
    PrlProperty<IntRange>(name, value, PropertyType.INT, lineNumber) {

    constructor(name: String, value: Int, lineNumber: Int?) : this(name, IntRange.list(value), lineNumber)
}

data class PrlEnumProperty(
    override val name: String,
    override val value: EnumRange,
    override val lineNumber: Int? = null
) :
    PrlProperty<EnumRange>(name, value, PropertyType.ENUM, lineNumber) {

    constructor(name: String, value: String, lineNumber: Int?) : this(name, EnumRange.list(value), lineNumber)
}
