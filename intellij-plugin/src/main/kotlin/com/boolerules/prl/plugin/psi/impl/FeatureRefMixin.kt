package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition
import com.boolerules.prl.plugin.psi.PrlASTWrapperPsiElement
import com.boolerules.prl.plugin.psi.RuleFile
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.findParentOfType

abstract class FeatureRefMixin(node: ASTNode) : PrlASTWrapperPsiElement(node), PsiReference, PsiElement {

    private fun myName(): String = text.substringAfterLast(".")

    override fun getElement(): PsiElement = node.psi

    override fun getRangeInElement(): TextRange {
        return TextRange(0, text.length)
    }

    override fun resolve(): FeatureOrGroupDefinition? {
        val root = element.findParentOfType<RuleFile>() ?: return null
        val name = myName()
        val declarations = (root.featureDefinitionList + root.groupDefinitionList).filter { it.getFeatureDef().text == name }
        return declarations.firstOrNull()
    }

    override fun getCanonicalText(): String {
        // TODO
        return text
    }

    override fun handleElementRename(newElementName: String): PsiElement = renameIdentifier(this, newElementName)

    override fun bindToElement(element: PsiElement): PsiElement {
        TODO("not yet implemented")
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return resolve()?.getFeatureDef() == element
    }

    override fun isSoft() = false
}
