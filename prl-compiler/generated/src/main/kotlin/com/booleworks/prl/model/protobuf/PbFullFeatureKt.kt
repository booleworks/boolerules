// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: model_featurestore.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package com.booleworks.prl.model.protobuf;

@kotlin.jvm.JvmName("-initializepbFullFeature")
public inline fun pbFullFeature(block: com.booleworks.prl.model.protobuf.PbFullFeatureKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature =
  com.booleworks.prl.model.protobuf.PbFullFeatureKt.Dsl._create(com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `boolerules.features.PbFullFeature`
 */
public object PbFullFeatureKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature = _builder.build()

    /**
     * `int32 id = 1;`
     */
    public var id: kotlin.Int
      @JvmName("getId")
      get() = _builder.getId()
      @JvmName("setId")
      set(value) {
        _builder.setId(value)
      }
    /**
     * `int32 id = 1;`
     */
    public fun clearId() {
      _builder.clearId()
    }

    /**
     * `string featureCode = 2;`
     */
    public var featureCode: kotlin.String
      @JvmName("getFeatureCode")
      get() = _builder.getFeatureCode()
      @JvmName("setFeatureCode")
      set(value) {
        _builder.setFeatureCode(value)
      }
    /**
     * `string featureCode = 2;`
     */
    public fun clearFeatureCode() {
      _builder.clearFeatureCode()
    }

    /**
     * `.boolerules.features.PbTheory theory = 3;`
     */
    public var theory: com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbTheory
      @JvmName("getTheory")
      get() = _builder.getTheory()
      @JvmName("setTheory")
      set(value) {
        _builder.setTheory(value)
      }
    public var theoryValue: kotlin.Int
      @JvmName("getTheoryValue")
      get() = _builder.getTheoryValue()
      @JvmName("setTheoryValue")
      set(value) {
        _builder.setTheoryValue(value)
      }
    /**
     * `.boolerules.features.PbTheory theory = 3;`
     */
    public fun clearTheory() {
      _builder.clearTheory()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature.copy(block: `com.booleworks.prl.model.protobuf`.PbFullFeatureKt.Dsl.() -> kotlin.Unit): com.booleworks.prl.model.protobuf.ProtoBufFeatureStore.PbFullFeature =
  `com.booleworks.prl.model.protobuf`.PbFullFeatureKt.Dsl._create(this.toBuilder()).apply { block() }._build()
