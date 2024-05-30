package com.booleworks.prl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PropertyRangeComparisonTest {
    @Test
    fun testCompareBooleanRange() {
        val b1 = EmptyBooleanRange
        val b2 = BooleanList(sortedSetOf(false))
        val b3 = BooleanList(sortedSetOf(true))
        val b4 = BooleanList(sortedSetOf(false, true))

        val set = sortedSetOf(b3, b1, b4, b2)

        assertThat(set).containsExactly(b1, b2, b4, b3)
    }

    @Test
    fun testCompareEnumRange() {
        val e1 = EmptyEnumRange
        val e2 = EnumList(sortedSetOf("A"))
        val e3 = EnumList(sortedSetOf("C"))
        val e4 = EnumList(sortedSetOf("A", "B", "C"))
        val e5 = EnumList(sortedSetOf("A", "B", "D"))
        val e6 = EnumList(sortedSetOf("B", "D"))

        val set = sortedSetOf(e6, e4, e2, e1, e5, e3)

        assertThat(set).containsExactly(e1, e2, e4, e5, e6, e3)
    }

    @Test
    fun testCompareIntRange() {
        val i1 = EmptyIntRange
        val i2 = IntList(sortedSetOf(1))
        val i3 = IntList(sortedSetOf(3))
        val i4 = IntList(sortedSetOf(1, 2, 4))
        val i5 = IntList(sortedSetOf(1, 4, 6))
        val i6 = IntList(sortedSetOf(2, 3, 5))
        val i7 = IntInterval(1, 4)
        val i8 = IntInterval(2, 5)
        val i9 = IntInterval(1, 3)

        val set = sortedSetOf(i9, i7, i1, i3, i5, i8, i2, i4, i6)

        assertThat(set).containsExactly(i1, i9, i7, i8, i2, i4, i5, i6, i3)
    }

    @Test
    fun testCompareDateRange() {
        val d1 = LocalDate.of(2020, 1, 1)
        val d2 = LocalDate.of(2020, 1, 2)
        val d3 = LocalDate.of(2020, 1, 3)
        val d4 = LocalDate.of(2020, 1, 4)
        val d5 = LocalDate.of(2020, 1, 5)
        val d6 = LocalDate.of(2020, 1, 6)

        val i1 = EmptyDateRange
        val i2 = DateList(sortedSetOf(d1))
        val i3 = DateList(sortedSetOf(d3))
        val i4 = DateList(sortedSetOf(d1, d2, d4))
        val i5 = DateList(sortedSetOf(d1, d4, d6))
        val i6 = DateList(sortedSetOf(d2, d3, d5))
        val i7 = DateInterval(d1, d4)
        val i8 = DateInterval(d2, d5)
        val i9 = DateInterval(d1, d3)

        val set = sortedSetOf(i9, i7, i1, i3, i5, i8, i2, i4, i6)

        assertThat(set).containsExactly(i1, i9, i7, i8, i2, i4, i5, i6, i3)
    }
}
