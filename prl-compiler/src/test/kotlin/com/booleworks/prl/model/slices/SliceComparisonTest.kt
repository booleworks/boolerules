package com.booleworks.prl.model.slices

import com.booleworks.prl.model.EmptyIntRange
import com.booleworks.prl.model.EnumList
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntInterval
import com.booleworks.prl.model.IntList
import com.booleworks.prl.model.IntProperty
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SliceComparisonTest {
    @Test
    fun testComparisonOneElement() {
        val i1 = Slice.of(IntProperty("i1", EmptyIntRange))
        val i2 = Slice.of(IntProperty("i1", IntList(sortedSetOf(1))))
        val i3 = Slice.of(IntProperty("i1", IntList(sortedSetOf(3))))
        val i4 = Slice.of(IntProperty("i1", IntList(sortedSetOf(1, 2, 4))))
        val i5 = Slice.of(IntProperty("i1", IntList(sortedSetOf(1, 4, 6))))
        val i6 = Slice.of(IntProperty("i1", IntList(sortedSetOf(2, 3, 5))))
        val i7 = Slice.of(IntProperty("i1", IntInterval(1, 4)))
        val i8 = Slice.of(IntProperty("i1", IntInterval(2, 5)))
        val i9 = Slice.of(IntProperty("i1", IntInterval(1, 3)))

        val set = sortedSetOf(i9, i7, i1, i3, i5, i8, i2, i4, i6)

        Assertions.assertThat(set).containsExactly(i1, i9, i7, i8, i2, i4, i5, i6, i3)
    }

    @Test
    fun testComparisonThreeElement() {
        val s1 = Slice.of(
            IntProperty("i1", EmptyIntRange),
            EnumProperty("e1", EnumList(sortedSetOf("a", "b", "c"))),
            IntProperty("i2", IntInterval(1, 4))
        )
        val s2 = Slice.of(
            IntProperty("i1", IntList(sortedSetOf(1))),
            EnumProperty("e1", EnumList(sortedSetOf("a", "b", "c"))),
            IntProperty("i2", IntInterval(1, 4))
        )
        val s3 = Slice.of(
            IntProperty("i1", IntList(sortedSetOf(1))),
            EnumProperty("e1", EnumList(sortedSetOf("a", "b", "c"))),
            IntProperty("i2", IntInterval(5, 7))
        )
        val s4 = Slice.of(
            IntProperty("i1", IntList(sortedSetOf(1, 2, 4))),
            EnumProperty("e1", EnumList(sortedSetOf("a", "b", "c"))),
            IntProperty("i2", IntInterval(1, 4))
        )
        val s5 = Slice.of(
            IntProperty("i1", IntList(sortedSetOf(1, 2, 5))),
            EnumProperty("e1", EnumList(sortedSetOf("b", "c"))),
            IntProperty("i2", IntInterval(1, 4))
        )

        val set = sortedSetOf(s4, s2, s3, s1, s5)

        Assertions.assertThat(set).containsExactly(s1, s2, s3, s4, s5)
    }
}
