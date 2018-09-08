grammar PostgreDDL;
import PostgreKeyword, DataType, Keyword, BaseRule,DDLBase,MySQLDQL,DQLBase,Symbol;

createIndex:
	CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName indexType?
	keyParts
	withStorageParameters?
    tableSpaceClause?
    whereClause?
    ;

alterIndex:
    (alterIndexName(renameIndex | setTableSpace | setStorageParameter | resetStorageParameter))
    | alterIndexDependsOnExtension
    | alterIndexSetTableSpace
    ;

dropIndex:
    DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexName (COMMA indexName)* (CASCADE | RESTRICT)
    ;

indexType:
    USING (BTREE | HASH | GIST | SPGIST | GIN | BRIN)
	;

keyParts:
    LEFT_PAREN keyPart (COMMA keyPart)* RIGHT_PAREN
    ;

keyPart:
    (columnName | simpleExpr | LEFT_PAREN simpleExpr RIGHT_PAREN) collateClause? opclass? (ASC | DESC)? (NULLS (FIRST | LAST))?
	;

simpleExpr:
    | functionCall
    | liter
    | ID
    ;

alterIndexName:
    ALTER INDEX (IF EXISTS)? indexName
    ;

renameIndex:
    RENAME TO indexName
    ;

setTableSpace:
    SET TABLESPACE tablespaceName
    ;

setStorageParameter:
    SET LEFT_PAREN storageParameters RIGHT_PAREN
    ;

resetStorageParameter:
    RESET LEFT_PAREN storageParameters RIGHT_PAREN
    ;

alterIndexDependsOnExtension:
    ALTER INDEX indexName DEPENDS ON EXTENSION extensionName
    ;

alterIndexSetTableSpace:
    ALTER INDEX ALL IN TABLESPACE indexName (OWNED BY rowNames)?
    SET TABLESPACE tablespaceName (NOWAIT)?
    ;

rowNames:
    rowName (COMMA rowName)*
    ;

rowName:
    ID
    ;

whereClause:
    WHERE expr
    ;

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
   LEFT_PAREN (createDefinition (COMMA createDefinition)*)? RIGHT_PAREN
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
	PARTITION BY (RANGE | LIST) LEFT_PAREN partitionClauseParam (COMMA partitionClauseParam)* RIGHT_PAREN
	;

partitionClauseParam:
	(columnName | LEFT_PAREN simpleExpr RIGHT_PAREN) collateClause? opclass?
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
	createDefinition1s?
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
	(columnName (WITH OPTIONS )? columnConstraint*)
    | tableConstraint
    ;

createTableForPartition:
	createTableHeader
    partitionOfParent
    createDefinition1s?
    forValuesParition
    partitionClause?
	tableWithClause?
	commitClause?
	tableSpaceClause?
	;

partitionOfParent:
	PARTITION OF tableName
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

fromValue:
	inValue
	|MINVALUE
	|MAXVALUE
	;

fromValueOption:
	LEFT_PAREN fromValue (COMMA fromValue)* RIGHT_PAREN
	;

createTableOptions:
	;

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
numericPrecision:
	LEFT_PAREN NUMBER (COMMA NUMBER)? RIGHT_PAREN
	;

defaultExpr:
	CURRENT_TIMESTAMP
	|expr;

exprsWithParen:
	LEFT_PAREN exprs RIGHT_PAREN
	;

exprWithParen:
	LEFT_PAREN expr RIGHT_PAREN
	;

collateClause:
	COLLATE collationName
	;

columnConstraint:
	constraintClause?
	columnConstraintOption
	constraintOptionalParam
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

