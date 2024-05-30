// SPDX-License-Identifier: MIT
// Copyright 2023 BooleWorks GmbH

grammar PragmaticRuleLanguage;

@header {
  package com.booleworks.prl.parser.internal;

  import com.booleworks.prl.model.rules.GroupType;
  import com.booleworks.prl.parser.*;
  import com.booleworks.prl.model.*;
  import com.booleworks.prl.model.constraints.ComparisonOperator;

  import java.util.List;
  import java.time.LocalDate;
}

@parser::members {
  private String fileName;
  private FeatureFactory ff;

  public void setFeatureFactory(final FeatureFactory ff) {
    this.ff = ff;
  }

  public PrlRuleFile ruleFile(final String fileName) {
    this.fileName = fileName;
    return rulefile().rf;
  }

  public PrlConstraint getConstraint() {
    return only_constraint().c;
  }

  public PrlRule getRule() {
    return only_rule().r;
  }

  private int lnr() {
    return getRuleContext().getStart().getLine();
  }
}

@lexer::members {
  public static String symbol(final int i) {
    return _LITERAL_NAMES[i].substring(1, _LITERAL_NAMES[i].length() - 1);
  }
}

only_rule returns [PrlRule r]
  : rule_def EOF {$r = $rule_def.r;}
  ;

only_constraint returns [PrlConstraint c]
  : constraint EOF {$c = $constraint.c;}
  ;

rulefile returns [PrlRuleFile rf]
  @init{
    List<PrlSlicingPropertyDefinition> properties = new ArrayList<>();
  }
  : header
    (SLICING PROPERTIES LBRA (sp1 = slicing_property {properties.add($slicing_property.pd);})* RBRA)?
    ruleset EOF {$rf = new PrlRuleFile($header.h, $ruleset.rs, properties, this.fileName);}
  ;

header returns [PrlHeader h]
  @init{ List<PrlProperty<?>> properties = new ArrayList<>(); }
  : HEADER LBRA PRL_VERSION major = number '.' minor = number
    (property {properties.add($property.p);})* RBRA {$h = new PrlHeader($major.i, $minor.i, properties);}
  ;

slicing_property returns [PrlSlicingPropertyDefinition pd]
  : BOOL identifier  {$pd = new PrlSlicingBooleanPropertyDefinition($identifier.i, lnr());}
  | INT identifier {$pd = new PrlSlicingIntPropertyDefinition($identifier.i, lnr());}
  | DATE identifier {$pd = new PrlSlicingDatePropertyDefinition($identifier.i, lnr());}
  | ENUM identifier {$pd = new PrlSlicingEnumPropertyDefinition($identifier.i, lnr());}
  ;

ruleset returns [PrlRuleSet rs]
  : con = ruleset_content {$rs = $con.rsc.generateRuleSet(lnr());}
  ;

ruleset_content returns [PrlRuleSetContent rsc]
  @init{PrlRuleSetContent content = new PrlRuleSetContent();}
  : (fea = feature_def {content.addFeature($fea.f);}
     | rul = rule_def {content.addRule($rul.r);})*
     {$rsc = content;}
  ;

feature_def returns [PrlFeatureDefinition f]
  @init{PrlFeatureContent c = new PrlFeatureContent();}
  : (VERS {c.setVs(true);})? (BOOL)? FEATURE identifier
    (LBRA (DESC quoted_string {c.setDesc($quoted_string.s);} | property {c.addProperty($property.p);})* RBRA)?
    {c.setLnr(lnr()); $f = c.generateBoolFeature($identifier.i);}
  | ENUM FEATURE identifier string_list
    (LBRA (DESC quoted_string {c.setDesc($quoted_string.s);} | property {c.addProperty($property.p);})* RBRA)?
    {c.setLnr(lnr()); $f = c.generateEnumFeature($identifier.i, $string_list.ls);}
  | INT FEATURE identifier int_range
    (LBRA (DESC quoted_string {c.setDesc($quoted_string.s);} | property {c.addProperty($property.p);})* RBRA)?
    {c.setLnr(lnr()); $f = c.generateIntFeature($identifier.i, $int_range.r);}
  ;

