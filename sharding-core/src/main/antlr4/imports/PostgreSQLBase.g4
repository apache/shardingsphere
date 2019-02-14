grammar PostgreSQLBase;

import PostgreSQLKeyword, Symbol, DataType, Keyword, BaseRule, Symbol;


columnDefinition
    : columnName dataType collateClause? columnConstraint*
    ;
    
collateClause
    : COLLATE collationName
    ;
    
columnConstraint
    : constraintClause? columnConstraintOption constraintOptionalParam
    ;
    
constraintClause
    : CONSTRAINT constraintName
    ;
    
columnConstraintOption
    : NOT? NULL
    | checkOption
    | DEFAULT defaultExpr
    | GENERATED (ALWAYS | BY DEFAULT) AS IDENTITY (LP_ sequenceOptions RP_)?
    | UNIQUE indexParameters
    | primaryKey indexParameters
    | REFERENCES tableName (LP_ columnName RP_)? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?(ON DELETE action)? foreignKeyOnAction*
    ;
    
checkOption
    : CHECK expr (NO INHERIT)?
    ;
    
defaultExpr
    : CURRENT_TIMESTAMP | expr
    ;
    
sequenceOptions
    : sequenceOption+
    ;
    
sequenceOption
    : START WITH? NUMBER_
    | INCREMENT BY? NUMBER_
    | MAXVALUE NUMBER_
    | NO MAXVALUE
    | MINVALUE NUMBER_
    | NO MINVALUE
    | CYCLE
    | NO CYCLE
    | CACHE NUMBER_
    ;
    
indexParameters
    : (USING INDEX TABLESPACE tablespaceName)?
    ;
    
action
    : NO ACTION | RESTRICT | CASCADE | SET (NULL | DEFAULT)
    ;
    
foreignKeyOnAction
    : ON (UPDATE foreignKeyOn | DELETE foreignKeyOn)
    ;
    
foreignKeyOn
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;
    
constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED | IMMEDIATE))?
    ;
    
    
dataType
    : typeName intervalFields? dataTypeLength? (WITHOUT TIME ZONE | WITH TIME ZONE)? (LBT_ RBT_)* | ID
    ;
    
typeName
    : DOUBLE PRECISION | CHARACTER VARYING? | BIT VARYING? | ID
    ;
    
typeNames
    : typeName (COMMA_ typeName)*
    ;
    
intervalFields
    : intervalField (TO intervalField)?
    ;
    
intervalField
    : YEAR
    | MONTH
    | DAY
    | HOUR
    | MINUTE
    | SECOND
    ;
    
privateExprOfDb
    : aggregateExpression
    | windowFunction
    | arrayConstructorWithCast
    | (TIMESTAMP (WITH TIME ZONE)? STRING_)
    | extractFromFunction
    ;
    
pgExpr
    : castExpr | collateExpr | expr
    ;
    
aggregateExpression
    : ID (LP_ (ALL | DISTINCT)? exprs orderByClause? RP_) asteriskWithParen (LP_ exprs RP_ WITHIN GROUP LP_ orderByClause RP_) filterClause?
    ;
    
filterClause
    : FILTER LP_ WHERE booleanPrimary RP_
    ;
    
asteriskWithParen
    : LP_ ASTERISK_ RP_
    ;
    
windowFunction
    : ID (exprList | asteriskWithParen) filterClause? windowFunctionWithClause
    ;
    
windowFunctionWithClause
    : OVER (ID | LP_ windowDefinition RP_)
    ;
    
windowDefinition
    : ID? (PARTITION BY exprs)? (orderByClause (COMMA_ orderByClause)*)? frameClause?
    ;
    
orderByClause
    : ORDER BY expr (ASC | DESC | USING operator)? (NULLS (FIRST | LAST))?
    ;
    
operator
    : SAFE_EQ_
    | EQ_
    | NEQ_
    | GT_
    | GTE_
    | LT_
    | LTE_
    | AND_
    | OR_
    | NOT_
    ;
    
frameClause
    : (RANGE | ROWS) frameStart | (RANGE | ROWS) BETWEEN frameStart AND frameEnd
    ;
    
frameStart
    : UNBOUNDED PRECEDING
    | NUMBER_ PRECEDING
    | CURRENT ROW
    | NUMBER_ FOLLOWING
    | UNBOUNDED FOLLOWING
    ;
    
frameEnd
    : frameStart
    ;
    
castExpr
    : CAST LP_ expr AS dataType RP_ | expr COLON_ COLON_ dataType
    ;
    
castExprWithCOLON_
    : COLON_ COLON_ dataType(LBT_ RBT_)*
    ;
    
collateExpr
    : expr COLLATE expr
    ;
arrayConstructorWithCast
    : arrayConstructor castExprWithCOLON_? | ARRAY LBT_ RBT_ castExprWithCOLON_
    ;
    
arrayConstructor
    : ARRAY LBT_ exprs RBT_ | ARRAY LBT_ arrayConstructor (COMMA_ arrayConstructor)* RBT_
    ;
    
extractFromFunction
    : EXTRACT LP_ ID FROM ID RP_
    ;
