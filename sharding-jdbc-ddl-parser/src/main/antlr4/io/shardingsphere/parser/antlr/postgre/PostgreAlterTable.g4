grammar PostgreAlterTable;
import PostgreKeyword, DataType, Keyword, PostgreBase, BaseRule, Symbol;

alterTable:
	(alterTableNameWithAsterisk(alterTableActions| renameColumn | renameConstraint))
    |(alterTableNameExists(renameTable | setSchema |attachTableSpace |detachTableSpace))
    |alterTableSetTableSpace
    ;

alterTableNameWithAsterisk:
	alterTableOp (IF EXISTS)? ONLY? tableName ASTERISK?
	;

alterTableOp:
	ALTER TABLE
	;

alterTableActions:
	alterTableAction (COMMA alterTableAction)*
	;

alterTableAction:
    (ADD COLUMN? (IF NOT EXISTS )? columnName dataType collateClause? (columnConstraint columnConstraint*)?)
    |(DROP COLUMN? (IF EXISTS)? columnName (RESTRICT | CASCADE)?)
    |(alterColumnOp columnName (SET DATA)? TYPE dataType collateClause? (USING simpleExpr)?)
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

renameColumn:
	 RENAME COLUMN? columnName TO columnName
    ;

renameConstraint:
	RENAME CONSTRAINT constraintName TO constraintName
    ;

alterTableNameExists:
	alterTableOp (IF EXISTS)? tableName
	;

renameTable:
	RENAME TO tableName
    ;

setSchema:
	SET SCHEMA schemaName
    ;

attachTableSpace:
    ATTACH PARTITION partitionName forValuesParition
    ;

detachTableSpace:
    DETACH PARTITION partitionName
    ;

alterTableSetTableSpace:
	alterTableOp ALL IN TABLESPACE tablespaceName (OWNED BY roleName (COMMA roleName)* )?
    SET TABLESPACE tablespaceName NOWAIT?
    ;