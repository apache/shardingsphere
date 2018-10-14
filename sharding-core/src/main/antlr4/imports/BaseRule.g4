//rule in this file does not allow override

grammar BaseRule;

import DataType,Keyword,Symbol;

ID: 
    (BACK_QUOTA?[a-zA-Z_$][a-zA-Z0-9_$]* BACK_QUOTA? DOT)?
    (BACK_QUOTA?[a-zA-Z_$][a-zA-Z0-9_$]* BACK_QUOTA?)
    |[a-zA-Z_$0-9]+ DOT ASTERISK
    ;

schemaName: ID;
tableName: ID;
columnName: ID; 
tablespaceName: ID;
collationName: STRING | ID;
indexName: ID;
alias: ID;
cteName:ID;
parserName: ID;
extensionName: ID;
rowName: ID;
opclass: ID;

fileGroup: ID;
groupName: ID;
constraintName: ID;
keyName: ID;
typeName: ID;
xmlSchemaCollection:ID;
columnSetName: ID;
directoryName: ID;
triggerName: ID;

roleName: ID;
partitionName: ID;
rewriteRuleName: ID;
ownerName: ID;

ifExists
    : IF EXISTS;

ifNotExists
    : IF NOT EXISTS;

dataTypeLength
    : LEFT_PAREN (NUMBER (COMMA NUMBER)?)? RIGHT_PAREN
    ;

nullNotnull
    : NULL
    | NOT NULL
    ;

primaryKey
	: PRIMARY? KEY
	;

matchNone
    : 'Default does not match anything'
    ;
    
idList
    : LEFT_PAREN ID (COMMA  ID)* RIGHT_PAREN
    ;

rangeClause
    : NUMBER (COMMA  NUMBER)* 
    | NUMBER OFFSET NUMBER
    ;

tableNamesWithParen
    : LEFT_PAREN tableNames RIGHT_PAREN
    ;

tableNames
    : tableName (COMMA tableName)*
    ;

columnNamesWithParen
    : LEFT_PAREN columnNames RIGHT_PAREN
    ;

columnNames
    : columnName (COMMA columnName)*
    ;
    
columnList
    : LEFT_PAREN columnNames RIGHT_PAREN
    ;

indexNames
    : indexName (COMMA indexName)*
    ;

rowNames
    : rowName (COMMA rowName)*
    ;
    
bitExprs:
    bitExpr (COMMA bitExpr)*
    ;

exprs
    : expr (COMMA expr)*
    ;
 
exprsWithParen
    : LEFT_PAREN exprs RIGHT_PAREN
    ;

//https://dev.mysql.com/doc/refman/8.0/en/expressions.html
expr
    : expr OR expr
    | expr OR_SYM  expr
    | expr XOR expr
    | expr AND expr
    | expr AND_SYM expr
   
    | LEFT_PAREN expr RIGHT_PAREN
    | NOT expr
    | NOT_SYM expr
    | booleanPrimary
    | exprRecursive
    ;

exprRecursive
    : matchNone
    ;
    
booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN |NULL)
    | booleanPrimary SAFE_EQ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;

comparisonOperator
    : EQ_OR_ASSIGN 
    | GTE 
    | GT 
    | LTE 
    | LT 
    | NEQ_SYM 
    | NEQ
    ;

predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LEFT_PAREN simpleExpr ( COMMA  simpleExpr)* RIGHT_PAREN
    | bitExpr NOT? BETWEEN simpleExpr AND predicate
    | bitExpr SOUNDS LIKE simpleExpr
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
    | bitExpr NOT? REGEXP simpleExpr
    | bitExpr
    ;
  
bitExpr
    : bitExpr BIT_INCLUSIVE_OR bitExpr
    | bitExpr BIT_AND bitExpr
    | bitExpr SIGNED_LEFT_SHIFT bitExpr
    | bitExpr SIGNED_RIGHT_SHIFT bitExpr
    | bitExpr PLUS bitExpr
    | bitExpr MINUS bitExpr
    | bitExpr ASTERISK bitExpr
    | bitExpr SLASH bitExpr
    | bitExpr MOD bitExpr
    | bitExpr MOD_SYM bitExpr
    | bitExpr BIT_EXCLUSIVE_OR bitExpr
    //| bitExpr '+' interval_expr
    //| bitExpr '-' interval_expr
    | simpleExpr
    ;
    
simpleExpr
    : functionCall
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
    |privateExprOfDb
    ;

functionCall
    : ID LEFT_PAREN( bitExprs?) RIGHT_PAREN
    ;    
 
privateExprOfDb
    : matchNone
    ;
     
liter
    : QUESTION
    | NUMBER
    | TRUE 
    | FALSE
    | NULL
    | LEFT_BRACE ID STRING RIGHT_BRACE
    | HEX_DIGIT
    | ID? STRING  collateClause?
    | (DATE | TIME |TIMESTAMP) STRING
    | ID? BIT_NUM collateClause?
    ; 

subquery
    : matchNone
    ;

collateClause
    : matchNone
    ;

orderByClause
    : ORDER BY groupByItem (COMMA groupByItem)*
    ;
    
groupByItem
    : (columnName | NUMBER |expr)  (ASC|DESC)?
    ;