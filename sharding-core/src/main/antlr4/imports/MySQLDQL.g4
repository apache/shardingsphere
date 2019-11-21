grammar MySQLDQL;

import MySQLBase, DQLBase, MySQLKeyword, Keyword, Symbol, DataType, BaseRule;

select 
    : withClause | unionSelect
    ;
    
withClause
    : WITH RECURSIVE? cteClause (COMMA cteClause)* unionSelect
    ;
    
cteClause
    : cteName idList? AS subquery
    ;
    
selectExpression
    : selectClause fromClause? whereClause? groupByClause? havingClause?  windowClause? orderByClause? limitClause?
    ;
    
selectClause
    : SELECT selectSpec selectExprs
    ;
    
selectSpec
    : (ALL | distinct | DISTINCTROW)? HIGH_PRIORITY? STRAIGHT_JOIN? SQL_SMALL_RESULT?
    SQL_BIG_RESULT? SQL_BUFFER_RESULT? (SQL_CACHE | SQL_NO_CACHE)? SQL_CALC_FOUND_ROWS?
    ;
    
windowClause
    : WINDOW windowItem (COMMA windowItem)* 
    ;
    
windowItem
    : ID AS LP_ windowSpec RP_
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
    : tableReference(COMMA tableReference)*
    ;
    
tableReference
    : (tableFactor joinTable)+ | tableFactor joinTable+ | tableFactor
    ;
    
tableFactor
    : tableName (PARTITION idList)? (AS? alias)? indexHintList? | subquery AS? alias | LP_ tableReferences RP_
    ;
    
joinTable
    : (INNER | CROSS)? JOIN tableFactor joinCondition?
    | STRAIGHT_JOIN tableFactor
    | STRAIGHT_JOIN tableFactor joinCondition
    | (LEFT | RIGHT) OUTER? JOIN tableFactor joinCondition
    | NATURAL (INNER | (LEFT | RIGHT) (OUTER))? JOIN tableFactor
    ;
    
joinCondition
    : ON expr | USING idList
    ;
    
indexHintList
    : indexHint(COMMA indexHint)*
    ;
    
indexHint
    : (USE | IGNORE | FORCE) (INDEX | KEY) (FOR (JOIN | ORDER BY | GROUP BY))* indexList
    ;
    
selectExpr
    : (columnName | expr) AS? alias?
    | columnName DOT_ASTERISK
    ;
    
intervalExpr
    : INTERVAL expr ID
    ;
