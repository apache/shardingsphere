grammar MySQLDML;
import MySQLBase,MySQLKeyword,DMLBase,SQLBase,Keyword,Symbol;


selectSpec: 
	(ALL | DISTINCT | DISTINCTROW)? 
	HIGH_PRIORITY? 
	STRAIGHT_JOIN?
	SQL_SMALL_RESULT?
	SQL_BIG_RESULT?
	SQL_BUFFER_RESULT?
	(SQL_CACHE | SQL_NO_CACHE)?
	SQL_CALC_FOUND_ROWS?
	;


caseExpress:
	caseCond
	|caseComp
	;
	
caseComp:
	CASE simpleExpr caseWhenComp+ elseResult? END  
	;
	
caseWhenComp:
	WHEN simpleExpr THEN caseResult
	;

caseCond:
	CASE whenResult+ elseResult? END
	;
	
whenResult:
	WHEN booleanPrimary THEN caseResult
	;

elseResult:
	ELSE caseResult
	;

caseResult:
	expr
	;

selectExpr:
	(bitExpr| caseExpress) AS? alias?
	;
	
	
//https://dev.mysql.com/doc/refman/8.0/en/join.html
tableReferences:
    tableReference(COMMA  tableReference)*
    ;

tableReference:
	(tableFactor joinTable)+
  	| tableFactor joinTable+
  	| tableFactor
 	;
 	
tableFactor:
    tableName (PARTITION  itemList)?
        (AS? alias)? indexHintList? 
  	| subquery AS? alias
  	| LEFT_PAREN tableReferences RIGHT_PAREN
	;
	

joinTable:
	(INNER | CROSS)? JOIN tableFactor joinCondition?
  	| STRAIGHT_JOIN tableFactor
  	| STRAIGHT_JOIN tableFactor joinCondition
  	| (LEFT|RIGHT) OUTER? JOIN tableFactor joinCondition
  	| NATURAL (INNER | (LEFT|RIGHT) (OUTER))? JOIN tableFactor
	;
	
joinCondition:
    ON expr
  	| USING itemList
	;
    
indexHintList:
    indexHint(COMMA  indexHint)*
    ;

indexHint:
	USE (INDEX|KEY) (FOR (JOIN|ORDER BY|GROUP BY))* itemList
  	| IGNORE (INDEX|KEY) (FOR (JOIN|ORDER BY|GROUP BY))* itemList
 	;

//delete 
deleteClause:
	DELETE deleteSpec (fromMulti| fromSingle) 
	;
	
fromSingle: 
	FROM ID partitionClause?
	; 
	 
fromMulti:
	(ID ('.*')? (COMMA ID ('.*')?)* FROM  tableReferences)
	|FROM (ID ('.*')? (COMMA ID ('.*')?)* USING tableReferences)
	;

deleteSpec: 
	LOW_PRIORITY?
	|QUICK?
	|IGNORE?
	;
	
// define insert rule
insert:
	insertClause INTO? ID partitionClause? 
	(setClause | columnClause) onDuplicateClause?
	;
	
insertClause:
	INSERT insertSpec?
	;
	
insertSpec:
	LOW_PRIORITY
	| DELAYED 
	| HIGH_PRIORITY IGNORE
	;
	
partitionClause: 
	PARTITION itemList
	;
	
columnClause: 
	itemListWithEmpty? (valueClause | select)
	;
	
valueClause: 
	(VALUES | VALUE) valueList (COMMA valueList)*
	;
	
setClause: 
	SET assignmentList
	;
	
onDuplicateClause: 
	ON DUPLICATE KEY UPDATE assignmentList
	;

itemListWithEmpty:
	(LEFT_PAREN RIGHT_PAREN)
	|itemList
	;

assignmentList: 
	assignment (COMMA assignment)*
	;
	
assignment:
	columnName EQ_OR_ASSIGN value;
	
//override update rule
updateClause: 
	UPDATE updateSpec tableReferences
	;
	
updateSpec: 
	LOW_PRIORITY? IGNORE?
	;

item: ID; 









     
