lexer grammar Symbol;

AND_
    : '&&'
    ;
    
OR_
    : '||'
    ;
    
NOT_
    : '!'
    ;

TILDE_
    : '~'
    ;

VERTICAL_BAR_
    : '|'
    ;

AMPERSAND_
    : '&'
    ;

SIGNED_LEFT_SHIFT_
    : '<<'
    ;

SIGNED_RIGHT_SHIFT_
    : '>>'
    ;

BIT_EXCLUSIVE_OR_
    : '^'
    ;

MOD_
    : '%'
    ;

COLON_
    :':'
    ;

PLUS_
    : '+'
    ;

MINUS_
    : '-'
    ;

ASTERISK_
    : '*'
    ;

SLASH_
    : '/'
    ;

BACKSLASH_
    : '\\'
    ;

DOT_
    : '.'
    ;

DOT_ASTERISK_
    : '.*'
    ;

SAFE_EQ_
    : '<=>'
    ;

DEQ_
    : '=='
    ;

EQ_
    : '='
    ;

NEQ_
    : '<>' | '!='
    ;

GT_
    : '>'
    ;

GTE_
    : '>='
    ;

LT_
    : '<'
    ;

LTE_
    : '<='
    ;

POUND_
    : '#'
    ;

LP_
    : '('
    ;

RP_
    : ')'
    ;

LBE_
    : '{'
    ;

RBE_
    : '}'
    ;

LBT_
    :'['
    ;
RBT_
    :']'
    ;
COMMA_
    : ','
    ;
DQ_
    : '"'
    ;

SQ_ : '\''
    ;

BQ_
    : '`'
    ;

UL_
    : '_'
    ;

QUESTION_
    : '?'
    ;

AT_
    : '@'
    ;

SEMI_
    :';'
    ;

fragment A: [Aa];
fragment B: [Bb];
fragment C: [Cc];
fragment D: [Dd];
fragment E: [Ee];
fragment F: [Ff];
fragment G: [Gg];
fragment H: [Hh];
fragment I: [Ii];
fragment J: [Jj];
fragment K: [Kk];
fragment L: [Ll];
fragment M: [Mm];
fragment N: [Nn];
fragment O: [Oo];
fragment P: [Pp];
fragment Q: [Qq];
fragment R: [Rr];
fragment S: [Ss];
fragment T: [Tt];
fragment U: [Uu];
fragment V: [Vv];
fragment W: [Ww];
fragment X: [Xx];
fragment Y: [Yy];
fragment Z: [Zz];
