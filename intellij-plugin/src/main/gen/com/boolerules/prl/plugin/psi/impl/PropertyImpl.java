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

public class PropertyImpl extends PrlASTWrapperPsiElement implements Property {

  public PropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DateRange getDateRange() {
    return findChildByClass(DateRange.class);
  }

  @Override
  @Nullable
  public EnumRange getEnumRange() {
    return findChildByClass(EnumRange.class);
  }

  @Override
  @Nullable
  public IntRange getIntRange() {
    return findChildByClass(IntRange.class);
  }

  @Override
  @Nullable
  public PosNegNumber getPosNegNumber() {
    return findChildByClass(PosNegNumber.class);
  }

  @Override
  @NotNull
  public PropertyRef getPropertyRef() {
    return findNotNullChildByClass(PropertyRef.class);
  }

  @Override
  @Nullable
  public QuotedString getQuotedString() {
    return findChildByClass(QuotedString.class);
  }

}
