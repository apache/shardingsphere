lexer grammar DataType;

import Symbol;

STRING_
    : DQ_ ('\\"' | .)*? DQ_ | SQ_ (SQ_ | .)*? SQ_
    ;
    
NUMBER_
    : MINUS? INT_? DOT? INT_ EXP?
    ;
    
HEX__DIGIT_
    : '0x' HEX_+ | 'X' SQ_ HEX_+ SQ_
    ;
    
BIT_NUM_
    : '0b' ('0' | '1')+ | B SQ_ ('0' | '1')+ SQ_
    ;
    
fragment INT_ 
    : [0-9]+
    ;
    
fragment EXP 
    : E [+\-]? INT_
    ;
    
fragment HEX_
    : [0-9a-fA-F] 
    ;
    
WS
    : [ \t\r\n] + ->skip
    ;