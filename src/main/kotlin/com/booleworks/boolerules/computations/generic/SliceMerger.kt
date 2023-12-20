// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.boolerules.rulefile.SlicingPropertyDO
import com.booleworks.prl.model.slices.Slice
import java.time.LocalDate

private data class SliceWithDetails<DETAIL : ComputationDetail>(
    val slice: SliceDO,
    val details: List<SplitComputationDetail<DETAIL>>
)

internal data class MergeResult<MAIN, DETAIL : ComputationDetail>(
    val merge: List<SliceComputationResult<MAIN>>,
    val detailMap: Map<Int, List<SplitComputationDetail<DETAIL>>>
)

internal fun <REQUEST : ComputationRequest, MAIN, DETAIL : ComputationDetail> mergeMainResults(
    request: REQUEST,
    result: Map<Slice, InternalResult<MAIN, DETAIL>>
): MergeResult<MAIN, DETAIL> {
    val mainMap: Map<MAIN, MutableList<SliceWithDetails<DETAIL>>> = mkMainMap(request, result)
    val splitProperties = request.sliceSelection.filter { it.sliceType == SliceTypeDO.SPLIT }
    val sortedContinuousPropertyValues: Map<String, PropertyRangeDO> =
        collectContinuousPropertyValues(result.keys, request.sliceSelection)
    var id = 1
    val merge = mutableListOf<SliceComputationResult<MAIN>>()
    val detailMap = mutableMapOf<Int, List<SplitComputationDetail<DETAIL>>>()
    mainMap.forEach { (main, splitResult) ->
        val res = mergeResults(splitResult, splitProperties, sortedContinuousPropertyValues)
        merge.add(SliceComputationResult(id, main, res.map { it.slice }))
        detailMap[id] = res.flatMap { it.details }
        id++
    }
    return MergeResult(merge, detailMap)
}

private fun <REQUEST : ComputationRequest, MAIN, DETAIL : ComputationDetail> mkMainMap(
    request: REQUEST,
    result: Map<Slice, InternalResult<MAIN, DETAIL>>
): MutableMap<MAIN, MutableList<SliceWithDetails<DETAIL>>> {
    val mainMap = mutableMapOf<MAIN, MutableList<SliceWithDetails<DETAIL>>>()
    val isEmptyDetail = result.any { (_, internalResult) -> internalResult.extractDetails() is NoComputationDetail }
    for ((slice, internalResult) in result) {
        val details = if (!isEmptyDetail) {
            listOf(SplitComputationDetail(internalResult, request.splitProperties()))
        } else {
            listOf()
        }
        mainMap.computeIfAbsent(internalResult.extractMainResult()) { mutableListOf() }
            .add(SliceWithDetails(slice.toDO(), details))
    }
    return mainMap
}

private fun collectContinuousPropertyValues(
    slices: Set<Slice>,
    sliceSelection: List<PropertySelectionDO>
): Map<String, PropertyRangeDO> =
    sliceSelection
        .filter { it.sliceType == SliceTypeDO.SPLIT && it.range.isContinuous() }
        .associate { propertySelection ->
            propertySelection.property to
                    slices.map { slice -> slice.toDO().content.first { it.name == propertySelection.property } }
        }
        .mapValues { mergeToDiscreteProperties(it.value).range }

private fun <DETAIL : ComputationDetail> mergeResults(
    splitResults: List<SliceWithDetails<DETAIL>>,
    splitProperties: List<PropertySelectionDO>,
    sortedContinuousPropertyValues: Map<String, PropertyRangeDO>
): List<SliceWithDetails<DETAIL>> {
    var currentMerge: List<SliceWithDetails<DETAIL>> = splitResults
    for (splitProperty in splitProperties) {
        val groupedProperties: MutableMap<
                Set<SlicingPropertyDO>,
                MutableList<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>>> = mutableMapOf()
        for (splitResult in currentMerge) {
            val key: Set<SlicingPropertyDO> = splitResult.slice.content
                .filterNot { it.name == splitProperty.property }.toSet()
            groupedProperties.computeIfAbsent(key) { mutableListOf() }
                .add(Pair(splitResult.slice.content.first { it.name == splitProperty.property }, splitResult.details))
        }
        val newMerge: MutableList<SliceWithDetails<DETAIL>> = mutableListOf()
        for ((keyProperties, propertiesToMerge) in groupedProperties) {
            val mergedProperties: List<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>> =
                mergeProperties(
                    propertiesToMerge, splitProperty.range.isContinuous(),
                    sortedContinuousPropertyValues[splitProperty.property]
                )
            for (mergedProperty in mergedProperties) {
                newMerge.add(SliceWithDetails(SliceDO(keyProperties.toList() + mergedProperty.first), mergedProperty.second))
            }
        }
        currentMerge = newMerge
    }
    return currentMerge
}

