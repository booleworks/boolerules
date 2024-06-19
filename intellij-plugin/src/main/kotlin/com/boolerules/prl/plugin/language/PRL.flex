package com.boolerules.prl.plugin.language;

import com.boolerules.prl.plugin.psi.PrlTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

%%

%class PrlLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

CRLF=\R
WHITE_SPACE=[\ \n\t\f]
LBRA="\{"
RBRA="\}"
LSQB="\["
RSQB="\]"
LPAR="\("
RPAR="\)"
DOT="\."
EQ="="
NOT_MINUS="-"
AND="&"
OR="/"
IMPL="=>"
EQUIV="<=>"
AMO="amo"
EXO="exo"
COMMA="\,"
ADD="+"
MUL="*"
NE="!="
LT="<"
LE="<="
GT=">"
GE=">="

SLICING="slicing"
PROPERTIES="properties"
HEADER="header"
PRL_VERSION="prl_version"
MODULE="module"
FEAT="feature"
BOOL="bool"
INT="int"
DATE="date"
ENUM="enum"
IMPORT="import"
VERSIONED="versioned"
ID="id"
DESCRIPTION="description"
RULE="rule"
IF="if"
THEN="then"
THEN_NOT="thenNot"
ELSE="else"
OPTIONAL="optional"
GROUP="group"
IS="is"
IN="in"
CONTAINS="contains"
FORBIDDEN="forbidden"
MANDATORY="mandatory"
TRUE="true"
FALSE="false"
PUBLIC="public"
INTERNAL="internal"
PRIVATE_="private"

DATE_VAL=[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]
NUMBER_VAL=0|[1-9][0-9]*
VERSION_VAL=(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)

