// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.parser.PragmaticRuleLanguage.quote
import java.time.LocalDate
import java.util.Objects
import java.util.SortedSet
import java.util.TreeSet
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min

sealed interface PropertyRange<T> : Comparable<PropertyRange<T>> {
    fun isDiscrete(): Boolean
    fun isContinuous(): Boolean
    fun contains(search: T): Boolean
    fun first(): T
    fun last(): T
    fun isEmpty(): Boolean
    fun allValues(): SortedSet<T>
    fun intersection(other: PropertyRange<T>): PropertyRange<T>
    fun intersects(other: PropertyRange<T>): Boolean
    fun disjoint(other: PropertyRange<T>) = !intersects(other);
}

sealed class EmptyRange<T> : PropertyRange<T> {
    override fun isDiscrete() = true
    override fun isContinuous() = true
    override fun contains(search: T) = false
    override fun first() = throw NoSuchElementException("Cannot get element from empty range")
    override fun last() = throw NoSuchElementException("Cannot get element from empty range")
    override fun isEmpty() = true
    override fun allValues() = sortedSetOf<T>()
    override fun intersects(other: PropertyRange<T>) = false
}

sealed interface BooleanRange : PropertyRange<Boolean> {
    companion object {
        fun empty(): BooleanRange = EmptyBooleanRange
        fun list(values: Collection<Boolean>): BooleanRange =
            if (values.isEmpty()) EmptyBooleanRange else BooleanList(values.toSortedSet())

        fun list(vararg values: Boolean): BooleanRange =
            if (values.isEmpty()) EmptyBooleanRange else BooleanList(values.toSortedSet())
    }
}

object EmptyBooleanRange : EmptyRange<Boolean>(), BooleanRange {
    override fun intersection(other: PropertyRange<Boolean>) = EmptyBooleanRange
    override fun compareTo(other: PropertyRange<Boolean>): Int = if (other is EmptyBooleanRange) 0 else -1
    override fun toString() = "EmptyBooleanRange"
}

class BooleanList internal constructor(private val values: SortedSet<Boolean>) : BooleanRange {
    override fun isDiscrete() = true
    override fun isContinuous() = false
    override fun contains(search: Boolean) = values.contains(search)
    override fun first(): Boolean = values.first()
    override fun last(): Boolean = values.last()
    override fun isEmpty() = values.isEmpty()
    override fun allValues() = values
    override fun intersection(other: PropertyRange<Boolean>) = if (other is EmptyBooleanRange) {
        EmptyBooleanRange
    } else {
        BooleanRange.list(values.intersect(other.allValues()))
    }

    override fun intersects(other: PropertyRange<Boolean>) =
        other !is EmptyBooleanRange && values.any { other.contains(it) }

    override fun toString() = if (values.size == 1) values.first().toString() else values.toString()
    override fun hashCode() = values.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return values == (other as BooleanList).values
    }

    override fun compareTo(other: PropertyRange<Boolean>) =
        if (other is EmptyBooleanRange) 1 else compareLexicographic(values.toList(), other.allValues().toList())
}

sealed interface IntRange : PropertyRange<Int> {
    companion object {
        fun empty(): IntRange = EmptyIntRange
        fun list(values: Collection<Int>): IntRange =
            if (values.isEmpty()) EmptyIntRange else IntList(values.toSortedSet())

        fun list(vararg values: Int): IntRange = if (values.isEmpty()) EmptyIntRange else IntList(values.toSortedSet())
        fun interval(start: Int, end: Int): IntRange = if (end < start) EmptyIntRange else IntInterval(start, end)
    }
}

object EmptyIntRange : EmptyRange<Int>(), IntRange {
    override fun intersection(other: PropertyRange<Int>) = EmptyIntRange
    override fun compareTo(other: PropertyRange<Int>): Int = if (other is EmptyIntRange) 0 else -1
    override fun toString() = "EmptyIntRange"
}

