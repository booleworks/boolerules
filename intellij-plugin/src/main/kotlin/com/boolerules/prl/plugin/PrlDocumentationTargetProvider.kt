package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.FeatureDefinition
import com.boolerules.prl.plugin.psi.ModuleRef
import com.boolerules.prl.plugin.psi.PropertyRef
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement

class PrlDocumentationTargetProvider : PsiDocumentationTargetProvider {

    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        return when (element) {
            is FeatureDefinition -> FeatureDefinitionDocumentationTarget(element)
            is PropertyRef       -> null//element.resolve()?.let { PrlDocumentationTarget(it) }
            is ModuleRef         -> null//element.resolve()?.let { PrlDocumentationTarget(it) }
            else                 -> null
        }
    }

}

@Suppress("UnstableApiUsage")
class FeatureDefinitionDocumentationTarget(feature: FeatureDefinition) : DocumentationTarget {

    private val featureName = feature.getFeatureDef().text
    private val description = feature.featureBody?.featureBodyContentList.orEmpty().find { it.elementDescription != null }?.elementDescription?.quotedString?.text
    private val properties =
        feature.featureBody?.featureBodyContentList.orEmpty().filter { it.property != null }.map { it.property!!.propertyRef.text + " = " + it.lastChild.text } // TODO fix

    override fun computePresentation(): TargetPresentation {
        // TODO this does not work yet
        return TargetPresentation.builder("Test Documentation").containerText("$description<br>  ${properties.joinToString("<br>  ")}").presentation()
    }

    override fun computeDocumentationHint(): String {
        return "Feature <b>$featureName</b>: $description<br><ul>${properties.joinToString("") { "<li>$it</li>" }}</ul>"
    }

    override fun createPointer(): Pointer<out DocumentationTarget> {
        return Pointer.hardPointer(this)
    }
}
