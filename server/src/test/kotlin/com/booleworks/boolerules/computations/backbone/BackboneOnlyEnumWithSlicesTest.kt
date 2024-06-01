package com.booleworks.boolerules.computations.backbone

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.backbone.BackboneType.FORBIDDEN
import com.booleworks.boolerules.computations.backbone.BackboneType.MANDATORY
import com.booleworks.boolerules.computations.backbone.BackboneType.OPTIONAL
import com.booleworks.boolerules.computations.generic.ComputationElement
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.LIST
import com.booleworks.boolerules.computations.generic.FeatureDO
import com.booleworks.boolerules.computations.generic.FeatureTypeDO
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.resForMain
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.boolerules.rulefile.SlicingPropertyDO
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class BackboneOnlyEnumWithSlicesTest : TestWithConfig() {

    private val cut = BackboneComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testModel() {
        assertThat(compiler.errors()).isEmpty()
        assertThat(model.rules).hasSize(5)
    }

    @Test
    fun testComputeForSlice() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val request = BackboneRequest("any", mutableListOf(), listOf(), listOf())
        val modelTranslation = transpileModel(cf, model, listOf())
        val res = cut.computeForSlice(
            request,
            Slice.empty(),
            modelTranslation[0].info,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )

        assertThat(res.slice).isEqualTo(Slice.empty())
        assertThat(res.backbone).hasSize(9)
        assertThat(res.backbone[FeatureDO.enum("a", "a1")]).isEqualTo(FORBIDDEN)
        assertThat(res.backbone[FeatureDO.enum("a", "a2")]).isEqualTo(MANDATORY)
        assertThat(res.backbone[FeatureDO.enum("b", "b1")]).isEqualTo(MANDATORY)
        assertThat(res.backbone[FeatureDO.enum("b", "b2")]).isEqualTo(FORBIDDEN)
        assertThat(res.backbone[FeatureDO.enum("b", "b3")]).isEqualTo(FORBIDDEN)
        assertThat(res.backbone[FeatureDO.enum("c", "c1")]).isEqualTo(OPTIONAL)
        assertThat(res.backbone[FeatureDO.enum("c", "c2")]).isEqualTo(OPTIONAL)
        assertThat(res.backbone[FeatureDO.enum("p", "px")]).isEqualTo(FORBIDDEN)
        assertThat(res.backbone[FeatureDO.enum("p", "p1")]).isEqualTo(MANDATORY)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = BackboneRequest("any", sliceSelection, listOf(), listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", LIST))

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(9)
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(11)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesAndVersionSplitSlicesWithRestrictedVars(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )
        val slice3 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice4 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 2), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intValues = setOf(1, 2)),
                SliceTypeDO.SPLIT
            )
        )
        val request = BackboneRequest("any", sliceSelection, listOf(), listOf("a", "b", "c"))

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", LIST))
        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!
        val sliceResult3 = result[slice3]!!
        val sliceResult4 = result[slice4]!!

        assertThat(result).hasSize(4)

        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(7)
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(7)
        assertThat(sliceResult3.slice).isEqualTo(slice3)
        assertThat(sliceResult3.backbone).hasSize(8)
        assertThat(sliceResult4.slice).isEqualTo(slice4)
        assertThat(sliceResult4.backbone).hasSize(8)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitSlicesWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = BackboneRequest("any", sliceSelection, listOf("[c = \"c1\"]"), listOf())

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", LIST))

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(9)
        assertThat(sliceResult1.backbone.keys).containsExactly(
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p1", null),
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a1", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b3", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c2", null),
            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "px", null)
        )
        assertThat(sliceResult1.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN
        )
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(11)
        assertThat(sliceResult2.backbone.keys).containsExactly(
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p2", null),
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a1", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b3", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c2", null),
            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "px", null),
            FeatureDO("q", FeatureTypeDO.ENUM, null, null, "q1", null),
            FeatureDO("q", FeatureTypeDO.ENUM, null, null, "q2", null)
        )
        assertThat(sliceResult2.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            OPTIONAL,
            OPTIONAL
        )
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitSlicesWithRestrictedVarsWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S1"), SliceType.SPLIT)
            )
        )
        val slice2 = Slice.of(
            mapOf(
                Pair(IntProperty("version", 1), SliceType.SPLIT),
                Pair(EnumProperty("series", "S2"), SliceType.SPLIT)
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intValues = setOf(1)), SliceTypeDO.SPLIT)
        )
        val request = BackboneRequest("any", sliceSelection, listOf("[c = \"c1\"]"), listOf("a", "b", "c"))

        val result = cut.computeForModel(request, model, ComputationStatusBuilder("fileId", "jobId", LIST))

        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(7)
        assertThat(sliceResult1.backbone.keys).containsExactly(
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a1", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b3", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c2", null)
        )
        assertThat(sliceResult1.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN
        )
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(7)
        assertThat(sliceResult2.backbone.keys).containsExactly(
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a1", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b3", null),
            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c2", null)
        )
        assertThat(sliceResult2.backbone.values).containsExactly(
            MANDATORY,
            MANDATORY,
            MANDATORY,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN,
            FORBIDDEN
        )
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeSeriesSplitVersionAllSlices(tc: TestConfig) {
        setUp(tc)
        val slice1 = Slice.of(mapOf(Pair(EnumProperty("series", "S1"), SliceType.SPLIT)))
        val slice2 = Slice.of(mapOf(Pair(EnumProperty("series", "S2"), SliceType.SPLIT)))

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2), SliceTypeDO.ALL)
        )
        val request = BackboneRequest("any", sliceSelection, listOf(), listOf())

        val status = ComputationStatusBuilder("fileId", "jobId", LIST)
        val result = cut.computeForModel(request, model, status)
        val sliceResult1 = result[slice1]!!
        val sliceResult2 = result[slice2]!!

        assertThat(result).hasSize(2)
        assertThat(status.build().warnings).containsExactlyInAnyOrder(
            "Original rule set for the slice Slice({series \"S1\"=SPLIT}) is inconsistent. Use the 'consistency' check to get an explanation, why.",
            "Original rule set for the slice Slice({series \"S2\"=SPLIT}) is inconsistent. Use the 'consistency' check to get an explanation, why."
        )
        assertThat(sliceResult1.slice).isEqualTo(slice1)
        assertThat(sliceResult1.backbone).hasSize(10)
        assertThat(sliceResult1.backbone.values).containsOnly(FORBIDDEN)
        assertThat(sliceResult2.slice).isEqualTo(slice2)
        assertThat(sliceResult2.backbone).hasSize(12)
        assertThat(sliceResult2.backbone.values).containsOnly(FORBIDDEN)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseAllSplit(tc: TestConfig) {
        setUp(tc)
        val sliceAll = SliceDO(
            listOf(
                SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1", "S2"))),
                SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2))
            )
        )
        val sliceOnlyV1 = SliceDO(
            listOf(
                SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1", "S2"))),
                SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 1))
            )
        )
        val sliceOnlyV2 = SliceDO(
            listOf(
                SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1", "S2"))),
                SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 2, intMax = 2))
            )
        )
        val sliceOnlyS1 = SliceDO(
            listOf(
                SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S1"))),
                SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2))
            )
        )
        val sliceOnlyS2 = SliceDO(
            listOf(
                SlicingPropertyDO("series", PropertyTypeDO.ENUM, PropertyRangeDO(enumValues = setOf("S2"))),
                SlicingPropertyDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2))
            )
        )

        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.SPLIT
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intMin = 1, intMax = 2),
                SliceTypeDO.SPLIT
            )
        )
        val request = BackboneRequest("any", sliceSelection, listOf(), listOf())

        val sb = ComputationStatusBuilder("fileId", "jobId", LIST)
        val respone = cut.computeResponse(request, model, sb)
        val status = sb.build()

        assertThat(status.success).isTrue
        assertThat(status.statistics.numberOfSlices).isEqualTo(4)
        assertThat(status.statistics.numberOfSliceComputations).isEqualTo(4)
        assertThat(status.jobId).isEqualTo("jobId")
        assertThat(status.ruleFileId).isEqualTo("fileId")
        assertThat(status.errors).isEmpty()
        assertThat(status.warnings).isEmpty()
        assertThat(status.infos).isEmpty()

        assertThat(respone).hasSize(13)

        var r = respone[ComputationElement(1, FeatureDO.enum("a", "a1"))]!!
        assertThat(r.merge).hasSize(1)
        assertThat(r.merge[0].result).isEqualTo(FORBIDDEN)
        assertThat(r.merge[0].slices).hasSize(1)
        assertThat(r.merge[0].slices[0]).isEqualTo(sliceAll)

        r = respone[ComputationElement(2, FeatureDO.enum("a", "a2"))]!!
        assertThat(r.merge).hasSize(1)
        assertThat(r.merge[0].result).isEqualTo(MANDATORY)
        assertThat(r.merge[0].slices).hasSize(1)
        assertThat(r.merge[0].slices[0]).isEqualTo(sliceAll)

        r = respone[ComputationElement(3, FeatureDO.enum("b", "b1"))]!!
        assertThat(r.merge).hasSize(2)

        var res = resForMain(MANDATORY, r.merge)
        assertThat(res.result).isEqualTo(MANDATORY)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyV1)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyV2)

        r = respone[ComputationElement(4, FeatureDO.enum("b", "b2"))]!!
        assertThat(r.merge).hasSize(2)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyV1)

        res = resForMain(MANDATORY, r.merge)
        assertThat(res.result).isEqualTo(MANDATORY)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyV2)

        r = respone[ComputationElement(5, FeatureDO.enum("b", "b3"))]!!
        assertThat(r.merge).hasSize(1)
        assertThat(r.merge[0].result).isEqualTo(FORBIDDEN)
        assertThat(r.merge[0].slices).hasSize(1)
        assertThat(r.merge[0].slices[0]).isEqualTo(sliceAll)

        r = respone[ComputationElement(6, FeatureDO.enum("c", "c1"))]!!
        assertThat(r.merge).hasSize(1)
        assertThat(r.merge[0].result).isEqualTo(OPTIONAL)
        assertThat(r.merge[0].slices).hasSize(1)
        assertThat(r.merge[0].slices[0]).isEqualTo(sliceAll)

        r = respone[ComputationElement(7, FeatureDO.enum("c", "c2"))]!!
        assertThat(r.merge).hasSize(1)
        assertThat(r.merge[0].result).isEqualTo(OPTIONAL)
        assertThat(r.merge[0].slices).hasSize(1)
        assertThat(r.merge[0].slices[0]).isEqualTo(sliceAll)

        r = respone[ComputationElement(8, FeatureDO.enum("c", "c3"))]!!
        assertThat(r.merge).hasSize(2)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyV1)

        res = resForMain(OPTIONAL, r.merge)
        assertThat(res.result).isEqualTo(OPTIONAL)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyV2)

        r = respone[ComputationElement(9, FeatureDO.enum("p", "p1"))]!!
        assertThat(r.merge).hasSize(2)

        res = resForMain(MANDATORY, r.merge)
        assertThat(res.result).isEqualTo(MANDATORY)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS1)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS2)

        r = respone[ComputationElement(10, FeatureDO.enum("p", "p2"))]!!
        assertThat(r.merge).hasSize(2)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS1)

        res = resForMain(MANDATORY, r.merge)
        assertThat(res.result).isEqualTo(MANDATORY)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS2)

        r = respone[ComputationElement(11, FeatureDO.enum("p", "px"))]!!
        assertThat(r.merge).hasSize(1)
        assertThat(r.merge[0].result).isEqualTo(FORBIDDEN)
        assertThat(r.merge[0].slices).hasSize(1)
        assertThat(r.merge[0].slices[0]).isEqualTo(sliceAll)

        r = respone[ComputationElement(12, FeatureDO.enum("q", "q1"))]!!
        assertThat(r.merge).hasSize(2)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS1)

        res = resForMain(OPTIONAL, r.merge)
        assertThat(res.result).isEqualTo(OPTIONAL)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS2)

        r = respone[ComputationElement(13, FeatureDO.enum("q", "q2"))]!!
        assertThat(r.merge).hasSize(2)

        res = resForMain(FORBIDDEN, r.merge)
        assertThat(res.result).isEqualTo(FORBIDDEN)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS1)

        res = resForMain(OPTIONAL, r.merge)
        assertThat(res.result).isEqualTo(OPTIONAL)
        assertThat(res.slices).hasSize(1)
        assertThat(res.slices[0]).isEqualTo(sliceOnlyS2)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeResponseWithAnySlice(tc: TestConfig) {
        setUp(tc)
        val sliceSelection = mutableListOf(
            PropertySelectionDO(
                "series",
                PropertyTypeDO.ENUM,
                PropertyRangeDO(enumValues = setOf("S1", "S2")),
                SliceTypeDO.ANY
            ),
            PropertySelectionDO(
                "version",
                PropertyTypeDO.INT,
                PropertyRangeDO(intMin = 1, intMax = 2),
                SliceTypeDO.SPLIT
            )
        )
        val request = BackboneRequest("any", sliceSelection, listOf(), listOf())

        assertThatThrownBy { cut.computeResponse(request, model, ComputationStatusBuilder("fileId", "jobId", LIST)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Selection for property 'series' has slice type ANY which is not allowed for this computation")
    }
}
