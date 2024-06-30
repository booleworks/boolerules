package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class CoverageAlgorithmNoSlicesTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    @Test
    fun testComputeWithFeatures() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info

        val buildable = listOf(
            "qzb",
            "zhd",
            "pow",
            "lmp",
            "xvo",
            "wyl",
            "zqm",
            "atu",
            "vpf",
            "vti",
            "suz",
            "gao",
            "csq",
            "emc",
            "eop",
            "jlt",
            "dxg",
            "xpx",
            "bhy",
            "fdj"
        )
        val notBuildable = listOf(
            "oda",
            "aai",
            "zgg",
            "dez"
        )

        val algo = Coverage(buildable + notBuildable, false)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.slice).isEqualTo(Slice.empty())
        assertThat(result.result).hasSize(3)
        assertThat(result.result.flatMap { it.coveredConstraints }).containsAll(buildable)
        assertThat(result.uncoverableConstraints).isEqualTo(4)
    }

    @Test
    fun testComputeWithFeaturesAndAdditionalRestriction() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = listOf("vti => -zhd"))
        val info = modelTranslation[0].info

        val constraints = listOf(
            "qzb & zhd",
            "-qzb / vti",
            "pow / lmp & -xvo & -bhy",
            "wyl & -zqm & emc",
            "-atu & xpx & bhy / oda"
        )

        val algo = Coverage(constraints, false)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.slice).isEqualTo(Slice.empty())
        assertThat(result.result).hasSize(2)
        assertThat(result.result.flatMap { it.coveredConstraints }).containsAll(constraints)
        assertThat(result.result).allMatch { it.coveredConstraints.isNotEmpty() }
        assertThat(result.uncoverableConstraints).isZero()
    }

    @Test
    fun testComputeWithPairwiseCover() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info

        val buildable = listOf("vti", "suz", "gao", "csq", "emc", "xpx", "bhy", "fdj")
        val buildableCombinations: List<Pair<String, String>> = buildable.indices
            .flatMap { i -> (i + 1 until buildable.size).map { j -> buildable[i] to buildable[j] } }
        val notBuildable = listOf("oda")

        val algo1 = Coverage(buildable + notBuildable, true)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo1.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.result).hasSize(4)
        assertThat(result1.result.flatMap { it.coveredConstraints }).containsAll(buildableCombinations.map { "[${it.first}, ${it.second}]" } - listOf(
            "[suz, bhy]",
            "[csq, emc]"
        ))
        assertThat(result1.uncoverableConstraints).isEqualTo(10)

        val algo2 = Coverage(buildableCombinations.map { "${it.first} & ${it.second}" }, true)
        val result2 = algo2.executeForSlice(cf, model, info, Slice.empty(), handler)
        assertThat(result2.result).hasSameSizeAs(result1.result)
    }
}
