package com.boolerules.prl.plugin.psi

import com.boolerules.prl.plugin.language.PrlLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class PrlElementType(@NonNls debugName: String) : IElementType(debugName, PrlLanguage)
