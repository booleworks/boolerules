package com.boolerules.prl.plugin.psi

import com.intellij.psi.tree.TokenSet

object PrlTokenSets {

    val IDENTIFIERS = TokenSet.create(PrlTypes.IDENT)

    val COMMENTS = TokenSet.create(PrlTypes.COMMENT)

    val STRING_LITS = TokenSet.create(PrlTypes.QUOTED_STRING)

    val LITERALS = TokenSet.create(PrlTypes.QUOTED_STRING, PrlTypes.DATE_VAL, PrlTypes.NUMBER_VAL)

    val KEYWORDS = TokenSet.create(
        PrlTypes.AMO,
        PrlTypes.AND,
        PrlTypes.BOOL,
        PrlTypes.CONTAINS,
        PrlTypes.DATE,
        PrlTypes.DESCRIPTION,
        PrlTypes.ELSE,
        PrlTypes.ENUM,
        PrlTypes.EQ,
        PrlTypes.EQUIV,
        PrlTypes.EXO,
        PrlTypes.FALSE,
        PrlTypes.FEAT,
        PrlTypes.FORBIDDEN,
        PrlTypes.GROUP,
        PrlTypes.HEADER,
        PrlTypes.ID,
        PrlTypes.IF,
        PrlTypes.IN,
        PrlTypes.INT,
        PrlTypes.IS,
        PrlTypes.MANDATORY,
        PrlTypes.OPTIONAL,
        PrlTypes.OR,
        PrlTypes.PRL_VERSION,
        PrlTypes.PROPERTIES,
        PrlTypes.RULE,
        PrlTypes.SLICING,
        PrlTypes.THEN,
        PrlTypes.THEN_NOT,
        PrlTypes.TRUE,
        PrlTypes.VERSIONED
    )
}
