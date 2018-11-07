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
    LP_ indexExprSort
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
    : LP_ tablespaceClause RIGHT_PAREN
    ;
    
tablespaceClause
    : TABLESPACE tablespaceName
    ;
    
domainIndexClause
    : indexTypeName
    ;

bitmapJoinIndexClause
    : tableName
    LP_ 
    columnSortClause( COMMA columnSortClause)*
    RIGHT_PAREN 
    FROM tableAndAlias (COMMA tableAndAlias)*
    WHERE expr
    ;