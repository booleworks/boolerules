package com.boolerules.prl.plugin.psi

import com.boolerules.prl.plugin.language.PrlFileType
import com.boolerules.prl.plugin.language.PrlLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class PrlFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, PrlLanguage) {
    override fun getFileType() = PrlFileType
    override fun toString() = "PRL File"
}
