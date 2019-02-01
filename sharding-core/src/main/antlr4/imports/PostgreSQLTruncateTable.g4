grammar PostgreSQLTruncateTable;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, BaseRule, Symbol;

truncateTable
    : TRUNCATE TABLE? ONLY? tableNameParts
    ;
    
tableNameParts
    : tableNamePart (COMMA tableNamePart)*
    ;
    
tableNamePart
    : tableName ASTERISK?
    ;
