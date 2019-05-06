grammar PostgreSQLBase;

import PostgreSQLKeyword, DataType, Keyword, Symbol, BaseRule;

columnDefinition
    : columnName dataType collateClause? columnConstraint*
    ;
    
dataType
    : typeName intervalFields? dataTypeLength? (WITHOUT TIME ZONE | WITH TIME ZONE)? (LBT_ RBT_)*
    | ID
    ;
    
typeName
    : DOUBLE PRECISION | CHARACTER VARYING? | BIT VARYING? | ID
    ;
    
typeNames
    : typeName (COMMA typeName)*
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
    
collateClause
    : COLLATE collationName
    ;
    
usingIndexType:
    USING (BTREE | HASH | GIST | SPGIST | GIN | BRIN)
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
    | REFERENCES tableName (LP_ columnName RP_)?
     (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?(ON DELETE action)? foreignKeyOnAction*
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
    : START WITH? NUMBER
    | INCREMENT BY? NUMBER
    | MAXVALUE NUMBER
    | NO MAXVALUE
    | MINVALUE NUMBER
    | NO MINVALUE
    | CYCLE
    | NO CYCLE
    | CACHE NUMBER
    ;
    
indexParameters
    : (USING INDEX TABLESPACE tablespaceName)?
    ;
    
action
    : NO ACTION | RESTRICT | CASCADE | SET (NULL | DEFAULT)
    ;

constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED | IMMEDIATE))?
    ;
    
tableConstraint
    : constraintClause? tableConstraintOption constraintOptionalParam
    ;
    
tableConstraintOption
    : checkOption
    | UNIQUE columnList indexParameters
    | primaryKey columnList indexParameters
    | FOREIGN KEY columnList REFERENCES tableName columnList (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? foreignKeyOnAction*
    ;
    
foreignKeyOnAction
    : ON (UPDATE foreignKeyOn | DELETE foreignKeyOn)
    ;
    
foreignKeyOn
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;
    
excludeElement
    : (columnName | expr) opclass? (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;
    
privateExprOfDb
    : aggregateExpression
    | windowFunction
    | arrayConstructorWithCast
    | (TIMESTAMP (WITH TIME ZONE)? STRING)
    | extractFromFunction
    ;
    
pgExpr
    : castExpr | collateExpr | expr
    ;
    
aggregateExpression
    : ID (LP_ (ALL | DISTINCT)? exprs orderByClause? RP_) asteriskWithParen
    (LP_ exprs RP_ WITHIN GROUP LP_ orderByClause RP_) filterClause?
    ;
    
filterClause
    : FILTER LP_ WHERE booleanPrimary RP_
    ;
    
asteriskWithParen
    : LP_ ASTERISK RP_
    ;
    
windowFunction
    : ID (exprsWithParen | asteriskWithParen) 
    filterClause? windowFunctionWithClause
    ;
    
windowFunctionWithClause
    : OVER (ID | LP_ windowDefinition RP_ )
    ;
    
windowDefinition
    : ID? (PARTITION BY exprs)?
    (orderByExpr (COMMA orderByExpr)*)?
    frameClause?
    ;
    
orderByExpr
    : ORDER BY expr (ASC | DESC | USING operator)? (NULLS (FIRST | LAST ))?
    ;
    
operator
    : SAFE_EQ
    | EQ_
    | NEQ
    | NEQ_
    | GT
    | GTE
    | LT
    | LTE
    | AND_
    | OR_
    | NOT_
    ;
    
frameClause
    : (RANGE | ROWS) frameStart
    | (RANGE | ROWS ) BETWEEN frameStart AND frameEnd
    ;
    
frameStart
    : UNBOUNDED PRECEDING
    | NUMBER PRECEDING
    | CURRENT ROW
    | NUMBER FOLLOWING
    | UNBOUNDED FOLLOWING
    ;
    
frameEnd
    : frameStart
    ;
    
castExpr
    : CAST LP_ expr AS dataType RP_
    | expr COLON COLON dataType
    ;
    
castExprWithColon
    : COLON COLON dataType(LBT_ RBT_)*
    ;
    
collateExpr
    : expr COLLATE expr
    ;
arrayConstructorWithCast
    : arrayConstructor castExprWithColon?
    | ARRAY LBT_ RBT_ castExprWithColon
    ;
    
arrayConstructor
    : ARRAY LBT_ exprs RBT_
    | ARRAY LBT_ arrayConstructor (COMMA arrayConstructor)* RBT_
    ;
    
extractFromFunction
    : EXTRACT LP_ ID FROM ID RP_
    ;
