grammar MySQLCreateTable;
import MySQLKeyword, Keyword, MySQLDQL, MySQLTableBase, DQLBase, MySQLBase, BaseRule, DataType, Symbol;

createTable
    : CREATE TEMPORARY? TABLE ifNotExists? tableName
    createTableOptions
    ; 

createTableOptions
    : createTableBasic
    | createTableSelect
    | createTableLike
    ;
    
createTableBasic
    : createDefinitionsWithParen
    tableOptions?
    partitionOptions?
    ;

createDefinitionsWithParen
    : LEFT_PAREN createDefinitions RIGHT_PAREN
    ;

createDefinitions
    : createDefinition (COMMA createDefinition)*
    ;
    
createDefinition
    : columnDefinition
    | constraintDefinition
    | indexDefinition
    | checkExpr
    ;
    
checkExpr
    : CHECK expr
    ;
        
createTableSelect
    : createDefinitionsWithParen?
    tableOptions?
    partitionOptions?
    (IGNORE | REPLACE)?
    AS? unionSelect
    ;
    
createTableLike
    : likeTable
    | LEFT_PAREN likeTable RIGHT_PAREN
    ;    

likeTable
    : LIKE tableName
    ;

 
