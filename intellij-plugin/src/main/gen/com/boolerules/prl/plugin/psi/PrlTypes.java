// This is a generated file. Not intended for manual editing.
package com.boolerules.prl.plugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.boolerules.prl.plugin.psi.impl.*;

public interface PrlTypes {

  IElementType ATOMIC_CONSTRAINT = new PrlElementType("ATOMIC_CONSTRAINT");
  IElementType BOOL_FEATURE_RANGE = new PrlElementType("BOOL_FEATURE_RANGE");
  IElementType CC = new PrlElementType("CC");
  IElementType COMPARATOR = new PrlElementType("COMPARATOR");
  IElementType COMPARISON_PREDICATE = new PrlElementType("COMPARISON_PREDICATE");
  IElementType CONJ = new PrlElementType("CONJ");
  IElementType CONSTANT = new PrlElementType("CONSTANT");
  IElementType CONSTRAINT = new PrlElementType("CONSTRAINT");
  IElementType DATE_LIST = new PrlElementType("DATE_LIST");
  IElementType DATE_RANGE = new PrlElementType("DATE_RANGE");
  IElementType DISJ = new PrlElementType("DISJ");
  IElementType ELEMENT_DESCRIPTION = new PrlElementType("ELEMENT_DESCRIPTION");
  IElementType ENUM_FEATURE_RANGE = new PrlElementType("ENUM_FEATURE_RANGE");
  IElementType ENUM_RANGE = new PrlElementType("ENUM_RANGE");
  IElementType EQUIVALENCE = new PrlElementType("EQUIVALENCE");
  IElementType FEATURE_BODY = new PrlElementType("FEATURE_BODY");
  IElementType FEATURE_BODY_CONTENT = new PrlElementType("FEATURE_BODY_CONTENT");
  IElementType FEATURE_DEF = new PrlElementType("FEATURE_DEF");
  IElementType FEATURE_DEFINITION = new PrlElementType("FEATURE_DEFINITION");
  IElementType FEATURE_LIST = new PrlElementType("FEATURE_LIST");
  IElementType FEATURE_REF = new PrlElementType("FEATURE_REF");
  IElementType FEATURE_RESTRICTION = new PrlElementType("FEATURE_RESTRICTION");
  IElementType FEATURE_VERSION_RESTRICTION = new PrlElementType("FEATURE_VERSION_RESTRICTION");
  IElementType FORBIDDEN_FEATURE_RULE = new PrlElementType("FORBIDDEN_FEATURE_RULE");
  IElementType GROUP_DEFINITION = new PrlElementType("GROUP_DEFINITION");
  IElementType HEADER_CONTENT = new PrlElementType("HEADER_CONTENT");
  IElementType HEADER_DEF = new PrlElementType("HEADER_DEF");
  IElementType HEADER_PROPERTY = new PrlElementType("HEADER_PROPERTY");
  IElementType HEADER_PROPERTY_NAME = new PrlElementType("HEADER_PROPERTY_NAME");
  IElementType IMPLICATION = new PrlElementType("IMPLICATION");
  IElementType IMPORT_DEF = new PrlElementType("IMPORT_DEF");
  IElementType INT_FEATURE_RANGE = new PrlElementType("INT_FEATURE_RANGE");
  IElementType INT_LIST = new PrlElementType("INT_LIST");
  IElementType INT_MUL = new PrlElementType("INT_MUL");
  IElementType INT_RANGE = new PrlElementType("INT_RANGE");
  IElementType INT_SUM = new PrlElementType("INT_SUM");
  IElementType IN_ENUM_PREDICATE = new PrlElementType("IN_ENUM_PREDICATE");
  IElementType IN_INT_PREDICATE = new PrlElementType("IN_INT_PREDICATE");
  IElementType LIT = new PrlElementType("LIT");
  IElementType MANDATORY_FEATURE_RULE = new PrlElementType("MANDATORY_FEATURE_RULE");
  IElementType MODULE_DEF = new PrlElementType("MODULE_DEF");
  IElementType MODULE_DEFINITION = new PrlElementType("MODULE_DEFINITION");
  IElementType MODULE_REF = new PrlElementType("MODULE_REF");
  IElementType NUM = new PrlElementType("NUM");
  IElementType POS_NEG_NUMBER = new PrlElementType("POS_NEG_NUMBER");
  IElementType PROPERTY = new PrlElementType("PROPERTY");
  IElementType PROPERTY_REF = new PrlElementType("PROPERTY_REF");
  IElementType QUOTED_STRING = new PrlElementType("QUOTED_STRING");
  IElementType RULE_BODY = new PrlElementType("RULE_BODY");
  IElementType RULE_BODY_CONTENT = new PrlElementType("RULE_BODY_CONTENT");
  IElementType RULE_DEF = new PrlElementType("RULE_DEF");
  IElementType RULE_FILE = new PrlElementType("RULE_FILE");
  IElementType RULE_ID = new PrlElementType("RULE_ID");
  IElementType SIMP = new PrlElementType("SIMP");
  IElementType SLICING_PROPERTIES = new PrlElementType("SLICING_PROPERTIES");
  IElementType SLICING_PROPERTY_DEF = new PrlElementType("SLICING_PROPERTY_DEF");
  IElementType SLICING_PROPERTY_DEFINITION = new PrlElementType("SLICING_PROPERTY_DEFINITION");
  IElementType SLICING_PROPERTY_TYPE = new PrlElementType("SLICING_PROPERTY_TYPE");
  IElementType STRING_LIST = new PrlElementType("STRING_LIST");
  IElementType TERM = new PrlElementType("TERM");
  IElementType VERSION = new PrlElementType("VERSION");
  IElementType VERSIONED_BOOL_FEATURE_RANGE = new PrlElementType("VERSIONED_BOOL_FEATURE_RANGE");
  IElementType VERSION_PREDICATE = new PrlElementType("VERSION_PREDICATE");
  IElementType VIS = new PrlElementType("VIS");

