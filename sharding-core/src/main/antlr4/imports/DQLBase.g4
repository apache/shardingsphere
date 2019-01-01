grammar DQLBase;

import BaseRule, Keyword, MySQLKeyword, Symbol, DataType;

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
    : GROUP BY orderByItem (COMMA orderByItem)* (WITH ROLLUP)?
    ;
    
havingClause
    : HAVING expr
    ;
    
limitClause
    : LIMIT rangeClause
    ;
    
partitionClause 
    : PARTITION idList
    ;
    
selectExprs
    : (asterisk | selectExpr) (COMMA selectExpr)*
    ; 
    
asterisk
    : ASTERISK
    ;
    
selectExpr
    :
    ;
    
tableReferences
    : 
    ;