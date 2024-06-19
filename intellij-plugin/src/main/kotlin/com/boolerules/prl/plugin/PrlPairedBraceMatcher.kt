package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.PrlTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class PrlPairedBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            BracePair(PrlTypes.LPAR, PrlTypes.RPAR, true),
            BracePair(PrlTypes.LBRA, PrlTypes.RBRA, true),
            BracePair(PrlTypes.LSQB, PrlTypes.RSQB, true),
        )
    }
}
