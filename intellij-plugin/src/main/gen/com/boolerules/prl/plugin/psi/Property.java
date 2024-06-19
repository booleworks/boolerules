// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface Property extends PsiElement {

  @Nullable
  DateRange getDateRange();

  @Nullable
  EnumRange getEnumRange();

  @Nullable
  IntRange getIntRange();

  @Nullable
  PosNegNumber getPosNegNumber();

  @NotNull
  PropertyRef getPropertyRef();

  @Nullable
  QuotedString getQuotedString();

}