rule_def returns [PrlRule r]
  @init{PrlRuleContent c = new PrlRuleContent();}
  : RULE c = constraint
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateConstraintRule($c.c);}
  | RULE IF c1 = constraint THEN c2 = constraint
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateInclusionRule($c1.c, $c2.c);}
  | RULE IF c1 = constraint THEN_NOT c2 = constraint
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateExclusionRule($c1.c, $c2.c);}
  | RULE IF c1 = constraint THEN c2 = constraint ELSE c3 = constraint
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateIfThenElseRule($c1.c, $c2.c, $c3.c);}
  | RULE f = feature IS c2 = constraint
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateDefinitionRule($f.f, $c2.c);}
  | OPTIONAL GROUP g = feature CONTAINS gl = feature_list
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateGroupRule(GroupType.OPTIONAL, $g.f, $gl.ls);}
  | MANDATORY GROUP g = feature CONTAINS gl = feature_list
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateGroupRule(GroupType.MANDATORY, $g.f, $gl.ls);}
  | forbidden_feature_rule {$r = $forbidden_feature_rule.r;}
  | mandatory_feature_rule {$r = $mandatory_feature_rule.r;}
  ;

forbidden_feature_rule returns [PrlForbiddenFeatureRule r]
  @init{PrlRuleContent c = new PrlRuleContent();}
  : RULE FORBIDDEN FEATURE b = feature
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateForbiddenFeatureRule($b.f, null, null);}
  | RULE FORBIDDEN FEATURE v = feature LSQB EQ n = number RSQB
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateForbiddenFeatureRule($v.f, null, $n.i);}
  | RULE FORBIDDEN FEATURE i = feature EQ pn = pos_neg_number
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateForbiddenFeatureRule($i.f, null, $pn.i);}
  | RULE FORBIDDEN FEATURE e = feature EQ q = quoted_string
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateForbiddenFeatureRule($e.f, $q.s, null);}
  ;

mandatory_feature_rule returns [PrlMandatoryFeatureRule r]
  @init{PrlRuleContent c = new PrlRuleContent();}
  : RULE MANDATORY FEATURE b = feature
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateMandatoryFeatureRule($b.f, null, null);}
  | RULE MANDATORY FEATURE v = feature LSQB EQ n = number RSQB
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateMandatoryFeatureRule($v.f, null, $n.i);}
  | RULE MANDATORY FEATURE i = feature EQ pn = pos_neg_number
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateMandatoryFeatureRule($i.f, null, $pn.i);}
  | RULE MANDATORY FEATURE e = feature EQ q = quoted_string
    (LBRA (ID s = quoted_string {c.setId($s.s);} | DESC s = quoted_string {c.setDesc($s.s);} | p = property {c.addProperty($p.p);})* RBRA)?
    {c.setLnr(lnr()); $r = c.generateMandatoryFeatureRule($e.f, $q.s, null);}
  ;

property returns [PrlProperty<?> p]
  : identifier TRUE {$p = new PrlBooleanProperty($identifier.i, BooleanRange.Companion.list(true), lnr());}
  | identifier FALSE {$p = new PrlBooleanProperty($identifier.i, BooleanRange.Companion.list(false), lnr());}
  | identifier pos_neg_number {$p = new PrlIntProperty($identifier.i, IntRange.Companion.list($pos_neg_number.i), lnr());}
  | identifier int_range {$p = new PrlIntProperty($identifier.i, $int_range.r, lnr());}
  | identifier quoted_string {$p = new PrlEnumProperty($identifier.i, $quoted_string.s, lnr());}
  | identifier enum_range {$p = new PrlEnumProperty($identifier.i, $enum_range.r, lnr());}
  | identifier date {$p = new PrlDateProperty($identifier.i, $date.d, lnr());}
  | identifier date_range {$p = new PrlDateProperty($identifier.i, $date_range.r, lnr());}
  ;
// Constraints
constraint returns [PrlConstraint c]
  : equiv {$c = $equiv.c;};

constant returns [PrlConstraint c]
  : TRUE  {$c = new PrlConstant(true);}
  | FALSE {$c = new PrlConstant(false);};

simp returns [PrlConstraint c]
  :	atomic_constraint  {$c = $atomic_constraint.a;}
  |	constant           {$c = $constant.c;}
  | cc                 {$c = $cc.c;}
  | LPAR equiv RPAR    {$c = $equiv.c;};

lit returns [PrlConstraint c]
  :	NOT_MINUS a = lit {$c = new PrlNot($a.c);}
  |	simp              {$c = $simp.c;};

conj returns [PrlConstraint c]
  @init{List<PrlConstraint> ops = new ArrayList<>(); }
	:	a = lit {ops.add($a.c);} (AND b = lit {ops.add($b.c);})* {$c = ops.size() == 1 ? ops.get(0) : new PrlAnd(ops);};

