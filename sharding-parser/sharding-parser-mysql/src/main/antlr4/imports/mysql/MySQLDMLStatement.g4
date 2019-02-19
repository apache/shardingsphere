grammar MySQLDMLStatement;

import MySQLKeyword, Keyword, Symbol, MySQLDQLStatement, MySQLBase, BaseRule, DataType;

insert
    : INSERT (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE? INTO? tableName (PARTITION ignoredIdentifiers_)? (setClause | columnClause) onDuplicateKeyClause?
    ;

columnClause
    : columnNames? (valueClause | select)
    ;

valueClause
    : (VALUES | VALUE) assignmentValueList (COMMA_ assignmentValueList)*
    ;

setClause
    : SET assignmentList
    ;

onDuplicateKeyClause
    : ON DUPLICATE KEY UPDATE assignmentList
    ;

update
    : updateClause setClause whereClause? orderByClause? limitClause?
    ;

updateClause
    : UPDATE LOW_PRIORITY? IGNORE? tableReferences
    ;

delete
    : deleteClause whereClause? orderByClause? limitClause?
    ;

deleteClause
    : DELETE LOW_PRIORITY? QUICK? IGNORE? (fromMulti | fromSingle) 
    ;

fromSingle
    : FROM tableName (PARTITION ignoredIdentifiers_)?
    ;

fromMulti
    : fromMultiTables FROM tableReferences | FROM fromMultiTables USING tableReferences
    ;

fromMultiTables
    : fromMultiTable (COMMA_ fromMultiTable)*
    ;

fromMultiTable
    : tableName DOT_ASTERISK_?
    ;
