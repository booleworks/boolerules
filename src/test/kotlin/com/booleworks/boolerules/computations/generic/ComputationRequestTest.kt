package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.computations.consistency.ConsistencyRequest
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalStdlibApi::class)
class ComputationRequestTest {

    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("test-files/prl/compiler/slices.prl"))

    @Test
    fun testAddAllSlicingPropertiesForMissingFilters() {
        val request = ConsistencyRequest("ruleFile", mutableListOf(), listOf())
        request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet())

        assertThat(request.sliceSelection).containsExactlyInAnyOrder(
            PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true, false))),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 5)),
            PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2", "M3", "M10"))),
            PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2026, 1, 12))),
            PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3"))),
        )
    }

    @Test
    fun testAddSlicingPropertiesForMissingFilters() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true))),
                PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2"))),
                PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2023, 1, 12))),
            ), listOf()
        )
        request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet())

        assertThat(request.sliceSelection).containsExactlyInAnyOrder(
            PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true))),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 5)),
            PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2"))),
            PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2023, 1, 12))),
            PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3"))),
        )
    }

    @Test
    fun testAddMissingUpperBoundsPropertiesForMissingFilters() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true))),
                PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1)),
                PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2"))),
                PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1))),
                PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3"))),
            ), listOf()
        )
        request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet())

        assertThat(request.sliceSelection).containsExactlyInAnyOrder(
            PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true))),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 5)),
            PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2"))),
            PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2026, 1, 12))),
            PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3"))),
        )
    }

    @Test
    fun testAddMissingLowerBoundsPropertiesForMissingFilters() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true))),
                PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMax = 3)),
                PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2"))),
                PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMax = LocalDate.of(2022, 1, 1))),
                PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3"))),
            ), listOf()
        )
        request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet())

        assertThat(request.sliceSelection).containsExactlyInAnyOrder(
            PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true))),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 3)),
            PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2"))),
            PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2022, 1, 1))),
            PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3"))),
        )
    }

    @Test
    fun testAddMissingPropertiesForMissingFilters() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(), SliceTypeDO.SPLIT),
                PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(), SliceTypeDO.ANY),
                PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(), SliceTypeDO.ALL),
                PropertySelectionDO("validity", PropertyTypeDO.DATE, PropertyRangeDO(), SliceTypeDO.SPLIT),
                PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(), SliceTypeDO.ALL),
            ), listOf()
        )
        request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet())

        assertThat(request.sliceSelection).containsExactlyInAnyOrder(
            PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true, false)), SliceTypeDO.SPLIT),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 5), SliceTypeDO.ANY),
            PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2", "M3", "M10")), SliceTypeDO.ALL),
            PropertySelectionDO(
                "validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2026, 1, 12)), SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3")), SliceTypeDO.ALL),
        )
    }

    @Test
    fun testErrorInt() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1, 2), intMin = 3), SliceTypeDO.ANY),
            ), listOf()
        )
        assertThatThrownBy { request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet()) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Set values and a range of int property 'version' at the same time")
    }

    @Test
    fun testErrorDate() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO(
                    "validity", PropertyTypeDO.DATE,
                    PropertyRangeDO(dateValues = setOf(LocalDate.now()), dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2026, 1, 12))
                ),
            ), listOf()
        )
        assertThatThrownBy { request.validateAndAugmentSliceSelection(model, SliceTypeDO.entries.toSet()) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Set values and a range of date property 'validity' at the same time")
    }

    @Test
    fun testNotAllowedSliceTypeDefault() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO(
                    "validity", PropertyTypeDO.DATE,
                    PropertyRangeDO(dateValues = setOf(LocalDate.now()), dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2026, 1, 12))
                ),
            ), listOf()
        )
        assertThatThrownBy { request.validateAndAugmentSliceSelection(model, setOf(SliceTypeDO.SPLIT, SliceTypeDO.ALL)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Property 'active' has no selection and defaults to slice type ANY which is not allowed for this computation")
    }

    @Test
    fun testNotAllowedSliceType() {
        val request = ConsistencyRequest(
            "ruleFile", mutableListOf(
                PropertySelectionDO("active", PropertyTypeDO.BOOLEAN, PropertyRangeDO(booleanValues = setOf(true, false)), SliceTypeDO.SPLIT),
                PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 5), SliceTypeDO.SPLIT),
                PropertySelectionDO("model", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("M1", "M2", "M3", "M10")), SliceTypeDO.ANY),
                PropertySelectionDO(
                    "validity", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = LocalDate.of(2022, 1, 1), dateMax = LocalDate.of(2026, 1, 12)), SliceTypeDO.SPLIT
                ),
                PropertySelectionDO("release", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("R1", "R2", "R3")), SliceTypeDO.ALL),
            ), listOf()
        )
        assertThatThrownBy { request.validateAndAugmentSliceSelection(model, setOf(SliceTypeDO.SPLIT, SliceTypeDO.ANY)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Selection for property 'release' has slice type ALL which is not allowed for this computation")
    }
}
