grammar PostgreTruncateTable;
import PostgreKeyword, DataType, Keyword, PostgreBase,BaseRule,Symbol;

truncateTable:
    TRUNCATE TABLE? ONLY? tableNameParts (RESTART IDENTITY | CONTINUE IDENTITY)? (CASCADE | RESTRICT)?
    ;

tableNameParts:
    tableNamePart (COMMA tableNamePart)*
    ;

tableNamePart:
    tableName ASTERISK?
    ;