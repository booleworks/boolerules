// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.helpers

import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo
import java.util.*

fun computeRelevantVars(f: FormulaFactory, info: TranspilationInfo, features: List<Feature>): SortedSet<Variable> =
    if (features.isEmpty()) {
        val intSatVariables =
            info.integerVariables.flatMap { info.encodingContext.variableMap[it.variable]?.values ?: emptySet() }
        (info.knownVariables + intSatVariables).toSortedSet()
    } else {
        val vars = TreeSet<Variable>()
        info.integerVariables
            .filter { features.contains(intFt(it.feature)) }
            .forEach { vars.addAll(info.encodingContext.variableMap[it.variable]?.values.orEmpty()) }
        features.forEach {
            val v = f.variable(it.featureCode)
            if (v in info.booleanVariables) {
                vars.add(v)
            } else if (it.featureCode in info.enumMapping.keys) {
                vars.addAll(info.enumMapping[it.featureCode]!!.values)
            }
        }
        vars
    }

fun computeRelevantIntVars(info: TranspilationInfo, features: List<Feature>): SortedSet<LngIntVariable> =
    if (features.isEmpty()) info.integerVariables.toSortedSet { a, b -> a.variable.compareTo(b.variable) }
    else info.integerVariables.filter { features.contains(intFt(it.feature)) }
        .toSortedSet { a, b -> a.variable.compareTo(b.variable) }


