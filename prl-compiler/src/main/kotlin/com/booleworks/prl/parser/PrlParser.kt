// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

package com.booleworks.prl.parser

import com.booleworks.prl.parser.internal.FeatureFactory
import com.booleworks.prl.parser.internal.PragmaticRuleLanguageLexer
import com.booleworks.prl.parser.internal.PragmaticRuleLanguageParser
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.DefaultErrorStrategy
import org.antlr.v4.runtime.LexerNoViableAltException
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.ParseCancellationException

object PrlParser {
    fun prepareParser(charStream: CharStream): PragmaticRuleLanguageParser {
        val lexer = InternalLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ExtendedErrorListener.get())
        val tokens = CommonTokenStream(lexer)
        val parser = PragmaticRuleLanguageParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ExtendedErrorListener.get())
        parser.errorHandler = DefaultErrorStrategy()
        parser.buildParseTree = false
        parser.setFeatureFactory(FeatureFactory())
        return parser
    }
}

private class InternalLexer(input: CharStream?) : PragmaticRuleLanguageLexer(input) {
    override fun recover(exception: LexerNoViableAltException) {
        throw LexerException(exception.message)
    }
}

class LexerException(override val message: String?) : RuntimeException(message)

private class ExtendedErrorListener : BaseErrorListener() {
    companion object {
        private val INSTANCE = ExtendedErrorListener()
        internal fun get() = INSTANCE
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        throw ParseCancellationException("line $line:$charPositionInLine $msg")
    }
}

