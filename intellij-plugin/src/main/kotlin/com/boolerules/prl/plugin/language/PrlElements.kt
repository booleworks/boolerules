package com.boolerules.prl.plugin.language

import com.boolerules.prl.plugin.psi.FeatureDef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope

interface FeatureOrGroupDefinition: PsiElement {
    fun getFeatureDef(): FeatureDef
}

interface PrlNamedElement : PsiNameIdentifierOwner

interface PrlReference : PsiReference, PsiElement
