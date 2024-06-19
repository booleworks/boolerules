// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AtomicConstraint extends PsiElement {

  @Nullable
  ComparisonPredicate getComparisonPredicate();

  @Nullable
  FeatureRef getFeatureRef();

  @Nullable
  InEnumPredicate getInEnumPredicate();

  @Nullable
  InIntPredicate getInIntPredicate();

  @Nullable
  VersionPredicate getVersionPredicate();

}
