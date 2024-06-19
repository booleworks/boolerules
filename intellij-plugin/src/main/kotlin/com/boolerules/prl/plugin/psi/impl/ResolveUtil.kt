package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition
import com.boolerules.prl.plugin.psi.ModuleDefinition
import com.boolerules.prl.plugin.psi.RuleFile
import com.boolerules.prl.plugin.psi.SlicingPropertyDefinition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.findParentOfType

object ResolveUtil {
    fun resolveImports(thisModule: ModuleDefinition): List<ModuleDefinition> {
        val knownModules = resolveModulesOfFile(thisModule)
        return thisModule.importDefList.filterNot { it.moduleRef == null }.mapNotNull { import -> knownModules.find { it.moduleDef?.text == import.moduleRef?.text } }
    }

    fun resolveModules(requiredModule: String?, thisModule: ModuleDefinition): List<ModuleDefinition> {
        return when (requiredModule) {
            null                       -> resolveParentModules(thisModule)
            thisModule.moduleDef?.text -> listOf(thisModule)
            else                       -> resolveModulesOfFile(thisModule).filter { it.moduleDef?.text == requiredModule }
        }
    }

    private fun resolveParentModules(thisModule: ModuleDefinition): List<ModuleDefinition> = resolveModulesOfFile(thisModule)
        .filter { thisModule.moduleDef?.text?.contains(it.moduleDef?.text ?: "") != false }

    fun resolveModulesOfFile(element: PsiElement): List<ModuleDefinition> = element.findParentOfType<RuleFile>()?.moduleDefinitionList.orEmpty()

    fun resolveFeaturesAndGroupsOfModule(element: PsiElement): List<FeatureOrGroupDefinition> =
        element.findParentOfType<ModuleDefinition>()?.let { it.featureDefinitionList + it.groupDefinitionList } ?: emptyList()

    fun resolveSlicingPropertiesOfFile(element: PsiElement): List<SlicingPropertyDefinition> =
        element.findParentOfType<RuleFile>()?.slicingProperties?.slicingPropertyDefinitionList.orEmpty()
}
