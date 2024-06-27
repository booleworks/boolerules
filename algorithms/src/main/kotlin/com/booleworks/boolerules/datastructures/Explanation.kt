// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.datastructures

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.explanations.mus.MUSGeneration
import com.booleworks.logicng.solvers.sat.SATCall
import com.booleworks.prl.transpiler.PrlProposition

data class Explanation(val rules: List<PrlProposition>)

fun computeExplanation(satCall: SATCall, cf: CspFactory): Explanation {
    assert(satCall.satResult == Tristate.FALSE)
    val mus = MUSGeneration().computeMUS(cf.formulaFactory(), satCall.unsatCore().propositions())
    return Explanation(mus.propositions().filterIsInstance<PrlProposition>())
}
