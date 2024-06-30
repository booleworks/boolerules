package com.booleworks.boolerules.algorithms.bom

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PositionValidationOnlyBooleanNoSlicesTest {

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
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info = modelTranslation[0].info

        val algo = PositionValidity(
            listOf(BomCheckType.UNIQUENESS, BomCheckType.COMPLETENESS, BomCheckType.DEAD_PV), pos
        )
        val handler = BRTimeoutHandler.fromDuration(60)
        val result = algo.executeForSlice(cf, model, info, Slice.empty(), handler)

        assertThat(result.slice).isEqualTo(Slice.empty())
        assertThat(result.position).isEqualTo(pos)

        assertThat(result.slice).isEqualTo(Slice.empty())
        assertThat(result.deadPvs).containsExactly(pv1)
        assertThat(result.nonComplete).isNotNull
        assertThat(result.nonUniquePVs).hasSize(1)
        assertThat(result.nonUniquePVs[0].firstPositionVariant).isEqualTo(pv2)
        assertThat(result.nonUniquePVs[0].secondPositionVariant).isEqualTo(pv3)
    }
}
