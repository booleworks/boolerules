package com.booleworks.boolerules.computations.packagesolving

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PackageSolvingComputationTest : TestWithConfig() {

    private val cut = PackageSolvingComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/version-toy-example.prl"))

    @Test
    fun testInstallOnePackage() {
        val currentInstallation = listOf("SoundModule[=1]", "Spotify[=1]")
        val install = listOf("MusicPlus[=2]")
        val request = PackageSolvingRequest("fileId", mutableListOf(), listOf(), currentInstallation, install)
        val sb = ComputationStatusBuilder("fileId", "jobId", SINGLE)
        val response = cut.computeResponse(request, model, sb)
        println(response)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(1)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(1)
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(response.merge[0].result.newFeatures).containsExactlyInAnyOrder(
            VersionedFeature("MusicPlus", 0, 2),
            VersionedFeature("SoundHelper", 0, 1),
            VersionedFeature("Tidal", 0, 1)
        )
        assertThat(response.merge[0].result.removedFeatures).isEmpty()
        assertThat(response.merge[0].result.changedFeatures).containsExactlyInAnyOrder(
            VersionedFeature("SoundModule", 1, 2)
        )
    }

}
