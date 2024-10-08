// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: model_featurestore.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package com.booleworks.prl.model.protobuf;

@kotlin.jvm.JvmName("-initializepbFeatureDefinition")
public inline fun pbFeatureDefinition(block: com.booleworks.prl.model.protobuf.PbFeatureDefinitionKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition =
  com.booleworks.prl.model.protobuf.PbFeatureDefinitionKt.Dsl._create(com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `boolerules.features.PbFeatureDefinition`
 */
public object PbFeatureDefinitionKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition = _builder.build()

    /**
     * `string code = 2;`
     */
    public var code: kotlin.String
      @JvmName("getCode")
      get() = _builder.getCode()
      @JvmName("setCode")
      set(value) {
        _builder.setCode(value)
      }
    /**
     * `string code = 2;`
     */
    public fun clearCode() {
      _builder.clearCode()
    }

    /**
     * `optional string description = 4;`
     */
    public var description: kotlin.String
      @JvmName("getDescription")
      get() = _builder.getDescription()
      @JvmName("setDescription")
      set(value) {
        _builder.setDescription(value)
      }
    /**
     * `optional string description = 4;`
     */
    public fun clearDescription() {
      _builder.clearDescription()
    }
    /**
     * `optional string description = 4;`
     * @return Whether the description field is set.
     */
    public fun hasDescription(): kotlin.Boolean {
      return _builder.hasDescription()
    }

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class PropertiesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     */
     public val properties: com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getPropertiesList()
      )
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     * @param value The properties to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addProperties")
    public fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>.add(value: com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty) {
      _builder.addProperties(value)
    }
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     * @param value The properties to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignProperties")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>.plusAssign(value: com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty) {
      add(value)
    }
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     * @param values The properties to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllProperties")
    public fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>.addAll(values: kotlin.collections.Iterable<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty>) {
      _builder.addAllProperties(values)
    }
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     * @param values The properties to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllProperties")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>.plusAssign(values: kotlin.collections.Iterable<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty>) {
      addAll(values)
    }
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     * @param index The index to set the value at.
     * @param value The properties to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setProperties")
    public operator fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>.set(index: kotlin.Int, value: com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty) {
      _builder.setProperties(index, value)
    }
    /**
     * `repeated .boolerules.properties.PbProperty properties = 5;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearProperties")
    public fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufProperties.PbProperty, PropertiesProxy>.clear() {
      _builder.clearProperties()
    }


    /**
     * `optional int32 lineNumber = 6;`
     */
    public var lineNumber: kotlin.Int
      @JvmName("getLineNumber")
      get() = _builder.getLineNumber()
      @JvmName("setLineNumber")
      set(value) {
        _builder.setLineNumber(value)
      }
    /**
     * `optional int32 lineNumber = 6;`
     */
    public fun clearLineNumber() {
      _builder.clearLineNumber()
    }
    /**
     * `optional int32 lineNumber = 6;`
     * @return Whether the lineNumber field is set.
     */
    public fun hasLineNumber(): kotlin.Boolean {
      return _builder.hasLineNumber()
    }

    /**
     * `bool used = 7;`
     */
    public var used: kotlin.Boolean
      @JvmName("getUsed")
      get() = _builder.getUsed()
      @JvmName("setUsed")
      set(value) {
        _builder.setUsed(value)
      }
    /**
     * `bool used = 7;`
     */
    public fun clearUsed() {
      _builder.clearUsed()
    }

    /**
     * ```
     * boolean/versioned boolean features
     * ```
     *
     * `optional bool versioned = 8;`
     */
    public var versioned: kotlin.Boolean
      @JvmName("getVersioned")
      get() = _builder.getVersioned()
      @JvmName("setVersioned")
      set(value) {
        _builder.setVersioned(value)
      }
    /**
     * ```
     * boolean/versioned boolean features
     * ```
     *
     * `optional bool versioned = 8;`
     */
    public fun clearVersioned() {
      _builder.clearVersioned()
    }
    /**
     * ```
     * boolean/versioned boolean features
     * ```
     *
     * `optional bool versioned = 8;`
     * @return Whether the versioned field is set.
     */
    public fun hasVersioned(): kotlin.Boolean {
      return _builder.hasVersioned()
    }

    /**
     * ```
     * int feature
     * ```
     *
     * `optional .boolerules.primitives.PbIntRange intDomain = 9;`
     */
    public var intDomain: com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbIntRange
      @JvmName("getIntDomain")
      get() = _builder.getIntDomain()
      @JvmName("setIntDomain")
      set(value) {
        _builder.setIntDomain(value)
      }
    /**
     * ```
     * int feature
     * ```
     *
     * `optional .boolerules.primitives.PbIntRange intDomain = 9;`
     */
    public fun clearIntDomain() {
      _builder.clearIntDomain()
    }
    /**
     * ```
     * int feature
     * ```
     *
     * `optional .boolerules.primitives.PbIntRange intDomain = 9;`
     * @return Whether the intDomain field is set.
     */
    public fun hasIntDomain(): kotlin.Boolean {
      return _builder.hasIntDomain()
    }
    public val PbFeatureDefinitionKt.Dsl.intDomainOrNull: com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbIntRange?
      get() = _builder.intDomainOrNull

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class EnumValuesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     * @return A list containing the enumValues.
     */
    public val enumValues: com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getEnumValuesList()
      )
    /**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     * @param value The enumValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addEnumValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>.add(value: kotlin.String) {
      _builder.addEnumValues(value)
    }
    /**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     * @param value The enumValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignEnumValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>.plusAssign(value: kotlin.String) {
      add(value)
    }
    /**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     * @param values The enumValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllEnumValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>.addAll(values: kotlin.collections.Iterable<kotlin.String>) {
      _builder.addAllEnumValues(values)
    }
    /**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     * @param values The enumValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllEnumValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>.plusAssign(values: kotlin.collections.Iterable<kotlin.String>) {
      addAll(values)
    }
    /**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     * @param index The index to set the value at.
     * @param value The enumValues to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setEnumValues")
    public operator fun com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>.set(index: kotlin.Int, value: kotlin.String) {
      _builder.setEnumValues(index, value)
    }/**
     * ```
     * enum feature
     * ```
     *
     * `repeated string enumValues = 10;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearEnumValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.String, EnumValuesProxy>.clear() {
      _builder.clearEnumValues()
    }}
}
@kotlin.jvm.JvmSynthetic
public inline fun com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition.copy(block: `com.booleworks.prl.model.protobuf`.PbFeatureDefinitionKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinition =
  `com.booleworks.prl.model.protobuf`.PbFeatureDefinitionKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFeatureDefinitionOrBuilder.intDomainOrNull: com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbIntRange?
  get() = if (hasIntDomain()) getIntDomain() else null

