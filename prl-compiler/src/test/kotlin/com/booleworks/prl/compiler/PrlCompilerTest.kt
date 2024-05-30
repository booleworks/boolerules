package com.booleworks.prl.compiler

import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.intEq
import com.booleworks.prl.model.constraints.intLe
import com.booleworks.prl.model.constraints.intMul
import com.booleworks.prl.model.constraints.intSum
import com.booleworks.prl.model.deserialize
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.model.serialize
import com.booleworks.prl.parser.parseRuleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.time.LocalDate

class PrlCompilerTest {

    @Test
    fun testOnlyFeatures() {
        val parsed = parseRuleFile("../test-files/prl/parser/only_feature.prl")
        val compiler = PrlCompiler()
        val model = compiler.compile(parsed)
        val f1 = model.featureStore.findMatchingDefinitions("f1")[0].feature as EnumFeature
        assertThat(compiler.errors()).isEmpty()
        assertThat(f1.featureCode).isEqualTo("f1")
        assertThat(deserialize(serialize(model))).isEqualTo(model)
    }

    @Test
    fun testIntFile() {
        val parsed = parseRuleFile("../test-files/prl/parser/int_test.prl")
        val compiler = PrlCompiler()
        val model = compiler.compile(parsed)

        val i1 = model.featureStore.findMatchingDefinitions("i1")[0].feature as IntFeature
        val i2 = model.featureStore.findMatchingDefinitions("i2")[0].feature as IntFeature
        val i3 = model.featureStore.findMatchingDefinitions("i3")[0].feature as IntFeature
        val i4 = model.featureStore.findMatchingDefinitions("i4")[0].feature as IntFeature
        val sum = model.featureStore.findMatchingDefinitions("sum")[0].feature as IntFeature

        assertThat(compiler.errors()).isEmpty()
        assertThat(model.featureStore.allDefinitions().map { it.feature }).containsExactly(i1, i2, i3, i4, sum)
        assertThat(model.rules).hasSize(10)
        assertThat(model.rules[0]).isEqualTo(ConstraintRule(intLe(i1, sum)))
        assertThat(model.rules[1]).isEqualTo(ConstraintRule(intLe(i1, 48)))
        assertThat(model.rules[6]).isEqualTo(
            ConstraintRule(
                intEq(
                    intSum(
                        17,
                        intMul(i1),
                        intMul(2, i2),
                        intMul(-4, i3),
                        intMul(i4, false),
                    ),
                    sum,
                ),
            )
        )

        assertThat(deserialize(serialize(model))).isEqualTo(model)
    }

    @Test
    fun testIntToRuleFile() {
        val parsed = parseRuleFile("../test-files/prl/parser/int_test.prl")
        val model = PrlCompiler().compile(parsed)
        val tempFile = Files.createTempFile("temp", "prl")
        tempFile.toFile().writeText(model.toRuleFile().toString())
        val parsedModel = PrlCompiler().compile(parseRuleFile(tempFile))
        Files.deleteIfExists(tempFile)
        assertThat(parsedModel).isEqualTo(model)
    }

    @Test
    fun testInvalidFeatureNames() {
        val parsed = parseRuleFile("../test-files/prl/parser/invalid_feature_names.prl")
        val compiler = PrlCompiler()
        val model = compiler.compile(parsed)

        val f1 = model.featureStore.findMatchingDefinitions("f1")[0].feature as BooleanFeature
        val i1 = model.featureStore.findMatchingDefinitions("i1")[0].feature as IntFeature
        assertThat(compiler.errors()).containsExactly(
            "[feature=com.x1, lineNumber=7] Feature name invalid: com.x1",
            "[feature=invalid.name, lineNumber=11] Rule name invalid: invalid.name",
        )
        assertThat(model.featureStore.allDefinitions().map { it.feature }).containsExactly(f1, i1)
        assertThat(deserialize(serialize(model))).isEqualTo(model)
    }

    @Test
    fun testReal() {
        val parsed = parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl")
        val compiler = PrlCompiler()
        val model = compiler.compile(parsed)

        assertThat(deserialize(serialize(model))).isEqualTo(model)
    }

