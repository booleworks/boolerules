package com.booleworks.prl.transpiler;

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogicNGTranspilerVersionNoSlicesTest {
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/compiler/simple_version.prl"))
    private val cf = CspFactory(FormulaFactory.caching())
    private val modelTranslation = transpileModel(cf, model, listOf())

    @Test
    fun testModel() {
        assertThat(model.rules).hasSize(6)
        assertThat(modelTranslation.numberOfComputations).isEqualTo(1)
    }

//    @Test
//    fun testIntMapping() {
//        val trans = modelTranslation[0]
//        assertThat(trans.versionMapping).hasSize(3)
//        assertThat(trans.versionMapping["versions.i1"]).containsExactly(
//            entry(0, f.variable("@VER_versions#i1_0")),
//            entry(1, f.variable("@VER_versions#i1_1")),
//            entry(2, f.variable("@VER_versions#i1_2")),
//            entry(3, f.variable("@VER_versions#i1_3")),
//            entry(4, f.variable("@VER_versions#i1_4")),
//            entry(5, f.variable("@VER_versions#i1_5")),
//        )
//        assertThat(trans.versionMapping["versions.i2"]).containsExactly(
//            entry(0, f.variable("@VER_versions#i2_0")),
//            entry(1, f.variable("@VER_versions#i2_1")),
//            entry(2, f.variable("@VER_versions#i2_2")),
//            entry(3, f.variable("@VER_versions#i2_3")),
//        )
//        assertThat(trans.versionMapping["versions.i3"]).containsExactly(
//            entry(0, f.variable("@VER_versions#i3_0")),
//            entry(1, f.variable("@VER_versions#i3_1")),
//            entry(2, f.variable("@VER_versions#i3_2")),
//        )
//        assertThat(trans.versionVariables).hasSize(13)
//    }

//    @Test
//    fun testIntConstraints() {
//        val rules = model.rules
//        val trans = modelTranslation[0]
//        val sliceSet = trans.sliceSet
//        val props = trans.propositions
//        assertThat(props).hasSize(12 + 4 + 5)
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[0], sliceSet),
//                f.parse("@INT_ints#i1_0 | @INT_ints#i1_10 | @INT_ints#i1_20")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[1], sliceSet),
//                f.parse("@INT_ints#i1_20 => @INT_ints#i2_0")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[2], sliceSet),
//                f.parse("@INT_ints#i1_10 => @INT_ints#i2_10")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[3], sliceSet),
//                f.parse("~@INT_ints#i1_0")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[4], sliceSet),
//                f.parse(
//                    "ints.b1 <=> (@INT_ints#i3_0 | @INT_ints#i3_10 | @INT_ints#i3_20) & " +
//                            "(@INT_ints#i4_0 | @INT_ints#i4_10 | @INT_ints#i4_20 | @INT_ints#i4_40 | @INT_ints#i4_100)",
//                )
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[5], sliceSet),
//                f.parse(
//                    "ints.b2 <=> @INT_ints#i3_m20 | @INT_ints#i4_m20 | @INT_ints#i4_m40 | @INT_ints#i4_m100",
//                )
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[6], sliceSet),
//                f.parse(
//                    "ints.b1 => ~(@INT_ints#i3_0 | @INT_ints#i3_10 | @INT_ints#i3_20)",
//                )
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[7], sliceSet),
//                f.parse(
//                    "ints.b2 & (@INT_ints#i4_20 | @INT_ints#i4_40) | " +
//                            "~ints.b2 & (@INT_ints#i4_m40 | @INT_ints#i4_m20 | @INT_ints#i4_0)",
//                )
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[8], sliceSet),
//                f.parse(
//                    " @INT_ints#i3_m20 & @INT_ints#i4_m100 | @INT_ints#i3_m20 & @INT_ints#i4_m40 | " +
//                            "@INT_ints#i3_m20 & @INT_ints#i4_m20 | @INT_ints#i3_0 & @INT_ints#i4_m100 | " +
//                            "@INT_ints#i3_0 & @INT_ints#i4_m40 | @INT_ints#i3_0 & @INT_ints#i4_m20 | " +
//                            "@INT_ints#i3_0 & @INT_ints#i4_0 | @INT_ints#i3_10 & @INT_ints#i4_m100 | " +
//                            "@INT_ints#i3_10 & @INT_ints#i4_m40 | @INT_ints#i3_10 & @INT_ints#i4_m20 | " +
//                            "@INT_ints#i3_10 & @INT_ints#i4_0 | @INT_ints#i3_10 & @INT_ints#i4_10 | " +
//                            "@INT_ints#i3_20 & @INT_ints#i4_m100 | @INT_ints#i3_20 & @INT_ints#i4_m40 | " +
//                            "@INT_ints#i3_20 & @INT_ints#i4_m20 | @INT_ints#i3_20 & @INT_ints#i4_0 | " +
//                            "@INT_ints#i3_20 & @INT_ints#i4_10 | @INT_ints#i3_20 & @INT_ints#i4_20 => ints.b1 ",
//                )
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(rules[9], sliceSet),
//                f.parse(
//                    "@INT_ints#i1_0 | @INT_ints#i1_10 | @INT_ints#i1_50 | @INT_ints#i1_100",
//                )
//            )
//        )
//        assertThat(props).contains(PrlProposition(RuleInformation(rules[10], sliceSet), f.parse("\$true")))
//        assertThat(props).contains(PrlProposition(RuleInformation(rules[11], sliceSet), f.parse("\$true")))
//    }
//
//    @Test
//    fun testIntMetaPropositionsExo() {
//        val trans = modelTranslation[0]
//        val sliceSet = trans.sliceSet
//        val props = trans.propositions
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_FEATURE_CONSTRAINT, sliceSet),
//                f.parse("@INT_ints#i1_0 + @INT_ints#i1_10 + @INT_ints#i1_20 + @INT_ints#i1_50 + @INT_ints#i1_100 = 1")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_FEATURE_CONSTRAINT, sliceSet),
//                f.parse(
//                    "@INT_ints#i2_0 + @INT_ints#i2_2 + @INT_ints#i2_4 + @INT_ints#i2_6 + @INT_ints#i2_8 + " +
//                            "@INT_ints#i2_10 + @INT_ints#i2_12 + @INT_ints#i2_14 + @INT_ints#i2_16 + " +
//                            "@INT_ints#i2_18 + @INT_ints#i2_20 = 1"
//                )
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_FEATURE_CONSTRAINT, sliceSet),
//                f.parse("@INT_ints#i3_m20 + @INT_ints#i3_0 + @INT_ints#i3_10 + @INT_ints#i3_20 = 1")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_FEATURE_CONSTRAINT, sliceSet),
//                f.parse(
//                    "@INT_ints#i4_m100 + @INT_ints#i4_m40 + @INT_ints#i4_m20 + @INT_ints#i4_0 +" +
//                            "@INT_ints#i4_10 + @INT_ints#i4_20 + @INT_ints#i4_40 + @INT_ints#i4_100 = 1"
//                )
//            )
//        )
//    }
//
//    @Test
//    fun testIntMetaPropositionsExclusions() {
//        val trans = modelTranslation[0]
//        val sliceSet = trans.sliceSet
//        val props = trans.propositions
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_OUT_OF_BOUNDS_CONSTRAINT, sliceSet),
//                f.parse("~@INT_ints#i2_30")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_OUT_OF_BOUNDS_CONSTRAINT, sliceSet),
//                f.parse("~@INT_ints#i3_m100")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_OUT_OF_BOUNDS_CONSTRAINT, sliceSet),
//                f.parse("~@INT_ints#i3_m40")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_OUT_OF_BOUNDS_CONSTRAINT, sliceSet),
//                f.parse("~@INT_ints#i3_40")
//            )
//        )
//        assertThat(props).contains(
//            PrlProposition(
//                RuleInformation(RuleType.INT_OUT_OF_BOUNDS_CONSTRAINT, sliceSet),
//                f.parse("~@INT_ints#i3_100")
//            )
//        )
//    }
//
//    @Test
//    fun testIntSolving() {
//        val trans = modelTranslation[0]
//        val solver = MiniSat.miniSat(f)
//        solver.addPropositions(trans.propositions)
//        assertThat(solver.sat()).isEqualTo(Tristate.TRUE)
//        val models = solver.enumerateAllModels(trans.intVariables)
//        models.forEach { println(it.positiveVariables()) }
//        assertThat(models).hasSize(2)
//    }
}

