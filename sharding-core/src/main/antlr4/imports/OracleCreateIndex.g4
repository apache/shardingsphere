grammar OracleCreateIndex;

import OracleKeyword, Keyword, DataType, OracleBase, BaseRule, Symbol;

createIndex
    : CREATE ( UNIQUE | BITMAP)? INDEX indexName
    ON (tableIndexClause | bitmapJoinIndexClause)
    ;
    
tableIndexClause
    : tableName alias?
    LP_ indexExprSort
    (COMMA indexExprSort)* RP_ 
    ;
    
indexExprSort
    : indexExpr (ASC | DESC)?
    ;
    
indexExpr
    : columnName | expr 
    ;
    
tablespaceClauseWithParen
    : LP_ tablespaceClause RP_
    ;
    
tablespaceClause
    : TABLESPACE tablespaceName
    ;
    
domainIndexClause
    : indexTypeName
    ;
    
bitmapJoinIndexClause
    : tableName LP_ columnSortClause( COMMA columnSortClause)* RP_ 
    FROM tableAndAlias (COMMA tableAndAlias)*
    WHERE expr
    ;
