grammar PostgreDDL;
import PostgreKeyword, DataType, Keyword, BaseRule,DDLBase,MySQLDQL,DQLBase,Symbol;

createTable:
	createBasicTable
	|createTypeTable
	|createTableForPartition
	;

createBasicTable:
	createTableHeader
	createDefinitions 
	inheritClause?
	partitionClause?
	tableWithClause?
	commitClause?
	tableSpaceClause?
	;

createTableHeader:
	CREATE ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)? TABLE (IF NOT EXISTS)? tableName
	;
	
createDefinitions:
   LEFT_PAREN createDefinition (COMMA createDefinition)* RIGHT_PAREN
   ;
   
createDefinition:
 	(columnName dataType collateClause? columnConstraint*)
    | tableConstraint
    | LIKE tableName likeOption*
 	;
 		
inheritClause:
	INHERITS LEFT_PAREN tableName (COMMA tableName)* RIGHT_PAREN 
	;

partitionClause:
	PARTITION BY (RANGE | LIST) LEFT_PAREN (columnName | exprWithParen) collateClause? (opclass (COMMA opclass)*)? RIGHT_PAREN 
	;
	
tableWithClause:
	withStorageParameters
	|(WITH OIDS) 
	|(WITHOUT OIDS)
	;

commitClause:
	ON COMMIT (PRESERVE ROWS | DELETE ROWS | DROP) 
	;

tableSpaceClause:
	TABLESPACE tablespaceName
	;
	
createTypeTable:	
	createTableHeader 
	typeNameClause
	createDefinition1s
	partitionClause?
	tableWithClause?
	commitClause?
	tableSpaceClause?
	;

typeNameClause:
	OF typeName 
	;
	
createDefinition1s:
	LEFT_PAREN createDefinition1 (COMMA createDefinition1)* RIGHT_PAREN
	;

createDefinition1:
	columnName (WITH OPTIONS )? columnConstraint*
    | tableConstraint 
    ;	

createTableForPartition:
	createTableHeader
    partitionOfParent
    createDefinition1s
    partitionClause?
	tableWithClause?
	commitClause?
	tableSpaceClause?
	;
	
partitionOfParent:
	PARTITION OF tableName
	;
	
valuesClause:
	FOR VALUES partitionBoundSpec
	;

partitionBoundSpec:
	(IN inValueOption)
	|FROM fromValueOption TO fromValueOption
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

fromValue:
	inValue
	|MINVALUE
	|MAXVALUE
	;
	
fromValueOption:
	LEFT_PAREN fromValue (COMMA fromValue)* RIGHT_PAREN 
	;
	
createTableOptions:
	NONE;

dataType:
	BIGINT
	|INT8
	|BIGSERIAL	
	|SERIAL8
	|BIT VARYING? numericPrecision?
	|VARBIT numericPrecision?
	|BOOLEAN
	|BOOL
	|BOX
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
	|(TIME | TIMESTAMP) numericPrecision?((WITHOUT TIME ZONE)? | (WITHOUT TIME ZONE))
	|TSQUERY
	|TSVECTOR
	|TXID_SNAPSHOT
	|UUID
	|XML
	;

/** loose match 
 *  ID = Y M W D H M S
* */	
intervalType:	
	INTERVAL ID? numericPrecision?
	;	
		
numericPrecision:
	LEFT_PAREN NUMBER (COMMA NUMBER)? RIGHT_PAREN
	;

		
defaultExpr: expr;

exprWithParen:
	LEFT_PAREN expr RIGHT_PAREN
	;
	
collateClause:
	COLLATE collationName
	;
	
columnConstraint:
	constraintClause?
	columnConstraintOption
	(DEFERRABLE | NOT DEFERRABLE)?
	(INITIALLY DEFERRED | INITIALLY IMMEDIATE)?
	;
	
columnConstraintOption:
	(NOT NULL)
	|NULL 
  	|checkOption
  	|(DEFAULT defaultExpr)
    |(GENERATED ( ALWAYS | BY DEFAULT ) AS IDENTITY ( LEFT_PAREN sequenceOptions RIGHT_PAREN )?) 
    |(UNIQUE indexParameters) 
    |(PRIMARY KEY indexParameters)
    |(REFERENCES tableName (LEFT_PAREN columnName RIGHT_PAREN)? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?(ON DELETE action )? (ON UPDATE action )?) 
	;
	
checkOption:
	CHECK exprWithParen (NO INHERIT )?
	;

action:
	(NO ACTION)
	|RESTRICT
	|CASCADE
	|(SET NULL)
	|(SET DEFAULT)
	;
	
sequenceOptions:
	(INCREMENT BY? NUMBER)?
	(MINVALUE NUMBER | NO MINVALUE)?
	(MAXVALUE NUMBER | NO MAXVALUE)?
	(START WITH? NUMBER)?
	(CACHE NUMBER)? 
	(NO? CYCLE)?
	;
	
likeOption:
	(INCLUDING | EXCLUDING ) 
	(COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
	;
	
tableConstraint:
	constraintClause?
	tableConstraintOption
	(DEFERRABLE | NOT DEFERRABLE )? 
	(INITIALLY DEFERRED | INITIALLY IMMEDIATE )?
	;

tableConstraintOption:
	checkOption
	|(UNIQUE columnList indexParameters) 
	|(PRIMARY KEY columnList indexParameters)
	|(EXCLUDE (USING extensionName)? LEFT_PAREN excludeParam (COMMA excludeParam)* RIGHT_PAREN  indexParameters (WHERE ( predicate ))?) 
	|(FOREIGN KEY columnList REFERENCES tableName columnList (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON DELETE action )? (ON UPDATE action)?) 
	;
	
indexParameters:
	withStorageParameters?
	(USING INDEX TABLESPACE tablespaceName)?
	;

extensionName:
	ID
	;
	
excludeParam:
	excludeElement WITH operator
	;

excludeElement:	
	(columnName | exprWithParen) opclass? (ASC | DESC)? (NULLS (FIRST | LAST))?
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
	;
		
typeName:ID;
constraintName:ID;
constraintClause:
	CONSTRAINT constraintName
	;

withStorageParameters:
	WITH LEFT_PAREN storageParameters LEFT_PAREN 
	;
	
storageParameters:
	storageParameterWithValue (COMMA storageParameterWithValue)*
	;
	
storageParameterWithValue:
	storageParameter (EQ_OR_ASSIGN NUMBER)?
	;
	
storageParameter:
	ID
	;	
 	
opclass:
	ID
	; 	
