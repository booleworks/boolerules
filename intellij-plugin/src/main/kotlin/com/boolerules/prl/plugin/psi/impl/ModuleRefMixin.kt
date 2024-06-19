package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.psi.ModuleDefinition
import com.boolerules.prl.plugin.psi.PrlASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.findParentOfType

abstract class ModuleRefMixin(node: ASTNode) : PrlASTWrapperPsiElement(node), PsiReference, PsiElement {

    override fun getElement(): PsiElement = node.psi

    override fun getRangeInElement(): TextRange {
        return TextRange(0, text.length)
    }

    override fun resolve(): ModuleDefinition? {
        val thisModule = element.findParentOfType<ModuleDefinition>() ?: return null
        return ResolveUtil.resolveModules(text, thisModule).firstOrNull()
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
        return resolve()?.moduleDef == element
    }

    override fun isSoft() = false
}
