grammar PostgreSQLDALStatement;

import PostgreSQLKeyword, Keyword, BaseRule, DataType, Symbol;

show
    : SHOW (ALL | ID | TRANSACTION ISOLATION LEVEL)
    ;
    
setParam
    : SET scope? setClause
    ;
    
scope
    : SESSION | LOCAL
    ;
    
setClause
    : TIME ZONE timeZoneType
    | ID (TO | EQ_) (STRING_ | DEFAULT)
    ;
    
timeZoneType
    : NUMBER_ | LOCAL | DEFAULT
    ;
    
resetParam
    : RESET (ALL | ID)
    ;