disj returns [PrlConstraint c]
  @init{List<PrlConstraint> ops = new ArrayList<>(); }
  :	a = conj {ops.add($a.c);} (OR b = conj {ops.add($b.c);})* {$c = ops.size() == 1 ? ops.get(0) : new PrlOr(ops);};

impl returns [PrlConstraint c]
  @init{PrlConstraint[] ops = new PrlConstraint[2];}
  :	a = disj {ops[0] =$a.c;} (IMPL b = impl {ops[1] = $b.c;})? {$c = ops[1] == null ? ops[0] : new PrlImplication(ops[0], ops[1]);};

equiv returns [PrlConstraint c]
  @init{PrlConstraint[] ops = new PrlConstraint[2];}
  :	a = impl {ops[0] =$a.c;} (EQUIV b = equiv {ops[1] = $b.c;})? {$c = ops[1] == null ? ops[0] : new PrlEquivalence(ops[0], ops[1]);};

cc returns [PrlConstraint c]
  : AMO l = feature_list {$c = new PrlAmo($l.ls);}
  | EXO l = feature_list {$c = new PrlExo($l.ls);}
  ;

// Atoms
atomic_constraint returns [PrlConstraint a]
  : b = feature {$a = $b.f;}
  | v = version_predicate {$a = $v.a;}
  | c = comparison_predicate {$a = $c.p;}
  | e = in_enum_predicate {$a = $e.c;}
  | i = in_int_predicate {$a = $i.c;}
  ;

feature returns [PrlFeature f]
  : ff = identifier {$f = this.ff.getOrAddFeature($ff.i);}
  ;

version_predicate returns [PrlComparisonPredicate a]
  : f = feature LSQB comparator number RSQB {$a = new PrlComparisonPredicate($comparator.c, $f.f, new PrlIntValue($number.i));}
  ;

feature_list returns [List<PrlFeature> ls]
  @init{List<PrlFeature> list = new ArrayList<>();}
  : LSQB (f = feature {list.add($f.f);})? (COMMA g = feature {list.add($g.f);})* RSQB {$ls = list;}
  ;

comparison_predicate returns [PrlComparisonPredicate p]
  : LSQB t1 = term comparator t2 = term  RSQB {$p = new PrlComparisonPredicate($comparator.c, $t1.t, $t2.t);}
  ;

in_enum_predicate returns [PrlInEnumsPredicate c]
  : LSQB f = feature IN sl = string_list RSQB {$c = new PrlInEnumsPredicate($f.f, $sl.ls);}
  ;

in_int_predicate returns [PrlInIntRangePredicate c]
  : LSQB t = term IN ir = int_range RSQB {$c = new PrlInIntRangePredicate($t.t, $ir.r);}
  ;

term returns [PrlTerm t]
    : b = feature {$t = $b.f;}
    | n = pos_neg_number {$t = new PrlIntValue($n.i);}
    | e = quoted_string {$t = new PrlEnumValue($e.s);}
    | s = int_sum {$t = $s.t;}
    ;

int_mul returns [PrlTerm t]
  :  feature {$t = $feature.f;}
  |  NOT_MINUS feature {$t = new PrlIntMulFunction(new PrlIntValue(-1), $feature.f);}
  |  n = pos_neg_number {$t = new PrlIntValue($n.i);}
  |  n = pos_neg_number MUL feature {$t = new PrlIntMulFunction(new PrlIntValue($n.i), $feature.f);}
  |  feature MUL n = pos_neg_number  {$t = new PrlIntMulFunction($feature.f, new PrlIntValue($n.i));}
  ;

int_sum returns [PrlTerm t]
    @init{List<PrlTerm> list = new ArrayList<>();}
    : (t1 = int_mul {list.add($t1.t);}) (ADD t2 = int_mul {list.add($t2.t);})* {$t = list.size() == 1 ? list.get(0) : new PrlIntAddFunction(list);}
    ;

// Primitive data structures
identifier returns [String i]
  : IDENTIFIER {$i = $IDENTIFIER.text;}
  | BTCK_IDENTIFIER {$i = $BTCK_IDENTIFIER.text.substring(1, $BTCK_IDENTIFIER.text.length() - 1);}
  ;

string_list returns [List<String> ls]
  @init{List<String> list = new ArrayList<>();}
  : LSQB (quoted_string {list.add($quoted_string.s);}) (COMMA quoted_string {list.add($quoted_string.s);})* RSQB {$ls = list;}
  ;