  IElementType ADD = new PrlTokenType("ADD");
  IElementType AMO = new PrlTokenType("AMO");
  IElementType AND = new PrlTokenType("AND");
  IElementType BOOL = new PrlTokenType("BOOL");
  IElementType BTCK_IDENTIFIER = new PrlTokenType("BTCK_IDENTIFIER");
  IElementType COMMA = new PrlTokenType("COMMA");
  IElementType COMMENT = new PrlTokenType("COMMENT");
  IElementType CONTAINS = new PrlTokenType("CONTAINS");
  IElementType DATE = new PrlTokenType("DATE");
  IElementType DATE_VAL = new PrlTokenType("DATE_VAL");
  IElementType DESCRIPTION = new PrlTokenType("DESCRIPTION");
  IElementType DOT = new PrlTokenType("DOT");
  IElementType ELSE = new PrlTokenType("ELSE");
  IElementType ENUM = new PrlTokenType("ENUM");
  IElementType EQ = new PrlTokenType("EQ");
  IElementType EQUIV = new PrlTokenType("EQUIV");
  IElementType EXO = new PrlTokenType("EXO");
  IElementType FALSE = new PrlTokenType("FALSE");
  IElementType FEAT = new PrlTokenType("FEAT");
  IElementType FORBIDDEN = new PrlTokenType("FORBIDDEN");
  IElementType GE = new PrlTokenType("GE");
  IElementType GROUP = new PrlTokenType("GROUP");
  IElementType GT = new PrlTokenType("GT");
  IElementType HEADER = new PrlTokenType("HEADER");
  IElementType ID = new PrlTokenType("ID");
  IElementType IDENT = new PrlTokenType("IDENT");
  IElementType IF = new PrlTokenType("IF");
  IElementType IMPL = new PrlTokenType("IMPL");
  IElementType IMPORT = new PrlTokenType("IMPORT");
  IElementType IN = new PrlTokenType("IN");
  IElementType INT = new PrlTokenType("INT");
  IElementType INTERNAL = new PrlTokenType("INTERNAL");
  IElementType IS = new PrlTokenType("IS");
  IElementType LBRA = new PrlTokenType("LBRA");
  IElementType LE = new PrlTokenType("LE");
  IElementType LPAR = new PrlTokenType("LPAR");
  IElementType LSQB = new PrlTokenType("LSQB");
  IElementType LT = new PrlTokenType("LT");
  IElementType MANDATORY = new PrlTokenType("MANDATORY");
  IElementType MODULE = new PrlTokenType("MODULE");
  IElementType MUL = new PrlTokenType("MUL");
  IElementType NE = new PrlTokenType("NE");
  IElementType NOT_MINUS = new PrlTokenType("NOT_MINUS");
  IElementType NUMBER_VAL = new PrlTokenType("NUMBER_VAL");
  IElementType OPTIONAL = new PrlTokenType("OPTIONAL");
  IElementType OR = new PrlTokenType("OR");
  IElementType PRIVATE_ = new PrlTokenType("PRIVATE_");
  IElementType PRL_VERSION = new PrlTokenType("PRL_VERSION");
  IElementType PROPERTIES = new PrlTokenType("PROPERTIES");
  IElementType PUBLIC = new PrlTokenType("PUBLIC");
  IElementType QUOTED = new PrlTokenType("QUOTED");
  IElementType RBRA = new PrlTokenType("RBRA");
  IElementType RPAR = new PrlTokenType("RPAR");
  IElementType RSQB = new PrlTokenType("RSQB");
  IElementType RULE = new PrlTokenType("RULE");
  IElementType SLICING = new PrlTokenType("SLICING");
  IElementType THEN = new PrlTokenType("THEN");
  IElementType THEN_NOT = new PrlTokenType("THEN_NOT");
  IElementType TRUE = new PrlTokenType("TRUE");
  IElementType VERSIONED = new PrlTokenType("VERSIONED");
  IElementType VERSION_VAL = new PrlTokenType("VERSION_VAL");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ATOMIC_CONSTRAINT) {
        return new AtomicConstraintImpl(node);
      }
      else if (type == BOOL_FEATURE_RANGE) {
        return new BoolFeatureRangeImpl(node);
      }
      else if (type == CC) {
        return new CcImpl(node);
      }
      else if (type == COMPARATOR) {
        return new ComparatorImpl(node);
      }
      else if (type == COMPARISON_PREDICATE) {
        return new ComparisonPredicateImpl(node);
      }
      else if (type == CONJ) {
        return new ConjImpl(node);
      }
      else if (type == CONSTANT) {
        return new ConstantImpl(node);
      }
      else if (type == CONSTRAINT) {
        return new ConstraintImpl(node);
      }
      else if (type == DATE_LIST) {
        return new DateListImpl(node);
      }
      else if (type == DATE_RANGE) {
        return new DateRangeImpl(node);
      }
      else if (type == DISJ) {
        return new DisjImpl(node);
      }
      else if (type == ELEMENT_DESCRIPTION) {
        return new ElementDescriptionImpl(node);
      }
      else if (type == ENUM_FEATURE_RANGE) {
        return new EnumFeatureRangeImpl(node);
      }
      else if (type == ENUM_RANGE) {
        return new EnumRangeImpl(node);
      }
      else if (type == EQUIVALENCE) {
        return new EquivalenceImpl(node);
      }
      else if (type == FEATURE_BODY) {
        return new FeatureBodyImpl(node);
      }
      else if (type == FEATURE_BODY_CONTENT) {
        return new FeatureBodyContentImpl(node);
      }
      else if (type == FEATURE_DEF) {
        return new FeatureDefImpl(node);
      }
      else if (type == FEATURE_DEFINITION) {
        return new FeatureDefinitionImpl(node);
      }
      else if (type == FEATURE_LIST) {
        return new FeatureListImpl(node);
      }
      else if (type == FEATURE_REF) {
        return new FeatureRefImpl(node);
      }
      else if (type == FEATURE_RESTRICTION) {
        return new FeatureRestrictionImpl(node);
      }
      else if (type == FEATURE_VERSION_RESTRICTION) {
        return new FeatureVersionRestrictionImpl(node);
      }
      else if (type == FORBIDDEN_FEATURE_RULE) {
        return new ForbiddenFeatureRuleImpl(node);
      }
      else if (type == GROUP_DEFINITION) {
        return new GroupDefinitionImpl(node);
      }
      else if (type == HEADER_CONTENT) {
        return new HeaderContentImpl(node);
      }
      else if (type == HEADER_DEF) {
        return new HeaderDefImpl(node);
      }
      else if (type == HEADER_PROPERTY) {
        return new HeaderPropertyImpl(node);
      }
      else if (type == HEADER_PROPERTY_NAME) {
        return new HeaderPropertyNameImpl(node);
      }
      else if (type == IMPLICATION) {
        return new ImplicationImpl(node);
      }
      else if (type == IMPORT_DEF) {
        return new ImportDefImpl(node);
      }
      else if (type == INT_FEATURE_RANGE) {
        return new IntFeatureRangeImpl(node);
      }
      else if (type == INT_LIST) {
        return new IntListImpl(node);
      }
      else if (type == INT_MUL) {
        return new IntMulImpl(node);
      }
      else if (type == INT_RANGE) {
        return new IntRangeImpl(node);
      }
      else if (type == INT_SUM) {
        return new IntSumImpl(node);
      }
      else if (type == IN_ENUM_PREDICATE) {
        return new InEnumPredicateImpl(node);
      }
      else if (type == IN_INT_PREDICATE) {
        return new InIntPredicateImpl(node);
      }
      else if (type == LIT) {
        return new LitImpl(node);
      }
      else if (type == MANDATORY_FEATURE_RULE) {
        return new MandatoryFeatureRuleImpl(node);
      }
      else if (type == MODULE_DEF) {
        return new ModuleDefImpl(node);
      }
      else if (type == MODULE_DEFINITION) {
        return new ModuleDefinitionImpl(node);
      }
      else if (type == MODULE_REF) {
        return new ModuleRefImpl(node);
      }
      else if (type == NUM) {
        return new NumImpl(node);
      }
      else if (type == POS_NEG_NUMBER) {
        return new PosNegNumberImpl(node);
      }
      else if (type == PROPERTY) {
        return new PropertyImpl(node);
      }
      else if (type == PROPERTY_REF) {
        return new PropertyRefImpl(node);
      }
      else if (type == QUOTED_STRING) {
        return new QuotedStringImpl(node);
      }
      else if (type == RULE_BODY) {
        return new RuleBodyImpl(node);
      }
      else if (type == RULE_BODY_CONTENT) {
        return new RuleBodyContentImpl(node);
      }
      else if (type == RULE_DEF) {
        return new RuleDefImpl(node);
      }
      else if (type == RULE_FILE) {
        return new RuleFileImpl(node);
      }
      else if (type == RULE_ID) {
        return new RuleIdImpl(node);
      }
      else if (type == SIMP) {
        return new SimpImpl(node);
      }
      else if (type == SLICING_PROPERTIES) {
        return new SlicingPropertiesImpl(node);
      }
      else if (type == SLICING_PROPERTY_DEF) {
        return new SlicingPropertyDefImpl(node);
      }
      else if (type == SLICING_PROPERTY_DEFINITION) {
        return new SlicingPropertyDefinitionImpl(node);
      }
      else if (type == SLICING_PROPERTY_TYPE) {
        return new SlicingPropertyTypeImpl(node);
      }
      else if (type == STRING_LIST) {
        return new StringListImpl(node);
      }
      else if (type == TERM) {
        return new TermImpl(node);
      }
      else if (type == VERSION) {
        return new VersionImpl(node);
      }
      else if (type == VERSIONED_BOOL_FEATURE_RANGE) {
        return new VersionedBoolFeatureRangeImpl(node);
      }
      else if (type == VERSION_PREDICATE) {
        return new VersionPredicateImpl(node);
      }
      else if (type == VIS) {
        return new VisImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
