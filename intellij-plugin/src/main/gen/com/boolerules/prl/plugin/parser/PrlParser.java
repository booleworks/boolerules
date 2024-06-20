// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.boolerules.prl.plugin.psi.PrlTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PrlParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  /* ********************************************************** */
  // versionPredicate | featureRef | comparisonPredicate | inEnumPredicate | inIntPredicate
  public static boolean atomicConstraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atomicConstraint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATOMIC_CONSTRAINT, "<atomic constraint>");
    r = versionPredicate(b, l + 1);
    if (!r) r = featureRef(b, l + 1);
    if (!r) r = comparisonPredicate(b, l + 1);
    if (!r) r = inEnumPredicate(b, l + 1);
    if (!r) r = inIntPredicate(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BOOL? FEAT featureDef
  public static boolean boolFeatureRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolFeatureRange")) return false;
    if (!nextTokenIs(b, "<bool feature range>", BOOL, FEAT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BOOL_FEATURE_RANGE, "<bool feature range>");
    r = boolFeatureRange_0(b, l + 1);
    r = r && consumeToken(b, FEAT);
    p = r; // pin = 2
    r = r && featureDef(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // BOOL?
  private static boolean boolFeatureRange_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolFeatureRange_0")) return false;
    consumeToken(b, BOOL);
    return true;
  }

  /* ********************************************************** */
  // AMO featureList | EXO featureList
  public static boolean cc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cc")) return false;
    if (!nextTokenIs(b, "<cc>", AMO, EXO)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CC, "<cc>");
    r = cc_0(b, l + 1);
    if (!r) r = cc_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AMO featureList
  private static boolean cc_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cc_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AMO);
    r = r && featureList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EXO featureList
  private static boolean cc_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cc_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXO);
    r = r && featureList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // COMMENT
  static boolean comment_(PsiBuilder b, int l) {
    return consumeToken(b, COMMENT);
  }

  /* ********************************************************** */
  // EQ | NE | LT | LE | GT | GE
  public static boolean comparator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPARATOR, "<comparator>");
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, NE);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LE);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, GE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LSQB term comparator term RSQB
  public static boolean comparisonPredicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparisonPredicate")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LSQB);
    r = r && term(b, l + 1);
    r = r && comparator(b, l + 1);
    r = r && term(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, COMPARISON_PREDICATE, r);
    return r;
  }

  /* ********************************************************** */
  // lit (AND lit)*
  public static boolean conj(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conj")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONJ, "<conj>");
    r = lit(b, l + 1);
    r = r && conj_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (AND lit)*
  private static boolean conj_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conj_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!conj_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "conj_1", c)) break;
    }
    return true;
  }

  // AND lit
  private static boolean conj_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conj_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    r = r && lit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TRUE | FALSE
  public static boolean constant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constant")) return false;
    if (!nextTokenIs(b, "<constant>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT, "<constant>");
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // equivalence {
  // //  recoverWhile = recoverConstraint
  // }
  public static boolean constraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constraint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTRAINT, "<constraint>");
    r = equivalence(b, l + 1);
    r = r && constraint_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // {
  // //  recoverWhile = recoverConstraint
  // }
  private static boolean constraint_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // constraint
  static boolean constraintRule(PsiBuilder b, int l) {
    return constraint(b, l + 1);
  }

  /* ********************************************************** */
  // LSQB DATE_VAL (COMMA DATE_VAL)* RSQB
  public static boolean dateList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dateList")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LSQB, DATE_VAL);
    r = r && dateList_2(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, DATE_LIST, r);
    return r;
  }

  // (COMMA DATE_VAL)*
  private static boolean dateList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dateList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!dateList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "dateList_2", c)) break;
    }
    return true;
  }

  // COMMA DATE_VAL
  private static boolean dateList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dateList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, DATE_VAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // dateList | LSQB DATE_VAL NOT_MINUS DATE_VAL RSQB
  public static boolean dateRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dateRange")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = dateList(b, l + 1);
    if (!r) r = parseTokens(b, 0, LSQB, DATE_VAL, NOT_MINUS, DATE_VAL, RSQB);
    exit_section_(b, m, DATE_RANGE, r);
    return r;
  }

  /* ********************************************************** */
  // conj (OR conj)*
  public static boolean disj(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "disj")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DISJ, "<disj>");
    r = conj(b, l + 1);
    r = r && disj_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (OR conj)*
  private static boolean disj_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "disj_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!disj_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "disj_1", c)) break;
    }
    return true;
  }

  // OR conj
  private static boolean disj_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "disj_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR);
    r = r && conj(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // quotedString
  public static boolean elementDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementDescription")) return false;
    if (!nextTokenIs(b, QUOTED)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = quotedString(b, l + 1);
    exit_section_(b, m, ELEMENT_DESCRIPTION, r);
    return r;
  }

  /* ********************************************************** */
  // ENUM FEAT featureDef stringList
  public static boolean enumFeatureRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumFeatureRange")) return false;
    if (!nextTokenIs(b, ENUM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_FEATURE_RANGE, null);
    r = consumeTokens(b, 1, ENUM, FEAT);
    p = r; // pin = 1
    r = r && report_error_(b, featureDef(b, l + 1));
    r = p && stringList(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // stringList
  public static boolean enumRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumRange")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringList(b, l + 1);
    exit_section_(b, m, ENUM_RANGE, r);
    return r;
  }

  /* ********************************************************** */
  // implication (EQUIV equivalence)?
  public static boolean equivalence(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equivalence")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EQUIVALENCE, "<equivalence>");
    r = implication(b, l + 1);
    r = r && equivalence_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (EQUIV equivalence)?
  private static boolean equivalence_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equivalence_1")) return false;
    equivalence_1_0(b, l + 1);
    return true;
  }

  // EQUIV equivalence
  private static boolean equivalence_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equivalence_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQUIV);
    r = r && equivalence(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // featureBodyContent*
  public static boolean featureBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureBody")) return false;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_BODY, "<feature body>");
    while (true) {
      int c = current_position_(b);
      if (!featureBodyContent(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "featureBody", c)) break;
    }
    exit_section_(b, l, m, true, false, PrlParser::recoverFeatureBody);
    return true;
  }

  /* ********************************************************** */
  // DESCRIPTION elementDescription | property
  public static boolean featureBodyContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureBodyContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_BODY_CONTENT, "<feature body content>");
    r = featureBodyContent_0(b, l + 1);
    if (!r) r = property(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DESCRIPTION elementDescription
  private static boolean featureBodyContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureBodyContent_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DESCRIPTION);
    p = r; // pin = 1
    r = r && elementDescription(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean featureDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureDef")) return false;
    if (!nextTokenIs(b, "<feature def>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_DEF, "<feature def>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (versionedBoolFeatureRange | boolFeatureRange | enumFeatureRange | intFeatureRange) (LBRA featureBody RBRA)?
  public static boolean featureDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_DEFINITION, "<feature definition>");
    r = featureDefinition_0(b, l + 1);
    r = r && featureDefinition_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // versionedBoolFeatureRange | boolFeatureRange | enumFeatureRange | intFeatureRange
  private static boolean featureDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureDefinition_0")) return false;
    boolean r;
    r = versionedBoolFeatureRange(b, l + 1);
    if (!r) r = boolFeatureRange(b, l + 1);
    if (!r) r = enumFeatureRange(b, l + 1);
    if (!r) r = intFeatureRange(b, l + 1);
    return r;
  }

  // (LBRA featureBody RBRA)?
  private static boolean featureDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureDefinition_1")) return false;
    featureDefinition_1_0(b, l + 1);
    return true;
  }

  // LBRA featureBody RBRA
  private static boolean featureDefinition_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureDefinition_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRA);
    p = r; // pin = LBRA
    r = r && report_error_(b, featureBody(b, l + 1));
    r = p && consumeToken(b, RBRA) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LSQB featureRef? (COMMA featureRef)* RSQB
  public static boolean featureList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureList")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_LIST, null);
    r = consumeToken(b, LSQB);
    p = r; // pin = 1
    r = r && report_error_(b, featureList_1(b, l + 1));
    r = p && report_error_(b, featureList_2(b, l + 1)) && r;
    r = p && consumeToken(b, RSQB) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // featureRef?
  private static boolean featureList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureList_1")) return false;
    featureRef(b, l + 1);
    return true;
  }

  // (COMMA featureRef)*
  private static boolean featureList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!featureList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "featureList_2", c)) break;
    }
    return true;
  }

  // COMMA featureRef
  private static boolean featureList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && featureRef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean featureRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureRef")) return false;
    if (!nextTokenIs(b, "<feature ref>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_REF, "<feature ref>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EQ (posNegNumber | quotedString)
  public static boolean featureRestriction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureRestriction")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_RESTRICTION, null);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && featureRestriction_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // posNegNumber | quotedString
  private static boolean featureRestriction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureRestriction_1")) return false;
    boolean r;
    r = posNegNumber(b, l + 1);
    if (!r) r = quotedString(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // LSQB EQ num RSQB
  public static boolean featureVersionRestriction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "featureVersionRestriction")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FEATURE_VERSION_RESTRICTION, null);
    r = consumeTokens(b, 1, LSQB, EQ);
    p = r; // pin = 1
    r = r && report_error_(b, num(b, l + 1));
    r = p && consumeToken(b, RSQB) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FORBIDDEN FEAT featureRef (featureRestriction | featureVersionRestriction)?
  public static boolean forbiddenFeatureRule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forbiddenFeatureRule")) return false;
    if (!nextTokenIs(b, FORBIDDEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FORBIDDEN_FEATURE_RULE, null);
    r = consumeTokens(b, 1, FORBIDDEN, FEAT);
    p = r; // pin = 1
    r = r && report_error_(b, featureRef(b, l + 1));
    r = p && forbiddenFeatureRule_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (featureRestriction | featureVersionRestriction)?
  private static boolean forbiddenFeatureRule_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forbiddenFeatureRule_3")) return false;
    forbiddenFeatureRule_3_0(b, l + 1);
    return true;
  }

  // featureRestriction | featureVersionRestriction
  private static boolean forbiddenFeatureRule_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forbiddenFeatureRule_3_0")) return false;
    boolean r;
    r = featureRestriction(b, l + 1);
    if (!r) r = featureVersionRestriction(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // forbiddenFeatureRule | mandatoryFeatureRule
  static boolean forbiddenOrMandatoryFeatureRule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forbiddenOrMandatoryFeatureRule")) return false;
    if (!nextTokenIs(b, "", FORBIDDEN, MANDATORY)) return false;
    boolean r;
    r = forbiddenFeatureRule(b, l + 1);
    if (!r) r = mandatoryFeatureRule(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (OPTIONAL | MANDATORY) GROUP featureDef CONTAINS featureList (LBRA ruleBody RBRA)?
  public static boolean groupDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "groupDefinition")) return false;
    if (!nextTokenIs(b, "<group definition>", MANDATORY, OPTIONAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP_DEFINITION, "<group definition>");
    r = groupDefinition_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, GROUP));
    r = p && report_error_(b, featureDef(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, CONTAINS)) && r;
    r = p && report_error_(b, featureList(b, l + 1)) && r;
    r = p && groupDefinition_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // OPTIONAL | MANDATORY
  private static boolean groupDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "groupDefinition_0")) return false;
    boolean r;
    r = consumeToken(b, OPTIONAL);
    if (!r) r = consumeToken(b, MANDATORY);
    return r;
  }

  // (LBRA ruleBody RBRA)?
  private static boolean groupDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "groupDefinition_5")) return false;
    groupDefinition_5_0(b, l + 1);
    return true;
  }

  // LBRA ruleBody RBRA
  private static boolean groupDefinition_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "groupDefinition_5_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRA);
    p = r; // pin = 1
    r = r && report_error_(b, ruleBody(b, l + 1));
    r = p && consumeToken(b, RBRA) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // prlVersion headerProperties
  public static boolean headerContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HEADER_CONTENT, "<header content>");
    r = prlVersion(b, l + 1);
    r = r && headerProperties(b, l + 1);
    exit_section_(b, l, m, r, false, PrlParser::recoverHeaderContent);
    return r;
  }

  /* ********************************************************** */
  // HEADER LBRA headerContent RBRA
  public static boolean headerDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerDef")) return false;
    if (!nextTokenIs(b, HEADER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HEADER_DEF, null);
    r = consumeTokens(b, 1, HEADER, LBRA);
    p = r; // pin = 1
    r = r && report_error_(b, headerContent(b, l + 1));
    r = p && consumeToken(b, RBRA) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // headerProperty*
  static boolean headerProperties(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerProperties")) return false;
    Marker m = enter_section_(b, l, _NONE_);
    while (true) {
      int c = current_position_(b);
      if (!headerProperty(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "headerProperties", c)) break;
    }
    exit_section_(b, l, m, true, false, PrlParser::recoverHeaderProperties);
    return true;
  }

  /* ********************************************************** */
  // headerPropertyName propertyValue
  public static boolean headerProperty(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerProperty")) return false;
    if (!nextTokenIs(b, "<header property>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HEADER_PROPERTY, "<header property>");
    r = headerPropertyName(b, l + 1);
    p = r; // pin = 1
    r = r && propertyValue(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean headerPropertyName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerPropertyName")) return false;
    if (!nextTokenIs(b, "<header property name>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HEADER_PROPERTY_NAME, "<header property name>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENT | BTCK_IDENTIFIER
  static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    if (!nextTokenIs(b, "", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    r = consumeToken(b, IDENT);
    if (!r) r = consumeToken(b, BTCK_IDENTIFIER);
    return r;
  }

  /* ********************************************************** */
  // IF constraint (thenElsePart | thenOrThenNotPart)
  static boolean ifRule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifRule")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, constraint(b, l + 1));
    r = p && ifRule_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // thenElsePart | thenOrThenNotPart
  private static boolean ifRule_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifRule_2")) return false;
    boolean r;
    r = thenElsePart(b, l + 1);
    if (!r) r = thenOrThenNotPart(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // disj (IMPL implication)?
  public static boolean implication(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "implication")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IMPLICATION, "<implication>");
    r = disj(b, l + 1);
    r = r && implication_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (IMPL implication)?
  private static boolean implication_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "implication_1")) return false;
    implication_1_0(b, l + 1);
    return true;
  }

  // IMPL implication
  private static boolean implication_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "implication_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPL);
    r = r && implication(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LSQB featureRef IN stringList RSQB
  public static boolean inEnumPredicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inEnumPredicate")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LSQB);
    r = r && featureRef(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && stringList(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, IN_ENUM_PREDICATE, r);
    return r;
  }

  /* ********************************************************** */
  // LSQB term IN intRange RSQB
  public static boolean inIntPredicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inIntPredicate")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LSQB);
    r = r && term(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && intRange(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, IN_INT_PREDICATE, r);
    return r;
  }

  /* ********************************************************** */
  // INT FEAT featureDef intRange
  public static boolean intFeatureRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intFeatureRange")) return false;
    if (!nextTokenIs(b, INT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INT_FEATURE_RANGE, null);
    r = consumeTokens(b, 1, INT, FEAT);
    p = r; // pin = 1
    r = r && report_error_(b, featureDef(b, l + 1));
    r = p && intRange(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LSQB posNegNumber (COMMA posNegNumber)* RSQB
  public static boolean intList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intList")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LSQB);
    r = r && posNegNumber(b, l + 1);
    r = r && intList_2(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, INT_LIST, r);
    return r;
  }

  // (COMMA posNegNumber)*
  private static boolean intList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!intList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "intList_2", c)) break;
    }
    return true;
  }

  // COMMA posNegNumber
  private static boolean intList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && posNegNumber(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NOT_MINUS featureRef | posNegNumber MUL featureRef | featureRef MUL posNegNumber | posNegNumber | featureRef
  public static boolean intMul(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intMul")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INT_MUL, "<int mul>");
    r = intMul_0(b, l + 1);
    if (!r) r = intMul_1(b, l + 1);
    if (!r) r = intMul_2(b, l + 1);
    if (!r) r = posNegNumber(b, l + 1);
    if (!r) r = featureRef(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NOT_MINUS featureRef
  private static boolean intMul_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intMul_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT_MINUS);
    r = r && featureRef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // posNegNumber MUL featureRef
  private static boolean intMul_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intMul_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = posNegNumber(b, l + 1);
    r = r && consumeToken(b, MUL);
    r = r && featureRef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // featureRef MUL posNegNumber
  private static boolean intMul_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intMul_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = featureRef(b, l + 1);
    r = r && consumeToken(b, MUL);
    r = r && posNegNumber(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // intList | LSQB posNegNumber NOT_MINUS posNegNumber RSQB
  public static boolean intRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intRange")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = intList(b, l + 1);
    if (!r) r = intRange_1(b, l + 1);
    exit_section_(b, m, INT_RANGE, r);
    return r;
  }

  // LSQB posNegNumber NOT_MINUS posNegNumber RSQB
  private static boolean intRange_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intRange_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LSQB);
    r = r && posNegNumber(b, l + 1);
    r = r && consumeToken(b, NOT_MINUS);
    r = r && posNegNumber(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // intMul (ADD intMul)*
  public static boolean intSum(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intSum")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INT_SUM, "<int sum>");
    r = intMul(b, l + 1);
    r = r && intSum_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (ADD intMul)*
  private static boolean intSum_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intSum_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!intSum_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "intSum_1", c)) break;
    }
    return true;
  }

  // ADD intMul
  private static boolean intSum_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intSum_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ADD);
    r = r && intMul(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // featureRef IS constraint
  static boolean isDefRule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isDefRule")) return false;
    if (!nextTokenIs(b, "", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = featureRef(b, l + 1);
    r = r && consumeToken(b, IS);
    p = r; // pin = 2
    r = r && constraint(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NOT_MINUS lit | simp
  public static boolean lit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIT, "<lit>");
    r = lit_0(b, l + 1);
    if (!r) r = simp(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NOT_MINUS lit
  private static boolean lit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lit_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT_MINUS);
    r = r && lit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MANDATORY FEAT featureRef (featureRestriction | featureVersionRestriction)?
  public static boolean mandatoryFeatureRule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mandatoryFeatureRule")) return false;
    if (!nextTokenIs(b, MANDATORY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MANDATORY_FEATURE_RULE, null);
    r = consumeTokens(b, 1, MANDATORY, FEAT);
    p = r; // pin = 1
    r = r && report_error_(b, featureRef(b, l + 1));
    r = p && mandatoryFeatureRule_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (featureRestriction | featureVersionRestriction)?
  private static boolean mandatoryFeatureRule_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mandatoryFeatureRule_3")) return false;
    mandatoryFeatureRule_3_0(b, l + 1);
    return true;
  }

  // featureRestriction | featureVersionRestriction
  private static boolean mandatoryFeatureRule_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mandatoryFeatureRule_3_0")) return false;
    boolean r;
    r = featureRestriction(b, l + 1);
    if (!r) r = featureVersionRestriction(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // NUMBER_VAL
  public static boolean num(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "num")) return false;
    if (!nextTokenIs(b, NUMBER_VAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER_VAL);
    exit_section_(b, m, NUM, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER_VAL | NOT_MINUS NUMBER_VAL
  public static boolean posNegNumber(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "posNegNumber")) return false;
    if (!nextTokenIs(b, "<pos neg number>", NOT_MINUS, NUMBER_VAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POS_NEG_NUMBER, "<pos neg number>");
    r = consumeToken(b, NUMBER_VAL);
    if (!r) r = parseTokens(b, 0, NOT_MINUS, NUMBER_VAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PRL_VERSION version
  static boolean prlVersion(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prlVersion")) return false;
    if (!nextTokenIs(b, PRL_VERSION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, PRL_VERSION);
    p = r; // pin = 1
    r = r && version(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // propertyRef propertyValue
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, "<property>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
    r = propertyRef(b, l + 1);
    p = r; // pin = 1
    r = r && propertyValue(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean propertyRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyRef")) return false;
    if (!nextTokenIs(b, "<property ref>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_REF, "<property ref>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TRUE | FALSE | posNegNumber | intRange | quotedString | enumRange | DATE_VAL | dateRange
  static boolean propertyValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyValue")) return false;
    boolean r;
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = posNegNumber(b, l + 1);
    if (!r) r = intRange(b, l + 1);
    if (!r) r = quotedString(b, l + 1);
    if (!r) r = enumRange(b, l + 1);
    if (!r) r = consumeToken(b, DATE_VAL);
    if (!r) r = dateRange(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // QUOTED
  public static boolean quotedString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quotedString")) return false;
    if (!nextTokenIs(b, QUOTED)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUOTED);
    exit_section_(b, m, QUOTED_STRING, r);
    return r;
  }

  /* ********************************************************** */
  // !(THEN | THEN_NOT | ELSE | (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)? FEAT | RULE | OPTIONAL | MANDATORY | LBRA)
  static boolean recoverConstraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverConstraint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recoverConstraint_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // THEN | THEN_NOT | ELSE | (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)? FEAT | RULE | OPTIONAL | MANDATORY | LBRA
  private static boolean recoverConstraint_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverConstraint_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, THEN_NOT);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = recoverConstraint_0_3(b, l + 1);
    if (!r) r = consumeToken(b, RULE);
    if (!r) r = consumeToken(b, OPTIONAL);
    if (!r) r = consumeToken(b, MANDATORY);
    if (!r) r = consumeToken(b, LBRA);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)? FEAT
  private static boolean recoverConstraint_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverConstraint_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recoverConstraint_0_3_0(b, l + 1);
    r = r && consumeToken(b, FEAT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)?
  private static boolean recoverConstraint_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverConstraint_0_3_0")) return false;
    recoverConstraint_0_3_0_0(b, l + 1);
    return true;
  }

  // VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT
  private static boolean recoverConstraint_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverConstraint_0_3_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, VERSIONED, BOOL);
    if (!r) r = consumeToken(b, VERSIONED);
    if (!r) r = consumeToken(b, BOOL);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, INT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(RBRA)
  static boolean recoverFeatureBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverFeatureBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !RBRA
  static boolean recoverHeaderContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverHeaderContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(property | RBRA)
  static boolean recoverHeaderProperties(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverHeaderProperties")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recoverHeaderProperties_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // property | RBRA
  private static boolean recoverHeaderProperties_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverHeaderProperties_0")) return false;
    boolean r;
    r = property(b, l + 1);
    if (!r) r = consumeToken(b, RBRA);
    return r;
  }

  /* ********************************************************** */
  // !(RBRA)
  static boolean recoverRuleBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>>)
  static boolean recoverRuleFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleFile")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recoverRuleFile_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>>
  private static boolean recoverRuleFile_0(PsiBuilder b, int l) {
    return eof(b, l + 1);
  }

  /* ********************************************************** */
  // !((VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)? FEAT | RULE | OPTIONAL | MANDATORY | RBRA)
  static boolean recoverRuleSetElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleSetElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recoverRuleSetElement_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)? FEAT | RULE | OPTIONAL | MANDATORY | RBRA
  private static boolean recoverRuleSetElement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleSetElement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recoverRuleSetElement_0_0(b, l + 1);
    if (!r) r = consumeToken(b, RULE);
    if (!r) r = consumeToken(b, OPTIONAL);
    if (!r) r = consumeToken(b, MANDATORY);
    if (!r) r = consumeToken(b, RBRA);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)? FEAT
  private static boolean recoverRuleSetElement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleSetElement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recoverRuleSetElement_0_0_0(b, l + 1);
    r = r && consumeToken(b, FEAT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT)?
  private static boolean recoverRuleSetElement_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleSetElement_0_0_0")) return false;
    recoverRuleSetElement_0_0_0_0(b, l + 1);
    return true;
  }

  // VERSIONED BOOL | VERSIONED | BOOL | ENUM | INT
  private static boolean recoverRuleSetElement_0_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverRuleSetElement_0_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, VERSIONED, BOOL);
    if (!r) r = consumeToken(b, VERSIONED);
    if (!r) r = consumeToken(b, BOOL);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, INT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(BOOL | ENUM | DATE | INT | identifier | RBRA)
  static boolean recoverSlicingPropertyDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverSlicingPropertyDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recoverSlicingPropertyDefinition_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOL | ENUM | DATE | INT | identifier | RBRA
  private static boolean recoverSlicingPropertyDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverSlicingPropertyDefinition_0")) return false;
    boolean r;
    r = consumeToken(b, BOOL);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, DATE);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = identifier(b, l + 1);
    if (!r) r = consumeToken(b, RBRA);
    return r;
  }

  /* ********************************************************** */
  // !(RBRA)
  static boolean recoverSlicingPropertyDefinitions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recoverSlicingPropertyDefinitions")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ruleFile
  static boolean root(PsiBuilder b, int l) {
    return ruleFile(b, l + 1);
  }

  /* ********************************************************** */
  // ruleBodyContent*
  public static boolean ruleBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleBody")) return false;
    Marker m = enter_section_(b, l, _NONE_, RULE_BODY, "<rule body>");
    while (true) {
      int c = current_position_(b);
      if (!ruleBodyContent(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ruleBody", c)) break;
    }
    exit_section_(b, l, m, true, false, PrlParser::recoverRuleBody);
    return true;
  }

  /* ********************************************************** */
  // ID ruleId | DESCRIPTION elementDescription | property
  public static boolean ruleBodyContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleBodyContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RULE_BODY_CONTENT, "<rule body content>");
    r = ruleBodyContent_0(b, l + 1);
    if (!r) r = ruleBodyContent_1(b, l + 1);
    if (!r) r = property(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ID ruleId
  private static boolean ruleBodyContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleBodyContent_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ID);
    p = r; // pin = 1
    r = r && ruleId(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // DESCRIPTION elementDescription
  private static boolean ruleBodyContent_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleBodyContent_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DESCRIPTION);
    p = r; // pin = 1
    r = r && elementDescription(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // RULE (ifRule | forbiddenOrMandatoryFeatureRule | isDefRule | constraintRule) (LBRA ruleBody RBRA)?
  public static boolean ruleDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleDef")) return false;
    if (!nextTokenIs(b, RULE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RULE_DEF, null);
    r = consumeToken(b, RULE);
    p = r; // pin = 1
    r = r && report_error_(b, ruleDef_1(b, l + 1));
    r = p && ruleDef_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ifRule | forbiddenOrMandatoryFeatureRule | isDefRule | constraintRule
  private static boolean ruleDef_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleDef_1")) return false;
    boolean r;
    r = ifRule(b, l + 1);
    if (!r) r = forbiddenOrMandatoryFeatureRule(b, l + 1);
    if (!r) r = isDefRule(b, l + 1);
    if (!r) r = constraintRule(b, l + 1);
    return r;
  }

  // (LBRA ruleBody RBRA)?
  private static boolean ruleDef_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleDef_2")) return false;
    ruleDef_2_0(b, l + 1);
    return true;
  }

  // LBRA ruleBody RBRA
  private static boolean ruleDef_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleDef_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRA);
    r = r && ruleBody(b, l + 1);
    r = r && consumeToken(b, RBRA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // headerDef slicingProperties? rulesetElement* {
  // //  recoverWhile = recoverRuleFile
  // }
  public static boolean ruleFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleFile")) return false;
    if (!nextTokenIs(b, HEADER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = headerDef(b, l + 1);
    r = r && ruleFile_1(b, l + 1);
    r = r && ruleFile_2(b, l + 1);
    r = r && ruleFile_3(b, l + 1);
    exit_section_(b, m, RULE_FILE, r);
    return r;
  }

  // slicingProperties?
  private static boolean ruleFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleFile_1")) return false;
    slicingProperties(b, l + 1);
    return true;
  }

  // rulesetElement*
  private static boolean ruleFile_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleFile_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!rulesetElement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ruleFile_2", c)) break;
    }
    return true;
  }

  // {
  // //  recoverWhile = recoverRuleFile
  // }
  private static boolean ruleFile_3(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // quotedString
  public static boolean ruleId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleId")) return false;
    if (!nextTokenIs(b, QUOTED)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = quotedString(b, l + 1);
    exit_section_(b, m, RULE_ID, r);
    return r;
  }

  /* ********************************************************** */
  // featureDefinition | ruleDef | groupDefinition
  static boolean rulesetElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rulesetElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = featureDefinition(b, l + 1);
    if (!r) r = ruleDef(b, l + 1);
    if (!r) r = groupDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, PrlParser::recoverRuleSetElement);
    return r;
  }

  /* ********************************************************** */
  // atomicConstraint | constant | cc | LPAR equivalence RPAR
  public static boolean simp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMP, "<simp>");
    r = atomicConstraint(b, l + 1);
    if (!r) r = constant(b, l + 1);
    if (!r) r = cc(b, l + 1);
    if (!r) r = simp_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAR equivalence RPAR
  private static boolean simp_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simp_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAR);
    r = r && equivalence(b, l + 1);
    r = r && consumeToken(b, RPAR);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SLICING PROPERTIES LBRA slicingPropertyDefinitions RBRA
  public static boolean slicingProperties(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slicingProperties")) return false;
    if (!nextTokenIs(b, SLICING)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SLICING_PROPERTIES, null);
    r = consumeTokens(b, 1, SLICING, PROPERTIES, LBRA);
    p = r; // pin = 1
    r = r && report_error_(b, slicingPropertyDefinitions(b, l + 1));
    r = p && consumeToken(b, RBRA) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean slicingPropertyDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slicingPropertyDef")) return false;
    if (!nextTokenIs(b, "<slicing property def>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SLICING_PROPERTY_DEF, "<slicing property def>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // slicingPropertyType slicingPropertyDef
  public static boolean slicingPropertyDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slicingPropertyDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SLICING_PROPERTY_DEFINITION, "<slicing property definition>");
    r = slicingPropertyType(b, l + 1);
    p = r; // pin = 1
    r = r && slicingPropertyDef(b, l + 1);
    exit_section_(b, l, m, r, p, PrlParser::recoverSlicingPropertyDefinition);
    return r || p;
  }

  /* ********************************************************** */
  // slicingPropertyDefinition*
  static boolean slicingPropertyDefinitions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slicingPropertyDefinitions")) return false;
    Marker m = enter_section_(b, l, _NONE_);
    while (true) {
      int c = current_position_(b);
      if (!slicingPropertyDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "slicingPropertyDefinitions", c)) break;
    }
    exit_section_(b, l, m, true, false, PrlParser::recoverSlicingPropertyDefinitions);
    return true;
  }

  /* ********************************************************** */
  // BOOL | INT | DATE | ENUM
  public static boolean slicingPropertyType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slicingPropertyType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SLICING_PROPERTY_TYPE, "<slicing property type>");
    r = consumeToken(b, BOOL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, DATE);
    if (!r) r = consumeToken(b, ENUM);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LSQB quotedString (COMMA quotedString)* RSQB
  public static boolean stringList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringList")) return false;
    if (!nextTokenIs(b, LSQB)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LSQB);
    r = r && quotedString(b, l + 1);
    r = r && stringList_2(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, m, STRING_LIST, r);
    return r;
  }

  // (COMMA quotedString)*
  private static boolean stringList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!stringList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "stringList_2", c)) break;
    }
    return true;
  }

  // COMMA quotedString
  private static boolean stringList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && quotedString(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // intSum | quotedString | posNegNumber | featureRef
  public static boolean term(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "term")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TERM, "<term>");
    r = intSum(b, l + 1);
    if (!r) r = quotedString(b, l + 1);
    if (!r) r = posNegNumber(b, l + 1);
    if (!r) r = featureRef(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // THEN constraint ELSE constraint
  static boolean thenElsePart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenElsePart")) return false;
    if (!nextTokenIs(b, THEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, THEN);
    r = r && constraint(b, l + 1);
    r = r && consumeToken(b, ELSE);
    p = r; // pin = 3
    r = r && constraint(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (THEN | THEN_NOT) constraint
  static boolean thenOrThenNotPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenOrThenNotPart")) return false;
    if (!nextTokenIs(b, "", THEN, THEN_NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = thenOrThenNotPart_0(b, l + 1);
    p = r; // pin = 1
    r = r && constraint(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // THEN | THEN_NOT
  private static boolean thenOrThenNotPart_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenOrThenNotPart_0")) return false;
    boolean r;
    r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, THEN_NOT);
    return r;
  }

  /* ********************************************************** */
  // VERSION_VAL
  public static boolean version(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version")) return false;
    if (!nextTokenIs(b, VERSION_VAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VERSION_VAL);
    exit_section_(b, m, VERSION, r);
    return r;
  }

  /* ********************************************************** */
  // featureRef LSQB comparator num RSQB
  public static boolean versionPredicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "versionPredicate")) return false;
    if (!nextTokenIs(b, "<version predicate>", BTCK_IDENTIFIER, IDENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VERSION_PREDICATE, "<version predicate>");
    r = featureRef(b, l + 1);
    r = r && consumeToken(b, LSQB);
    r = r && comparator(b, l + 1);
    r = r && num(b, l + 1);
    r = r && consumeToken(b, RSQB);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VERSIONED BOOL? FEAT featureDef
  public static boolean versionedBoolFeatureRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "versionedBoolFeatureRange")) return false;
    if (!nextTokenIs(b, VERSIONED)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VERSIONED_BOOL_FEATURE_RANGE, null);
    r = consumeToken(b, VERSIONED);
    p = r; // pin = 1
    r = r && report_error_(b, versionedBoolFeatureRange_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, FEAT)) && r;
    r = p && featureDef(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // BOOL?
  private static boolean versionedBoolFeatureRange_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "versionedBoolFeatureRange_1")) return false;
    consumeToken(b, BOOL);
    return true;
  }

}
