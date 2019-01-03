lexer grammar BaseLexer;

import Keyword, Symbol;

STRING
    : DQ_ ('\\"'|.)*? DQ_ | SQ_ (SQ_ |.)*? SQ_
    ;

NUMBER
    : MINUS? INT_? DOT? INT_ EXP?
    ;

INT_
    : [0-9]+
    ;

EXP
    : E [+\-]? INT_
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

HEX_DIGIT
    : '0x' HEX+ | 'X' SQ_ HEX+ SQ_
    ;

BIT_NUM
    : '0b' ('0'|'1')+ | B SQ_ ('0'|'1')+ SQ_
    ;

BLOCK_COMMENT
    : SLASH ASTERISK .*? ASTERISK SLASH -> channel(HIDDEN)
    ;

SL_COMMENT
    : MINUS MINUS ~[\r\n]* -> channel(HIDDEN)
    ;

WS
    : [ \t\r\n] + ->skip
    ;
