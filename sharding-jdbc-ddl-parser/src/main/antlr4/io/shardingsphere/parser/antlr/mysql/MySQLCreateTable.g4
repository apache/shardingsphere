grammar MySQLCreateTable;
import MySQLKeyword, DataType, Keyword, MySQLTableBase, MySQLDQL, DQLBase, MySQLBase,BaseRule,Symbol;

createTable:
    CREATE TEMPORARY? TABLE (IF NOT EXISTS)? tableName createTableOptions
    ; 

createTableOptions:
    createTableBasic
    |createTableSelect
    |createTableLike
    ;
    
createTableBasic:
    LEFT_PAREN createDefinitions RIGHT_PAREN
    tableOptions?
    partitionOptions?
    ;

createDefinitions:
    createDefinition (COMMA createDefinition)*
    ;
    
createDefinition:
    columnDefinition
    |(constraintDefinition|indexDefinition| checkExpr)
    ;
    
checkExpr:
    CHECK exprWithParen
    ;
        
createTableSelect:
    (LEFT_PAREN createDefinitions RIGHT_PAREN)?
    tableOptions?
    partitionOptions??
    (IGNORE | REPLACE)?
    AS?
    unionSelect
    ;
    
createTableLike:
    likeTable
    | LEFT_PAREN likeTable RIGHT_PAREN
    ;    

likeTable:
    LIKE tableName
    ;

 
