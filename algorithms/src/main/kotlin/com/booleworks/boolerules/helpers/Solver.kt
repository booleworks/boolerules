// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.helpers

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.logicng.solvers.sat.SATSolverConfig
import com.booleworks.prl.transpiler.TranspilationInfo

fun satSolver(cf: CspFactory, proofTracing: Boolean, info: TranspilationInfo): SATSolver {
    val f = cf.formulaFactory()
    val solver = SATSolver.newSolver(f, SATSolverConfig.builder().proofGeneration(proofTracing).build()).apply {
        addPropositions(info.propositions)
    }
    return solver
}

fun maxSATSolver(
    config: MaxSATConfig,
    algo: (FormulaFactory, MaxSATConfig) -> MaxSATSolver,
    f: FormulaFactory,
    info: TranspilationInfo,
): MaxSATSolver {
    val solver = algo(f, config)
    info.propositions.forEach { solver.addHardFormula(it.formula()) }
    return solver
}
