package com.booleworks.prl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SlicingPropertyDefinitionTest {

    @Test
    fun testBoolenPropertyDefinition() {
        val p1 = SlicingBooleanPropertyDefinition("p1")
        val p2 = SlicingBooleanPropertyDefinition("p1")
        p2.addRange(BooleanRange.list(true))
        val p3 = SlicingBooleanPropertyDefinition("p1")
        p3.addRange(BooleanRange.list(true))
        p3.addRange(BooleanRange.list(false))

        assertThat(p1.computeRelevantValues()).isEmpty()
        assertThat(p2.computeRelevantValues()).containsExactlyInAnyOrder(true, false)
        assertThat(p3.computeRelevantValues()).containsExactlyInAnyOrder(true, false)

        val p4 = SlicingBooleanPropertyDefinition("p1")
        p4.addRange(BooleanRange.list(true))
        assertThat(p1 == p2).isFalse
        assertThat(p2 == p4).isTrue
    }

    @Test
    fun testIntPropertyDefinition() {
        val p1 = SlicingIntPropertyDefinition("p1")
        val p2 = SlicingIntPropertyDefinition("p2")
        p2.addRange(IntRange.list(1, 3, 5))
        val p3 = SlicingIntPropertyDefinition("p3")
        p3.addRange(IntRange.interval(1, 10))
        p3.addRange(IntRange.list(1, 14, 22))
        p3.addRange(IntRange.list(3))
        p3.addRange(IntRange.interval(3, 12))
        p3.addRange(IntRange.interval(1, 22))

        assertThat(p1.computeRelevantValues()).isEmpty()
        assertThat(p2.computeRelevantValues()).containsExactlyInAnyOrder(1, 3, 5, 2, 4)
        assertThat(p3.computeRelevantValues()).containsExactlyInAnyOrder(1, 2, 3, 4, 11, 13, 14, 15, 22)
    }

    @Test
    fun testDatePropertyDefinition() {
        val d1 = LocalDate.of(2023, 7, 1)
        val d2 = LocalDate.of(2023, 7, 2)
        val d3 = LocalDate.of(2023, 7, 3)
        val d4 = LocalDate.of(2023, 7, 4)
        val d5 = LocalDate.of(2023, 7, 5)
        val d10 = LocalDate.of(2023, 7, 10)
        val d11 = LocalDate.of(2023, 7, 11)
        val d12 = LocalDate.of(2023, 7, 12)
        val d13 = LocalDate.of(2023, 7, 13)
        val d14 = LocalDate.of(2023, 7, 14)
        val d15 = LocalDate.of(2023, 7, 15)
        val d22 = LocalDate.of(2023, 7, 22)

        val p1 = SlicingDatePropertyDefinition("p1")
        val p2 = SlicingDatePropertyDefinition("p2")
        p2.addRange(DateRange.list(d1, d3, d5))
        val p3 = SlicingDatePropertyDefinition("p3")
        p3.addRange(DateRange.interval(d1, d10))
        p3.addRange(DateRange.list(d1, d14, d22))
        p3.addRange(DateRange.list(d3))
        p3.addRange(DateRange.interval(d3, d12))
        p3.addRange(DateRange.interval(d1, d22))
        val p4 = SlicingDatePropertyDefinition("p4")
        p4.addRange(DateRange.interval(d1, d3))

        assertThat(p1.computeRelevantValues()).isEmpty()
        assertThat(p2.computeRelevantValues()).containsExactlyInAnyOrder(d1, d3, d5, d2, d4)
        assertThat(p3.computeRelevantValues()).containsExactlyInAnyOrder(d1, d2, d3, d4, d11, d13, d14, d15, d22)
        assertThat(p4.computeRelevantValues()).containsExactlyInAnyOrder(d1, d3)
    }

    @Test
    fun testEnumPropertyDefinition() {
        val p1 = SlicingEnumPropertyDefinition("p1")
        val p2 = SlicingEnumPropertyDefinition("p1")
        p2.addRange(EnumRange.list("A", "B"))
        val p3 = SlicingEnumPropertyDefinition("p1")
        p3.addRange(EnumRange.list("A", "B", "C"))
        p3.addRange(EnumRange.list(setOf("B", "C", "D")))

        assertThat(p1.computeRelevantValues()).isEmpty()
        assertThat(p2.computeRelevantValues()).containsExactlyInAnyOrder("A", "B")
        assertThat(p3.computeRelevantValues()).containsExactlyInAnyOrder("A", "B", "C", "D")
    }
}
