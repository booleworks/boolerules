package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.language.PrlLanguage
import com.boolerules.prl.plugin.psi.PrlTokenSets
import com.boolerules.prl.plugin.psi.PrlTypes
import com.boolerules.prl.plugin.psi.PrlTypes.FEATURE_BODY_CONTENT
import com.boolerules.prl.plugin.psi.PrlTypes.FEATURE_DEFINITION
import com.boolerules.prl.plugin.psi.PrlTypes.GROUP_DEFINITION
import com.boolerules.prl.plugin.psi.PrlTypes.HEADER_CONTENT
import com.boolerules.prl.plugin.psi.PrlTypes.HEADER_DEF
import com.boolerules.prl.plugin.psi.PrlTypes.RULE_BODY_CONTENT
import com.boolerules.prl.plugin.psi.PrlTypes.RULE_DEF
import com.boolerules.prl.plugin.psi.PrlTypes.RULE_FILE
import com.boolerules.prl.plugin.psi.PrlTypes.SIMP
import com.boolerules.prl.plugin.psi.PrlTypes.SLICING_PROPERTIES
import com.boolerules.prl.plugin.psi.PrlTypes.SLICING_PROPERTY_DEFINITION
import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.impl.source.codeStyle.SemanticEditorPosition
import com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class PrlFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val prlCodeStyleSettings = formattingContext.codeStyleSettings.getCustomSettings(PrlCodeStyleSettings::class.java)
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            PrlBlock(formattingContext.node, prlCodeStyleSettings, prlSpacingBuilder(prlCodeStyleSettings)),
            formattingContext.codeStyleSettings
        )
    }
}

class PrlBlock(
    private val node: ASTNode,
    private val customSettings: PrlCodeStyleSettings,
    private val spacingBuilder: SpacingBuilder
) : ASTBlock {

    private val _indent: Indent? by lazy {
        when (node.elementType) {
            RULE_FILE, HEADER_DEF, SLICING_PROPERTIES, FEATURE_DEFINITION, GROUP_DEFINITION, RULE_DEF -> Indent.getAbsoluteNoneIndent()
            HEADER_CONTENT, SLICING_PROPERTY_DEFINITION,
            FEATURE_BODY_CONTENT, RULE_BODY_CONTENT -> Indent.getNormalIndent()

            SIMP -> Indent.getContinuationIndent()

            else -> Indent.getNoneIndent()
        }
    }

    private val _subBlocks: List<Block> by lazy {
        node.getChildren(null)
            .filterNot { it.elementType == TokenType.WHITE_SPACE || it.textRange.isEmpty }
            .map { child -> PrlBlock(child, customSettings, spacingBuilder) }
    }

    override fun getNode() = node

    override fun getTextRange(): TextRange = node.textRange

    override fun getSubBlocks() = _subBlocks

    override fun getIndent() = _indent

    override fun getWrap() = null

    override fun getAlignment() = null

    override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)

    override fun getChildAttributes(newChildIndex: Int) = ChildAttributes(
        if (node.elementType == RULE_FILE) Indent.getNoneIndent() else Indent.getNormalIndent(), null
    )

    override fun isIncomplete() = false

    override fun isLeaf() = false

}

// TODO would be cleaner if we had specific block types instead of just the dummy-block PrlBlock
fun prlSpacingBuilder(prlCodeStyleSettings: PrlCodeStyleSettings): SpacingBuilder =
    SpacingBuilder(prlCodeStyleSettings.container, PrlLanguage)
        .before(PrlTypes.COMMA).spacing(0, 0, 0, false, 0)
        .after(PrlTypes.COMMA).spacing(1, 1, 0, true, 0)

        .afterInside(PrlTypes.NOT_MINUS, TokenSet.create(PrlTypes.POS_NEG_NUMBER, PrlTypes.INT_MUL, PrlTypes.LIT)).spacing(0, 0, 0, false, 0)
        .before(PrlTypes.NOT_MINUS).spacing(1, 1, 0, false, 0)
        .after(PrlTypes.NOT_MINUS).spacing(1, 1, 0, false, 0)

        .before(PrlTypes.ADD).spacing(1, 1, 0, false, 0)
        .after(PrlTypes.ADD).spacing(1, 1, 0, false, 0)

        .before(PrlTypes.MUL).spacing(0, 0, 0, false, 0)
        .after(PrlTypes.MUL).spacing(0, 0, 0, false, 0)

        .beforeInside(PrlTypes.EQ, PrlTypes.FEATURE_VERSION_RESTRICTION).spacing(0, 0, 0, false, 0)
        .afterInside(PrlTypes.EQ, PrlTypes.FEATURE_VERSION_RESTRICTION).spacing(0, 0, 0, false, 0)
        .around(PrlTypes.COMPARATOR).spacing(1, 1, 0, false, 0)

        .before(PrlTypes.FEATURE_RESTRICTION).spacing(1, 1, 0, false, 0)
        .afterInside(PrlTypes.EQ, PrlTypes.FEATURE_RESTRICTION).spacing(1, 1, 0, false, 0)

        .withinPairInside(PrlTypes.ID, PrlTypes.LSQB, PrlTypes.VERSION_PREDICATE).spacing(0, 0, 0, false, 0)
        .afterInside(TokenSet.create(PrlTypes.AMO, PrlTypes.EXO), PrlTypes.CC).spacing(0, 0, 0, false, 0)
        .before(PrlTypes.LSQB).spacing(1, 1, 0, false, 0)
        .after(PrlTypes.LSQB).spacing(0, 0, 0, true, 0)

        .before(PrlTypes.RSQB).spacing(0, 0, 0, true, 0)
        .after(PrlTypes.RSQB).spacing(0, 1, 0, true, 0)

        .before(PrlTypes.LBRA).spacing(1, 1, 0, false, 0)
        .after(PrlTypes.LBRA).spacing(0, 0, 1, true, 0)

        .before(PrlTypes.RBRA).spacing(0, 0, 1, true, 0)
        .after(PrlTypes.RBRA).spacing(0, 0, 1, true, 2)

        .after(PrlTypes.LPAR).spacing(0, 0, 0, true, 0)
        .before(PrlTypes.RPAR).spacing(0, 0, 0, true, 0)

        .after(TokenSet.create(PrlTypes.ID, PrlTypes.DESCRIPTION)).spacing(1, 1, 0, false, 0)
        .afterInside(PrlTypes.PROPERTY_REF, PrlTypes.PROPERTY).spacing(1, 1, 0, false, 0)
        .afterInside(PrlTypes.PROPERTY_REF, PrlTypes.PROPERTY).spacing(1, 1, 0, false, 0)

        .before(TokenSet.create(PrlTypes.PROPERTY, RULE_BODY_CONTENT, FEATURE_BODY_CONTENT)).spacing(0, 0, 1, true, 1)
        .after(TokenSet.create(PrlTypes.PROPERTY, RULE_BODY_CONTENT, FEATURE_BODY_CONTENT)).spacing(0, 0, 1, true, 1)

        .after(PrlTypes.FEATURE_DEF).spacing(1, 1, 0, false, 0)

        .before(PrlTokenSets.KEYWORDS).spacing(1, 1, 0, true, 1)
        .after(PrlTokenSets.KEYWORDS).spacing(1, 1, 0, true, 1)


class PrlCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(PrlLanguage.id, container)

class PrlCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun createCustomSettings(settings: CodeStyleSettings) = PrlCodeStyleSettings(settings)
    override fun getLanguage() = PrlLanguage

    override fun getConfigurableDisplayName() = "PRL (Pragmatic Rule Language)"

    override fun createConfigurable(baseSettings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable =
        object : CodeStyleAbstractConfigurable(baseSettings, modelSettings, "PRL") {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return object : TabbedLanguageCodeStylePanel(PrlLanguage, currentSettings, settings) {
                    override fun initTabs(settings: CodeStyleSettings) {
                        addIndentOptionsTab(settings)
//                        addSpacesTab(settings)
//                        addWrappingAndBracesTab(settings)
//                        addBlankLinesTab(settings)
//                        addWrappingAndBracesTab(settings)
                    }
                }
            }
        }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showAllStandardOptions()
        } else if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showAllStandardOptions()
        }
    }

    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        indentOptions.INDENT_SIZE = 2
        indentOptions.TAB_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 4
    }

    override fun getIndentOptionsEditor() = SmartIndentOptionsEditor(this)

    // TODO cleanup example
    override fun getCodeSample(settingsType: SettingsType) = """
        header {
          prl_version 1.0
          test "abc"
        }
        
        slicing properties {
          int p1
          date p2
        }
        
        int feature f1 [1 - 5]
        versioned feature f2
        int feature f4 [1, 2, 43]
        feature c1
        feature c2
        
        feature dd1
        feature dd7 {
          description "123"
        }
        
        rule if dd1 then dd1 else dd7 & ([f1 = 4] / (dd1 & dd7) / dd7)
        rule mandatory feature dd7 {
          rul 1
          prop 2
        }
        
        rule if dd1 then dd7
        
        rule mandatory feature dd1
        
        mandatory group gr contains [dd1, c1]
        
        rule mandatory feature c1 {
          id "123"
          description "Test"
        }
        
        mandatory group g1 contains [g1]
        optional group g2 contains [g1]
        rule if dd1 then dd1 else dd1
        optional group g3 contains [dd1]
        rule mandatory feature dd1 {
          description "123"
          id "123"
        }
        rule if dd1 thenNot dd1
        rule if dd1 then dd1 {
          description "123"
        }
        
        enum feature d1 ["1"]
        
        rule forbidden feature dd1 {
          dote 1
          uint 2
        }
        rule if dd1 then dd1 else dd1
    """.trimIndent()
}

/**
 * Indentation does not work properly when typing Enter at the end of the document (it does indent, although it should not) , so we "override" the behavior here.
 * Also, this provider should be more efficient than the formatter-based provider.
 */
class PrlLineIndentProvider : JavaLikeLangLineIndentProvider() {
    override fun mapType(tokenType: IElementType): SemanticEditorPosition.SyntaxElement? = Companion.SYNTAX_MAP[tokenType]

    override fun isSuitableForLanguage(language: Language) = language == PrlLanguage

    private object Companion {
        val SYNTAX_MAP = mapOf(
            TokenType.WHITE_SPACE to JavaLikeElement.Whitespace,
            PrlTypes.COMMENT to JavaLikeElement.LineComment,
            PrlTypes.LBRA to JavaLikeElement.BlockOpeningBrace,
            PrlTypes.RBRA to JavaLikeElement.BlockClosingBrace,
            PrlTypes.LSQB to JavaLikeElement.ArrayOpeningBracket,
            PrlTypes.RSQB to JavaLikeElement.ArrayClosingBracket,
            PrlTypes.LPAR to JavaLikeElement.LeftParenthesis,
            PrlTypes.RPAR to JavaLikeElement.RightParenthesis,
            PrlTypes.COMMA to JavaLikeElement.Comma,
        )
    }
}
