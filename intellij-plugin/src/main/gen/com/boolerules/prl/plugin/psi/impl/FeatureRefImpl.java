// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.boolerules.prl.plugin.psi.PrlTypes.*;
import com.boolerules.prl.plugin.psi.*;
import com.intellij.psi.PsiReference;

public class FeatureRefImpl extends FeatureRefMixin implements FeatureRef {

  public FeatureRefImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitFeatureRef(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<String> getReferencedName() {
    return PrlPsiImplUtil.getReferencedName(this);
  }

  @Override
  @NotNull
  public PsiReference getReference() {
    return PrlPsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    return PrlPsiImplUtil.getVariants(this);
  }

}
