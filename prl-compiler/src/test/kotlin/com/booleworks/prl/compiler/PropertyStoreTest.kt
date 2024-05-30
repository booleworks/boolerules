package com.booleworks.prl.compiler

import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.parser.PrlBooleanProperty
import com.booleworks.prl.parser.PrlDateProperty
import com.booleworks.prl.parser.PrlEnumFeatureDefinition
import com.booleworks.prl.parser.PrlEnumProperty
import com.booleworks.prl.parser.PrlIntProperty
import com.booleworks.prl.parser.PrlSlicingBooleanPropertyDefinition
import com.booleworks.prl.parser.PrlSlicingDatePropertyDefinition
import com.booleworks.prl.parser.PrlSlicingEnumPropertyDefinition
import com.booleworks.prl.parser.PrlSlicingIntPropertyDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PropertyStoreTest {
    @Nested
    inner class AddSingleSlicingPropertyDefinitionTest {
        @Test
        fun testAddSingleBooleanSlicingPropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingBooleanPropertyDefinition("slb1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("slb1") as SlicingBooleanPropertyDefinition
            assertThat(definition.name).isEqualTo("slb1")
            assertThat(definition.values).isEmpty()
        }

        @Test
        fun testAddSingleEnumSlicingPropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("sle1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("sle1") as SlicingEnumPropertyDefinition
            assertThat(definition.name).isEqualTo("sle1")
            assertThat(definition.values).isEmpty()
        }

        @Test
        fun testAddSingleEnumListSlicingPropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("sle1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("sle1") as SlicingEnumPropertyDefinition
            assertThat(definition.name).isEqualTo("sle1")
            assertThat(definition.values).isEmpty()
        }

        @Test
        fun testAddSingleIntPropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("sln1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("sln1") as SlicingIntPropertyDefinition
            assertThat(definition.name).isEqualTo("sln1")
            assertThat(definition.startValues).isEmpty()
            assertThat(definition.endValues).isEmpty()
            assertThat(definition.singleValues).isEmpty()
        }

        @Test
        fun testAddSingleIntRangePropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("slir1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("slir1") as SlicingIntPropertyDefinition
            assertThat(definition.name).isEqualTo("slir1")
            assertThat(definition.startValues).isEmpty()
            assertThat(definition.endValues).isEmpty()
            assertThat(definition.singleValues).isEmpty()
        }

        @Test
        fun testAddSingleDatePropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingDatePropertyDefinition("sld1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("sld1") as SlicingDatePropertyDefinition
            assertThat(definition.name).isEqualTo("sld1")
            assertThat(definition.startValues).isEmpty()
            assertThat(definition.endValues).isEmpty()
            assertThat(definition.singleValues).isEmpty()
        }

        @Test
        fun testAddSingleDateRangePropertyDefinition() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingDatePropertyDefinition("sldr1"), state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition("sldr1") as SlicingDatePropertyDefinition
            assertThat(definition.name).isEqualTo("sldr1")
            assertThat(definition.startValues).isEmpty()
            assertThat(definition.endValues).isEmpty()
            assertThat(definition.singleValues).isEmpty()
        }
    }

    @Nested
    inner class AddMultiplePropertiesTest {
        @Test
        fun testAddMultipleBooleanProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingBooleanPropertyDefinition(p1), state)
            propertyStore.addProperty(PrlBooleanProperty(p1, BooleanRange.list(true)), state)
            propertyStore.addProperty(PrlBooleanProperty(p1, BooleanRange.list(false)), state)
            propertyStore.addProperty(PrlBooleanProperty(p1, BooleanRange.list(true)), state)
            propertyStore.addProperty(PrlBooleanProperty(p1, BooleanRange.list(true)), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition(p1) as SlicingBooleanPropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.values).containsExactly(false, true)
        }

        @Test
        fun testAddMultipleEnumProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition(p1), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("B")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("C")), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition(p1) as SlicingEnumPropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.values).containsExactly("A", "B", "C")
        }

        @Test
        fun testAddMultipleEnumListProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition(p1), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("C", "D")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A", "D")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A", "D")), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A", "B")), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            assertThat(state.warnings + state.errors).isEmpty()
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            val definition = propertyStore.definition(p1) as SlicingEnumPropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.values).containsExactly("A", "B", "C", "D")
        }

        @Test
        fun testAddMultipleIntProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition(p1), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.list(1)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.list(2)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.list(1)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.list(2)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.list(3)), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).isEmpty()
            val definition = propertyStore.definition(p1) as SlicingIntPropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.startValues).isEmpty()
            assertThat(definition.endValues).isEmpty()
            assertThat(definition.singleValues).containsExactly(1, 2, 3)
            assertThat(definition.min()).isEqualTo(1)
            assertThat(definition.max()).isEqualTo(3)
        }

        @Test
        fun testAddMultipleIntRangeProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition(p1, 10), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.interval(42, 100)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.interval(300, 666)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.list(42, 44, 101)), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.interval(45, 101)), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            assertThat(state.warnings + state.errors).isEmpty()
            val definition = propertyStore.definition(p1) as SlicingIntPropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.startValues).containsExactly(42, 45, 300)
            assertThat(definition.endValues).containsExactly(100, 101, 666)
            assertThat(definition.singleValues).containsExactly(42, 44, 101)
            assertThat(definition.min()).isEqualTo(42)
            assertThat(definition.max()).isEqualTo(666)
        }

        @Test
        fun testAddMultipleDateProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingDatePropertyDefinition(p1), state)
            val d1 = LocalDate.of(2023, 1, 1)
            val d2 = LocalDate.of(2020, 7, 7)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d1)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d2)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d1)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d1)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d1)), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            assertThat(state.warnings + state.errors).isEmpty()
            val definition = propertyStore.definition(p1) as SlicingDatePropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.startValues).isEmpty()
            assertThat(definition.endValues).isEmpty()
            assertThat(definition.singleValues).containsExactly(d2, d1)
            assertThat(definition.min()).isEqualTo(d2)
            assertThat(definition.max()).isEqualTo(d1)
        }

        @Test
        fun testAddMultipleDateRangeProperty() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingDatePropertyDefinition(p1), state)
            val d1 = LocalDate.of(2020, 7, 7)
            val d2 = LocalDate.of(2021, 7, 7)
            val d3 = LocalDate.of(2022, 8, 8)
            val d4 = LocalDate.of(2023, 1, 1)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.interval(d1, d3)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.interval(d2, d4)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.interval(d1, d1)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d2, d3)), state)
            propertyStore.addProperty(PrlDateProperty(p1, DateRange.list(d4)), state)
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).isEmpty()
            val definition = propertyStore.definition(p1) as SlicingDatePropertyDefinition
            assertThat(definition.name).isEqualTo(p1)
            assertThat(definition.startValues).containsExactly(d1, d2)
            assertThat(definition.endValues).containsExactly(d1, d3, d4)
            assertThat(definition.singleValues).containsExactly(d2, d3, d4)
            assertThat(definition.min()).isEqualTo(d1)
            assertThat(definition.max()).isEqualTo(d4)
        }

        @Test
        fun testAddPropertyWithWrongType() {
            val state = CompilerState()
            val propertyStore = PropertyStore()
            val p1 = "p1"
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition(p1), state)
            propertyStore.addProperty(PrlBooleanProperty(p1, BooleanRange.list(false), 10), state)
            propertyStore.addProperty(PrlEnumProperty(p1, "A", 20), state)
            propertyStore.addProperty(PrlEnumProperty(p1, EnumRange.list("A", "B"), 30), state)
            propertyStore.addProperty(PrlIntProperty(p1, 1, 40), state)
            propertyStore.addProperty(PrlIntProperty(p1, IntRange.interval(100, 200), 50), state)
            propertyStore.addProperty(PrlDateProperty(p1, LocalDate.of(2023, 1, 1), 60), state)
            propertyStore.addProperty(
                PrlDateProperty(
                    p1,
                    DateRange.interval(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 1)),
                    70
                ), state
            )
            assertThat(propertyStore.slicingPropertyDefinitions.size).isEqualTo(1)
            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).containsExactlyInAnyOrder(
                "[lineNumber=10] Property 'p1' type is not matching the defined slicing property type",
                "[lineNumber=40] Property 'p1' type is not matching the defined slicing property type",
                "[lineNumber=50] Property 'p1' type is not matching the defined slicing property type",
                "[lineNumber=60] Property 'p1' type is not matching the defined slicing property type",
                "[lineNumber=70] Property 'p1' type is not matching the defined slicing property type"
            )
        }
    }


    //	int feature f1 [1,2,3] {
    //		version 1
    //		event ["E1", "E2"]
    //	}
    //	int feature f1 [1,2,3,5] {
    //		version A 			# Typ Fehler
    //	}
    @Test
    fun testAddProperties_WithWrongSlicingPropertyType() {
        val state = CompilerState()
        val propertyStore = PropertyStore()
        propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("version", 5), state)
        propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event", 6), state)
        val properties1 = listOf(
            PrlIntProperty("version", IntRange.list(1), 11),
            PrlEnumProperty("event", EnumRange.list("E1", "E2"), 12)
        )
        val featureDefinition1 = PrlEnumFeatureDefinition("enum1", listOf("a", "b"), "", properties1, 10)
        val properties2 = listOf(PrlEnumProperty("version", EnumRange.list("VX"), 22))
        val featureDefinition2 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "C"), "", properties2, 20)

        state.context = CompilerContext(featureDefinition1.code, null, featureDefinition1.lineNumber)
        propertyStore.addProperties(featureDefinition1, state)
        state.context = CompilerContext(featureDefinition2.code, null, featureDefinition2.lineNumber)
        propertyStore.addProperties(featureDefinition2, state)

        val slicingPropDefVersion = propertyStore.slicingPropertyDefinitions["version"]
        assertThat(slicingPropDefVersion).isNotNull
        assertThat(slicingPropDefVersion!!.computeRelevantValues()).containsExactly(1)
        val slicingPropDefEvent = propertyStore.slicingPropertyDefinitions["event"]
        assertThat(slicingPropDefEvent).isNotNull
        assertThat(slicingPropDefEvent!!.computeRelevantValues()).contains("E1", "E2")
        assertThat(state.warnings).isEmpty()
        assertThat(state.errors).contains("[feature=enum1, lineNumber=20] Property type does not match slicing property type")
    }

    @Test
    fun testAddFeatureDefinition_WithDuplicateProperty() {
        val state = CompilerState()
        val propertyStore = PropertyStore()
        propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("version", 5), state)
        propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event", 6), state)
        val properties1 = listOf(
            PrlIntProperty("version", IntRange.list(1), 11),
            PrlEnumProperty("event", EnumRange.list("E1", "E2"), 12),
            PrlIntProperty("version", IntRange.list(1, 2), 13)
        )
        val featureDefinition1 = PrlEnumFeatureDefinition("enum1", listOf("a", "b"), "", properties1, 10)

        state.context = CompilerContext(featureDefinition1.code, null, featureDefinition1.lineNumber)
        propertyStore.addProperties(featureDefinition1, state)

        val slicingPropDefVersion = propertyStore.slicingPropertyDefinitions["version"]
        assertThat(slicingPropDefVersion).isNotNull
        assertThat(slicingPropDefVersion!!.computeRelevantValues()).isEmpty()
        val slicingPropDefEvent = propertyStore.slicingPropertyDefinitions["event"]
        assertThat(slicingPropDefEvent).isNotNull
        assertThat(slicingPropDefEvent!!.computeRelevantValues()).isEmpty()
        assertThat(state.warnings).isEmpty()
        assertThat(state.errors).contains("[feature=enum1, lineNumber=10] Properties in feature or rule are not unique")
    }

    @Test
    fun testAddFeatureDefinition_WithMultipleErrors() {
        val state = CompilerState()
        val propertyStore = PropertyStore()
        propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("version", 5), state)
        propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event", 6), state)
        val properties1 = listOf(
            PrlIntProperty("version", IntRange.list(1), 11),
            PrlEnumProperty("event", EnumRange.list("E1", "E2"), 12),
            PrlEnumProperty("version", EnumRange.list("1", "2"), 13)
        )
        val featureDefinition1 = PrlEnumFeatureDefinition("enum1", listOf("a", "b"), "", properties1, 10)
        val properties2 = listOf(
            PrlEnumProperty("test", EnumRange.list("VX"), 21),
            PrlEnumProperty("version", EnumRange.list("VX"), 22),
            PrlEnumProperty("test", EnumRange.list("A"), 23)
        )
        val featureDefinition2 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "C"), "", properties2, 20)

        state.context = CompilerContext(featureDefinition1.code, null, featureDefinition1.lineNumber)
        propertyStore.addProperties(featureDefinition1, state)
        state.context = CompilerContext(featureDefinition2.code, null, featureDefinition2.lineNumber)
        propertyStore.addProperties(featureDefinition2, state)

        val slicingPropDefVersion = propertyStore.slicingPropertyDefinitions["version"]
        assertThat(slicingPropDefVersion).isNotNull
        assertThat(slicingPropDefVersion!!.computeRelevantValues()).isEmpty()
        val slicingPropDefEvent = propertyStore.slicingPropertyDefinitions["event"]
        assertThat(slicingPropDefEvent).isNotNull
        assertThat(slicingPropDefEvent!!.computeRelevantValues()).isEmpty()

        assertThat(state.warnings).isEmpty()
        assertThat(state.errors).containsExactlyInAnyOrder(
            "[feature=enum1, lineNumber=10] Properties in feature or rule are not unique",
            "[feature=enum1, lineNumber=20] Properties in feature or rule are not unique"
        )
    }
}
