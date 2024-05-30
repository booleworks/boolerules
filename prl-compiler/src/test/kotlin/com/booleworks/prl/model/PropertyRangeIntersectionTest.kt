package com.booleworks.prl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PropertyRangeIntersectionTest {
    @Test
    fun testBooleanIntersection() {
        assertThat(BooleanRange.list(true).intersects(BooleanRange.list(true))).isTrue
        assertThat(BooleanRange.list(true).intersects(BooleanRange.list(false))).isFalse
        assertThat(BooleanRange.list(false).intersects(BooleanRange.list(true))).isFalse
        assertThat(BooleanRange.list(false).intersects(BooleanRange.list(false))).isTrue

        assertThat(BooleanRange.list(true).intersection(BooleanRange.list(true))).isEqualTo(BooleanRange.list(true))
        assertThat(BooleanRange.list(true).intersection(BooleanRange.list(false))).isEqualTo(BooleanRange.empty())
        assertThat(BooleanRange.list(false).intersection(BooleanRange.list(true))).isEqualTo(BooleanRange.empty())
        assertThat(BooleanRange.list(false).intersection(BooleanRange.list(false))).isEqualTo(BooleanRange.list(false))
    }

    @Test
    fun testEnumIntersection() {
        assertThat(EnumRange.list("a", "b", "c").intersects(EnumRange.list("a"))).isTrue
        assertThat(EnumRange.list("a", "x").intersects(EnumRange.list("x"))).isTrue
        assertThat(EnumRange.list("a", "b", "c").intersects(EnumRange.list("d"))).isFalse
        assertThat(EnumRange.list("a", "x").intersects(EnumRange.list("y"))).isFalse
        assertThat(EnumRange.list("a", "b", "c").intersects(EnumRange.list("a"))).isTrue
        assertThat(EnumRange.list("a", "b", "x").intersects(EnumRange.list("y", "w", "x"))).isTrue
        assertThat(EnumRange.list("a", "b", "c").intersects(EnumRange.list("d"))).isFalse

        assertThat(EnumRange.list("a", "b", "c").intersection(EnumRange.list("a"))).isEqualTo(EnumRange.list("a"))
        assertThat(EnumRange.list("a", "x").intersection(EnumRange.list("x"))).isEqualTo(EnumRange.list("x"))
        assertThat(EnumRange.list("a", "b", "c").intersection(EnumRange.list("d"))).isEqualTo(EmptyEnumRange)
        assertThat(EnumRange.list("a", "x").intersection(EnumRange.list("y"))).isEqualTo(EmptyEnumRange)
        assertThat(EnumRange.list("a", "b", "c").intersection(EnumRange.list("a", "b"))).isEqualTo(EnumRange.list("a", "b"))
        assertThat(EnumRange.list("a", "b", "x").intersection(EnumRange.list("y", "w", "x"))).isEqualTo(EnumRange.list("x"))
        assertThat(EnumRange.list("a", "b", "c").intersection(EnumRange.list("a", "b", "c"))).isEqualTo(EnumRange.list("a", "b", "c"))
    }

    @Test
    fun testIntIntersection() {
        assertThat(IntRange.interval(-2, 7).intersects(IntRange.list(-2))).isTrue
        assertThat(IntRange.interval(-2, 7).intersects(IntRange.list(0))).isTrue
        assertThat(IntRange.interval(-2, 7).intersects(IntRange.list(6))).isTrue
        assertThat(IntRange.interval(-2, 7).intersects(IntRange.list(7))).isTrue
        assertThat(IntRange.interval(-2, 7).intersects(IntRange.list(-3))).isFalse
        assertThat(IntRange.interval(-2, 7).intersects(IntRange.list(8))).isFalse

        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(-2))).isEqualTo(IntRange.list(-2))
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(0))).isEqualTo(IntRange.list(0))
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(6))).isEqualTo(IntRange.list(6))
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(7))).isEqualTo(IntRange.list(7))
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(-3))).isEqualTo(EmptyIntRange)
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(8))).isEqualTo(EmptyIntRange)

        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(-2, 4, 12))).isEqualTo(IntRange.list(-2, 4))
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(-5, 0, 8))).isEqualTo(IntRange.list(0))
        assertThat(IntRange.interval(-2, 7).intersection(IntRange.list(-1, 2, 3, 5, 7, 9, 12, 13))).isEqualTo(IntRange.list(-1, 2, 3, 5, 7))

        assertThat(IntRange.list(1, 3, 5, 7).intersects(IntRange.list(1))).isTrue
        assertThat(IntRange.list(1, 3, 5, 7).intersects(IntRange.list(5))).isTrue
        assertThat(IntRange.list(1, 3, 5, 7).intersects(IntRange.list(7))).isTrue
        assertThat(IntRange.list(1, 3, 5, 7).intersects(IntRange.list(0))).isFalse
        assertThat(IntRange.list(1, 3, 5, 7).intersects(IntRange.list(4))).isFalse

        assertThat(IntRange.list(1, 3, 5, 7).intersection(IntRange.list(-2, 4, 12))).isEqualTo(IntRange.empty())
        assertThat(IntRange.list(1, 3, 5, 7).intersection(IntRange.list(5, 0, 8))).isEqualTo(IntRange.list(5))
        assertThat(IntRange.list(1, 3, 5, 7).intersection(IntRange.list(-1, 2, 3, 5, 7, 9, 12, 13))).isEqualTo(IntRange.list(3, 5, 7))

        val r1 = IntRange.interval(-1, 7)
        val r2 = IntRange.list(1, 3, 5, 7)
        assertThat(IntRange.interval(1, 4).intersects(r1)).isTrue
        assertThat(IntRange.interval(0, 9).intersects(r1)).isTrue
        assertThat(IntRange.interval(-5, -2).intersects(r1)).isFalse
        assertThat(IntRange.interval(10, 18).intersects(r1)).isFalse
        assertThat(IntRange.interval(1, 4).intersects(r2)).isTrue
        assertThat(IntRange.interval(0, 9).intersects(r2)).isTrue
        assertThat(IntRange.interval(-5, -2).intersects(r2)).isFalse
        assertThat(IntRange.interval(10, 18).intersects(r2)).isFalse
        assertThat(IntRange.interval(2, 2).intersects(r2)).isFalse

        assertThat(IntRange.interval(1, 4).intersection(IntRange.interval(-3, 8))).isEqualTo(IntRange.interval(1, 4))
        assertThat(IntRange.interval(1, 4).intersection(IntRange.interval(2, 3))).isEqualTo(IntRange.interval(2, 3))
        assertThat(IntRange.interval(1, 4).intersection(IntRange.interval(0, 3))).isEqualTo(IntRange.interval(1, 3))
        assertThat(IntRange.interval(1, 4).intersection(IntRange.interval(2, 8))).isEqualTo(IntRange.interval(2, 4))
        assertThat(IntRange.interval(1, 4).intersection(IntRange.interval(7, 10)).isEmpty()).isTrue
    }

    @Test
    fun testDateIntersection() {
        val bd = LocalDate.of(2022, 12, 24)
        val d1 = LocalDate.of(2023, 1, 1)
        val id = LocalDate.of(2023, 10, 10)
        val d2 = LocalDate.of(2024, 12, 31)
        val ad = LocalDate.of(2025, 1, 3)

        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(d1))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(id))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(d2))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(bd))).isFalse
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(ad))).isFalse

        assertThat(DateRange.interval(d1, d2).intersection(DateRange.list(d1))).isEqualTo(DateRange.list(d1))
        assertThat(DateRange.interval(d1, d2).intersection(DateRange.list(id))).isEqualTo(DateRange.list(id))
        assertThat(DateRange.interval(d1, d2).intersection(DateRange.list(d2))).isEqualTo(DateRange.list(d2))
        assertThat(DateRange.interval(d1, d2).intersection(DateRange.list(bd))).isEqualTo(DateRange.empty())
        assertThat(DateRange.interval(d1, d2).intersection(DateRange.list(ad))).isEqualTo(DateRange.empty())

        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(d1))).isTrue
        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(id))).isFalse
        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(d2))).isTrue
        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(bd))).isFalse
        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(ad))).isFalse

        assertThat(DateRange.list(d1, d2).intersection(DateRange.list(d1))).isEqualTo(DateRange.list(d1))
        assertThat(DateRange.list(d1, d2).intersection(DateRange.list(id))).isEqualTo(DateRange.empty())
        assertThat(DateRange.list(d1, d2).intersection(DateRange.list(d2))).isEqualTo(DateRange.list(d2))
        assertThat(DateRange.list(d1, d2).intersection(DateRange.list(bd, d1, d1, ad))).isEqualTo(DateRange.list(d1))
        assertThat(DateRange.list(d1, d2).intersection(DateRange.list(ad, d1, d2, ad))).isEqualTo(DateRange.list(d1, d2))

        val r1 = DateRange.interval(d1, d2)
        val r2 = DateRange.list(d1, d2)

        assertThat(DateRange.interval(d1, d2).intersects(r1)).isTrue
        assertThat(DateRange.interval(bd, ad).intersects(r1)).isTrue
        assertThat(DateRange.interval(bd, d1).intersects(r1)).isTrue
        assertThat(DateRange.interval(d1, ad).intersects(r1)).isTrue
        assertThat(DateRange.interval(id, id).intersects(r1)).isTrue
        assertThat(DateRange.interval(bd, bd).intersects(r1)).isFalse
        assertThat(DateRange.interval(ad, ad).intersects(r1)).isFalse

        assertThat(DateRange.interval(d1, d2).intersection(r1)).isEqualTo(DateRange.interval(d1, d2))
        assertThat(DateRange.interval(bd, ad).intersection(r1)).isEqualTo(DateRange.interval(d1, d2))
        assertThat(DateRange.interval(bd, d1).intersection(r1)).isEqualTo(DateRange.interval(d1, d1))
        assertThat(DateRange.interval(d1, ad).intersection(r1)).isEqualTo(DateRange.interval(d1, d2))
        assertThat(DateRange.interval(id, id).intersection(r1)).isEqualTo(DateRange.interval(id, id))
        assertThat(DateRange.interval(bd, bd).intersection(r1).isEmpty()).isTrue
        assertThat(DateRange.interval(ad, ad).intersection(r1).isEmpty()).isTrue

        assertThat(DateRange.interval(d1, d2).intersects(r2)).isTrue
        assertThat(DateRange.interval(bd, ad).intersects(r2)).isTrue
        assertThat(DateRange.interval(bd, d1).intersects(r2)).isTrue
        assertThat(DateRange.interval(d1, ad).intersects(r2)).isTrue
        assertThat(DateRange.interval(id, id).intersects(r2)).isFalse
        assertThat(DateRange.interval(bd, bd).intersects(r2)).isFalse
        assertThat(DateRange.interval(ad, ad).intersects(r2)).isFalse

        assertThat(DateRange.list(d1, d2).intersects(r1)).isTrue
        assertThat(DateRange.list(d2, ad).intersects(r1)).isTrue
        assertThat(DateRange.list(id, bd).intersects(r1)).isTrue
        assertThat(DateRange.list(bd).intersects(r1)).isFalse
        assertThat(DateRange.list(ad).intersects(r1)).isFalse

        assertThat(DateRange.list(d1, d2).intersection(r1)).isEqualTo(DateRange.list(d1, d2))
        assertThat(DateRange.list(d2, ad).intersection(r1)).isEqualTo(DateRange.list(d2))
        assertThat(DateRange.list(id, bd).intersection(r1)).isEqualTo(DateRange.list(id))
        assertThat(DateRange.list(bd).intersection(r1)).isEqualTo(DateRange.empty())
        assertThat(DateRange.list(ad).intersection(r1)).isEqualTo(DateRange.empty())

        assertThat(DateRange.list(d1, d2).intersects(r2)).isTrue
        assertThat(DateRange.list(d2, ad).intersects(r2)).isTrue
        assertThat(DateRange.list(id, bd).intersects(r2)).isFalse
        assertThat(DateRange.list(bd).intersects(r2)).isFalse
        assertThat(DateRange.list(ad).intersects(r2)).isFalse
    }
}
