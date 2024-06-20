package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.language.PrlLexerAdapter
import com.boolerules.prl.plugin.language.PrlNamedElement
import com.boolerules.prl.plugin.psi.FeatureDef
import com.boolerules.prl.plugin.psi.FeatureDefinition
import com.boolerules.prl.plugin.psi.PrlTokenSets
import com.boolerules.prl.plugin.psi.SlicingPropertyDef
import com.boolerules.prl.plugin.psi.SlicingPropertyDefinition
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely

class PrlFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner() = DefaultWordsScanner(PrlLexerAdapter(), PrlTokenSets.IDENTIFIERS, PrlTokenSets.COMMENTS, PrlTokenSets.LITERALS)

    override fun canFindUsagesFor(psiElement: PsiElement) = psiElement is PrlNamedElement

    override fun getHelpId(psiElement: PsiElement) = null

    override fun getType(element: PsiElement): String = when (element) {
        is SlicingPropertyDef -> "property"
        is FeatureDef -> "feature"
        else -> error("Unexpected Find Usages type")
    }

    override fun getDescriptiveName(element: PsiElement): String = when (element) {
        is SlicingPropertyDef -> element.text
        is FeatureDef -> element.text
        else -> error("Unexpected Find Usages type")
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = when (element) {
        is SlicingPropertyDef -> (element.parent.asSafely<SlicingPropertyDefinition>()?.slicingPropertyType?.let { "${it.text} " } ?: "") + element.text
        is FeatureDef -> element.text +
                (element.parent.asSafely<FeatureDefinition>()?.featureBody?.featureBodyContentList?.find { it.elementDescription != null }?.let { " (${it.text})" } ?: "")

        else -> error("Unexpected Find Usages type")
    }
}
