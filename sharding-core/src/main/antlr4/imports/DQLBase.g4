grammar DQLBase;

import BaseRule, Keyword, Symbol, DataType;

select 
    : withClause | unionSelect
    ;

withClause
    : WITH RECURSIVE? cteClause (COMMA cteClause)* unionSelect
    ;

cteClause
    : cteName idList? AS subquery
    ;

unionSelect
    : selectExpression (UNION ALL? selectExpression)*
    ;

selectExpression
    : selectClause fromClause? whereClause? groupByClause? orderByClause? limitClause?
    ;

selectClause
    : SELECT selectSpec selectExprs
    ;

selectSpec
    : 
    ;

fromClause
    : FROM tableReferences
    ;

whereClause
    : WHERE expr
    ;

groupByClause 
    : GROUP BY groupByItem (COMMA groupByItem)* (WITH ROLLUP)? havingClause?
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
    : ASTERISK (COMMA selectExpr)* | selectExpr (COMMA ASTERISK)? (COMMA selectExpr)*
    ;

subquery
    : LP_ unionSelect RP_
    ;

alias
    : ID 
    ;

selectExpr
    :
    ;

tableReferences
    : 
    ;
