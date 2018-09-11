grammar PostgreBase;

import PostgreKeyword, DataType, Keyword, Symbol, BaseRule;

dataType:
	basicDataType (LEFT_BRACKET RIGHT_BRACKET)*
	;

basicDataType:
	BIGINT
	|INT8
	|BIGSERIAL
	|SERIAL8
	|BIT VARYING? numericPrecision?
	|VARBIT numericPrecision?
	|BOOLEAN
	|BOOL
//	|BOX
	|BYTEA
	|((CHARACTER VARYING?) | CHAR | VARCHAR) numericPrecision?
	|CIDR
	|CIRCLE
	|DATE
	|DOUBLE PRECISION
	|FLOAT8
	|(INTEGER | INT4 | INT)
	|intervalType
	|JSON
	|JSONB
	|LINE
	|LSEG
	|MACADDR
	|MACADDR8
	|MONEY
	|NUMERIC numericPrecision
	|DECIMAL numericPrecision?
	|PATH
	|PG_LSN
	|POINT
	|POLYGON
	|REAL
	|FLOAT4
	|SMALLINT
	|INT2
	|SERIAL
	|SERIAL4
	|FLOAT numericPrecision?
	|TEXT
	|(TIME | TIMESTAMP) numericPrecision?((WITHOUT TIME ZONE)? | (WITH TIME ZONE))
	|TSQUERY
	|TSVECTOR
	|TXID_SNAPSHOT
	|UUID
	|XML
	;

numericPrecision:
    LEFT_PAREN NUMBER (COMMA NUMBER)? RIGHT_PAREN
    ;

intervalType:
	INTERVAL intervalFields? numericPrecision?
	;

intervalFields:
	intervalField (TO intervalField)?
	;

intervalField:
	YEAR
	|MONTH
	|DAY
	|HOUR
	|MINUTE
	|SECOND
	;

collateClause:
    COLLATE collationName
    ;

withStorageParameters:
	WITH storageParametersWithParen
	;

storageParametersWithParen:
    LEFT_PAREN storageParameters RIGHT_PAREN
    ;

storageParameters:
	storageParameterWithValue (COMMA storageParameterWithValue)*
	;

storageParameterWithValue:
	storageParameter EQ_OR_ASSIGN simpleExpr
	;

tableSpaceClause:
	TABLESPACE tablespaceName
	;

usingIndexType:
    USING (BTREE | HASH | GIST | SPGIST | GIN | BRIN)
	;

columnConstraint:
	constraintClause?
	columnConstraintOption
	constraintOptionalParam
	;

constraintClause:
	CONSTRAINT constraintName
	;

columnConstraintOption:
	(NOT NULL)
	|NULL
  	|checkOption
  	|(DEFAULT defaultExpr)
    |(GENERATED ( ALWAYS | BY DEFAULT ) AS IDENTITY ( LEFT_PAREN sequenceOptions RIGHT_PAREN )?)
    |(UNIQUE indexParameters)
    |(PRIMARY KEY indexParameters)
    |(REFERENCES tableName (LEFT_PAREN columnName RIGHT_PAREN)? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?(ON DELETE action)? (ON UPDATE action)?)
	;

checkOption:
	CHECK expr (NO INHERIT )?
	;

defaultExpr:
	CURRENT_TIMESTAMP
	|expr;

sequenceOptions:
	sequenceOption
	(START WITH? NUMBER)?
	(CACHE NUMBER)?
	(NO? CYCLE)?
	;

sequenceOption:
	(INCREMENT BY? NUMBER)?
	(MINVALUE NUMBER | NO MINVALUE)?
	(MAXVALUE NUMBER | NO MAXVALUE)?
	;

indexParameters:
	withStorageParameters?
	(USING INDEX TABLESPACE tablespaceName)?
	;

action:
	(NO ACTION)
	|RESTRICT
	|CASCADE
	|(SET NULL)
	|(SET DEFAULT)
	;

constraintOptionalParam:
 	(NOT? DEFERRABLE)? (INITIALLY (DEFERRED |IMMEDIATE))?
 	;

tableConstraint:
	constraintClause?
	tableConstraintOption
	constraintOptionalParam
	;

tableConstraintOption:
	checkOption
	|(UNIQUE columnList indexParameters)
	|(PRIMARY KEY columnList indexParameters)
	|(EXCLUDE (usingIndexType)? LEFT_PAREN excludeParam (COMMA excludeParam)* RIGHT_PAREN  indexParameters (WHERE ( predicate ))?)
	|(FOREIGN KEY columnList REFERENCES tableName columnList (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON DELETE action )? (ON UPDATE action)?)
	;

excludeParam:
	excludeElement WITH operator
	;

excludeElement:
	(columnName | expr) opclass? (ASC | DESC)? (NULLS (FIRST | LAST))?
	;

operator:
	SAFE_EQ
	|EQ_OR_ASSIGN
	|NEQ
	|NEQ_SYM
	|GT
	|GTE
	|LT
	|LTE
	|AND_SYM
	|OR_SYM
	|NOT_SYM
	;

forValuesParition:
	FOR VALUES partitionBoundSpec
	;

partitionBoundSpec:
	(IN inValueOption)
	|(FROM fromValueOption TO fromValueOption)
	;

inValueOption:
	LEFT_PAREN inValue (COMMA inValue)* RIGHT_PAREN
	;

inValue:
	NUMBER
	|STRING
	|TRUE
	|FALSE
	|NULL
	;

fromValueOption:
	LEFT_PAREN fromValue (COMMA fromValue)* RIGHT_PAREN
	;

fromValue:
	inValue
	|MINVALUE
	|MAXVALUE
	;
