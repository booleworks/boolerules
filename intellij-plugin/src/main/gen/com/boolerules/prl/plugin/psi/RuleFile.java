// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RuleFile extends PsiElement {

  @NotNull
  List<FeatureDefinition> getFeatureDefinitionList();

  @NotNull
  List<GroupDefinition> getGroupDefinitionList();

  @NotNull
  HeaderDef getHeaderDef();

  @NotNull
  List<RuleDef> getRuleDefList();

  @Nullable
  SlicingProperties getSlicingProperties();

}