likeOption:
	(INCLUDING | EXCLUDING )
	(COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
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
	|AND_SYM
	|OR_SYM
	|NOT_SYM
	;

typeName:ID;
constraintName:ID;
constraintClause:
	CONSTRAINT constraintName
	;

withStorageParameters:
	WITH LEFT_PAREN storageParameters RIGHT_PAREN
	;

storageParameters:
	storageParameterWithValue (COMMA storageParameterWithValue)*
	;

storageParameterWithValue:
	storageParameter EQ_OR_ASSIGN simpleExpr
	;

storageParameter:
	ID
	;

opclass:
	ID
	;

alterTable:
	(alterTableNameWithAsterisk(alterTableActions| renameColumn | renameConstraint))
    |(alterTableNameExists(renameTable | setSchema |attachTableSpace |detachTableSpace))
    |alterTableSetTableSpace
    ;
	
alterTableOp:
	ALTER TABLE
	;

alterTableActions:
	alterTableAction (COMMA alterTableAction)*
	;

renameColumn:
	 RENAME COLUMN? columnName TO columnName
    ;
    
renameConstraint:
	RENAME CONSTRAINT constraintName TO constraintName
    ; 
   
renameTable:
	RENAME TO tableName
    ; 
    
setSchema:
	SET SCHEMA schemaName
    ; 
    
alterTableSetTableSpace:
	alterTableOp ALL IN TABLESPACE tablespaceName (OWNED BY roleName (COMMA roleName)* )?
    SET TABLESPACE tablespaceName NOWAIT?
    ; 

attachTableSpace:
    ATTACH PARTITION partitionName forValuesParition
    ; 

detachTableSpace:
    DETACH PARTITION partitionName
    ; 
    
alterTableNameWithAsterisk:
	alterTableOp (IF EXISTS)? ONLY? tableName ASTERISK?
	;

alterTableNameExists:
	alterTableOp (IF EXISTS)? tableName 
	;

roleName:
	ID
	;
	
partitionName:
	ID
	;
	
triggerName:
	ID
	;
	
rewriteRuleName:
	ID
	;
	
ownerName:
	ID
	;

alterTableAction:
    (ADD COLUMN? (IF NOT EXISTS )? columnName dataType collateClause? (columnConstraint columnConstraint*)?)
    |(DROP COLUMN? (IF EXISTS)? columnName (RESTRICT | CASCADE)?)
    |(alterColumnOp columnName (SET DATA)? TYPE dataType collateClause? (USING expr)?)
    |(alterColumnOp columnName SET DEFAULT expr)
    |(alterColumnOp columnName DROP DEFAULT)
    |(alterColumnOp columnName (SET | DROP) NOT NULL)
    |(alterColumnOp columnName ADD GENERATED (ALWAYS | (BY DEFAULT)) AS IDENTITY (LEFT_PAREN sequenceOptions RIGHT_PAREN)?)
    |(alterColumnOp columnName  alterColumnSetOption alterColumnSetOption*)
    |(alterColumnOp columnName DROP IDENTITY (IF EXISTS)?)
    |(alterColumnOp columnName SET STATISTICS NUMBER)
    |(alterColumnOp columnName SET LEFT_PAREN attributeOptions RIGHT_PAREN)
    |(alterColumnOp columnName RESET LEFT_PAREN attributeOptions RIGHT_PAREN)
    |(alterColumnOp columnName SET STORAGE (PLAIN | EXTERNAL | EXTENDED | MAIN))
    |(ADD tableConstraint (NOT VALID)?)
   	|(ADD tableConstraintUsingIndex)
    |(ALTER CONSTRAINT constraintName constraintOptionalParam)
    |(VALIDATE CONSTRAINT constraintName)
    |(DROP CONSTRAINT (IF EXISTS)? constraintName (RESTRICT | CASCADE)?)
    |((DISABLE |ENABLE) TRIGGER (triggerName | ALL | USER )?)
    |(ENABLE (REPLICA | ALWAYS) TRIGGER triggerName)
    |((DISABLE | ENABLE) RULE rewriteRuleName)
    |(ENABLE (REPLICA | ALWAYS) RULE rewriteRuleName)
    |((DISABLE | ENABLE | (NO? FORCE)) ROW LEVEL SECURITY)
    |(CLUSTER ON indexName)
    |(SET WITHOUT CLUSTER)
    |(SET (WITH | WITHOUT) OIDS)
    |(SET TABLESPACE tablespaceName)
    |(SET (LOGGED | UNLOGGED))
    |(SET LEFT_PAREN storageParameterWithValue (COMMA storageParameterWithValue)* RIGHT_PAREN)
    |(RESET LEFT_PAREN storageParameter (COMMA storageParameter)* RIGHT_PAREN)
    |(INHERIT tableName)
    |(NO INHERIT tableName)
    |(OF typeName)
    |(NOT OF)
    |(OWNER TO (ownerName | CURRENT_USER | SESSION_USER))
    |(REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING))
	;

