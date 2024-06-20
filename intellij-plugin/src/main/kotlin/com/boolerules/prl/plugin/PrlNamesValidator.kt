package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.FeatureDef
import com.boolerules.prl.plugin.psi.PrlTokenSets
import com.boolerules.prl.plugin.psi.SlicingPropertyDef
import com.boolerules.prl.plugin.psi.impl.ResolveUtil
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext

class PrlNamesValidator : NamesValidator {
    override fun isKeyword(name: String, project: Project?): Boolean = name in prlKeywordSet

    override fun isIdentifier(name: String, project: Project?): Boolean = name.matches("[A-Za-z_][A-Za-z0-9_\\-.]*".toRegex())
}

class PrlRenameInputValidator : RenameInputValidator {
    override fun getPattern(): ElementPattern<out PsiElement> {
        return PrlElementPattern()
    }

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean = when (element) {
        is FeatureDef -> ResolveUtil.resolveFeaturesAndGroupsOfFile(element).none { it.getFeatureDef().text == newName }
        is SlicingPropertyDef -> ResolveUtil.resolveSlicingPropertiesOfFile(element).none { it.slicingPropertyDef?.text == newName }
        else -> true
    }
}

private val prlKeywordSet = PrlTokenSets.KEYWORDS.types.map { it.debugName }.toString()

class PrlElementPattern : PsiElementPattern<PsiElement, PrlElementPattern>(PsiElement::class.java) {}
