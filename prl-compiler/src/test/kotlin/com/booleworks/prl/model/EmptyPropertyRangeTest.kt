package com.booleworks.prl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EmptyPropertyRangeTest {

    @Test
    fun testBooleanProperty() {
        val p1 = BooleanRange.empty()
        val p2 = BooleanRange.list(true)
        assertThat(p1.isEmpty()).isTrue
        assertThat(p1.isDiscrete()).isTrue
        assertThat(p1.isContinuous()).isTrue
        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.contains(true)).isFalse
        assertThat(p1.contains(false)).isFalse

        assertThat(p1.intersects(BooleanRange.list(true))).isFalse
        assertThat(p1.intersection(BooleanRange.list(true))).isEqualTo(BooleanRange.empty())
        assertThat(p2.intersects(p1)).isFalse
        assertThat(p2.intersection(p1)).isEqualTo(BooleanRange.empty())
    }

    @Test
    fun testIntProperty() {
        val p1 = IntRange.interval(3, 1)
        val p2 = IntRange.empty()
        val p3 = IntRange.list(1, 3, 5)
        val p4 = IntRange.interval(1, 5)

        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.isEmpty()).isTrue
        assertThat(p1.isDiscrete()).isTrue
        assertThat(p1.isContinuous()).isTrue
        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.contains(1)).isFalse
        assertThat(p1.intersects(p3)).isFalse
        assertThat(p1.intersects(p4)).isFalse
        assertThat(p1.intersection(p3)).isEqualTo(EmptyIntRange)
        assertThat(p1.intersection(p4)).isEqualTo(EmptyIntRange)
        assertThat(p3.intersects(p1)).isFalse
        assertThat(p4.intersects(p1)).isFalse
        assertThat(p3.intersection(p1)).isEqualTo(EmptyIntRange)
        assertThat(p4.intersection(p1)).isEqualTo(EmptyIntRange)

        assertThat(p2.allValues()).isEmpty()
        assertThat(p2.isEmpty()).isTrue
        assertThat(p2.isDiscrete()).isTrue
        assertThat(p2.isContinuous()).isTrue
        assertThat(p2.allValues()).isEmpty()
        assertThat(p2.contains(1)).isFalse
        assertThat(p2.intersects(p3)).isFalse
        assertThat(p2.intersects(p4)).isFalse
        assertThat(p2.intersection(p3)).isEqualTo(EmptyIntRange)
        assertThat(p2.intersection(p4)).isEqualTo(EmptyIntRange)
        assertThat(p3.intersects(p2)).isFalse
        assertThat(p4.intersects(p2)).isFalse
        assertThat(p3.intersection(p2)).isEqualTo(EmptyIntRange)
        assertThat(p4.intersection(p2)).isEqualTo(EmptyIntRange)
    }

    @Test
    fun testDateProperty() {
        val d1 = LocalDate.of(2021, 1, 1)
        val d3 = LocalDate.of(2023, 1, 1)
        val d5 = LocalDate.of(2025, 1, 1)

        val p1 = DateRange.interval(d3, d1)
        val p2 = DateRange.empty()
        val p3 = DateRange.list(d1, d3, d5)
        val p4 = DateRange.interval(d1, d5)

        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.isEmpty()).isTrue
        assertThat(p1.isDiscrete()).isTrue
        assertThat(p1.isContinuous()).isTrue
        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.contains(d1)).isFalse
        assertThat(p1.intersects(p3)).isFalse
        assertThat(p1.intersects(p4)).isFalse
        assertThat(p1.intersection(p3)).isEqualTo(EmptyDateRange)
        assertThat(p1.intersection(p4)).isEqualTo(EmptyDateRange)
        assertThat(p3.intersects(p1)).isFalse
        assertThat(p4.intersects(p1)).isFalse
        assertThat(p3.intersection(p1)).isEqualTo(EmptyDateRange)
        assertThat(p4.intersection(p1)).isEqualTo(EmptyDateRange)

        assertThat(p2.allValues()).isEmpty()
        assertThat(p2.isEmpty()).isTrue
        assertThat(p2.isDiscrete()).isTrue
        assertThat(p2.isContinuous()).isTrue
        assertThat(p2.allValues()).isEmpty()
        assertThat(p2.contains(d1)).isFalse
        assertThat(p2.intersects(p3)).isFalse
        assertThat(p2.intersects(p4)).isFalse
        assertThat(p2.intersection(p3)).isEqualTo(EmptyDateRange)
        assertThat(p2.intersection(p4)).isEqualTo(EmptyDateRange)
        assertThat(p3.intersects(p2)).isFalse
        assertThat(p4.intersects(p2)).isFalse
        assertThat(p3.intersection(p2)).isEqualTo(EmptyDateRange)
        assertThat(p4.intersection(p2)).isEqualTo(EmptyDateRange)
    }

    @Test
    fun testEnumListProperty() {
        val p1 = EnumRange.empty()
        val p2 = EnumRange.list("A", "B")

        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.isEmpty()).isTrue
        assertThat(p1.isDiscrete()).isTrue
        assertThat(p1.isContinuous()).isTrue
        assertThat(p1.allValues()).isEmpty()
        assertThat(p1.contains("A")).isFalse
        assertThat(p1.intersects(p2)).isFalse
        assertThat(p1.intersects(p2)).isFalse
        assertThat(p1.intersection(p2)).isEqualTo(EmptyEnumRange)
        assertThat(p2.intersects(p1)).isFalse
        assertThat(p2.intersection(p1)).isEqualTo(EmptyEnumRange)
    }
}
