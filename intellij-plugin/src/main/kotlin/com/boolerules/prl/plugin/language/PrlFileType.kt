package com.boolerules.prl.plugin.language

import com.boolerules.prl.plugin.Icons
import com.intellij.openapi.fileTypes.LanguageFileType

object PrlFileType : LanguageFileType(PrlLanguage) {
    override fun getName() = "PRL File"

    override fun getDescription() = "Pragmatic Rule Language (PRL) file"

    override fun getDefaultExtension() = "prl"

    override fun getIcon() = Icons.PRL_FILE
}
