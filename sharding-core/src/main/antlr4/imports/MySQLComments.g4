lexer grammar MySQLComments;

import Symbol;

BLOCK_COMMENT
    : SLASH_ ASTERISK_ .*? ASTERISK_ SLASH_ -> channel(HIDDEN)
    ;
    
SL_COMMENT
    : MINUS_ MINUS_ ~[\r\n]* -> channel(HIDDEN)
    ;