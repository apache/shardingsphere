grammar MySQLDML;
import MySQLKeyword, DataType, Keyword, BaseRule, MySQLDQL, DQLBase, DMLBase,Symbol;

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

columnClause: 
	idListWithEmpty? (valueClause | select)
	;
	
valueClause: 
	(VALUES | VALUE) valueListWithParen (COMMA valueListWithParen)*
	;
		
setClause: 
	SET assignmentList
	;
	
onDuplicateClause: 
	ON DUPLICATE KEY UPDATE assignmentList
	;

itemListWithEmpty:
	(LEFT_PAREN RIGHT_PAREN)
	|idList
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
