package com.booleworks.boolerules.computations.modelenumeration

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.LIST
import com.booleworks.boolerules.computations.generic.FeatureDO
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.FeatureTypeDO
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ModelEnumerationTest : TestWithConfig() {

    private val cut = ModelEnumerationComputation
    private val model = PrlCompiler().compile(parseRuleFile("test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testComputeForSlice() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())

        val request = ModelEnumerationRequest("any", mutableListOf(), listOf(), listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.models.size).isEqualTo(2)
        assertThat(result2.models.size).isEqualTo(4)
        assertThat(result3.models.size).isEqualTo(3)
        assertThat(result4.models.size).isEqualTo(6)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeWithoutSlices(tc: TestConfig) {
        setUp(tc)
        val request = ModelEnumerationRequest("any", mutableListOf(), listOf(), listOf())
        val response = cut.computeResponse(request, model, ComputationStatusBuilder("fileId", "jobId", LIST))

        assertThat(response).hasSize(15)
        response.values.forEach { res ->
            assertThat(res.merge.filter { it.result }).hasSize(1)
        }
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeAllSplit(tc: TestConfig) {
        setUp(tc)
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
        val request = ModelEnumerationRequest("any", sliceSelection, listOf(), listOf())
        val response = cut.computeResponse(request, model, ComputationStatusBuilder("fileId", "jobId", LIST))

        assertThat(response).hasSize(15)
        response.values.forEach { res ->
            assertThat(res.merge).hasSize(2)
            assertThat(res.merge.filter { it.result }.map { it.slices }).hasSize(1)
        }
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeForSliceWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val modelTranslation = transpileModel(cf, model, listOf())

        val request = ModelEnumerationRequest("any", mutableListOf(), listOf("[c = \"c1\"]"), listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", LIST)
        )

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.models.size).isEqualTo(1)
        assertThat(result1.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
                            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
                            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
                            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p1", null),
                        )
                    ), Slice.empty()
                )
            )
        )
        assertThat(result2.models.size).isEqualTo(2)
        assertThat(result2.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
                            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
                            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
                            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p2", null),
                            FeatureDO("q", FeatureTypeDO.ENUM, null, null, "q1", null),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
                            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b1", null),
                            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
                            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p2", null),
                            FeatureDO("q", FeatureTypeDO.ENUM, null, null, "q2", null),
                        )
                    ), Slice.empty()
                )
            )
        )
        assertThat(result3.models.size).isEqualTo(1)
        assertThat(result3.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
                            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
                            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
                            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p1", null),
                        )
                    ), Slice.empty()
                )
            )
        )
        assertThat(result4.models.size).isEqualTo(2)
        assertThat(result4.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
                            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
                            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
                            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p2", null),
                            FeatureDO("q", FeatureTypeDO.ENUM, null, null, "q1", null),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.ENUM, null, null, "a2", null),
                            FeatureDO("b", FeatureTypeDO.ENUM, null, null, "b2", null),
                            FeatureDO("c", FeatureTypeDO.ENUM, null, null, "c1", null),
                            FeatureDO("p", FeatureTypeDO.ENUM, null, null, "p2", null),
                            FeatureDO("q", FeatureTypeDO.ENUM, null, null, "q2", null),
                        )
                    ), Slice.empty()
                )
            )
        )
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testNonOccurringVariables(tc: TestConfig) {
        setUp(tc)
        val featuresModels = PrlCompiler().compile(parseRuleFile("test-files/prl/transpiler/simple-color.prl"))
        val request = ModelEnumerationRequest("any", mutableListOf(), listOf(), listOf())
        val response = cut.computeResponse(request, featuresModels, ComputationStatusBuilder("fileId", "jobId", LIST))

        assertThat(response).hasSize(20)
        response.values.forEach { res ->
            assertThat(res.merge.filter { it.result }).hasSize(1)
        }
    }
}
