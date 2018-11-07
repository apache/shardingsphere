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
    : LP_ simpleExprs RIGHT_PAREN 
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
    : LP_ lobItems RIGHT_PAREN
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
    : typeName (LP_ NUMBER ID  RIGHT_PAREN)
    | NATIONAL typeName (VARYING)? LP_ NUMBER RIGHT_PAREN 
    | typeName LP_? columnName  RIGHT_PAREN?
    ;

datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)?
    | TO MONTH
    | TO SECOND (LP_ NUMBER RIGHT_PAREN)?
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
    : TREAT LP_ expr AS REF? typeName RIGHT_PAREN
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
    : LP_ expr MINUS expr RIGHT_PAREN 
    (
         DAY ( LP_ NUMBER RIGHT_PAREN )? TO SECOND ( LP_ NUMBER RIGHT_PAREN )?
       | YEAR ( LP_ NUMBER RIGHT_PAREN )? TO MONTH
    )
    ;

objectAccessExpression
    : ( LP_ simpleExpr RIGHT_PAREN |treatFunction)
    DOT
    ( 
        attributeName (DOT attributeName )* (DOT functionCall)?
        |functionCall
    )
    ;
    
constructorExpr
    : NEW typeName exprsWithParen
    ;