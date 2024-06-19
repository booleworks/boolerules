// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface VersionPredicate extends PsiElement {

  @NotNull
  Comparator getComparator();

  @NotNull
  FeatureRef getFeatureRef();

  @NotNull
  Num getNum();

}
