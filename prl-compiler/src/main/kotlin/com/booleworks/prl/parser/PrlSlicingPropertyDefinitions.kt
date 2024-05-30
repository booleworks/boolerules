// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser

sealed class PrlSlicingPropertyDefinition(open val name: String, open val lineNumber: Int? = null)

data class PrlSlicingBooleanPropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    PrlSlicingPropertyDefinition(name, lineNumber)

data class PrlSlicingIntPropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    PrlSlicingPropertyDefinition(name, lineNumber)

data class PrlSlicingEnumPropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    PrlSlicingPropertyDefinition(name, lineNumber)

data class PrlSlicingDatePropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    PrlSlicingPropertyDefinition(name, lineNumber)
