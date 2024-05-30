// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_BOOL
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_DATE
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_ENUM
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_INT
import com.booleworks.prl.parser.PrlSlicingBooleanPropertyDefinition
import com.booleworks.prl.parser.PrlSlicingDatePropertyDefinition
import com.booleworks.prl.parser.PrlSlicingEnumPropertyDefinition
import com.booleworks.prl.parser.PrlSlicingIntPropertyDefinition
import com.booleworks.prl.parser.PrlSlicingPropertyDefinition
import java.time.LocalDate
import java.util.Objects
import java.util.SortedSet

typealias AnySlicingPropertyDefinition = SlicingPropertyDefinition<*, *>

sealed class SlicingPropertyDefinition<E, R : PropertyRange<E>>(
    open val name: String,
    val propertyType: PropertyType,
    open val lineNumber: Int? = null
) {
    abstract fun appendString(appendable: Appendable): Appendable
    abstract fun addRange(range: R): AddRangeResult
    abstract fun computeRelevantValues(filter: R): SortedSet<E>
    abstract fun computeRelevantValues(): SortedSet<E>

    override fun toString(): String = StringBuilder().apply { appendString(this) }.toString()

    companion object {
        fun from(def: PrlSlicingPropertyDefinition): AnySlicingPropertyDefinition {
            return when (def) {
                is PrlSlicingBooleanPropertyDefinition -> SlicingBooleanPropertyDefinition(def.name, def.lineNumber)
                is PrlSlicingIntPropertyDefinition -> SlicingIntPropertyDefinition(def.name, def.lineNumber)
                is PrlSlicingDatePropertyDefinition -> SlicingDatePropertyDefinition(def.name, def.lineNumber)
                is PrlSlicingEnumPropertyDefinition -> SlicingEnumPropertyDefinition(def.name, def.lineNumber)
            }
        }
    }
}

data class SlicingBooleanPropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    SlicingPropertyDefinition<Boolean, BooleanRange>(name, PropertyType.BOOL, lineNumber) {

    val values: SortedSet<Boolean> = sortedSetOf()

    override fun appendString(appendable: Appendable) =
        appendable.apply { append(KEYWORD_BOOL).append(" ").append(name) }

    override fun addRange(range: BooleanRange) = AddRangeResult.guaranteedSucces { values.addAll(range.allValues()) }
    override fun computeRelevantValues(filter: BooleanRange) =
        BooleanRange.list(computeRelevantValues()).intersection(filter).allValues()

    override fun computeRelevantValues() = if (values.isEmpty()) values else sortedSetOf(false, true)

    override fun hashCode() = Objects.hash(name, values)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SlicingBooleanPropertyDefinition
        if (name != other.name) return false
        return values == other.values
    }
}

data class SlicingIntPropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    SlicingPropertyDefinition<Int, IntRange>(name, PropertyType.INT, lineNumber) {

    val startValues: SortedSet<Int> = sortedSetOf()
    val endValues: SortedSet<Int> = sortedSetOf()
    val singleValues: SortedSet<Int> = sortedSetOf()

    override fun appendString(appendable: Appendable) =
        appendable.apply { append(KEYWORD_INT).append(" ").append(name) }

    override fun addRange(range: IntRange) = AddRangeResult.guaranteedSucces {
        if (range is IntList) {
            singleValues.addAll(range.allValues())
        } else {
            startValues.add(range.first())
            endValues.add(range.last())
        }
    }

    override fun computeRelevantValues(filter: IntRange) =
        IntRange.list(computeRelevantValues()).intersection(filter).allValues().toSortedSet()

    override fun computeRelevantValues(): SortedSet<Int> {
        val max = max()
        return if (max == null) {
            sortedSetOf()
        } else {
            (startValues + singleValues + singleValues.map { it + 1 }.filter { it < max } +
                    endValues.map { it + 1 }.filter { it < max } + max).toSortedSet()
        }
    }

    fun min(): Int? = (startValues + endValues + singleValues).minOrNull()
    fun max(): Int? = (startValues + endValues + singleValues).maxOrNull()

    override fun hashCode() = Objects.hash(name, startValues, endValues, singleValues)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SlicingIntPropertyDefinition
        if (name != other.name) return false
        if (startValues != other.startValues) return false
        if (endValues != other.endValues) return false
        return singleValues == other.singleValues
    }
}

data class SlicingDatePropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    SlicingPropertyDefinition<LocalDate, DateRange>(name, PropertyType.DATE, lineNumber) {

    val startValues: SortedSet<LocalDate> = sortedSetOf()
    val endValues: SortedSet<LocalDate> = sortedSetOf()
    val singleValues: SortedSet<LocalDate> = sortedSetOf()

    override fun appendString(appendable: Appendable) =
        appendable.apply { append(KEYWORD_DATE).append(" ").append(name) }

    override fun addRange(range: DateRange) = AddRangeResult.guaranteedSucces {
        if (range is DateList) {
            singleValues.addAll(range.allValues())
        } else {
            startValues.add(range.first())
            endValues.add(range.last())
        }
    }

    override fun computeRelevantValues(filter: DateRange) =
        DateRange.list(computeRelevantValues()).intersection(filter).allValues().toSortedSet()

    override fun computeRelevantValues(): SortedSet<LocalDate> {
        val max = max()
        return if (max == null) {
            return sortedSetOf()
        } else {
            (startValues + singleValues + singleValues.map { it.plusDays(1) }
                .filter { it < max } + endValues.map { it.plusDays(1) }
                .filter { it < max } + max).toSortedSet()
        }
    }

    fun min(): LocalDate? = (startValues + endValues + singleValues).minOrNull()
    fun max(): LocalDate? = (startValues + endValues + singleValues).maxOrNull()

    override fun hashCode() = Objects.hash(name, startValues, endValues, singleValues)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SlicingDatePropertyDefinition
        if (name != other.name) return false
        if (startValues != other.startValues) return false
        if (endValues != other.endValues) return false
        return singleValues == other.singleValues
    }
}

data class SlicingEnumPropertyDefinition(override val name: String, override val lineNumber: Int? = null) :
    SlicingPropertyDefinition<String, EnumRange>(name, PropertyType.ENUM, lineNumber) {

    val values: SortedSet<String> = sortedSetOf()

    override fun appendString(appendable: Appendable) =
        appendable.apply { append(KEYWORD_ENUM).append(" ").append(name) }

    override fun addRange(range: EnumRange) = AddRangeResult.guaranteedSucces { values.addAll(range.allValues()) }

    override fun computeRelevantValues(filter: EnumRange) =
        EnumRange.list(computeRelevantValues()).intersection(filter).allValues().toSortedSet()

    override fun computeRelevantValues() = values

    override fun hashCode() = Objects.hash(name, values)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SlicingEnumPropertyDefinition
        if (name != other.name) return false
        return values == other.values
    }
}

data class AddRangeResult internal constructor(val success: Boolean, val message: String?) {
    companion object {
        private val SUCCESS = AddRangeResult(true, null)

        fun guaranteedSucces(block: () -> Unit) = SUCCESS.also { block() }
        fun error(message: String) = AddRangeResult(false, message)
    }
}
