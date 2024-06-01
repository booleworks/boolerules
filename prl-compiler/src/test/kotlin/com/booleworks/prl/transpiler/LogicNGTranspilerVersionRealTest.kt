package com.booleworks.prl.transpiler;

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.sat.SATSolverConfig
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogicNGTranspilerVersionTest {

    @Test
    fun testLinuxProblem() {
        val compiler = PrlCompiler()
        val model = compiler.compile(parseRuleFile("../test-files/prl/real/linux/package_universe.prl"))
        val cf = CspFactory(FormulaFactory.caching())
        val modelTranslation = transpileModel(cf, model, listOf())

        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(21213)
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
        assertThat(modelTranslation[0].versionVariables).hasSize(19595)

        val trans = modelTranslation[0]
        val solver = SATSolver.newSolver(cf.formulaFactory())
        trans.propositions.forEach {
            solver.add(it.formula().cnf(cf.formulaFactory()))
        }
        assertThat(solver.sat()).isTrue()
    }

    @Test
    fun testSmallExample() {
        val compiler = PrlCompiler()
        val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/version-small.prl"))
        val cf = CspFactory(FormulaFactory.caching())
        val modelTranslation = transpileModel(cf, model, listOf())

        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(100)
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
        assertThat(modelTranslation[0].versionVariables).hasSize(101)

        val trans = modelTranslation[0]
        val solver = SATSolver.newSolver(cf.formulaFactory())
        trans.propositions.forEach {
            solver.add(it.formula().cnf(cf.formulaFactory()))
        }
        assertThat(solver.sat()).isTrue()
    }

    @Test
    fun testToyExample() {
        val compiler = PrlCompiler()
        val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/version-toy-example.prl"))
        val cf = CspFactory(FormulaFactory.caching())
        val modelTranslation = transpileModel(cf, model, listOf())

        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(9)
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
        assertThat(modelTranslation[0].versionVariables).hasSize(10)

        val trans = modelTranslation[0]
        val solver = SATSolver.newSolver(cf.formulaFactory(), SATSolverConfig.builder().proofGeneration(true).build())
        trans.propositions.forEach {
            solver.add(it.formula().cnf(cf.formulaFactory()))
        }
        val musicPlus = cf.formulaFactory().variable("@VER_MusicPlus_2")
        solver.add(musicPlus)
        val models = solver.enumerateAllModels(trans.versionVariables)
        models.forEach { assertThat(it.positiveVariables().contains(musicPlus)).isTrue() }
        assertThat(models).hasSize(12)
    }
}

