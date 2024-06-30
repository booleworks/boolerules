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
import java.nio.file.Files
import java.nio.file.Paths

internal class PackageSolvingLinuxAlgorithmTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/linux/package_universe.prl"))

    @Test
    fun testInstallOnePackage() {
        val currentInstallation = Files.readAllLines(Paths.get("src/test/resources/linux_install.txt"))
        val install = listOf("truc[=1]", "gnump3d[=1]", "python-dev[=2]", "mp3c[=1]")
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf(), considerConstraints = currentInstallation + install)

        val algo = PackageSolving(currentInstallation, install, listOf(), false)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, modelTranslation[0].info, Slice.empty(), handler)

        assertThat(result.newFeatures.size).isEqualTo(7)
        assertThat(result.removedFeatures.size).isEqualTo(0)
        assertThat(result.changedFeatures.size).isEqualTo(2)
    }

    @Test
    fun testUpgrade() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val currentInstallation = Files.readAllLines(Paths.get("src/test/resources/linux_install.txt"))

        val algo = PackageSolving(currentInstallation, listOf(), listOf(), true)
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, modelTranslation[0].info, Slice.empty(), handler)

        val updated = result.changedFeatures.filter { it.versionNew > it.versionOld }.size
        assertThat(result.newFeatures.size).isEqualTo(103)
        assertThat(result.removedFeatures.size).isEqualTo(0)
        assertThat(result.changedFeatures.size).isEqualTo(109)
        assertThat(updated).isEqualTo(88)
    }
}
