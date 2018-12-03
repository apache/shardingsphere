grammar PostgreSQLDALStatement;

import PostgreSQLKeyword, Keyword, BaseRule, DataType, Symbol;

show
    : SHOW showParam
    ;
    
showParam
    : ALL
    | ID
    ;
