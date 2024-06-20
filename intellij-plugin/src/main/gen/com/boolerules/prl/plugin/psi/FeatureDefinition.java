// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition;

public interface FeatureDefinition extends FeatureOrGroupDefinition {

  @Nullable
  BoolFeatureRange getBoolFeatureRange();

  @Nullable
  EnumFeatureRange getEnumFeatureRange();

  @Nullable
  FeatureBody getFeatureBody();

  @Nullable
  IntFeatureRange getIntFeatureRange();

  @Nullable
  VersionedBoolFeatureRange getVersionedBoolFeatureRange();

  @NotNull
  FeatureDef getFeatureDef();

}
