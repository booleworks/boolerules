package com.boolerules.prl.plugin.psi.impl

import com.boolerules.prl.plugin.psi.FeatureDef
import com.boolerules.prl.plugin.psi.FeatureDefinition
import com.boolerules.prl.plugin.psi.FeatureRef
import com.boolerules.prl.plugin.psi.PrlTypes
import com.boolerules.prl.plugin.psi.PropertyRef
import com.boolerules.prl.plugin.psi.RuleFile
import com.boolerules.prl.plugin.psi.SlicingPropertyDef
import com.boolerules.prl.plugin.psi.createPrlElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.findParentOfType

object PrlPsiImplUtil {

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
    fun getFeatureDef(element: FeatureDefinition): FeatureDef =
        element.versionedBoolFeatureRange?.featureDef
            ?: element.boolFeatureRange?.featureDef
            ?: element.enumFeatureRange?.featureDef
            ?: element.intFeatureRange?.featureDef
            ?: error("Unexpected feature type: $element")

    @JvmStatic
    fun getName(element: FeatureDef): String = element.text.removeSurrounding("`")

    @JvmStatic
    fun setName(element: FeatureDef, newName: String): PsiElement = renameIdentifier(element, newName)

    @JvmStatic
    fun getNameIdentifier(element: FeatureDef): PsiElement = element
    //</editor-fold>

    //<editor-fold desc="FeatureRef">
    @JvmStatic
    fun getReference(element: FeatureRef): PsiReference = element

    @JvmStatic
    fun getVariants(element: FeatureRef): Array<Any> = element.findParentOfType<RuleFile>()?.featureDefinitionList?.map { it.getFeatureDef() }?.toTypedArray() ?: emptyArray()

    @JvmStatic
    fun getName(element: FeatureRef): String = element.text.removeSurrounding("`")

    @JvmStatic
    fun setName(element: FeatureRef, newName: String): PsiElement = renameIdentifier(element, newName)

    @JvmStatic
    fun getNameIdentifier(element: FeatureRef): PsiElement = element
    //</editor-fold>

    //<editor-fold desc="PropertyRef">
    @JvmStatic
    fun getReference(element: PropertyRef): PsiReference = element

    @JvmStatic
    fun getVariants(element: PropertyRef): Array<Any> = ResolveUtil.resolveSlicingPropertiesOfFile(element).mapNotNull { it.slicingPropertyDef }.toTypedArray()
    //</editor-fold>
}

fun renameIdentifier(element: PsiElement, newName: String): PsiElement {
    val idNode = element.node.findChildByType(PrlTypes.IDENT) ?: element.node.findChildByType(PrlTypes.BTCK_IDENTIFIER) ?: return element
    val newElem = createPrlElement(element.project, newName.addBackTicksIfNecessary())
    element.node.replaceChild(idNode, newElem.firstChild.node)
    return element
}

private fun String.addBackTicksIfNecessary() = if (matches("[A-Za-z_][A-Za-z0-9_\\-.]*".toRegex())) this else "`$this`"
