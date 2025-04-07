// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo
import java.util.SortedSet
import java.util.TreeSet

fun computeRelevantVars(f: FormulaFactory, info: TranspilationInfo, features: List<String>): SortedSet<Variable> =
    if (features.isEmpty()) {
        val intSatVariables =
            info.integerVariables.flatMap { info.encodingContext.variableMap[it.variable]?.filterNotNull().orEmpty() }
        (info.knownVariables + intSatVariables).toSortedSet()
    } else {
        val vars = TreeSet<Variable>()
        info.integerVariables
            .filter { features.contains(it.feature) }
            .forEach { vars.addAll(info.encodingContext.variableMap[it.variable]?.filterNotNull().orEmpty()) }
        features.forEach {
            val v = f.variable(it)
            if (v in info.booleanVariables) {
                vars.add(v)
            } else if (it in info.enumMapping.keys) {
                vars.addAll(info.enumMapping[it]!!.values)
            }
        }
        vars
    }

fun computeRelevantIntVars(info: TranspilationInfo, features: List<String>): SortedSet<LngIntVariable> =
    if (features.isEmpty()) info.integerVariables.toSortedSet { a, b -> a.variable.compareTo(b.variable) }
    else info.integerVariables.filter { features.contains(it.feature) }.toSortedSet { a, b -> a.variable.compareTo(b.variable) }
