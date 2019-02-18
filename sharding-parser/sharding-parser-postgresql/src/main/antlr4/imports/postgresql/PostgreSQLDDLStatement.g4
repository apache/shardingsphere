grammar PostgreSQLDDLStatement;

import PostgreSQLKeyword, Keyword, Symbol, PostgreSQLBase, DataType, BaseRule;

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
    : alterTableNameWithAsterisk (alterTableActions | renameColumn | renameConstraint) | alterTableNameExists renameTable
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
    : ALTER INDEX indexName DEPENDS ON EXTENSION ignoredIdentifier_
    ;

alterIndexSetTableSpace
    : ALTER INDEX ALL IN TABLESPACE indexName (OWNED BY ignoredIdentifiers_)?
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
    | ALTER CONSTRAINT ignoredIdentifier_ constraintOptionalParam
    | VALIDATE CONSTRAINT ignoredIdentifier_
    | DROP CONSTRAINT (IF EXISTS)? ignoredIdentifier_ (RESTRICT | CASCADE)?
    | (DISABLE | ENABLE) TRIGGER (ignoredIdentifier_ | ALL | USER)?
    | ENABLE (REPLICA | ALWAYS) TRIGGER ignoredIdentifier_
    | (DISABLE | ENABLE) RULE ignoredIdentifier_
    | ENABLE (REPLICA | ALWAYS) RULE ignoredIdentifier_
    | (DISABLE | ENABLE | (NO? FORCE)) ROW LEVEL SECURITY
    | CLUSTER ON indexName
    | SET WITHOUT CLUSTER
    | SET (WITH | WITHOUT) OIDS
    | SET TABLESPACE ignoredIdentifier_
    | SET (LOGGED | UNLOGGED)
    | SET LP_ storageParameterWithValue (COMMA_ storageParameterWithValue)* RP_
    | RESET LP_ storageParameter (COMMA_ storageParameter)* RP_
    | INHERIT tableName
    | NO INHERIT tableName
    | OF dataTypeName_
    | NOT OF
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING)
    ;

tableConstraintUsingIndex
    : (CONSTRAINT ignoredIdentifier_)? (UNIQUE | primaryKey) USING INDEX indexName constraintOptionalParam
    ;

addColumn
    : ADD COLUMN? (IF NOT EXISTS)? columnDefinition
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
    : RENAME CONSTRAINT ignoredIdentifier_ TO ignoredIdentifier_
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

usingIndexType
    : USING (BTREE | HASH | GIST | SPGIST | GIN | BRIN)
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

excludeElement
    : (columnName | expr) ignoredIdentifier_? (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;
