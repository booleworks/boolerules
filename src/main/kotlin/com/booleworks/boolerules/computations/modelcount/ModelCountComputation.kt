// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.modelcount

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.CACHING_IMPORT_FF
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.modelcount.ModelCountComputation.ModelCountInternalResult
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.modelcounting.ModelCounter
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranslationInfo
import java.math.BigInteger

val MODELCOUNT = object : ComputationType<
    ModelCountRequest,
    ModelCountResponse,
    BigInteger,
    NoComputationDetail,
    NoElement> {
    override val path: String = "modelcount"
    override val docs: ApiDocs = computationDoc<ModelCountRequest, ModelCountResponse>(
        "Configuration Counting",
        "Compute the model count of a rule file",
        "The model count represents the number of valid configurations for the rule file."
    )

    override val request = ModelCountRequest::class.java
    override val main = BigInteger::class.java
    override val detail = NoComputationDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(ModelCountComputation)
    override val computationFunction = runner::compute
}

internal object ModelCountComputation :
    SingleComputation<ModelCountRequest, BigInteger, NoComputationDetail, ModelCountInternalResult>(CACHING_IMPORT_FF) {

    override fun allowedSliceTypes() = setOf(SliceTypeDO.SPLIT, SliceTypeDO.ALL)

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeInternalResult(
        existingResult: ModelCountInternalResult?,
        newResult: ModelCountInternalResult
    ) = newResult

    override fun computeForSlice(
        request: ModelCountRequest,
        slice: Slice,
        info: TranslationInfo,
        model: PrlModel,
        f: FormulaFactory,
        status: ComputationStatusBuilder,
    ): ModelCountInternalResult {
        // currently no projected model counting
//        val variables = computeRelevantVars(f, info, listOf())
        val intSatVariables = info.integerVariables.flatMap { info.encodingContext.variableMap[it.variable]?.values ?: emptySet() }
        val variables = (info.knownVariables + info.intPredicateMapping.values + intSatVariables).toSortedSet()
        val formulas = info.propositions.map { it.formula() }
        val additionalDefinitions =
            request.additionalConstraints.map { constraint -> processConstraint(f, constraint, model, info, status) }
        val additionalConstraints = additionalDefinitions.map { it!!.first.formula() }
        val additionalVarDefinitions = additionalDefinitions.flatMap { it!!.second }.toSet().map { info.integerEncodings.getEncoding(it)!! }
        val allFormulas = formulas + additionalConstraints + additionalVarDefinitions
        val allVariables = (allFormulas.flatMap { it.variables(f) } + variables).toSortedSet()
        if (!status.successful()) {
            return ModelCountInternalResult(slice, BigInteger.ZERO)
        }
        return ModelCountInternalResult(
            slice,
            ModelCounter.count(f, formulas + additionalConstraints + additionalVarDefinitions, allVariables)
        )
    }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranslationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        f: FormulaFactory
    ) = error("details are always computed in main computation")

    data class ModelCountInternalResult(override val slice: Slice, val count: BigInteger) :
        InternalResult<BigInteger, NoComputationDetail>(slice) {
        override fun extractMainResult() = count
        override fun extractDetails() = NoComputationDetail
    }
}
