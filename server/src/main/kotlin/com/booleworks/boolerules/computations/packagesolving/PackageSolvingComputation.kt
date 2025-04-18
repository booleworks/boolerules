// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.booleworks.boolerules.computations.packagesolving

import com.booleworks.boolerules.computations.ComputationType
import com.booleworks.boolerules.computations.NoComputationDetail
import com.booleworks.boolerules.computations.NoElement
import com.booleworks.boolerules.computations.generic.ApiDocs
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.InternalResult
import com.booleworks.boolerules.computations.generic.NON_CACHING_USE_FF
import com.booleworks.boolerules.computations.generic.SingleComputation
import com.booleworks.boolerules.computations.generic.SingleComputationRunner
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.generic.computationDoc
import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.datastructures.Assignment
import com.booleworks.logicng.formulas.Formula
import com.booleworks.logicng.formulas.FormulaFactory
import com.booleworks.logicng.formulas.Variable
import com.booleworks.logicng.solvers.MaxSatSolver
import com.booleworks.logicng.solvers.maxsat.algorithms.MaxSatConfig
import com.booleworks.prl.model.PrlModel
import com.booleworks.prl.model.constraints.ComparisonOperator
import com.booleworks.prl.model.constraints.VersionPredicate
import com.booleworks.prl.model.slices.Slice
import com.booleworks.prl.transpiler.TranspilationInfo

val PACKAGESOLVING = object : ComputationType<
        PackageSolvingRequest,
        PackageSolvingResponse,
        PackageSolvingResult,
        NoComputationDetail,
        NoElement> {
    override val path = "packagesolving"
    override val docs: ApiDocs = computationDoc<PackageSolvingRequest, PackageSolvingResponse>(
        "Package Solving",
        "Solve the package update problem for a given installation",
        "Updates a given installation by adding, removing, and updating versioned features"
    )

    override val request = PackageSolvingRequest::class.java
    override val main = PackageSolvingResult::class.java
    override val detail = NoComputationDetail::class.java
    override val element = NoElement::class.java

    override val runner = SingleComputationRunner(PackageSolvingComputation)
    override val computationFunction = runner::compute
}

object PackageSolvingComputation :
    SingleComputation<PackageSolvingRequest, PackageSolvingResult, NoComputationDetail, PackageSolvingInternalResult>(
        NON_CACHING_USE_FF
    ) {

    override fun allowedSliceTypes() = setOf(SliceTypeDO.SPLIT)

    override fun mergeInternalResult(
        existingResult: PackageSolvingInternalResult?,
        newResult: PackageSolvingInternalResult
    ) = if (existingResult == null) {
        newResult
    } else {
        error("Only split slices are allowed, so this method should never be called")
    }

    override fun computeDetailForSlice(
        slice: Slice,
        model: PrlModel,
        info: TranspilationInfo,
        additionalConstraints: List<String>,
        splitProperties: Set<String>,
        cf: CspFactory
    ) = error("details are always computed in main computation")

    override fun computeForSlice(
        request: PackageSolvingRequest,
        slice: Slice,
        info: TranspilationInfo,
        model: PrlModel,
        cf: CspFactory,
        status: ComputationStatusBuilder
    ): PackageSolvingInternalResult {
        val f = cf.formulaFactory
        val req = initialize(request, info, cf, status)
            ?: return PackageSolvingInternalResult(slice, emptyList(), emptyList(), emptyList())

        val solver = maxSat(MaxSatConfig.CONFIG_OLL, f, info)
        return if (!request.update) {
            handleAddRemove(f, req, solver, info, slice, status)
        } else {
            handleUpgrade(f, req, solver, info, slice, status)
        }
    }
}

private fun handleAddRemove(
    f: FormulaFactory,
    req: InternalRequest,
    solver: MaxSatSolver,
    info: TranspilationInfo,
    slice: Slice,
    status: ComputationStatusBuilder
): PackageSolvingInternalResult {
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
    val solverResult = solver.solve()
    if (solverResult.isSatisfiable) {
        return extractResult(f, slice, req, info, solverResult.model.toAssignment())
    } else {
        status.addWarning("installation request for slice $slice is not satisfiable -> skipping")
        return PackageSolvingInternalResult(slice, listOf(), listOf(), listOf())
    }
}

