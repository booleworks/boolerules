// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.compiler.FeatureStore
import com.booleworks.prl.compiler.PropertyStore
import com.booleworks.prl.model.constraints.Amo
import com.booleworks.prl.model.constraints.And
import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.constraints.Constant
import com.booleworks.prl.model.constraints.Constraint
import com.booleworks.prl.model.constraints.EnumComparisonPredicate
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.EnumInPredicate
import com.booleworks.prl.model.constraints.EnumPredicate
import com.booleworks.prl.model.constraints.EnumValue
import com.booleworks.prl.model.constraints.Equivalence
import com.booleworks.prl.model.constraints.Exo
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.Implication
import com.booleworks.prl.model.constraints.IntComparisonPredicate
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.IntInPredicate
import com.booleworks.prl.model.constraints.IntMul
import com.booleworks.prl.model.constraints.IntSum
import com.booleworks.prl.model.constraints.IntTerm
import com.booleworks.prl.model.constraints.IntValue
import com.booleworks.prl.model.constraints.Not
import com.booleworks.prl.model.constraints.Or
import com.booleworks.prl.model.constraints.Predicate
import com.booleworks.prl.model.constraints.VersionPredicate
import com.booleworks.prl.model.constraints.VersionedBooleanFeature
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraint
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.AMO
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.AND
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.EQUIV
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.EXO
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.IMPL
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.NOT
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.OR
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbConstraintType.UNRECOGNIZED
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbEnumPredicate
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntPredicate
import com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm
import com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition
import com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinitionList
import com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureStore
import com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbTheory
import com.booleworks.prl.model.protobuf.ProtoBufModel.PbHeader
import com.booleworks.prl.model.protobuf.ProtoBufModel.PbModel
import com.booleworks.prl.model.protobuf.ProtoBufPrimitives
import com.booleworks.prl.model.protobuf.ProtoBufProperties
import com.booleworks.prl.model.protobuf.ProtoBufProperties.PbPropertyStore
import com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingBooleanPropertyDefinition
import com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingDatePropertyDefinition
import com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingEnumPropertyDefinition
import com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition
import com.booleworks.prl.model.protobuf.ProtoBufRules.PbRule
import com.booleworks.prl.model.protobuf.pbBooleanRange
import com.booleworks.prl.model.protobuf.pbConstraint
import com.booleworks.prl.model.protobuf.pbDateRange
import com.booleworks.prl.model.protobuf.pbEnumPredicate
import com.booleworks.prl.model.protobuf.pbEnumRange
import com.booleworks.prl.model.protobuf.pbFeature
import com.booleworks.prl.model.protobuf.pbFeatureDefinition
import com.booleworks.prl.model.protobuf.pbFeatureDefinitionList
import com.booleworks.prl.model.protobuf.pbFeatureStore
import com.booleworks.prl.model.protobuf.pbFullFeature
import com.booleworks.prl.model.protobuf.pbHeader
import com.booleworks.prl.model.protobuf.pbIntMul
import com.booleworks.prl.model.protobuf.pbIntPredicate
import com.booleworks.prl.model.protobuf.pbIntRange
import com.booleworks.prl.model.protobuf.pbIntTerm
import com.booleworks.prl.model.protobuf.pbModel
import com.booleworks.prl.model.protobuf.pbProperty
import com.booleworks.prl.model.protobuf.pbPropertyStore
import com.booleworks.prl.model.protobuf.pbRule
import com.booleworks.prl.model.protobuf.pbSlicingBooleanPropertyDefinition
import com.booleworks.prl.model.protobuf.pbSlicingDatePropertyDefinition
import com.booleworks.prl.model.protobuf.pbSlicingEnumPropertyDefinition
import com.booleworks.prl.model.protobuf.pbSlicingIntPropertyDefinition
import com.booleworks.prl.model.rules.AnyRule
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.model.rules.DefinitionRule
import com.booleworks.prl.model.rules.ExclusionRule
import com.booleworks.prl.model.rules.ForbiddenFeatureRule
import com.booleworks.prl.model.rules.GroupRule
import com.booleworks.prl.model.rules.GroupType
import com.booleworks.prl.model.rules.IfThenElseRule
import com.booleworks.prl.model.rules.InclusionRule
import com.booleworks.prl.model.rules.MandatoryFeatureRule
import com.booleworks.prl.parser.PrlVersion
import java.sql.Timestamp
import java.time.LocalDate

