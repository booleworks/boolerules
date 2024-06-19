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

public class RuleBodyContentImpl extends PrlASTWrapperPsiElement implements RuleBodyContent {

  public RuleBodyContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitRuleBodyContent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ElementDescription getElementDescription() {
    return findChildByClass(ElementDescription.class);
  }

  @Override
  @Nullable
  public Property getProperty() {
    return findChildByClass(Property.class);
  }

  @Override
  @Nullable
  public RuleId getRuleId() {
    return findChildByClass(RuleId.class);
  }

}
