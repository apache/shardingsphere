grammar DQLBase;

import BaseRule, Keyword, Symbol, DataType;

unionSelect
    : selectExpression (UNION ALL? selectExpression)*
    ;
    
selectExpression
    :
    ;
    
fromClause
    : FROM tableReferences
    ;
    
whereClause
    : WHERE expr
    ;
    
groupByClause 
    : GROUP BY orderByItem (COMMA orderByItem)* (WITH ROLLUP)? havingClause?
    ;
    
havingClause
    : HAVING  expr
    ;
    
limitClause:
    LIMIT rangeClause
    ;
    
partitionClause 
    : PARTITION idList
    ;
    
selectExprs
    : selectExpr (COMMA selectExpr)*
    ;
    
selectExpr
    :
    ;
    
tableReferences
    : 
    ;
