package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.language.PrlFileType
import com.boolerules.prl.plugin.language.PrlLanguage
import com.boolerules.prl.plugin.language.PrlLexerAdapter
import com.boolerules.prl.plugin.psi.PrlTypes.ADD
import com.boolerules.prl.plugin.psi.PrlTypes.AMO
import com.boolerules.prl.plugin.psi.PrlTypes.AND
import com.boolerules.prl.plugin.psi.PrlTypes.BOOL
import com.boolerules.prl.plugin.psi.PrlTypes.BTCK_IDENTIFIER
import com.boolerules.prl.plugin.psi.PrlTypes.COMMA
import com.boolerules.prl.plugin.psi.PrlTypes.COMMENT
import com.boolerules.prl.plugin.psi.PrlTypes.CONTAINS
import com.boolerules.prl.plugin.psi.PrlTypes.DATE
import com.boolerules.prl.plugin.psi.PrlTypes.DATE_VAL
import com.boolerules.prl.plugin.psi.PrlTypes.DESCRIPTION
import com.boolerules.prl.plugin.psi.PrlTypes.ELSE
import com.boolerules.prl.plugin.psi.PrlTypes.ENUM
import com.boolerules.prl.plugin.psi.PrlTypes.EQ
import com.boolerules.prl.plugin.psi.PrlTypes.EQUIV
import com.boolerules.prl.plugin.psi.PrlTypes.EXO
import com.boolerules.prl.plugin.psi.PrlTypes.FALSE
import com.boolerules.prl.plugin.psi.PrlTypes.FEAT
import com.boolerules.prl.plugin.psi.PrlTypes.FORBIDDEN
import com.boolerules.prl.plugin.psi.PrlTypes.GE
import com.boolerules.prl.plugin.psi.PrlTypes.GROUP
import com.boolerules.prl.plugin.psi.PrlTypes.GT
import com.boolerules.prl.plugin.psi.PrlTypes.HEADER
import com.boolerules.prl.plugin.psi.PrlTypes.ID
import com.boolerules.prl.plugin.psi.PrlTypes.IDENT
import com.boolerules.prl.plugin.psi.PrlTypes.IF
import com.boolerules.prl.plugin.psi.PrlTypes.IMPL
import com.boolerules.prl.plugin.psi.PrlTypes.IN
import com.boolerules.prl.plugin.psi.PrlTypes.INT
import com.boolerules.prl.plugin.psi.PrlTypes.IS
import com.boolerules.prl.plugin.psi.PrlTypes.LBRA
import com.boolerules.prl.plugin.psi.PrlTypes.LE
import com.boolerules.prl.plugin.psi.PrlTypes.LPAR
import com.boolerules.prl.plugin.psi.PrlTypes.LSQB
import com.boolerules.prl.plugin.psi.PrlTypes.LT
import com.boolerules.prl.plugin.psi.PrlTypes.MANDATORY
import com.boolerules.prl.plugin.psi.PrlTypes.MUL
import com.boolerules.prl.plugin.psi.PrlTypes.NE
import com.boolerules.prl.plugin.psi.PrlTypes.NOT_MINUS
import com.boolerules.prl.plugin.psi.PrlTypes.NUMBER_VAL
import com.boolerules.prl.plugin.psi.PrlTypes.OPTIONAL
import com.boolerules.prl.plugin.psi.PrlTypes.OR
import com.boolerules.prl.plugin.psi.PrlTypes.PRL_VERSION
import com.boolerules.prl.plugin.psi.PrlTypes.PROPERTIES
import com.boolerules.prl.plugin.psi.PrlTypes.QUOTED
import com.boolerules.prl.plugin.psi.PrlTypes.RBRA
import com.boolerules.prl.plugin.psi.PrlTypes.RPAR
import com.boolerules.prl.plugin.psi.PrlTypes.RSQB
import com.boolerules.prl.plugin.psi.PrlTypes.RULE
import com.boolerules.prl.plugin.psi.PrlTypes.SLICING
import com.boolerules.prl.plugin.psi.PrlTypes.THEN
import com.boolerules.prl.plugin.psi.PrlTypes.THEN_NOT
import com.boolerules.prl.plugin.psi.PrlTypes.TRUE
import com.boolerules.prl.plugin.psi.PrlTypes.VERSIONED
import com.boolerules.prl.plugin.psi.Property
import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.RainbowColorSettingsPage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import javax.swing.Icon

class PrlSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = PrlSyntaxHighlighter()
}

class PrlSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return PrlLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            SLICING, PROPERTIES, HEADER, PRL_VERSION, FEAT,
            VERSIONED, RULE, IF, THEN, THEN_NOT, ELSE, OPTIONAL, GROUP, IS, IN, CONTAINS, FORBIDDEN, MANDATORY,
            BOOL, INT, DATE, ENUM, ID, DESCRIPTION -> arrayOf(PrlSyntaxGroup.KEYWORD.attributesKey)

            EQ, NOT_MINUS, AND, OR, IMPL, EQUIV, AMO, EXO,
            COMMA, ADD, MUL, NE, LT, LE, GT, GE -> arrayOf(PrlSyntaxGroup.OPERATORS.attributesKey)

            IDENT, BTCK_IDENTIFIER -> arrayOf(PrlSyntaxGroup.ID.attributesKey)
            LBRA, RBRA -> arrayOf(PrlSyntaxGroup.BRACES.attributesKey)
            LSQB, RSQB -> arrayOf(PrlSyntaxGroup.BRACKETS.attributesKey)
            LPAR, RPAR -> arrayOf(PrlSyntaxGroup.PARENTHESES.attributesKey)
            QUOTED, DATE_VAL -> arrayOf(PrlSyntaxGroup.STRINGLIT.attributesKey)
            NUMBER_VAL -> arrayOf(PrlSyntaxGroup.NUMBER.attributesKey)
            TRUE, FALSE -> arrayOf(PrlSyntaxGroup.CONSTANT.attributesKey)
            COMMENT -> arrayOf(PrlSyntaxGroup.COMMENT.attributesKey)
            else -> arrayOf()
        }
    }

    enum class PrlSyntaxGroup(val colorSettingsName: String, val attributesKey: TextAttributesKey) {
        KEYWORD("Keywords", TextAttributesKey.createTextAttributesKey("PRL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)),
        ID("Identifier", TextAttributesKey.createTextAttributesKey("PRL_IDENTIFIER", DefaultLanguageHighlighterColors.INSTANCE_FIELD)),
        PARENTHESES("Parentheses", TextAttributesKey.createTextAttributesKey("PRL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)),
        BRACES("Braces", TextAttributesKey.createTextAttributesKey("PRL_BRACES", DefaultLanguageHighlighterColors.BRACES)),
        BRACKETS("Brackets", TextAttributesKey.createTextAttributesKey("PRL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)),
        STRINGLIT("String Literals", TextAttributesKey.createTextAttributesKey("PRL_STRINGLIT", DefaultLanguageHighlighterColors.STRING)),
        NUMBER("Numbers", TextAttributesKey.createTextAttributesKey("PRL_NUMBER", DefaultLanguageHighlighterColors.NUMBER)),
        PROPERTIES("Properties", TextAttributesKey.createTextAttributesKey("PRL_PROPERTIES", DefaultLanguageHighlighterColors.CLASS_REFERENCE)),
        OPERATORS("Operators", TextAttributesKey.createTextAttributesKey("PRL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)),
        CONSTANT("Boolean Constants", TextAttributesKey.createTextAttributesKey("PRL_CONSTANTS", DefaultLanguageHighlighterColors.CONSTANT)),
        COMMENT("Comments", TextAttributesKey.createTextAttributesKey("PRL_COMMENTS", DefaultLanguageHighlighterColors.LINE_COMMENT)),
    }
}

class PrlSemanticHighlighter : RainbowVisitor() {
    init {
        println("Rainbow Visitor created")
    }

    override fun suitableForFile(file: PsiFile): Boolean {
        println("Checking File type: ${file.fileType == PrlFileType}")
        return file.fileType == PrlFileType
    }

    override fun visit(element: PsiElement) {
        println("Visiting in Rainbow")
        if (element is Property) {
            val file = element.containingFile
            addInfo(getInfo(file, element.propertyRef, element.propertyRef.text, PrlSyntaxHighlighter.PrlSyntaxGroup.PROPERTIES.attributesKey))
            println("Found Property!")
        }
    }

    override fun clone() = PrlSemanticHighlighter()
}

class PrlColorSettingPage : RainbowColorSettingsPage {

    private val DESCRIPTORS = PrlSyntaxHighlighter.PrlSyntaxGroup.values()
        .map { AttributesDescriptor(it.colorSettingsName, it.attributesKey) }
        .toTypedArray()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = emptyArray()

    override fun getDisplayName(): String = "PRL"

    override fun getIcon(): Icon = Icons.PRL_FILE

    override fun getHighlighter(): SyntaxHighlighter = PrlSyntaxHighlighter()

    override fun getDemoText(): String = """
        header {
          prl_version 1.0
        } # some comment

        module com.boolerules {

          import com.booleworks.features

          feature hitch2
          feature rim1
          enum feature name ["text"]
          int feature version_1 [1-10]

          rule hitch2 & (rim1/rim3) & parking_sensors
          rule hitch2 & (rim1/rim3) & parking_sensors[=3]
          rule if hitch2 then (rim1/rim3) & parking_sensors
          rule if hitch2 thenNot (rim1/rim3) & parking_sensors[>=2]
          rule if hitch2 then (rim1/rim3) else parking_sensors
          rule hitch2 is (rim1/rim3) & parking_sensors

          optional group hitches contains [h1, h2, h3, h4]
          mandatory group hitches contains [h1, h2, h3, h4]

          rule [name = "text"] => [version > 3]
          rule [name != "not text"]
          rule [version_1 > version_2]
          rule [name in ["a", "b", "c"]]
          rule [version in [1-7]]
          rule [version in [1, 2, 3, 4, 5]]

          rule a <=> (b => -(c/d)) {
            id "4711"
            description "text description"
            version 4
            validFrom 2010-01-01
            validTo   2022-12-31

            text "text text text"
            releases ["R1", "R2", "R3"]
            active false
            cool true
          }

          rule amo[b1, b2, b3]
          rule exo[c1]
          rule amo[]

          rule [version_1 != version_2]
          rule [version_1 = version_2]

          rule forbidden feature bf1
          rule forbidden feature v[=2]
          rule forbidden feature ii = 2
          rule forbidden feature st = "te xt"

          rule mandatory feature bf1
          rule mandatory feature v[=2]
          rule mandatory feature ii = 2
          rule mandatory feature st = "te xt" {
            `range` [1]
          }
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

    override fun isRainbowType(type: TextAttributesKey?) = true

    override fun getLanguage() = PrlLanguage
}