fun serialize(model: PrlModel) = pbModel {
    header = serialize(model.header)
    val featureMap = mutableMapOf<Feature, Int>()
    model.features().forEach {
        val featureId = featureMap.size
        feature.add(pbFullFeature {
            id = featureId
            featureCode = it.featureCode
            theory = when (it) {
                is VersionedBooleanFeature -> PbTheory.VERSIONED_BOOL
                is BooleanFeature -> PbTheory.BOOL
                is EnumFeature -> PbTheory.ENUM
                is IntFeature -> PbTheory.INT
            }
        })
        featureMap[it] = featureId
    }
    featureStore = serialize(model.featureStore, featureMap)
    propertyStore = serialize(model.propertyStore)
    rule += model.rules.map { serialize(it, featureMap) }
}

fun serialize(fs: FeatureStore, featureMap: Map<Feature, Int>) = pbFeatureStore {
    booleanFeatures.putAll(fs.booleanFeatures.map { it.key to serialize(it.value) }.toMap())
    intFeatures.putAll(fs.intFeatures.map { it.key to serialize(it.value) }.toMap())
    enumFeatures.putAll(fs.enumFeatures.map { it.key to serialize(it.value) }.toMap())
    group.addAll(fs.groups.map { pbFeature { id = featureMap[it]!! } })
    nonUniqueFeatures.addAll(fs.nonUniqueFeatures)
}

fun serialize(ps: PropertyStore) = pbPropertyStore {
    ps.slicingPropertyDefinitions.forEach {
        when (val def = it.value) {
            is SlicingBooleanPropertyDefinition -> booleanProperties += serialize(def)
            is SlicingDatePropertyDefinition -> dateProperties += serialize(def)
            is SlicingIntPropertyDefinition -> intProperties += serialize(def)
            is SlicingEnumPropertyDefinition -> enumProperties += serialize(def)
        }
    }
}

fun serialize(c: Constraint, fm: Map<Feature, Int>): PbConstraint = when (c) {
    is Constant -> pbConstraint { value = c.value }
    is BooleanFeature -> pbConstraint { boolFeature = fm[c]!! }
    is VersionedBooleanFeature -> pbConstraint { boolFeature = fm[c]!! }
    is Amo -> pbConstraint { type = AMO; operands += c.features.map { pbConstraint { boolFeature = fm[it]!! } } }
    is Exo -> pbConstraint { type = EXO; operands += c.features.map { pbConstraint { boolFeature = fm[it]!! } } }
    is Not -> pbConstraint { type = NOT; operands += serialize(c.operand, fm) }
    is Implication -> pbConstraint { type = IMPL; operands += listOf(serialize(c.left, fm), serialize(c.right, fm)) }
    is Equivalence -> pbConstraint { type = EQUIV; operands += listOf(serialize(c.left, fm), serialize(c.right, fm)) }
    is Or -> pbConstraint { type = OR; operands += c.operands.map { serialize(it, fm) } }
    is And -> pbConstraint { type = AND; operands += c.operands.map { serialize(it, fm) } }
    is VersionPredicate -> pbConstraint {
        intPredicate = pbIntPredicate { feature = fm[c.feature]!!; version = c.version; comp = comp(c.comparison) }
    }
    is IntInPredicate -> pbConstraint {
        intPredicate = pbIntPredicate { term1 = term(c.term, fm); range = range(c.range) }
    }
    is EnumInPredicate -> pbConstraint {
        enumPredicate = pbEnumPredicate { feature = fm[c.feature]!!; values += c.values }
    }
    is IntComparisonPredicate -> pbConstraint {
        intPredicate = pbIntPredicate { term1 = term(c.left, fm); term2 = term(c.right, fm); comp = comp(c.comparison) }
    }

    is EnumComparisonPredicate -> pbConstraint {
        enumPredicate = pbEnumPredicate { feature = fm[c.feature]!!; value = c.value.value; comp = comp(c.comparison) }
    }
}

