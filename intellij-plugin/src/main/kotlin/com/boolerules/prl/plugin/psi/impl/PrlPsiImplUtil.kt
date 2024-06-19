package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.psi.FeatureDef
import com.boolerules.prl.plugin.psi.FeatureDefinition
import com.boolerules.prl.plugin.psi.FeatureRef
import com.boolerules.prl.plugin.psi.ModuleDef
import com.boolerules.prl.plugin.psi.ModuleDefinition
import com.boolerules.prl.plugin.psi.ModuleRef
import com.boolerules.prl.plugin.psi.PrlTypes
import com.boolerules.prl.plugin.psi.PropertyRef
import com.boolerules.prl.plugin.psi.SlicingPropertyDef
import com.boolerules.prl.plugin.psi.createPrlElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings

object PrlPsiImplUtil {

    //<editor-fold> desc="ModuleDef"
    @JvmStatic
    fun getNameIdentifier(element: ModuleDef): PsiElement = element

    @JvmStatic
    fun getName(element: ModuleDef): String = element.text

    @JvmStatic
    fun setName(element: ModuleDef, newName: String): PsiElement = renameIdentifier(element, newName)
    //</editor-fold>

    //<editor-fold desc="SlicingPropertyDef">
    @JvmStatic
    fun getNameIdentifier(element: SlicingPropertyDef): PsiElement = element

    @JvmStatic
    fun getName(element: SlicingPropertyDef): String = element.text

    @JvmStatic
    fun setName(element: SlicingPropertyDef, newName: String): PsiElement = renameIdentifier(element, newName)
    //</editor-fold>

    //<editor-fold desc="FeatureDef">
    @JvmStatic
    fun getFeatureDef(element: FeatureDefinition): FeatureDef = element.versionedBoolFeatureRange?.featureDef
        ?: element.boolFeatureRange?.featureDef
        ?: element.enumFeatureRange?.featureDef
        ?: element.intFeatureRange?.featureDef
        ?: error("Unexpected feature type")

    @JvmStatic
    fun getName(element: FeatureDef): String = element.text

    @JvmStatic
    fun getFullName(element: FeatureDef): String = element.parentOfType<ModuleDefinition>()?.moduleDef?.text + element.text

    @JvmStatic
    fun setName(element: FeatureDef, newName: String): PsiElement = renameIdentifier(element, newName)

    @JvmStatic
    fun getNameIdentifier(element: FeatureDef): PsiElement = element
    //</editor-fold>

    //<editor-fold desc="ModuleRef">
    @JvmStatic
    fun getReferencedName(element: ModuleRef): List<String> = element.text.split(".")

    @JvmStatic
    fun getReference(element: ModuleRef): PsiReference = element

    @JvmStatic
    fun getVariants(element: ModuleRef): Array<Any> = ResolveUtil.resolveModulesOfFile(element).mapNotNull { it.moduleDef }.toTypedArray()
    //</editor-fold>

    //<editor-fold desc="FeatureRef">
    @JvmStatic
    fun getReferencedName(element: FeatureRef): List<String> = element.text.split(".")

    @JvmStatic
    fun getReference(element: FeatureRef): PsiReference = element

    @JvmStatic
    fun getVariants(element: FeatureRef): Array<Any> = ResolveUtil.resolveFeaturesAndGroupsOfModule(element).map { it.getFeatureDef() }.toTypedArray()
    //</editor-fold>

    //<editor-fold desc="PropertyRef">
    @JvmStatic
    fun getReference(element: PropertyRef): PsiReference = element

    @JvmStatic
    fun getVariants(element: PropertyRef): Array<Any> = ResolveUtil.resolveSlicingPropertiesOfFile(element).mapNotNull { it.slicingPropertyDef }.toTypedArray()
    //</editor-fold>
}

private fun PsiElement.resolveDottedName() = siblings(forward = false).filterNot { it.elementType == PrlTypes.DOT }.map { it.text }.toList().asReversed()

fun renameIdentifier(element: PsiElement, newName: String): PsiElement {
    val idNode = element.node.findChildByType(PrlTypes.IDENT)
    if (idNode != null) {
        val newElem = createPrlElement(element.project, newName)
        element.node.replaceChild(idNode, newElem.firstChild.node)
    } else {
//        val btckNode = element.node.findChildByType(PrlTypes.BTCK_IDENTIFIER)
        TODO("Rename refactoring for backtick identifiers not yet implemented")
    }
    return element
}
