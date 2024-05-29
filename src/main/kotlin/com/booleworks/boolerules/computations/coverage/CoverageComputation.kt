package com.booleworks.boolerules.computations.coverage

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.boolerules.computations.generic.extractModel
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.datastructures.Tristate
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.SATSolver
import com.booleworks.logicng.solvers.functions.OptimizationFunction
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranslationInfo
import java.util.SortedSet

val COVERAGE = object : ComputationType<
        CoverageRequest,
        CoverageResponse,
        CoverageMainResult,
        CoverageDetail,
        NoElement> {
    override val path = "coverage"
    override val docs: ApiDocs = computationDoc<CoverageRequest, CoverageResponse>(
        "Coverage",
        "Compute a minimal number of configurations to cover a set of constraints",
        "Compute a minimal number of configurations to cover a set of constraints"
    )

    override val request = CoverageRequest::class.java
    override val main = CoverageMainResult::class.java
    override val detail = CoverageDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(CoverageComputation)
    override val computationFunction = runner::compute
}

internal object CoverageComputation :
    SingleComputation<CoverageRequest, CoverageMainResult, CoverageDetail, CoverageInternalResult>(NON_CACHING_USE_FF) {

    override fun allowedSliceTypes() = setOf(SliceTypeDO.SPLIT)

    override fun mergeInternalResult(
        existingResult: CoverageInternalResult?,
        newResult: CoverageInternalResult
    ) = if (existingResult == null) newResult else error("Only split slices are allowed, so this method should never be called")

    override fun computeForSlice(
        request: CoverageRequest,
        slice: Slice,
        info: TranslationInfo,
        model: PrlModel,
        f: FormulaFactory,
        status: ComputationStatusBuilder,
    ): CoverageInternalResult {
        val baseConstraints = info.propositions.map { it.formula() }.toMutableList()
        baseConstraints += request.additionalConstraints.mapNotNull { processConstraint(f, it, model, info, status)?.formula() }
        val (invalidConstraints, constraintsToCover) = computeConstraintsToCover(f, baseConstraints, request, model, info, slice, status)
            ?: return CoverageInternalResult(slice, emptyList(), 0)
        baseConstraints += constraintsToCover.values.map { it.first }

        val selectors = constraintsToCover.keys
        val baseFormula = f.and(baseConstraints)
        val initialCover = computeInitialCover(baseConstraints, selectors, f)
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
        return CoverageInternalResult(slice, covers, invalidConstraints)
    }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranslationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        f: FormulaFactory
    ) = error("details are always computed in main computation")

    private fun computeConstraintsToCover(
        f: FormulaFactory,
        baseConstraints: List<Formula>,
        request: CoverageRequest,
        model: PrlModel,
        info: TranslationInfo,
        slice: Slice,
        status: ComputationStatusBuilder,
    ): Pair<Int, Map<Variable, Pair<Formula, String>>>? {
        val baseSolver = SATSolver.newSolver(f)
        baseConstraints.forEach { baseSolver.add(it) }
        val validConstraints = mutableListOf<Pair<Formula, String>>()
        val invalidConstraints = mutableListOf<String>()
        for (constraint in request.constraintsToCover) {
            val formula = processConstraint(f, constraint, model, info, status)?.formula() ?: return null
            if (baseSolver.satCall().addFormulas(formula).sat() == Tristate.FALSE) {
                invalidConstraints.add(constraint)
            } else {
                validConstraints += formula to constraint
            }
        }

        if (invalidConstraints.isNotEmpty()) {
            status.addWarning("For slice $slice the following constraints were not buildable and are therefore not covered by the result: ${invalidConstraints.joinToString()}")
        }

        val constraintsToCover = mutableMapOf<Variable, Pair<Formula, String>>()
        // k nicht baubare Constraints = (n-1)+(n-2)+...+(n-k) nicht baubare Kombinationen = k*n-(k*(k+1)/2)
        var numInvalidCombinations: Int
        if (request.pairwiseCover) {
            numInvalidCombinations = invalidConstraints.size * request.constraintsToCover.size - (invalidConstraints.size * (invalidConstraints.size + 1)) / 2
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
                        constraintsToCover[selector] = f.equivalence(selector, combination) to "[$constraintI, $constraintJ]"
                        index += 1
                    }
                }
            }
            if (invalidCombinations.isNotEmpty()) {
                status.addWarning("For slice $slice the following constraint-combinations were not buildable and are therefore not covered by the result: $invalidCombinations")
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

    private fun computeInitialCover(
        baseConstraints: List<Formula>,
        variablesToCover: Set<Variable>,
        f: FormulaFactory
    ): Int {
        val solver = SATSolver.newSolver(f)
        solver.add(baseConstraints)
        val remainingSelectors = variablesToCover.toMutableSet()
        var initialCoverSize = 0
        while (remainingSelectors.isNotEmpty()) {
            initialCoverSize += 1
            val maxSelectors = solver.execute(OptimizationFunction.maximize(remainingSelectors)).positiveVariables()
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
        val selectors = sortedSetOf<Variable>()
        val selectorConstraints = mutableListOf<Formula>()
        for (i in 0 until initialCover) {
            val substitution = Substitution()
            for (variable in baseFormula.variables(f)) {
                substitution.addMapping(variable, variableCopy(variable, i, f))
            }
            val baseSubstituted = baseFormula.substitute(f, substitution)
            val selector = baseFormulaSelector(i, f)
            if (i > 0) {
                selectorConstraints.add(f.implication(selector, baseFormulaSelector(i - 1, f)))
            }
            val substitutedWithSelector = f.or(selector.negate(f), baseSubstituted)
            baseFormulaCopies.add(substitutedWithSelector)
            selectors.add(selector)
        }
        val coverageConstraints = variablesToCover.map { variable ->
            f.or((0 until initialCover).map { f.and(variableCopy(variable, it, f), baseFormulaSelector(it, f)) })
        }

        val oll = MaxSATSolver.oll(f)
        baseFormulaCopies.forEach(oll::addHardFormula)
        selectorConstraints.forEach(oll::addHardFormula)
        coverageConstraints.forEach(oll::addHardFormula)
        selectors.forEach { oll.addSoftFormula(it.negate(f), 1) }
        val ollResult = oll.solve()
        assert(ollResult == MaxSAT.MaxSATResult.OPTIMUM) { "OLL not optimal" }
        val fullModel = oll.model().positiveVariables()
        return fullModel
    }

}

data class CoverageInternalResult(
    override val slice: Slice,
    val result: List<CoveringConfiguration>,
    val uncoverableConstraints: Int
) : InternalResult<CoverageMainResult, CoverageDetail>(slice) {
    override fun extractMainResult() = CoverageMainResult(result.size, uncoverableConstraints)
    override fun extractDetails() = CoverageDetail(result)
    override fun toString(): String {
        return "CoverageInternalResult(slice=$slice, result=[${result.joinToString { "[$it]" }}])"
    }
}

private fun baseFormulaSelector(i: Int, f: FormulaFactory): Variable {
    return f.variable("BASE_COPY_USED_" + (i + 1))
}

private fun variableCopy(variable: Variable, i: Int, f: FormulaFactory): Variable {
    return f.variable(variable.name() + "_copy_" + (i + 1))
}
