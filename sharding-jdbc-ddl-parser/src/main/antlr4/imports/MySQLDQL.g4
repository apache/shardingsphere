grammar MySQLDQL;
import MySQLKeyword,Keyword,Symbol,DataType, BaseRule, DQLBase;


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



idListWithEmpty:
	(LEFT_PAREN RIGHT_PAREN)
	|idList
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
    tableName (PARTITION  idList)?
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
  	| USING idList
	;
	
indexHintList:
    indexHint(COMMA  indexHint)*
    ;

indexHint:
	USE (INDEX|KEY) (FOR (JOIN|ORDER BY|GROUP BY))* idList
  	| IGNORE (INDEX|KEY) (FOR (JOIN|ORDER BY|GROUP BY))* idList
 	;
	
//https://dev.mysql.com/doc/refman/8.0/en/expressions.html
expr:
	expr OR expr
  	| expr OR_SYM  expr
  	| expr XOR expr
  	| expr AND expr
 	| expr AND_SYM expr
 	| LEFT_PAREN expr RIGHT_PAREN
  	| NOT expr
  	| NOT_SYM expr
  	| booleanPrimary
	;
	
booleanPrimary:
    booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN |NULL)
  	| booleanPrimary SAFE_EQ predicate
 	| booleanPrimary comparisonOperator predicate
  	| booleanPrimary comparisonOperator (ALL | ANY) subquery
  	| predicate
  	;

comparisonOperator:
	EQ_OR_ASSIGN | GTE | GT | LTE | LT | NEQ_SYM | NEQ
	;

predicate:
	bitExpr NOT? IN subquery
	| bitExpr NOT? IN LEFT_PAREN simpleExpr ( COMMA  simpleExpr)* RIGHT_PAREN
	| bitExpr NOT? BETWEEN simpleExpr AND predicate
	| bitExpr SOUNDS LIKE simpleExpr
	| bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
	| bitExpr NOT? REGEXP simpleExpr
	| bitExpr
	;
  
bitExpr:
	bitExpr BIT_INCLUSIVE_OR bitExpr
	| bitExpr BIT_AND bitExpr
	| bitExpr SIGNED_LEFT_SHIFT bitExpr
	| bitExpr SIGNED_RIGHT_SHIFT bitExpr
	| bitExpr PLUS bitExpr
	| bitExpr MINUS bitExpr
	| bitExpr ASTERISK bitExpr
	| bitExpr SLASH bitExpr
	| bitExpr DIV bitExpr
	| bitExpr MOD bitExpr
	| bitExpr MOD_SYM bitExpr
	| bitExpr BIT_EXCLUSIVE_OR bitExpr
	//| bitExpr '+' interval_expr
	//| bitExpr '-' interval_expr
	| simpleExpr
	;
	
simpleExpr:
	| functionCall
	| liter
	| ID
	| simpleExpr collateClause
	//| param_marker
	//| variable
	| simpleExpr AND_SYM simpleExpr
	| PLUS simpleExpr
	| MINUS simpleExpr
	| UNARY_BIT_COMPLEMENT simpleExpr
	| NOT_SYM simpleExpr
	| BINARY simpleExpr
	| LEFT_PAREN expr RIGHT_PAREN
	| ROW LEFT_PAREN simpleExpr( COMMA  simpleExpr)* RIGHT_PAREN
	| subquery
	| EXISTS subquery
	// | (identifier expr)
	//| match_expr
	//| case_expr
	// | interval_expr 
	;
 
liter:
	QUESTION
	|NUMBER
	|TRUE 
	|FALSE
	|NULL
	|LEFT_BRACE ID STRING RIGHT_BRACE //
	|HEX_DIGIT
	|ID? STRING  collateClause?
	|(DATE | TIME |TIMESTAMP)STRING
	|ID? BIT_NUM collateClause?
	; 

characterAndCollate:
	characterSet
    collateClause
	;
	
characterSet:
	((CHARACTER | CHAR) SET charsetName)
	|CHARSET EQ_OR_ASSIGN? charsetName
	;
	
collateClause:
	COLLATE ID
	;
	
charsetName:
	ID
	|BINARY
	;

characterAndCollateWithEqual:
	characterSetWithEqual
    collateClauseWithEqual
	;
		
characterSetWithEqual:
	((CHARACTER | CHAR) SET EQ_OR_ASSIGN? charsetName)
	|CHARSET EQ_OR_ASSIGN? charsetName
	;
			
collateClauseWithEqual:
	COLLATE EQ_OR_ASSIGN? ID
	;
     
selectExpr:
	bitExpr AS? alias?
	;
