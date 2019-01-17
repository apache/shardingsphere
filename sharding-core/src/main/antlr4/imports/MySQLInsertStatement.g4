grammar MySQLInsertStatement;

import MySQLKeyword, Keyword, BaseRule, MySQLSelectStatement, MySQLBase, DataType, Symbol;

insert
    : INSERT (LOW_PRIORITY | DELAYED | HIGH_PRIORITY IGNORE)? INTO? tableName partitionClause? (setClause | columnClause) onDuplicateClause?
    ;
    
columnClause
    : columnList? (valueClause | select)
    ;
    
valueClause
    : (VALUES | VALUE) assignmentValueList (COMMA assignmentValueList)*
    ;
    
setClause
    : SET assignmentList
    ;
    
onDuplicateClause
    : ON DUPLICATE KEY UPDATE assignmentList
    ;
