package com.booleworks.prl.transpiler;

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import java.util.TreeSet

class LogicNGTranspilerVersionNoSlicesTest {
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/compiler/simple_version.prl"))
    private val cf = CspFactory(FormulaFactory.caching())
    private val f = cf.formulaFactory()
    private val modelTranslation = transpileModel(cf, model, listOf())

    @Test
    fun testModel() {
        assertThat(model.rules).hasSize(7)
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
    }

    @Test
    fun testVersionMapping() {
        val trans = modelTranslation[0]
        assertThat(trans.info.versionMapping.keys).containsExactlyInAnyOrderElementsOf(f.variables("i1", "i2", "i3"))

        assertThat(trans.info.versionMapping[f.variable("i1")]).containsExactly(
            entry(1, f.variable("@VER_i1_1")),
            entry(2, f.variable("@VER_i1_2")),
            entry(3, f.variable("@VER_i1_3")),
            entry(4, f.variable("@VER_i1_4")),
        )

        assertThat(trans.info.versionMapping[f.variable("i2")]).containsExactly(
            entry(1, f.variable("@VER_i2_1")),
            entry(2, f.variable("@VER_i2_2")),
            entry(3, f.variable("@VER_i2_3")),
        )
        assertThat(trans.info.versionMapping[f.variable("i3")]).containsExactly(
            entry(1, f.variable("@VER_i3_1")),
            entry(2, f.variable("@VER_i3_2")),
            entry(3, f.variable("@VER_i3_3")),
        )

        assertThat(trans.info.versionVariables).containsExactlyInAnyOrderElementsOf(
            f.variables(
                "@VER_i1_1", "@VER_i1_2", "@VER_i1_3", "@VER_i1_4",
                "@VER_i2_1", "@VER_i2_2", "@VER_i2_3",
                "@VER_i3_1", "@VER_i3_2", "@VER_i3_3",
            )
        )
    }

    @Test
    fun testVersionConstraints() {
        val trans = modelTranslation[0]
        val props = trans.info.propositions
        assertThat(props).hasSize(7 + 3 + 3)
    }

