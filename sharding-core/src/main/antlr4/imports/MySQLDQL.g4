grammar MySQLDQL;

import MySQLKeyword, Keyword, Symbol, DataType, BaseRule, DQLBase;

selectSpec
    : (ALL | DISTINCT | DISTINCTROW)? 
    HIGH_PRIORITY? 
    STRAIGHT_JOIN?
    SQL_SMALL_RESULT?
    SQL_BIG_RESULT?
    SQL_BUFFER_RESULT?
    (SQL_CACHE | SQL_NO_CACHE)?
    SQL_CALC_FOUND_ROWS?
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
    : WHEN booleanPrimary THEN caseResult
    ;

elseResult
    : ELSE caseResult
    ;

caseResult
    : expr
    ;

idListWithEmpty
    : LP_ RP_ | idList
    ;

tableReferences
    : tableReference(COMMA  tableReference)*
    ;

tableReference
    : (tableFactor joinTable)+ | tableFactor joinTable+ | tableFactor
    ;

tableFactor
    : tableName (PARTITION  idList)? (AS? alias)? indexHintList?  | subquery AS? alias | LP_ tableReferences RP_
    ;

joinTable
    : (INNER | CROSS)? JOIN tableFactor joinCondition?
    | STRAIGHT_JOIN tableFactor
    | STRAIGHT_JOIN tableFactor joinCondition
    | (LEFT|RIGHT) OUTER? JOIN tableFactor joinCondition
    | NATURAL (INNER | (LEFT|RIGHT) (OUTER))? JOIN tableFactor
    ;

joinCondition
    : ON expr | USING idList
    ;

indexHintList
    : indexHint(COMMA  indexHint)*
    ;

indexHint
    : USE (INDEX|KEY) (FOR (JOIN|ORDER BY|GROUP BY))* idList
    | IGNORE (INDEX|KEY) (FOR (JOIN|ORDER BY|GROUP BY))* idList
    ;

selectExpr
    : bitExpr AS? alias?
    ;
