grammar PostgreSQLDALStatement;

import PostgreSQLKeyword, Keyword, BaseRule, DataType, Symbol;

show
    : SHOW (ALL | ID)
    ;
    
setParam
    : SET scope? setClause
    ;
    
scope
    : SESSION | LOCAL
    ;
    
setClause
    : TIME ZONE timeZoneType
    | ID (TO | EQ_) (STRING | DEFAULT)
    ;
    
timeZoneType
    : NUMBER | LOCAL | DEFAULT
    ;
    
resetParam
    : RESET (ALL | ID)
    ;