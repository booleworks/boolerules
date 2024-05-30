// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model.slices

import com.booleworks.prl.model.AnyProperty
import com.booleworks.prl.model.BooleanProperty
import com.booleworks.prl.model.BooleanRange
import com.booleworks.prl.model.DateProperty
import com.booleworks.prl.model.DateRange
import com.booleworks.prl.model.EnumProperty
import com.booleworks.prl.model.EnumRange
import com.booleworks.prl.model.IntProperty
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.Property
import com.booleworks.prl.model.PropertyRange
import com.booleworks.prl.model.compareLexicographic
import java.time.LocalDate

enum class SliceType { ANY, ALL, SPLIT }

class Slice private constructor(private val properties: MutableMap<AnyProperty, SliceType>) : Comparable<Slice> {

    companion object {
        fun empty(): Slice = Slice(mutableMapOf())

        fun of(property: AnyProperty, sliceType: SliceType = SliceType.ANY): Slice =
            Slice(mutableMapOf(Pair(property, sliceType)))

        fun of(map: Map<AnyProperty, SliceType>): Slice = Slice(map.toMutableMap())
        fun of(list: List<AnyProperty>): Slice = Slice(list.associateWith { SliceType.ANY }.toMutableMap())
        fun of(vararg list: AnyProperty): Slice = Slice(list.associateWith { SliceType.ANY }.toMutableMap())
    }

    fun copyWithAdditonalProperty(property: AnyProperty, sliceType: SliceType) =
        Slice(properties.toMutableMap()).apply { properties[property] = sliceType }

    fun selector(): List<AnySliceSelection> = properties.map { entry ->
        when (val prop = entry.key) {
            is BooleanProperty -> BooleanSliceSelection(prop, entry.value)
            is DateProperty -> DateSliceSelection(prop, entry.value)
            is IntProperty -> IntSliceSelection(prop, entry.value)
            is EnumProperty -> EnumSliceSelection(prop, entry.value)
        }
    }

    fun property(name: String) = properties.keys.first { it.name == name }
    fun allProperties() = properties.keys.toList()
    fun filterProperties(sliceTypes: Set<SliceType>) =
        Slice(properties.filter { it.value in sliceTypes }.toMutableMap())

    fun matches(slice: Slice) = slice.properties.all { properties[it.key] == it.value }

    override fun toString() = "Slice($properties)"
    override fun hashCode() = properties.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return properties == (other as Slice).properties
    }

    override fun compareTo(other: Slice) =
        compareLexicographic(properties.keys.toList(), other.properties.keys.toList())
}

typealias AnySliceSelection = SliceSelection<*, *, *>

sealed class SliceSelection<E : Any, R : PropertyRange<E>, P : Property<E, R>>(
    open val property: P,
    open val sliceType: SliceType
) {

    fun evaluate(otherProperty: P): Boolean {
        require(property.name == otherProperty.name) {
            "Cannot evaluate a filter for property '${property.name}' on property '${otherProperty.name}'"
        }
        return property.intersects(otherProperty)
    }

    fun isDiscrete() = property.range.isDiscrete()
    fun isContinuous() = property.range.isContinuous()
}

data class BooleanSliceSelection(
    override val property: BooleanProperty,
    override val sliceType: SliceType = SliceType.ANY
) :
    SliceSelection<Boolean, BooleanRange, BooleanProperty>(property, sliceType) {
    constructor(name: String, range: BooleanRange, sliceType: SliceType = SliceType.ANY) : this(
        BooleanProperty(
            name,
            range
        ), sliceType
    )
}

data class EnumSliceSelection(override val property: EnumProperty, override val sliceType: SliceType = SliceType.ANY) :
    SliceSelection<String, EnumRange, EnumProperty>(property, sliceType) {
    constructor(name: String, range: EnumRange, sliceType: SliceType = SliceType.ANY) : this(
        EnumProperty(name, range),
        sliceType
    )
}

data class IntSliceSelection(override val property: IntProperty, override val sliceType: SliceType = SliceType.ANY) :
    SliceSelection<Int, IntRange, IntProperty>(property, sliceType) {
    constructor(name: String, range: IntRange, sliceType: SliceType = SliceType.ANY) : this(
        IntProperty(name, range),
        sliceType
    )
}

data class DateSliceSelection(override val property: DateProperty, override val sliceType: SliceType = SliceType.ANY) :
    SliceSelection<LocalDate, DateRange, DateProperty>(property, sliceType) {
    constructor(name: String, range: DateRange, sliceType: SliceType = SliceType.ANY) : this(
        DateProperty(name, range),
        sliceType
    )
}

fun evaluateProperties(props: Map<String, AnyProperty>, selections: List<AnySliceSelection>) =
    selections.all { selection ->
        when (val prop = props[selection.property.name]) {
            is BooleanProperty -> (selection as BooleanSliceSelection).evaluate(prop)
            is DateProperty -> (selection as DateSliceSelection).evaluate(prop)
            is IntProperty -> (selection as IntSliceSelection).evaluate(prop)
            is EnumProperty -> (selection as EnumSliceSelection).evaluate(prop)
            else -> true
        }
    }
