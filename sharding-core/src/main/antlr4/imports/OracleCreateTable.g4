grammar OracleCreateTable;

import OracleKeyword, Keyword, DataType, OracleCreateIndex, OracleTableBase, DQLBase, OracleBase, BaseRule, Symbol;

createTable
    : CREATE (GLOBAL TEMPORARY)? TABLE tableName relationalTable
    ;
    
relationalTable
    : (LP_ relationalProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)? tableProperties
    ;
    
relationalProperties
    : relationalProperty (COMMA relationalProperty)*
    ;
    
relationalProperty
    : columnDefinition | virtualColumnDefinition | outOfLineConstraint | outOfLineRefConstraint
    ;
    
tableProperties
    : columnProperties? (AS unionSelect)?
    ;