private fun <DETAIL : ComputationDetail> mergeProperties(
    propertiesToMerge: List<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>>,
    isContinuous: Boolean,
    rangeOfAllValuesOfContinuousProperty: PropertyRangeDO?
): List<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>> {
    return if (isContinuous) {
        mergeToContinuousProperties(propertiesToMerge, rangeOfAllValuesOfContinuousProperty!!)
    } else {
        listOf(mergeToDiscreteProperties(propertiesToMerge.map { it.first }) to propertiesToMerge.flatMap { it.second })
    }
}

private fun mergeToDiscreteProperties(propertiesToMerge: List<SlicingPropertyDO>): SlicingPropertyDO =
    propertiesToMerge.reduce { v1, v2 -> combineDiscrete(v1, v2) }

private fun combineDiscrete(v1: SlicingPropertyDO, v2: SlicingPropertyDO): SlicingPropertyDO = when {
    v1.range.booleanValues != null && v2.range.booleanValues != null ->
        SlicingPropertyDO(
            v1.name, v1.type,
            PropertyRangeDO(v1.range.booleanValues as Set<Boolean> + v2.range.booleanValues as Set<Boolean>)
        )

    v1.range.intValues != null && v2.range.intValues != null         ->
        SlicingPropertyDO(
            v1.name, v1.type,
            PropertyRangeDO(intValues = v1.range.intValues as Set<Int> + v2.range.intValues as Set<Int>)
        )

    v1.range.enumValues != null && v2.range.enumValues != null       ->
        SlicingPropertyDO(
            v1.name, v1.type,
            PropertyRangeDO(enumValues = v1.range.enumValues as Set<String> + v2.range.enumValues as Set<String>)
        )

    v1.range.dateValues != null && v2.range.dateValues != null       ->
        SlicingPropertyDO(v1.name, v1.type, PropertyRangeDO(dateValues = v1.range.dateValues as Set<LocalDate> + v2.range.dateValues as Set<LocalDate>))

    else                                                             -> error("Unexpected property ranges")
}

private fun <DETAIL : ComputationDetail> mergeToContinuousProperties(
    propertiesToMerge: List<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>>,
    rangeOfAllValuesOfContinuousProperty: PropertyRangeDO
): List<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>> {
    val rangeToSplitDetails = propertiesToMerge.associate { it.first.range to it.second }
    val propertyName = propertiesToMerge[0].first.name
    val propertyType = propertiesToMerge[0].first.type
    return when {
        propertiesToMerge.all { it.first.range.intValues != null } -> mergeIntProperties(
            rangeToSplitDetails.mapKeys { it.key.intValues!!.first() },
            rangeOfAllValuesOfContinuousProperty.intValues!!,
            propertyName,
            propertyType,
            { it - 1 },
            { min, max -> PropertyRangeDO(intMin = min, intMax = max) }
        )

        propertiesToMerge.all { it.first.range.dateValues != null } -> mergeIntProperties(
            rangeToSplitDetails.mapKeys { it.key.dateValues!!.first() },
            rangeOfAllValuesOfContinuousProperty.dateValues!!,
            propertyName,
            propertyType,
            { it.minusDays(1) },
            { min, max -> PropertyRangeDO(dateMin = min, dateMax = max) }
        )

        else -> error("Unexpected property ranges")
    }
}

private fun <V : Comparable<V>, DETAIL : ComputationDetail> mergeIntProperties(
    valuesToSplitDetails: Map<V, List<SplitComputationDetail<DETAIL>>>,
    allOccurringValues: Set<V>,
    propertyName: String,
    propertyType: PropertyTypeDO,
    previousValue: (V) -> V,
    propertyRangeGenerator: (V, V) -> PropertyRangeDO
): List<Pair<SlicingPropertyDO, List<SplitComputationDetail<DETAIL>>>> {
    val allOccurringValuesSorted = allOccurringValues.toSortedSet().toList()
    val valueToIndex = allOccurringValuesSorted.mapIndexed { i, value -> value to i }.toMap()

    val windows = mutableListOf<Triple<V, V, List<SplitComputationDetail<DETAIL>>>>()
    val currentDetails: MutableList<SplitComputationDetail<DETAIL>> = mutableListOf()
    var rangeStart: V? = null
    var previousIndex: Int? = null
    for ((value, splitDetails) in valuesToSplitDetails.toSortedMap()) {
        val index = valueToIndex[value]!!
        if (previousIndex == null) {
            rangeStart = value
            currentDetails.addAll(splitDetails)
        } else if (index == previousIndex + 1) {
            currentDetails.addAll(splitDetails)
        } else {
            val rangeEnd = previousValue(allOccurringValuesSorted[previousIndex + 1])
            windows.add(Triple(rangeStart!!, rangeEnd, currentDetails))
            currentDetails.clear()
            rangeStart = value
        }
        previousIndex = index
    }
    val rangeEnd = if (previousIndex!! == valueToIndex.values.last()) {
        allOccurringValuesSorted[previousIndex]
    } else {
        previousValue(allOccurringValuesSorted[previousIndex + 1])
    }
    windows.add(Triple(rangeStart!!, rangeEnd, currentDetails))
    return windows.map {
        SlicingPropertyDO(propertyName, propertyType, propertyRangeGenerator(it.first, it.second)) to it.third
    }
}
