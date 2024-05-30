package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.sat.SATSolverConfig
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SliceMergeTest {
    private val f = FormulaFactory.caching()
    private val cf = CspFactory(f)

    private val a = f.variable("a")
    private val b = f.variable("b")
    private val c = f.variable("c")
    private val p = f.variable("p")
    private val q = f.variable("q")
    private val r = f.variable("r")
    private val x = f.variable("x")
    private val y = f.variable("y")
    private val z = f.variable("z")

    @Test
    fun testModel1() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge1.prl"))
        val modelTranslation = transpileModel(cf, model, listOf())
        assertThat(modelTranslation).hasSize(3)
        assertThat(modelTranslation.computations[0].knownVariables).containsExactly(a, b, c, x)
        assertThat(modelTranslation.computations[1].knownVariables).containsExactly(a, b, c, x)
        assertThat(modelTranslation.computations[2].knownVariables).containsExactly(a, b, c)
    }

    @Test
    fun testModel2() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge2.prl"))
        val modelTranslation = transpileModel(cf, model, listOf())
        assertThat(modelTranslation).hasSize(9)
        assertThat(modelTranslation.computations[0].knownVariables).containsExactly(a, b, c, p, x)
        assertThat(modelTranslation.computations[1].knownVariables).containsExactly(a, b, c, q, x)
        assertThat(modelTranslation.computations[2].knownVariables).containsExactly(a, b, c, r, x)
        assertThat(modelTranslation.computations[3].knownVariables).containsExactly(a, b, c, p, x, y, z)
        assertThat(modelTranslation.computations[4].knownVariables).containsExactly(a, b, c, q, x, y, z)
        assertThat(modelTranslation.computations[5].knownVariables).containsExactly(a, b, c, r, x, y, z)
        assertThat(modelTranslation.computations[6].knownVariables).containsExactly(a, b, c, p, z)
        assertThat(modelTranslation.computations[7].knownVariables).containsExactly(a, b, c, q, z)
        assertThat(modelTranslation.computations[8].knownVariables).containsExactly(a, b, c, r, z)
    }

    @Test
    fun testSimpleSliceMerge1() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge1.prl"))
        val modelTranslation = transpileModel(cf, model, listOf())
        val merged = mergeSlices(cf, modelTranslation.computations)
        assertThat(merged.sliceSelectors).hasSize(3)
        assertThat(merged.knownVariables).containsExactly(a, b, c, x)
        assertThat(merged.propositions).hasSize(16)
        assertThat(merged.booleanVariables).containsExactly(a, b, c, x)
        assertThat(merged.enumVariables).isEmpty()
        assertThat(merged.enumMapping).isEmpty()
        val solver = SATSolver.newSolver(f)
        solver.addPropositions(merged.propositions)
        val models = solver.enumerateAllModels(listOf(a, b, c))
        assertThat(models).hasSize(2)
        models.forEach {
            assertThat(it.positiveVariables()).contains(a)
            assertThat(it.positiveVariables()).contains(b)
        }
    }

    @Test
    fun testSimpleSliceMerge2() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge2.prl"))
        val modelTranslation = transpileModel(cf, model, listOf())
        val merged = mergeSlices(cf, modelTranslation.computations)
        assertThat(merged.sliceSelectors).hasSize(9)
        assertThat(merged.knownVariables).containsExactly(a, b, c, p, q, r, x, y, z)
        assertThat(filter(merged.propositions, "@SL0")).hasSize(9 + 4 + 4)
        assertThat(filter(merged.propositions, "@SL1")).hasSize(9 + 4 + 5)
        assertThat(filter(merged.propositions, "@SL2")).hasSize(9 + 4 + 4)
        assertThat(filter(merged.propositions, "@SL3")).hasSize(9 + 2 + 5)
        assertThat(filter(merged.propositions, "@SL4")).hasSize(9 + 2 + 7)
        assertThat(filter(merged.propositions, "@SL5")).hasSize(9 + 2 + 5)
        assertThat(filter(merged.propositions, "@SL6")).hasSize(9 + 4 + 3)
        assertThat(filter(merged.propositions, "@SL7")).hasSize(9 + 4 + 4)
        assertThat(filter(merged.propositions, "@SL8")).hasSize(9 + 4 + 3)
        assertThat(merged.propositions).hasSize(166)
        val solver = SATSolver.newSolver(f, SATSolverConfig.builder().proofGeneration(true).build())
        solver.addPropositions(merged.propositions)
        assertThat(solver.sat()).isEqualTo(true)
        val models = solver.enumerateAllModels(listOf(a, b, c, p, q, r, x, y, z))
        assertThat(models).hasSize(2)
        models.forEach {
            assertThat(it.positiveVariables()).contains(a)
            assertThat(it.positiveVariables()).contains(b)
            assertThat(it.negativeVariables()).contains(p)
            assertThat(it.negativeVariables()).contains(q)
            assertThat(it.negativeVariables()).contains(r)
            assertThat(it.negativeVariables()).contains(x)
            assertThat(it.negativeVariables()).contains(y)
            assertThat(it.negativeVariables()).contains(z)
        }
    }

    @Test
    fun testSimpleSliceMerge3() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))
        val modelTranslation = transpileModel(cf, model, listOf())
        val merged = mergeSlices(cf, modelTranslation.computations)

        val allVariables = f.variables(
            "@ENUM_a_a1", "@ENUM_a_a2",
            "@ENUM_b_b1", "@ENUM_b_b2", "@ENUM_b_b3",
            "@ENUM_c_c1", "@ENUM_c_c2", "@ENUM_c_c3",
            "@ENUM_p_px", "@ENUM_p_p1", "@ENUM_p_p2",
            "@ENUM_q_q1", "@ENUM_q_q2"
        )

        assertThat(merged.sliceSelectors).hasSize(4)
        assertThat(merged.unknownFeatures).isEmpty()
        assertThat(merged.booleanVariables).isEmpty()
        assertThat(merged.enumVariables).containsExactlyInAnyOrderElementsOf(allVariables)
        assertThat(merged.enumMapping).hasSize(5)
        assertThat(merged.info.getFeatureAndValue(f.variable("@ENUM_c_c3"))).isEqualTo(Pair("c", "c3"))
        assertThat(merged.knownVariables).containsExactlyInAnyOrderElementsOf(allVariables)
        assertThat(filter(merged.propositions, "@SL0")).hasSize(13 + 4 + 7)
        assertThat(filter(merged.propositions, "@SL1")).hasSize(13 + 2 + 8)
        assertThat(filter(merged.propositions, "@SL2")).hasSize(13 + 3 + 7)
        assertThat(filter(merged.propositions, "@SL3")).hasSize(13 + 1 + 9)
        assertThat(merged.propositions).hasSize(94)
        val solver = SATSolver.newSolver(f, SATSolverConfig.builder().proofGeneration(true).build())
        solver.addPropositions(merged.propositions)
        assertThat(solver.sat()).isEqualTo(false)
    }

    private fun filter(props: List<PrlProposition>, sel: String) =
        props.filter { it.formula().variables(f).any { v -> v.name().contains(sel) } }
}
