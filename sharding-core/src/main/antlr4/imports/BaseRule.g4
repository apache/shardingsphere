//rule in this file does not allow override

grammar BaseRule;

import DataType,Keyword,Symbol;

ID: 
    (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_? DOT)?
    (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_?)
    | [a-zA-Z_$0-9]+ DOT ASTERISK
    ;

schemaName
    : ID
    ;

tableName
    : ID
    ;

columnName
    : ID
    ;

tablespaceName
    : ID
    ;

collationName
    : STRING
    | ID
    ;

indexName
    : ID
    ;

alias
    : ID
    ;

cteName
    : ID
    ;

parserName
    : ID
    ;

extensionName
    : ID
    ;

rowName
    : ID
    ;

opclass
    : ID
    ;


fileGroup
    : ID
    ;

groupName
    : ID
    ;

constraintName
    : ID
    ;

keyName
    : ID
    ;

typeName
    : ID
    ;

xmlSchemaCollection
    : ID
    ;

columnSetName
    : ID
    ;

directoryName
    : ID
    ;

triggerName
    : ID
    ;

routineName
    : ID
    ;


roleName
    : ID
    ;

partitionName
    : ID
    ;

rewriteRuleName
    : ID
    ;

ownerName
    : ID
    ;

userName
    : ID
    ;


ifExists
    : IF EXISTS;

ifNotExists
    : IF NOT EXISTS;

dataTypeLength
    : LP_ (NUMBER (COMMA NUMBER)?)? RP_
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

ids
    : ID (COMMA  ID)*
    ;

idList
    : LP_ ids RP_
    ;

rangeClause
    : NUMBER (COMMA  NUMBER)*
    | NUMBER OFFSET NUMBER
    ;

tableNamesWithParen
    : LP_ tableNames RP_
    ;

tableNames
    : tableName (COMMA tableName)*
    ;

columnNamesWithParen
    : LP_ columnNames RP_
    ;

columnNames
    : columnName (COMMA columnName)*
    ;

columnList
    : LP_ columnNames RP_
    ;

indexNames
    : indexName (COMMA indexName)*
    ;

rowNames
    : rowName (COMMA rowName)*
    ;

roleNames
    : roleName (COMMA roleName)*
    ;

userNames
    : userName (COMMA userName)*
    ;

bitExprs:
    bitExpr (COMMA bitExpr)*
    ;

exprs
    : expr (COMMA expr)*
    ;

exprsWithParen
    : LP_ exprs RP_
    ;

//https://dev.mysql.com/doc/refman/8.0/en/expressions.html
expr
    : expr OR expr
    | expr OR_ expr
    | expr XOR expr
    | expr AND expr
    | expr AND_ expr

    | LP_ expr RP_
    | NOT expr
    | NOT_ expr
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
    : EQ_
    | GTE
    | GT
    | LTE
    | LT
    | NEQ_
    | NEQ
    ;

predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ simpleExpr ( COMMA  simpleExpr)* RP_
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
    | bitExpr MOD_ bitExpr
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

    | simpleExpr AND_ simpleExpr
    | PLUS simpleExpr
    | MINUS simpleExpr
    | UNARY_BIT_COMPLEMENT simpleExpr
    | NOT_ simpleExpr
    | BINARY simpleExpr
    | LP_ expr RP_
    | ROW LP_ simpleExpr( COMMA  simpleExpr)* RP_
    | subquery
    | EXISTS subquery

    // | (identifier expr)
    //| match_expr
    //| case_expr
    // | interval_expr
    |privateExprOfDb
    ;

functionCall
    : ID LP_( bitExprs?) RP_
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
    | LBE_ ID STRING RBE_
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