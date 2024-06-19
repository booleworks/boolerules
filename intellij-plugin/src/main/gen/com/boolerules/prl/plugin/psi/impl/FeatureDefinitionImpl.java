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

public class FeatureDefinitionImpl extends PrlASTWrapperPsiElement implements FeatureDefinition {

  public FeatureDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitFeatureDefinition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BoolFeatureRange getBoolFeatureRange() {
    return findChildByClass(BoolFeatureRange.class);
  }

  @Override
  @Nullable
  public EnumFeatureRange getEnumFeatureRange() {
    return findChildByClass(EnumFeatureRange.class);
  }

  @Override
  @Nullable
  public FeatureBody getFeatureBody() {
    return findChildByClass(FeatureBody.class);
  }

  @Override
  @Nullable
  public IntFeatureRange getIntFeatureRange() {
    return findChildByClass(IntFeatureRange.class);
  }

  @Override
  @Nullable
  public VersionedBoolFeatureRange getVersionedBoolFeatureRange() {
    return findChildByClass(VersionedBoolFeatureRange.class);
  }

  @Override
  @Nullable
  public Vis getVis() {
    return findChildByClass(Vis.class);
  }

  @Override
  @NotNull
  public FeatureDef getFeatureDef() {
    return PrlPsiImplUtil.getFeatureDef(this);
  }

}
