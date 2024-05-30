package com.booleworks.prl.parser

import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ParserFeaturesTest {

    private val ruleFile = parseRuleFile("../test-files/prl/parser/features.prl")
    private val features = ruleFile.ruleSet.featureDefinitions

    @Test
    fun testGeneral() {
        assertThat(this.features).hasSize(20)
        assertThat(this.ruleFile.fileName).isEqualTo("features.prl")
    }

    @Test
    fun testBooleanFeature1() {
        val feature: PrlBooleanFeatureDefinition = features[0] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(false)
        assertThat(feature.code).isEqualTo("ft1")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(6)
    }

    @Test
    fun testBooleanFeature2() {
        val feature: PrlBooleanFeatureDefinition = features[1] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(false)
        assertThat(feature.code).isEqualTo("ft2")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(7)
    }

    @Test
    fun testBooleanFeature3() {
        val feature: PrlBooleanFeatureDefinition = features[2] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(false)
        assertThat(feature.code).isEqualTo("ft3")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(8)
    }

    @Test
    fun testBooleanFeature4() {
        val feature: PrlBooleanFeatureDefinition = features[3] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(true)
        assertThat(feature.code).isEqualTo("ft4")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(10)
    }

    @Test
    fun testBooleanFeature5() {
        val feature: PrlBooleanFeatureDefinition = features[4] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(true)
        assertThat(feature.code).isEqualTo("ft5")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(11)
    }

    @Test
    fun testBooleanFeature6() {
        val feature: PrlBooleanFeatureDefinition = features[5] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(true)
        assertThat(feature.code).isEqualTo("ft6")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(12)
    }

    @Test
    fun testBooleanFeature7() {
        val feature: PrlBooleanFeatureDefinition = features[6] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(true)
        assertThat(feature.code).isEqualTo("ft7")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(13)
    }

    @Test
    fun testBooleanFeature8() {
        val feature: PrlBooleanFeatureDefinition = features[7] as PrlBooleanFeatureDefinition
        assertThat(feature.versioned).isEqualTo(false)
        assertThat(feature.code).isEqualTo("ft8")
        assertThat(feature.description).isEqualTo("description of ft8")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(15)
    }

    @Test
    fun testEnumFeature1() {
        val feature: PrlEnumFeatureDefinition = features[8] as PrlEnumFeatureDefinition
        assertThat(feature.values).isEqualTo(listOf("a", "b", "c"))
        assertThat(feature.code).isEqualTo("sft1")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(20)
    }

    @Test
    fun testEnumFeature2() {
        val feature: PrlEnumFeatureDefinition = features[9] as PrlEnumFeatureDefinition
        assertThat(feature.values).isEqualTo(listOf("a"))
        assertThat(feature.code).isEqualTo("sft2")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(21)
    }

    @Test
    fun testEnumFeature3() {
        val feature: PrlEnumFeatureDefinition = features[10] as PrlEnumFeatureDefinition
        assertThat(feature.values).containsExactly("bla")
        assertThat(feature.code).isEqualTo("sft3")
        assertThat(feature.description).isEqualTo("description of sft3")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(22)
    }

    @Test
    fun testEnumFeature4() {
        val feature: PrlEnumFeatureDefinition = features[11] as PrlEnumFeatureDefinition
        assertThat(feature.values).isEqualTo(listOf("a", "b"))
        assertThat(feature.code).isEqualTo("2022-10-10")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(26)
    }

    @Test
    fun testEnumFeature5() {
        val feature: PrlEnumFeatureDefinition = features[12] as PrlEnumFeatureDefinition
        assertThat(feature.values).isEqualTo(listOf("a", "b"))
        assertThat(feature.code).isEqualTo("foo")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).containsExactly(
            PrlIntProperty("v1", IntRange.list(1, 2, 3), 29),
            PrlIntProperty("v2", IntRange.interval(1, 6), 30),
            PrlDateProperty(
                "validity",
                DateRange.interval(LocalDate.parse("2020-01-01"), LocalDate.parse("2022-12-31")),
                31
            ),
            PrlDateProperty("soc", LocalDate.parse("2018-07-31"), 32)
        )
        assertThat(feature.lineNumber).isEqualTo(28)
        assertThat(feature.properties[0].lineNumber).isEqualTo(29)
        assertThat(feature.properties[1].lineNumber).isEqualTo(30)
        assertThat(feature.properties[2].lineNumber).isEqualTo(31)
        assertThat(feature.properties[3].lineNumber).isEqualTo(32)
    }

    @Test
    fun testIntFeature1() {
        val feature: PrlIntFeatureDefinition = features[13] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.list(1, 2, 3))
        assertThat(feature.code).isEqualTo("ift1")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(36)
    }

    @Test
    fun testIntFeature2() {
        val feature: PrlIntFeatureDefinition = features[14] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.list(1))
        assertThat(feature.code).isEqualTo("ift2")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(37)
    }

    @Test
    fun testIntFeature3() {
        val feature: PrlIntFeatureDefinition = features[15] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.list(0))
        assertThat(feature.code).isEqualTo("ift3")
        assertThat(feature.description).isEqualTo("description of ift3")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(38)
    }

    @Test
    fun testIntFeature4() {
        val feature: PrlIntFeatureDefinition = features[16] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.interval(1, 7))
        assertThat(feature.code).isEqualTo("ift4")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(41)
    }

    @Test
    fun testIntFeature5() {
        val feature: PrlIntFeatureDefinition = features[17] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.interval(1, 142))
        assertThat(feature.code).isEqualTo("ift5")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(42)
    }

    @Test
    fun testIntFeature6() {
        val feature: PrlIntFeatureDefinition = features[18] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.interval(1, 5))
        assertThat(feature.code).isEqualTo("ift6")
        assertThat(feature.description).isEqualTo("description of ift6")
        assertThat(feature.properties).containsExactly(
            PrlIntProperty("important version", 4, 46),
            PrlDateProperty("validFrom", LocalDate.parse("2010-01-01"), 47),
            PrlDateProperty("validTo", LocalDate.parse("2022-12-31"), 48),
            PrlEnumProperty("text", "text text text", 49),
            PrlEnumProperty("releases", EnumRange.list(listOf("R1", "R2", "R3")), 50),
            PrlBooleanProperty("active", BooleanRange.list(false), 51),
            PrlBooleanProperty("cool", BooleanRange.list(true), 52)
        )
        assertThat(feature.properties[0].lineNumber).isEqualTo(46)
        assertThat(feature.properties[1].lineNumber).isEqualTo(47)
        assertThat(feature.properties[2].lineNumber).isEqualTo(48)
        assertThat(feature.properties[3].lineNumber).isEqualTo(49)
        assertThat(feature.properties[4].lineNumber).isEqualTo(50)
        assertThat(feature.properties[5].lineNumber).isEqualTo(51)
        assertThat(feature.properties[6].lineNumber).isEqualTo(52)
        assertThat(feature.lineNumber).isEqualTo(44)
    }

    @Test
    fun testIntFeature7() {
        val feature: PrlIntFeatureDefinition = features[19] as PrlIntFeatureDefinition
        assertThat(feature.domain).isEqualTo(IntRange.list(1, 2, 3))
        assertThat(feature.code).isEqualTo("123")
        assertThat(feature.description).isEqualTo("")
        assertThat(feature.properties).isEmpty()
        assertThat(feature.lineNumber).isEqualTo(55)
    }
}
