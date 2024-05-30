// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.model

import com.booleworks.prl.model.constraints.BooleanFeature
import com.booleworks.prl.model.constraints.EnumFeature
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.IntFeature
import com.booleworks.prl.model.constraints.boolFt
import com.booleworks.prl.model.constraints.enumFt
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.constraints.versionFt
import com.booleworks.prl.model.datastructures.FeatureRenaming
import com.booleworks.prl.model.slices.AnySliceSelection
import com.booleworks.prl.model.slices.evaluateProperties
import com.booleworks.prl.parser.PragmaticRuleLanguage.INDENT
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_DESCRIPTION
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_ENUM
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_FEATURE
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_INT
import com.booleworks.prl.parser.PragmaticRuleLanguage.KEYWORD_VERSIONED
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_LBRA
import com.booleworks.prl.parser.PragmaticRuleLanguage.SYMBOL_RBRA
import com.booleworks.prl.parser.PragmaticRuleLanguage.identifier
import com.booleworks.prl.parser.PragmaticRuleLanguage.quote
import com.booleworks.prl.parser.PrlBooleanFeatureDefinition
import com.booleworks.prl.parser.PrlEnumFeatureDefinition
import com.booleworks.prl.parser.PrlFeatureDefinition
import com.booleworks.prl.parser.PrlIntFeatureDefinition
import java.util.Objects

typealias AnyFeatureDef = FeatureDefinition<*, *>

enum class Theory { BOOL, VERSIONED_BOOL, ENUM, INT }

/**
 * Super class for features of different types.  A feature always has a code
 * (name) which has to be unique.  It can have the following optional
 * parameters:
 *
 *  description: A textual description of the feature
 *  enforces: a list of other features which are directly enforced by this
 *            feature
 *  versioned: a flag whether this feature is versioned or not
 *  visibility: the visibility of the feature
 *  properties: a list of additional properties of the feature
 */
sealed class FeatureDefinition<F : FeatureDefinition<F, *>, T : Feature>(
    open val code: String,
    open val description: String,
    open val properties: Map<String, AnyProperty>,
    open val lineNumber: Int? = null,
    var used: Boolean = false
) {
    abstract fun rename(renaming: FeatureRenaming): F
    abstract fun stripProperties(): F
    abstract fun stripMetaInfo(): F
    abstract fun stripAll(): F
    abstract val headerLine: String
    abstract val feature: T

    fun appendString(app: Appendable, depth: Int): Appendable {
        val i = INDENT.repeat(depth)
        val ii = i + INDENT
        if (description.isBlank() && properties.isEmpty()) {
            app.append(i).append(headerLine)
        } else {
            app.append(i).append(headerLine).append(" ").append(SYMBOL_LBRA).append(System.lineSeparator())
            if (description.isNotBlank()) app.append(ii).append(KEYWORD_DESCRIPTION).append(" ")
                .append(quote(description)).append(System.lineSeparator())
            properties.forEach { app.append(ii).append(it.value.toString()).append(System.lineSeparator()) }
            app.append(i).append(SYMBOL_RBRA)
        }
        return app
    }

    fun filter(sliceSelections: List<AnySliceSelection>) = evaluateProperties(properties, sliceSelections)

    override fun toString() = StringBuilder().apply { appendString(this, 0) }.toString()

    override fun hashCode() = Objects.hash(code, description, properties, headerLine, feature)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AnyFeatureDef
        if (code != other.code) return false
        if (description != other.description) return false
        if (properties != other.properties) return false
        if (headerLine != other.headerLine) return false
        return feature == other.feature
    }

    companion object {
        internal fun fromPrlDef(definition: PrlFeatureDefinition): AnyFeatureDef =
            when (definition) {
                is PrlBooleanFeatureDefinition -> BooleanFeatureDefinition(definition)
                is PrlEnumFeatureDefinition -> EnumFeatureDefinition(definition)
                is PrlIntFeatureDefinition -> IntFeatureDefinition(definition)
            }
    }
}

/**
 * A boolean feature.
 *
 * A boolean feature can have two values - false or true - and is the main building block of
 * many rule sets in propositional logic.
 */
