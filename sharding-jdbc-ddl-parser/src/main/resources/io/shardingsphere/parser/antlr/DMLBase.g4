grammar DMLBase;

import SQLBase, Keyword, Symbol;

execute:
	select
	|insert
	|update
	|delete
	;
	
insert:
	;
	
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
	cteName itemList? AS subquery
	;
	
cteName:
   ID
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
	
selectSpec:
	;
fromClause: 
	FROM tableReferences
	;
	
tableReferences:
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
	
rangeClause:
	NUMBER (COMMA  NUMBER)* 
	| NUMBER OFFSET NUMBER
	;
    
subquery:
	LEFT_PAREN unionSelect RIGHT_PAREN
	;
	
selectExprs:
    (ASTERISK (COMMA selectExpr)*) 
    |selectExpr (COMMA ASTERISK)? (COMMA selectExpr)*
    ;
     
selectExpr:
	bitExpr AS? alias?
	;

bitExpr:
	;
 
alias:
	ID
	;

//define delete rule template
delete: 
	deleteClause 
	whereClause? 
	orderByClause? 
	limitClause?
	;

deleteClause:
	;
	
partitionClause: 
	PARTITION itemList
	;

update: 
	updateClause 
	setClause 
	whereClause? 
	orderByClause? 
	limitClause?
	;

updateClause:
	;
	
updateSpec: 
	;

setClause: 
	SET assignmentList
	;
	
assignmentList: 
	assignment (COMMA assignment)*
	;
	
assignment:
	columnName EQ_OR_ASSIGN value
	;
	
value:
	DEFAULT
	|expr
	;

expr:
	;
