// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.compiler

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.AnySlicingPropertyDefinition
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.PropertyRange
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.model.SlicingPropertyDefinition
import com.booleworks.prl.model.compileProperties
import com.booleworks.prl.parser.AnyPrlProperty
import com.booleworks.prl.parser.PrlFeatureDefinition
import com.booleworks.prl.parser.PrlProperty
import com.booleworks.prl.parser.PrlRule
import com.booleworks.prl.parser.PrlSlicingPropertyDefinition

data class PropertyStore(
    internal val slicingPropertyDefinitions: MutableMap<String, AnySlicingPropertyDefinition> = mutableMapOf()
) {

    internal fun addSlicingPropertyDefinition(
        prlSlicingPropertyDefinition: PrlSlicingPropertyDefinition,
        state: CompilerState
    ): Boolean {
        state.context.lineNumber = prlSlicingPropertyDefinition.lineNumber
        val existingDefinition = slicingPropertyDefinitions[prlSlicingPropertyDefinition.name]
        if (existingDefinition != null && existingDefinition.javaClass != prlSlicingPropertyDefinition.javaClass) {
            state.addError("Slicing property type does not match the defined property type.")
            return false
        }
        if (existingDefinition == null) {
            slicingPropertyDefinitions[prlSlicingPropertyDefinition.name] =
                SlicingPropertyDefinition.from(prlSlicingPropertyDefinition)
        }
        return true
    }

    internal fun addProperties(featureDef: PrlFeatureDefinition, state: CompilerState) {
        if (!checkPropertiesCorrect(
                featureDef.properties,
                compileProperties(featureDef.properties).associateBy { it.name },
                state
            )
        ) {
            return
        }
        featureDef.properties.forEach {
            state.context.lineNumber = it.lineNumber
            addProperty(it, state)
        }
    }

    internal fun addProperties(rule: PrlRule, state: CompilerState) {
        if (!checkPropertiesCorrect(rule.properties, compileProperties(rule.propsMap()), state)) {
            return
        }
        rule.properties.forEach {
            state.context.lineNumber = it.lineNumber
            addProperty(it, state)
        }
    }

    fun definition(name: String) =
        slicingPropertyDefinitions[name] ?: throw NoSuchElementException("Unknown property '$name'")

    fun allDefinitions(): List<AnySlicingPropertyDefinition> = slicingPropertyDefinitions.values.toList()

    internal fun <T : PropertyRange<*>> addProperty(prlProperty: PrlProperty<T>, state: CompilerState): Boolean {
        state.context.lineNumber = prlProperty.lineNumber
        val def = slicingPropertyDefinitions[prlProperty.name]
        if (def != null) {
            if (!isTypeMatching(def, prlProperty)) {
                state.addError("Property '${prlProperty.name}' type is not matching the defined slicing property type")
                return false
            }
            val result = when (def) {
                is SlicingBooleanPropertyDefinition -> def.addRange(prlProperty.value as BooleanRange)
                is SlicingIntPropertyDefinition -> def.addRange(prlProperty.value as IntRange)
                is SlicingDatePropertyDefinition -> def.addRange(prlProperty.value as DateRange)
                is SlicingEnumPropertyDefinition -> def.addRange(prlProperty.value as EnumRange)
            }
            if (!result.success) {
                state.addError(result.message.orEmpty())
                return false
            }
        }
        return true
    }

    internal fun checkPropertiesCorrect(
        prlProperties: List<AnyPrlProperty>,
        propMap: Map<String, AnyProperty>,
        state: CompilerState
    ): Boolean {
        if (!propertiesUnique(prlProperties)) {
            state.addError("Properties in feature or rule are not unique")
            return false
        }
        if (!propertyTypesCorrect(propMap, slicingPropertyDefinitions)) {
            state.addError("Property type does not match slicing property type")
            return false
        }
        return true
    }

    companion object {
        internal fun uniqueSlices(
            ps1: Map<String, AnyProperty>,
            ps2: Map<String, AnyProperty>,
            slicingProperties: Collection<String>
        ) =
            slicingProperties.any { propertyName ->
                val p1 = ps1[propertyName]
                val p2 = ps2[propertyName]
                p1 != null && p2 != null && disjointSlicingProperties(p1, p2)
            }

        private fun disjointSlicingProperties(p1: AnyProperty, p2: AnyProperty) = when {
            p1 is BooleanProperty && p2 is BooleanProperty -> p1.disjoint(p2)
            p1 is IntProperty && p2 is IntProperty -> p1.disjoint(p2)
            p1 is DateProperty && p2 is DateProperty -> p1.disjoint(p2)
            p1 is EnumProperty && p2 is EnumProperty -> p1.disjoint(p2)
            else -> throw IllegalArgumentException("Slicing property have to have a unique type")
        }

        private fun isTypeMatching(property: AnySlicingPropertyDefinition, value: AnyPrlProperty) =
            property.propertyType == value.propertyType

        private fun isTypeMatching(property: AnySlicingPropertyDefinition, value: AnyProperty): Boolean =
            property.propertyType == value.propertyType

        internal fun propertiesUnique(properties: List<PrlProperty<*>>) =
            properties.map { it.name }.toSet().size == properties.size

        internal fun propertyTypesCorrect(
            properties: Map<String, AnyProperty>,
            slicingProperties: MutableMap<String, AnySlicingPropertyDefinition>
        ): Boolean =
            slicingProperties.all { sp ->
                val property = properties[sp.key]
                property == null || isTypeMatching(sp.value, property)
            }

    }
}
