grammar OracleBase;

import OracleKeyword,Keyword,Symbol,BaseRule,DataType;

ID: 
    (BACK_QUOTA?[a-zA-Z_$][a-zA-Z0-9_$#]* BACK_QUOTA? DOT)?
    (BACK_QUOTA?[a-zA-Z_$][a-zA-Z0-9_$#]* BACK_QUOTA?)
    |[a-zA-Z_$#0-9]+ DOT ASTERISK
    ;

oracleId
   : ID
   | (STRING DOT)* STRING
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
    : LEFT_PAREN simpleExprs RIGHT_PAREN 
    ;
    
simpleExprs
    : simpleExpr ( COMMA simpleExpr)* 
    ;

lobItem
    : attributeName
    | columnName
    ;
    
lobItems
    : lobItem (COMMA lobItem)*
    ;

lobItemList
    : LEFT_PAREN lobItems RIGHT_PAREN
    ;

dataType
    : typeName dataTypeLength?
    | specialDatatype
    | typeName dataTypeLength? datetimeTypeSuffix
    ;
    
typeName
	: DOUBLE PRECISION
	| INTERVAL YEAR
	| INTERVAL DAY
	| ID
	;
	
specialDatatype
    : typeName (LEFT_PAREN NUMBER ID  RIGHT_PAREN)
    | NATIONAL typeName (VARYING)? LEFT_PAREN NUMBER RIGHT_PAREN 
    | typeName LEFT_PAREN? columnName  RIGHT_PAREN?
    ;

datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)?
    | TO MONTH
    | TO SECOND (LEFT_PAREN NUMBER RIGHT_PAREN)?
    ;

columnSortClause
    : tableAndAlias columnName
    (ASC | DESC)?
    ;
  
 tableAndAlias
    : tableName alias?
    ;
 
 privateExprOfDb
    : treatFunction
    | caseExpr
    | intervalExpression
    | objectAccessExpression
    | constructorExpr
    ;

treatFunction
    : TREAT LEFT_PAREN expr AS REF? typeName RIGHT_PAREN
    ;

caseExpr
    : CASE ( simpleCaseExpr
     | searchedCaseExpr
     )
     elseClause?
     END
    ;
    
simpleCaseExpr
    : expr
    searchedCaseExpr+
    ;
    
searchedCaseExpr
    : WHEN expr THEN simpleExpr
    ;
    
elseClause
    : ELSE expr
    ;

dateTimeExpr
    : expr AT 
    (
         LOCAL
        | TIME ZONE (STRING | DBTIMEZONE | expr)      
    )
    ;
    
exprRecursive
    : PRIOR expr
    ;

intervalExpression
    : LEFT_PAREN expr MINUS expr RIGHT_PAREN 
    (
         DAY ( LEFT_PAREN NUMBER RIGHT_PAREN )? TO SECOND ( LEFT_PAREN NUMBER RIGHT_PAREN )?
       | YEAR ( LEFT_PAREN NUMBER RIGHT_PAREN )? TO MONTH
    )
    ;

objectAccessExpression
    : ( LEFT_PAREN simpleExpr RIGHT_PAREN |treatFunction)
    DOT
    ( 
        attributeName (DOT attributeName )* (DOT functionCall)?
        |functionCall
    )
    ;
    
constructorExpr
    : NEW typeName exprsWithParen
    ;