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
FEAT="feature"
BOOL="bool"
INT="int"
DATE="date"
ENUM="enum"
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
    {FEAT} { return PrlTypes.FEAT ; }
    {BOOL} { return PrlTypes.BOOL ; }
    {INT} { return PrlTypes.INT ; }
    {DATE} { return PrlTypes.DATE ; }
    {ENUM} { return PrlTypes.ENUM ; }
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
