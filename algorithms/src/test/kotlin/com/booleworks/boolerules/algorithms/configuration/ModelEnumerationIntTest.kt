package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureInstance
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.parser.parseRuleFile
import com.booleworks.prl.transpiler.transpileModel
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class ModelEnumerationIntTest {

    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge4.prl"))

    @Test
    fun testComputeForSlice() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info

        val algo = ModelEnumeration(listOf())
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)
        val result5 = algo.executeForSlice(cf, model, info5, Slice.empty(), handler)
        val result6 = algo.executeForSlice(cf, model, info6, Slice.empty(), handler)

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.models.size).isEqualTo(19)
        Assertions.assertThat(result2.models.size).isEqualTo(1)
        Assertions.assertThat(result3.models.size).isEqualTo(22)
        Assertions.assertThat(result4.models.size).isEqualTo(6)
        Assertions.assertThat(result5.models.size).isEqualTo(1)
        Assertions.assertThat(result6.models.size).isEqualTo(14)
    }

    @Test
    fun testComputeForSliceWithAdditionalConstraints() {
        val f = FormulaFactory.nonCaching()
        val cf = CspFactory(f)
        val additionalConstraints = listOf("[a = 5]")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraints)
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info
        val info5 = modelTranslation[4].info
        val info6 = modelTranslation[5].info

        val algo = ModelEnumeration(listOf())
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)
        val result5 = algo.executeForSlice(cf, model, info5, Slice.empty(), handler)
        val result6 = algo.executeForSlice(cf, model, info6, Slice.empty(), handler)

        Assertions.assertThat(result1.slice).isEqualTo(Slice.empty())
        Assertions.assertThat(result1.models.size).isEqualTo(5)
        Assertions.assertThat(result1.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 5),
                            FeatureInstance.int("c", 10)
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 4),
                            FeatureInstance.int("c", 9),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 3),
                            FeatureInstance.int("c", 8),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 2),
                            FeatureInstance.int("c", 7),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 1),
                            FeatureInstance.int("c", 6),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result2.models.size).isEqualTo(1)
        Assertions.assertThat(result2.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 5),
                            FeatureInstance.int("c", 10),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result3.models.size).isEqualTo(3)
        Assertions.assertThat(result3.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 5),
                            FeatureInstance.int("c", 10),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 4),
                            FeatureInstance.int("c", 9),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 3),
                            FeatureInstance.int("c", 8),
                        )
                    ), Slice.empty()
                ),
            )
        )
        Assertions.assertThat(result4.models.size).isEqualTo(3)
        Assertions.assertThat(result4.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 7),
                            FeatureInstance.int("c", 12),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 6),
                            FeatureInstance.int("c", 11),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 5),
                            FeatureInstance.int("c", 10),
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
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 10),
                            FeatureInstance.int("c", 15),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.int("a", 5),
                            FeatureInstance.int("b", 9),
                            FeatureInstance.int("c", 14),
                        )
                    ), Slice.empty()
                ),
            )
        )
    }
}
