grammar BaseRule;

import Keyword, DataType, Symbol;

ID 
    : (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_? DOT_)? (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_?)
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

collationName
    : STRING_ | ID
    ;

indexName
    : ID
    ;

alias
    : ID
    ;

dataTypeLength
    : LP_ (NUMBER_ (COMMA_ NUMBER_)?)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;

rangeClause
    : rangeItem (COMMA_ rangeItem)* | rangeItem OFFSET rangeItem
    ;

rangeItem
    : number | question
    ;

columnNames
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

exprs
    : expr (COMMA_ expr)*
    ;

exprList
    : LP_ exprs RP_
    ;

expr
    : expr AND expr
    | expr AND_ expr
    | expr XOR expr
    | LP_ expr RP_
    | NOT expr
    | NOT_ expr
    | expr OR expr
    | expr OR_ expr
    | booleanPrimary
    | exprRecursive
    ;

exprRecursive
    : matchNone
    ;

booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN |NULL)
    | booleanPrimary SAFE_EQ_ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;

comparisonOperator
    : EQ_
    | GTE_
    | GT_
    | LTE_
    | LT_
    | NEQ_
    ;

predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ simpleExpr (COMMA_ simpleExpr)* RP_
    | bitExpr NOT? BETWEEN simpleExpr AND predicate
    | bitExpr SOUNDS LIKE simpleExpr
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
    | bitExpr NOT? REGEXP simpleExpr
    | bitExpr
    ;

bitExpr
    : bitExpr VERTICAL_BAR_ bitExpr
    | bitExpr AMPERSAND_ bitExpr
    | bitExpr SIGNED_LEFT_SHIFT_ bitExpr
    | bitExpr SIGNED_RIGHT_SHIFT_ bitExpr
    | bitExpr PLUS_ bitExpr
    | bitExpr MINUS_ bitExpr
    | bitExpr ASTERISK_ bitExpr
    | bitExpr SLASH_ bitExpr
    | bitExpr MOD bitExpr
    | bitExpr MOD_ bitExpr
    | bitExpr CARET_ bitExpr
    | bitExpr PLUS_ intervalExpr
    | bitExpr MINUS_ intervalExpr
    | simpleExpr
    ;

simpleExpr
    : functionCall
    | literal
    | columnName
    | simpleExpr collateClause
    //| param_marker
    | variable
    | simpleExpr AND_ simpleExpr
    | PLUS_ simpleExpr
    | MINUS_ simpleExpr
    | TILDE_ simpleExpr
    | NOT_ simpleExpr
    | BINARY simpleExpr
    | exprList
    | ROW exprList
    | subquery
    | EXISTS subquery
    // | (identifier expr)
    //| match_expr
    | caseExpress
    | intervalExpr
    | privateExprOfDb
    ;

functionCall
    : ID LP_ distinct? (exprs | ASTERISK_)? RP_
    ;

distinct
    : DISTINCT
    ;

intervalExpr
    : matchNone
    ;

caseExpress
    : matchNone
    ;

privateExprOfDb
    : matchNone
    ;

variable
    : matchNone
    ;

literal
    : question
    | number
    | TRUE
    | FALSE
    | NULL
    | LBE_ ID STRING_ RBE_
    | HEX_DIGIT_
    | string
    | ID STRING_ collateClause?
    | (DATE | TIME | TIMESTAMP) STRING_
    | ID? BIT_NUM_ collateClause?
    ;

question
    : QUESTION_
    ;

number
   : NUMBER_
   ;

string
    : STRING_
    ;

subquery
    : matchNone
    ;

collateClause
    : matchNone
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | number | expr) (ASC | DESC)?
    ;

asterisk
    : ASTERISK_
    ;

selectExprs
    : (asterisk | selectExpr) (COMMA_ selectExpr)*
    ; 

selectExpr
    : (columnName | expr) AS? alias? | columnName DOT_ASTERISK_
    ;

ignoredIdentifier_
    : ID
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;

matchNone
    : 'Default does not match anything'
    ;
