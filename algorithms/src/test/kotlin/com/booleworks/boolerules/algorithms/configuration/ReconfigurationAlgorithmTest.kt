package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureInstance
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ReconfigurationAlgorithmTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    @Test
    fun testEmptyOrder() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info

        val algo = Reconfiguration(listOf(), ReconfigurationAlgorithm.MAX_COV)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.featuresToRemove).isEmpty()
        assertThat(result.featuresToAdd).hasSize(9)
    }

    @Test
    fun testMaxCov() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info
        val order = listOf(
            FeatureInstance.boolean("qzb"),
            FeatureInstance.boolean("zhd"),
            FeatureInstance.boolean("pow"),
            FeatureInstance.boolean("lmp"),
            FeatureInstance.boolean("xvo"),
            FeatureInstance.boolean("wyl"),
            FeatureInstance.boolean("zqm"),
            FeatureInstance.boolean("atu"),
            FeatureInstance.boolean("vpf"),
            FeatureInstance.boolean("vti"),
            FeatureInstance.boolean("suz"),
            FeatureInstance.boolean("gao"),
            FeatureInstance.boolean("csq"),
            FeatureInstance.boolean("emc"),
            FeatureInstance.boolean("eop"),
            FeatureInstance.boolean("jlt"),
            FeatureInstance.boolean("dxg"),
            FeatureInstance.boolean("xpx")
        )

        val algo = Reconfiguration(order, ReconfigurationAlgorithm.MAX_COV)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.featuresToRemove).isSubsetOf(order).hasSize(4)
        assertThat(result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(15)
    }

    @Test
    fun testMinDiff() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info
        val order = listOf(
            FeatureInstance.boolean("qzb"),
            FeatureInstance.boolean("zhd"),
            FeatureInstance.boolean("pow"),
            FeatureInstance.boolean("lmp"),
            FeatureInstance.boolean("xvo"),
            FeatureInstance.boolean("wyl"),
            FeatureInstance.boolean("zqm"),
            FeatureInstance.boolean("atu"),
            FeatureInstance.boolean("vpf"),
            FeatureInstance.boolean("vti"),
            FeatureInstance.boolean("suz"),
            FeatureInstance.boolean("gao"),
            FeatureInstance.boolean("csq"),
            FeatureInstance.boolean("emc"),
            FeatureInstance.boolean("eop"),
            FeatureInstance.boolean("jlt"),
            FeatureInstance.boolean("dxg"),
            FeatureInstance.boolean("xpx")
        )

        val algo = Reconfiguration(order, ReconfigurationAlgorithm.MIN_DIFF)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.featuresToRemove).isSubsetOf(order).hasSize(7)
        assertThat(result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(12)
    }

    @Test
    fun testUnknownCodes() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info
        val order = listOf(
            FeatureInstance.boolean("YYYY"),
            FeatureInstance.boolean("qzb"),
            FeatureInstance.boolean("zhd"),
            FeatureInstance.boolean("pow"),
            FeatureInstance.boolean("lmp"),
            FeatureInstance.boolean("xvo"),
            FeatureInstance.boolean("wyl"),
            FeatureInstance.boolean("zqm"),
            FeatureInstance.boolean("atu"),
            FeatureInstance.boolean("vpf"),
            FeatureInstance.boolean("vti"),
            FeatureInstance.boolean("suz"),
            FeatureInstance.boolean("gao"),
            FeatureInstance.boolean("csq"),
            FeatureInstance.boolean("emc"),
            FeatureInstance.boolean("eop"),
            FeatureInstance.boolean("jlt"),
            FeatureInstance.boolean("dxg"),
            FeatureInstance.boolean("xpx"),
            FeatureInstance.boolean("XXXX")
        )

        val algo = Reconfiguration(order, ReconfigurationAlgorithm.MAX_COV)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.featuresToRemove).isSubsetOf(order)
            .contains(FeatureInstance.boolean("YYYY"), FeatureInstance.boolean("XXXX")).hasSize(6)
        assertThat(result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(15)
    }

    @Test
    fun testAdditionalRestrictionsWithMaxCov() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val additionalConstraints = listOf("qzb / zhd => zqm & vti", "xpx => blw / pzg")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraints)
        val info = modelTranslation[0].info
        val order = listOf(
            FeatureInstance.boolean("qzb"),
            FeatureInstance.boolean("zhd"),
            FeatureInstance.boolean("pow"),
            FeatureInstance.boolean("lmp"),
            FeatureInstance.boolean("xvo"),
            FeatureInstance.boolean("wyl"),
            FeatureInstance.boolean("zqm"),
            FeatureInstance.boolean("atu"),
            FeatureInstance.boolean("vpf"),
            FeatureInstance.boolean("vti"),
            FeatureInstance.boolean("suz"),
            FeatureInstance.boolean("gao"),
            FeatureInstance.boolean("csq"),
            FeatureInstance.boolean("emc"),
            FeatureInstance.boolean("eop"),
            FeatureInstance.boolean("jlt"),
            FeatureInstance.boolean("dxg"),
            FeatureInstance.boolean("xpx")
        )

        val algo = Reconfiguration(order, ReconfigurationAlgorithm.MAX_COV)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.featuresToRemove).isSubsetOf(order).hasSize(6)
        assertThat(result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(16)
    }

    @Test
    fun testAdditionalRestrictionsWithMinDiff() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val additionalConstraints = listOf("qzb / zhd => zqm & vti", "xpx => blw / pzg")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraints)
        val info = modelTranslation[0].info
        val order = listOf(
            FeatureInstance.boolean("qzb"),
            FeatureInstance.boolean("zhd"),
            FeatureInstance.boolean("pow"),
            FeatureInstance.boolean("lmp"),
            FeatureInstance.boolean("xvo"),
            FeatureInstance.boolean("wyl"),
            FeatureInstance.boolean("zqm"),
            FeatureInstance.boolean("atu"),
            FeatureInstance.boolean("vpf"),
            FeatureInstance.boolean("vti"),
            FeatureInstance.boolean("suz"),
            FeatureInstance.boolean("gao"),
            FeatureInstance.boolean("csq"),
            FeatureInstance.boolean("emc"),
            FeatureInstance.boolean("eop"),
            FeatureInstance.boolean("jlt"),
            FeatureInstance.boolean("dxg"),
            FeatureInstance.boolean("xpx")
        )

        val algo = Reconfiguration(order, ReconfigurationAlgorithm.MIN_DIFF)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.featuresToRemove).isSubsetOf(order).hasSize(10)
        assertThat(result.featuresToAdd).doesNotContainAnyElementsOf(order).hasSize(11)
    }
}
