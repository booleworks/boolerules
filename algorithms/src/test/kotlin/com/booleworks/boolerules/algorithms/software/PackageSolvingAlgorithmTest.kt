package com.booleworks.boolerules.algorithms.software

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PackageSolvingAlgorithmTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/version-toy-example.prl"))

    @Test
    fun testInstallOnePackage() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val currentInstallation = listOf("SoundModule[=1]", "Spotify[=1]")
        val install = listOf("MusicPlus[=2]")

        val algo = PackageSolving(currentInstallation, install, listOf(), false)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, modelTranslation[0].info, Slice.empty(), handler)

        assertThat(result.newFeatures).containsExactlyInAnyOrder(
            VersionedFeature("MusicPlus", 0, 2),
            VersionedFeature("SoundHelper", 0, 1),
            VersionedFeature("Tidal", 0, 1)
        )
        assertThat(result.removedFeatures).isEmpty()
        assertThat(result.changedFeatures).containsExactlyInAnyOrder(
            VersionedFeature("SoundModule", 1, 2)
        )
    }
}