fun serialize(r: AnyRule, fm: Map<Feature, Int>): PbRule = pbRule {
    id = r.id
    description = r.description
    properties += r.properties.values.map { serialize(it) }
    if (r.lineNumber != null) lineNumber = r.lineNumber!!
    when (r) {
        is ConstraintRule -> constraint = serialize(r.constraint, fm)
        is DefinitionRule -> {
            feature = fm[r.feature]!!
            constraint = serialize(r.definition, fm)
        }

        is ExclusionRule -> {
            ifPart = serialize(r.ifConstraint, fm)
            thenNotPart = serialize(r.thenNotConstraint, fm)
        }

        is ForbiddenFeatureRule -> {
            isForbidden = true
            feature = fm[r.feature]!!
            if (r.enumValue != null) enumValue = r.enumValue
            if (r.intValueOrVersion != null) intValueOrVersion = r.intValueOrVersion
        }

        is MandatoryFeatureRule -> {
            isForbidden = false
            feature = fm[r.feature]!!
            if (r.enumValue != null) enumValue = r.enumValue
            if (r.intValueOrVersion != null) intValueOrVersion = r.intValueOrVersion
        }

        is GroupRule -> {
            isAmo = r.type == GroupType.OPTIONAL
            feature = fm[r.group]!!
            groupFeatures += r.content.map { fm[it]!! }
        }

        is IfThenElseRule -> {
            ifPart = serialize(r.ifConstraint, fm)
            thenPart = serialize(r.thenConstraint, fm)
            elsePart = serialize(r.elseConstraint, fm)
        }

        is InclusionRule -> {
            ifPart = serialize(r.ifConstraint, fm)
            thenPart = serialize(r.thenConstraint, fm)
        }
    }
}

fun deserialize(bin: PbModel): PrlModel {
    val featureMap = mutableMapOf<Int, Feature>()
    bin.featureList.forEach {
        val featureCode = it.featureCode
        val feature: Feature = when (it.theory) {
            PbTheory.BOOL -> BooleanFeature(featureCode)
            PbTheory.VERSIONED_BOOL -> VersionedBooleanFeature(featureCode)
            PbTheory.ENUM -> EnumFeature(featureCode)
            else -> IntFeature(featureCode)
        }
        featureMap[it.id] = feature
    }
    return PrlModel(
        deserialize(bin.header),
        deserialize(bin.featureStore, featureMap),
        bin.ruleList.map { deserialize(it, featureMap) },
        deserialize(bin.propertyStore)
    )
}

fun deserialize(bin: PbPropertyStore) = PropertyStore().apply {
    bin.booleanPropertiesList.forEach { deserialize(it).let { def -> slicingPropertyDefinitions[def.name] = def } }
    bin.intPropertiesList.forEach { deserialize(it).let { def -> slicingPropertyDefinitions[def.name] = def } }
    bin.datePropertiesList.forEach { deserialize(it).let { def -> slicingPropertyDefinitions[def.name] = def } }
    bin.enumPropertiesList.forEach { deserialize(it).let { def -> slicingPropertyDefinitions[def.name] = def } }
}

fun deserialize(bin: PbFeatureStore, featureMap: Map<Int, Feature>) = FeatureStore(
    bin.booleanFeaturesMap.map { it.key to deserialize(it.value) }.toMap().toMutableMap(),
    bin.intFeaturesMap.map { it.key to deserialize(it.value) }.toMap().toMutableMap(),
    bin.enumFeaturesMap.map { it.key to deserialize(it.value) }.toMap().toMutableMap(),
    bin.groupList.map { featureMap[it.id] as BooleanFeature }.toMutableList(),
    bin.nonUniqueFeaturesList.toMutableSet()
)

fun deserialize(bin: PbConstraint, fm: Map<Int, Feature>): Constraint = when {
    bin.hasValue() -> Constant(bin.value)
    bin.hasBoolFeature() -> fm[bin.boolFeature]!! as BooleanFeature
    bin.hasEnumPredicate() -> deserialize(bin.enumPredicate, fm)
    bin.hasIntPredicate() -> deserialize(bin.intPredicate, fm)
    else -> when (bin.type!!) {
        AMO -> Amo(bin.operandsList.map { deserialize(it, fm) as BooleanFeature }.toSet())
        EXO -> Exo(bin.operandsList.map { deserialize(it, fm) as BooleanFeature }.toSet())
        NOT -> Not(deserialize(bin.operandsList[0], fm))
        IMPL -> Implication(deserialize(bin.operandsList[0], fm), deserialize(bin.operandsList[1], fm))
        EQUIV -> Equivalence(deserialize(bin.operandsList[0], fm), deserialize(bin.operandsList[1], fm))
        AND -> And(bin.operandsList.map { deserialize(it, fm) }.toSet())
        OR -> Or(bin.operandsList.map { deserialize(it, fm) }.toSet())
        UNRECOGNIZED -> throw IllegalArgumentException("Unknown constraint type")
    }
}

