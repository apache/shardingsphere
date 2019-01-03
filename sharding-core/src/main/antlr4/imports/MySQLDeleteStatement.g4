grammar MySQLDeleteStatement;

import MySQLKeyword, Keyword, BaseRule, MySQLDQL, DQLBase, MySQLBase, DataType, Symbol;

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
    : tableName DOT_ASTERISK?
    ;
    
deleteSpec
    : LOW_PRIORITY? | QUICK? | IGNORE?
    ;

