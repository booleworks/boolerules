// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.transpiler.TranslationInfo
import java.util.SortedSet
import java.util.TreeSet

fun computeRelevantVars(
    f: FormulaFactory,
    info: TranslationInfo,
    features: List<String>
): SortedSet<Variable> =
    if (features.isEmpty()) {
        info.knownVariables.toSortedSet()
    } else {
        val vars = TreeSet<Variable>()
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
