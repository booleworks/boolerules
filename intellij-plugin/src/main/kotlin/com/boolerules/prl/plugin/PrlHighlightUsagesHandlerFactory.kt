package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.FeatureDef
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.util.Consumer

class PrlHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactoryBase() {
    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile, target: PsiElement): HighlightUsagesHandlerBase<*> =
        object : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
            override fun getTargets(): List<PsiElement> = file.childrenOfType<FeatureDef>()

            override fun computeUsages(targets: MutableList<out PsiElement>) {
                TODO("Not yet implemented")
            }

            override fun selectTargets(targets: MutableList<out PsiElement>, selectionConsumer: Consumer<in MutableList<out PsiElement>>) {
                TODO("Not yet implemented")
            }
        }
}
