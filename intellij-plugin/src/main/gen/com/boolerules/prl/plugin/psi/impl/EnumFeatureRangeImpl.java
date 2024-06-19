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

public class EnumFeatureRangeImpl extends PrlASTWrapperPsiElement implements EnumFeatureRange {

  public EnumFeatureRangeImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitEnumFeatureRange(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FeatureDef getFeatureDef() {
    return findChildByClass(FeatureDef.class);
  }

  @Override
  @Nullable
  public StringList getStringList() {
    return findChildByClass(StringList.class);
  }

}
