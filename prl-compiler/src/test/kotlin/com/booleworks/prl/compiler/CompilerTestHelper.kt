package com.booleworks.prl.compiler

import com.booleworks.prl.model.BooleanFeatureDefinition
import com.booleworks.prl.model.EnumFeatureDefinition
import com.booleworks.prl.model.IntFeatureDefinition
import com.booleworks.prl.model.IntRange
import com.booleworks.prl.model.Theory
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.parser.PrlFeature

val b1Definition = BooleanFeatureDefinition("b1")
val b2Definition = BooleanFeatureDefinition("b2")
val b3Definition = BooleanFeatureDefinition("b3")
val vDefinition = BooleanFeatureDefinition("v", true)
val e1Definition = EnumFeatureDefinition("e1", setOf("a"))
val e2Definition = EnumFeatureDefinition("e2", setOf("a"))
val i1Definition = IntFeatureDefinition("i1", IntRange.list(0))
val i2Definition = IntFeatureDefinition("i2", IntRange.list(0))
val i3Definition = IntFeatureDefinition("i3", IntRange.list(0))

val b1 = b1Definition.feature
val b2 = b2Definition.feature
val b3 = b3Definition.feature
val e1 = e1Definition.feature
val e2 = e2Definition.feature
val i1 = i1Definition.feature
val i2 = i2Definition.feature
val i3 = i3Definition.feature
val v = vDefinition.feature as VersionedBooleanFeature

val theoryMap: Map<PrlFeature, Theory> = mapOf(
    Pair(PrlFeature("b1"), Theory.BOOL),
    Pair(PrlFeature("b2"), Theory.BOOL),
    Pair(PrlFeature("b3"), Theory.BOOL),
    Pair(PrlFeature("v"), Theory.VERSIONED_BOOL),
    Pair(PrlFeature("e1"), Theory.ENUM),
    Pair(PrlFeature("e2"), Theory.ENUM),
    Pair(PrlFeature("i1"), Theory.INT),
    Pair(PrlFeature("i2"), Theory.INT),
    Pair(PrlFeature("i3"), Theory.INT),
)
