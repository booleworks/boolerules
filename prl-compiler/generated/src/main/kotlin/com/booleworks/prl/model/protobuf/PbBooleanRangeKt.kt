// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: model_primitives.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package com.booleworks.prl.model.protobuf;

@kotlin.jvm.JvmName("-initializepbBooleanRange")
public inline fun pbBooleanRange(block: com.booleworks.prl.model.protobuf.PbBooleanRangeKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange =
  com.booleworks.prl.model.protobuf.PbBooleanRangeKt.Dsl._create(com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `boolerules.primitives.PbBooleanRange`
 */
public object PbBooleanRangeKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange = _builder.build()

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class ValuesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated bool values = 1;`
     */
     public val values: com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getValuesList()
      )
    /**
     * `repeated bool values = 1;`
     * @param value The values to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>.add(value: kotlin.Boolean) {
      _builder.addValues(value)
    }/**
     * `repeated bool values = 1;`
     * @param value The values to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>.plusAssign(value: kotlin.Boolean) {
      add(value)
    }/**
     * `repeated bool values = 1;`
     * @param values The values to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>.addAll(values: kotlin.collections.Iterable<kotlin.Boolean>) {
      _builder.addAllValues(values)
    }/**
     * `repeated bool values = 1;`
     * @param values The values to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllValues")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>.plusAssign(values: kotlin.collections.Iterable<kotlin.Boolean>) {
      addAll(values)
    }/**
     * `repeated bool values = 1;`
     * @param index The index to set the value at.
     * @param value The values to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setValues")
    public operator fun com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>.set(index: kotlin.Int, value: kotlin.Boolean) {
      _builder.setValues(index, value)
    }/**
     * `repeated bool values = 1;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearValues")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Boolean, ValuesProxy>.clear() {
      _builder.clearValues()
    }}
}
@kotlin.jvm.JvmSynthetic
public inline fun com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange.copy(block: `com.booleworks.prl.model.protobuf`.PbBooleanRangeKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufPrimitives.PbBooleanRange =
  `com.booleworks.prl.model.protobuf`.PbBooleanRangeKt.Dsl._create(this.toBuilder()).apply { block() }._build()
