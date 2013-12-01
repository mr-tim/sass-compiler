grammar Sass;

NL : '\r'? '\n' -> skip;
WS : (' ' | '\t' | NL) -> skip;
COMMENT: '/*' .*? '*/';
LINE_COMMENT : '//' ~[\r\n]* NL?;
STRING : '"' ('\\"' | ~'"')* '"';
URL : 'url(' ~[)]* ')';
COMMA : ',';
SEMICOLON: ';';
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
DOLLAR: '$';
EQUALS: '=';
COLON: ':';
SPACE: ' ';
IMPORT_KW : '@import';
MIXIN_KW: '@mixin';
FUNCTION_KW: '@function';
INCLUDE_KW: '@include';
DIMENSION : 'px';
NUMBER : [0-9]+;
IDENTIFIER: [a-zA-Z] [a-zA-Z0-9_-]*;
VARIABLE: '$' IDENTIFIER;

comment : COMMENT | LINE_COMMENT;

import_target: URL | STRING;

import_statement : IMPORT_KW
                   import_target ( ',' import_target )*
                   SEMICOLON;

definition : ( MIXIN_KW | FUNCTION_KW)
             IDENTIFIER
             parameter_def_list
             block_body
           ;

include_statement : INCLUDE_KW IDENTIFIER parameter_list SEMICOLON;

parameter_def_list: LPAREN ( variable_def (COMMA variable_def)* )? RPAREN;

parameter_list: LPAREN ( parameter (COMMA parameter)* )? RPAREN;

parameter: IDENTIFIER | variable_def;

variable_def: VARIABLE (COLON value_list)?;

variable: variable_def SEMICOLON;

//parser.cpp:534
block_body: LBRACE 
       (
         comment
       | import_statement // not allowed inside mixins and functions
       | assignment
       | ruleset
       | include_statement
       | variable
       )*
       RBRACE;

variable_assignment: VARIABLE COLON value_list SEMICOLON;

assignment: IDENTIFIER COLON value_list SEMICOLON;
            
value_list: value ( value )*;

value : VARIABLE | IDENTIFIER | STRING | NUMBER | value DIMENSION;

ruleset: selector_list block_body;

selector_list: selector (COMMA selector)*;

//parser.cpp:643, parser.cpp:1388
selector: IDENTIFIER;

sass_file : (
              comment
            | import_statement
            | definition
            | ruleset
            | variable
            )*;
