package com.booleworks.boolerules.algorithms.optimization

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.NON_CACHING_USE_FF
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Substitution
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo

data class CoverableConstraints(
    val numberOfConfigurations: Int,
    val maxCoverableConstraints: Int,
)

data class CoverageGraphResult(
    override val slice: Slice,
    override val state: ComputationState,
    val coverableConstraints: List<CoverableConstraints>
) : SliceResult

class CoverageGraph(
    private val maxConfigurations: Int,
    private val cover: List<String>,
    private val pairwise: Boolean = false
) : Algorithm<CoverageGraphResult> {

    override fun ffProvider() = NON_CACHING_USE_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): CoverageGraphResult {
        // TODO handler
        val (baseFormula, constraintsToCover, _) = initializeCoverage(info, cf, cover, pairwise)!!
        val results = (1..maxConfigurations).map {
            CoverableConstraints(it, computeMaxCover(baseFormula, it, constraintsToCover.keys, cf.formulaFactory()))
        }
        return CoverageGraphResult(slice, ComputationState.success(), results)
    }

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(existingResult: CoverageGraphResult?, newResult: CoverageGraphResult) = newResult

    private fun computeMaxCover(
        baseFormula: Formula,
        numCopies: Int,
        variablesToCover: Set<Variable>,
        f: FormulaFactory
    ): Int {
        val baseFormulaCopies = mutableListOf<Formula>()
        for (i in 0 until numCopies) {
            val substitution = Substitution()
            for (variable in baseFormula.variables(f)) {
                substitution.addMapping(variable, variableCopy(variable, i, f))
            }
            val baseSubstituted = baseFormula.substitute(f, substitution)
            baseFormulaCopies.add(baseSubstituted)
        }
        val coverageConstraints = variablesToCover.map { variable ->
            f.or((0 until numCopies).map { variableCopy(variable, it, f) })
        }

        val oll = MaxSATSolver.oll(f)
        baseFormulaCopies.forEach(oll::addHardFormula)
        coverageConstraints.forEach { oll.addSoftFormula(it, 1) }
        val ollResult = oll.solve()
        assert(ollResult == MaxSAT.MaxSATResult.OPTIMUM) { "OLL not optimal" }
        val fullModel = oll.model()
        return coverageConstraints.count { it.evaluate(fullModel) }
    }
}
