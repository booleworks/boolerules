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
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SliceSelectorTest {

    @Test
    fun testBooleanSliceSelector() {
        val slice = Slice.of(BooleanProperty("b1", true), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(BooleanSliceSelection("b1", BooleanRange.list(true)))
    }

    @Test
    fun testIntSliceSelector() {
        val slice = Slice.of(IntProperty("i1", 17), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(IntSliceSelection("i1", IntRange.list(17)))
    }

    @Test
    fun testIntRangeSliceSelector() {
        val slice = Slice.of(IntProperty("i2", 17), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(IntSliceSelection("i2", IntRange.list(17)))
    }

    @Test
    fun testDateSliceSelector() {
        val date = LocalDate.of(2020, 1, 1)
        val slice = Slice.of(DateProperty("d1", date), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(DateSliceSelection("d1", DateRange.list(date)))
    }

    @Test
    fun testDateRangeSliceSelector() {
        val date = LocalDate.of(2020, 1, 1)
        val slice = Slice.of(DateProperty("d2", date), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(DateSliceSelection("d2", DateRange.list(date)))
    }

    @Test
    fun testEnumSliceSelector() {
        val slice = Slice.of(EnumProperty("e1", "text"), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(EnumSliceSelection("e1", EnumRange.list("text")))
    }

    @Test
    fun testEnumListSliceSelector() {
        val slice = Slice.of(EnumProperty("e2", "text"), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(EnumSliceSelection("e2", EnumRange.list("text")))
    }

    @Test
    fun testSortedEnumSliceSelector() {
        val slice = Slice.of(EnumProperty("e3", "text"), SliceType.ANY)
        val selector = slice.selector()
        assertThat(selector).containsExactly(EnumSliceSelection("e3", EnumRange.list("text")))
    }

    @Test
    fun testCombined() {
        val slice = Slice.of(
            mapOf(
                Pair(BooleanProperty("b1", true), SliceType.ANY),
                Pair(IntProperty("i1", 17), SliceType.ANY),
                Pair(IntProperty("i2", 17), SliceType.ANY),
                Pair(EnumProperty("e1", "text"), SliceType.ANY),
                Pair(EnumProperty("e3", "text"), SliceType.ANY)
            )
        )
        val selector = slice.selector()
        assertThat(selector).containsExactlyInAnyOrder(
            BooleanSliceSelection("b1", BooleanRange.list(true)),
            IntSliceSelection("i1", IntRange.list(17)),
            IntSliceSelection("i2", IntRange.list(17)),
            EnumSliceSelection("e1", EnumRange.list("text")),
            EnumSliceSelection("e3", EnumRange.list("text"))
        )
    }
}
