grammar Sass;

NL : '\r'? '\n' -> skip;
WS : (' ' | '\t' | NL) -> skip;
COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* NL? -> skip;
DSTRING : '"' ('\\"' | ~'"')* '"';
SSTRING : '\'' ('\\\'' | ~'\'')* '\'';
URL : 'url(' ~[)]* ')';
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
IMPORT_KW : '@import';
MIXIN_KW: '@mixin';
FUNCTION_KW: '@function';
INCLUDE_KW: '@include';

//CSS constants
EVEN_KW: 'even';
ODD_KW: 'odd';
PSEUDO_NOT_KW: ':not';

DIMENSION : 'px';
DIGITS: [0-9]+;
PLUS: '+';
MINUS: '-';
IDENTIFIER: [a-zA-Z][a-zA-Z0-9_-]*;
VARIABLE: '$' IDENTIFIER;
TILDE: '~';
RARROW: '>';
PIPE: '|';
CARET: '^';
HASH: '#';
PERCENT: '%';
ID_NAME: HASH IDENTIFIER;
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

parameter_def_list: LPAREN ( variable_def (COMMA variable_def)* )? RPAREN;

parameter_list: LPAREN ( parameter (COMMA parameter)* )? RPAREN;

parameter: (IDENTIFIER | variable_def | value);

variable_def: VARIABLE (COLON value_list)?;

//selectors: L309-532
//selector_schema: parser.cpp:309

//selector_group: parser.cpp:336
selector_list: selector_combination (COMMA selector_combination)*;

//selector_combination: parser.cpp:362
selector_combination: (simple_selector+)? ((PLUS | TILDE | RARROW) selector_combination)?;

//simple_selector_sequence: parser.cpp:399
//simple_selector_sequence: simple_selector+;

//simple_selector: parser.cpp:426
simple_selector: (ID_NAME | CLASS_NAME | string ) // or number
                 | type_selector // don't think this is right...
                 | negated_selector
                 | pseudo_selector
                 | attribute_selector
                 | placeholder_selector
               ;

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

binomial: integer IDENTIFIER (PLUS DIGITS)?;

//attribute_selector: parser.cpp:517
attribute_selector: LSQBRACKET type_selector ((TILDE | PIPE | STAR | CARET | DOLLAR)? EQUALS (string | IDENTIFIER))? RSQBRACKET;

type_selector: namespace_prefix? IDENTIFIER;

namespace_prefix: (IDENTIFIER | STAR) BAR;

variable: variable_def SEMICOLON;

//parser.cpp:534
block_body: LBRACE 
       (
         import_statement // not allowed inside mixins and functions
       | assignment
       | ruleset
       | include_statement
       | variable
       )*
       RBRACE;

variable_assignment: VARIABLE COLON value_list SEMICOLON;

css_identifier: MINUS? IDENTIFIER;

assignment: css_identifier COLON value_list SEMICOLON;
            
value_list: value ( value )*;

value : (VARIABLE | IDENTIFIER | string | integer ( DIMENSION | PERCENT)? );

integer: (PLUS | MINUS)? DIGITS;

ruleset: selector_list block_body;

sass_file : (
              import_statement
            | definition
            | ruleset
            | variable
            | include_statement
            )*;
