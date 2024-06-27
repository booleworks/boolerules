// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.algorithms.software

import com.booleworks.boolerules.algorithms.Algorithm
import com.booleworks.boolerules.algorithms.CACHING_IMPORT_FF
import com.booleworks.boolerules.algorithms.ComputationState
import com.booleworks.boolerules.algorithms.SliceResult
import com.booleworks.boolerules.datastructures.BRTimeoutHandler
import com.booleworks.boolerules.helpers.maxSATSolver
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Assignment
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.handlers.TimeoutHandler
import com.booleworks.logicng.handlers.TimeoutMaxSATHandler
import com.booleworks.logicng.solvers.MaxSATSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSAT
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSATConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.constraints.VersionPredicate
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.model.slices.SliceType
import com.booleworks.prl.transpiler.TranspilationInfo

data class VersionedFeature(
    val feature: String,
    val versionOld: Int,
    val versionNew: Int
)

data class PackageSolvingResult(
    override val slice: Slice,
    override val state: ComputationState,
    val removedFeatures: List<VersionedFeature>,
    val newFeatures: List<VersionedFeature>,
    val changedFeatures: List<VersionedFeature>
) : SliceResult

class PackageSolving(
    private val currentInstallation: List<String>,
    private val install: List<String>,
    private val remove: List<String>,
    private val update: Boolean
) : Algorithm<PackageSolvingResult> {
    override fun ffProvider() = CACHING_IMPORT_FF

    override fun allowedSliceTypes() = setOf(SliceType.SPLIT)

    override fun executeForSlice(
        cf: CspFactory,
        model: PrlModel,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): PackageSolvingResult {
        val f = cf.formulaFactory()
        val (req, error) = initialize(info, cf)
        if (error != null) {
            return PackageSolvingResult(slice, ComputationState.error(error), listOf(), listOf(), listOf())
        }

        val solver = maxSATSolver(MaxSATConfig.builder().build(), MaxSATSolver::oll, f, info)
        return if (!update) {
            handleAddRemove(f, req!!, solver, info, slice, timeoutHandler)
        } else {
            handleUpgrade(f, req!!, solver, info, slice, timeoutHandler)
        }
    }

    // ANY slices are not allowed, so no real merging is required here
    override fun mergeAnyResult(
        existingResult: PackageSolvingResult?,
        newResult: PackageSolvingResult
    ) = newResult

    private fun handleAddRemove(
        f: FormulaFactory,
        req: InternalRequest,
        solver: MaxSATSolver,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): PackageSolvingResult {
        req.hardInstallation.forEach { solver.addHardFormula(it) }
        req.install.forEach { (fea, ver) -> solver.addHardFormula(vv(info, fea, ver)) }
        req.remove.forEach { solver.addHardFormula(it.key.negate(f)) }
        val currentInstall = req.softInstallation.size
        info.versionMapping.forEach { (fea, vers) ->
            if (fea in req.softInstallation) {
                solver.addSoftFormula(fea, currentInstall * 3)
            } else {
                solver.addSoftFormula(fea.negate(f), currentInstall)
            }

            val aux = f.variable("@VER_AUX_$fea")
            vers.forEach { ver ->
                if (req.softInstallation[fea] != null && req.softInstallation[fea]!! == ver.key) {
                    solver.addHardFormula(f.or(aux.negate(f), ver.value))
                } else {
                    solver.addHardFormula(f.or(aux.negate(f), ver.value.negate(f)))
                }
            }
            solver.addSoftFormula(aux, 1)
        }
        return solve(req, solver, info, slice, timeoutHandler)
    }

    private fun handleUpgrade(
        f: FormulaFactory,
        req: InternalRequest,
        solver: MaxSATSolver,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): PackageSolvingResult {
        val currentInstall = req.softInstallation.size
        req.softInstallation.forEach { (fea, _) ->
            if (fea in req.softInstallation) {
                solver.addSoftFormula(fea, currentInstall)
            } else {
                solver.addSoftFormula(fea.negate(f), currentInstall)
            }

            val maxVer = info.versionMapping[fea]!!.lastKey()
            solver.addSoftFormula(vv(info, fea, maxVer), 1) // AUX => x in max version
        }
        return solve(req, solver, info, slice, timeoutHandler)
    }

    private fun extractResult(
        f: FormulaFactory,
        slice: Slice,
        req: InternalRequest,
        info: TranspilationInfo,
        model: Assignment
    ): PackageSolvingResult {
        val removed = mutableListOf<VersionedFeature>()
        val new = mutableListOf<VersionedFeature>()
        val changed = mutableListOf<VersionedFeature>()
        val allInstalled = mutableSetOf<Variable>()
        model.positiveVariables().filter { it in info.versionVariables }.forEach { verVar ->
            val feaVer = info.getFeatureAndVersion(verVar)!!
            val feature = f.variable(feaVer.first)
            val version = feaVer.second
            val found = req.softInstallation[feature]
            if (found == null) {
                new.add(VersionedFeature(feature.name(), 0, version))
            } else {
                if (found != version) {
                    changed.add(VersionedFeature(feature.name(), found, version))
                }
            }
            allInstalled.add(feature)
        }
        req.softInstallation.keys
            .filter { it !in allInstalled }
            .forEach { removed.add(VersionedFeature(it.name(), req.softInstallation[it]!!, 0)) }
        return PackageSolvingResult(slice, ComputationState.success(), removed, new, changed)
    }

    private fun initialize(
        info: TranspilationInfo,
        cf: CspFactory,
    ): Pair<InternalRequest?, String?> {
        val res = InternalRequest()
        for (installed in currentInstallation) {
            val c = info.translateConstraint(cf.formulaFactory(), installed)
            if (c != null) {
                if (c.first is VersionPredicate && (c.first as VersionPredicate).comparison == ComparisonOperator.EQ) {
                    val p = c.first as VersionPredicate
                    res.softInstallation[cf.formulaFactory().variable(p.feature.featureCode)] = p.version
                } else {
                    res.hardInstallation.add(c.second)
                }
            } else {
                return Pair(null, "Could not parse the installed feature $installed")
            }
        }
        var error = parseVersionPredicates(install, res.install, info, cf)
        if (error != null) {
            return Pair(null, error)
        }
        error = parseVersionPredicates(remove, res.remove, info, cf)
        if (error != null) {
            return Pair(null, error)
        }
        return Pair(res, null)
    }

    private fun parseVersionPredicates(
        input: List<String>,
        result: MutableMap<Variable, Int>,
        info: TranspilationInfo,
        cf: CspFactory,
    ): String? {
        for (i in input) {
            val vf = info.translateConstraint(cf.formulaFactory(), i)
            if (vf == null) {
                return "Could not parse the constraint $i"
            } else if (vf.first !is VersionPredicate || (vf.first as VersionPredicate).comparison != ComparisonOperator.EQ) {
                return "Constraint must be a feature[=x] version predicate, '$i' is not"
            } else {
                val p = vf.first as VersionPredicate
                result[cf.formulaFactory().variable(p.feature.featureCode)] = p.version
            }
        }
        return null
    }

    private fun vv(info: TranspilationInfo, p: Variable, v: Int): Variable = info.versionMapping[p]!![v]!!

    private fun solve(
        req: InternalRequest,
        solver: MaxSATSolver,
        info: TranspilationInfo,
        slice: Slice,
        timeoutHandler: BRTimeoutHandler
    ): PackageSolvingResult {
        val maxsatHandler = TimeoutMaxSATHandler(timeoutHandler.computationEnd, TimeoutHandler.TimerType.FIXED_END)
        val result = solver.solve(maxsatHandler)
        return when (result) {
            MaxSAT.MaxSATResult.OPTIMUM -> extractResult(solver.factory(), slice, req, info, solver.model())
            MaxSAT.MaxSATResult.UNDEF ->
                PackageSolvingResult(
                    slice, ComputationState.aborted(), listOf(), listOf(), listOf()
                )

            MaxSAT.MaxSATResult.UNSATISFIABLE -> {
                PackageSolvingResult(slice, ComputationState.notBuildable(), listOf(), listOf(), listOf())
            }
        }
    }

    data class InternalRequest(
        val softInstallation: MutableMap<Variable, Int> = mutableMapOf(),
        val hardInstallation: MutableList<Formula> = mutableListOf(),
        val install: MutableMap<Variable, Int> = mutableMapOf(),
        val remove: MutableMap<Variable, Int> = mutableMapOf(),
        val update: MutableMap<Variable, Int> = mutableMapOf(),
    )
}
