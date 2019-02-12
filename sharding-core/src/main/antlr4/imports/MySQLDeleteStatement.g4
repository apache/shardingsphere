grammar MySQLDeleteStatement;

import MySQLKeyword, Keyword, BaseRule, MySQLSelectStatement, MySQLBase, DataType, Symbol;

delete
    : deleteClause whereClause? orderByClause? limitClause?
    ;
    
deleteClause
    : DELETE deleteSpec (fromMulti | fromSingle) 
    ;
    
fromSingle
    : FROM tableName partitionClause?
    ;
    
fromMulti
    : fromMultiTables FROM tableReferences
    | FROM fromMultiTables USING tableReferences
    ;
    
fromMultiTables
    : fromMultiTable (COMMA_ fromMultiTable)*
    ;
    
fromMultiTable
    : tableName DOT_ASTERISK_?
    ;
    
deleteSpec
    : LOW_PRIORITY? | QUICK? | IGNORE?
    ;

