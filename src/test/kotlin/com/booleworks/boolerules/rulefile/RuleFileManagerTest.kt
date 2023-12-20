package com.booleworks.boolerules.rulefile

import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class RuleFileManagerTest {

    @Test
    fun testGenerateFeatures() {
        val parsed = parseRuleFile("test-files/prl/compiler/non_unique_features.prl")
        val compiler = PrlCompiler()
        val model = compiler.compile(parsed)
        val generatedFeatures = generateFeatures(model)
        assertThat(generatedFeatures).containsExactlyInAnyOrder(
            FeatureNameDO("top.second.a.f1", "top.second.a.f1"),
            FeatureNameDO("top.second.b.f1", "top.second.b.f1"),
            FeatureNameDO("f2", "top.second.a.f2"),
            FeatureNameDO("f3", "top.second.a.f3"),
            FeatureNameDO("f5", "top.second.b.f5")
        )
    }
}
