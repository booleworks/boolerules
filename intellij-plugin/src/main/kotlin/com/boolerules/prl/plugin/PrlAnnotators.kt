package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.FeatureBody
import com.boolerules.prl.plugin.psi.FeatureDefinition
import com.boolerules.prl.plugin.psi.FeatureRef
import com.boolerules.prl.plugin.psi.GroupDefinition
import com.boolerules.prl.plugin.psi.HeaderContent
import com.boolerules.prl.plugin.psi.RuleBody
import com.boolerules.prl.plugin.psi.RuleDef
import com.boolerules.prl.plugin.psi.SlicingProperties
import com.boolerules.prl.plugin.quickfix.AddUnresolvedFeatureQuickFix
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class PrlInvalidReferenceAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is FeatureRef -> element.resolve() ?: holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved feature reference").withFix(AddUnresolvedFeatureQuickFix(element.text))
                .create()
        }
    }
}

class PrlDuplicatePropertyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is HeaderContent     -> element.headerPropertyList.map { it.headerPropertyName.text }.findDuplicates().forEach {
                holder.newAnnotation(HighlightSeverity.WARNING, "Duplicate Property Definition: $it").create()
            }

            is SlicingProperties -> element.slicingPropertyDefinitionList.mapNotNull { it.slicingPropertyDef?.name }.findDuplicates().forEach {
                holder.newAnnotation(HighlightSeverity.WARNING, "Duplicate Property Definition: $it").create()
            }

            is GroupDefinition   -> element.ruleBody?.annotateDuplicateProperties(holder)
            is RuleDef           -> element.ruleBody?.annotateDuplicateProperties(holder)
            is FeatureDefinition -> element.featureBody?.annotateDuplicateProperties(holder)
        }
    }
}

private fun <E> Collection<E>.findDuplicates(): List<E> = this.groupBy { it }.filter { it.value.size > 1 }.map { it.key }

private fun RuleBody.annotateDuplicateProperties(holder: AnnotationHolder) {
    ruleBodyContentList.mapNotNull { it.ruleId?.let { "id" } ?: it.elementDescription?.let { "description" } ?: it.property?.propertyRef?.text }
        .findDuplicates().forEach {
            holder.newAnnotation(HighlightSeverity.WARNING, "Duplicate Property Definition: $it").create()
        }
}

private fun FeatureBody.annotateDuplicateProperties(holder: AnnotationHolder) {
    featureBodyContentList.mapNotNull { it.elementDescription?.let { "description" } ?: it.property?.propertyRef?.text }
        .findDuplicates().forEach {
            holder.newAnnotation(HighlightSeverity.WARNING, "Duplicate Property Definition: $it").create()
        }
}
