grammar PostgreBase;

import PostgreKeyword, DataType, Keyword, Symbol, BaseRule;

columnDefinition
	: columnName dataType collateClause? columnConstraint*
	;
	
dataType
    : typeName intervalFields? dataTypeLength? (WITHOUT TIME ZONE | WITH TIME ZONE)? (LEFT_BRACKET RIGHT_BRACKET)*
    | ID
    ;

typeName
	: DOUBLE PRECISION
    | CHARACTER VARYING?
	| BIT VARYING?
	| ID
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
    : constraintClause?
    columnConstraintOption
    constraintOptionalParam
    ;

constraintClause
    : CONSTRAINT constraintName
    ;

columnConstraintOption
    : NOT? NULL
    | checkOption
    | DEFAULT defaultExpr
    | GENERATED (ALWAYS | BY DEFAULT) AS IDENTITY (LEFT_PAREN sequenceOptions RIGHT_PAREN)?
    | UNIQUE indexParameters
    | primaryKey indexParameters
    | REFERENCES tableName (LEFT_PAREN columnName RIGHT_PAREN)?
     (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?(ON DELETE action)? foreignKeyOnAction*
    ;

checkOption
    : CHECK expr (NO INHERIT )?
    ;

defaultExpr
    : CURRENT_TIMESTAMP
    | expr;

sequenceOptions:
    sequenceOption+
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
    : NO ACTION
    | RESTRICT
    | CASCADE
    | SET NULL
    | SET DEFAULT
    ;

constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED |IMMEDIATE))?
    ;

tableConstraint
    : constraintClause?
    tableConstraintOption
    constraintOptionalParam
    ;

tableConstraintOption
    : checkOption
    | UNIQUE columnList indexParameters
    | primaryKey columnList indexParameters
    | FOREIGN KEY columnList REFERENCES tableName columnList (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? foreignKeyOnAction*
    ;

foreignKeyOnAction
    : ON UPDATE foreignKeyOn
    | ON DELETE foreignKeyOn
    ;

foreignKeyOn
    : RESTRICT
    | CASCADE
    | SET NULL
    | NO ACTION
    | SET DEFAULT
    ;

excludeElement
    : (columnName | expr) opclass? (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;

privateExprOfDb:
     aggregateExpression
     |windowFunction
     |arrayConstructorWithCast
     |(TIMESTAMP (WITH TIME ZONE)? STRING)
     |extractFromFunction
     ;
 
 pgExpr
     : castExpr
     | collateExpr
     | expr
     ;
     
 aggregateExpression
     : ID (LEFT_PAREN (ALL | DISTINCT)? exprs  orderByClause? RIGHT_PAREN)
     asteriskWithParen
     (LEFT_PAREN exprs RIGHT_PAREN  WITHIN GROUP LEFT_PAREN orderByClause RIGHT_PAREN)
     filterClause?
     ;
     
 filterClause
     : FILTER LEFT_PAREN WHERE booleanPrimary RIGHT_PAREN
     ;
     
 asteriskWithParen
     : LEFT_PAREN ASTERISK RIGHT_PAREN
     ;
 
 windowFunction
     : ID (exprsWithParen | asteriskWithParen) 
     filterClause? windowFunctionWithClause
     ; 
 
 windowFunctionWithClause
     : OVER (ID | LEFT_PAREN windowDefinition RIGHT_PAREN )
     ;    
 
 windowDefinition
     : ID? (PARTITION BY exprs)?
     (orderByExpr (COMMA orderByExpr)*)?
     frameClause?
     ;
     
orderByExpr
     : ORDER BY expr (ASC | DESC | USING operator)?  (NULLS (FIRST | LAST ))?
     ;
     
operator
    : SAFE_EQ
    | EQ_OR_ASSIGN
    | NEQ
    | NEQ_SYM
    | GT
    | GTE
    | LT
    | LTE
    | AND_SYM
    | OR_SYM
    | NOT_SYM
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
    : CAST LEFT_PAREN expr AS dataType RIGHT_PAREN
    | expr COLON COLON dataType
    ;

castExprWithColon
    : COLON COLON dataType(LEFT_BRACKET RIGHT_BRACKET)*
    ;
    
collateExpr
    : expr COLLATE expr
    ;

arrayConstructorWithCast
    : arrayConstructor castExprWithColon?
    | ARRAY LEFT_BRACKET RIGHT_BRACKET castExprWithColon  
    ;
    
arrayConstructor
    : ARRAY LEFT_BRACKET exprs RIGHT_BRACKET
    | ARRAY LEFT_BRACKET arrayConstructor (COMMA arrayConstructor)* RIGHT_BRACKET
    ;

extractFromFunction
    : EXTRACT LEFT_PAREN ID FROM ID RIGHT_PAREN
    ;