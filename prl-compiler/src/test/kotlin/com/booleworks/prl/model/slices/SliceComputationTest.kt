package com.booleworks.prl.model.slices

import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.model.slices.SliceType.ALL
import com.booleworks.prl.model.slices.SliceType.ANY
import com.booleworks.prl.model.slices.SliceType.SPLIT
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SliceComputationTest {
    private val date1 = LocalDate.of(2023, 7, 1)
    private val date2 = LocalDate.of(2023, 7, 2)
    private val date3 = LocalDate.of(2023, 7, 3)

    private val b1 = SlicingBooleanPropertyDefinition("b1")
    private val b2 = SlicingBooleanPropertyDefinition("b2")
    private val b3 = SlicingBooleanPropertyDefinition("b3")
    private val i1 = SlicingIntPropertyDefinition("i1")
    private val d1 = SlicingDatePropertyDefinition("d1")

    init {
        b2.addRange(BooleanRange.list(true))
        b3.addRange(BooleanRange.list(true))
        b3.addRange(BooleanRange.list(false))
        i1.addRange(IntRange.interval(1, 10))
        i1.addRange(IntRange.list(1, 14, 22))
        i1.addRange(IntRange.list(3))
        i1.addRange(IntRange.interval(3, 12))
        i1.addRange(IntRange.interval(1, 22))
        d1.addRange(DateRange.interval(date1, date3))
    }

    @Test
    fun testOneEmptyPropertyNoFilter() {
        val slices = computeAllSlices(listOf(), listOf(b1))

        assertThat(slices).isEmpty()
    }

    @Test
    fun testOneEmptyPropertyWithFilter() {
        val slices = computeAllSlices(listOf(BooleanSliceSelection("b1", BooleanRange.list(true, false))), listOf(b1))

        assertThat(slices).isEmpty()
    }

    @Test
    fun testOnePropertyNoFilter() {
        val slices1 = computeAllSlices(listOf(), listOf(b2))
        val slices2 = computeAllSlices(listOf(), listOf(b3))

        assertThat(slices1).containsExactlyInAnyOrder(
            Slice.of(BooleanProperty("b2", true)),
            Slice.of(BooleanProperty("b2", false))
        )
        assertThat(slices2).containsExactlyInAnyOrder(
            Slice.of(BooleanProperty("b3", true)),
            Slice.of(BooleanProperty("b3", false))
        )
    }

    @Test
    fun testOnePropertyWithFilter() {
        val slices1 = computeAllSlices(listOf(BooleanSliceSelection("b1", BooleanRange.list(true), ALL)), listOf(b2))
        val slices2 = computeAllSlices(listOf(BooleanSliceSelection("b2", BooleanRange.list(true), ALL)), listOf(b2))

        assertThat(slices1).containsExactlyInAnyOrder(
            Slice.of(BooleanProperty("b2", true)),
            Slice.of(BooleanProperty("b2", false))
        )
        assertThat(slices2).containsExactlyInAnyOrder(Slice.of(BooleanProperty("b2", true), ALL))
    }

    @Test
    fun testTwoPropertiesNoFilter() {
        val slices1 = computeAllSlices(listOf(), listOf(b2, d1))
        val slices2 = computeAllSlices(listOf(), listOf(d1))

        assertThat(slices1).containsExactlyInAnyOrder(
            Slice.of(BooleanProperty("b2", true), DateProperty("d1", date1)),
            Slice.of(BooleanProperty("b2", true), DateProperty("d1", date3)),
            Slice.of(BooleanProperty("b2", false), DateProperty("d1", date1)),
            Slice.of(BooleanProperty("b2", false), DateProperty("d1", date3)),
        )

        assertThat(slices2).containsExactlyInAnyOrder(
            Slice.of(DateProperty("d1", date1)), Slice.of(DateProperty("d1", date3))
        )
    }

    @Test
    fun testTwoPropertiesWithFilter() {
        val slices1 =
            computeAllSlices(listOf(BooleanSliceSelection("b2", BooleanRange.list(true), SPLIT)), listOf(b2, d1))
        val slices2 = computeAllSlices(
            listOf(
                DateSliceSelection("d1", DateRange.list(date1, date2, date3), SPLIT),
            ),
            listOf(d1)
        )

        assertThat(slices1).containsExactlyInAnyOrder(
            Slice.of(mapOf(Pair(BooleanProperty("b2", true), SPLIT), Pair(DateProperty("d1", date1), ANY))),
            Slice.of(mapOf(Pair(BooleanProperty("b2", true), SPLIT), Pair(DateProperty("d1", date3), ANY))),
        )

        assertThat(slices2).containsExactlyInAnyOrder(
            Slice.of(mapOf(Pair(DateProperty("d1", date1), SPLIT))),
            Slice.of(mapOf(Pair(DateProperty("d1", date3), SPLIT)))
        )
    }

    @Test
    fun testThreePropertiesNoFilter() {
        val slices1 = computeAllSlices(listOf(), listOf(b2, d1, i1))

        assertThat(slices1).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 1), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 2), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 3), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 4), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 11), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 13), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 14), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 15), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 22), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 1), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 2), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 3), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 4), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 11), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 13), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 14), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 15), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 22), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 1), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 2), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 3), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 4), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 11), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 13), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 14), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 15), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), ANY),
                    Pair(IntProperty("i1", 22), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 1), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 2), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 3), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 4), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 11), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 13), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 14), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 15), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date3), ANY),
                    Pair(IntProperty("i1", 22), ANY)
                )
            ),
        )
    }

    @Test
    fun testThreePropertiesNoFilterExceedMaximalSliceNumber() {
        assertThatThrownBy { computeAllSlices(listOf(), listOf(b2, d1, i1), 10) }
            .isInstanceOf(MaxNumberOfSlicesExceededException::class.java)
            .hasMessage("Number of slice combinationes exceeded 10")
    }

    @Test
    fun testThreePropertiesWithFilter() {
        val slices1 = computeAllSlices(
            listOf(
                DateSliceSelection("d1", DateRange.list(date1), SPLIT),
                IntSliceSelection("i1", IntRange.interval(5, 16), ANY)
            ),
            listOf(b2, d1, i1)
        )

        assertThat(slices1).containsExactlyInAnyOrder(
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 11), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 13), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 14), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", true), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 15), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 11), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 13), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 14), ANY)
                )
            ),
            Slice.of(
                mapOf(
                    Pair(BooleanProperty("b2", false), ANY),
                    Pair(DateProperty("d1", date1), SPLIT),
                    Pair(IntProperty("i1", 15), ANY)
                )
            ),
        )
    }

    @Test
    fun testComputeSliceSetsSingleProperty() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/compiler/rules_with_slices_1.prl"))
        val defs = model.featureStore.allDefinitions()
        val rules = model.rules

        val slices = computeAllSlices(listOf(), listOf(i1))
        val sliceSets = computeSliceSets(slices, model, listOf(), listOf())

        assertThat(slices).hasSize(9)
        assertThat(sliceSets).hasSize(7)

        assertThat(sliceSets[0].slices).containsExactly(Slice.of(IntProperty("i1", 1)))
        assertThat(sliceSets[0].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3])
        assertThat(sliceSets[0].rules).containsExactly(rules[0], rules[1], rules[2])

        assertThat(sliceSets[1].slices).containsExactly(Slice.of(IntProperty("i1", 2)))
        assertThat(sliceSets[1].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3])
        assertThat(sliceSets[1].rules).containsExactly(rules[0])

        assertThat(sliceSets[2].slices).containsExactly(Slice.of(IntProperty("i1", 3)))
        assertThat(sliceSets[2].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3], defs[4])
        assertThat(sliceSets[2].rules).containsExactly(rules[0], rules[3], rules[4])

        assertThat(sliceSets[3].slices).containsExactly(Slice.of(IntProperty("i1", 4)))
        assertThat(sliceSets[3].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3])
        assertThat(sliceSets[3].rules).containsExactly(rules[0], rules[3], rules[4])

        assertThat(sliceSets[4].slices).containsExactly(Slice.of(IntProperty("i1", 11)))
        assertThat(sliceSets[4].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3])
        assertThat(sliceSets[4].rules).containsExactly(rules[3], rules[4])

        assertThat(sliceSets[5].slices).containsExactly(
            Slice.of(IntProperty("i1", 13)),
            Slice.of(IntProperty("i1", 15))
        )
        assertThat(sliceSets[5].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3])
        assertThat(sliceSets[5].rules).isEmpty()

        assertThat(sliceSets[6].slices).containsExactly(
            Slice.of(IntProperty("i1", 14)),
            Slice.of(IntProperty("i1", 22))
        )
        assertThat(sliceSets[6].definitions).containsExactly(defs[0], defs[1], defs[2], defs[3])
        assertThat(sliceSets[6].rules).containsExactly(rules[1], rules[2])
    }

    @Test
    fun testComputeSliceSets() {
        val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/compiler/rules_with_slices_2.prl"))
        val defs = model.featureStore.allDefinitions()
        val rules = model.rules
        val version = model.propertyStore.definition("version")
        val release = model.propertyStore.definition("release")

        val slices = computeAllSlices(listOf(), listOf(version, release))
        val sliceSets = computeSliceSets(slices, model, listOf(), listOf())

        assertThat(slices).hasSize(45)
        assertThat(sliceSets).hasSize(4)

        assertThat(sliceSets[0].slices).containsExactly(
            Slice.of(IntProperty("version", 1), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 3), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 5), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 7), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 9), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 11), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 13), EnumProperty("release", "R1"))
        )
        assertThat(sliceSets[0].definitions).containsExactly(defs[0], defs[1])
        assertThat(sliceSets[0].rules).containsExactly(rules[0], rules[1], rules[2], rules[3])

        assertThat(sliceSets[1].slices).containsExactly(
            Slice.of(IntProperty("version", 1), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 1), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 3), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 3), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 5), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 5), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 7), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 7), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 9), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 9), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 11), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 11), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 13), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 13), EnumProperty("release", "R4"))
        )
        assertThat(sliceSets[1].definitions).containsExactly(defs[2], defs[3])
        assertThat(sliceSets[1].rules).containsExactly(rules[1], rules[2], rules[4])

        assertThat(sliceSets[2].slices).containsExactly(
            Slice.of(IntProperty("version", 2), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 4), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 6), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 8), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 10), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 12), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 14), EnumProperty("release", "R1")),
            Slice.of(IntProperty("version", 20), EnumProperty("release", "R1")),
        )
        assertThat(sliceSets[2].definitions).containsExactly(defs[0])
        assertThat(sliceSets[2].rules).containsExactly(rules[0], rules[1])

        assertThat(sliceSets[3].slices).containsExactly(
            Slice.of(IntProperty("version", 2), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 2), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 4), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 4), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 6), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 6), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 8), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 8), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 10), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 10), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 12), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 12), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 14), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 14), EnumProperty("release", "R4")),
            Slice.of(IntProperty("version", 20), EnumProperty("release", "R3")),
            Slice.of(IntProperty("version", 20), EnumProperty("release", "R4")),
        )
        assertThat(sliceSets[3].definitions).containsExactly(defs[2], defs[3])
        assertThat(sliceSets[3].rules).containsExactly(rules[1])

    }
}
