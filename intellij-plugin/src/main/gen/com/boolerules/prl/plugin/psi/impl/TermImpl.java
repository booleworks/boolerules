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

public class TermImpl extends PrlASTWrapperPsiElement implements Term {

  public TermImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitTerm(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FeatureRef getFeatureRef() {
    return findChildByClass(FeatureRef.class);
  }

  @Override
  @Nullable
  public IntSum getIntSum() {
    return findChildByClass(IntSum.class);
  }

  @Override
  @Nullable
  public PosNegNumber getPosNegNumber() {
    return findChildByClass(PosNegNumber.class);
  }

  @Override
  @Nullable
  public QuotedString getQuotedString() {
    return findChildByClass(QuotedString.class);
  }

}
