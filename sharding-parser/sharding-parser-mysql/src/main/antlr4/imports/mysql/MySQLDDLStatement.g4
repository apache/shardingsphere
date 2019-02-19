grammar MySQLDDLStatement;

import MySQLKeyword, Keyword, Symbol, MySQLDQLStatement, DataType, MySQLBase, BaseRule;

createTable
    : CREATE TEMPORARY? TABLE (IF NOT EXISTS)? tableName (LP_ createDefinitions_ RP_ | createLike_)
    ;

createDefinitions_
    : createDefinition_ (COMMA_ createDefinition_)*
    ;

createDefinition_
    : columnDefinition | constraintDefinition | indexDefinition_ | checkConstraintDefinition_
    ;

columnDefinition
    : columnName dataType (dataTypeOption_* | dataTypeGenerated_?)
    ;

dataType
    : dataTypeName_ dataTypeLength? characterSet_? collateClause_? UNSIGNED? ZEROFILL? | dataTypeName_ LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet_? collateClause_?
    ;

dataTypeName_
    : ID ID?
    ;

characterSet_
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier_ | CHARSET EQ_? ignoredIdentifier_
    ;

collateClause_
    : COLLATE EQ_? (STRING_ | ignoredIdentifier_)
    ;

dataTypeOption_
    : dataTypeGeneratedOption_
    | DEFAULT? defaultValue_
    | AUTO_INCREMENT
    | COLUMN_FORMAT (FIXED | DYNAMIC | DEFAULT)
    | STORAGE (DISK | MEMORY | DEFAULT)
    | referenceDefinition_
    ;

dataTypeGeneratedOption_
    : NULL | NOT NULL | UNIQUE KEY? | primaryKey | COMMENT STRING_
    ;

dataTypeGenerated_
    : (GENERATED ALWAYS)? AS expr (VIRTUAL | STORED)? dataTypeGeneratedOption_*
    ;

defaultValue_
    : NULL | NUMBER_ | STRING_ | currentTimestampType_ (ON UPDATE currentTimestampType_)? | ON UPDATE currentTimestampType_
    ;

currentTimestampType_
    : (CURRENT_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | NOW | NUMBER_) dataTypeLength?
    ;

