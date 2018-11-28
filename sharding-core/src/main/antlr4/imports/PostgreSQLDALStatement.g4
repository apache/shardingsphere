grammar PostgreSQLDALStatement;

import PostgreSQLKeyword, Keyword, BaseRule, DataType, Symbol;

show
    : SHOW showParam
    ;
    
showParam
    : ALL
    | ID
    ;

setParam
    : SET scope? setClause
    ;

scope
    : SESSION
    | LOCAL
    ;

setClause
    : TIME ZONE timeZoneType
    | ID (TO | EQ_) (STRING | DEFAULT)
    ;

timeZoneType
    : NUMBER
    | LOCAL
    | DEFAULT
    ;
