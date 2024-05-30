// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.slices

import com.booleworks.prl.model.AnyFeatureDef
import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.AnySlicingPropertyDefinition
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.Property
import com.booleworks.prl.model.PropertyRange
import com.booleworks.prl.model.PropertyType
import com.booleworks.prl.model.SlicingBooleanPropertyDefinition
import com.booleworks.prl.model.SlicingDatePropertyDefinition
import com.booleworks.prl.model.SlicingEnumPropertyDefinition
import com.booleworks.prl.model.SlicingIntPropertyDefinition
import com.booleworks.prl.model.SlicingPropertyDefinition
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.rules.AnyRule
import java.time.LocalDate
import java.util.IdentityHashMap

const val MAXIMUM_NUMBER_OF_SLICES = 10_000

class MaxNumberOfSlicesExceededException(override val message: String) : Exception(message)

data class SliceSet(val slices: MutableList<Slice>, val definitions: List<AnyFeatureDef>, val rules: List<AnyRule>) {
    fun hasIntFeatures() = definitions.any { it.feature is IntFeature }
    fun hasVersionFeatures() = definitions.any { it.feature is VersionedBooleanFeature }

    override fun hashCode() = slices.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return slices == (other as SliceSet).slices
    }
}

fun computeSliceSets(slices: List<Slice>, model: PrlModel): List<SliceSet> {
    class RuleFeatureIdentitySet(featureDefs: List<AnyFeatureDef>, rules: List<AnyRule>) {
        private val features = IdentityHashMap(featureDefs.associateWith { true })
        private val rules = IdentityHashMap(rules.associateWith { true })
        private val hashCode = features.hashCode() + rules.hashCode()
        override fun hashCode() = hashCode
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as RuleFeatureIdentitySet
            if (features != other.features) return false
            if (rules != other.rules) return false
            return hashCode == other.hashCode
        }
    }

    val sliceMap = mutableMapOf<RuleFeatureIdentitySet, SliceSet>()
    slices.forEach { slice ->
        val featureDefs = model.featureDefinitions(slice)
        val rules = model.rules(slice)
        sliceMap.computeIfAbsent(RuleFeatureIdentitySet(featureDefs, rules)) {
            SliceSet(mutableListOf(), featureDefs, rules)
        }.slices.add(slice)
    }
    return sliceMap.values.toList()
}

fun computeAllSlices(
    selectors: Collection<AnySliceSelection>,
    definitions: Collection<AnySlicingPropertyDefinition>,
    maxNumberOfSlices: Int = MAXIMUM_NUMBER_OF_SLICES
): List<Slice> {
    val selectorMap = selectors.associateBy { it.property.name }
    var slices = listOf<Slice>()
    definitions.forEach { def ->
        val selector = selectorMap[def.name]
        val sliceType = selector?.sliceType
        slices = when (def) {
            is SlicingBooleanPropertyDefinition -> factorizeProperty(
                slices,
                def,
                selector?.property as BooleanProperty?,
                sliceType
            )
            is SlicingDatePropertyDefinition -> factorizeProperty(
                slices,
                def,
                selector?.property as DateProperty?,
                sliceType
            )
            is SlicingEnumPropertyDefinition -> factorizeProperty(
                slices,
                def,
                selector?.property as EnumProperty?,
                sliceType
            )
            is SlicingIntPropertyDefinition -> factorizeProperty(
                slices,
                def,
                selector?.property as IntProperty?,
                sliceType
            )
        }
        if (slices.size > maxNumberOfSlices) {
            throw MaxNumberOfSlicesExceededException("Number of slice combinationes exceeded $maxNumberOfSlices")
        }
    }
    return if (definitions.isEmpty()) listOf(Slice.empty()) else slices
}

private fun <E : Any, R : PropertyRange<E>> factorizeProperty(
    slices: List<Slice>,
    def: SlicingPropertyDefinition<E, R>,
    property: Property<E, R>?,
    sliceType: SliceType?
): List<Slice> =
    (if (property == null) def.computeRelevantValues() else def.computeRelevantValues(property.range)).let { values ->
        if (slices.isEmpty()) {
            values.map { Slice.of(property(def.name, def.propertyType, it), sliceType ?: SliceType.ANY) }
        } else {
            slices.flatMap { slice ->
                values.map {
                    slice.copyWithAdditonalProperty(
                        property(
                            def.name,
                            def.propertyType,
                            it
                        ), sliceType ?: SliceType.ANY
                    )
                }
            }
        }
    }

private fun property(propertyName: String, propertyType: PropertyType, value: Any): AnyProperty = when (propertyType) {
    PropertyType.BOOL -> BooleanProperty(propertyName, value as Boolean)
    PropertyType.DATE -> DateProperty(propertyName, value as LocalDate)
    PropertyType.ENUM -> EnumProperty(propertyName, value as String)
    PropertyType.INT -> IntProperty(propertyName, value as Int)
}
