// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.parser.AnyPrlProperty
import com.booleworks.prl.parser.PragmaticRuleLanguage.identifier
import com.booleworks.prl.parser.PrlBooleanProperty
import com.booleworks.prl.parser.PrlDateProperty
import com.booleworks.prl.parser.PrlEnumProperty
import com.booleworks.prl.parser.PrlIntProperty
import java.time.LocalDate
import java.util.Objects

typealias AnyProperty = Property<*, *>

enum class PropertyType {
    BOOL,
    INT,
    DATE,
    ENUM
}

sealed class Property<E : Any, R : PropertyRange<E>>(
    open val name: String,
    open val range: R,
    val propertyType: PropertyType
) : Comparable<AnyProperty> {
    companion object {
        internal fun fromPrlProperty(prlProperty: AnyPrlProperty): AnyProperty = when (prlProperty) {
            is PrlBooleanProperty -> BooleanProperty(prlProperty.name, prlProperty.value)
            is PrlEnumProperty -> EnumProperty(prlProperty.name, prlProperty.value)
            is PrlIntProperty -> IntProperty(prlProperty.name, prlProperty.value)
            is PrlDateProperty -> DateProperty(prlProperty.name, prlProperty.value)
        }
    }

    abstract fun intersects(property: Property<E, R>): Boolean
    fun disjoint(property: Property<E, R>) = !intersects(property)

    override fun hashCode() = Objects.hash(name, range)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyProperty) return false

        if (name != other.name) return false
        return range == other.range
    }
}

class BooleanProperty(override val name: String, override val range: BooleanRange) :
    Property<Boolean, BooleanRange>(name, range, PropertyType.BOOL) {
    constructor(name: String, value: Boolean) : this(name, BooleanRange.list(value))

    override fun toString() = identifier(name) + " " + range
    override fun intersects(property: Property<Boolean, BooleanRange>) = range.intersects(property.range)
    override fun compareTo(other: AnyProperty): Int {
        require(other is BooleanProperty && other.name == name) { "Cannot compare properties of different type" }
        return range.compareTo(other.range)
    }
}

class DateProperty(override val name: String, override val range: DateRange) :
    Property<LocalDate, DateRange>(name, range, PropertyType.DATE) {
    constructor(name: String, value: LocalDate) : this(name, DateRange.list(value))

    override fun toString() = identifier(name) + " " + range
    override fun intersects(property: Property<LocalDate, DateRange>) = range.intersects(property.range)
    override fun compareTo(other: AnyProperty): Int {
        require(other is DateProperty && other.name == name) { "Cannot compare properties of different type" }
        return range.compareTo(other.range)
    }
}

class IntProperty(override val name: String, override val range: IntRange) :
    Property<Int, IntRange>(name, range, PropertyType.INT) {
    constructor(name: String, value: Int) : this(name, IntRange.list(value))

    override fun toString() = identifier(name) + " " + range
    override fun intersects(property: Property<Int, IntRange>) = range.intersects(property.range)
    override fun compareTo(other: AnyProperty): Int {
        require(other is IntProperty && other.name == name) { "Cannot compare properties of different type" }
        return range.compareTo(other.range)
    }
}

class EnumProperty(override val name: String, override val range: EnumRange) :
    Property<String, EnumRange>(name, range, PropertyType.ENUM) {
    constructor(name: String, value: String) : this(name, EnumRange.list(value))

    override fun toString() = identifier(name) + " " + range
    override fun intersects(property: Property<String, EnumRange>) = range.intersects(property.range)
    override fun compareTo(other: AnyProperty): Int {
        require(other is EnumProperty && other.name == name) { "Cannot compare properties of different type" }
        return range.compareTo(other.range)
    }
}

internal fun compileProperties(propsMap: Map<String, AnyPrlProperty>) =
    propsMap.mapValues { (_, value) -> Property.fromPrlProperty(value) }

internal fun compileProperties(props: List<AnyPrlProperty>) = props.map { Property.fromPrlProperty(it) }
internal fun compilePropertiesToMap(props: List<AnyPrlProperty>) =
    props.associate { Pair(it.name, Property.fromPrlProperty(it)) }
