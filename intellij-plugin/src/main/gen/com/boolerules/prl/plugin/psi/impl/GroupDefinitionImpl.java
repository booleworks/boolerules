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

public class GroupDefinitionImpl extends PrlASTWrapperPsiElement implements GroupDefinition {

  public GroupDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitGroupDefinition(this);
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
  public FeatureList getFeatureList() {
    return findChildByClass(FeatureList.class);
  }

  @Override
  @Nullable
  public RuleBody getRuleBody() {
    return findChildByClass(RuleBody.class);
  }

  @Override
  @Nullable
  public Vis getVis() {
    return findChildByClass(Vis.class);
  }

}
