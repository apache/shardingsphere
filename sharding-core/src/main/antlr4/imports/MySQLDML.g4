grammar MySQLDML;

import MySQLKeyword, Keyword, BaseRule, MySQLDQL, DQLBase, MySQLBase, DataType, Symbol;

insert
    : INSERT (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE? INTO? tableName (PARTITION ignoredIdentifiers_)? (setClause | columnClause | selectClause) onDuplicateKeyClause?
    ;

setClause
    : SET assignmentList
    ;

columnClause
    : columnList? valueClause
    ;

valueClause
    : (VALUES | VALUE) assignmentValueList (COMMA assignmentValueList)*
    ;

selectClause
    : columnList? select
    ;

onDuplicateKeyClause
    : ON DUPLICATE KEY UPDATE assignmentList
    ;

update
    : updateClause setClause whereClause?
    ;

updateClause
    : UPDATE LOW_PRIORITY? IGNORE? tableReferences
    ;

delete
    : deleteClause whereClause?
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
    : fromMultiTable (COMMA fromMultiTable)*
    ;

fromMultiTable
    : tableName DOT_ASTERISK?
    ;
    
replace
    : REPLACE (LOW_PRIORITY | DELAYED)? INTO? tableName (PARTITION ignoredIdentifiers_)? (setClause | columnClause | selectClause) 
    ;
