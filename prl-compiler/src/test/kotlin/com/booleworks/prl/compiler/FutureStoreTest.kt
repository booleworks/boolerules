package com.booleworks.prl.compiler

import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.FeatureDefinition
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.PrlBooleanFeatureDefinition
import com.booleworks.prl.parser.PrlBooleanProperty
import com.booleworks.prl.parser.PrlDateProperty
import com.booleworks.prl.parser.PrlEnumFeatureDefinition
import com.booleworks.prl.parser.PrlEnumProperty
import com.booleworks.prl.parser.PrlFeature
import com.booleworks.prl.parser.PrlIntFeatureDefinition
import com.booleworks.prl.parser.PrlIntProperty
import com.booleworks.prl.parser.PrlSlicingDatePropertyDefinition
import com.booleworks.prl.parser.PrlSlicingEnumPropertyDefinition
import com.booleworks.prl.parser.PrlSlicingIntPropertyDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FutureStoreTest {
    private val s1 = PrlFeature("s1")
    private val bool1FeatureDefinition = PrlBooleanFeatureDefinition("bool1", false)
    private val boolVersioned1FeatureDefinition = PrlBooleanFeatureDefinition("bool1", true)
    private val string1FeatureDefinition = PrlEnumFeatureDefinition(
        "string1", listOf(), "desc", listOf(PrlEnumProperty("prop1", EnumRange.list("val1")))
    )
    private val int1FeatureDefinition =
        PrlIntFeatureDefinition("int1", IntRange.interval(1, 1), "desc")
    private val bool2FeatureDefinition = PrlBooleanFeatureDefinition("bool2", false)
    private val string2FeatureDefinition = PrlEnumFeatureDefinition("string2", listOf())

    /////////////////////////////////
    // Add Features Tests          //
    /////////////////////////////////
    @Nested
    inner class AddFeaturesTest {
        @Test
        fun testAddSingleBooleanFeature() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            featureStore.addDefinition(bool1FeatureDefinition, state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddSingleVersionedBooleanFeature() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            featureStore.addDefinition(boolVersioned1FeatureDefinition, state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddSingleStringFeature() {
            val state = CompilerState()
            val featureStore2 = FeatureStore()
            featureStore2.addDefinition(string1FeatureDefinition, state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(deserialize(serialize(featureStore2, mapOf()), mapOf())).isEqualTo(featureStore2)
        }

        @Test
        fun testAddSingleIntFeature() {
            val state = CompilerState()
            val featureStore3 = FeatureStore()
            featureStore3.addDefinition(int1FeatureDefinition, state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(deserialize(serialize(featureStore3, mapOf()), mapOf())).isEqualTo(featureStore3)
        }

        @Test
        fun testAddMultipleFeaturesWith_SameNameAndType() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            featureStore.addDefinition(bool1FeatureDefinition, state)
            state.context = CompilerContext(bool1FeatureDefinition.code)
            featureStore.addDefinition(bool1FeatureDefinition, state)
            assertThat(state.errors).containsExactly("[feature=bool1] Duplicate feature definition")
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddMultipleFeaturesWith_SameName_DifferentTypes() {
            val state = CompilerState()
            state.context = CompilerContext()
            val feature2 = PrlEnumFeatureDefinition("x1", listOf())
            val feature3 = PrlIntFeatureDefinition("x1", IntRange.interval(1, 1))
            val featureStore = FeatureStore()
            featureStore.addDefinition(PrlBooleanFeatureDefinition("x1", false), state)
            state.context.feature = feature2.code
            featureStore.addDefinition(feature2, state)
            assertThat(state.errors).containsExactly("[feature=x1] Duplicate feature definition")
            state.errors.clear()
            state.context.feature = feature3.code
            featureStore.addDefinition(feature3, state)
            assertThat(state.errors).containsExactly("[feature=x1] Duplicate feature definition")
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddMultipleFeatures_SameName_WithVersionedBooleanFeatures() {
            val state = CompilerState()
            state.context = CompilerContext()
            val featureBoolVersioned1 = PrlBooleanFeatureDefinition("x1", true)
            val featureBoolVersioned2 = PrlBooleanFeatureDefinition("x1", true)
            val featureBool = PrlBooleanFeatureDefinition("x1", false)
            val featureStore = FeatureStore()
            featureStore.addDefinition(featureBoolVersioned1, state)
            state.context.feature = featureBoolVersioned2.code
            featureStore.addDefinition(featureBoolVersioned2, state)
            assertThat(state.errors + state.warnings).containsExactly("[feature=x1] Duplicate feature definition")
            state.errors.clear()
            state.context.feature = featureBool.code
            featureStore.addDefinition(featureBool, state)
            assertThat(state.errors + state.warnings).containsExactly("[feature=x1] Duplicate feature definition")
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddMultipleFeaturesWith_SameType_DifferentNames() {
            val state = CompilerState()
            state.context = CompilerContext()
            val feature1 = PrlBooleanFeatureDefinition("x1", false)
            val feature2 = PrlBooleanFeatureDefinition("x2", false)
            val feature3 = PrlBooleanFeatureDefinition("x3", false)
            val feature4Versioned = PrlBooleanFeatureDefinition("x4", true)
            val featureStore = FeatureStore()
            featureStore.addDefinition(feature1, state)
            state.context.feature = feature2.code
            featureStore.addDefinition(feature2, state)
            assertThat(state.errors + state.warnings).isEmpty()
            state.context.feature = feature3.code
            featureStore.addDefinition(feature3, state)
            assertThat(state.errors + state.warnings).isEmpty()
            state.context.feature = feature4Versioned.code
            featureStore.addDefinition(feature4Versioned, state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddMultipleFeaturesWith_DifferentNamesAndTypes() {
            val state = CompilerState()
            state.context = CompilerContext()
            val feature1 = bool1FeatureDefinition
            val feature2 = string1FeatureDefinition
            val feature3 = int1FeatureDefinition
            val featureStore = FeatureStore()
            featureStore.addDefinition(feature1, state)
            state.context.feature = feature2.code
            featureStore.addDefinition(feature2, state)
            assertThat(state.errors + state.warnings).isEmpty()
            state.context.feature = feature2.code
            featureStore.addDefinition(feature3, state)
            assertThat(state.errors + state.warnings).isEmpty()
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }


        @Test
        fun testAddMultipleFeaturesWith_SameNameAndType_DifferentSlicingProperties() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("p1"), state)
            val properties1 =
                listOf(PrlBooleanProperty("ns1", BooleanRange.list(true), 11), PrlEnumProperty("p1", "A", 12))
            val bool1FeatureDefinitionSlicing = PrlBooleanFeatureDefinition("bool1", false, "", properties1, 10)
            val properties2 =
                listOf(PrlBooleanProperty("ns1", BooleanRange.list(true), 21), PrlEnumProperty("p1", "B", 22))
            val bool2FeatureDefinitionSlicing = PrlBooleanFeatureDefinition("bool1", false, "", properties2, 20)
            val properties3 =
                listOf(PrlBooleanProperty("ns1", BooleanRange.list(true), 31), PrlEnumProperty("p1", "C", 32))
            val bool3FeatureDefinitionSlicing = PrlBooleanFeatureDefinition("bool1", true, "desc", properties3, 30)

            state.context = CompilerContext(
                bool1FeatureDefinitionSlicing.code, null, bool1FeatureDefinitionSlicing.lineNumber
            )
            featureStore.addDefinition(
                bool1FeatureDefinitionSlicing, state, propertyStore.slicingPropertyDefinitions
            )
            state.context =
                CompilerContext(bool2FeatureDefinitionSlicing.code, null, bool2FeatureDefinitionSlicing.lineNumber)
            featureStore.addDefinition(
                bool2FeatureDefinitionSlicing, state, propertyStore.slicingPropertyDefinitions
            )
            state.context = CompilerContext(
                bool3FeatureDefinitionSlicing.code, null, bool3FeatureDefinitionSlicing.lineNumber
            )
            featureStore.addDefinition(
                bool3FeatureDefinitionSlicing, state, propertyStore.slicingPropertyDefinitions
            )

            assertThat(state.warnings + state.errors).isEmpty()
            assertThat(featureStore.booleanFeatures.size).isEqualTo(1)
            assertThat(featureStore.booleanFeatures["bool1"]!!.size).isEqualTo(3)
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }

        @Test
        fun testAddMultipleFeaturesWith_SameNameAndType_DuplicateSlicingProperties() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("p1"), state)
            val properties1 =
                listOf(PrlBooleanProperty("ns1", BooleanRange.list(true), 11), PrlEnumProperty("p1", "A", 12))
            val bool1FeatureDefinitionSlicing = PrlBooleanFeatureDefinition("bool1", false, "", properties1, 10)
            val properties2 =
                listOf(PrlBooleanProperty("ns2", BooleanRange.list(true), 21), PrlEnumProperty("p2", "A", 22))
            val bool2FeatureDefinitionSlicing = PrlBooleanFeatureDefinition("bool1", false, "", properties2, 20)
            val properties3 =
                listOf(PrlBooleanProperty("ns3", BooleanRange.list(true), 31), PrlEnumProperty("p3", "A", 32))
            val stringFeatureDefinitionSlicing = PrlEnumFeatureDefinition("string1", listOf(), "desc", properties3, 30)

            state.context = CompilerContext(
                bool1FeatureDefinitionSlicing.code, null, bool1FeatureDefinitionSlicing.lineNumber
            )
            featureStore.addDefinition(
                bool1FeatureDefinitionSlicing, state, propertyStore.slicingPropertyDefinitions
            )
            state.context = CompilerContext(
                bool2FeatureDefinitionSlicing.code, null, bool2FeatureDefinitionSlicing.lineNumber
            )
            featureStore.addDefinition(
                bool2FeatureDefinitionSlicing, state, propertyStore.slicingPropertyDefinitions
            )

            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).containsExactly("[feature=bool1, lineNumber=20] Duplicate feature definition")

            state.context = CompilerContext(
                stringFeatureDefinitionSlicing.code, null, stringFeatureDefinitionSlicing.lineNumber
            )
            featureStore.addDefinition(
                stringFeatureDefinitionSlicing, state, propertyStore.slicingPropertyDefinitions
            )

            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
        }
    }

    /////////////////////////////////
    // Find Matching Features Tests//
    /////////////////////////////////
    @Nested
    inner class FindMatchingFeaturesTest {
        @Test
        fun testFeatureNotDeclaredInFullFeatureStore() {
            val state = CompilerState()
            state.context = CompilerContext()
            val featureStore = FeatureStore()
            featureStore.addDefinition(bool1FeatureDefinition, state)
            featureStore.addDefinition(int1FeatureDefinition, state)
            featureStore.addDefinition(string1FeatureDefinition, state)
            val features = featureStore.findMatchingDefinitions(s1.featureCode)
            assertThat(features).isEmpty()
        }

    }

    /////////////////////////////////
    // Feature Uniqueness Tests//
    /////////////////////////////////
    @Nested
    inner class AddNotUniqueFeaturesTest {
        //	int feature f1 [1,2,3] {
        //		version 1
        //		event ["E1", "E2"]
        //	}
        //	int feature f1 [1,2,3,4] {
        //		version 2
        //		event ["E1", "E2"]
        //	}
        //	int feature f1 [1,2,3,5] {
        //		version 1
        //		event ["E3"]
        //	}
        @Test
        fun testAddMultipleFeatureDefinitions_WithUniqueSlices() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("version"), state)
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event"), state)
            val properties1 = listOf(
                PrlIntProperty("version", IntRange.list(1), 11),
                PrlEnumProperty("event", EnumRange.list("E1", "E2"), 12)
            )
            val featureDefinition1 = PrlEnumFeatureDefinition("enum1", listOf("a", "b"), "", properties1, 10)
            val properties2 = listOf(
                PrlIntProperty("version", IntRange.list(2), 21),
                PrlEnumProperty("event", EnumRange.list("E1", "E2"), 22)
            )
            val featureDefinition2 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "C"), "", properties2, 20)
            val properties3 = listOf(
                PrlIntProperty("version", IntRange.list(1), 31),
                PrlEnumProperty("event", EnumRange.list("E3"), 32)
            )
            val featureDefinition3 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "D"), "", properties3, 30)

            state.context = CompilerContext(featureDefinition1.code, null, featureDefinition1.lineNumber)
            featureStore.addDefinition(featureDefinition1, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinition2.code, null, featureDefinition2.lineNumber)
            featureStore.addDefinition(featureDefinition2, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinition3.code, null, featureDefinition3.lineNumber)
            featureStore.addDefinition(featureDefinition3, state, propertyStore.slicingPropertyDefinitions)

            assertThat(featureStore.enumFeatures.size).isEqualTo(1)
            assertThat(featureStore.enumFeatures[featureDefinition2.code]).containsExactlyInAnyOrder(
                FeatureDefinition.fromPrlDef(featureDefinition1),
                FeatureDefinition.fromPrlDef(featureDefinition2),
                FeatureDefinition.fromPrlDef(featureDefinition3)
            )
        }

        //	int feature f1 [1,2,3] {
        //		version 1
        //		event ["E1", "E2"]
        //	}
        //	int feature f1 [1,2,3,4] {
        //		version 1
        //		event ["E1"]		# Error because of the colling slice in version 1 + event "E1"
        //	}
        @Test
        fun testAddMultipleFeatureDefinitions_WithCollidingSlices() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("version"), state)
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event"), state)
            val properties1 = listOf(
                PrlIntProperty("version", IntRange.list(1), 11),
                PrlEnumProperty("event", EnumRange.list("E1", "E2"), 12)
            )
            val featureDefinition1 = PrlEnumFeatureDefinition("enum1", listOf("a", "b"), "", properties1, 10)
            val properties2 = listOf(
                PrlIntProperty("version", IntRange.list(1), 21),
                PrlEnumProperty("event", EnumRange.list("E1"), 22)
            )
            val featureDefinition2 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "C"), "", properties2, 20)

            state.context = CompilerContext(featureDefinition1.code, null, featureDefinition1.lineNumber)
            featureStore.addDefinition(featureDefinition1, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinition2.code, null, featureDefinition2.lineNumber)
            featureStore.addDefinition(featureDefinition2, state, propertyStore.slicingPropertyDefinitions)

            assertThat(featureStore.enumFeatures.size).isEqualTo(1)
            assertThat(featureStore.enumFeatures[featureDefinition2.code]).containsExactlyInAnyOrder(
                FeatureDefinition.fromPrlDef(featureDefinition1)
            )
            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).containsExactly("[feature=enum1, lineNumber=20] Duplicate feature definition")
        }

        //	int feature f1 [1,2,3] {
        //		version 1
        //		event ["E1", "E2"]
        //	}
        //	int feature f1 [1,2,3,4] {
        //		version 1			# Error, because property event is missing and thus all values of event are valid
        //	}
        @Test
        fun testAddMultipleFeatureDefinitions_WithCollidingSlicesThroughMissingProperty() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingIntPropertyDefinition("version"), state)
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event"), state)
            val properties1 = listOf(
                PrlIntProperty("version", IntRange.list(1), 11),
                PrlEnumProperty("event", EnumRange.list("E1", "E2"), 12)
            )
            val featureDefinition1 = PrlEnumFeatureDefinition("enum1", listOf("a", "b"), "", properties1, 10)
            val properties2 = listOf(PrlIntProperty("version", IntRange.list(1), 21))
            val featureDefinition2 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "C"), "", properties2, 20)
            val featureDefinition3 = PrlEnumFeatureDefinition("enum1", listOf("a", "b", "D"), "", listOf(), 30)

            state.context = CompilerContext(featureDefinition1.code, null, featureDefinition1.lineNumber)
            featureStore.addDefinition(featureDefinition1, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinition2.code, null, featureDefinition2.lineNumber)
            featureStore.addDefinition(featureDefinition2, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinition3.code, null, featureDefinition3.lineNumber)
            featureStore.addDefinition(featureDefinition3, state, propertyStore.slicingPropertyDefinitions)

            assertThat(featureStore.enumFeatures.size).isEqualTo(1)
            assertThat(featureStore.enumFeatures[featureDefinition1.code]).containsExactlyInAnyOrder(
                FeatureDefinition.fromPrlDef(featureDefinition1)
            )
            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).containsExactlyInAnyOrder(
                "[feature=enum1, lineNumber=20] Duplicate feature definition",
                "[feature=enum1, lineNumber=30] Duplicate feature definition"
            )
        }

        //	int feature f1 [1 - 100] {
        //		validity [2023-01-01 - 2023-04-01]
        //		event ["E1" - "E3"]
        //	}
        //	int feature f1 [101 - 999] {
        //		validity [2023-05-01 - 2040-12-31]
        //		event "E4"
        //	}
        //	int feature f1 [101 - 200] {
        //		validity [2020-01-01 - 2023-03-01]  # Error: Disjoint slices
        //		event "E1"
        //	}
        //	int feature f1 [201 - 400] {
        //      validity [2023-01-01 - 2023-04-01]
        //		event ["E2" - "E4"]                 # Error: Disjoint slices
        //	}
        @Test
        fun testAddMultipleFeaturesWith_SameNameAndType_DifferentSlicingProperties() {
            val state = CompilerState()
            val featureStore = FeatureStore()
            val propertyStore = PropertyStore()
            propertyStore.addSlicingPropertyDefinition(PrlSlicingDatePropertyDefinition("validity", 5), state)
            propertyStore.addSlicingPropertyDefinition(PrlSlicingEnumPropertyDefinition("event"), state)
            val properties1 = listOf(
                PrlDateProperty("validity", DateRange.interval(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 4, 1)), 11),
                PrlEnumProperty("event", EnumRange.list("E1", "E3"), 12)
            )
            val featureDefinitions1 =
                PrlIntFeatureDefinition("feature1", IntRange.interval(1, 100), "", properties1, 10)
            val properties2 = listOf(
                PrlDateProperty(
                    "validity",
                    DateRange.interval(LocalDate.of(2023, 5, 1), LocalDate.of(2040, 12, 31)),
                    21
                ),
                PrlEnumProperty("event", EnumRange.list("E4"), 22)
            )
            val featureDefinitions2 =
                PrlIntFeatureDefinition("feature1", IntRange.interval(101, 999), "", properties2, 20)
            val properties3 = listOf(
                PrlDateProperty("validity", DateRange.interval(LocalDate.of(2020, 1, 1), LocalDate.of(2023, 3, 1)), 31),
                PrlEnumProperty("event", EnumRange.list("E1"), 32)
            )
            val featureDefinitions3 =
                PrlIntFeatureDefinition("feature1", IntRange.interval(101, 200), "", properties3, 30)
            val properties4 = listOf(
                PrlDateProperty("validity", DateRange.interval(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 4, 1)), 41),
                PrlEnumProperty("event", EnumRange.list("E2", "E4"), 42)
            )
            val featureDefinitions4 =
                PrlIntFeatureDefinition("feature1", IntRange.interval(201, 400), "", properties4, 40)

            state.context = CompilerContext(featureDefinitions1.code, null, featureDefinitions1.lineNumber)
            featureStore.addDefinition(featureDefinitions1, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinitions2.code, null, featureDefinitions2.lineNumber)
            featureStore.addDefinition(featureDefinitions2, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinitions3.code, null, featureDefinitions3.lineNumber)
            featureStore.addDefinition(featureDefinitions3, state, propertyStore.slicingPropertyDefinitions)
            state.context = CompilerContext(featureDefinitions4.code, null, featureDefinitions4.lineNumber)
            featureStore.addDefinition(featureDefinitions4, state, propertyStore.slicingPropertyDefinitions)

            assertThat(featureStore.intFeatures.size).isEqualTo(1)
            assertThat(featureStore.intFeatures["feature1"]!!.size).isEqualTo(3)
            assertThat(deserialize(serialize(featureStore, mapOf()), mapOf())).isEqualTo(featureStore)
            assertThat(state.warnings).isEmpty()
            assertThat(state.errors).containsExactlyInAnyOrder("[feature=feature1, lineNumber=30] Duplicate feature definition")
        }
    }
}
