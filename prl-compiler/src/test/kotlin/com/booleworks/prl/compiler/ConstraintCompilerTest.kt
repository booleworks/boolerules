package com.booleworks.prl.compiler

import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.constraints.ComparisonOperator.EQ
import com.booleworks.prl.model.constraints.FALSE
import com.booleworks.prl.model.constraints.TRUE
import com.booleworks.prl.model.constraints.amo
import com.booleworks.prl.model.constraints.and
import com.booleworks.prl.model.constraints.enumEq
import com.booleworks.prl.model.constraints.enumIn
import com.booleworks.prl.model.constraints.enumNe
import com.booleworks.prl.model.constraints.equiv
import com.booleworks.prl.model.constraints.exo
import com.booleworks.prl.model.constraints.impl
import com.booleworks.prl.model.constraints.intEq
import com.booleworks.prl.model.constraints.intGt
import com.booleworks.prl.model.constraints.intIn
import com.booleworks.prl.model.constraints.intMul
import com.booleworks.prl.model.constraints.intSum
import com.booleworks.prl.model.constraints.intVal
import com.booleworks.prl.model.constraints.not
import com.booleworks.prl.model.constraints.or
import com.booleworks.prl.model.constraints.toIntValue
import com.booleworks.prl.model.constraints.versionEq
import com.booleworks.prl.model.constraints.versionGe
import com.booleworks.prl.parser.PrlAmo
import com.booleworks.prl.parser.PrlComparisonPredicate
import com.booleworks.prl.parser.PrlConstant
import com.booleworks.prl.parser.PrlExo
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlIntAddFunction
import com.booleworks.prl.parser.PrlIntMulFunction
import com.booleworks.prl.parser.PrlIntValue
import com.booleworks.prl.parser.parseConstraint
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ConstraintCompilerTest {
    val cc = ConstraintCompiler()

    @Test
    fun testCompileConstant() {
        assertThat(cc.compileConstraint(PrlConstant(true), theoryMap)).isEqualTo(TRUE)
        assertThat(cc.compileConstraint(PrlConstant(false), theoryMap)).isEqualTo(FALSE)
    }

    @Test
    fun testCompileBooleanFeature() {
        assertThat(cc.compileConstraint(PrlFeature("b1"), theoryMap)).isEqualTo(b1Definition.feature)
        assertThat(cc.compileConstraint(PrlFeature("v"), theoryMap)).isEqualTo(vDefinition.feature)
        assertThatThrownBy { cc.compileConstraint(PrlFeature("x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(PrlFeature("e1"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("ENUM feature 'e1' is used as BOOL feature")
        assertThatThrownBy { cc.compileConstraint(PrlFeature("i1"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("INT feature 'i1' is used as BOOL feature")
    }

    @Test
    fun testCompileAmo() {
        assertThat(cc.compileConstraint(PrlAmo(), theoryMap)).isEqualTo(amo())
        assertThat(cc.compileConstraint(PrlAmo(PrlFeature("b1")), theoryMap)).isEqualTo(amo(b1))
        assertThat(
            cc.compileConstraint(
                PrlAmo(PrlFeature("b1"), PrlFeature("b2"), PrlFeature("b3")),
                theoryMap
            )
        ).isEqualTo(amo(b1, b2, b3))
        assertThatThrownBy { cc.compileConstraint(PrlAmo(PrlFeature("b1"), PrlFeature("x")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(PrlAmo(PrlFeature("b1"), PrlFeature("e1")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("ENUM feature 'e1' is used as BOOL feature")
        assertThatThrownBy { cc.compileConstraint(PrlAmo(PrlFeature("b1"), PrlFeature("i1")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("INT feature 'i1' is used as BOOL feature")
        assertThatThrownBy { cc.compileConstraint(PrlAmo(PrlFeature("b1"), PrlFeature("v")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("VERSIONED_BOOL feature 'v' is used as BOOL feature")
    }

    @Test
    fun testCompileExo() {
        assertThat(cc.compileConstraint(PrlExo(), theoryMap)).isEqualTo(exo())
        assertThat(cc.compileConstraint(PrlExo(PrlFeature("b1")), theoryMap)).isEqualTo(exo(b1))
        assertThat(
            cc.compileConstraint(
                PrlExo(PrlFeature("b1"), PrlFeature("b2"), PrlFeature("b3")),
                theoryMap
            )
        ).isEqualTo(exo(b1, b2, b3))
        assertThatThrownBy { cc.compileConstraint(PrlExo(PrlFeature("b1"), PrlFeature("x")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(PrlExo(PrlFeature("b1"), PrlFeature("e1")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("ENUM feature 'e1' is used as BOOL feature")
        assertThatThrownBy { cc.compileConstraint(PrlExo(PrlFeature("b1"), PrlFeature("i1")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("INT feature 'i1' is used as BOOL feature")
        assertThatThrownBy { cc.compileConstraint(PrlExo(PrlFeature("b1"), PrlFeature("v")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("VERSIONED_BOOL feature 'v' is used as BOOL feature")
    }

    @Test
    fun testImplication() {
        assertThat(cc.compileConstraint(parseConstraint("b1 => b2/b3"), theoryMap)).isEqualTo(impl(b1, or(b2, b3)))
        assertThat(cc.compileConstraint(parseConstraint("-(b1&b2) => true"), theoryMap)).isEqualTo(
            impl(
                not(
                    and(
                        b1,
                        b2
                    )
                ), TRUE
            )
        )
        assertThatThrownBy { cc.compileConstraint(parseConstraint("b1 => x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("x => b2"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
    }

    @Test
    fun testEquivalence() {
        assertThat(cc.compileConstraint(parseConstraint("b1 <=> b2/b3"), theoryMap)).isEqualTo(equiv(b1, or(b2, b3)))
        assertThat(cc.compileConstraint(parseConstraint("-(b1&b2) <=> true"), theoryMap)).isEqualTo(
            equiv(
                not(
                    and(
                        b1,
                        b2
                    )
                ), TRUE
            )
        )
        assertThatThrownBy { cc.compileConstraint(parseConstraint("b1 <=> x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("x <=> b2"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
    }

    @Test
    fun testAnd() {
        assertThat(cc.compileConstraint(parseConstraint("b1 & b2"), theoryMap)).isEqualTo(and(b1, b2))
        assertThat(cc.compileConstraint(parseConstraint("b1 & (b2/b3) & -b2"), theoryMap)).isEqualTo(
            and(
                b1,
                or(b2, b3),
                not(b2)
            )
        )
        assertThatThrownBy { cc.compileConstraint(parseConstraint("true & x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
    }

    @Test
    fun testOr() {
        assertThat(cc.compileConstraint(parseConstraint("b1 / b2"), theoryMap)).isEqualTo(or(b1, b2))
        assertThat(cc.compileConstraint(parseConstraint("b1 / b2 & b3 / -b2"), theoryMap)).isEqualTo(
            or(
                b1,
                and(b2, b3),
                not(b2)
            )
        )
        assertThatThrownBy { cc.compileConstraint(parseConstraint("true / x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
    }

    @Test
    fun testNot() {
        assertThat(cc.compileConstraint(parseConstraint("-(b1 / b2)"), theoryMap)).isEqualTo(not(or(b1, b2)))
        assertThat(cc.compileConstraint(parseConstraint("-(b1 / b2 & b3 / -b2)"), theoryMap)).isEqualTo(
            not(
                or(
                    b1,
                    and(b2, b3),
                    not(b2)
                )
            )
        )
        assertThatThrownBy { cc.compileConstraint(parseConstraint("-x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
    }

    @Test
    fun testEnumInPredicate() {
        assertThat(cc.compileConstraint(parseConstraint("""[e1 in ["a"]]"""), theoryMap)).isEqualTo(
            enumIn(
                e1,
                listOf("a")
            )
        )
        assertThat(
            cc.compileConstraint(
                parseConstraint("""[e1 in ["a", "b", "c"]]"""),
                theoryMap
            )
        ).isEqualTo(enumIn(e1, listOf("a", "b", "c")))
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[x in ["a", "b"]]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[b1 in ["a", "b"]]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("BOOL feature 'b1' is used as ENUM feature")
    }

    @Test
    fun testIntValue() {
        assertThat(cc.compileIntTerm(PrlIntValue(3), theoryMap)).isEqualTo(intVal(3))
    }

    @Test
    fun testIntFeature() {
        assertThat(cc.compileIntTerm(PrlFeature("i1"), theoryMap)).isEqualTo(i1)
        assertThatThrownBy { cc.compileIntTerm(PrlFeature("x"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileIntTerm(PrlFeature("b1"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("BOOL feature 'b1' is used as INT feature")
    }

    @Test
    fun testIntMul() {
        assertThat(cc.compileIntTerm(PrlIntMulFunction(PrlFeature("i1"), PrlIntValue(3)), theoryMap)).isEqualTo(
            intMul(
                3,
                i1
            )
        )
        assertThat(
            cc.compileIntTerm(
                PrlIntMulFunction(PrlIntValue(-3), PrlFeature("i1")),
                theoryMap
            )
        ).isEqualTo(intMul(-3, i1))
        assertThatThrownBy { cc.compileIntTerm(PrlIntMulFunction(PrlIntValue(-3), PrlIntValue(3)), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Integer multiplication is only allowed between a fixed coefficient and an integer feature")
        assertThatThrownBy {
            cc.compileIntTerm(
                PrlIntMulFunction(
                    PrlIntMulFunction(PrlIntValue(3), PrlFeature("i")),
                    PrlIntValue(3)
                ), theoryMap
            )
        }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Integer multiplication is only allowed between a fixed coefficient and an integer feature")
        assertThatThrownBy { cc.compileIntTerm(PrlIntMulFunction(PrlIntValue(-3), PrlFeature("x")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileIntTerm(PrlIntMulFunction(PrlIntValue(-3), PrlFeature("b1")), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("BOOL feature 'b1' is used as INT feature")
    }

    @Test
    fun testIntSum() {
        val mul1 = PrlIntMulFunction(PrlFeature("i1"), PrlIntValue(3))
        val mul2 = PrlIntMulFunction(PrlFeature("i2"), PrlIntValue(2))
        val mul3 = PrlIntMulFunction(PrlFeature("i3"), PrlIntValue(-4))
        assertThat(cc.compileIntTerm(PrlIntAddFunction(PrlIntValue(13), mul1, mul2, mul3), theoryMap))
            .isEqualTo(intSum(13, intMul(3, i1), intMul(2, i2), intMul(-4, i3)))
        assertThat(
            cc.compileIntTerm(
                PrlIntAddFunction(PrlIntValue(13), mul1, PrlIntAddFunction(mul2, mul3)),
                theoryMap
            )
        )
            .isEqualTo(intSum(13, intMul(3, i1), intMul(2, i2), intMul(-4, i3)))
        assertThat(cc.compileIntTerm(PrlIntAddFunction(PrlIntValue(13), mul1, PrlIntValue(-12), mul3), theoryMap))
            .isEqualTo(intSum(1, intMul(3, i1), intMul(-4, i3)))
        assertThatThrownBy {
            cc.compileIntTerm(
                PrlIntAddFunction(
                    PrlIntValue(13),
                    mul1,
                    PrlIntMulFunction(PrlIntValue(3), PrlFeature("x"))
                ), theoryMap
            )
        }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy {
            cc.compileIntTerm(
                PrlIntAddFunction(
                    PrlIntValue(13),
                    mul1,
                    PrlIntMulFunction(PrlIntValue(3), PrlFeature("b1"))
                ), theoryMap
            )
        }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("BOOL feature 'b1' is used as INT feature")
    }

    @Test
    fun testIntInPredicate() {
        assertThat(cc.compileConstraint(parseConstraint("[i1 in [-1 - 7]]"), theoryMap)).isEqualTo(
            intIn(
                i1,
                IntRange.interval(-1, 7)
            )
        )
        assertThat(cc.compileConstraint(parseConstraint("[i2*3 in [-1 - 7]]"), theoryMap)).isEqualTo(
            intIn(
                intMul(
                    3,
                    i2
                ), IntRange.interval(-1, 7)
            )
        )
        assertThat(cc.compileConstraint(parseConstraint("[i3*3 + 4 in [-1, 1, 10]]"), theoryMap))
            .isEqualTo(intIn(intSum(4, intMul(3, i3)), IntRange.list(-1, 1, 10)))
        assertThatThrownBy { cc.compileConstraint(parseConstraint("[x in [1-5]]"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown feature: 'x'")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("[e1 in [1-5]]"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("ENUM feature 'e1' is used as INT feature")
    }

    @Test
    fun testComparisonErrors() {
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[e1 = i1]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Cannot determine theory of predicate, mixed features of theories ENUM and INT")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[i1 = "text"]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unknown integer term type: PrlEnumValue")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[e1 = 7]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Enum comparison must compare an enum feature with an enum value")
    }

    @Test
    fun testEnumComparisonPredicate() {
        assertThat(cc.compileConstraint(parseConstraint("""[e1 = "text"]"""), theoryMap)).isEqualTo(enumEq(e1, "text"))
        assertThat(cc.compileConstraint(parseConstraint("""["text" != e2]"""), theoryMap)).isEqualTo(
            enumNe(
                e2,
                "text"
            )
        )
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[e1 = e2]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Enum comparison must compare an enum feature with an enum value")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""["t" = "p"]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Enum comparison must compare an enum feature with an enum value")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("""[e1 > "p"]"""), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Only comparisons with = and != are allowed for enums")
    }

    @Test
    fun testIntComparisonPredicate() {
        assertThat(cc.compileConstraint(parseConstraint("[i1 = 7]"), theoryMap)).isEqualTo(intEq(i1, 7))
        assertThat(cc.compileConstraint(parseConstraint("[7 = i1]"), theoryMap)).isEqualTo(intEq(7.toIntValue(), i1))
        assertThat(cc.compileConstraint(parseConstraint("[7 = 7]"), theoryMap)).isEqualTo(
            intEq(
                7.toIntValue(),
                7.toIntValue()
            )
        )
        assertThat(cc.compileConstraint(parseConstraint("[i3*3 + 47 > 8*i1 + -2*i2]"), theoryMap))
            .isEqualTo(intGt(intSum(47, intMul(3, i3)), intSum(intMul(8, i1), intMul(-2, i2))))
        assertThat(cc.compileConstraint(parseConstraint("[i1 = i2]"), theoryMap)).isEqualTo(intEq(i1, i2))
    }

    @Test
    fun testVersionPredicate() {
        assertThat(cc.compileConstraint(parseConstraint("v[>=7]"), theoryMap)).isEqualTo(versionGe(v, 7))
        assertThat(
            cc.compileConstraint(
                PrlComparisonPredicate(EQ, PrlIntValue(4), PrlFeature("v")),
                theoryMap
            )
        ).isEqualTo(versionEq(v, 4))
        assertThat(
            cc.compileConstraint(
                PrlComparisonPredicate(EQ, PrlIntValue(4), PrlFeature("v")),
                theoryMap
            )
        ).isEqualTo(versionEq(v, 4))
        assertThatThrownBy {
            cc.compileConstraint(
                PrlComparisonPredicate(EQ, PrlIntValue(-4), PrlFeature("v")),
                theoryMap
            )
        }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Versions must be > 0")
        assertThatThrownBy {
            cc.compileConstraint(
                PrlComparisonPredicate(EQ, PrlFeature("v"), PrlFeature("v")),
                theoryMap
            )
        }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Version predicate must compare a versioned boolean feature with a fixed version")
        assertThatThrownBy { cc.compileConstraint(parseConstraint("[b1 >= 7]"), theoryMap) }
            .isInstanceOf(CoCoException::class.java)
            .hasMessage("Unversioned feature in version predicate: b1")
    }

    @Test
    fun testLargeExample() {
        val formula =
            """b1 => ([e1 in ["a", "b", "c"]] / -b2) & -(v[>=7] <=> [3*i3 + 47 > 8*i1 + -2*i2] / [-2*i2 + 4 in [-1, 1, 10]])"""
        assertThat(cc.compileConstraint(parseConstraint(formula), theoryMap).toString()).isEqualTo(
            formula
        )
    }
}
