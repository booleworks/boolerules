package com.booleworks.prl.parser

import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.ComparisonOperator.EQ
import com.booleworks.prl.model.constraints.ComparisonOperator.GE
import com.booleworks.prl.model.constraints.ComparisonOperator.GT
import com.booleworks.prl.model.constraints.ComparisonOperator.LE
import com.booleworks.prl.model.constraints.ComparisonOperator.LT
import com.booleworks.prl.model.constraints.ComparisonOperator.NE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConstraintsTest {

    @Test
    fun testConstant() {
        assertThat(parseConstraint<PrlConstant>("true")).isEqualTo(PrlConstant(true))
        assertThat(parseConstraint<PrlConstant>("false")).isEqualTo(PrlConstant(false))
    }

    @Test
    fun testFeature() {
        assertThat(parseConstraint<PrlFeature>("a")).isEqualTo(PrlFeature("a"))
        assertThat(parseConstraint<PrlFeature>("A")).isEqualTo(PrlFeature("A"))
        assertThat(parseConstraint<PrlFeature>("`a feature &# x`")).isEqualTo(PrlFeature("a feature &# x"))
    }

    @Test
    fun testVersionPredicate() {
        assertThat(parseConstraint<PrlComparisonPredicate>("a[= 1]")).isEqualTo(PrlComparisonPredicate(EQ, PrlFeature("a"), PrlIntValue(1)))
        assertThat(parseConstraint<PrlComparisonPredicate>("a[!= 1]")).isEqualTo(PrlComparisonPredicate(NE, PrlFeature("a"), PrlIntValue(1)))
        assertThat(parseConstraint<PrlComparisonPredicate>("a[> 17]")).isEqualTo(PrlComparisonPredicate(GT, PrlFeature("a"), PrlIntValue(17)))
        assertThat(parseConstraint<PrlComparisonPredicate>("a[>= 1]")).isEqualTo(PrlComparisonPredicate(GE, PrlFeature("a"), PrlIntValue(1)))
        assertThat(parseConstraint<PrlComparisonPredicate>("a[< 42]")).isEqualTo(PrlComparisonPredicate(LT, PrlFeature("a"), PrlIntValue(42)))
        assertThat(parseConstraint<PrlComparisonPredicate>("`f 1`[<= 1]")).isEqualTo(PrlComparisonPredicate(LE, PrlFeature("f 1"), PrlIntValue(1)))
    }

    @Test
    fun testComparisonPredicate() {
        assertThat(parseConstraint<PrlComparisonPredicate>("[a = b]")).isEqualTo(PrlComparisonPredicate(EQ, PrlFeature("a"), PrlFeature("b")))
        assertThat(parseConstraint<PrlComparisonPredicate>("[a = -4]")).isEqualTo(PrlComparisonPredicate(EQ, PrlFeature("a"), PrlIntValue(-4)))
        assertThat(parseConstraint<PrlComparisonPredicate>("[4 = b]")).isEqualTo(PrlComparisonPredicate(EQ, PrlIntValue(4), PrlFeature("b")))
        assertThat(parseConstraint<PrlComparisonPredicate>("[\"text\" = b]")).isEqualTo(PrlComparisonPredicate(EQ, PrlEnumValue("text"), PrlFeature("b")))
        assertThat(parseConstraint<PrlComparisonPredicate>("[b = \"text\"]")).isEqualTo(PrlComparisonPredicate(EQ, PrlFeature("b"), PrlEnumValue("text")))

        assertThat(parseConstraint<PrlComparisonPredicate>("[a != \"text\"]")).isEqualTo(PrlComparisonPredicate(NE, PrlFeature("a"), PrlEnumValue("text")))
        assertThat(parseConstraint<PrlComparisonPredicate>("[\"a\" != \"b\"]")).isEqualTo(PrlComparisonPredicate(NE, PrlEnumValue("a"), PrlEnumValue("b")))
        assertThat(parseConstraint<PrlComparisonPredicate>("[a > 18]")).isEqualTo(PrlComparisonPredicate(GT, PrlFeature("a"), PrlIntValue(18)))
        assertThat(parseConstraint<PrlComparisonPredicate>("[a >= b]")).isEqualTo(PrlComparisonPredicate(GE, PrlFeature("a"), PrlFeature("b")))
        assertThat(parseConstraint<PrlComparisonPredicate>("[a < -b]")).isEqualTo(
            PrlComparisonPredicate(LT, PrlFeature("a"), PrlIntMulFunction(PrlIntValue(-1), PrlFeature("b")))
        )
        assertThat(parseConstraint<PrlComparisonPredicate>("[-3 <= b]")).isEqualTo(PrlComparisonPredicate(LE, PrlIntValue(-3), PrlFeature("b")))
    }

    @Test
    fun testIntFunction() {
        assertThat(parseConstraint<PrlComparisonPredicate>("[a = 4*b]")).isEqualTo(
            PrlComparisonPredicate(
                EQ, PrlFeature("a"),
                PrlIntMulFunction(PrlIntValue(4), PrlFeature("b"))
            )
        )
        assertThat(parseConstraint<PrlComparisonPredicate>("[a = -4*b]")).isEqualTo(
            PrlComparisonPredicate(
                EQ, PrlFeature("a"),
                PrlIntMulFunction(PrlIntValue(-4), PrlFeature("b"))
            )
        )
        assertThat(parseConstraint<PrlComparisonPredicate>("[a = 4 + b]")).isEqualTo(
            PrlComparisonPredicate(
                EQ, PrlFeature("a"),
                PrlIntAddFunction(listOf(PrlIntValue(4), PrlFeature("b")))
            )
        )
        assertThat(parseConstraint<PrlComparisonPredicate>("[a = f1 + 4*f2 + -7*f3 + 8]")).isEqualTo(
            PrlComparisonPredicate(
                EQ, PrlFeature("a"),
                PrlIntAddFunction(
                    listOf(
                        PrlFeature("f1"),
                        PrlIntMulFunction(PrlIntValue(4), PrlFeature("f2")),
                        PrlIntMulFunction(PrlIntValue(-7), PrlFeature("f3")),
                        PrlIntValue(8)
                    )
                )
            )
        )
    }

    @Test
    fun testInPredicate() {
        assertThat(parseConstraint<PrlInIntRangePredicate>("[a in [1]]")).isEqualTo(PrlInIntRangePredicate(PrlFeature("a"), IntRange.list(1)))
        assertThat(parseConstraint<PrlInIntRangePredicate>("[a in [-10-20]]")).isEqualTo(PrlInIntRangePredicate(PrlFeature("a"), IntRange.interval(-10, 20)))
        assertThat(parseConstraint<PrlInIntRangePredicate>("[a in [-1, 2, 3]]")).isEqualTo(PrlInIntRangePredicate(PrlFeature("a"), IntRange.list(-1, 2, 3)))
        assertThat(parseConstraint<PrlInIntRangePredicate>("[2*a in [-1, 2, 3]]")).isEqualTo(
            PrlInIntRangePredicate(PrlIntMulFunction(PrlIntValue(2), PrlFeature("a")), IntRange.list(-1, 2, 3))
        )
        assertThat(parseConstraint<PrlInEnumsPredicate>("[a in [\"bla\"]]")).isEqualTo(PrlInEnumsPredicate(PrlFeature("a"), listOf("bla")))
        assertThat(parseConstraint<PrlInEnumsPredicate>("[a in [\"T\", \"X\", \"Y\"]]")).isEqualTo(PrlInEnumsPredicate(PrlFeature("a"), listOf("T", "X", "Y")))
    }

    @Test
    fun testCardinalityConstraint() {
        assertThat(parseConstraint<PrlAmo>("amo[]")).isEqualTo(PrlAmo(listOf()));
        assertThat(parseConstraint<PrlAmo>("amo[f1]")).isEqualTo(PrlAmo(listOf(PrlFeature("f1"))));
        assertThat(parseConstraint<PrlAmo>("amo[f1, f2, f3]")).isEqualTo(PrlAmo(listOf(PrlFeature("f1"), PrlFeature("f2"), PrlFeature("f3"))));
        assertThat(parseConstraint<PrlExo>("exo[]")).isEqualTo(PrlExo(listOf()));
        assertThat(parseConstraint<PrlExo>("exo[f1]")).isEqualTo(PrlExo(listOf(PrlFeature("f1"))));
        assertThat(parseConstraint<PrlExo>("exo[f1, f2, f3]")).isEqualTo(PrlExo(listOf(PrlFeature("f1"), PrlFeature("f2"), PrlFeature("f3"))));
    }

    @Test
    fun testNot() {
        assertThat(parseConstraint<PrlNot>("-a")).isEqualTo(PrlNot(PrlFeature("a")))
        assertThat(parseConstraint<PrlNot>("-(a => b)")).isEqualTo(PrlNot(PrlImplication(PrlFeature("a"), PrlFeature("b"))))
        assertThat(parseConstraint<PrlImplication>("-a => b")).isEqualTo(PrlImplication(PrlNot(PrlFeature("a")), PrlFeature("b")))
    }

    @Test
    fun testBinaryOperator() {
        assertThat(parseConstraint<PrlImplication>("a => b")).isEqualTo(PrlImplication(PrlFeature("a"), PrlFeature("b")))
        assertThat(parseConstraint<PrlImplication>("a => b/c")).isEqualTo(PrlImplication(PrlFeature("a"), PrlOr(PrlFeature("b"), PrlFeature("c"))))
        assertThat(parseConstraint<PrlEquivalence>("a <=> b")).isEqualTo(PrlEquivalence(PrlFeature("a"), PrlFeature("b")))
        assertThat(parseConstraint<PrlEquivalence>("a <=> b/c")).isEqualTo(PrlEquivalence(PrlFeature("a"), PrlOr(PrlFeature("b"), PrlFeature("c"))))
    }

    @Test
    fun testNaryOperators() {
        assertThat(parseConstraint<PrlAnd>("a & b")).isEqualTo(PrlAnd(PrlFeature("a"), PrlFeature("b")))
        assertThat(parseConstraint<PrlAnd>("a & b & c")).isEqualTo(PrlAnd(PrlFeature("a"), PrlFeature("b"), PrlFeature("c")))
        assertThat(parseConstraint<PrlOr>("a / b")).isEqualTo(PrlOr(PrlFeature("a"), PrlFeature("b")))
        assertThat(parseConstraint<PrlOr>("a / b / c")).isEqualTo(PrlOr(PrlFeature("a"), PrlFeature("b"), PrlFeature("c")))
        assertThat(parseConstraint<PrlOr>("a & b / c & d")).isEqualTo(
            PrlOr(PrlAnd(PrlFeature("a"), PrlFeature("b")), PrlAnd(PrlFeature("c"), PrlFeature("d")))
        )
        assertThat(parseConstraint<PrlOr>("a / b & c / d")).isEqualTo(
            PrlOr(PrlFeature("a"), PrlAnd(PrlFeature("b"), PrlFeature("c")), PrlFeature("d"))
        )
        assertThat(parseConstraint<PrlAnd>("(a / b) & (c / d)")).isEqualTo(
            PrlAnd(PrlOr(PrlFeature("a"), PrlFeature("b")), PrlOr(PrlFeature("c"), PrlFeature("d")))
        )
    }

    @Test
    fun testMixed() {
        assertThat(parseConstraint<PrlNot>("-(a <=> (b / [i < 8] / v[>3]) <=> [x > 3] <=> (b / [desc = \"a\"]))")).isNotNull
        assertThat(parseConstraint<PrlOr>("a / (b & [i < 8] => v[>3]) / [x > 3] / (b / [desc = \"a\"])")).isNotNull
    }
}
