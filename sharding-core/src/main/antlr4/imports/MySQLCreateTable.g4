grammar MySQLCreateTable;

import MySQLKeyword, Keyword, MySQLDQL, MySQLTableBase, DQLBase, MySQLBase, BaseRule, DataType, Symbol;

createTable
    : CREATE TEMPORARY? TABLE (IF NOT EXISTS)? tableName createTableOptions
    ;
    
createTableOptions
    : createTableBasic | createTableSelect | createTableLike
    ;
    
createTableBasic
    : createDefinitionsWithParen tableOptions? partitionOptions?
    ;
    
createDefinitionsWithParen
    : LP_ createDefinitions RP_
    ;
    
createDefinitions
    : createDefinition (COMMA createDefinition)*
    ;
    
createDefinition
    : columnDefinition | constraintDefinition | indexDefinition | checkExpr
    ;
    
checkExpr
    : CHECK expr
    ;
    
createTableSelect
    : createDefinitionsWithParen? tableOptions? partitionOptions? (IGNORE | REPLACE)? AS? unionSelect
    ;
    
createTableLike
    : likeTable | LP_ likeTable RP_
    ;
    
likeTable
    : LIKE tableName
    ;
