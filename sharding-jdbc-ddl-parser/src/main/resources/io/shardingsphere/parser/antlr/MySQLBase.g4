grammar MySQLBase;

import MySQLKeyword,SQLBase,Keyword,Symbol;

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
	|INT
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

subquery:
	;
	
collateClause:
	COLLATE ID
	;

functionCall:
	ID LEFT_PAREN(|expr ( COMMA  expr)*) RIGHT_PAREN
	;

value:
	DEFAULT|expr;

valueList:
	 value (COMMA value)*
	;
	
valueListWithParen:
	LEFT_PAREN valueList RIGHT_PAREN
	;
	
columnList:
	LEFT_PAREN columnName (COMMA columnName)* RIGHT_PAREN
	;