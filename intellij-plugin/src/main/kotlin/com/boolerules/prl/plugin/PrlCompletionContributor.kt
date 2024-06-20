package com.boolerules.prl.plugin

import com.boolerules.prl.plugin.psi.FeatureBody
import com.boolerules.prl.plugin.psi.FeatureBodyContent
import com.boolerules.prl.plugin.psi.PrlTypes
import com.boolerules.prl.plugin.psi.RuleBody
import com.boolerules.prl.plugin.psi.RuleBodyContent
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.not
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.ProcessingContext

class PrlCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, psiElement().atStartOf(psiFile()), provideKeywords("header"))
        extend(
            CompletionType.BASIC,
            psiElement().withParentAndPotentialError(PrlTypes.HEADER_DEF, true).afterLeaf("{"),
            provideKeywords("prl_version")
        )
        extend(
            CompletionType.BASIC,
            psiElement().withParentErrorAndSuperParent(PrlTypes.RULE_FILE)
                .withParent(
                    psiElement().afterSibling(psiElement(PrlTypes.HEADER_DEF)).andNot(psiElement().beforeLeaf("slicing"))
                ),
            provideKeywords("slicing properties")
        )
        extend(CompletionType.BASIC, psiElement().withParentErrorAndSuperParent(PrlTypes.SLICING_PROPERTIES), provideKeywords("bool", "int", "enum", "date"))
        extend(
            CompletionType.BASIC,
            psiElement().withParentErrorAndSuperParent(PrlTypes.RULE_FILE)
                .andNot(psiElement().beforeLeaf("slicing"))
                .andOr(not(placeForElse()), psiElement().with(AfterNewLine())),
            // a (possibly empty) identifier after a module is still identified as part of the module, so we have to check if it is preceded by the '}' of the module
            ruleSetKeywords
        )
        extend(
            CompletionType.BASIC,
            // first (possible) identifier after 'rule' is identified as Constraint-Literal (FeatureRef), the tree (constraint -> equiv -> ... -> identifier) is 9 levels deep
            psiElement().withSuperParent(9, psiElement(PrlTypes.CONSTRAINT).afterLeaf("rule"))
                .andNot(psiElement().withParent(PsiErrorElement::class.java)),
            provideKeywordsPrioritized("if", "mandatory", "forbidden")
        )
        extend(
            CompletionType.BASIC,
            psiElement().withParent(psiElement(PrlTypes.RULE_FILE)).afterLeaf("bool", "int", "enum", "date", "versioned"),
            provideKeywords("feature")
        )
        extend(
            CompletionType.BASIC,
            psiElement().afterLeaf(psiElement().withAncestor(99, psiElement(PrlTypes.CONSTRAINT).afterLeaf("if").withParent(psiElement(PrlTypes.RULE_DEF)))),
            provideKeywords("then", "thenNot")
        )
        extend(
            CompletionType.BASIC,
            placeForElse(),
            provideKeywordsPrioritized("else")
        )
        extend(
            CompletionType.BASIC,
            psiElement().withParent(
                psiElement(PsiErrorElement::class.java).afterLeaf(
                    psiElement().withAncestor(99, psiElement(PrlTypes.CONSTRAINT).afterLeaf("rule").withParent(psiElement(PrlTypes.RULE_DEF)))
                )
            ),
            provideKeywordsPrioritized("is")
        )
        extend(
            CompletionType.BASIC,
            psiElement()
                .afterLeaf(
                    psiElement().andOr(psiElement(PrlTypes.IDENT), psiElement(PrlTypes.BTCK_IDENTIFIER))
                        .withParent(
                            psiElement(PrlTypes.FEATURE_DEF)
                                .afterLeaf("group")
                                .withParent(psiElement(PrlTypes.GROUP_DEFINITION))
                        )
                ),
            provideKeywords("contains")
        )
        extend(
            CompletionType.BASIC,
            psiElement(PrlTypes.IDENT).withParent(
                psiElement(PrlTypes.PROPERTY_REF).withParent(
                    psiElement(PrlTypes.PROPERTY).withParent(
                        psiElement(PrlTypes.RULE_BODY_CONTENT)
                            .withParent(psiElement(RuleBody::class.java).with(NotYetContainingRuleProperty { it.ruleId }))
                    )
                )
            ),
            provideKeywordsPrioritized("id")
        )
        extend(
            CompletionType.BASIC,
            psiElement(PrlTypes.IDENT).withParent(
                psiElement(PrlTypes.PROPERTY_REF).withParent(
                    psiElement(PrlTypes.PROPERTY).withParent(
                        psiElement(PrlTypes.RULE_BODY_CONTENT)
                            .withParent(psiElement(RuleBody::class.java).with(NotYetContainingRuleProperty { it.elementDescription }))
                    )
                )
            ),
            provideKeywordsPrioritized("description")
        )
        extend(
            CompletionType.BASIC,
            psiElement(PrlTypes.IDENT).withParent(
                psiElement(PrlTypes.PROPERTY_REF).withParent(
                    psiElement(PrlTypes.PROPERTY).withParent(
                        psiElement(PrlTypes.FEATURE_BODY_CONTENT)
                            .withParent(psiElement(FeatureBody::class.java).with(NotYetContainingFeatureProperty { it.elementDescription }))
                    )
                )
            ),
            provideKeywordsPrioritized("description")
        )
    }
}

