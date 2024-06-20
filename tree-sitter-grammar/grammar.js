module.exports = grammar({
    name: 'prl',

    word: $ => $._ident,

    extras: $ => [$.comment, /\s/],

    conflicts: $ => [
        [$.int_mul, $.term],
        [$.in_enum_predicate, $.int_mul]
    ],

    rules: {
        source_file: $ => choice(
            seq($.header, $.slicing_props, repeat($._ruleset_content)),
            seq($.header, repeat($._ruleset_content))),

        header: $ => seq('header', '{', 'prl_version', $.number, '.', $.number, repeat($.property), '}'),

        slicing_props: $ => seq('slicing', 'properties', '{', repeat($.slicing_prop), '}'),

        slicing_prop: $ => seq(choice('bool', 'int', 'enum', 'date'), $.property_key),

        _ruleset_content: $ => choice(
            $.feature_definition,
            $.rule
        ),

        feature_definition: $ => choice(
            $.boolean_feature_definition,
            $.versioned_boolean_feature_definition,
            $.string_feature_definition,
            $.int_feature_definition
        ),

        boolean_feature_definition: $ => choice(
            seq('feature', $.identifier),
            seq('feature', $.identifier, $.feature_definition_content),
            seq('bool', 'feature', $.identifier),
            seq('bool', 'feature', $.identifier, $.feature_definition_content),
        ),

        versioned_boolean_feature_definition: $ => choice(
            seq('versioned', 'feature', $.identifier),
            seq('versioned', 'feature', $.identifier, $.feature_definition_content),
            seq('versioned', 'bool', 'feature', $.identifier),
            seq('versioned', 'bool', 'feature', $.identifier, $.feature_definition_content),
        ),

        string_feature_definition: $ => choice(
            seq('enum', 'feature', $.identifier, $.quoted_string_list),
            seq('enum', 'feature', $.identifier, $.quoted_string_list, $.feature_definition_content),
        ),

        int_feature_definition: $ => choice(
            seq('int', 'feature', $.identifier, $.int_range),
            seq('int', 'feature', $.identifier, $.int_range, $.feature_definition_content),
        ),

        feature_definition_content: $ => seq('{', repeat($._fdc), '}'),

        _fdc: $ => choice(
            $.description,
            $.property
        ),

        rule: $ => choice(
            $.constraint_rule,
            $.inclusion_rule,
            $.exclusion_rule,
            $.if_then_else_rule,
            $.definition_rule,
            $.group_rule,
            $.mandatory_feature_rule,
            $.forbidden_feature_rule
        ),

        constraint_rule: $ => choice(
            seq('rule', $.constraint),
            seq('rule', $.constraint, $.rule_definition_content)
        ),

        inclusion_rule: $ => choice(
            seq('rule', 'if', $.constraint, 'then', $.constraint),
            seq('rule', 'if', $.constraint, 'then', $.constraint, $.rule_definition_content)
        ),

        exclusion_rule: $ => choice(
            seq('rule', 'if', $.constraint, 'thenNot', $.constraint),
            seq('rule', 'if', $.constraint, 'thenNot', $.constraint, $.rule_definition_content)
        ),

        if_then_else_rule: $ => choice(
            seq('rule', 'if', $.constraint, 'then', $.constraint, 'else', $.constraint),
            seq('rule', 'if', $.constraint, 'then', $.constraint, 'else', $.constraint, $.rule_definition_content)
        ),

        definition_rule: $ => choice(
            seq('rule', $.identifier, 'is', $.constraint),
            seq('rule', $.identifier, 'is', $.constraint, $.rule_definition_content)
        ),

        group_rule: $ => choice(
            seq(choice('mandatory', 'optional'), 'group', $.identifier, 'contains', $.feature_list),
            seq(choice('mandatory', 'optional'), 'group', $.identifier, 'contains', $.feature_list, $.rule_definition_content)
        ),

        mandatory_feature_rule: $ => choice(
            seq('rule', 'mandatory', 'feature', $.identifier),
            seq('rule', 'mandatory', 'feature', $.identifier, '[', '=', $.pos_neg_number, ']'),
            seq('rule', 'mandatory', 'feature', $.identifier, '=', $.pos_neg_number),
            seq('rule', 'mandatory', 'feature', $.identifier, '=', $.quoted_string),
            seq('rule', 'mandatory', 'feature', $.identifier, $.rule_definition_content),
            seq('rule', 'mandatory', 'feature', $.identifier, '[', '=', $.pos_neg_number, ']', $.rule_definition_content),
            seq('rule', 'mandatory', 'feature', $.identifier, '=', $.pos_neg_number, $.rule_definition_content),
            seq('rule', 'mandatory', 'feature', $.identifier, '=', $.quoted_string, $.rule_definition_content)
        ),

        forbidden_feature_rule: $ => choice(
            seq('rule', 'forbidden', 'feature', $.identifier),
            seq('rule', 'forbidden', 'feature', $.identifier, '[', '=', $.pos_neg_number, ']'),
            seq('rule', 'forbidden', 'feature', $.identifier, '=', $.pos_neg_number),
            seq('rule', 'forbidden', 'feature', $.identifier, '=', $.quoted_string),
            seq('rule', 'forbidden', 'feature', $.identifier, $.rule_definition_content),
            seq('rule', 'forbidden', 'feature', $.identifier, '[', '=', $.pos_neg_number, ']', $.rule_definition_content),
            seq('rule', 'forbidden', 'feature', $.identifier, '=', $.pos_neg_number, $.rule_definition_content),
            seq('rule', 'forbidden', 'feature', $.identifier, '=', $.quoted_string, $.rule_definition_content)
        ),

        rule_definition_content: $ => seq('{', repeat($._rdc), '}'),

        _rdc: $ => choice(
            $.id,
            $.description,
            $.property
        ),

        constraint: $ => $.equiv,

        constant: $ => choice('true', 'false'),

        cc: $ => choice(
            seq('amo', $.feature_list),
            seq('exo', $.feature_list),
        ),

        simp: $ => choice(
            $.atomic_constraint,
            $.constant,
            $.cc,
            seq('(', $.equiv, ')')
        ),

        lit: $ => choice(
            seq('-', $.lit),
            $.simp,
        ),

        conj: $ => choice(
            $.lit,
            seq($.lit, repeat1(seq('&', $.lit)))
        ),

        disj: $ => choice(
            $.conj,
            seq($.conj, repeat1(seq('/', $.conj)))
        ),

        impl: $ => choice(
            $.disj,
            seq($.disj, '=>', $.disj)
        ),

        equiv: $ => choice(
            $.impl,
            seq($.impl, '<=>', $.impl)
        ),

        atomic_constraint: $ => choice(
            $.feature,
            $.version_predicate,
            $.comparison_predicate,
            $.in_enum_predicate,
            $.in_int_predicate
        ),

        feature: $ => $.identifier,

        feature_list: $ => choice(
            seq('[', ']'),
            seq('[', $.feature, repeat(seq(',', $.feature)), ']')
        ),

        version_predicate: $ => seq($.feature, '[', $.comparison, $.number, ']'),

        comparison_predicate: $ => seq('[', $.term, $.comparison, $.term, ']'),

        in_enum_predicate: $ => choice(
            seq('[', $.feature, 'in', $.quoted_string_list, ']')
        ),

        in_int_predicate: $ => choice(
            seq('[', $.int_sum, 'in', $.int_range, ']')
        ),

        term: $ => choice(
            $.feature,
            $.pos_neg_number,
            $.quoted_string,
            $.int_sum
        ),

        int_mul: $ => choice(
            $.feature,
            seq('-', $.feature),
            $.pos_neg_number,
            seq($.pos_neg_number, '*', $.feature),
            seq($.feature, '*', $.pos_neg_number)
        ),

        int_sum: $ => choice(
            $.int_mul,
            seq($.int_mul, repeat1(seq('+', $.int_mul)))
        ),

        id: $ => seq('id', $.quoted_string),

        description: $ => seq('description', $.quoted_string),

        property: $ => choice(
            seq($.property_key, 'true'),
            seq($.property_key, 'false'),
            seq($.property_key, $.pos_neg_number),
            seq($.property_key, $.int_range),
            seq($.property_key, $.quoted_string),
            seq($.property_key, $.quoted_string_list),
            seq($.property_key, $.date),
            seq($.property_key, $.date_range)
        ),

        property_key: $ => $.identifier,

        int_range: $ => choice(
            seq('i[', ']'),
            seq(choice('i[', '['), $.pos_neg_number, ']'),
            seq(choice('i[', '['), $.pos_neg_number, '-', $.pos_neg_number, ']'),
            seq(choice('i[', '['), $.pos_neg_number, ',', $.pos_neg_number, repeat(seq(',', $.pos_neg_number)), ']')
        ),

        quoted_string_list: $ => choice(
            seq('s[', ']'),
            seq(choice('s[', '['), $.quoted_string, repeat(seq(',', $.quoted_string)), ']')
        ),

        date_range: $ => choice(
            seq('d[', ']'),
            seq(choice('d[', '['), $.date, ']'),
            seq(choice('d[', '['), $.date, '-', $.date, ']'),
            seq(choice('d[', '['), $.date, ',', $.date, repeat(seq(',', $.date)), ']')
        ),

        pos_neg_number: $ => choice($.number, seq('-', $.number)),

        identifier: $ => choice(
            $._backtick_string,
            $._ident
        ),

        comment: $ => /#.+/,

        comparison: $ => choice('=', '!=', '<', '<=', '>', '>='),

        quoted_string: $ => /"(?:[^"\\]|\\.)*"/,
        number: $ => /0|[1-9][0-9]*/,
        date: $ => /[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]/,
        _ident: $ => /[A-Za-z_][A-Za-z0-9_\-.]*/,
        _backtick_string: $ => /`(?:[^`\\]|\\.)*`/,
    }
});
