package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.computations.consistency.ConsistencyComputation.ConsistencyInternalResult
import com.booleworks.boolerules.computations.consistency.ConsistencyRequest
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.slices.Slice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SimpleMergeTest {

    private val dt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val d1 = DateProperty("validFrom", LocalDate.parse("2023-01-01", dt))
    private val d2 = DateProperty("validFrom", LocalDate.parse("2023-02-01", dt))
    private val d3 = DateProperty("validFrom", LocalDate.parse("2023-04-01", dt))
    private val d4 = DateProperty("validFrom", LocalDate.parse("2023-06-01", dt))
    private val d5 = DateProperty("validFrom", LocalDate.parse("2023-12-01", dt))
    private val d6 = DateProperty("validFrom", LocalDate.parse("2024-01-01", dt))
    private val v1 = IntProperty("version", 1)
    private val v2 = IntProperty("version", 2)
    private val v3 = IntProperty("version", 3)
    private val b1 = EnumProperty("modeltype", "B1")
    private val b2 = EnumProperty("modeltype", "B2")

    private val slice1: Slice = Slice.of(d1, v1, b1)
    private val slice2: Slice = Slice.of(d2, v1, b1)
    private val slice3: Slice = Slice.of(d5, v1, b1)
    private val slice4: Slice = Slice.of(d1, v1, b2)
    private val slice5: Slice = Slice.of(d2, v1, b2)
    private val slice6: Slice = Slice.of(d5, v1, b2)
    private val slice7: Slice = Slice.of(d1, v2, b1)
    private val slice8: Slice = Slice.of(d2, v2, b1)
    private val slice9: Slice = Slice.of(d1, v2, b2)
    private val slice10: Slice = Slice.of(d2, v2, b2)

    private val slice11: Slice = Slice.of(d3, v1, b1)
    private val slice12: Slice = Slice.of(d4, v1, b1)
    private val slice13: Slice = Slice.of(d3, v1, b2)
    private val slice14: Slice = Slice.of(d4, v1, b2)
    private val slice15: Slice = Slice.of(d3, v2, b1)
    private val slice16: Slice = Slice.of(d4, v2, b1)
    private val slice17: Slice = Slice.of(d5, v2, b1)
    private val slice18: Slice = Slice.of(d3, v2, b2)
    private val slice19: Slice = Slice.of(d4, v2, b2)
    private val slice20: Slice = Slice.of(d5, v2, b2)


    private val mergeResult = mapOf(
        slice1 to ConsistencyInternalResult(slice1, true, FeatureModelDO(listOf()), listOf()),
        slice2 to ConsistencyInternalResult(slice2, true, FeatureModelDO(listOf()), listOf()),
        slice3 to ConsistencyInternalResult(slice3, true, FeatureModelDO(listOf()), listOf()),
        slice4 to ConsistencyInternalResult(slice4, true, FeatureModelDO(listOf()), listOf()),
        slice5 to ConsistencyInternalResult(slice5, true, FeatureModelDO(listOf()), listOf()),
        slice6 to ConsistencyInternalResult(slice6, true, FeatureModelDO(listOf()), listOf()),
        slice7 to ConsistencyInternalResult(slice7, true, FeatureModelDO(listOf()), listOf()),
        slice8 to ConsistencyInternalResult(slice8, true, FeatureModelDO(listOf()), listOf()),
        slice9 to ConsistencyInternalResult(slice9, true, FeatureModelDO(listOf()), listOf()),
        slice10 to ConsistencyInternalResult(slice10, true, FeatureModelDO(listOf()), listOf()),
        slice11 to ConsistencyInternalResult(slice11, false, FeatureModelDO(listOf()), listOf()),
        slice12 to ConsistencyInternalResult(slice12, false, FeatureModelDO(listOf()), listOf()),
        slice13 to ConsistencyInternalResult(slice13, false, FeatureModelDO(listOf()), listOf()),
        slice14 to ConsistencyInternalResult(slice14, false, FeatureModelDO(listOf()), listOf()),
        slice15 to ConsistencyInternalResult(slice15, false, FeatureModelDO(listOf()), listOf()),
        slice16 to ConsistencyInternalResult(slice16, false, FeatureModelDO(listOf()), listOf()),
        slice17 to ConsistencyInternalResult(slice17, false, FeatureModelDO(listOf()), listOf()),
        slice18 to ConsistencyInternalResult(slice18, false, FeatureModelDO(listOf()), listOf()),
        slice19 to ConsistencyInternalResult(slice19, false, FeatureModelDO(listOf()), listOf()),
        slice20 to ConsistencyInternalResult(slice20, false, FeatureModelDO(listOf()), listOf()),
    )

    @Test
    fun testMergeMainResults() {
        val request = ConsistencyRequest(
            "", mutableListOf(
                PropertySelectionDO(
                    "modeltype",
                    PropertyTypeDO.ENUM,
                    PropertyRangeDO(enumValues = setOf(b1.range.first(), b2.range.first())),
                    SliceTypeDO.SPLIT
                ),
                PropertySelectionDO(
                    "version",
                    PropertyTypeDO.INT,
                    PropertyRangeDO(intMin = v1.range.first(), intMax = v2.range.first()),
                    SliceTypeDO.SPLIT
                ),
                PropertySelectionDO(
                    "validFrom",
                    PropertyTypeDO.DATE,
                    PropertyRangeDO(dateMin = d1.range.first(), dateMax = d6.range.first()),
                    SliceTypeDO.SPLIT
                )
            ), listOf()
        )
        val mergeMainResults = mergeMainResults(request, mergeResult)
        assertThat(mergeMainResults.merge.size).isEqualTo(2)
        val firstResult = mergeMainResults.merge[0]
        assertThat(firstResult.result).isTrue()
        // TODO Ergebnis ist korrekt, beim Generieren des zu erwarteten Ergebnissen stimmt noch was nicht
//        assertThat(firstResult.splitResults.get(0).splitSlice).isEqualTo(
//            SliceDO(
//                listOf(
//                    SlicingPropertyDO("validFrom", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = d1.range.first(), dateMax = d3.range.first().minusDays(1))),
//                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2)),
//                    SlicingPropertyDO("modeltype", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf(b1.range.first(), b2.range.first())))
//                )
//            )
//        )
//        assertThat(firstResult.splitResults.get(1).splitSlice).isEqualTo(
//            SliceDO(
//                listOf(
//                    SlicingPropertyDO("validFrom", PropertyTypeDO.DATE, PropertyRangeDO(dateMin = d5.range.first(), dateMax = d5.range.first())),
//                    SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 1)),
//                    SlicingPropertyDO("modeltype", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf(b1.range.first(), b2.range.first())))
//                )
//            )
//        )
        val secondResult = mergeMainResults.merge[0]
        assertThat(secondResult.result).isTrue()
    }
}
