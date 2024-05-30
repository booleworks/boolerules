// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser

import com.booleworks.prl.model.IntRange

sealed class PrlFeatureDefinition(
    open val code: String,
    open val description: String = "",
    open val properties: List<PrlProperty<*>> = emptyList(),
    open val lineNumber: Int? = null
)

data class PrlBooleanFeatureDefinition(
    override val code: String,
    val versioned: Boolean,
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = emptyList(),
    override val lineNumber: Int? = null
) : PrlFeatureDefinition(code, description, properties, lineNumber)

data class PrlEnumFeatureDefinition(
    override val code: String,
    val values: List<String>,
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = emptyList(),
    override val lineNumber: Int? = null
) : PrlFeatureDefinition(code, description, properties, lineNumber)

data class PrlIntFeatureDefinition(
    override val code: String,
    val domain: IntRange,
    override val description: String = "",
    override val properties: List<PrlProperty<*>> = emptyList(),
    override val lineNumber: Int? = null
) : PrlFeatureDefinition(code, description, properties, lineNumber)
