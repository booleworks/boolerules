package com.boolerules.prl.plugin.psi

import com.boolerules.prl.plugin.language.PrlLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class PrlTokenType(@NonNls debugName: String) : IElementType(debugName, PrlLanguage) {
    override fun toString(): String {
        return "PrlTokenType." + super.toString()
    }
}
