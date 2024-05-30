package com.booleworks.transpiler.slices

import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.slices.BooleanSliceSelection
import com.booleworks.prl.model.slices.DateSliceSelection
import com.booleworks.prl.model.slices.EnumSliceSelection
import com.booleworks.prl.model.slices.IntSliceSelection
import com.booleworks.prl.model.slices.evaluateProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SliceSelectionTest {
    private val selections = listOf(
        BooleanSliceSelection("b1", BooleanRange.list(true)),
        EnumSliceSelection("e1", EnumRange.list("t1", "t2")),
        IntSliceSelection("i1", IntRange.interval(10, 100))
    )

    @Test
    fun testWrongPropertyName() {
        val prop = EnumProperty("prop", "t")
        assertThatThrownBy { EnumSliceSelection("p", EnumRange.list("")).evaluate(prop) }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Cannot evaluate a filter for property 'p' on property 'prop'")
    }

    @Test
    fun testBooleanSliceSelection() {
        val prop = BooleanProperty("prop", true)
        assertThat(BooleanSliceSelection("prop", BooleanRange.empty()).evaluate(prop)).isFalse
        assertThat(BooleanSliceSelection("prop", BooleanRange.list(true)).evaluate(prop)).isTrue
        assertThat(BooleanSliceSelection("prop", BooleanRange.list(false)).evaluate(prop)).isFalse
        assertThat(BooleanSliceSelection("prop", BooleanRange.list(false, true)).evaluate(prop)).isTrue
    }

    @Test
    fun testEnumSliceSelection() {
        val prop = EnumProperty("prop", "t1")
        assertThat(EnumSliceSelection("prop", EnumRange.empty()).evaluate(prop)).isFalse
        assertThat(EnumSliceSelection("prop", EnumRange.list("t1", "t2")).evaluate(prop)).isTrue
        assertThat(EnumSliceSelection("prop", EnumRange.list("t2", "t3")).evaluate(prop)).isFalse
        assertThat(EnumSliceSelection("prop", EnumRange.list("t2", "t3", "t4", "t1")).evaluate(prop)).isTrue
    }

    @Test
    fun testEnumListSliceSelection() {
        val prop = EnumProperty("prop", EnumRange.list("t1", "t2"))
        assertThat(EnumSliceSelection("prop", EnumRange.list("t1")).evaluate(prop)).isTrue
        assertThat(EnumSliceSelection("prop", EnumRange.list("t3", "t2")).evaluate(prop)).isTrue
        assertThat(EnumSliceSelection("prop", EnumRange.list("t3", "t4")).evaluate(prop)).isFalse
        assertThat(EnumSliceSelection("prop", EnumRange.list("t3", "t4", "t2")).evaluate(prop)).isTrue
    }

    @Test
    fun testIntSliceSelection() {
        val prop = IntProperty("prop", 17)

        assertThat(IntSliceSelection("prop", IntRange.interval(7, 1)).evaluate(prop)).isFalse
        assertThat(IntSliceSelection("prop", IntRange.interval(1, 20)).evaluate(prop)).isTrue
        assertThat(IntSliceSelection("prop", IntRange.interval(10, 100)).evaluate(prop)).isTrue
        assertThat(IntSliceSelection("prop", IntRange.interval(0, 16)).evaluate(prop)).isFalse
        assertThat(IntSliceSelection("prop", IntRange.empty()).evaluate(prop)).isFalse
        assertThat(IntSliceSelection("prop", IntRange.list(0, 1, 2, 17)).evaluate(prop)).isTrue
    }

    @Test
    fun testIntRangeSliceSelection() {
        val prop = IntProperty("prop", IntRange.interval(1, 20))

        assertThat(IntSliceSelection("prop", IntRange.interval(5, 7)).evaluate(prop)).isTrue
        assertThat(IntSliceSelection("prop", IntRange.interval(0, 10)).evaluate(prop)).isTrue
        assertThat(IntSliceSelection("prop", IntRange.interval(22, 100)).evaluate(prop)).isFalse
        assertThat(IntSliceSelection("prop", IntRange.list(1, 2, 17)).evaluate(prop)).isTrue
    }

    @Test
    fun testDateSliceSelection() {
        val d1 = LocalDate.of(2023, 1, 1)
        val id = LocalDate.of(2023, 10, 10)
        val d2 = LocalDate.of(2024, 12, 31)
        val prop = DateProperty("prop", id)

        assertThat(DateSliceSelection("prop", DateRange.interval(d2, d1)).evaluate(prop)).isFalse
        assertThat(DateSliceSelection("prop", DateRange.interval(d1, d2)).evaluate(prop)).isTrue
        assertThat(DateSliceSelection("prop", DateRange.interval(id, LocalDate.MAX)).evaluate(prop)).isTrue
        assertThat(DateSliceSelection("prop", DateRange.interval(LocalDate.MIN, d1)).evaluate(prop)).isFalse
        assertThat(DateSliceSelection("prop", DateRange.empty()).evaluate(prop)).isFalse
        assertThat(DateSliceSelection("prop", DateRange.list(id, d2)).evaluate(prop)).isTrue
    }

    @Test
    fun testDateRangeSliceSelection() {
        val bd = LocalDate.of(2022, 12, 24)
        val d1 = LocalDate.of(2023, 1, 1)
        val id = LocalDate.of(2023, 10, 10)
        val d2 = LocalDate.of(2024, 12, 31)
        val prop = DateProperty("prop", DateRange.interval(d1, d2))

        assertThat(DateSliceSelection("prop", DateRange.interval(bd, d2)).evaluate(prop)).isTrue
        assertThat(DateSliceSelection("prop", DateRange.interval(id, LocalDate.MAX)).evaluate(prop)).isTrue
        assertThat(DateSliceSelection("prop", DateRange.interval(LocalDate.MIN, bd)).evaluate(prop)).isFalse
        assertThat(DateSliceSelection("prop", DateRange.list(id, d2)).evaluate(prop)).isTrue
    }

    @Test
    fun testEvaluateSliceFilter() {
        val p1 = mapOf(Pair("x1", EnumProperty("x1", "t1")))
        val p2 = mapOf(Pair("x1", EnumProperty("x1", "t1")), Pair("b1", BooleanProperty("b1", true)))
        val p3 = mapOf(Pair("x1", EnumProperty("x1", "t1")), Pair("b1", BooleanProperty("b1", true)), Pair("i1", IntProperty("i1", 17)))
        val p4 = mapOf(
            Pair("x1", EnumProperty("x1", "t1")),
            Pair("b1", BooleanProperty("b1", true)),
            Pair("i1", IntProperty("i1", 17)),
            Pair("e1", EnumProperty("e1", "t1"))
        )

        val x1 = mapOf(
            Pair("x1", EnumProperty("x1", "t1")),
            Pair("b1", BooleanProperty("b1", false)),
            Pair("i1", IntProperty("i1", 17)),
            Pair("e1", EnumProperty("e1", "t1"))
        )
        val x2 = mapOf(
            Pair("x1", EnumProperty("x1", "t1")),
            Pair("b1", BooleanProperty("b1", true)),
            Pair("i1", IntProperty("i1", 7)),
            Pair("e1", EnumProperty("e1", "t1"))
        )
        val x3 = mapOf(
            Pair("x1", EnumProperty("x1", "t1")),
            Pair("b1", BooleanProperty("b1", true)),
            Pair("i1", IntProperty("i1", 17)),
            Pair("e1", EnumProperty("e1", "t112"))
        )

        assertThat(evaluateProperties(p1, selections)).isTrue
        assertThat(evaluateProperties(p2, selections)).isTrue
        assertThat(evaluateProperties(p3, selections)).isTrue
        assertThat(evaluateProperties(p4, selections)).isTrue

        assertThat(evaluateProperties(x1, selections)).isFalse
        assertThat(evaluateProperties(x2, selections)).isFalse
        assertThat(evaluateProperties(x3, selections)).isFalse
    }
}
