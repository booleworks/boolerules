package com.booleworks.prl.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PropertyComparisonTest {
    @Test
    fun testCompareBooleanProperty() {
        val b1 = BooleanProperty("b1", EmptyBooleanRange)
        val b2 = BooleanProperty("b1", BooleanList(sortedSetOf(false)))
        val b3 = BooleanProperty("b1", BooleanList(sortedSetOf(true)))
        val b4 = BooleanProperty("b1", BooleanList(sortedSetOf(false, true)))

        val set = sortedSetOf(b3, b1, b4, b2)

        assertThat(set).containsExactly(b1, b2, b4, b3)
    }

    @Test
    fun testCompareEnumProperty() {
        val e1 = EnumProperty("e1", EmptyEnumRange)
        val e2 = EnumProperty("e1", EnumList(sortedSetOf("A")))
        val e3 = EnumProperty("e1", EnumList(sortedSetOf("C")))
        val e4 = EnumProperty("e1", EnumList(sortedSetOf("A", "B", "C")))
        val e5 = EnumProperty("e1", EnumList(sortedSetOf("A", "B", "D")))
        val e6 = EnumProperty("e1", EnumList(sortedSetOf("B", "D")))

        val set = sortedSetOf(e6, e4, e2, e1, e5, e3)

        assertThat(set).containsExactly(e1, e2, e4, e5, e6, e3)
    }

    @Test
    fun testCompareIntProperty() {
        val i1 = IntProperty("i1", EmptyIntRange)
        val i2 = IntProperty("i1", IntList(sortedSetOf(1)))
        val i3 = IntProperty("i1", IntList(sortedSetOf(3)))
        val i4 = IntProperty("i1", IntList(sortedSetOf(1, 2, 4)))
        val i5 = IntProperty("i1", IntList(sortedSetOf(1, 4, 6)))
        val i6 = IntProperty("i1", IntList(sortedSetOf(2, 3, 5)))
        val i7 = IntProperty("i1", IntInterval(1, 4))
        val i8 = IntProperty("i1", IntInterval(2, 5))
        val i9 = IntProperty("i1", IntInterval(1, 3))

        val set = sortedSetOf(i9, i7, i1, i3, i5, i8, i2, i4, i6)

        assertThat(set).containsExactly(i1, i9, i7, i8, i2, i4, i5, i6, i3)
    }

    @Test
    fun testCompareDateProperty() {
        val d1 = LocalDate.of(2020, 1, 1)
        val d2 = LocalDate.of(2020, 1, 2)
        val d3 = LocalDate.of(2020, 1, 3)
        val d4 = LocalDate.of(2020, 1, 4)
        val d5 = LocalDate.of(2020, 1, 5)
        val d6 = LocalDate.of(2020, 1, 6)

        val i1 = DateProperty("d1", EmptyDateRange)
        val i2 = DateProperty("d1", DateList(sortedSetOf(d1)))
        val i3 = DateProperty("d1", DateList(sortedSetOf(d3)))
        val i4 = DateProperty("d1", DateList(sortedSetOf(d1, d2, d4)))
        val i5 = DateProperty("d1", DateList(sortedSetOf(d1, d4, d6)))
        val i6 = DateProperty("d1", DateList(sortedSetOf(d2, d3, d5)))
        val i7 = DateProperty("d1", DateInterval(d1, d4))
        val i8 = DateProperty("d1", DateInterval(d2, d5))
        val i9 = DateProperty("d1", DateInterval(d1, d3))

        val set = sortedSetOf(i9, i7, i1, i3, i5, i8, i2, i4, i6)

        assertThat(set).containsExactly(i1, i9, i7, i8, i2, i4, i5, i6, i3)
    }

    @Test
    fun testIllegalComparisons() {
        assertThatThrownBy { DateProperty("d1", EmptyDateRange).compareTo(DateProperty("d2", EmptyDateRange)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Cannot compare properties of different type")

        assertThatThrownBy { DateProperty("d1", EmptyDateRange).compareTo(EnumProperty("d1", EmptyEnumRange)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Cannot compare properties of different type")
    }
}
