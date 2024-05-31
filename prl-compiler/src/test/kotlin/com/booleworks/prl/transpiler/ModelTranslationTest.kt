package com.booleworks.prl.transpiler

import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceSet
import com.booleworks.prl.model.slices.SliceType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ModelTranslationTest {
    private val s1 = Slice.of(
        mapOf(
            Pair(BooleanProperty("b1", true), SliceType.SPLIT),
            Pair(BooleanProperty("b2", true), SliceType.ALL),
            Pair(EnumProperty("e1", "text1"), SliceType.ANY),
            Pair(EnumProperty("e2", "E22"), SliceType.SPLIT),
            Pair(IntProperty("i1", 17), SliceType.SPLIT),
            Pair(IntProperty("i2", 100), SliceType.ANY),
            Pair(DateProperty("d1", LocalDate.of(2020, 1, 1)), SliceType.ALL)
        )
    )
    private val s2 = Slice.of(
        mapOf(
            Pair(BooleanProperty("b1", false), SliceType.SPLIT),
            Pair(BooleanProperty("b2", false), SliceType.ALL),
            Pair(EnumProperty("e1", "text2"), SliceType.ANY),
            Pair(EnumProperty("e2", "E23"), SliceType.SPLIT),
            Pair(IntProperty("i1", 19), SliceType.SPLIT),
            Pair(IntProperty("i2", 105), SliceType.ANY),
            Pair(DateProperty("d1", LocalDate.of(2020, 2, 1)), SliceType.ALL)
        )
    )
    private val s3 = Slice.of(
        mapOf(
            Pair(BooleanProperty("b1", false), SliceType.SPLIT),
            Pair(BooleanProperty("b2", false), SliceType.ALL),
            Pair(EnumProperty("e1", "nothing"), SliceType.ANY),
            Pair(EnumProperty("e2", "E00"), SliceType.SPLIT),
            Pair(IntProperty("i1", 0), SliceType.SPLIT),
            Pair(IntProperty("i2", 110), SliceType.ANY),
            Pair(DateProperty("d1", LocalDate.of(2000, 1, 1)), SliceType.ALL)
        )
    )
    private val s4 = Slice.of(
        mapOf(
            Pair(BooleanProperty("b1", true), SliceType.SPLIT),
            Pair(BooleanProperty("b2", true), SliceType.ALL),
            Pair(EnumProperty("e1", "text3"), SliceType.ANY),
            Pair(EnumProperty("e2", "E22"), SliceType.SPLIT),
            Pair(IntProperty("i1", 17), SliceType.SPLIT),
            Pair(IntProperty("i2", 100), SliceType.ANY),
            Pair(DateProperty("d1", LocalDate.of(2020, 1, 1)), SliceType.ALL)
        )
    )

    private val st1 = SliceTranslation(
        SliceSet(mutableListOf(s1), listOf(), mutableListOf(), mutableSetOf()), TranslationInfo(
            listOf(), setOf(), IntegerEncodingStore.empty(), FeatureInstantiation.empty(),
            setOf(), setOf(), setOf(), mapOf(), setOf(), mapOf(), CspEncodingContext(), mapOf()
        )
    )
    private val st2 = SliceTranslation(
        SliceSet(mutableListOf(s2, s3), listOf(), mutableListOf(), mutableSetOf()), TranslationInfo(
            listOf(), setOf(), IntegerEncodingStore.empty(), FeatureInstantiation.empty(),
            setOf(), setOf(), setOf(), mapOf(), setOf(), mapOf(), CspEncodingContext(), mapOf()
        )
    )
    private val st3 = SliceTranslation(
        SliceSet(mutableListOf(s4), listOf(), mutableListOf(), mutableSetOf()), TranslationInfo(
            listOf(), setOf(), IntegerEncodingStore.empty(), FeatureInstantiation.empty(),
            setOf(), setOf(), setOf(), mapOf(), setOf(), mapOf(), CspEncodingContext(), mapOf()
        )
    )

    private val computations = listOf(st1, st2, st3)

    private val modelTranslation = ModelTranslation(computations, emptyList())

    @Test
    fun testSliceMap() {
        assertThat(modelTranslation.sliceMap()).hasSize(4)
        assertThat(modelTranslation.sliceMap()[s1]).isEqualTo(st1)
        assertThat(modelTranslation.sliceMap()[s2]).isEqualTo(st2)
        assertThat(modelTranslation.sliceMap()[s3]).isEqualTo(st2)
        assertThat(modelTranslation.sliceMap()[s4]).isEqualTo(st3)
    }

    @Test
    fun testModelAllSlices() {
        assertThat(modelTranslation.allSlices).hasSize(4)
    }

    @Test
    fun testModelAllSplitSlices() {
        assertThat(modelTranslation.allSplitSlices()).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", true), SliceType.SPLIT),
                    Pair(EnumProperty("e2", "E22"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 17), SliceType.SPLIT)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", false), SliceType.SPLIT),
                    Pair(EnumProperty("e2", "E23"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 19), SliceType.SPLIT)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", false), SliceType.SPLIT),
                    Pair(EnumProperty("e2", "E00"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 0), SliceType.SPLIT)
                )
            )
        )
    }

    @Test
    fun testModelAllAnySlices() {
        assertThat(modelTranslation.allAnySlices()).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(EnumProperty("e1", "text1"), SliceType.ANY),
                    Pair(IntProperty("i2", 100), SliceType.ANY),
                )
            ),
            Slice.of(
                mapOf(
                    Pair(EnumProperty("e1", "text2"), SliceType.ANY),
                    Pair(IntProperty("i2", 105), SliceType.ANY),
                )
            ),
            Slice.of(
                mapOf(
                    Pair(EnumProperty("e1", "nothing"), SliceType.ANY),
                    Pair(IntProperty("i2", 110), SliceType.ANY),
                )
            ),
            Slice.of(
                mapOf(
                    Pair(EnumProperty("e1", "text3"), SliceType.ANY),
                    Pair(IntProperty("i2", 100), SliceType.ANY),
                )
            )
        )
    }

    @Test
    fun testModelAllAnySlicesForSplit() {
        val splitSlices = modelTranslation.allSplitSlices().toList()
        assertThat(modelTranslation.allAnySlices(splitSlices[0])).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", true), SliceType.SPLIT),
                    Pair(EnumProperty("e1", "text1"), SliceType.ANY),
                    Pair(EnumProperty("e2", "E22"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 17), SliceType.SPLIT),
                    Pair(IntProperty("i2", 100), SliceType.ANY),
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", true), SliceType.SPLIT),
                    Pair(EnumProperty("e1", "text3"), SliceType.ANY),
                    Pair(EnumProperty("e2", "E22"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 17), SliceType.SPLIT),
                    Pair(IntProperty("i2", 100), SliceType.ANY),
                )
            )
        )
        assertThat(modelTranslation.allAnySlices(splitSlices[1])).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", false), SliceType.SPLIT),
                    Pair(EnumProperty("e1", "text2"), SliceType.ANY),
                    Pair(EnumProperty("e2", "E23"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 19), SliceType.SPLIT),
                    Pair(IntProperty("i2", 105), SliceType.ANY),
                )
            )
        )
        assertThat(modelTranslation.allAnySlices(splitSlices[2])).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b1", false), SliceType.SPLIT),
                    Pair(EnumProperty("e1", "nothing"), SliceType.ANY),
                    Pair(EnumProperty("e2", "E00"), SliceType.SPLIT),
                    Pair(IntProperty("i1", 0), SliceType.SPLIT),
                    Pair(IntProperty("i2", 110), SliceType.ANY),
                )
            )
        )
    }

    @Test
    fun testModelAllAllSlices() {
        assertThat(modelTranslation.allAllSlices()).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), SliceType.ALL),
                    Pair(DateProperty("d1", LocalDate.of(2020, 1, 1)), SliceType.ALL)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), SliceType.ALL),
                    Pair(DateProperty("d1", LocalDate.of(2020, 2, 1)), SliceType.ALL)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), SliceType.ALL),
                    Pair(DateProperty("d1", LocalDate.of(2000, 1, 1)), SliceType.ALL)
                )
            ),
        )
    }

    @Test
    fun testModelAllAllSlicesForSplit() {
        val splitSlices = modelTranslation.allSplitSlices().toList()
        assertThat(modelTranslation.allAllSlices(splitSlices[0])).containsExactlyInAnyOrder(s1, s4)
        assertThat(modelTranslation.allAllSlices(splitSlices[1])).containsExactlyInAnyOrder(s2)
        assertThat(modelTranslation.allAllSlices(splitSlices[2])).containsExactlyInAnyOrder(s3)
    }
}
