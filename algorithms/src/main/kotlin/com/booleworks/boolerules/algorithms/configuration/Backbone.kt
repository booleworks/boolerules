// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.configuration

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureInstance
import com.booleworks.boolerules.datastructures.extractFeature
import com.booleworks.boolerules.helpers.computeRelevantIntVars
import com.booleworks.boolerules.helpers.computeRelevantVars
import com.booleworks.boolerules.helpers.satSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.csp.encodings.CspEncodingContext
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.constraints.Feature
import com.booleworks.prl.model.constraints.intFt
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.LngIntVariable
import com.booleworks.prl.transpiler.TranspilationInfo

enum class BackboneType { MANDATORY, FORBIDDEN, OPTIONAL }

data class BackboneResult(
    override val slice: Slice,
    override val state: ComputationState,
    val backbone: Map<FeatureInstance, BackboneType>
) : SliceResult

class Backbone(
    private val features: List<Feature>
) : Algorithm<BackboneResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT, SliceType.ALL)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): BackboneResult {
        //TODO Handler
        //TODO Explanation
        val f = cf.formulaFactory()
        val map = LinkedHashMap<FeatureInstance, BackboneType>()
        val relevantVars = computeRelevantVars(f, info, features).intersect(info.knownVariables)
        val relevantIntVars = computeRelevantIntVars(info, features)
        val (translationFormula, translationMap) = computeTranslatedIntVars(relevantIntVars, cf.formulaFactory(), info)

        val solver = satSolver(cf, false, info).also {
            if (!it.sat()) {
                return BackboneResult(slice, ComputationState.notBuildable(), mapOf())
            }
        }
        solver.add(translationFormula)
        val backbone = solver.backbone(relevantVars + translationMap.keys)

        val mandatoryIntFeatures = mutableSetOf<String>()
        fun addToBackbone(variable: Variable, type: BackboneType) {
            if (translationMap.containsKey(variable)) {
                val (intVar, value) = translationMap[variable]!!
                if (!mandatoryIntFeatures.contains(intVar.feature)) {
                    val feature = FeatureInstance(intFt(intVar.feature), value)
                    map[feature] = type
                    if (type == BackboneType.MANDATORY) mandatoryIntFeatures.add(intVar.feature)
                }
            } else {
                map[extractFeature(variable, info)] = type
            }
        }

        if (backbone.isSat) {
            backbone.positiveBackbone.forEach { addToBackbone(it, BackboneType.MANDATORY) }
            backbone.negativeBackbone.forEach { addToBackbone(it, BackboneType.FORBIDDEN) }
            backbone.optionalVariables.forEach { addToBackbone(it, BackboneType.OPTIONAL) }
        } else {
            relevantVars.forEach { addToBackbone(it, BackboneType.FORBIDDEN) }
        }
        return BackboneResult(slice, ComputationState.success(), map)
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

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: BackboneResult?, newResult: BackboneResult) = newResult
}
