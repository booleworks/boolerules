// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.datastructures.FeatureModel
import com.booleworks.boolerules.datastructures.extractModel
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo
import java.util.*

data class CoveringConfiguration(
    val configuration: FeatureModel,
    val coveredConstraints: List<String>
)

data class CoverageResult(
    override val slice: Slice,
    override val state: ComputationState,
    val result: List<CoveringConfiguration>,
    val uncoverableConstraints: Int
) : SliceResult

class Coverage(
    private val cover: List<String>,
    private val pairwise: Boolean = false
) : Algorithm<CoverageResult> {
    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): CoverageResult {
        //TODO error handling
        val (baseFormula, constraintsToCover, invalidConstraints) = initializeCoverage(info, cf, cover, pairwise)
            ?: return CoverageResult(slice, ComputationState.error("Invalid constraint"), emptyList(), 0)

        val f = cf.formulaFactory()
        val selectors = constraintsToCover.keys
        val initialCover = computeInitialCover(baseFormula, selectors, f)
        val fullModel = minimizeInitialCover(initialCover, baseFormula, selectors, f)
        val modelByIndex: Map<Int, List<Variable>> = fullModel.asSequence()
            .filter { "_copy_" in it.name() }
            .groupBy { it.name().substringAfterLast("_").toInt() - 1 }
        val selectedCopies = (0 until initialCover).filter { baseFormulaSelector(it, f) in fullModel }
        val covers = selectedCopies.map { copyIndex ->
            val matchingModel = modelByIndex[copyIndex]!!.map { f.variable(it.name().substringBefore("_copy_")) }
            val configuration = extractModel(matchingModel, info)
            val coveredConstraints = matchingModel.mapNotNull { constraintsToCover[it]?.second }
            CoveringConfiguration(configuration, coveredConstraints)
        }
        return CoverageResult(slice, ComputationState.success(), covers, invalidConstraints)
    }

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: CoverageResult?, newResult: CoverageResult) = newResult

    private fun computeInitialCover(baseFormula: Formula, variablesToCover: Set<Variable>, f: FormulaFactory): Int {
        val solver = SATSolver.newSolver(f)
        solver.add(baseFormula)
        val remainingSelectors = variablesToCover.toMutableSet()
        var initialCoverSize = 0
        while (remainingSelectors.isNotEmpty()) {
            initialCoverSize += 1
            val maxSelectors = solver.satCall()
                .addFormulas(remainingSelectors.first())
                .selectionOrder(remainingSelectors.drop(1))
                .model(remainingSelectors).positiveVariables()
            remainingSelectors -= maxSelectors
        }
        return initialCoverSize
    }

    private fun minimizeInitialCover(
        initialCover: Int,
        baseFormula: Formula,
        variablesToCover: Set<Variable>,
        f: FormulaFactory
    ): SortedSet<Variable> {
        val baseFormulaCopies = mutableListOf<Formula>()
        val baseFormulaSelectors = sortedSetOf<Variable>()
        val selectorConstraints = mutableListOf<Formula>()
        for (i in 0 until initialCover) {
            val substitution = Substitution()
            for (variable in baseFormula.variables(f)) {
                substitution.addMapping(variable, variableCopy(variable, i, f))
            }
            val baseSubstituted = baseFormula.substitute(f, substitution)
            val baseFormulaSelector = baseFormulaSelector(i, f)
            if (i > 0) {
                selectorConstraints.add(f.implication(baseFormulaSelector, baseFormulaSelector(i - 1, f)))
            }
            val substitutedWithSelector = f.or(baseFormulaSelector.negate(f), baseSubstituted)
            baseFormulaCopies.add(substitutedWithSelector)
            baseFormulaSelectors.add(baseFormulaSelector)
        }
        val coverageConstraints = variablesToCover.map { variable ->
            f.or((0 until initialCover).map { f.and(variableCopy(variable, it, f), baseFormulaSelector(it, f)) })
        }

        val oll = MaxSATSolver.oll(f)
        baseFormulaCopies.forEach(oll::addHardFormula)
        selectorConstraints.forEach(oll::addHardFormula)
        coverageConstraints.forEach(oll::addHardFormula)
        baseFormulaSelectors.forEach { oll.addSoftFormula(it.negate(f), 1) }
        val ollResult = oll.solve()
        assert(ollResult == MaxSAT.MaxSATResult.OPTIMUM) { "OLL not optimal" }
        val fullModel = oll.model().positiveVariables()
        return fullModel
    }
}

