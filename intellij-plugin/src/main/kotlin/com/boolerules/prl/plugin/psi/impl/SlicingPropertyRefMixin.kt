package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.psi.PrlASTWrapperPsiElement
import com.boolerules.prl.plugin.psi.RuleFile
import com.boolerules.prl.plugin.psi.SlicingPropertyDefinition
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.findParentOfType

abstract class SlicingPropertyRefMixin(node: ASTNode) : PrlASTWrapperPsiElement(node), PsiReference, PsiElement {

    override fun getElement(): PsiElement = node.psi

    override fun getRangeInElement(): TextRange {
        return TextRange(0, text.length)
    }

    override fun resolve(): SlicingPropertyDefinition? =
        element.findParentOfType<RuleFile>()?.slicingProperties?.slicingPropertyDefinitionList.orEmpty().find { it.slicingPropertyDef?.text == this.text }

    override fun getCanonicalText(): String {
        // TODO
        return text
    }

    override fun handleElementRename(newElementName: String): PsiElement = renameIdentifier(this, newElementName)

    override fun bindToElement(element: PsiElement): PsiElement {
        TODO("not yet implemented")
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return resolve()?.slicingPropertyDef == element
    }

    override fun isSoft() = false
}
