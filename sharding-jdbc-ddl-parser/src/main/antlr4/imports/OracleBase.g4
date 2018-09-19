grammar OracleBase;

import OracleKeyword,Keyword,Symbol,BaseRule,DataType;


attributeName: ID;
segName: ID;

objectName:ID;
elementName: ID;
archiveName: ID;
indexTypeName: ID;
clusterName: ID;
varrayItemName: ID;

simpleExprsWithParen:
	LEFT_PAREN simpleExprs RIGHT_PAREN 
	;
	
simpleExprs:
	simpleExpr ( COMMA simpleExpr)* 
	;

lobItem:
	attributeName
	|columnName
	;
	
lobItems:
	lobItem (COMMA lobItem)*
	;

lobItemList:
	LEFT_PAREN lobItems RIGHT_PAREN
	;

dataType: 
	(typeName(LEFT_PAREN NUMBER ( COMMA NUMBER )? RIGHT_PAREN)?) 
  	|specialDatatype
  	|datetimeDatatype
	;

specialDatatype:
	(typeName (LEFT_PAREN NUMBER ID  RIGHT_PAREN))
	| (DOUBLE PRECISION)
	| (NATIONAL typeName (VARYING)? LEFT_PAREN NUMBER RIGHT_PAREN) 
	|(REF LEFT_PAREN? columnName  RIGHT_PAREN?)
	;

datetimeDatatype:
( DATE
| (TIMESTAMP ( LEFT_PAREN NUMBER RIGHT_PAREN )?
     ( WITH ( LOCAL )? TIME ZONE )?)
| (INTERVAL YEAR ( LEFT_PAREN NUMBER RIGHT_PAREN )? TO MONTH)
| (INTERVAL DAY ( LEFT_PAREN NUMBER RIGHT_PAREN )? TO SECOND
     ( LEFT_PAREN NUMBER RIGHT_PAREN )?)
)
;

columnSortClause:
	tableAndAlias columnName
    (ASC | DESC)?
    ;
  
 tableAndAlias:
	tableName alias?
	;
 
 privateExprOfDb:
	treatFunction
	|caseExpr
	|compoundExpr
	|intervalExpression
	|objectAccessExpression
	|constructorExpr
	;

treatFunction:
	TREAT LEFT_PAREN expr AS REF? typeName RIGHT_PAREN
	;

caseExpr:
	CASE ( simpleCaseExpr
     | searchedCaseExpr
     )
     elseClause?
     END
	;
	
simpleCaseExpr:
	expr
	searchedCaseExpr+
	;
	
searchedCaseExpr:
	WHEN expr THEN simpleExpr
	;
	
elseClause:
	ELSE expr
	;

dateTimeExpr:
	expr AT (
		LOCAL
		|(TIME ZONE ( 
				STRING
               | DBTIMEZONE
               | expr
               ))
	)
	;
	
compoundExpr:
	(PRIOR expr)
	//|(expr OR_SYM expr)
	;

intervalExpression:
	LEFT_PAREN expr MINUS expr RIGHT_PAREN 
   	( (DAY ( LEFT_PAREN NUMBER RIGHT_PAREN )? TO
     SECOND ( LEFT_PAREN NUMBER RIGHT_PAREN )?)
   	| (YEAR ( LEFT_PAREN NUMBER RIGHT_PAREN )? TO
     MONTH)
   	)
	;

objectAccessExpression:
	( 
		columnName
		| tableName
		| (LEFT_PAREN simpleExpr RIGHT_PAREN)
	)
	( 
		(attributeName (attributeName )*functionCall)
		| functionCall
	)
	;
	
constructorExpr:
	NEW typeName exprsWithParen
   ;