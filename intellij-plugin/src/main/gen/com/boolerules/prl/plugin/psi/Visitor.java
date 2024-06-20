// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.boolerules.prl.plugin.language.PrlReference;
import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition;
import com.boolerules.prl.plugin.language.PrlNamedElement;

public class Visitor extends PsiElementVisitor {

  public void visitAtomicConstraint(@NotNull AtomicConstraint o) {
    visitPsiElement(o);
  }

  public void visitBoolFeatureRange(@NotNull BoolFeatureRange o) {
    visitPsiElement(o);
  }

  public void visitCc(@NotNull Cc o) {
    visitPsiElement(o);
  }

  public void visitComparator(@NotNull Comparator o) {
    visitPsiElement(o);
  }

  public void visitComparisonPredicate(@NotNull ComparisonPredicate o) {
    visitPsiElement(o);
  }

  public void visitConj(@NotNull Conj o) {
    visitPsiElement(o);
  }

  public void visitConstant(@NotNull Constant o) {
    visitPsiElement(o);
  }

  public void visitConstraint(@NotNull Constraint o) {
    visitPsiElement(o);
  }

  public void visitDateList(@NotNull DateList o) {
    visitPsiElement(o);
  }

  public void visitDateRange(@NotNull DateRange o) {
    visitPsiElement(o);
  }

  public void visitDisj(@NotNull Disj o) {
    visitPsiElement(o);
  }

  public void visitElementDescription(@NotNull ElementDescription o) {
    visitPsiElement(o);
  }

  public void visitEnumFeatureRange(@NotNull EnumFeatureRange o) {
    visitPsiElement(o);
  }

  public void visitEnumRange(@NotNull EnumRange o) {
    visitPsiElement(o);
  }

  public void visitEquivalence(@NotNull Equivalence o) {
    visitPsiElement(o);
  }

  public void visitFeatureBody(@NotNull FeatureBody o) {
    visitPsiElement(o);
  }

  public void visitFeatureBodyContent(@NotNull FeatureBodyContent o) {
    visitPsiElement(o);
  }

  public void visitFeatureDef(@NotNull FeatureDef o) {
    visitPrlNamedElement(o);
  }

  public void visitFeatureDefinition(@NotNull FeatureDefinition o) {
    visitFeatureOrGroupDefinition(o);
  }

  public void visitFeatureList(@NotNull FeatureList o) {
    visitPsiElement(o);
  }

  public void visitFeatureRef(@NotNull FeatureRef o) {
    visitPrlReference(o);
  }

  public void visitFeatureRestriction(@NotNull FeatureRestriction o) {
    visitPsiElement(o);
  }

  public void visitFeatureVersionRestriction(@NotNull FeatureVersionRestriction o) {
    visitPsiElement(o);
  }

  public void visitForbiddenFeatureRule(@NotNull ForbiddenFeatureRule o) {
    visitPsiElement(o);
  }

  public void visitGroupDefinition(@NotNull GroupDefinition o) {
    visitFeatureOrGroupDefinition(o);
  }

  public void visitHeaderContent(@NotNull HeaderContent o) {
    visitPsiElement(o);
  }

  public void visitHeaderDef(@NotNull HeaderDef o) {
    visitPsiElement(o);
  }

  public void visitHeaderProperty(@NotNull HeaderProperty o) {
    visitPsiElement(o);
  }

  public void visitHeaderPropertyName(@NotNull HeaderPropertyName o) {
    visitPsiElement(o);
  }

  public void visitImplication(@NotNull Implication o) {
    visitPsiElement(o);
  }

  public void visitInEnumPredicate(@NotNull InEnumPredicate o) {
    visitPsiElement(o);
  }

  public void visitInIntPredicate(@NotNull InIntPredicate o) {
    visitPsiElement(o);
  }

  public void visitIntFeatureRange(@NotNull IntFeatureRange o) {
    visitPsiElement(o);
  }

  public void visitIntList(@NotNull IntList o) {
    visitPsiElement(o);
  }

  public void visitIntMul(@NotNull IntMul o) {
    visitPsiElement(o);
  }

  public void visitIntRange(@NotNull IntRange o) {
    visitPsiElement(o);
  }

  public void visitIntSum(@NotNull IntSum o) {
    visitPsiElement(o);
  }

  public void visitLit(@NotNull Lit o) {
    visitPsiElement(o);
  }

  public void visitMandatoryFeatureRule(@NotNull MandatoryFeatureRule o) {
    visitPsiElement(o);
  }

  public void visitNum(@NotNull Num o) {
    visitPsiElement(o);
  }

  public void visitPosNegNumber(@NotNull PosNegNumber o) {
    visitPsiElement(o);
  }

  public void visitProperty(@NotNull Property o) {
    visitPsiElement(o);
  }

  public void visitPropertyRef(@NotNull PropertyRef o) {
    visitPrlReference(o);
  }

  public void visitQuotedString(@NotNull QuotedString o) {
    visitPsiElement(o);
  }

  public void visitRuleBody(@NotNull RuleBody o) {
    visitPsiElement(o);
  }

  public void visitRuleBodyContent(@NotNull RuleBodyContent o) {
    visitPsiElement(o);
  }

  public void visitRuleDef(@NotNull RuleDef o) {
    visitPsiElement(o);
  }

  public void visitRuleFile(@NotNull RuleFile o) {
    visitPsiElement(o);
  }

  public void visitRuleId(@NotNull RuleId o) {
    visitPsiElement(o);
  }

  public void visitSimp(@NotNull Simp o) {
    visitPsiElement(o);
  }

  public void visitSlicingProperties(@NotNull SlicingProperties o) {
    visitPsiElement(o);
  }

  public void visitSlicingPropertyDef(@NotNull SlicingPropertyDef o) {
    visitPrlNamedElement(o);
  }

  public void visitSlicingPropertyDefinition(@NotNull SlicingPropertyDefinition o) {
    visitPsiElement(o);
  }

  public void visitSlicingPropertyType(@NotNull SlicingPropertyType o) {
    visitPsiElement(o);
  }

  public void visitStringList(@NotNull StringList o) {
    visitPsiElement(o);
  }

  public void visitTerm(@NotNull Term o) {
    visitPsiElement(o);
  }

  public void visitVersion(@NotNull Version o) {
    visitPsiElement(o);
  }

  public void visitVersionPredicate(@NotNull VersionPredicate o) {
    visitPsiElement(o);
  }

  public void visitVersionedBoolFeatureRange(@NotNull VersionedBoolFeatureRange o) {
    visitPsiElement(o);
  }

  public void visitFeatureOrGroupDefinition(@NotNull FeatureOrGroupDefinition o) {
    visitElement(o);
  }

  public void visitPrlNamedElement(@NotNull PrlNamedElement o) {
    visitElement(o);
  }

  public void visitPrlReference(@NotNull PrlReference o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