class IntInterval internal constructor(private val start: Int, private val end: Int) : IntRange {
    override fun isDiscrete() = false
    override fun isContinuous() = true
    override fun contains(search: Int) = search in start..end
    override fun first() = start
    override fun last() = end
    override fun isEmpty() = end < start
    override fun allValues() = (start..end).toSortedSet()

    override fun intersection(other: PropertyRange<Int>) = when (other) {
        is EmptyIntRange -> EmptyIntRange
        is IntInterval -> IntRange.interval(max(start, other.start), min(end, other.end))
        else -> IntRange.list(other.allValues().filter { contains(it) })
    }

    override fun intersects(other: PropertyRange<Int>) = when (other) {
        is EmptyIntRange -> false
        is IntInterval -> start <= other.end && end >= other.start
        else -> other.allValues().any { contains(it) }
    }

    override fun toString() = "[$start - $end]"
    override fun hashCode() = 31 * start + end
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IntInterval
        if (start != other.start) return false
        return end == other.end
    }

    override fun compareTo(other: PropertyRange<Int>) = when (other) {
        EmptyIntRange -> 1
        is IntInterval -> compareLexicographic(listOf(start, end), listOf(other.start, other.end))
        is IntList -> -1
        else -> error("can never happen")
    }
}

class IntList internal constructor(private val values: SortedSet<Int>) : IntRange {
    override fun isDiscrete() = true
    override fun isContinuous() = false
    override fun contains(search: Int) = values.contains(search)
    override fun first(): Int = values.first()
    override fun last(): Int = values.last()
    override fun isEmpty() = values.isEmpty()
    override fun allValues() = values
    override fun intersection(other: PropertyRange<Int>) =
        if (other is EmptyIntRange) EmptyIntRange else IntRange.list(values.filter { other.contains(it) })

    override fun intersects(other: PropertyRange<Int>) = when (other) {
        is EmptyIntRange -> false
        is IntInterval -> values.any { other.contains(it) }
        else -> other.allValues().any { contains(it) }
    }

    override fun toString() = if (values.size == 1) values.first().toString() else values.toString()
    override fun hashCode() = values.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return values == (other as IntList).values
    }

    override fun compareTo(other: PropertyRange<Int>) = when (other) {
        EmptyIntRange -> 1
        is IntInterval -> 1
        is IntList -> compareLexicographic(values.toList(), other.values.toList())
        else -> error("can never happen")
    }
}

sealed interface DateRange : PropertyRange<LocalDate> {
    companion object {
        fun empty(): DateRange = EmptyDateRange
        fun interval(start: LocalDate, end: LocalDate): DateRange =
            if (end < start) EmptyDateRange else DateInterval(start, end)

        fun list(values: Collection<LocalDate>): DateRange =
            if (values.isEmpty()) EmptyDateRange else DateList(values.toSortedSet())

        fun list(vararg values: LocalDate): DateRange =
            if (values.isEmpty()) EmptyDateRange else DateList(values.toSortedSet())
    }
}

object EmptyDateRange : EmptyRange<LocalDate>(), DateRange {
    override fun intersection(other: PropertyRange<LocalDate>) = EmptyDateRange
    override fun compareTo(other: PropertyRange<LocalDate>): Int = if (other is EmptyDateRange) 0 else -1
    override fun toString() = "EmptyDateRange"
}

class DateInterval internal constructor(private val start: LocalDate, private val end: LocalDate) : DateRange {
    override fun isDiscrete() = false
    override fun isContinuous() = true
    override fun contains(search: LocalDate) = search in start..end
    override fun first() = start
    override fun last() = end
    override fun isEmpty() = start > end
    override fun allValues(): SortedSet<LocalDate> =
        start.datesUntil(end.plusDays(1)).collect(Collectors.toCollection(::TreeSet))

    override fun intersection(other: PropertyRange<LocalDate>) = when (other) {
        is EmptyDateRange -> EmptyDateRange
        is DateInterval -> DateRange.interval(start.coerceAtLeast(other.start), end.coerceAtMost(other.end))
        else -> DateRange.list(other.allValues().filter { contains(it) })
    }

