// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: model_properties.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package com.booleworks.prl.model.protobuf;

@kotlin.jvm.JvmName("-initializepbSlicingIntPropertyDefinition")
public inline fun pbSlicingIntPropertyDefinition(block: com.booleworks.prl.model.protobuf.PbSlicingIntPropertyDefinitionKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition =
  com.booleworks.prl.model.protobuf.PbSlicingIntPropertyDefinitionKt.Dsl._create(com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `boolerules.properties.PbSlicingIntPropertyDefinition`
 */
public object PbSlicingIntPropertyDefinitionKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition = _builder.build()

    /**
     * `string name = 1;`
     */
    public var name: kotlin.String
      @JvmName("getName")
      get() = _builder.getName()
      @JvmName("setName")
      set(value) {
        _builder.setName(value)
      }
    /**
     * `string name = 1;`
     */
    public fun clearName() {
      _builder.clearName()
    }

    /**
     * `optional int32 lineNumber = 2;`
     */
    public var lineNumber: kotlin.Int
      @JvmName("getLineNumber")
      get() = _builder.getLineNumber()
      @JvmName("setLineNumber")
      set(value) {
        _builder.setLineNumber(value)
      }
    /**
     * `optional int32 lineNumber = 2;`
     */
    public fun clearLineNumber() {
      _builder.clearLineNumber()
    }
    /**
     * `optional int32 lineNumber = 2;`
     * @return Whether the lineNumber field is set.
     */
    public fun hasLineNumber(): kotlin.Boolean {
      return _builder.hasLineNumber()
    }

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class StartValuesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated int32 startValues = 3;`
     */
     public val startValues: com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getStartValuesList()
      )
    /**
     * `repeated int32 startValues = 3;`
     * @param value The startValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addStartValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>.add(value: kotlin.Int) {
      _builder.addStartValues(value)
    }/**
     * `repeated int32 startValues = 3;`
     * @param value The startValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignStartValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>.plusAssign(value: kotlin.Int) {
      add(value)
    }/**
     * `repeated int32 startValues = 3;`
     * @param values The startValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllStartValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>.addAll(values: kotlin.collections.Iterable<kotlin.Int>) {
      _builder.addAllStartValues(values)
    }/**
     * `repeated int32 startValues = 3;`
     * @param values The startValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllStartValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>.plusAssign(values: kotlin.collections.Iterable<kotlin.Int>) {
      addAll(values)
    }/**
     * `repeated int32 startValues = 3;`
     * @param index The index to set the value at.
     * @param value The startValues to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setStartValues")
    public operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>.set(index: kotlin.Int, value: kotlin.Int) {
      _builder.setStartValues(index, value)
    }/**
     * `repeated int32 startValues = 3;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearStartValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, StartValuesProxy>.clear() {
      _builder.clearStartValues()
    }
    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class EndValuesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated int32 endValues = 4;`
     */
     public val endValues: com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getEndValuesList()
      )
    /**
     * `repeated int32 endValues = 4;`
     * @param value The endValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addEndValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>.add(value: kotlin.Int) {
      _builder.addEndValues(value)
    }/**
     * `repeated int32 endValues = 4;`
     * @param value The endValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignEndValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>.plusAssign(value: kotlin.Int) {
      add(value)
    }/**
     * `repeated int32 endValues = 4;`
     * @param values The endValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllEndValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>.addAll(values: kotlin.collections.Iterable<kotlin.Int>) {
      _builder.addAllEndValues(values)
    }/**
     * `repeated int32 endValues = 4;`
     * @param values The endValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllEndValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>.plusAssign(values: kotlin.collections.Iterable<kotlin.Int>) {
      addAll(values)
    }/**
     * `repeated int32 endValues = 4;`
     * @param index The index to set the value at.
     * @param value The endValues to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setEndValues")
    public operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>.set(index: kotlin.Int, value: kotlin.Int) {
      _builder.setEndValues(index, value)
    }/**
     * `repeated int32 endValues = 4;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearEndValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, EndValuesProxy>.clear() {
      _builder.clearEndValues()
    }
    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class SingleValuesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated int32 singleValues = 5;`
     */
     public val singleValues: com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getSingleValuesList()
      )
    /**
     * `repeated int32 singleValues = 5;`
     * @param value The singleValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addSingleValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>.add(value: kotlin.Int) {
      _builder.addSingleValues(value)
    }/**
     * `repeated int32 singleValues = 5;`
     * @param value The singleValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignSingleValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>.plusAssign(value: kotlin.Int) {
      add(value)
    }/**
     * `repeated int32 singleValues = 5;`
     * @param values The singleValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllSingleValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>.addAll(values: kotlin.collections.Iterable<kotlin.Int>) {
      _builder.addAllSingleValues(values)
    }/**
     * `repeated int32 singleValues = 5;`
     * @param values The singleValues to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllSingleValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>.plusAssign(values: kotlin.collections.Iterable<kotlin.Int>) {
      addAll(values)
    }/**
     * `repeated int32 singleValues = 5;`
     * @param index The index to set the value at.
     * @param value The singleValues to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setSingleValues")
    public operator fun com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>.set(index: kotlin.Int, value: kotlin.Int) {
      _builder.setSingleValues(index, value)
    }/**
     * `repeated int32 singleValues = 5;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearSingleValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Int, SingleValuesProxy>.clear() {
      _builder.clearSingleValues()
    }}
}
@kotlin.jvm.JvmSynthetic
public inline fun com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition.copy(block: `com.booleworks.prl.model.protobuf`.PbSlicingIntPropertyDefinitionKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufProperties.PbSlicingIntPropertyDefinition =
  `com.booleworks.prl.model.protobuf`.PbSlicingIntPropertyDefinitionKt.Dsl._create(this.toBuilder()).apply { block() }._build()