IDENT=[A-Za-z_][A-Za-z0-9_\-\.]*
BTCK_IDENTIFIER=`[^\r\n`]+`
QUOTED=\"[^\r\n\"]*\"

COMMENT=#[^\r\n]*

//%state EXPECT_RULE_TYPE
//%state ELEMENT_LIST
//%state RANGE_IN_ELEMENT_LIST
//%state EXPECT_FORMULA
//%state FORMULA

%%

<YYINITIAL> {
    {LBRA} { return PrlTypes.LBRA ; }
    {RBRA} { return PrlTypes.RBRA ; }
    {LSQB} { return PrlTypes.LSQB ; }
    {RSQB} { return PrlTypes.RSQB ; }
    {LPAR} { return PrlTypes.LPAR ; }
    {RPAR} { return PrlTypes.RPAR ; }
    {DOT} { return PrlTypes.DOT ; }
    {EQ} { return PrlTypes.EQ ; }
    {NOT_MINUS} { return PrlTypes.NOT_MINUS ; }
    {AND} { return PrlTypes.AND ; }
    {OR} { return PrlTypes.OR ; }
    {IMPL} { return PrlTypes.IMPL ; }
    {EQUIV} { return PrlTypes.EQUIV ; }
    {AMO} { return PrlTypes.AMO ; }
    {EXO} { return PrlTypes.EXO ; }
    {COMMA} { return PrlTypes.COMMA ; }
    {ADD} { return PrlTypes.ADD ; }
    {MUL} { return PrlTypes.MUL ; }
    {NE} { return PrlTypes.NE ; }
    {LT} { return PrlTypes.LT ; }
    {LE} { return PrlTypes.LE ; }
    {GT} { return PrlTypes.GT ; }
    {GE} { return PrlTypes.GE ; }
    {SLICING} { return PrlTypes.SLICING ; }
    {PROPERTIES} { return PrlTypes.PROPERTIES ; }
    {HEADER} { return PrlTypes.HEADER ; }
    {PRL_VERSION} { return PrlTypes.PRL_VERSION ; }
    {MODULE} { return PrlTypes.MODULE ; }
    {FEAT} { return PrlTypes.FEAT ; }
    {BOOL} { return PrlTypes.BOOL ; }
    {INT} { return PrlTypes.INT ; }
    {DATE} { return PrlTypes.DATE ; }
    {ENUM} { return PrlTypes.ENUM ; }
    {IMPORT} { return PrlTypes.IMPORT ; }
    {VERSIONED} { return PrlTypes.VERSIONED ; }
    {ID} { return PrlTypes.ID ; }
    {DESCRIPTION} { return PrlTypes.DESCRIPTION ; }
    {RULE} { return PrlTypes.RULE ; }
    {IF} { return PrlTypes.IF ; }
    {THEN} { return PrlTypes.THEN ; }
    {THEN_NOT} { return PrlTypes.THEN_NOT ; }
    {ELSE} { return PrlTypes.ELSE ; }
    {OPTIONAL} { return PrlTypes.OPTIONAL ; }
    {GROUP} { return PrlTypes.GROUP ; }
    {IS} { return PrlTypes.IS ; }
    {IN} { return PrlTypes.IN ; }
    {CONTAINS} { return PrlTypes.CONTAINS ; }
    {FORBIDDEN} { return PrlTypes.FORBIDDEN ; }
    {MANDATORY} { return PrlTypes.MANDATORY ; }
    {TRUE} { return PrlTypes.TRUE ; }
    {FALSE} { return PrlTypes.FALSE ; }
    {PUBLIC} { return PrlTypes.PUBLIC ; }
    {INTERNAL} { return PrlTypes.INTERNAL ; }
    {PRIVATE_} { return PrlTypes.PRIVATE_ ; }
    {DATE_VAL} { return PrlTypes.DATE_VAL ; }
    {NUMBER_VAL} { return PrlTypes.NUMBER_VAL ; }
    {VERSION_VAL} { return PrlTypes.VERSION_VAL ; }
    {IDENT} { return PrlTypes.IDENT ; }
    {QUOTED} { return PrlTypes.QUOTED ; }
    {BTCK_IDENTIFIER} { return PrlTypes.BTCK_IDENTIFIER ; }
}

({CRLF}|{WHITE_SPACE})+ { return TokenType.WHITE_SPACE; }

{COMMENT}+ { return PrlTypes.COMMENT; }

[^] { return TokenType.BAD_CHARACTER; }

//
//<YYINITIAL> {
//    {MODULE} { yybegin(EXPECT_IDENTIFIER); return PrlTypes.MODULE; }
//    {IMPORT} { yybegin(EXPECT_IDENTIFIER); return PrlTypes.IMPORT; }
//    {GROUP} { yybegin(EXPECT_IDENTIFIER); return PrlTypes.GROUP; }
//    {FEATURE} { yybegin(EXPECT_IDENTIFIER); return PrlTypes.FEATURE; }
//    {IN} { yybegin(EXPECT_IDENTIFIER); return PrlTypes.IN; }
//    {ID_KEYWORD} { yybegin(EXPECT_IDENTIFIER); return PrlTypes.ID_KEYWORD; }
//
//    {ATTRIBUTES} { return PrlTypes.ATTRIBUTES; }
//    {VERSION} { return PrlTypes.VERSION; }
//    {PUBLIC} { return PrlTypes.PUBLIC; }
//    {INTERNAL} { return PrlTypes.INTERNAL; }
//    {PRIVATE_} { return PrlTypes.PRIVATE_; }
//    {DESCRIPTION} { return PrlTypes.DESCRIPTION; }
//    {ATTRIBUTE_VALUES} { return PrlTypes.ATTRIBUTE_VALUES; }
//    {DATA} { return PrlTypes.DATA; }
//    {AMO_KEY} { return PrlTypes.AMO_KEY; }
//    {EXO_KEY} { return PrlTypes.EXO_KEY; }
//    {OR_KEY} { return PrlTypes.OR_KEY; }
//    {LBRACE} { return PrlTypes.LBRACE; }
//    {RBRACE} { return PrlTypes.RBRACE; }
//
//    {RULE} { yybegin(EXPECT_RULE_TYPE); return PrlTypes.RULE; }
//
//    {DEFINITION} { yybegin(EXPECT_FORMULA); return PrlTypes.DEFINITION; }
//    {CONSTRAINT} { yybegin(EXPECT_FORMULA); return PrlTypes.CONSTRAINT; }
//    {IF} { yybegin(EXPECT_FORMULA); return PrlTypes.IF; }
//    {THEN} { yybegin(EXPECT_FORMULA); return PrlTypes.THEN; }
//    {THENNOT} { yybegin(EXPECT_FORMULA); return PrlTypes.THENNOT; }
//    {ELSE} { yybegin(EXPECT_FORMULA); return PrlTypes.ELSE; }
//}
//
//<EXPECT_RULE_TYPE> {
//    {R_DEFINITION} { yybegin(YYINITIAL); return PrlTypes.R_DEFINITION; }
//    {R_CONSTRAINT} { yybegin(YYINITIAL); return PrlTypes.R_CONSTRAINT; }
//    {R_INCLUSION} { yybegin(YYINITIAL); return PrlTypes.R_INCLUSION; }
//    {R_EXCLUSION} { yybegin(YYINITIAL); return PrlTypes.R_EXCLUSION; }
//    {R_ITE} { yybegin(YYINITIAL); return PrlTypes.R_ITE; }
//}
//
//<EXPECT_FORMULA> {QUOTE} { yybegin(FORMULA); return PrlTypes.QUOTE; }
//
//<YYINITIAL> {LBRACKET} { yybegin(ELEMENT_LIST); return PrlTypes.LBRACKET; }
//<ELEMENT_LIST> {LBRACKET} { yybegin(RANGE_IN_ELEMENT_LIST); return PrlTypes.LBRACKET; }
//<ELEMENT_LIST> {DEFAULT} { return PrlTypes.DEFAULT; }
//<RANGE_IN_ELEMENT_LIST> {RBRACKET} { yybegin(ELEMENT_LIST); return PrlTypes.RBRACKET; }
//<ELEMENT_LIST> {RBRACKET} { yybegin(YYINITIAL); return PrlTypes.RBRACKET; }
//
//<FORMULA> {
//    {QUOTE} { yybegin(YYINITIAL); return PrlTypes.QUOTE; }
//
//    {SINGLE_QUOTE_LIT} { return PrlTypes.SINGLE_QUOTE_LIT; }
//    {FORMULA_TRUE} { return PrlTypes.FORMULA_TRUE; }
//    {FORMULA_FALSE} { return PrlTypes.FORMULA_FALSE; }
//    {LBRACKET} { return PrlTypes.LBRACKET; }
//    {RBRACKET} { return PrlTypes.RBRACKET; }
//    {LBRACE} { return PrlTypes.LBRACE; }
//    {RBRACE} { return PrlTypes.RBRACE; }
//    {NOT} { return PrlTypes.NOT; }
//    {OR} { return PrlTypes.OR; }
//    {AND} { return PrlTypes.AND; }
//    {IMPL} { return PrlTypes.IMPL; }
//    {EQUIV} { return PrlTypes.EQUIV; }
//    {EQ} { return PrlTypes.EQ; }
//    {NEQ} { return PrlTypes.NEQ; }
//    {LE} { return PrlTypes.LE; }
//    {LT} { return PrlTypes.LT; }
//    {GE} { return PrlTypes.GE; }
//    {GT} { return PrlTypes.GT; }
//    {AMO_OP} { return PrlTypes.AMO_OP; }
//    {EXO_OP} { return PrlTypes.EXO_OP; }
//    {IN_OP} { return PrlTypes.IN_OP; }
//    {ID} { return PrlTypes.ID; }
//}
//
//{LPAREN} { return PrlTypes.LPAREN; }
//{RPAREN} { return PrlTypes.RPAREN; }
//{DOT} { return PrlTypes.DOT; }
//{DASH} { return PrlTypes.DASH; }
//{COMMA} { return PrlTypes.COMMA; }
//{INT} { return PrlTypes.INT; }
//{STRING} { return PrlTypes.STRING; }
//{BOOLEAN} { return PrlTypes.BOOLEAN; }
//{TRUE} { return PrlTypes.TRUE; }
//{FALSE} { return PrlTypes.FALSE; }
//
//<ELEMENT_LIST> {ID} { yybegin(ELEMENT_LIST); return PrlTypes.ID; }
//<EXPECT_IDENTIFIER> {ID} { yybegin(YYINITIAL); return PrlTypes.ID; }
//<EXPECT_IDENTIFIER> {NUM} { yybegin(YYINITIAL); return PrlTypes.NUM; }
//<EXPECT_IDENTIFIER> {QUOTE} { yybegin(FORMULA); return PrlTypes.QUOTE; } // special case (hack) for feature in rule definition
//
//<YYINITIAL, ELEMENT_LIST, RANGE_IN_ELEMENT_LIST> {STRINGLIT} { return PrlTypes.STRINGLIT; }
//{NUM} { return PrlTypes.NUM; }
//
