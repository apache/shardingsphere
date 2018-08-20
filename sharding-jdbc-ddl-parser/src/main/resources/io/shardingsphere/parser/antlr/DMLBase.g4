grammar DMLBase;

import DQLBase,BaseRule,DataType,Keyword, Symbol;

execute:
	select
	|insert
	|update
	|delete
	;
	
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

//define delete rule template
delete: 
	deleteClause 
	whereClause? 
	orderByClause? 
	limitClause?
	;
	
partitionClause: 
	PARTITION idList
	;

update: 
	updateClause 
	setClause 
	whereClause? 
	orderByClause? 
	limitClause?
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
	DEFAULT|expr;

valueList:
	 value (COMMA value)*
	;
	
valueListWithParen:
	LEFT_PAREN valueList RIGHT_PAREN
	;	
	
insert:
	;

deleteClause:
	;
	
updateClause:
	;

updateSpec: 
	;