    override fun intersects(other: PropertyRange<LocalDate>) = when (other) {
        is EmptyDateRange -> false
        is DateInterval -> start <= other.end && end >= other.start
        else -> other.allValues().any { contains(it) }
    }

    override fun toString() = "[$start - $end]"
    override fun hashCode() = Objects.hash(start, end)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DateInterval
        if (start != other.start) return false
        return end == other.end
    }


    override fun compareTo(other: PropertyRange<LocalDate>) = when (other) {
        EmptyDateRange -> 1
        is DateInterval -> compareLexicographic(listOf(start, end), listOf(other.start, other.end))
        is DateList -> -1
        else -> error("can never happen")
    }
}

class DateList internal constructor(private val values: SortedSet<LocalDate>) : DateRange {
    override fun isDiscrete() = true
    override fun isContinuous() = false
    override fun contains(search: LocalDate) = values.contains(search)
    override fun first(): LocalDate = values.first()
    override fun last(): LocalDate = values.last()
    override fun isEmpty() = values.isEmpty()
    override fun allValues() = values

    override fun intersection(other: PropertyRange<LocalDate>) = if (other is EmptyDateRange) {
        EmptyDateRange
    } else {
        DateRange.list(values.filter { other.contains(it) })
    }

    override fun intersects(other: PropertyRange<LocalDate>) = when (other) {
        is EmptyDateRange -> false
        is DateInterval -> values.any { other.contains(it) }
        else -> other.allValues().any { contains(it) }
    }

    override fun toString() = if (values.size == 1) values.first().toString() else values.toString()
    override fun hashCode() = values.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return values == (other as DateList).values
    }

    override fun compareTo(other: PropertyRange<LocalDate>) = when (other) {
        EmptyDateRange -> 1
        is DateInterval -> 1
        is DateList -> compareLexicographic(values.toList(), other.values.toList())
        else -> error("can never happen")
    }
}

sealed interface EnumRange : PropertyRange<String> {
    companion object {
        fun empty(): EnumRange = EmptyEnumRange
        fun list(values: Collection<String>): EnumRange =
            if (values.isEmpty()) EmptyEnumRange else EnumList(values.toSortedSet())

        fun list(vararg values: String): EnumRange =
            if (values.isEmpty()) EmptyEnumRange else EnumList(values.toSortedSet())
    }
}

object EmptyEnumRange : EmptyRange<String>(), EnumRange {
    override fun intersection(other: PropertyRange<String>) = EmptyEnumRange
    override fun compareTo(other: PropertyRange<String>): Int = if (other is EmptyEnumRange) 0 else -1
    override fun toString() = "EmptyEnumRange"
}

class EnumList internal constructor(private val values: SortedSet<String>) : EnumRange {
    override fun isDiscrete() = true
    override fun isContinuous() = false
    override fun contains(search: String) = values.contains(search)
    override fun first(): String = values.first()
    override fun last(): String = values.last()
    override fun isEmpty() = values.isEmpty()
    override fun allValues() = values
    override fun intersection(other: PropertyRange<String>) = if (other is EmptyEnumRange) {
        EmptyEnumRange
    } else {
        EnumRange.list(values.filter { other.contains(it) })
    }

    override fun intersects(other: PropertyRange<String>) =
        if (other is EmptyEnumRange) false else other.allValues().any { contains(it) }

    override fun toString() = if (values.size == 1) quote(values.first()) else quote(values)
    override fun hashCode() = values.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return values == (other as EnumList).values
    }

    override fun compareTo(other: PropertyRange<String>) =
        if (other is EmptyEnumRange) 1 else compareLexicographic(values.toList(), other.allValues().toList())
}

fun <E : Comparable<E>> compareLexicographic(set1: List<E>, set2: List<E>): Int {
    val size = minOf(set1.size, set2.size)
    for (index in 0 until size) {
        val comparison = set1[index].compareTo(set2[index])
        if (comparison != 0) return comparison
    }
    return set1.size.compareTo(set2.size)
}
