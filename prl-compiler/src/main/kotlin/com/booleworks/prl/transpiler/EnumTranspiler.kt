// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.transpiler

import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.constraints.EnumComparisonPredicate
import com.booleworks.prl.model.constraints.EnumInPredicate

const val ENUM_FEATURE_PREFIX = "@ENUM"

fun translateEnumIn(
    f: FormulaFactory,
    enumMapping: Map<String, Map<String, Variable>>,
    constraint: EnumInPredicate
): Formula = enumMapping[constraint.feature.featureCode].let { enumMap ->
    if (enumMap == null) f.falsum() else f.or(constraint.values.map { enumMap[it] ?: f.falsum() })
}

fun translateEnumComparison(
    f: FormulaFactory,
    enumMapping: Map<String, Map<String, Variable>>,
    constraint: EnumComparisonPredicate
): Formula =
    enumMapping[constraint.feature.featureCode].let { enumMap ->
        if (enumMap == null) f.falsum() else enumMap[constraint.value.value].let { v ->
            if (v == null) f.constant(constraint.comparison != ComparisonOperator.EQ) else f.literal(
                v.name(),
                constraint.comparison == ComparisonOperator.EQ
            )
        }
    }

internal fun enumFeature(f: FormulaFactory, feature: String, value: String) = f.variable(
    "$ENUM_FEATURE_PREFIX$S${feature.replace(" ", S).replace(".", "#")}" + "$S${value.replace(" ", S)}"
)

