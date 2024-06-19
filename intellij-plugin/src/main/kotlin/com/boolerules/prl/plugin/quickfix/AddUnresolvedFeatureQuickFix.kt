package com.boolerules.prl.plugin.quickfix

import com.boolerules.prl.plugin.psi.PrlFile
import com.boolerules.prl.plugin.psi.RuleFile
import com.boolerules.prl.plugin.psi.createPrlElements
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.childrenOfType

class AddUnresolvedFeatureQuickFix(private val feature: String) : BaseIntentionAction() {

    override fun getText() = "Add feature $feature"
    override fun getFamilyName() = "Add feature"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile) = true

    // TODO just takes the first module of the file, which might not be the correct one
    override fun invoke(project: Project, editor: Editor?, file: PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            val prlFile: PrlFile = (PsiManager.getInstance(project).findFile(file.virtualFile) as PrlFile?)!!
            WriteCommandAction.runWriteCommandAction(project) {
                val moduleNode = prlFile.childrenOfType<RuleFile>().first().moduleDefinitionList.first()
                val newChildren = createPrlElements(project, "feature $feature\n    ")
                val beforeNode = moduleNode.ruleDefList.first().node
                newChildren.forEach { moduleNode.node.addChild(it.node, beforeNode) }
                (newChildren[1].navigationElement as Navigatable).navigate(true)
                FileEditorManager.getInstance(project).selectedTextEditor!!.caretModel
                    .moveCaretRelatively(1, 0, false, false, false)
            }
        }
    }
}
