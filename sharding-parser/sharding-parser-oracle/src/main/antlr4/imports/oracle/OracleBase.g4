grammar OracleBase;

import OracleKeyword, Keyword, Symbol, BaseRule, DataType;

ID
    : (BQ_?[a-zA-Z_$][a-zA-Z0-9_$#]* BQ_? DOT_)? (BQ_?[a-zA-Z_$][a-zA-Z0-9_$#]* BQ_?) | [a-zA-Z_$#0-9]+ DOT_ASTERISK_
    ;

oracleId
    : ID | (STRING_ DOT_)* STRING_
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
    : simpleExpr (COMMA_ simpleExpr)*
    ;

lobItem
    : attributeName | columnName
    ;

lobItems
    : lobItem (COMMA_ lobItem)*
    ;

lobItemList
    : LP_ lobItems RP_
    ;

dataType
    : dataTypeName_ dataTypeLength? | specialDatatype | dataTypeName_ dataTypeLength? datetimeTypeSuffix
    ;

specialDatatype
    : dataTypeName_ (LP_ NUMBER_ ID RP_) | NATIONAL dataTypeName_ VARYING? LP_ NUMBER_ RP_ | dataTypeName_ LP_? columnName RP_?
    ;

dataTypeName_
    : ID ID | ID
    ;

datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)? | TO MONTH | TO SECOND (LP_ NUMBER_ RP_)?
    ;

privateExprOfDb
    : treatFunction | caseExpr | intervalExpression | objectAccessExpression | constructorExpr
    ;

treatFunction
    : TREAT LP_ expr AS REF? dataTypeName_ RP_
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
    : expr AT (LOCAL | TIME ZONE (STRING_ | DBTIMEZONE | expr))
    ;

exprRecursive
    : PRIOR expr
    ;

intervalExpression
    : LP_ expr MINUS_ expr RP_ (DAY (LP_ NUMBER_ RP_)? TO SECOND (LP_ NUMBER_ RP_)? | YEAR (LP_ NUMBER_ RP_)? TO MONTH)
    ;

objectAccessExpression
    : (LP_ simpleExpr RP_ | treatFunction) DOT_ (attributeName (DOT_ attributeName)* (DOT_ functionCall)? | functionCall)
    ;

constructorExpr
    : NEW dataTypeName_ exprList
    ;
