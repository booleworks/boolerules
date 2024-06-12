package com.booleworks.boolerules.computations.bomcheck

import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.LIST
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PositionValidationOnlyBooleanNoSlicesTest : TestWithConfig() {

    private val cut = PositionValidationComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl"))

    private val pv1 = PositionVariant("pv1", "Position Variant 1", "zzt") // dead
    private val pv2 = PositionVariant("pv2", "Position Variant 2", "dbj")
    private val pv3 = PositionVariant("pv3", "Position Variant 3", "dbj/xca")
    private val pos = Position("p1", "Position 1", "", listOf(pv1, pv2, pv3))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(339)
    }

    @Test
    fun testComputeForSlice() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val request = PositionValidationRequest(
            "any", mutableListOf(), listOf(),
            listOf(BomCheckType.UNIQUENESS, BomCheckType.COMPLETENESS, BomCheckType.DEAD_PV), pos
        )
        val modelTranslation = transpileModel(cf, model, listOf())
        val status = ComputationStatusBuilder("fileId", "jobId", LIST)
        val res = cut.computeForSlice(
            request,
            Slice.empty(),
            modelTranslation[0].info,
            model,
            cf,
            status,
        )

        assertThat(res.slice).isEqualTo(Slice.empty())
        assertThat(res.position).isEqualTo(pos)

        assertThat(res.slice).isEqualTo(Slice.empty())
        assertThat(res.deadPvs).containsExactly(pv1)
        assertThat(res.nonComplete).isNotNull
        assertThat(res.nonUniquePVs).hasSize(1)
        assertThat(res.nonUniquePVs[0].firstPositionVariant).isEqualTo(pv2)
        assertThat(res.nonUniquePVs[0].secondPositionVariant).isEqualTo(pv3)
    }
}
