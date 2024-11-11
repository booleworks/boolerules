package com.booleworks.boolerules.computations.packagesolving

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import java.nio.file.Files
import java.nio.file.Paths
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PackageSolvingLinuxComputationTest : TestWithConfig() {

    private val cut = PackageSolvingComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/linux/package_universe.prl"))

    @Test
    fun testInstallOnePackage() {
        val currentInstallation = Files.readAllLines(Paths.get("src/test/resources/linux_install.txt"))
        val install = listOf("truc[=1]", "gnump3d[=1]", "python-dev[=2]", "mp3c[=1]")
        val request = PackageSolvingRequest("fileId", mutableListOf(), listOf(), currentInstallation, install)
        val sb = ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.newFeatures.size).isEqualTo(7)
        assertThat(response.merge[0].result.removedFeatures.size).isEqualTo(0)
        assertThat(response.merge[0].result.changedFeatures.size).isEqualTo(2)
    }

    @Test
    fun testUpgrade() {
        val currentInstallation = Files.readAllLines(Paths.get("src/test/resources/linux_install.txt"))
        val request = PackageSolvingRequest("fileId", mutableListOf(), listOf(), currentInstallation, update = true)
        val sb = ComputationStatusBuilder("fileId", "jobId", ComputationVariant.SINGLE)
        val response = cut.computeResponse(request, model, sb)
        val updated = response.merge[0].result.changedFeatures.filter { it.versionNew > it.versionOld }.size
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        //TODO: Adapt for new MaxSat Solver
        //assertThat(response.merge[0].result.newFeatures.size).isEqualTo(103)
        //assertThat(response.merge[0].result.removedFeatures.size).isEqualTo(0)
        //assertThat(response.merge[0].result.changedFeatures.size).isEqualTo(109)
        //assertThat(updated).isEqualTo(88)
    }
}
