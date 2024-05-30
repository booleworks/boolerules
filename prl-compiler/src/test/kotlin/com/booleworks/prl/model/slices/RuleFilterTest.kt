package com.booleworks.prl.model.slices

import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RuleFilterTest {
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/compiler/boolean_with_properties.prl"))
    private val rules = model.rules

    @Test
    fun testModel() {
        assertThat(model.rules).hasSize(9)
    }

    @Test
    fun testRuleFilterActive() {
        val selTrue = listOf(BooleanSliceSelection("active", BooleanRange.list(true)))
        val selFalse = listOf(BooleanSliceSelection("active", BooleanRange.list(false)))
        val selBoth = listOf(BooleanSliceSelection("active", BooleanRange.list(false, true)))
        val filtered1 = model.rules(selTrue)
        val filtered2 = model.rules(selFalse)
        val filtered3 = model.rules(selBoth)
        assertThat(filtered1).containsExactly(
            rules[1],
            rules[2],
            rules[3],
            rules[4],
            rules[5],
            rules[6],
            rules[7],
            rules[8]
        )
        assertThat(filtered2).containsExactly(
            rules[0],
            rules[2],
            rules[3],
            rules[4],
            rules[5],
            rules[6],
            rules[7],
            rules[8]
        )
        assertThat(filtered3).containsExactly(
            rules[0],
            rules[1],
            rules[2],
            rules[3],
            rules[4],
            rules[5],
            rules[6],
            rules[7],
            rules[8]
        )
    }

    @Test
    fun testRuleFilterVersion() {
        val sel1 = listOf(IntSliceSelection("version", IntRange.list(1)))
        val sel3_5 = listOf(IntSliceSelection("version", IntRange.interval(3, 5)))
        val sel2_4 = listOf(IntSliceSelection("version", IntRange.interval(2, 4)))
        val sel24 = listOf(IntSliceSelection("version", IntRange.list(2, 4)))
        val filtered1 = model.rules(sel1)
        val filtered2 = model.rules(sel3_5)
        val filtered3 = model.rules(sel2_4)
        val filtered4 = model.rules(sel24)
        assertThat(filtered1).containsExactly(
            rules[0],
            rules[1],
            rules[2],
            rules[4],
            rules[5],
            rules[6],
            rules[7],
            rules[8]
        )
        assertThat(filtered2).containsExactly(
            rules[0],
            rules[1],
            rules[3],
            rules[4],
            rules[5],
            rules[6],
            rules[7],
            rules[8]
        )
        assertThat(filtered3).containsExactly(
            rules[0],
            rules[1],
            rules[2],
            rules[3],
            rules[4],
            rules[5],
            rules[6],
            rules[7],
            rules[8]
        )
        assertThat(filtered4).containsExactly(
            rules[0],
            rules[1],
            rules[2],
            rules[3],
            rules[4],
            rules[5],
            rules[6],
            rules[7]
        )
    }

    @Test
    fun testRuleFilterCombined() {
        val sel1 = listOf(
            BooleanSliceSelection("active", BooleanRange.list(true)),
            IntSliceSelection("version", IntRange.list(1)),
            EnumSliceSelection("series", EnumRange.list("S2"))
        )
        val sel2 = listOf(
            BooleanSliceSelection("active", BooleanRange.list(true)),
            IntSliceSelection("version", IntRange.list(6)),
            EnumSliceSelection("series", EnumRange.list("S1", "S3"))
        )
        val filtered1 = model.rules(sel1)
        val filtered2 = model.rules(sel2)
        assertThat(filtered1).containsExactly(rules[1], rules[2], rules[5], rules[7])
        assertThat(filtered2).containsExactly(rules[1], rules[6])
    }
}
