grammar PostgreTruncate;
import PostgreKeyword, DataType, Keyword, PostgreBase,BaseRule,Symbol;

truncate:
    TRUNCATE TABLE? ONLY? tableNameParts (RESTART IDENTITY | CONTINUE IDENTITY)? (CASCADE | RESTRICT)?
    ;

tableNameParts:
    tableNamePart (COMMA tableNamePart)*
    ;

tableNamePart:
    tableName ASTERISK?
    ;