quoted_string returns [String s]
  : QUOTED {$s = $QUOTED.text.substring(1, $QUOTED.text.length() - 1);}
  ;

enum_range returns [EnumRange r]
  : string_list {$r = EnumRange.Companion.list($string_list.ls);}
  ;

int_range returns [IntRange r]
  : int_list {$r = IntRange.Companion.list($int_list.ls);}
  | LSQB lb = pos_neg_number '-' ub = pos_neg_number RSQB {$r = IntRange.Companion.interval($lb.i, $ub.i);}
  ;

int_list returns [List<Integer> ls]
  @init{List<Integer> list = new ArrayList<>();}
  : LSQB n = pos_neg_number {list.add($n.i);} (COMMA n = pos_neg_number {list.add($n.i);})* RSQB {$ls = list;}
  ;

pos_neg_number returns [int i]
  : NUMBER_VAL {$i = Integer.parseInt($NUMBER_VAL.text);}
  | NOT_MINUS NUMBER_VAL {$i = -Integer.parseInt($NUMBER_VAL.text);}
  ;

number returns [int i]
  : NUMBER_VAL {$i = Integer.parseInt($NUMBER_VAL.text);}
  ;

date returns [LocalDate d]
  : DATE_VAL { $d = LocalDate.parse($DATE_VAL.text);}
  ;

date_range returns [DateRange r]
  : date_list {$r = DateRange.Companion.list($date_list.ls);}
  | LSQB lb = date '-' ub = date RSQB {$r = DateRange.Companion.interval($lb.d, $ub.d);}
  ;

date_list returns [List<LocalDate> ls]
  @init{List<LocalDate> list = new ArrayList<>();}
  : LSQB (date {list.add($date.d);}) (COMMA date {list.add($date.d);})* RSQB {$ls = list;}
  ;

comparator returns [ComparisonOperator c]
  : EQ {$c = ComparisonOperator.EQ;}
  | NE {$c = ComparisonOperator.NE;}
  | LT {$c = ComparisonOperator.LT;}
  | LE {$c = ComparisonOperator.LE;}
  | GT {$c = ComparisonOperator.GT;}
  | GE {$c = ComparisonOperator.GE;}
  ;

// Top tokens to be prioritized before all others
// Identifier in backticks
BTCK_IDENTIFIER : BACKTICK~[\r\n`]+BACKTICK;
// Full quotation of a string without line breaks
QUOTED      : QUOTE~[\r\n"]*QUOTE;

// Special Symbols
LPAR        : '(';
RPAR        : ')';
LBRA        : '{';
RBRA        : '}';
LSQB        : '[';
RSQB        : ']';
COMMA       : ',';

// Keywords
HEADER      : 'header';
PRL_VERSION : 'prl_version';
FEATURE     : 'feature';
RULE        : 'rule';
SLICING     : 'slicing';
PROPERTIES  : 'properties';

DATE        : 'date';
VERS        : 'versioned';
BOOL        : 'bool';
INT         : 'int';
ENUM        : 'enum';
DESC        : 'description';
ID          : 'id';

// Contraint Language
TRUE        : 'true';
FALSE       : 'false';
IF          : 'if';
THEN        : 'then';
THEN_NOT    : 'thenNot';
ELSE        : 'else';
IS          : 'is';
CONTAINS    : 'contains';
AMO         : 'amo';
EXO         : 'exo';
GROUP       : 'group';
OPTIONAL    : 'optional';
MANDATORY   : 'mandatory';
FORBIDDEN   : 'forbidden';
IN          : 'in';

NOT_MINUS   : '-';
AND         : '&';
OR          : '/';
EQUIV       : '<=>';
IMPL        : '=>';

EQ          : '=';
NE          : '!=';
LT          : '<';
LE          : '<=';
GT          : '>';
GE          : '>=';

ADD         : '+';
MUL         : '*';

// Identifiers, Numbers, ...
QUOTE      : '"';
BACKTICK   : '`';
DATE_VAL       : [0-9][0-9][0-9][0-9]'-'[0-1][0-9]'-'[0-3][0-9];
NUMBER_VAL     : '0'|[1-9][0-9]*;
IDENTIFIER : [A-Za-z_][A-Za-z0-9_\-.]*;

// Comments and Whitespace
WS         : [ \t\r\n]+ -> skip;
COMMENT    : '#' ~[\r\n]* '\r'? '\n' -> skip ;
