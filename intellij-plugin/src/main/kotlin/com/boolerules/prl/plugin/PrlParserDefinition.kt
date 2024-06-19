package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.language.PrlLanguage
import com.boolerules.prl.plugin.language.PrlLexerAdapter
import com.boolerules.prl.plugin.parser.PrlParser
import com.boolerules.prl.plugin.psi.PrlFile
import com.boolerules.prl.plugin.psi.PrlTokenSets
import com.boolerules.prl.plugin.psi.PrlTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType

val FILE = IFileElementType(PrlLanguage)

class PrlParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = PrlLexerAdapter()

    override fun createParser(project: Project?) = PrlParser()

    override fun getFileNodeType() = FILE

    override fun getCommentTokens() = PrlTokenSets.COMMENTS

    override fun getStringLiteralElements() = PrlTokenSets.STRING_LITS

    override fun createElement(node: ASTNode?): PsiElement = PrlTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider) = PrlFile(viewProvider)
}
