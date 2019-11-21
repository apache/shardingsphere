grammar OracleBase;

import OracleKeyword, Keyword, Symbol, BaseRule, DataType;

ID
    : (BQ_?[a-zA-Z_$][a-zA-Z0-9_$#]* BQ_? DOT)? (BQ_?[a-zA-Z_$][a-zA-Z0-9_$#]* BQ_?)
    | [a-zA-Z_$#0-9]+ DOT_ASTERISK
    ;
    
oracleId
    : ID | (STRING DOT)* STRING
    ;
    
tableName
    : oracleId
    ;
    
columnName
    : oracleId
    ;
    
indexName
    : oracleId
    ;
    
attributeName
    : oracleId
    ;
    
indexTypeName
    : ID
    ;
    
simpleExprsWithParen
    : LP_ simpleExprs RP_ 
    ;
    
simpleExprs
    : simpleExpr ( COMMA simpleExpr)* 
    ;
    
lobItem
    : attributeName | columnName
    ;
    
lobItems
    : lobItem (COMMA lobItem)*
    ;
    
lobItemList
    : LP_ lobItems RP_
    ;
    
dataType
    : typeName dataTypeLength? | specialDatatype | typeName dataTypeLength? datetimeTypeSuffix
    ;
    
typeName
    : DOUBLE PRECISION | INTERVAL YEAR | INTERVAL DAY | ID
    ;
    
specialDatatype
    : typeName (LP_ NUMBER ID RP_) | NATIONAL typeName VARYING? LP_ NUMBER RP_ | typeName LP_? columnName RP_?
    ;
    
datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)? | TO MONTH | TO SECOND (LP_ NUMBER RP_)?
    ;
    
columnSortClause
    : tableAndAlias columnName (ASC | DESC)?
    ;
    
tableAndAlias
    : tableName alias?
    ;
    
privateExprOfDb
    : treatFunction | caseExpr | intervalExpression | objectAccessExpression | constructorExpr
    ;
    
treatFunction
    : TREAT LP_ expr AS REF? typeName RP_
    ;
    
caseExpr
    : CASE (simpleCaseExpr | searchedCaseExpr) elseClause? END
    ;
    
simpleCaseExpr
    : expr searchedCaseExpr+
    ;
    
searchedCaseExpr
    : WHEN expr THEN simpleExpr
    ;
    
elseClause
    : ELSE expr
    ;
    
dateTimeExpr
    : expr AT (LOCAL | TIME ZONE (STRING | DBTIMEZONE | expr))
    ;
    
exprRecursive
    : PRIOR expr
    ;
    
intervalExpression
    : LP_ expr MINUS expr RP_ 
    (
     DAY (LP_ NUMBER RP_)? TO SECOND (LP_ NUMBER RP_)?
     | YEAR (LP_ NUMBER RP_)? TO MONTH
    )
    ;
    
objectAccessExpression
    : (LP_ simpleExpr RP_ | treatFunction)
    DOT
    (
    attributeName (DOT attributeName )* (DOT functionCall)? | functionCall
    )
    ;
    
constructorExpr
    : NEW typeName exprsWithParen
    ;
