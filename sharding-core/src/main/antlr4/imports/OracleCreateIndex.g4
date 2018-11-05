grammar OracleCreateIndex;

import OracleKeyword, Keyword, DataType, OracleBase, BaseRule, Symbol;

createIndex
    : CREATE ( UNIQUE | BITMAP)? INDEX  indexName
    ON 
    ( tableIndexClause
     | bitmapJoinIndexClause
    )
    ;
    
tableIndexClause
    : tableName alias?
    LEFT_PAREN indexExprSort
    (COMMA indexExprSort)* RIGHT_PAREN 
    ;

indexExprSort
    : indexExpr (ASC | DESC)?
    ;
    
indexExpr
    : columnName 
    | expr 
    ;
    
tablespaceClauseWithParen
    : LEFT_PAREN tablespaceClause RIGHT_PAREN
    ;
    
tablespaceClause
    : TABLESPACE tablespaceName
    ;
    
domainIndexClause
    : indexTypeName
    ;

bitmapJoinIndexClause
    : tableName
    LEFT_PAREN 
    columnSortClause( COMMA columnSortClause)*
    RIGHT_PAREN 
    FROM tableAndAlias (COMMA tableAndAlias)*
    WHERE expr
    ;