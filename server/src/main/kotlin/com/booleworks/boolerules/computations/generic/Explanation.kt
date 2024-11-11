// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.boolerules.computations.generic

import com.booleworks.logicng.csp.CspFactory
import com.booleworks.logicng.explanations.mus.MusGeneration
import com.booleworks.logicng.solvers.sat.SatCall
import com.booleworks.prl.model.AnySlicingPropertyDefinition
import com.booleworks.prl.model.rules.ConstraintRule
import com.booleworks.prl.transpiler.PrlProposition
import com.booleworks.prl.transpiler.RuleType

/**
 * Computes an explanation for an unsatisfiable SAT solver state and returns
 * a list of original rules of the rule file involved in the conflict.
 */
internal fun computeExplanation(
    satCall: SatCall,
    allDefinitions: List<AnySlicingPropertyDefinition>,
    cf: CspFactory
): List<RuleDO> {
    assert(satCall.satResult.result == false)
    val mus = MusGeneration().computeMus(cf.formulaFactory, satCall.unsatCore().propositions)
    return mus.propositions.filterIsInstance<PrlProposition>().map { p ->
        when (p.backpack.ruleType) {
            RuleType.ORIGINAL_RULE -> RuleDO.fromRulesetRule(p.backpack.rule!!, allDefinitions.map { it.name })
            RuleType.ADDITIONAL_RESTRICTION -> RuleDO.fromAdditionalConstraint(p.backpack.rule as ConstraintRule)
            else -> RuleDO.fromInternalRule(p.backpack.ruleType)
        }
    }
}