referenceDefinition_
    : REFERENCES tableName keyParts_ (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? referenceType_*
    ;

referenceType_
    : ON (UPDATE | DELETE) referenceOption_
    ;

referenceOption_
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

constraintDefinition
    : (CONSTRAINT ignoredIdentifier_?)? (primaryKeyOption_ | uniqueOption_ | foreignKeyOption_)
    ;

primaryKeyOption_
    : primaryKey indexType? columnNames indexOption*
    ;

uniqueOption_
    : UNIQUE (INDEX | KEY)? indexName? indexType? keyParts_ indexOption*
    ;

foreignKeyOption_
    : FOREIGN KEY indexName? columnNames referenceDefinition_
    ;

indexDefinition_
    : (FULLTEXT | SPATIAL)? (INDEX | KEY)? indexName? indexType? keyParts_ indexOption*
    ;

keyParts_
    : LP_ keyPart_ (COMMA_ keyPart_)* RP_
    ;

keyPart_
    : columnName (LP_ NUMBER_ RP_)? (ASC | DESC)?
    ;

checkConstraintDefinition_
    : (CONSTRAINT ignoredIdentifier_?)? CHECK expr (NOT? ENFORCED)?
    ;

createLike_
    : LIKE tableName | LP_ LIKE tableName RP_
    ;

alterTable
    : ALTER TABLE tableName alterSpecifications_?
    ;

alterSpecifications_
    : alterSpecification_ (COMMA_ alterSpecification_)*
    ;

alterSpecification_
    : tableOptions_
    | addColumn
    | addIndex
    | addConstraint
    | ALGORITHM EQ_? (DEFAULT | INPLACE | COPY)
    | ALTER COLUMN? columnName (SET DEFAULT | DROP DEFAULT)
    | changeColumn
    | DEFAULT? characterSet_ collateClause_?
    | CONVERT TO characterSet_ collateClause_?
    | (DISABLE | ENABLE) KEYS
    | (DISCARD | IMPORT_) TABLESPACE
    | dropColumn
    | dropIndexDef
    | dropPrimaryKey
    | DROP FOREIGN KEY ignoredIdentifier_
    | FORCE
    | LOCK EQ_? (DEFAULT | NONE | SHARED | EXCLUSIVE)
    | modifyColumn
    | ORDER BY columnName (COMMA_ columnName)*
    | renameIndex
    | renameTable
    | (WITHOUT | WITH) VALIDATION
    | ADD PARTITION partitionDefinitions_
    | DROP PARTITION ignoredIdentifiers_
    | DISCARD PARTITION (ignoredIdentifiers_ | ALL) TABLESPACE
    | IMPORT_ PARTITION (ignoredIdentifiers_ | ALL) TABLESPACE
    | TRUNCATE PARTITION (ignoredIdentifiers_ | ALL)
    | COALESCE PARTITION NUMBER_
    | REORGANIZE PARTITION ignoredIdentifiers_ INTO partitionDefinitions_
    | EXCHANGE PARTITION ignoredIdentifier_ WITH TABLE tableName ((WITH | WITHOUT) VALIDATION)?
    | ANALYZE PARTITION (ignoredIdentifiers_ | ALL)
    | CHECK PARTITION (ignoredIdentifiers_ | ALL)
    | OPTIMIZE PARTITION (ignoredIdentifiers_ | ALL)
    | REBUILD PARTITION (ignoredIdentifiers_ | ALL)
    | REPAIR PARTITION (ignoredIdentifiers_ | ALL)
    | REMOVE PARTITIONING
    | UPGRADE PARTITIONING
    ;

singleColumn
    : columnDefinition firstOrAfterColumn?
    ;

firstOrAfterColumn
    : FIRST | AFTER columnName
    ;

multiColumn
    : LP_ columnDefinition (COMMA_ columnDefinition)* RP_
    ;

addConstraint
    : ADD constraintDefinition
    ;

addIndex
    : ADD indexDefinition_
    ;

addColumn
    : ADD COLUMN? (singleColumn | multiColumn)
    ;

changeColumn
    : changeColumnOp columnName columnDefinition firstOrAfterColumn?
    ;

changeColumnOp
    : CHANGE COLUMN?
    ;

dropColumn
    : DROP COLUMN? columnName
    ;

dropIndexDef
    : DROP (INDEX | KEY) indexName
    ;

dropPrimaryKey
    : DROP primaryKey
    ;

modifyColumn
    : MODIFY COLUMN? columnDefinition firstOrAfterColumn?
    ;

renameIndex
    : RENAME (INDEX | KEY) indexName TO indexName
    ;

renameTable
    : RENAME (TO | AS)? tableName
    ;

tableOptions_
    : tableOption_ (COMMA_? tableOption_)*
    ;

tableOption_
    : AUTO_INCREMENT EQ_? NUMBER_
    | AVG_ROW_LENGTH EQ_? NUMBER_
    | DEFAULT? (characterSet_ | collateClause_)
    | CHECKSUM EQ_? NUMBER_
    | COMMENT EQ_? STRING_
    | COMPRESSION EQ_? STRING_
    | CONNECTION EQ_? STRING_
    | (DATA | INDEX) DIRECTORY EQ_? STRING_
    | DELAY_KEY_WRITE EQ_? NUMBER_
    | ENCRYPTION EQ_? STRING_
    | ENGINE EQ_? ignoredIdentifier_
    | INSERT_METHOD EQ_? (NO | FIRST | LAST)
    | KEY_BLOCK_SIZE EQ_? NUMBER_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | PACK_KEYS EQ_? (NUMBER_ | DEFAULT)
    | PASSWORD EQ_? STRING_
    | ROW_FORMAT EQ_? (DEFAULT | DYNAMIC | FIXED | COMPRESSED | REDUNDANT | COMPACT)
    | STATS_AUTO_RECALC EQ_? (DEFAULT | NUMBER_)
    | STATS_PERSISTENT EQ_? (DEFAULT | NUMBER_)
    | STATS_SAMPLE_PAGES EQ_? NUMBER_
    | TABLESPACE ignoredIdentifier_ (STORAGE (DISK | MEMORY | DEFAULT))?
    | UNION EQ_? tableNames_
    ;

tableNames_
    : LP_ tableName (COMMA_ tableName)* RP_
    ;

partitionOptions_
    : PARTITION BY (linearPartition_ | rangeOrListPartition_) (PARTITIONS NUMBER_)? (SUBPARTITION BY linearPartition_ (SUBPARTITIONS NUMBER_)?)? (LP_ partitionDefinitions_ RP_)?
    ;

linearPartition_
    : LINEAR? (HASH (yearFunctionExpr_ | expr) | KEY (ALGORITHM EQ_ NUMBER_)? columnNames)
    ;

yearFunctionExpr_
    : LP_ YEAR expr RP_
    ;

rangeOrListPartition_
    : (RANGE | LIST) (expr | COLUMNS columnNames)
    ;

partitionDefinitions_
    : partitionDefinition_ (COMMA_ partitionDefinition_)*
    ;

partitionDefinition_
    : PARTITION ignoredIdentifier_ (VALUES (lessThanPartition_ | IN assignmentValueList))? partitionDefinitionOption_* (LP_ subpartitionDefinition_ (COMMA_ subpartitionDefinition_)* RP_)?
    ;

partitionDefinitionOption_
    : STORAGE? ENGINE EQ_? ignoredIdentifier_
    | COMMENT EQ_? STRING_
    | DATA DIRECTORY EQ_? STRING_
    | INDEX DIRECTORY EQ_? STRING_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | TABLESPACE EQ_? ignoredIdentifier_
    ;

lessThanPartition_
    : LESS THAN (LP_ (expr | assignmentValues) RP_ | MAXVALUE)
    ;

subpartitionDefinition_
    : SUBPARTITION ignoredIdentifier_ partitionDefinitionOption_*
    ;

dropTable
    : DROP TEMPORARY? TABLE (IF EXISTS)? tableName (COMMA_ tableName)*
    ;

truncateTable
    : TRUNCATE TABLE? tableName
    ;

createIndex
    : CREATE (UNIQUE | FULLTEXT | SPATIAL)? INDEX indexName indexType? ON tableName
    ;

dropIndex
    : DROP INDEX (ONLINE | OFFLINE)? indexName ON tableName
    ;
