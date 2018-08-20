grammar DQLBase;

import BaseRule,Keyword, Symbol,DataType;

//define delete rule template 
select: 
	withClause
	|unionSelect
	;

withClause:
	WITH RECURSIVE? cteClause (COMMA cteClause)*
	unionSelect
	;

cteClause:
	cteName idList? AS subquery
	;
	
unionSelect:
	selectExpression (UNION ALL? selectExpression)*
	;

selectExpression:
	selectClause 
	fromClause? 
	whereClause? 
	groupByClause? 
	orderByClause? 
	limitClause?
	;
	
selectClause:
	SELECT selectSpec selectExprs
	;

selectSpec: ;

fromClause: 
	FROM tableReferences
	;

whereClause: 
	WHERE expr
	;
	
groupByClause: 
	GROUP BY groupByItem (COMMA groupByItem)* 
	(WITH ROLLUP)? 
	havingClause?
	;
	
havingClause: 
	HAVING  expr
	;
	
orderByClause: 
	ORDER BY groupByItem (COMMA groupByItem)*
	;
	
groupByItem:
	(columnName | NUMBER |expr)  (ASC|DESC)?
	;
	
limitClause:
	LIMIT rangeClause
	;
	
	
partitionClause: 
	PARTITION idList
	;


functionCall:
	ID LEFT_PAREN(|expr ( COMMA  expr)*) RIGHT_PAREN
	;

selectExprs:
    (ASTERISK (COMMA selectExpr)*) 
    |selectExpr (COMMA ASTERISK)? (COMMA selectExpr)*
    ;
 
subquery:
	LEFT_PAREN unionSelect RIGHT_PAREN
	;
	   
alias: ID ;	
expr: ;
selectExpr: ;
tableReferences: ;