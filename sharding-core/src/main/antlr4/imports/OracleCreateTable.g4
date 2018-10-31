grammar OracleCreateTable;

import OracleKeyword, Keyword, DataType, OracleCreateIndex, OracleTableBase, DQLBase, OracleBase, BaseRule, Symbol;

createTable
    : CREATE (GLOBAL TEMPORARY)? TABLE tableName relationalTable
    ;
    
relationalTable
    : (LEFT_PAREN relationalProperties RIGHT_PAREN)?
    (ON COMMIT (DELETE | PRESERVE) ROWS)?
    tableProperties
    ;
    
relationalProperties
    : relationalProperty (COMMA relationalProperty)*
    ;
    
relationalProperty
    : columnDefinition
    | virtualColumnDefinition
    | outOfLineConstraint
    | outOfLineRefConstraint
    ;
    
tableProperties
    : columnProperties?
    (AS unionSelect)?
    ;
    
