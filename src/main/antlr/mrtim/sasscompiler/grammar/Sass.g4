grammar Sass;

NL : '\r'? '\n' -> skip;
WS : (' ' | '\t' | NL) -> skip;
COMMENT_START: '/*';
COMMENT_END: '*/';
COMMENT: COMMENT_START .*? COMMENT_END;
LINE_COMMENT : '//' ~[\r\n]* NL? -> skip;
DSTRING : '"' ('\\"' | ~'"')* '"';
SSTRING : '\'' ('\\\'' | ~'\'')* '\'';
URL : 'url' LPAREN ~[)]* RPAREN;
COMMA : ',';
SEMICOLON: ';';
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
LSQBRACKET: '[';
RSQBRACKET: ']';
DOLLAR: '$';
EQUALS: '=';
COLON: ':';
STAR: '*';
BAR: '|';
DOT: '.';
AMPERSAND: '&';
IMPORT_KW : '@import';
MIXIN_KW: '@mixin';
FUNCTION_KW: '@function';
INCLUDE_KW: '@include';

//CSS constants
EVEN_KW: 'even';
ODD_KW: 'odd';
PSEUDO_NOT_KW: ':not';

fragment PX : 'px';
fragment PT : 'pt';
fragment EM : 'em';
fragment EN : 'en';
fragment EX : 'ex';

fragment DIGITS: [0-9]+;
PLUS: '+';
MINUS: '-';
DIVIDE: '/';

fragment ID_CHARS: [a-zA-Z0-9_-];

IDENTIFIER: [a-zA-Z] ID_CHARS*;
VARIABLE: '$' IDENTIFIER;
TILDE: '~';
RARROW: '>';
PIPE: '|';
CARET: '^';
PERCENT: '%';
HASH_ID: '#' ID_CHARS+;
CLASS_NAME: DOT IDENTIFIER;

string: DSTRING | SSTRING;

import_target: URL | string;

import_statement : IMPORT_KW
                   import_target ( ',' import_target )*
                   SEMICOLON;

definition : ( MIXIN_KW | FUNCTION_KW)
             IDENTIFIER
             parameter_def_list
             block_body
           ;

include_statement : INCLUDE_KW IDENTIFIER parameter_list? SEMICOLON;

parameter_def_list: LPAREN ( COMMENT? variable_def COMMENT? (COMMA COMMENT? variable_def COMMENT?)* )? RPAREN;

parameter_list: '(' ( parameter (COMMA parameter)* )? ')';

parameter: (IDENTIFIER | variable_def | value);

variable_def: VARIABLE (COMMENT? COLON COMMENT? expression_list)?;

//selectors: L309-532
//selector_schema: parser.cpp:309

//selector_group: parser.cpp:336
selector_list: selector_combination (COMMA selector_combination)*;

//selector_combination: parser.cpp:362
selector_combination: simple_selector (selector_combinator selector_combination)*
                    | non_blank_combinator simple_selector (selector_combinator selector_combination)*;

non_blank_combinator : (PLUS | TILDE | RARROW);

selector_combinator : (non_blank_combinator | );

//simple_selector_sequence: parser.cpp:399
//simple_selector_sequence: simple_selector+;

//simple_selector: parser.cpp:426
simple_selector: tag (simple_selector_element)*
               | simple_selector_element+;

tag: IDENTIFIER;

simple_selector_element: (HASH_ID | CLASS_NAME | AMPERSAND)
                       | negated_selector
                       | pseudo_selector
                       | attribute_selector
                       | placeholder_selector;

placeholder_selector: PERCENT IDENTIFIER;

//negated_selector: parser.cpp:453
negated_selector: PSEUDO_NOT_KW LPAREN selector_list RPAREN;

//pseudo_selector: parser.cpp:464
pseudo_selector: ((pseudo_prefix)? functional
                    ((EVEN_KW | ODD_KW)
                     | binomial
                     | IDENTIFIER
                     | string
                    )?
                  RPAREN
                 )
                 | pseudo_prefix IDENTIFIER;

pseudo_prefix: COLON COLON?;

functional: IDENTIFIER LPAREN;

binomial: NUMBER IDENTIFIER (PLUS NUMBER)?;

//attribute_selector: parser.cpp:517
attribute_selector: LSQBRACKET type_selector ((TILDE | PIPE | STAR | CARET | DOLLAR)? EQUALS (string | IDENTIFIER))? RSQBRACKET;

type_selector: namespace_prefix? IDENTIFIER;

namespace_prefix: (IDENTIFIER | STAR) BAR;

variable: variable_def SEMICOLON;

//parser.cpp:534
block_body: LBRACE 
       (
       COMMENT
       | import_statement // not allowed inside mixins and functions
       | assignment
       | ruleset
       | include_statement
       | variable
       )*
       RBRACE;

variable_assignment: VARIABLE COLON expression_list SEMICOLON;

css_identifier: MINUS? IDENTIFIER;

assignment: css_identifier COLON expression_list SEMICOLON;
            
expression_list: expression ( expression )*              # ExpressionList
               | expression_list COMMA expression_list   # MultiExpressionList;

expression: expression STAR expression      # MultiplyExpression
          | expression DIVIDE expression    # DivideExpression
          | expression PLUS expression      # PlusExpression
          | expression MINUS expression     # MinusExpression
          | value                           # ValueExpression
          | LPAREN expression RPAREN        # ParenExpression
          | LPAREN expression_list RPAREN   # ListExpression;  

value : builtin_call | (VARIABLE | IDENTIFIER | DIMENSION | PERCENTAGE | NUMBER | URL ) | string | colour;

colour: HASH_ID;

NUMBER : MINUS? DIGITS+ ('.' DIGITS+)?
       | '.' DIGITS+;

DIMENSION: NUMBER (PX | PT | EM | EN | EX );

PERCENTAGE: NUMBER '%';

builtin_call: IDENTIFIER parameter_list;

ruleset: selector_list block_body;

top_level: (
             COMMENT
           | import_statement
           | definition
           | ruleset
           | variable
           | include_statement
           );

sass_file : top_level*;
