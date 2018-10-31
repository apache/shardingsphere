grammar PostgreTruncateTable;

import PostgreKeyword, Keyword, PostgreBase, BaseRule, Symbol;

truncateTable
    : TRUNCATE TABLE? ONLY? tableNameParts
    ;

tableNameParts
    : tableNamePart (COMMA tableNamePart)*
    ;

tableNamePart
    : tableName ASTERISK?
    ;