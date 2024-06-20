package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition
import com.boolerules.prl.plugin.psi.RuleFile
import com.boolerules.prl.plugin.psi.SlicingPropertyDefinition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.findParentOfType

object ResolveUtil {
    fun resolveFeaturesAndGroupsOfFile(element: PsiElement): List<FeatureOrGroupDefinition> =
        element.findParentOfType<RuleFile>()?.let { it.featureDefinitionList + it.groupDefinitionList } ?: emptyList()

    fun resolveSlicingPropertiesOfFile(element: PsiElement): List<SlicingPropertyDefinition> =
        element.findParentOfType<RuleFile>()?.slicingProperties?.slicingPropertyDefinitionList.orEmpty()
}