class NotYetContainingRuleProperty(val property: (RuleBodyContent) -> Any?) : PatternCondition<RuleBody>("NotYetContainingRuleProperty_$property") {
    override fun accepts(ruleBody: RuleBody, context: ProcessingContext?) = ruleBody.ruleBodyContentList.none { property(it) != null }
}

class NotYetContainingFeatureProperty(val property: (FeatureBodyContent) -> Any?) : PatternCondition<FeatureBody>("NotYetContainingFeatureProperty_$property") {
    override fun accepts(ruleBody: FeatureBody, context: ProcessingContext?) = ruleBody.featureBodyContentList.none { property(it) != null }
}

class AfterNewLine : PatternCondition<PsiElement>("CursorAfterNewLine") {
    override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
        val offset = element.textOffset
        return element.containingFile.text.getOrNull(offset - 1) == '\n'
    }
}

private fun placeForElse() = psiElement().afterLeaf(psiElement().withAncestor(99, psiElement(PrlTypes.CONSTRAINT).afterLeaf("then").withParent(psiElement(PrlTypes.RULE_DEF))))

private fun PsiElementPattern.Capture<PsiElement>.withParentAndPotentialError(type: IElementType, mustBeFirstChild: Boolean = false) = andOr(
    psiElement().withParentErrorAndSuperParent(type, mustBeFirstChild),
    psiElement().withParent(psiElement(type))
)

private fun PsiElementPattern.Capture<PsiElement>.withParentErrorAndSuperParent(type: IElementType, mustBeFirstChild: Boolean = false): PsiElementPattern.Capture<PsiElement> =
    withParent(psiElement(PsiErrorElement::class.java)).withSuperParent(2, if (mustBeFirstChild) psiElement().atStartOf(psiElement(type)) else psiElement(type))

private fun provideKeywords(vararg keywords: String): CompletionProvider<CompletionParameters> = provideKeywords(false, *keywords)

private fun provideKeywordsPrioritized(vararg keywords: String): CompletionProvider<CompletionParameters> = provideKeywords(true, *keywords)

private fun provideKeywords(prioritized: Boolean, vararg keywords: String): CompletionProvider<CompletionParameters> = object : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        keywords.forEach {
            result.addElement(LookupElementBuilder.create(it).let { lookupElem ->
                if (prioritized) PrioritizedLookupElement.withPriority(lookupElem, DEFAULT_PRIO) else lookupElem
            })
        }
    }
}

private val featureKeywords = listOf("feature", "versioned", "versioned feature", "int feature", "enum feature", "bool feature", "versioned bool feature")

private val groupKeyWords = listOf("optional group", "mandatory group")

private val ruleSetKeywords = provideKeywords(
    "feature", "rule", "rule forbidden feature", "rule mandatory feature", "rule if",
    *(featureKeywords + groupKeyWords).toTypedArray()
)

private const val DEFAULT_PRIO = 10.0
