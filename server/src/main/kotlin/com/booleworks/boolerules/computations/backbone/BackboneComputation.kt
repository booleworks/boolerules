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
import com.booleworks.boolerules.computations.generic.computeRelevantIntVars
import com.booleworks.boolerules.computations.generic.computeRelevantVars
import com.booleworks.boolerules.computations.generic.extractFeature
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo

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
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder,
    ): BackboneInternalResult {
        val f = cf.formulaFactory()
        val result = BackboneInternalResult(slice, LinkedHashMap())
        val relevantVars = computeRelevantVars(f, info, request.features).intersect(info.knownVariables)
        val relevantIntVars = computeRelevantIntVars(info, request.features)
        val (translationFormula, translationMap) = computeTranslatedIntVars(relevantIntVars, cf.formulaFactory(), info)

        val solver = satSolver(NON_PT_CONFIG, f, info, slice, status).also { if (!status.successful()) return result }
        solver.add(translationFormula)
        val backbone = solver.backbone(relevantVars + translationMap.keys)

        val mandatoryIntFeatures = mutableSetOf<String>()
        fun addToBackbone(variable: Variable, type: BackboneType) {
            if (translationMap.containsKey(variable)) {
                val (intVar, value) = translationMap[variable]!!
                if (!mandatoryIntFeatures.contains(intVar.feature)) {
                    val feature = FeatureDO.int(intVar.feature, value)
                    result.backbone[feature] = type
                    if (type == BackboneType.MANDATORY) mandatoryIntFeatures.add(intVar.feature)
                }
            } else result.backbone[extractFeature(variable, info)] = type
        }

        if (backbone.isSat) {
            backbone.positiveBackbone.forEach { addToBackbone(it, BackboneType.MANDATORY) }
            backbone.negativeBackbone.forEach { addToBackbone(it, BackboneType.FORBIDDEN) }
            backbone.optionalVariables.forEach { addToBackbone(it, BackboneType.OPTIONAL) }
        } else {
            relevantVars.forEach { addToBackbone(it, BackboneType.FORBIDDEN) }
        }
        return result
    }

    private fun computeTranslatedIntVars(
        variables: Set<LngIntVariable>,
        f: FormulaFactory,
        info: TranspilationInfo
    ): Pair<Formula, Map<Variable, Pair<LngIntVariable, Int>>> {
        val clauses = mutableListOf<Formula>()
        val map = mutableMapOf<Variable, Pair<LngIntVariable, Int>>()
        variables.forEach { intVar ->
            val satVars = info.encodingContext.variableMap[intVar.variable]!!
            val domain = intVar.variable.domain
            var previousVar: Variable? = null
            var index = 0
            var c = domain.lb()
            while (c < domain.ub()) {
                if (domain.contains(c)) {
                    val originalVar = satVars[index]!!
                    val translatedVar = f.newAuxVariable(CspEncodingContext.CSP_AUX_LNG_VARIABLE)
                    if (previousVar == null) {
                        clauses.add(f.equivalence(originalVar, translatedVar))
                    } else {
                        clauses.add(f.equivalence(f.and(previousVar.negate(f), originalVar), translatedVar))
                    }
                    map[translatedVar] = Pair(intVar, c)
                    previousVar = originalVar;
                    ++index
                }
                ++c
            }
            if (previousVar != null) {
                val translatedVar = f.newAuxVariable(CspEncodingContext.CSP_AUX_LNG_VARIABLE)
                clauses.add(f.equivalence(previousVar.negate(f), translatedVar))
                map[translatedVar] = Pair(intVar, domain.ub())
            }
        }
        return Pair(f.and(clauses), map)
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
