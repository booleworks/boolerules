// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RuleFile extends PsiElement {

  @NotNull
  HeaderDef getHeaderDef();

  @NotNull
  List<ModuleDefinition> getModuleDefinitionList();

  @Nullable
  SlicingProperties getSlicingProperties();

}
