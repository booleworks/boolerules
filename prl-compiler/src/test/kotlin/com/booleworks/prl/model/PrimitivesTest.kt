package com.booleworks.prl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PrimitivesTest {

    @Test
    fun testIntRangeContains() {
        assertThat(IntRange.interval(1, 18).contains(1)).isTrue
        assertThat(IntRange.interval(1, 18).contains(18)).isTrue
        assertThat(IntRange.interval(1, 18).contains(13)).isTrue
        assertThat(IntRange.interval(-5, -1).contains(-4)).isTrue
        assertThat(IntRange.interval(1, 18).contains(0)).isFalse
        assertThat(IntRange.interval(1, 18).contains(-2)).isFalse
        assertThat(IntRange.interval(1, 18).contains(19)).isFalse

        assertThat(IntRange.list(1, 3, 5, 22, 18).contains(1)).isTrue
        assertThat(IntRange.list(1, 3, 5, 22, 18).contains(18)).isTrue
        assertThat(IntRange.list(1, 3, 5, 22, 18).contains(22)).isTrue
        assertThat(IntRange.list(1, 3, 5, 22, 18).contains(0)).isFalse
        assertThat(IntRange.list(1, 3, 5, 22, 18).contains(-2)).isFalse
        assertThat(IntRange.list(1, 3, 5, 22, 18).contains(19)).isFalse
    }

    @Test
    fun testIntRangeIntersect() {
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(-3, -1))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(-3, 5))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(2, 6))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(8, 14))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(4, 14))).isTrue

        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(-4, -2))).isFalse
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.interval(9, 22))).isFalse

        assertThat(IntRange.interval(-1, 8).intersects(IntRange.list(-3, -1, 4))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.list(-3, 5, 10))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.list(8, 10))).isTrue
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.list(-3, -2))).isFalse
        assertThat(IntRange.interval(-1, 8).intersects(IntRange.list(10, 12, 155))).isFalse

        assertThat(IntRange.list(-3, -1, 4).intersects(IntRange.interval(-1, 8))).isTrue
        assertThat(IntRange.list(-3, 5, 10).intersects(IntRange.interval(-1, 8))).isTrue
        assertThat(IntRange.list(8, 10).intersects(IntRange.interval(-1, 8))).isTrue
        assertThat(IntRange.list(-3, -2).intersects(IntRange.interval(-1, 8))).isFalse
        assertThat(IntRange.list(10, 12, 155).intersects(IntRange.interval(-1, 8))).isFalse

        assertThat(IntRange.list(-3, -1, 4).intersects(IntRange.list(4, 5, -3))).isTrue
        assertThat(IntRange.list(-3, 5, 10).intersects(IntRange.list(-1, 8, 10))).isTrue
        assertThat(IntRange.list(8, 10).intersects(IntRange.list(-1, 5, 10))).isTrue
        assertThat(IntRange.list(-3, -2).intersects(IntRange.list(-1, 8))).isFalse
        assertThat(IntRange.list(10, 12, 155).intersects(IntRange.list(-1, 156, 13))).isFalse
    }

    @Test
    fun testDateRangeContains() {
        assertThat(DateRange.interval(LocalDate.of(2022, 10, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2022, 10, 1))).isTrue
        assertThat(DateRange.interval(LocalDate.of(2022, 10, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2023, 10, 1))).isTrue
        assertThat(DateRange.interval(LocalDate.of(2022, 10, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2024, 3, 31))).isTrue
        assertThat(DateRange.interval(LocalDate.of(2022, 10, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2022, 9, 28))).isFalse
        assertThat(DateRange.interval(LocalDate.of(2022, 10, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2024, 4, 1))).isFalse
        assertThat(DateRange.interval(LocalDate.of(2022, 10, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2026, 4, 1))).isFalse

        assertThat(DateRange.list(LocalDate.of(2022, 10, 1), LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2022, 10, 1))).isTrue
        assertThat(DateRange.list(LocalDate.of(2022, 10, 1), LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2023, 1, 1))).isTrue
        assertThat(DateRange.list(LocalDate.of(2022, 10, 1), LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2024, 3, 31))).isTrue
        assertThat(DateRange.list(LocalDate.of(2022, 10, 1), LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2025, 3, 31))).isFalse
        assertThat(DateRange.list(LocalDate.of(2022, 10, 1), LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 31)).contains(LocalDate.of(2020, 3, 31))).isFalse
    }

    @Test
    fun testDateRangeIntersect() {
        val bd = LocalDate.of(2019, 12, 31)
        val d1 = LocalDate.of(2020, 1, 1)
        val id = LocalDate.of(2021, 12, 31)
        val d2 = LocalDate.of(2022, 12, 31)
        val ad = LocalDate.of(2023, 1, 1)
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(bd, d1))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(d1, id))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(id, d2))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(d2, ad))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(bd, ad))).isTrue

        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(bd, bd))).isFalse
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.interval(ad, ad))).isFalse

        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(d1, id, d2))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(bd, ad, id))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(id, id, ad))).isTrue
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(bd, ad))).isFalse
        assertThat(DateRange.interval(d1, d2).intersects(DateRange.list(ad))).isFalse

        assertThat(DateRange.list(d1, id, d2).intersects(DateRange.interval(d1, d2))).isTrue
        assertThat(DateRange.list(bd, ad, id).intersects(DateRange.interval(d1, d2))).isTrue
        assertThat(DateRange.list(bd, ad, d1).intersects(DateRange.interval(d1, d2))).isTrue
        assertThat(DateRange.list(bd).intersects(DateRange.interval(d1, d2))).isFalse
        assertThat(DateRange.list(bd, ad).intersects(DateRange.interval(d1, d2))).isFalse

        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(ad, d1))).isTrue
        assertThat(DateRange.list(d1, d2, id).intersects(DateRange.list(bd, bd, id))).isTrue
        assertThat(DateRange.list(bd).intersects(DateRange.list(id, bd))).isTrue
        assertThat(DateRange.list(d1, d2).intersects(DateRange.list(ad, bd))).isFalse
        assertThat(DateRange.list(id, id).intersects(DateRange.list(d1, d2, bd))).isFalse
    }
}
