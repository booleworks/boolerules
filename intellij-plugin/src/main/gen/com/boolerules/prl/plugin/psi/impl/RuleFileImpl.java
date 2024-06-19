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

public class RuleFileImpl extends PrlASTWrapperPsiElement implements RuleFile {

  public RuleFileImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitRuleFile(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HeaderDef getHeaderDef() {
    return findNotNullChildByClass(HeaderDef.class);
  }

  @Override
  @NotNull
  public List<ModuleDefinition> getModuleDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ModuleDefinition.class);
  }

  @Override
  @Nullable
  public SlicingProperties getSlicingProperties() {
    return findChildByClass(SlicingProperties.class);
  }

}
