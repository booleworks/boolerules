package com.booleworks.prl.model.constraints

import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.IntRange

fun boolFt(featureCode: String) = BooleanFeatureDefinition(featureCode, false).feature
fun enumFt(featureCode: String) = EnumFeatureDefinition(featureCode, setOf()).feature
fun enumFt(featureCode: String, values: Collection<String>): EnumFeature =
    EnumFeatureDefinition(featureCode, values.toSet()).feature

fun intFt(featureCode: String) =
    IntFeatureDefinition(featureCode, IntRange.interval(Int.MIN_VALUE, Int.MAX_VALUE)).feature

fun versionFt(featureCode: String) = BooleanFeatureDefinition(featureCode, true).feature as VersionedBooleanFeature

fun ftMap(vararg constraints: Constraint): Pair<Map<Feature, Int>, Map<Int, Feature>> {
    val map1 = mutableMapOf<Feature, Int>()
    val map2 = mutableMapOf<Int, Feature>()
    constraints.forEach { constraint ->
        constraint.features().forEach {
            if (!map1.containsKey(it)) {
                map1[it] = map1.size
                map2[map2.size] = it
            }
        }
    }
    return Pair(map1, map2)
}
