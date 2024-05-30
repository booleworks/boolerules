// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.backbone

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.computations.backbone.BackboneComputation.BackboneElementResult
import com.booleworks.boolerules.computations.backbone.BackboneComputation.BackboneInternalResult
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.FeatureDO
import com.booleworks.boolerules.computations.generic.InternalListResult
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.ListComputation
import com.booleworks.boolerules.computations.generic.ListComputationRunner
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.NON_PT_CONFIG
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.computeRelevantVars
import com.booleworks.boolerules.computations.generic.extractFeature
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranslationInfo

val BACKBONE =
    object : ComputationType<
            BackboneRequest,
            BackboneResponse,
            BackboneType,
            NoComputationDetail,
            FeatureDO> {
        override val path = "backbone"
        override val docs: ApiDocs = computationDoc<BackboneRequest, BackboneResponse>(
            "Feature Buildability",
            "Compute mandatory and forbidden features of the rule set",
            "A boolean feature or enum/int value is mandatory, if it is contained " +
                    "in each buildable configuration, it is forbidden if it " +
                    "cannot be contained in any buildable configuration, it is " +
                    "optional if it is neither."
        )

        override val request = BackboneRequest::class.java
        override val main = BackboneType::class.java
        override val detail = NoComputationDetail::class.java
        override val element = FeatureDO::class.java

        override val runner = ListComputationRunner(BackboneComputation)
        override val computationFunction = runner::compute
    }

internal object BackboneComputation : ListComputation<
        BackboneRequest,
        Map<String, Map<Slice, BackboneType>>,
        NoComputationDetail,
        BackboneType,
        NoComputationDetail,
        BackboneInternalResult,
        BackboneElementResult,
        FeatureDO>(NON_CACHING_USE_FF) {

    override fun allowedSliceTypes() = setOf(SliceTypeDO.SPLIT, SliceTypeDO.ALL)

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeInternalResult(
        existingResult: BackboneInternalResult?,
        newResult: BackboneInternalResult
    ) = newResult

    override fun computeForSlice(
        request: BackboneRequest,
        slice: Slice,
        info: TranslationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): BackboneInternalResult {
        val result = BackboneInternalResult(slice, LinkedHashMap())
        val relevantVars = computeRelevantVars(cf.formulaFactory(), info, request.features)

        val solver = miniSat(
            NON_PT_CONFIG,
            request,
            cf,
            model,
            info,
            slice,
            status
        ).also { if (!status.successful()) return result }

        val backbone = solver.backbone(relevantVars)
        if (backbone.isSat) {
            backbone.positiveBackbone.forEach {
                result.backbone[extractFeature(it, info)] = BackboneType.MANDATORY
            }
            backbone.negativeBackbone.forEach {
                result.backbone[extractFeature(it, info)] = BackboneType.FORBIDDEN
            }
            backbone.optionalVariables.forEach {
                result.backbone[extractFeature(it, info)] = BackboneType.OPTIONAL
            }
        } else {
            relevantVars.forEach {
                result.backbone[extractFeature(it, info)] = BackboneType.FORBIDDEN
            }
        }
        return result
    }

    override fun extractElements(
        internalResult: BackboneInternalResult
    ): Set<FeatureDO> = internalResult.backbone.keys

    override fun extractInternalResult(
        element: FeatureDO,
        internalResult: BackboneInternalResult
    ): BackboneElementResult =
        BackboneElementResult(
            internalResult.slice,
            internalResult.backbone.getOrDefault(element, BackboneType.FORBIDDEN)
        )

    data class BackboneInternalResult(
        override val slice: Slice,
        val backbone: MutableMap<FeatureDO, BackboneType>
    ) : InternalListResult<Map<String, Map<Slice, BackboneType>>, NoComputationDetail>(slice)

    data class BackboneElementResult(override val slice: Slice, val backboneType: BackboneType) :
        InternalResult<BackboneType, NoComputationDetail>(slice) {
        override fun extractMainResult() = backboneType
        override fun extractDetails() = NoComputationDetail
    }
}
