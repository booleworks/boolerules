package com.boolerules.prl.plugin.language

import com.intellij.lexer.FlexAdapter

class PrlLexerAdapter : FlexAdapter(PrlLexer(null))
