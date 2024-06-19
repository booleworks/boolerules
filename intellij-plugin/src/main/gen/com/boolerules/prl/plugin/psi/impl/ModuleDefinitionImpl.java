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

public class ModuleDefinitionImpl extends PrlASTWrapperPsiElement implements ModuleDefinition {

  public ModuleDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitModuleDefinition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<FeatureDefinition> getFeatureDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FeatureDefinition.class);
  }

  @Override
  @NotNull
  public List<GroupDefinition> getGroupDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, GroupDefinition.class);
  }

  @Override
  @NotNull
  public List<ImportDef> getImportDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImportDef.class);
  }

  @Override
  @Nullable
  public ModuleDef getModuleDef() {
    return findChildByClass(ModuleDef.class);
  }

  @Override
  @NotNull
  public List<RuleDef> getRuleDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, RuleDef.class);
  }

}
