// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.boolerules.prl.plugin.language.PrlNamedElement;

public interface FeatureDef extends PrlNamedElement {

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @NotNull
  PsiElement getNameIdentifier();

}
