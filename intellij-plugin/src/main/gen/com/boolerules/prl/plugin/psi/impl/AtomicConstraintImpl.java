// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.boolerules.prl.plugin.psi.PrlTypes.*;
import com.boolerules.prl.plugin.psi.PrlASTWrapperPsiElement;
import com.boolerules.prl.plugin.psi.*;

public class AtomicConstraintImpl extends PrlASTWrapperPsiElement implements AtomicConstraint {

  public AtomicConstraintImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitAtomicConstraint(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ComparisonPredicate getComparisonPredicate() {
    return findChildByClass(ComparisonPredicate.class);
  }

  @Override
  @Nullable
  public FeatureRef getFeatureRef() {
    return findChildByClass(FeatureRef.class);
  }

  @Override
  @Nullable
  public InEnumPredicate getInEnumPredicate() {
    return findChildByClass(InEnumPredicate.class);
  }

  @Override
  @Nullable
  public InIntPredicate getInIntPredicate() {
    return findChildByClass(InIntPredicate.class);
  }

  @Override
  @Nullable
  public VersionPredicate getVersionPredicate() {
    return findChildByClass(VersionPredicate.class);
  }

}