    @Test
    fun testRealToRuleFile() {
        val parsed = parseRuleFile("../test-files/prl/real/automotive/automotive_simple_1.prl")
        val model = PrlCompiler().compile(parsed)
        val tempFile = Files.createTempFile("temp", "prl")
        tempFile.toFile().writeText(model.toRuleFile().toString())
        val parsedModel = PrlCompiler().compile(parseRuleFile(tempFile))
        Files.deleteIfExists(tempFile)
        assertThat(parsedModel).isEqualTo(model)
    }

    @Test
    fun testSlicingProperties() {
        val parsed = parseRuleFile("../test-files/prl/compiler/slices.prl")
        val compiler = PrlCompiler()
        val original = compiler.compile(parsed)
        val model = deserialize(serialize(original))
        assertThat(model).isEqualTo(original)

        assertThat(compiler.warnings() + compiler.errors()).isEmpty()
        val boolDef = model.propertyStore.definition("active") as SlicingBooleanPropertyDefinition
        assertThat(boolDef.values).containsExactly(false, true)

        val intDef = model.propertyStore.definition("version") as SlicingIntPropertyDefinition
        assertThat(intDef.startValues).containsExactly(1)
        assertThat(intDef.endValues).containsExactly(5)
        assertThat(intDef.singleValues).containsExactly(1, 3)

        val dateDef = model.propertyStore.definition("validity") as SlicingDatePropertyDefinition
        assertThat(dateDef.startValues).containsExactly(LocalDate.of(2022, 1, 1), LocalDate.of(2024, 1, 1))
        assertThat(dateDef.endValues).containsExactly(LocalDate.of(2024, 12, 31), LocalDate.of(2025, 12, 31))
        assertThat(dateDef.singleValues).containsExactly(
            LocalDate.of(2023, 1, 12), LocalDate.of(2024, 1, 12), LocalDate.of(2025, 1, 12), LocalDate.of(2026, 1, 12)
        )

        val enumDef = model.propertyStore.definition("model") as SlicingEnumPropertyDefinition
        assertThat(enumDef.values).containsExactly("M1", "M10", "M2", "M3")
    }

    @Test
    fun testSlicingPropertiesToRuleFile1() {
        val parsed = parseRuleFile("../test-files/prl/compiler/slices.prl")
        val model = PrlCompiler().compile(parsed)
        val tempFile = Files.createTempFile("temp", "prl")
        tempFile.toFile().writeText(model.toRuleFile().toString())
        val parsedModel = PrlCompiler().compile(parseRuleFile(tempFile))
        Files.deleteIfExists(tempFile)
        assertThat(parsedModel).isEqualTo(model)
    }

    @Test
    fun testSlicingPropertiesToRuleFile2() {
        val parsed = parseRuleFile("../test-files/prl/transpiler/merge3.prl")
        val model = PrlCompiler().compile(parsed)
        val tempFile = Files.createTempFile("temp", "prl")
        tempFile.toFile().writeText(model.toRuleFile().toString())
        val parsedModel = PrlCompiler().compile(parseRuleFile(tempFile))
        Files.deleteIfExists(tempFile)
        assertThat(parsedModel).isEqualTo(model)
        assertThat(deserialize(serialize(model))).isEqualTo(model)
    }

    @Test
    fun testBikeshop() {
        val parsed = parseRuleFile("../test-files/prl/real/bike/bikeshop_without_slices.prl")
        val compiler = PrlCompiler()
        val model = compiler.compile(parsed)
        assertThat(compiler.errors()).isEmpty()
        val tempFile = Files.createTempFile("temp", "prl")
        tempFile.toFile().writeText(model.toRuleFile().toString())
        val parsedModel = compiler.compile(parseRuleFile(tempFile))
        Files.deleteIfExists(tempFile)
        assertThat(parsedModel).isEqualTo(model)
        assertThat(deserialize(serialize(model))).isEqualTo(model)
    }
}