class BooleanFeatureDefinition(
    override val code: String,
    val versioned: Boolean = false,
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : FeatureDefinition<BooleanFeatureDefinition, BooleanFeature>(
    code,
    description,
    properties,
    lineNumber
) {

    constructor(d: PrlBooleanFeatureDefinition) : this(
        d.code,
        d.versioned,
        d.description,
        compileProperties(d.properties).associateBy { it.name },
        d.lineNumber
    )

    override fun stripProperties() = BooleanFeatureDefinition(code, versioned, description, mapOf(), lineNumber)

    override fun stripMetaInfo() = BooleanFeatureDefinition(code, versioned, "", properties, lineNumber)

    override fun stripAll() = BooleanFeatureDefinition(code, versioned, "", mapOf(), lineNumber)
    override fun rename(renaming: FeatureRenaming): BooleanFeatureDefinition {
        val renamedCode = renaming.rename(feature).featureCode
        return BooleanFeatureDefinition(renamedCode, versioned, description, properties, lineNumber)
    }

    override val headerLine = (if (versioned) "$KEYWORD_VERSIONED " else "") + "$KEYWORD_FEATURE " + identifier(code)

    override val feature = if (versioned) versionFt(code) else boolFt(code)

    companion object {
        fun merge(definitions: Collection<BooleanFeatureDefinition>): BooleanFeatureDefinition {
            assert(!definitions.isEmpty())
            val code = definitions.first().code
            val version = definitions.first().versioned
            return BooleanFeatureDefinition(code, version)
        }
    }
}

/**
 * An enum feature definition.
 *
 * An enum feature must be defined with a range of possible values. For any buildable feature combination, the
 * feature is assigned to exactly one value of these values. In constraints, enum features can be compared with
 * fixed enum values (like x = "abc") or with a range (like x in ["a", "b", "c"]).
 */
class EnumFeatureDefinition(
    override val code: String,
    val values: Set<String>,
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : FeatureDefinition<EnumFeatureDefinition, EnumFeature>(
    code,
    description,
    properties,
    lineNumber
) {

    constructor(d: PrlEnumFeatureDefinition) : this(
        d.code,
        d.values.toSet(),
        d.description,
        compileProperties(d.properties).associateBy { it.name },
        d.lineNumber
    )

    override fun stripProperties() = EnumFeatureDefinition(code, values, description, mapOf(), lineNumber)
    override fun stripMetaInfo() = EnumFeatureDefinition(code, values, "", properties, lineNumber)
    override fun stripAll() = EnumFeatureDefinition(code, values, "", mapOf(), lineNumber)
    override fun rename(renaming: FeatureRenaming): EnumFeatureDefinition {
        val renamedCode = renaming.rename(feature).featureCode
        val renamedValues = values.map { renaming.rename(feature, it) }.toSet()
        return EnumFeatureDefinition(renamedCode, renamedValues, description, properties, lineNumber)
    }

    override val headerLine = "$KEYWORD_ENUM $KEYWORD_FEATURE " + identifier(code) + " " +
            (if (values.isEmpty()) "e" else "") + quote(values)
    override val feature = enumFt(code)

    companion object {
        fun merge(definitions: Collection<EnumFeatureDefinition>): EnumFeatureDefinition {
            assert(!definitions.isEmpty())
            val code = definitions.first().code
            val values = definitions.map { it.values }.reduce { acc, values -> acc.intersect(values) }
            return EnumFeatureDefinition(code, values)
        }
    }
}


/**
 * An integer feature.
 *
 * An integer feature must be defined with a range of possible values (c.f. [IntRange]).
 * For any buildable feature combination, the feature is assigned to exactly one value of this range.
 * In constraints, integer features can be compared with each other, with fixed values (like x &lt;= 7)
 * or with a range (like x in [1-7]).
 */
class IntFeatureDefinition(
    override val code: String,
    val domain: PropertyRange<Int>,
    override val description: String = "",
    override val properties: Map<String, AnyProperty> = mapOf(),
    override val lineNumber: Int? = null
) : FeatureDefinition<IntFeatureDefinition, IntFeature>(code, description, properties, lineNumber) {

    constructor(d: PrlIntFeatureDefinition) : this(
        d.code,
        d.domain,
        d.description,
        compileProperties(d.properties).associateBy { it.name },
        d.lineNumber
    )

    override fun stripProperties() = IntFeatureDefinition(code, domain, description, mapOf(), lineNumber)
    override fun stripMetaInfo() = IntFeatureDefinition(code, domain, "", properties, lineNumber)
    override fun stripAll() = IntFeatureDefinition(code, domain, "", mapOf(), lineNumber)
    override fun rename(renaming: FeatureRenaming) =
        IntFeatureDefinition(
            renaming.rename(feature).featureCode,
            domain,
            description,
            properties,
            lineNumber
        )

    override val headerLine = "$KEYWORD_INT $KEYWORD_FEATURE " + identifier(code) + " " + domain
    override val feature = intFt(code)

    companion object {
        fun merge(definitions: Collection<IntFeatureDefinition>): IntFeatureDefinition {
            assert(!definitions.isEmpty())
            val code = definitions.first().code
            val domain = definitions.map { it.domain }.reduce { acc, domain -> acc.intersection(domain) }
            return IntFeatureDefinition(code, domain)
        }
    }
}
