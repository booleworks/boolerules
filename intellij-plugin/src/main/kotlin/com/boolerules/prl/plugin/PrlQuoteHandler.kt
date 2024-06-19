package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.PrlTypes
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class PrlQuoteHandler : SimpleTokenSetQuoteHandler(PrlTypes.QUOTED)
