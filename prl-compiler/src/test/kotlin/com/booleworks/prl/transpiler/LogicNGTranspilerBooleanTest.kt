package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.slices.BooleanSliceSelection
import com.booleworks.prl.model.slices.computeAllSlices
import com.booleworks.prl.model.slices.computeSliceSets
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogicNGTranspilerBooleanTest {
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/compiler/boolean_with_properties.prl"))
    private val rules = model.rules
    private val sliceSets =
        computeSliceSets(computeAllSlices(listOf(), listOf(model.propertyDefinition("active"))), model)
    private val ss1 = sliceSets[0]
    private val ss2 = sliceSets[1]
    private val f = FormulaFactory.caching()
    private val cf = CspFactory(f)
    private val context = CspEncodingContext()
    private val varDef = encodeIntFeatures(context, cf, model.featureStore)
    private val translation1 = transpileSliceSet(context, cf, varDef, emptyList(), ss1)
    private val translation2 = transpileSliceSet(context, cf, varDef, emptyList(), ss2)
    private val modelTranslation =
        transpileModel(cf, model, listOf(BooleanSliceSelection("active", BooleanRange.list(true))))

    private val f1 = f.variable("f1")
    private val f2 = f.variable("f2")
    private val f3 = f.variable("f3")
    private val f4 = f.variable("f4")
    private val f5 = f.variable("f5")
    private val f6 = f.variable("f6")
    private val g1 = f.variable("g1")

    @Test
    fun testModel() {
        assertThat(model.rules).hasSize(9)
    }

    @Test
    fun testSlice() {
        assertThat(sliceSets).hasSize(2)
        assertThat(translation1.sliceSet).isEqualTo(ss1)
        assertThat(translation1.propositions).hasSize(8)
        assertThat(translation2.sliceSet).isEqualTo(ss2)
        assertThat(translation2.propositions).hasSize(8)
    }

    @Test
    fun testModelTranslation() {
        assertThat(modelTranslation.numberOfComputations).isEqualTo(14)
        modelTranslation.forEach { c ->
            assertThat(c.sliceSet.slices.map { (it.property("active") as BooleanProperty).range }).containsOnly(
                BooleanRange.list(true)
            )
        }
    }

    @Test
    fun testVariables() {
        assertThat(translation1.knownVariables).containsExactlyInAnyOrder(f1, f3, f4, f5, f6, g1)
        assertThat(translation1.unknownFeatures).containsExactlyInAnyOrder(model.getFeature("f2"))
        assertThat(translation2.knownVariables).containsExactlyInAnyOrder(f1, f2, f3, f4, f5, f6, g1)
        assertThat(translation2.unknownFeatures).isEmpty()
    }

    @Test
    fun testInclusionRule() {
        assertThat(translation1.propositions[0]).isEqualTo(PrlProposition(RuleInformation(rules[0], ss1), f.verum()))
    }

    @Test
    fun testExclusionRule() {
        assertThat(translation2.propositions[0]).isEqualTo(
            PrlProposition(RuleInformation(rules[1], ss2), f.implication(f.and(f1, f2), f.not(f.equivalence(f3, f4))))
        )
    }

    @Test
    fun testConstraintRule() {
        assertThat(translation1.propositions[1]).isEqualTo(
            PrlProposition(
                RuleInformation(rules[2], ss1),
                f.exo(f4, f5)
            )
        )
        assertThat(translation2.propositions[1]).isEqualTo(
            PrlProposition(
                RuleInformation(rules[2], ss2),
                f.exo(f4, f5)
            )
        )
    }

    @Test
    fun testGroupRule() {
        assertThat(translation1.propositions[2]).isEqualTo(
            PrlProposition(
                RuleInformation(rules[3], ss1),
                f.and(f.amo(f4, f5, f6), f.equivalence(g1, f.or(f4, f5, f6)))
            )
        )
        assertThat(translation2.propositions[2]).isEqualTo(
            PrlProposition(
                RuleInformation(rules[3], ss2),
                f.and(f.amo(f4, f5, f6), f.equivalence(g1, f.or(f4, f5, f6)))
            )
        )
    }

    @Test
    fun testIfThenElseRule() {
        assertThat(translation1.propositions[3]).isEqualTo(
            PrlProposition(RuleInformation(rules[4], ss1), f.or(f.and(f3, f.or(f4, f5)), f.and(f3.negate(f), f5)))
        )
        assertThat(translation2.propositions[3]).isEqualTo(
            PrlProposition(RuleInformation(rules[4], ss2), f.or(f.and(f3, f.or(f4, f5)), f.and(f3.negate(f), f5)))
        )
    }

    @Test
    fun testDefinitionRule() {
        assertThat(translation1.propositions[4]).isEqualTo(
            PrlProposition(
                RuleInformation(rules[5], ss1),
                f.equivalence(f1, f3)
            )
        )
        assertThat(translation2.propositions[4]).isEqualTo(
            PrlProposition(
                RuleInformation(rules[5], ss2),
                f.equivalence(f1, f.or(f2, f3))
            )
        )
    }

    @Test
    fun testSimplificationFormula() {
        assertThat(translation1.propositions[5]).isEqualTo(PrlProposition(RuleInformation(rules[6], ss1), f.verum()))
        assertThat(translation2.propositions[5]).isEqualTo(PrlProposition(RuleInformation(rules[6], ss2), f.verum()))
    }

    @Test
    fun testMandatoryFeature() {
        assertThat(translation1.propositions[6]).isEqualTo(PrlProposition(RuleInformation(rules[7], ss1), f5))
        assertThat(translation2.propositions[6]).isEqualTo(PrlProposition(RuleInformation(rules[7], ss2), f5))
    }

    @Test
    fun testForbiddenFeature() {
        assertThat(translation1.propositions[7]).isEqualTo(PrlProposition(RuleInformation(rules[8], ss1), f5.negate(f)))
        assertThat(translation2.propositions[7]).isEqualTo(PrlProposition(RuleInformation(rules[8], ss2), f5.negate(f)))
    }
}