fun deserialize(bin: PbRule, fm: Map<Int, Feature>): AnyRule =
    (if (bin.hasLineNumber()) bin.lineNumber else null).let { ln ->
        val props = bin.propertiesList.map { deserialize(it) }.associateBy { it.name }
        val id = bin.id
        val desc = bin.description
        when {
            bin.hasElsePart() -> IfThenElseRule(
                deserialize(bin.ifPart, fm),
                deserialize(bin.thenPart, fm),
                deserialize(bin.elsePart, fm),
                id,
                desc,
                props,
                ln
            )

            bin.hasThenPart() -> InclusionRule(
                deserialize(bin.ifPart, fm),
                deserialize(bin.thenPart, fm),
                id,
                desc,
                props,
                ln
            )
            bin.hasThenNotPart() -> ExclusionRule(
                deserialize(bin.ifPart, fm),
                deserialize(bin.thenNotPart, fm),
                id,
                desc,
                props,
                ln
            )
            bin.hasIsForbidden() -> {
                val enumValue = if (bin.hasEnumValue()) bin.enumValue else null
                val intValue = if (bin.hasIntValueOrVersion()) bin.intValueOrVersion else null
                if (bin.isForbidden) {
                    ForbiddenFeatureRule(fm[bin.feature]!!, enumValue, intValue, id, desc, props, ln)
                } else {
                    MandatoryFeatureRule(fm[bin.feature]!!, enumValue, intValue, id, desc, props, ln)
                }
            }

            bin.hasIsAmo() -> {
                val type = if (bin.isAmo) GroupType.OPTIONAL else GroupType.MANDATORY
                GroupRule(
                    type,
                    fm[bin.feature]!! as BooleanFeature,
                    bin.groupFeaturesList.map { fm[it]!! as BooleanFeature },
                    id,
                    desc,
                    props,
                    ln
                )
            }

            bin.hasFeature() -> DefinitionRule(
                fm[bin.feature]!! as BooleanFeature,
                deserialize(bin.constraint, fm),
                id,
                desc,
                props,
                ln
            )
            else -> ConstraintRule(deserialize(bin.constraint, fm), id, desc, props, ln)
        }
    }

private fun serialize(h: PrlModelHeader) = pbHeader {
    major = h.version.major
    minor = h.version.minor
    properties += h.properties.values.map { serialize(it) }
}

private fun serialize(fd: AnyFeatureDef) = pbFeatureDefinition {
    code = fd.code
    description = fd.description
    properties += fd.properties.values.map { serialize(it) }
    if (fd.lineNumber != null) lineNumber = fd.lineNumber!!
    used = fd.used
    when (fd) {
        is BooleanFeatureDefinition -> versioned = fd.versioned
        is EnumFeatureDefinition -> enumValues += fd.values
        is IntFeatureDefinition -> intDomain = range(fd.domain)
    }
}

private fun serialize(l: List<AnyFeatureDef>): PbFeatureDefinitionList =
    pbFeatureDefinitionList { list.addAll(l.map { serialize(it) }) }

private fun term(term: IntTerm, fm: Map<Feature, Int>): PbIntTerm = when (term) {
    is IntFeature -> pbIntTerm { feature = fm[term]!! }
    is IntValue -> pbIntTerm { value = term.value }
    is IntMul -> pbIntTerm { mul = pbIntMul { coefficient = term.coefficient; feature = fm[term.feature]!! } }
    is IntSum -> pbIntTerm { muls += term.operands.map { term(it, fm) }; offset = term.offset }
}

private fun deserialize(bin: PbFeatureDefinition): AnyFeatureDef =
    (if (bin.hasLineNumber()) bin.lineNumber else null).let { ln ->
        val props = bin.propertiesList.map { deserialize(it) }.associateBy { it.name }
        when {
            bin.hasVersioned() -> BooleanFeatureDefinition(
                bin.code,
                bin.versioned,
                bin.description,
                props,
                ln,
            ).apply { used = bin.used }

            bin.hasIntDomain() -> IntFeatureDefinition(
                bin.code,
                deserialize(bin.intDomain),
                bin.description,
                props,
                ln
            ).apply { used = bin.used }

            else -> EnumFeatureDefinition(
                bin.code,
                bin.enumValuesList.toSet(),
                bin.description,
                props,
                ln
            ).apply { used = bin.used }
        }
    }

