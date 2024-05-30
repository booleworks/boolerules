package com.booleworks.prl.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

class ParserSlicingPropertiesTest {
    @Test
    fun testEmptyFile() {
        val ruleFile: PrlRuleFile = parseRuleFile(Paths.get("../test-files/prl/parser/empty.prl"))
        assertThat(ruleFile).isNotNull
        assertThat(ruleFile.ruleSet.featureDefinitions).isEmpty()
        assertThat(ruleFile.ruleSet.rules).isEmpty()
        assertThat(ruleFile.fileName).isEqualTo("empty.prl")
    }

    @Test
    fun testEmptySlicingProperties() {
        val ruleFile: PrlRuleFile = parseRuleFile(File("../test-files/prl/parser/empty_slicing_properties.prl"))
        assertThat(ruleFile).isNotNull
        assertThat(ruleFile.slicingPropertyDefinitions).isEmpty();
        assertThat(ruleFile.ruleSet.lineNumber).isEqualTo(7)
        assertThat(ruleFile.fileName).isEqualTo("empty_slicing_properties.prl")
    }

    @Test
    fun testSlicingProperties() {
        val ruleFile: PrlRuleFile = parseRuleFile(File("../test-files/prl/parser/slicing_properties.prl"))
        assertThat(ruleFile).isNotNull
        assertThat(ruleFile.slicingPropertyDefinitions.count()).isEqualTo(5);
        assertThat(ruleFile.slicingPropertyDefinitions).containsExactlyInAnyOrder(
            PrlSlicingBooleanPropertyDefinition("active", 6),
            PrlSlicingIntPropertyDefinition("version", 7),
            PrlSlicingEnumPropertyDefinition("puppy", 8),
            PrlSlicingDatePropertyDefinition("validFrom", 9),
            PrlSlicingEnumPropertyDefinition("event", 10)
        )
        assertThat(ruleFile.ruleSet.lineNumber).isEqualTo(13)
        assertThat(ruleFile.fileName).isEqualTo("slicing_properties.prl")
    }
}
