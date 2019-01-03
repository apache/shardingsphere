grammar MySQLDML;

import MySQLKeyword, Keyword, BaseRule, MySQLDQL, DQLBase, MySQLBase, DMLBase, DataType, Symbol;

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
    
selectExpr
    : (bitExpr | caseExpress) AS? alias?
    ;
    
deleteClause
    : DELETE deleteSpec (fromMulti | fromSingle) 
    ;
    
fromSingle
    : FROM tableName partitionClause?
    ;
    
fromMulti
    : fromMultiAliases FROM  tableReferences
    | FROM fromMultiAliases USING tableReferences
    ;
    
fromMultiAliases
    : tableName DOT_ASTERISK?
    ;
    
deleteSpec
    : LOW_PRIORITY? | QUICK? | IGNORE?
    ;
    
insert
    : insertClause INTO? ID partitionClause? (setClause | columnClause) onDuplicateClause?
    ;
    
insertClause
    : INSERT insertSpec?
    ;

insertSpec
    : LOW_PRIORITY | DELAYED | HIGH_PRIORITY IGNORE
    ;

columnClause
    : idListWithEmpty? (valueClause | select)
    ;

valueClause 
    : (VALUES | VALUE) valueListWithParen (COMMA valueListWithParen)*
    ;
    
setClause
    : SET assignmentList
    ;
    
onDuplicateClause: 
    ON DUPLICATE KEY UPDATE assignmentList
    ;
    
itemListWithEmpty
    : LP_ RP_ | idList
    ;

assignmentList
    : assignment (COMMA assignment)*
    ;
    
assignment
    : columnName EQ_ value
    ;
    