private fun deserialize(bin: PbFeatureDefinitionList): MutableList<AnyFeatureDef> =
    bin.listList.map { deserialize(it) }.toMutableList()

private fun deserialize(bin: PbEnumPredicate, fm: Map<Int, Feature>): EnumPredicate = if (bin.hasComp()) {
    EnumComparisonPredicate(fm[bin.feature]!! as EnumFeature, EnumValue(bin.value), deserialize(bin.comp))
} else {
    EnumInPredicate(fm[bin.feature]!! as EnumFeature, bin.valuesList.toSet())
}

private fun deserialize(bin: PbIntPredicate, fm: Map<Int, Feature>): Predicate = when {
    bin.hasVersion() -> VersionPredicate(
        fm[bin.feature]!! as VersionedBooleanFeature, deserialize(bin.comp), bin.version
    )
    bin.hasComp() -> IntComparisonPredicate(
        deserialize(bin.term1, fm),
        deserialize(bin.term2, fm),
        deserialize(bin.comp)
    )
    else -> IntInPredicate(deserialize(bin.term1, fm), deserialize(bin.range))
}

private fun deserialize(bin: PbIntTerm, fm: Map<Int, Feature>): IntTerm = when {
    bin.hasFeature() -> fm[bin.feature]!! as IntFeature
    bin.hasValue() -> IntValue(bin.value)
    bin.hasMul() -> IntMul(bin.mul.coefficient, fm[bin.mul.feature]!! as IntFeature)
    else -> IntSum(bin.mulsList.map { deserialize(it, fm) as IntMul }, bin.offset)
}

private fun range(range: BooleanRange) = pbBooleanRange { values += range.allValues() }

private fun range(range: PropertyRange<Int>) = when (range as IntRange) {
    is IntInterval -> pbIntRange { lowerBound = range.first(); upperBound = range.last() }
    is IntList -> pbIntRange { values += range.allValues() }
    is EmptyIntRange -> pbIntRange { }
}

private fun range(range: DateRange) = when (range) {
    is DateInterval -> pbDateRange { start = date(range.first()); end = date(range.last()) }
    is DateList -> pbDateRange { dates += range.allValues().map { date(it) } }
    is EmptyDateRange -> pbDateRange { }
}

private fun range(range: EnumRange) = when (range) {
    is EnumList -> pbEnumRange { values += range.allValues() }
    is EmptyEnumRange -> pbEnumRange { }
}

private fun date(date: LocalDate) = Timestamp.valueOf(date.atStartOfDay()).time

private fun comp(op: ComparisonOperator) = when (op) {
    ComparisonOperator.EQ -> ProtoBufPrimitives.PbComparisonOperator.EQ
    ComparisonOperator.NE -> ProtoBufPrimitives.PbComparisonOperator.NE
    ComparisonOperator.LT -> ProtoBufPrimitives.PbComparisonOperator.LT
    ComparisonOperator.LE -> ProtoBufPrimitives.PbComparisonOperator.LE
    ComparisonOperator.GT -> ProtoBufPrimitives.PbComparisonOperator.GT
    ComparisonOperator.GE -> ProtoBufPrimitives.PbComparisonOperator.GE
}

private fun serialize(prop: AnyProperty) = pbProperty {
    name = prop.name
    when (prop) {
        is BooleanProperty -> boolRange = range(prop.range)
        is DateProperty -> dateRange = range(prop.range)
        is IntProperty -> intRange = range(prop.range)
        is EnumProperty -> enumRange = range(prop.range)
    }
}

private fun serialize(def: SlicingBooleanPropertyDefinition) = pbSlicingBooleanPropertyDefinition {
    name = def.name
    if (def.lineNumber != null) lineNumber = def.lineNumber
    values += def.values
}

private fun serialize(def: SlicingIntPropertyDefinition) = pbSlicingIntPropertyDefinition {
    name = def.name
    if (def.lineNumber != null) lineNumber = def.lineNumber
    startValues += def.startValues
    endValues += def.endValues
    singleValues += def.singleValues
}

