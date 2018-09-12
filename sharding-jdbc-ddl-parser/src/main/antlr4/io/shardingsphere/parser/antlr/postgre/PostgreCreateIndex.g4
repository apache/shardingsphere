grammar PostgreCreateIndex;
import PostgreKeyword, DataType, Keyword, PostgreBase, BaseRule,Symbol;

createIndex:
    CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName usingIndexType?
    keyParts
    withStorageParameters?
    tableSpaceClause?
    whereClause?
    ;

keyParts:
    LEFT_PAREN keyPart (COMMA keyPart)* RIGHT_PAREN
    ;

keyPart:
    (columnName | simpleExpr | LEFT_PAREN simpleExpr RIGHT_PAREN) collateClause? opclass? (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;

whereClause:
    WHERE predicate
    ;