grammar MySQLInsertStatement;

import MySQLKeyword, Keyword, BaseRule, MySQLDQL, DQLBase, MySQLBase, DataType, Symbol;

insert
    : insertClause INTO? ID partitionClause? (setClause | columnClause) onDuplicateClause?
    ;

insertClause
    : INSERT (LOW_PRIORITY | DELAYED | HIGH_PRIORITY IGNORE)?
    ;

columnClause
    : idListWithEmpty? (valueClause | select)
    ;

valueClause
    : (VALUES | VALUE) valueListWithParen (COMMA valueListWithParen)*
    ;

setClause
    : SET assignmentList
    ;

onDuplicateClause
    : ON DUPLICATE KEY UPDATE assignmentList
    ;
