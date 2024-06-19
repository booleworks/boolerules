// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.boolerules.prl.plugin.language.PrlReference;
import com.intellij.psi.PsiReference;

public interface FeatureRef extends PrlReference {

  @NotNull
  List<String> getReferencedName();

  @NotNull
  PsiReference getReference();

  @NotNull
  Object[] getVariants();

}
