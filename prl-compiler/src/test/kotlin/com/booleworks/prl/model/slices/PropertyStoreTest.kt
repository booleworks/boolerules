package com.booleworks.prl.model.slices

import com.booleworks.prl.compiler.PropertyStore.Companion.uniqueSlices
import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PropertyStoreTest {
    private val slicingProperties = listOf("active", "model", "version")

    @Test
    fun testUniqueSlices_allDifferent() {
        val ps1 = mapOf(
            Pair("active", BooleanProperty("active", true)),
            Pair("model", EnumProperty("model", EnumRange.list("M1", "M2"))),
            Pair("version", IntProperty("version", IntRange.interval(1, 5)))
        )

        val ps2 = mapOf(
            Pair("active", BooleanProperty("active", false)),
            Pair("model", EnumProperty("model", EnumRange.list("M3"))),
            Pair("version", IntProperty("version", IntRange.interval(6, 8)))
        )

        assertThat(uniqueSlices(ps1, ps2, slicingProperties)).isTrue()
    }

    @Test
    fun testUniqueSlices_oneWildcard_oneIntersecting() {
        val ps1 = mapOf(
            Pair("active", BooleanProperty("active", true)),
            Pair("model", EnumProperty("model", EnumRange.list("M1", "M2"))),
            Pair("version", IntProperty("version", IntRange.interval(1, 5)))
        )

        val ps2 = mapOf(
            Pair("active", BooleanProperty("active", false)),
            Pair("model", EnumProperty("model", EnumRange.list("M2", "M3"))),
        )

        assertThat(uniqueSlices(ps1, ps2, slicingProperties)).isTrue
    }

    @Test
    fun testUniqueSlices_allWildcards() {
        val ps1 = mapOf(
            Pair("active", BooleanProperty("active", true)),
            Pair("model", EnumProperty("model", EnumRange.list("M1", "M2"))),
            Pair("version", IntProperty("version", IntRange.interval(1, 5)))
        )

        val ps2 = mapOf<String, AnyProperty>()

        assertThat(uniqueSlices(ps1, ps2, slicingProperties)).isFalse
    }

    @Test
    fun testUniqueSlices_allIntersecting() {
        val ps1 = mapOf(
            Pair("active", BooleanProperty("active", false)),
            Pair("model", EnumProperty("model", EnumRange.list("M1", "M2"))),
            Pair("version", IntProperty("version", IntRange.interval(1, 5)))
        )

        val ps2 = mapOf(
            Pair("active", BooleanProperty("active", false)),
            Pair("model", EnumProperty("model", EnumRange.list("M2", "M3"))),
            Pair("version", IntProperty("version", IntRange.interval(5, 7)))
        )

        assertThat(uniqueSlices(ps1, ps2, slicingProperties)).isFalse

        @Test
        fun testUniqueSlices_allIntersectingAndWildcards() {
            val ps1 = mapOf(
                Pair("model", EnumProperty("model", EnumRange.list("M1", "M2"))),
                Pair("version", IntProperty("version", IntRange.interval(1, 5)))
            )

            val ps2 = mapOf(
                Pair("model", EnumProperty("model", EnumRange.list("M2", "M3"))),
                Pair("version", IntProperty("version", IntRange.interval(5, 7)))
            )

            assertThat(uniqueSlices(ps1, ps2, slicingProperties)).isFalse
        }
    }
}
