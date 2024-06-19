package com.booleworks.boolerules.computations.modelenumeration

import com.booleworks.boolerules.TestConfig
import com.booleworks.boolerules.TestWithConfig
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant
import com.booleworks.boolerules.computations.generic.FeatureDO
import com.booleworks.boolerules.computations.generic.FeatureModelDO
import com.booleworks.boolerules.computations.generic.FeatureTypeDO
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ModelEnumerationIntTest : TestWithConfig() {

    private val cut = ModelEnumerationComputation
    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge4.prl"))

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
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result5 = cut.computeForSlice(
            request,
            Slice.empty(),
            info5,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result6 = cut.computeForSlice(
            request,
            Slice.empty(),
            info6,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.models.size).isEqualTo(19)
        Assertions.assertThat(result2.models.size).isEqualTo(1)
        Assertions.assertThat(result3.models.size).isEqualTo(22)
        Assertions.assertThat(result4.models.size).isEqualTo(6)
        Assertions.assertThat(result5.models.size).isEqualTo(1)
        Assertions.assertThat(result6.models.size).isEqualTo(14)
    }

    @ParameterizedTest
    @MethodSource("configs")
    fun testComputeForSliceWithAdditionalConstraints(tc: TestConfig) {
        setUp(tc)
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val additionalConstraints = listOf("[a = 5]")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraints)

        val request = ModelEnumerationRequest("any", mutableListOf(), additionalConstraints, listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info
        val result1 = cut.computeForSlice(
            request,
            Slice.empty(),
            info1,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result2 = cut.computeForSlice(
            request,
            Slice.empty(),
            info2,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result3 = cut.computeForSlice(
            request,
            Slice.empty(),
            info3,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result4 = cut.computeForSlice(
            request,
            Slice.empty(),
            info4,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result5 = cut.computeForSlice(
            request,
            Slice.empty(),
            info5,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )
        val result6 = cut.computeForSlice(
            request,
            Slice.empty(),
            info6,
            model,
            cf,
            ComputationStatusBuilder("fileId", "jobId", ComputationVariant.LIST)
        )

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.models.size).isEqualTo(5)
        Assertions.assertThat(result1.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 10),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 4),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 9),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 3),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 8),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 2),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 7),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 1),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 6),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result2.models.size).isEqualTo(1)
        Assertions.assertThat(result2.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 10),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result3.models.size).isEqualTo(3)
        Assertions.assertThat(result3.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 10),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 4),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 9),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 3),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 8),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result4.models.size).isEqualTo(3)
        Assertions.assertThat(result4.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 7),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 12),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 6),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 11),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 10),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result5.models.size).isEqualTo(0)
        Assertions.assertThat(result5.models).containsExactlyEntriesOf(
            mutableMapOf()
        )
        Assertions.assertThat(result6.models.size).isEqualTo(2)
        Assertions.assertThat(result6.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 10),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 15),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModelDO(
                        listOf(
                            FeatureDO("a", FeatureTypeDO.INT, null, null, null, 5),
                            FeatureDO("b", FeatureTypeDO.INT, null, null, null, 9),
                            FeatureDO("c", FeatureTypeDO.INT, null, null, null, 14),
                        )
                    ), Slice.empty()
                ),
            )
        )
    }
}