private fun serialize(def: SlicingDatePropertyDefinition) = pbSlicingDatePropertyDefinition {
    name = def.name
    if (def.lineNumber != null) lineNumber = def.lineNumber
    startValues += def.startValues.map { date(it) }
    endValues += def.endValues.map { date(it) }
    singleValues += def.singleValues.map { date(it) }
}

private fun serialize(def: SlicingEnumPropertyDefinition) = pbSlicingEnumPropertyDefinition {
    name = def.name
    if (def.lineNumber != null) lineNumber = def.lineNumber
    values += def.values
}

private fun deserialize(bin: PbHeader) =
    PrlModelHeader(PrlVersion(bin.major, bin.minor), bin.propertiesList.map { deserialize(it) }.associateBy { it.name })

private fun deserialize(comparison: ProtoBufPrimitives.PbComparisonOperator) = when (comparison) {
    ProtoBufPrimitives.PbComparisonOperator.EQ -> ComparisonOperator.EQ
    ProtoBufPrimitives.PbComparisonOperator.NE -> ComparisonOperator.NE
    ProtoBufPrimitives.PbComparisonOperator.LT -> ComparisonOperator.LT
    ProtoBufPrimitives.PbComparisonOperator.LE -> ComparisonOperator.LE
    ProtoBufPrimitives.PbComparisonOperator.GT -> ComparisonOperator.GT
    ProtoBufPrimitives.PbComparisonOperator.GE -> ComparisonOperator.GE
    ProtoBufPrimitives.PbComparisonOperator.UNRECOGNIZED -> throw IllegalArgumentException("Unknown comparison operator")
}

private fun deserialize(bin: ProtoBufPrimitives.PbBooleanRange) = BooleanRange.list(bin.valuesList)

private fun deserialize(bin: ProtoBufPrimitives.PbIntRange) = if (bin.hasLowerBound()) {
    IntRange.interval(bin.lowerBound, bin.upperBound)
} else {
    IntRange.list(bin.valuesList)
}

private fun deserialize(bin: ProtoBufPrimitives.PbDateRange) = if (bin.hasStart()) {
    DateRange.interval(deDate(bin.start), deDate(bin.end))
} else {
    DateRange.list(bin.datesList.map { deDate(it) })
}

private fun deserialize(bin: ProtoBufPrimitives.PbEnumRange) = EnumRange.list(bin.valuesList)

private fun deDate(bin: Long) = Timestamp(bin).toLocalDateTime().toLocalDate()

private fun deserialize(bin: ProtoBufProperties.PbProperty): AnyProperty = when {
    bin.hasBoolRange() -> BooleanProperty(bin.name, deserialize(bin.boolRange))
    bin.hasDateRange() -> DateProperty(bin.name, deserialize(bin.dateRange))
    bin.hasIntRange() -> IntProperty(bin.name, deserialize(bin.intRange))
    else -> EnumProperty(bin.name, deserialize(bin.enumRange))
}

private fun deserialize(bin: PbSlicingBooleanPropertyDefinition) =
    SlicingBooleanPropertyDefinition(
        bin.name,
        if (bin.hasLineNumber()) bin.lineNumber else null
    ).apply { values.addAll(bin.valuesList) }

private fun deserialize(bin: PbSlicingIntPropertyDefinition) =
    SlicingIntPropertyDefinition(bin.name, if (bin.hasLineNumber()) bin.lineNumber else null).apply {
        startValues.addAll(bin.startValuesList)
        endValues.addAll(bin.endValuesList)
        singleValues.addAll(bin.singleValuesList)
    }

private fun deserialize(bin: PbSlicingDatePropertyDefinition) =
    SlicingDatePropertyDefinition(bin.name, if (bin.hasLineNumber()) bin.lineNumber else null).apply {
        startValues.addAll(bin.startValuesList.map { deDate(it) })
        endValues.addAll(bin.endValuesList.map { deDate(it) })
        singleValues.addAll(bin.singleValuesList.map { deDate(it) })
    }

private fun deserialize(bin: PbSlicingEnumPropertyDefinition) =
    SlicingEnumPropertyDefinition(
        bin.name,
        if (bin.hasLineNumber()) bin.lineNumber else null
    ).apply { values.addAll(bin.valuesList) }

