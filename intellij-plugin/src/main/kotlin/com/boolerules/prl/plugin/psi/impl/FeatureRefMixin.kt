package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition
import com.boolerules.prl.plugin.psi.ModuleDefinition
import com.boolerules.prl.plugin.psi.PrlASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.findParentOfType

abstract class FeatureRefMixin(node: ASTNode) : PrlASTWrapperPsiElement(node), PsiReference, PsiElement {

    private fun myModule(): String? = text.substringBeforeLast(".", "").takeIf { it.isNotBlank() }

    private fun myName(): String = text.substringAfterLast(".")

    override fun getElement(): PsiElement = node.psi

    override fun getRangeInElement(): TextRange {
        return TextRange(0, text.length)
    }

    override fun resolve(): FeatureOrGroupDefinition? {
        val thisModule = element.findParentOfType<ModuleDefinition>() ?: return null
        val requiredModule = myModule()
        val name = myName()
        val modulesToSearch = ResolveUtil.resolveModules(requiredModule, thisModule) + ResolveUtil.resolveImports(thisModule)
        val declarations = modulesToSearch.flatMap { it.featureDefinitionList + it.groupDefinitionList }.filter { it.getFeatureDef().text == name }
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
