grammar PostgreTruncateTable;

import PostgreKeyword, PostgreBase, BaseRule;

truncateTable:
    TRUNCATE TABLE? ONLY? tableNameParts
    ;

tableNameParts:
    tableNamePart (COMMA tableNamePart)*
    ;

tableNamePart:
    tableName ASTERISK?
    ;