alterColumnOp:
	ALTER COLUMN?
	;
	
alterColumnSetOption:
	(SET GENERATED (ALWAYS | BY DEFAULT))
	|SET sequenceOption 
	|(RESTART (WITH? NUMBER)?)
	;

attributeOptions:
	attributeOption (COMMA attributeOption)*
	;
	
//options:n_distinct and n_distinct_inherited, loosen match
attributeOption:
	ID EQ_OR_ASSIGN simpleExpr
	;
	
tableConstraintUsingIndex:
    (CONSTRAINT constraintName)?
    (UNIQUE | PRIMARY KEY) USING INDEX indexName
    constraintOptionalParam
    ;
 
 constraintOptionalParam:
 	(NOT? DEFERRABLE)? (INITIALLY (DEFERRED |IMMEDIATE))?
 	;
    
privateExprOfDb:
 	aggregateExpression
 	|windowFunction
 	|arrayConstructorWithCast
 	|(TIMESTAMP (WITH TIME ZONE)? STRING)
 	;
 
 pgExpr:
 	|castExpr
 	|collateExpr
 	|expr
 	;
 	
 aggregateExpression:
 	ID (
 		(LEFT_PAREN (ALL | DISTINCT)? exprs  orderByClause? RIGHT_PAREN)
 		|asteriskWithParen
 		| (LEFT_PAREN exprs RIGHT_PAREN  WITHIN GROUP LEFT_PAREN orderByClause RIGHT_PAREN)
 		)  
 	filterClause ?
 	;
 	
 filterClause:
 	FILTER LEFT_PAREN WHERE booleanPrimary RIGHT_PAREN
 	;
 	
 asteriskWithParen:
 	LEFT_PAREN ASTERISK RIGHT_PAREN
 	;
 
 windowFunction:
 	ID (exprsWithParen | asteriskWithParen) 
 	filterClause? windowFunctionWithClause
 	; 
 
 windowFunctionWithClause:
 	OVER (ID | LEFT_PAREN windowDefinition RIGHT_PAREN )
 	;	
 
 windowDefinition:
 	ID? (PARTITION BY exprs)?
	(orderByExpr (COMMA orderByExpr)*)?
	frameClause?
 	;
 	
 orderByExpr:
 	ORDER BY expr (ASC | DESC | USING operator)?  (NULLS (FIRST | LAST ))?
 	;
 	
 frameClause:
 	((RANGE | ROWS) frameStart)
	 |(RANGE | ROWS ) BETWEEN frameStart AND frameEnd
	;
	
frameStart:
	(UNBOUNDED PRECEDING)
	|(NUMBER PRECEDING)
	|(CURRENT ROW)
	|(NUMBER FOLLOWING)
	|(UNBOUNDED FOLLOWING)
	;

frameEnd:
	frameStart
	;

castExpr:
	(CAST LEFT_PAREN expr AS dataType RIGHT_PAREN)
	|(expr COLON COLON dataType)
	;

castExprWithColon:
	COLON COLON dataType(LEFT_BRACKET RIGHT_BRACKET)*
	;
		
collateExpr:
	expr COLLATE expr
	;

arrayConstructorWithCast:
	arrayConstructor castExprWithColon?
	 |(ARRAY LEFT_BRACKET RIGHT_BRACKET castExprWithColon)	
	;
	
arrayConstructor:
	| ARRAY LEFT_BRACKET exprs RIGHT_BRACKET
	| ARRAY LEFT_BRACKET arrayConstructor (COMMA arrayConstructor)* RIGHT_BRACKET
	;