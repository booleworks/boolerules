// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.boolerules.prl.plugin.language.FeatureOrGroupDefinition;

public interface GroupDefinition extends FeatureOrGroupDefinition {

  @Nullable
  FeatureDef getFeatureDef();

  @Nullable
  FeatureList getFeatureList();

  @Nullable
  RuleBody getRuleBody();

}
