package com.boolerules.prl.plugin.psi

import com.boolerules.prl.plugin.language.PrlFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory

private fun createPrlDummyFile(project: Project, text: String): PsiFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.rulez", PrlFileType, text)

fun createPrlElement(project: Project, name: String): PsiElement = createPrlDummyFile(project, name).firstChild

fun createPrlElements(project: Project, text: String): Array<out PsiElement> = createPrlDummyFile(project, text).children
