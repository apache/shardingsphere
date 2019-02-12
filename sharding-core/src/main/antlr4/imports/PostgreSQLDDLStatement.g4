grammar PostgreSQLDDLStatement;

import PostgreSQLKeyword, Keyword, DataType, PostgreSQLBase, BaseRule, Symbol;

createIndex
    : CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName 
    ;
    
dropIndex
    : DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexNames
    ;
    
alterIndex
    : alterIndexName renameIndex | alterIndexDependsOnExtension | alterIndexSetTableSpace
    ;
    
createTable
    : createTableHeader createDefinitions inheritClause?
    ;
    
alterTable
    : alterTableNameWithAsterisk (alterTableActions | renameColumn | renameConstraint)
    | alterTableNameExists renameTable
    ;
    
truncateTable
    : TRUNCATE TABLE? ONLY? tableNameParts
    ;
    
dropTable
    : DROP TABLE (IF EXISTS)? tableNames
    ;
    
    
alterIndexName
    : ALTER INDEX (IF EXISTS)? indexName
    ;
    
renameIndex
    : RENAME TO indexName
    ;
    
alterIndexDependsOnExtension
    : ALTER INDEX indexName DEPENDS ON EXTENSION extensionName
    ;
    
alterIndexSetTableSpace
    : ALTER INDEX ALL IN TABLESPACE indexName (OWNED BY rowNames)?
    ;
    
tableNameParts
    : tableNamePart (COMMA_ tableNamePart)*
    ;
    
tableNamePart
    : tableName ASTERISK_?
    ;
    
createTableHeader
    : CREATE ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)? TABLE (IF NOT EXISTS)? tableName
    ;
    
createDefinitions
    : LP_ (createDefinition (COMMA_ createDefinition)*)? RP_
    ;
    
createDefinition
    : columnDefinition | tableConstraint | LIKE tableName likeOption*
    ;
    
likeOption
    : (INCLUDING | EXCLUDING) (COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
    ;
    
inheritClause
    : INHERITS LP_ tableName (COMMA_ tableName)* RP_
    ;
    
alterTableNameWithAsterisk
    : ALTER TABLE (IF EXISTS)? ONLY? tableName ASTERISK_?
    ;
    
alterTableActions
    : alterTableAction (COMMA_ alterTableAction)*
    ;
    
alterTableAction
    : addColumn
    | dropColumn
    | modifyColumn
    | addConstraint
    | ALTER CONSTRAINT constraintName constraintOptionalParam
    | VALIDATE CONSTRAINT constraintName
    | DROP CONSTRAINT (IF EXISTS)? constraintName (RESTRICT | CASCADE)?
    | (DISABLE | ENABLE) TRIGGER (triggerName | ALL | USER )?
    | ENABLE (REPLICA | ALWAYS) TRIGGER triggerName
    | (DISABLE | ENABLE) RULE rewriteRuleName
    | ENABLE (REPLICA | ALWAYS) RULE rewriteRuleName
    | (DISABLE | ENABLE | (NO? FORCE)) ROW LEVEL SECURITY
    | CLUSTER ON indexName
    | SET WITHOUT CLUSTER
    | SET (WITH | WITHOUT) OIDS
    | SET TABLESPACE tablespaceName
    | SET (LOGGED | UNLOGGED)
    | SET LP_ storageParameterWithValue (COMMA_ storageParameterWithValue)* RP_
    | RESET LP_ storageParameter (COMMA_ storageParameter)* RP_
    | INHERIT tableName
    | NO INHERIT tableName
    | OF typeName
    | NOT OF
    | OWNER TO (ownerName | CURRENT_USER | SESSION_USER)
    | REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING)
    ;
    
tableConstraintUsingIndex
    : (CONSTRAINT constraintName)?
    (UNIQUE | primaryKey) USING INDEX indexName
    constraintOptionalParam
    ;
    
constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED | IMMEDIATE))?
    ;
    
addColumn
    : ADD COLUMN? (IF NOT EXISTS )? columnDefinition
    ;
    
dropColumn
    : DROP COLUMN? (IF EXISTS)? columnName (RESTRICT | CASCADE)?
    ;
    
modifyColumn
    : alterColumn (SET DATA)? TYPE dataType collateClause? (USING simpleExpr)?
    | alterColumn SET DEFAULT expr
    | alterColumn DROP DEFAULT
    | alterColumn (SET | DROP) NOT NULL
    | alterColumn ADD GENERATED (ALWAYS | (BY DEFAULT)) AS IDENTITY (LP_ sequenceOptions RP_)?
    | alterColumn alterColumnSetOption alterColumnSetOption*
    | alterColumn DROP IDENTITY (IF EXISTS)?
    | alterColumn SET STATISTICS NUMBER_
    | alterColumn SET LP_ attributeOptions RP_
    | alterColumn RESET LP_ attributeOptions RP_
    | alterColumn SET STORAGE (PLAIN | EXTERNAL | EXTENDED | MAIN)
    ;
    
alterColumn
    : ALTER COLUMN? columnName
    ;
    
alterColumnSetOption
    : SET (GENERATED (ALWAYS | BY DEFAULT) | sequenceOption) | RESTART (WITH? NUMBER_)?
    ;
    
attributeOptions
    : attributeOption (COMMA_ attributeOption)*
    ;
    
attributeOption
    : ID EQ_ simpleExpr
    ;
    
addConstraint
    : ADD (tableConstraint (NOT VALID)? | tableConstraintUsingIndex)
    ;
    
renameColumn
    : RENAME COLUMN? columnName TO columnName
    ;
    
renameConstraint
    : RENAME CONSTRAINT constraintName TO constraintName
    ;
    
storageParameterWithValue
    : storageParameter EQ_ simpleExpr
    ;
    
storageParameter
    : ID
    ;
    
alterTableNameExists
    : ALTER TABLE (IF EXISTS)? tableName
    ;
    
renameTable
    : RENAME TO tableName
    ;
    