    @Test
    fun testVersionOriginalConstraints() {
        val rules = model.rules
        val trans = modelTranslation[0]
        val sliceSet = trans.sliceSet
        val props = trans.info.propositions

        val original = props.filter { it.backpack().ruleType == RuleType.ORIGINAL_RULE }
        assertThat(original).hasSize(7)

        // rule i1[ < 5]
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[0], sliceSet),
                f.parse("@VER_i1_1 | @VER_i1_2 | @VER_i1_3 | @VER_i1_4")
            )
        )
        // rule if i1[ = 1] then i2[ = 3]
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[1], sliceSet),
                f.parse("@VER_i1_1 => @VER_i2_3")
            )
        )
        // rule if i1[ = 2] then i2[ > 1]
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[2], sliceSet),
                f.parse("@VER_i1_2 => (@VER_i2_2 | @VER_i2_3)")
            )
        )
        // rule if i1[ = 3] then i2
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[3], sliceSet),
                f.parse("@VER_i1_3 => i2")
            )
        )
        // rule if i2 then -i3
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[4], sliceSet),
                f.parse("i2 => ~i3")
            )
        )
        // rule b1 is i1[ >= 2] & i3[ > 2]
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[5], sliceSet),
                f.parse("b1 <=> (@VER_i1_2 | @VER_i1_3 | @VER_i1_4) & @VER_i3_3")
            )
        )
        // rule forbidden feature i1 = 2
        assertThat(original).contains(
            PrlProposition(
                RuleInformation(rules[6], sliceSet),
                f.parse("~@VER_i1_2")
            )
        )
    }

    @Test
    fun testVersionAmoConstraints() {
        val trans = modelTranslation[0]
        val sliceSet = trans.sliceSet
        val props = trans.info.propositions

        val amo = props.filter { it.backpack().ruleType == RuleType.VERSION_AMO_CONSTRAINT }
        assertThat(amo).hasSize(3)

        assertThat(amo).contains(
            PrlProposition(
                RuleInformation(RuleType.VERSION_AMO_CONSTRAINT, sliceSet),
                f.parse("@VER_i1_1 + @VER_i1_2 + @VER_i1_3 + @VER_i1_4 <= 1")
            )
        )
        assertThat(amo).contains(
            PrlProposition(
                RuleInformation(RuleType.VERSION_AMO_CONSTRAINT, sliceSet),
                f.parse("@VER_i2_1 + @VER_i2_2 + @VER_i2_3 <= 1")
            )
        )
        assertThat(amo).contains(
            PrlProposition(
                RuleInformation(RuleType.VERSION_AMO_CONSTRAINT, sliceSet),
                f.parse("@VER_i3_1 + @VER_i3_2 + @VER_i3_3 <= 1")
            )
        )
    }

    @Test
    fun testVersionEquivConstraints() {
        val trans = modelTranslation[0]
        val sliceSet = trans.sliceSet
        val props = trans.info.propositions

        val equiv = props.filter { it.backpack().ruleType == RuleType.VERSION_EQUIVALENCE }
        assertThat(equiv).hasSize(3)

        assertThat(equiv).contains(
            PrlProposition(
                RuleInformation(RuleType.VERSION_EQUIVALENCE, sliceSet),
                f.parse("i1 <=> @VER_i1_1 | @VER_i1_2 | @VER_i1_3 | @VER_i1_4")
            )
        )
        assertThat(equiv).contains(
            PrlProposition(
                RuleInformation(RuleType.VERSION_EQUIVALENCE, sliceSet),
                f.parse("i2 <=> @VER_i2_1 | @VER_i2_2 | @VER_i2_3")
            )
        )
        assertThat(equiv).contains(
            PrlProposition(
                RuleInformation(RuleType.VERSION_EQUIVALENCE, sliceSet),
                f.parse("i3 <=> @VER_i3_1 | @VER_i3_2 | @VER_i3_3")
            )
        )
    }

    @Test
    fun testVersionSolvingUnversionedVars() {
        val trans = modelTranslation[0]
        val solver = SATSolver.newSolver(f)
        solver.addPropositions(trans.info.propositions)
        assertThat(solver.sat()).isTrue()
        var models = solver.enumerateAllModels(f.variables("i1", "i2", "i3"))
        models.forEach { assertThat(it.positiveVariables().contains(f.variable("i1"))) }
        assertThat(models).hasSize(3)

        models = solver.enumerateAllModels(f.variables("b1", "i1", "i2", "i3"))
        models.forEach { assertThat(it.positiveVariables().contains(f.variable("i1"))) }
        assertThat(models).hasSize(4)
    }

    @Test
    fun testVersionSolvingVersionedVars() {
        val trans = modelTranslation[0]
        val solver = SATSolver.newSolver(f)
        solver.addPropositions(trans.info.propositions)
        assertThat(solver.sat()).isTrue()
        val models = solver.enumerateAllModels(trans.info.versionVariables)
        assertThat(models).hasSize(11)
        assertThat(models.map { it.positiveVariables() }).containsExactlyInAnyOrder(
            TreeSet(f.variables("@VER_i1_1", "@VER_i2_3")),
            TreeSet(f.variables("@VER_i1_3", "@VER_i2_1")),
            TreeSet(f.variables("@VER_i1_3", "@VER_i2_2")),
            TreeSet(f.variables("@VER_i1_3", "@VER_i2_3")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i2_1")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i2_2")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i2_3")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i3_1")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i3_2")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i3_3")),
            TreeSet(f.variables("@VER_i1_4")),
        )
    }

    @Test
    fun testVersionSolvingVersionedVarsWithBoolen() {
        val trans = modelTranslation[0]
        val solver = SATSolver.newSolver(f)
        solver.addPropositions(trans.info.propositions)
        assertThat(solver.sat()).isTrue()
        val models = solver.enumerateAllModels(trans.info.versionVariables + f.variables("b1"))
        assertThat(models).hasSize(11)
        assertThat(models.map { it.positiveVariables() }).contains(
            TreeSet(f.variables("@VER_i1_1", "@VER_i2_3")),
            TreeSet(f.variables("@VER_i1_3", "@VER_i2_1")),
            TreeSet(f.variables("@VER_i1_3", "@VER_i2_2")),
            TreeSet(f.variables("@VER_i1_3", "@VER_i2_3")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i2_1")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i2_2")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i2_3")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i3_1")),
            TreeSet(f.variables("@VER_i1_4", "@VER_i3_2")),
            TreeSet(f.variables("@VER_i1_4")),
            TreeSet(f.variables("b1", "@VER_i1_4", "@VER_i3_3")),
        )
    }
}