internal fun initializeCoverage(
    info: TranspilationInfo,
    cf: CspFactory,
    cover: List<String>,
    pairwise: Boolean
): CoverageInitialization? {
    val baseConstraints = info.propositions.map { it.formula() }.toMutableList()
    val (invalidConstraints, constraintsToCover) = computeConstraintsToCover(
        info, cf, baseConstraints, cover, pairwise
    ) ?: return null
    baseConstraints += constraintsToCover.values.map { it.first }
    val baseFormula = cf.formulaFactory().and(baseConstraints)
    return CoverageInitialization(baseFormula, constraintsToCover, invalidConstraints)
}

private fun computeConstraintsToCover(
    info: TranspilationInfo,
    cf: CspFactory,
    baseConstraints: List<Formula>,
    cover: List<String>,
    pairwise: Boolean
): Pair<Int, Map<Variable, Pair<Formula, String>>>? {
    val f = cf.formulaFactory()
    val baseSolver = SATSolver.newSolver(f)
    baseConstraints.forEach { baseSolver.add(it) }
    val validConstraints = mutableListOf<Pair<Formula, String>>()
    val invalidConstraints = mutableListOf<String>()
    for (constraint in cover) {
        val cPair = info.translateConstraint(f, constraint) ?: return null
        val formula = cPair.second
        if (baseSolver.satCall().addFormulas(formula).sat() == Tristate.FALSE) {
            invalidConstraints.add(constraint)
        } else {
            validConstraints += formula to constraint
        }
    }

    val constraintsToCover = mutableMapOf<Variable, Pair<Formula, String>>()
    // k nicht baubare Constraints = (n-1)+(n-2)+...+(n-k) nicht baubare Kombinationen = k*n-(k*(k+1)/2)
    var numInvalidCombinations: Int
    if (pairwise) {
        numInvalidCombinations = invalidConstraints.size * constraintsToCover.size -
                invalidConstraints.size * (invalidConstraints.size + 1) / 2
        var index = 0
        val invalidCombinations = mutableListOf<String>()
        for (i in 0 until validConstraints.size) {
            val (formulaI, constraintI) = validConstraints[i]
            for (j in i + 1 until validConstraints.size) {
                val (formulaJ, constraintJ) = validConstraints[j]
                val combination = f.and(formulaI, formulaJ)
                if (baseSolver.satCall().addFormulas(combination).sat() == Tristate.FALSE) {
                    invalidCombinations += "[$constraintI, $constraintJ]"
                    numInvalidCombinations++
                } else {
                    val selector = f.variable("@COV_SELECTOR_$index")
                    constraintsToCover[selector] =
                        f.equivalence(selector, combination) to "[$constraintI, $constraintJ]"
                    index += 1
                }
            }
        }
    } else {
        numInvalidCombinations = invalidConstraints.size
        validConstraints.mapIndexed { index, (formula, constraint) ->
            val selector = f.variable("@COV_SELECTOR_$index")
            constraintsToCover[selector] = f.equivalence(selector, formula) to constraint
        }
    }

    return numInvalidCombinations to constraintsToCover
}


internal fun baseFormulaSelector(i: Int, f: FormulaFactory) = f.variable("BASE_COPY_USED_" + (i + 1))
internal fun variableCopy(variable: Variable, i: Int, f: FormulaFactory) =
    f.variable(variable.name() + "_copy_" + (i + 1))

internal data class CoverageInitialization(
    val baseFormula: Formula,
    val constraintsToCover: Map<Variable, Pair<Formula, String>>,
    val invalidConstraints: Int
)
