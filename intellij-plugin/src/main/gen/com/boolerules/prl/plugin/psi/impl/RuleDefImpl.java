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

public class RuleDefImpl extends PrlASTWrapperPsiElement implements RuleDef {

  public RuleDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitRuleDef(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<Constraint> getConstraintList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Constraint.class);
  }

  @Override
  @Nullable
  public FeatureRef getFeatureRef() {
    return findChildByClass(FeatureRef.class);
  }

  @Override
  @Nullable
  public ForbiddenFeatureRule getForbiddenFeatureRule() {
    return findChildByClass(ForbiddenFeatureRule.class);
  }

  @Override
  @Nullable
  public MandatoryFeatureRule getMandatoryFeatureRule() {
    return findChildByClass(MandatoryFeatureRule.class);
  }

  @Override
  @Nullable
  public RuleBody getRuleBody() {
    return findChildByClass(RuleBody.class);
  }

}
