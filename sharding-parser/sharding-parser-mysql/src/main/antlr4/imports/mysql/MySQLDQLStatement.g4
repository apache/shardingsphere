grammar MySQLDQLStatement;

import MySQLBase, MySQLKeyword, Keyword, Symbol, BaseRule, DataType;

select 
    : unionSelect | withClause_
    ;

unionSelect
    : selectExpression (UNION ALL? selectExpression)*
    ;

selectExpression
    : selectClause fromClause? whereClause? groupByClause? havingClause? windowClause_? orderByClause? limitClause?
    ;

selectClause
    : SELECT selectSpec_ selectExprs
    ;

selectSpec_
    : (ALL | distinct | DISTINCTROW)? HIGH_PRIORITY? STRAIGHT_JOIN? SQL_SMALL_RESULT? SQL_BIG_RESULT? SQL_BUFFER_RESULT? (SQL_CACHE | SQL_NO_CACHE)? SQL_CALC_FOUND_ROWS?
    ;

selectExprs
    : (asterisk | selectExpr) (COMMA_ selectExpr)*
    ; 

selectExpr
    : (columnName | expr) AS? alias? | columnName DOT_ASTERISK_
    ;

fromClause
    : FROM tableReferences
    ;

tableReferences
    : tableReference (COMMA_ tableReference)*
    ;

tableReference
    : (tableFactor joinTable)+ | tableFactor joinTable+ | tableFactor
    ;

tableFactor
    : tableName (PARTITION ignoredIdentifiers_)? (AS? alias)? indexHintList_? | subquery AS? alias | LP_ tableReferences RP_
    ;

indexHintList_
    : indexHint_(COMMA_ indexHint_)*
    ;

indexHint_
    : (USE | IGNORE | FORCE) (INDEX | KEY) (FOR (JOIN | ORDER BY | GROUP BY))* LP_ indexName (COMMA_ indexName)* RP_
    ;

joinTable
    : (INNER | CROSS)? JOIN tableFactor joinCondition?
    | STRAIGHT_JOIN tableFactor
    | STRAIGHT_JOIN tableFactor joinCondition
    | (LEFT | RIGHT) OUTER? JOIN tableFactor joinCondition
    | NATURAL (INNER | (LEFT | RIGHT) (OUTER))? JOIN tableFactor
    ;

joinCondition
    : ON expr | USING columnNames
    ;

groupByClause 
    : GROUP BY orderByItem (COMMA_ orderByItem)* (WITH ROLLUP)?
    ;

havingClause
    : HAVING expr
    ;

limitClause
    : LIMIT (rangeItem_ (COMMA_ rangeItem_)? | rangeItem_ OFFSET rangeItem_)
    ;

rangeItem_
    : number | question
    ;

windowClause_
    : WINDOW windowItem_ (COMMA_ windowItem_)*
    ;

windowItem_
    : ignoredIdentifier_ AS LP_ windowSpec RP_
    ;

subquery
    : LP_ unionSelect RP_
    ;

caseExpress
    : caseCond | caseComp
    ;

caseComp
    : CASE simpleExpr caseWhenComp+ elseResult? END
    ;

caseWhenComp
    : WHEN simpleExpr THEN caseResult
    ;

caseCond
    : CASE whenResult+ elseResult? END
    ;

whenResult
    : WHEN expr THEN caseResult
    ;

elseResult
    : ELSE caseResult
    ;

caseResult
    : expr
    ;

intervalExpr
    : INTERVAL expr ignoredIdentifier_
    ;

withClause_
    : WITH RECURSIVE? cteClause_ (COMMA_ cteClause_)* unionSelect
    ;

cteClause_
    : ignoredIdentifier_ columnNames? AS subquery
    ;