private fun handleUpgrade(
    f: FormulaFactory,
    req: InternalRequest,
    solver: MaxSatSolver,
    info: TranspilationInfo,
    slice: Slice,
    status: ComputationStatusBuilder
): PackageSolvingInternalResult {
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
    val solverResult = solver.solve()
    if (solverResult.isSatisfiable) {
        return extractResult(f, slice, req, info, solverResult.model.toAssignment())
    } else {
        status.addWarning("installation request for slice $slice is not satisfiable -> skipping")
        return PackageSolvingInternalResult(slice, listOf(), listOf(), listOf())
    }
}

private fun extractResult(
    f: FormulaFactory,
    slice: Slice,
    req: InternalRequest,
    info: TranspilationInfo,
    model: Assignment
): PackageSolvingInternalResult {
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
            new.add(VersionedFeature(feature.name, 0, version))
        } else {
            if (found != version) {
                changed.add(VersionedFeature(feature.name, found, version))
            }
        }
        allInstalled.add(feature)
    }
    req.softInstallation.keys
        .filter { it !in allInstalled }
        .forEach { removed.add(VersionedFeature(it.name, req.softInstallation[it]!!, 0)) }
    return PackageSolvingInternalResult(slice, removed, new, changed)
}

private fun initialize(
    request: PackageSolvingRequest,
    info: TranspilationInfo,
    cf: CspFactory,
    status: ComputationStatusBuilder
): InternalRequest? {
    val res = InternalRequest()
    for (installed in request.currentInstallation) {
        val c = info.translateConstraint(cf.formulaFactory, installed)
        if (c != null) {
            if (c.first is VersionPredicate && (c.first as VersionPredicate).comparison == ComparisonOperator.EQ) {
                val p = c.first as VersionPredicate
                res.softInstallation[cf.formulaFactory.variable(p.feature.featureCode)] = p.version
            } else {
                res.hardInstallation.add(c.second)
            }
        } else {
            status.addError("Could not parse the installed feature $installed")
            return null
        }
    }
    var ok = true
    ok = ok && parseVersionPredicates(request.install, res.install, info, cf, status)
    ok = ok && parseVersionPredicates(request.remove, res.remove, info, cf, status)
    if (!ok) {
        return null
    }
    return res
}

private fun parseVersionPredicates(
    input: List<String>,
    result: MutableMap<Variable, Int>,
    info: TranspilationInfo,
    cf: CspFactory,
    status: ComputationStatusBuilder
): Boolean {
    for (i in input) {
        val vf = info.translateConstraint(cf.formulaFactory, i)
        if (vf == null) {
            status.addError("Could not parse the constraint $i")
            return false
        } else if (vf.first !is VersionPredicate || (vf.first as VersionPredicate).comparison != ComparisonOperator.EQ) {
            status.addError("Constraint must be a feature[=x] version predicate, '$i' is not")
            return false
        } else {
            val p = vf.first as VersionPredicate
            result[cf.formulaFactory.variable(p.feature.featureCode)] = p.version
        }
    }
    return true
}

private fun vv(info: TranspilationInfo, p: Variable, v: Int): Variable = info.versionMapping[p]!![v]!!

data class PackageSolvingInternalResult(
    override val slice: Slice,
    val removedFeatures: List<VersionedFeature>,
    val newFeatures: List<VersionedFeature>,
    val changedFeatures: List<VersionedFeature>,
) : InternalResult<PackageSolvingResult, NoComputationDetail>(slice) {
    override fun extractMainResult() = PackageSolvingResult(removedFeatures, newFeatures, changedFeatures)
    override fun extractDetails() = NoComputationDetail
}

data class InternalRequest(
    val softInstallation: MutableMap<Variable, Int> = mutableMapOf(),
    val hardInstallation: MutableList<Formula> = mutableListOf(),
    val install: MutableMap<Variable, Int> = mutableMapOf(),
    val remove: MutableMap<Variable, Int> = mutableMapOf(),
    val update: MutableMap<Variable, Int> = mutableMapOf(),
)
