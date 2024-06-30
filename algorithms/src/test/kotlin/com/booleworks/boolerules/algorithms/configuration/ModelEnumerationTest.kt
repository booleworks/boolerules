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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ModelEnumerationTest {

    private val model = PrlCompiler().compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    @Test
    fun testComputeForSlice() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val modelTranslation = transpileModel(cf, model, listOf())
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info

        val algo = ModelEnumeration(listOf())
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.models.size).isEqualTo(2)
        assertThat(result2.models.size).isEqualTo(4)
        assertThat(result3.models.size).isEqualTo(3)
        assertThat(result4.models.size).isEqualTo(6)
    }

    @Test
    fun testComputeForSliceWithAdditionalConstraints() {
        val cf = CspFactory(FormulaFactory.nonCaching())
        val additionalConstraints = listOf("[c = \"c1\"]")
        val modelTranslation = transpileModel(cf, model, listOf(), additionalConstraints = additionalConstraints)
        val info1 = modelTranslation[0].info
        val info2 = modelTranslation[1].info
        val info3 = modelTranslation[2].info
        val info4 = modelTranslation[3].info

        val algo = ModelEnumeration(listOf())
        val handler = BRTimeoutHandler.fromDuration(60)
        val result1 = algo.executeForSlice(cf, model, info1, Slice.empty(), handler)
        val result2 = algo.executeForSlice(cf, model, info2, Slice.empty(), handler)
        val result3 = algo.executeForSlice(cf, model, info3, Slice.empty(), handler)
        val result4 = algo.executeForSlice(cf, model, info4, Slice.empty(), handler)

        assertThat(result1.slice).isEqualTo(Slice.empty())
        assertThat(result1.models.size).isEqualTo(1)
        assertThat(result1.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.enum("a", "a2"),
                            FeatureInstance.enum("b", "b1"),
                            FeatureInstance.enum("c", "c1"),
                            FeatureInstance.enum("p", "p1"),
                        )
                    ), Slice.empty()
                )
            )
        )
        assertThat(result2.models.size).isEqualTo(2)
        assertThat(result2.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.enum("a", "a2"),
                            FeatureInstance.enum("b", "b1"),
                            FeatureInstance.enum("c", "c1"),
                            FeatureInstance.enum("p", "p2"),
                            FeatureInstance.enum("q", "q1"),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.enum("a", "a2"),
                            FeatureInstance.enum("b", "b1"),
                            FeatureInstance.enum("c", "c1"),
                            FeatureInstance.enum("p", "p2"),
                            FeatureInstance.enum("q", "q2"),
                        )
                    ), Slice.empty()
                )
            )
        )
        assertThat(result3.models.size).isEqualTo(1)
        assertThat(result3.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.enum("a", "a2"),
                            FeatureInstance.enum("b", "b2"),
                            FeatureInstance.enum("c", "c1"),
                            FeatureInstance.enum("p", "p1"),
                        )
                    ), Slice.empty()
                )
            )
        )
        assertThat(result4.models.size).isEqualTo(2)
        assertThat(result4.models).containsExactlyEntriesOf(
            mutableMapOf(
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.enum("a", "a2"),
                            FeatureInstance.enum("b", "b2"),
                            FeatureInstance.enum("c", "c1"),
                            FeatureInstance.enum("p", "p2"),
                            FeatureInstance.enum("q", "q1"),
                        )
                    ), Slice.empty()
                ),
                Pair(
                    FeatureModel(
                        listOf(
                            FeatureInstance.enum("a", "a2"),
                            FeatureInstance.enum("b", "b2"),
                            FeatureInstance.enum("c", "c1"),
                            FeatureInstance.enum("p", "p2"),
                            FeatureInstance.enum("q", "q2"),
                        )
                    ), Slice.empty()
                )
            )
        )
    }
}
