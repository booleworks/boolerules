// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: model_constraints.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package com.booleworks.prl.model.protobuf;

@kotlin.jvm.JvmName("-initializepbIntTerm")
public inline fun pbIntTerm(block: com.booleworks.prl.model.protobuf.PbIntTermKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm =
  com.booleworks.prl.model.protobuf.PbIntTermKt.Dsl._create(com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `boolerules.constraints.PbIntTerm`
 */
public object PbIntTermKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm = _builder.build()

    /**
     * `optional int32 feature = 1;`
     */
    public var feature: kotlin.Int
      @JvmName("getFeature")
      get() = _builder.getFeature()
      @JvmName("setFeature")
      set(value) {
        _builder.setFeature(value)
      }
    /**
     * `optional int32 feature = 1;`
     */
    public fun clearFeature() {
      _builder.clearFeature()
    }
    /**
     * `optional int32 feature = 1;`
     * @return Whether the feature field is set.
     */
    public fun hasFeature(): kotlin.Boolean {
      return _builder.hasFeature()
    }

    /**
     * `optional int32 value = 2;`
     */
    public var value: kotlin.Int
      @JvmName("getValue")
      get() = _builder.getValue()
      @JvmName("setValue")
      set(value) {
        _builder.setValue(value)
      }
    /**
     * `optional int32 value = 2;`
     */
    public fun clearValue() {
      _builder.clearValue()
    }
    /**
     * `optional int32 value = 2;`
     * @return Whether the value field is set.
     */
    public fun hasValue(): kotlin.Boolean {
      return _builder.hasValue()
    }

    /**
     * `optional .boolerules.constraints.PbIntMul mul = 3;`
     */
    public var mul: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntMul
      @JvmName("getMul")
      get() = _builder.getMul()
      @JvmName("setMul")
      set(value) {
        _builder.setMul(value)
      }
    /**
     * `optional .boolerules.constraints.PbIntMul mul = 3;`
     */
    public fun clearMul() {
      _builder.clearMul()
    }
    /**
     * `optional .boolerules.constraints.PbIntMul mul = 3;`
     * @return Whether the mul field is set.
     */
    public fun hasMul(): kotlin.Boolean {
      return _builder.hasMul()
    }
    public val PbIntTermKt.Dsl.mulOrNull: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntMul?
      get() = _builder.mulOrNull

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class MulsProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     */
     public val muls: com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getMulsList()
      )
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     * @param value The muls to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addMuls")
    public fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>.add(value: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm) {
      _builder.addMuls(value)
    }
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     * @param value The muls to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignMuls")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>.plusAssign(value: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm) {
      add(value)
    }
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     * @param values The muls to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllMuls")
    public fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>.addAll(values: kotlin.collections.Iterable<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm>) {
      _builder.addAllMuls(values)
    }
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     * @param values The muls to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllMuls")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>.plusAssign(values: kotlin.collections.Iterable<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm>) {
      addAll(values)
    }
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     * @param index The index to set the value at.
     * @param value The muls to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setMuls")
    public operator fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>.set(index: kotlin.Int, value: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm) {
      _builder.setMuls(index, value)
    }
    /**
     * `repeated .boolerules.constraints.PbIntTerm muls = 4;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearMuls")
    public fun com.google.protobuf.kotlin.DslList<com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm, MulsProxy>.clear() {
      _builder.clearMuls()
    }


    /**
     * `optional int32 offset = 5;`
     */
    public var offset: kotlin.Int
      @JvmName("getOffset")
      get() = _builder.getOffset()
      @JvmName("setOffset")
      set(value) {
        _builder.setOffset(value)
      }
    /**
     * `optional int32 offset = 5;`
     */
    public fun clearOffset() {
      _builder.clearOffset()
    }
    /**
     * `optional int32 offset = 5;`
     * @return Whether the offset field is set.
     */
    public fun hasOffset(): kotlin.Boolean {
      return _builder.hasOffset()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm.copy(block: `com.booleworks.prl.model.protobuf`.PbIntTermKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTerm =
  `com.booleworks.prl.model.protobuf`.PbIntTermKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntTermOrBuilder.mulOrNull: com.booleworks.prl.model.protobuf.ProtoBufConstraints.PbIntMul?
  get() = if (hasMul()) getMul() else null
