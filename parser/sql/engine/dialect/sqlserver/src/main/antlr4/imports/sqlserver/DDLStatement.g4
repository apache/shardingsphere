/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar DDLStatement;

import DMLStatement, DCLStatement;

createTable
    : createTableClause | createTableAsSelectClause
    ;

createTableClause
    : CREATE TABLE tableName fileTableClause createDefinitionClause
    ;

createIndex
    : CREATE createIndexSpecification INDEX indexName ON tableName columnNamesWithSort createIndexClause
    ;

createDatabase
    : CREATE DATABASE databaseName createDatabaseClause
    ;

createFunction
    : CREATE (OR ALTER)? FUNCTION functionName funcParameters funcReturns
    ;

createProcedure
    : CREATE (OR ALTER)? (PROC | PROCEDURE) procedureName procParameters createOrAlterProcClause
    ;

createView
    : CREATE (OR ALTER)? VIEW viewName createOrAlterViewClause
    ;

createTrigger
    : CREATE (OR ALTER)? TRIGGER triggerName ON triggerTarget createTriggerClause
    ;

createSequence
    : CREATE SEQUENCE sequenceName createOrAlterSequenceClause*
    ;

createService
    : CREATE SERVICE serviceName (AUTHORIZATION STRING_)? ON QUEUE queueName createServiceClause?
    ;

createSchema
    : CREATE SCHEMA schemaNameClause schemaElement*
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause (COMMA_ alterDefinitionClause)*
    ;

alterIndex
    : ALTER INDEX (indexName | ALL) ON tableName alterIndexClause
    ;

alterDatabase
    : ALTER DATABASE (databaseName | CURRENT) alterDatabaseClause*
    ;

alterProcedure
    : ALTER (PROC | PROCEDURE) procedureName procParameters createOrAlterProcClause
    ;

alterFunction
    : ALTER FUNCTION functionName funcParameters funcReturns
    ;

alterView
    : ALTER VIEW viewName createOrAlterViewClause
    ;

alterTrigger
    : ALTER TRIGGER triggerName ON triggerTarget createTriggerClause
    ;

alterSequence
    : ALTER SEQUENCE sequenceName createOrAlterSequenceClause*
    ;

alterService
    : ALTER SERVICE serviceName (ON QUEUE queueName)? alterServiceClause?
    ;

alterSchema
    : ALTER SCHEMA schemaName TRANSFER class_? securableName
    ;

securableName
    : identifier (DOT_ identifier)?
    ;

dropTable
    : DROP TABLE ifExists? tableNames
    ;

dropIndex
    : DROP INDEX ifExists? indexName ON tableName
    ;

dropDatabase
    : DROP DATABASE ifExists? databaseName (COMMA_ databaseName)*
    ;

dropFunction
    : DROP FUNCTION ifExists? functionName (COMMA_ functionName)*
    ;

dropProcedure
    : DROP (PROC | PROCEDURE) ifExists? procedureName (COMMA_ procedureName)*
    ;

dropView
    : DROP VIEW ifExists? viewName (COMMA_ viewName)*
    ;

dropTrigger
    : DROP TRIGGER ifExists? triggerName (COMMA_ triggerName)* (ON (DATABASE | ALL SERVER))?
    ;

dropSequence
    : DROP SEQUENCE ifExists? sequenceName (COMMA_ sequenceName)*
    ;

dropService
    : DROP SERVICE serviceName
    ;

dropSchema
    : DROP SCHEMA ifExists? schemaName
    ;

truncateTable
    : TRUNCATE TABLE tableName
    ;

updateStatistics
    : UPDATE STATISTICS tableName (LP_? indexName (COMMA_ indexName)* RP_?)? statisticsWithClause?
    ;

statisticsWithClause
    : WITH sampleOption? statisticsOptions?
    ;

sampleOption
    : (FULLSCAN | (SAMPLE NUMBER_ (PERCENT | ROWS))) (PERSIST_SAMPLE_PERCENT EQ_ (ON | OFF))?
    | RESAMPLE (ON PARTITIONS LP_ NUMBER_ (COMMA_ NUMBER_)* RP_)?
    ;

statisticsOptions
    : (COMMA_? statisticsOption)+
    ;

statisticsOption
    : ALL | COLUMNS | INDEX
    | NORECOMPUTE
    | INCREMENTAL EQ_ (ON | OFF)
    | MAXDOP EQ_ NUMBER_
    | AUTO_DROP EQ_ (ON | OFF)
    ;

fileTableClause
    : (AS FILETABLE)?
    ;

createDefinitionClause
    : createTableDefinitions partitionScheme fileGroup
    ;

createTableDefinitions
    : LP_ createTableDefinition (COMMA_ createTableDefinition)* (COMMA_ periodClause)? RP_
    ;

createTableDefinition
    : columnDefinition | computedColumnDefinition | columnSetDefinition | tableConstraint | tableIndex
    ;

columnDefinition
    : columnName dataType columnDefinitionOption* columnConstraints columnIndex?
    ;

columnDefinitionOption
    : FILESTREAM
    | COLLATE collationName
    | SPARSE
    | MASKED WITH LP_ FUNCTION EQ_ STRING_ RP_
    | (CONSTRAINT ignoredIdentifier)? DEFAULT expr
    | IDENTITY (LP_ NUMBER_ COMMA_ NUMBER_ RP_)?
    | NOT FOR REPLICATION
    | GENERATED ALWAYS AS ROW (START | END) HIDDEN_?
    | NOT? NULL
    | ROWGUIDCOL 
    | ENCRYPTED WITH encryptedOptions
    | columnConstraint (COMMA_ columnConstraint)*
    | columnIndex
    ;

encryptedOptions
    : LP_ COLUMN_ENCRYPTION_KEY EQ_ ignoredIdentifier COMMA_ ENCRYPTION_TYPE EQ_ (DETERMINISTIC | RANDOMIZED) COMMA_ ALGORITHM EQ_ STRING_ RP_
    ;

columnConstraint
    : (CONSTRAINT constraintName)? (primaryKeyConstraint | columnForeignKeyConstraint | checkConstraint)
    ;

computedColumnConstraint
    : (CONSTRAINT constraintName)? (primaryKeyConstraint | computedColumnForeignKeyConstraint | checkConstraint)
    ;

computedColumnForeignKeyConstraint
    : (FOREIGN KEY)? tableName (LP_ columnName RP_)? computedColumnForeignKeyOnAction*
    ;

computedColumnForeignKeyOnAction
    : ON DELETE (NO ACTION | CASCADE) | ON UPDATE NO ACTION | NOT FOR REPLICATION
    ;

primaryKeyConstraint
    : (primaryKey | UNIQUE) (diskTablePrimaryKeyConstraintOption | memoryTablePrimaryKeyConstraintOption)
    ;

diskTablePrimaryKeyConstraintOption
    : clusterOption? primaryKeyWithClause? primaryKeyOnClause?
    ;

clusterOption
    : CLUSTERED | NONCLUSTERED
    ;

primaryKeyWithClause
    : WITH (FILLFACTOR EQ_ NUMBER_ | LP_ indexOption (COMMA_ indexOption)* RP_)
    ;

primaryKeyOnClause
    : onSchemaColumn | onFileGroup | onString
    ;

onSchemaColumn
    : ON schemaName LP_ columnName RP_
    ;

onFileGroup
    : ON ignoredIdentifier
    ;

onString
    : ON STRING_
    ;

memoryTablePrimaryKeyConstraintOption
    : NONCLUSTERED
    | NONCLUSTERED HASH withBucket?
    ;

withBucket
    : WITH LP_ BUCKET_COUNT EQ_ NUMBER_ RP_
    ;

columnForeignKeyConstraint
    : (FOREIGN KEY)? REFERENCES tableName (LP_ columnName RP_)? foreignKeyOnAction*
    ;

foreignKeyOnAction
    : ON (DELETE | UPDATE) foreignKeyOn | NOT FOR REPLICATION
    ;

foreignKeyOn
    : NO ACTION | CASCADE | SET (NULL | DEFAULT)
    ;

checkConstraint
    : CHECK(NOT FOR REPLICATION)? LP_ expr RP_
    ;

columnIndex
    : INDEX indexName clusterOption? withIndexOption? indexOnClause? fileStreamOn?
    ;

withIndexOption
    : WITH LP_ indexOption (COMMA_ indexOption)* RP_
    ;

indexOnClause
    : onSchemaColumn | onFileGroup | onDefault
    ;

onDefault
    : ON DEFAULT
    ;

fileStreamOn
    : FILESTREAM_ON (ignoredIdentifier | schemaName | STRING_)
    ;

columnConstraints
    : (columnConstraint(COMMA_ columnConstraint)*)?
    ;

computedColumnDefinition
    : columnName AS expr (PERSISTED(NOT NULL)?)? computedColumnConstraint?
    ;

columnSetDefinition 
    : ignoredIdentifier IDENTIFIER_ COLUMN_SET FOR ALL_SPARSE_COLUMNS
    ;

tableConstraint 
    : (CONSTRAINT constraintName)? (tablePrimaryConstraint | tableForeignKeyConstraint | checkConstraint | edgeConstraint)
    ;

edgeConstraint
    : connectionClause (ON DELETE (NO ACTION | CASCADE))?
    ;

connectionClause
    : CONNECTION LP_ (nodeAlias TO nodeAlias) (COMMA_ nodeAlias TO nodeAlias)*? RP_
    ;

tablePrimaryConstraint
    : primaryKeyUnique (diskTablePrimaryConstraintOption | memoryTablePrimaryConstraintOption)
    ;

primaryKeyUnique
    : primaryKey | UNIQUE
    ;

diskTablePrimaryConstraintOption
    : clusterOption? columnNames primaryKeyWithClause? primaryKeyOnClause?
    ;

memoryTablePrimaryConstraintOption
    : NONCLUSTERED (columnNames | hashWithBucket)
    ;

hashWithBucket
    : HASH columnNames withBucket
    ;

tableForeignKeyConstraint
    : (FOREIGN KEY)? columnNames REFERENCES tableName columnNames foreignKeyOnAction*
    ;

tableIndex
    : INDEX indexName indexNameOption (WITH indexOptions)? indexOnClause? fileStreamOn?
    ;

indexNameOption
    : clusterOption? columnNames | CLUSTERED COLUMNSTORE | NONCLUSTERED? COLUMNSTORE columnNames
    ;

indexOptions
    : LP_ indexOption (COMMA_ indexOption)* RP_
    ;

periodClause
    : PERIOD FOR SYSTEM_TIME LP_ columnName COMMA_ columnName RP_
    ;

partitionScheme
    : (ON (schemaName LP_ columnName RP_ | ignoredIdentifier | STRING_))?
    ;

fileGroup
    : (TEXTIMAGE_ON (ignoredIdentifier | STRING_))? ((FILESTREAM_ON (schemaName) | ignoredIdentifier STRING_))? (WITH tableOptions)?
    ;

tableOptions
    : LP_ tableOption (COMMA_ tableOption)* RP_
    ;

tableOption
    : DATA_COMPRESSION EQ_ (NONE | ROW | PAGE) (ON PARTITIONS LP_ partitionExpressions RP_)?
    | FILETABLE_DIRECTORY EQ_ ignoredIdentifier
    | FILETABLE_COLLATE_FILENAME EQ_ (collationName | DATABASE_DEAULT)
    | FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME EQ_ ignoredIdentifier
    | FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME EQ_ ignoredIdentifier
    | FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME EQ_ ignoredIdentifier
    | SYSTEM_VERSIONING EQ_ ON onHistoryTableClause?
    | REMOTE_DATA_ARCHIVE EQ_ (ON tableStretchOptions? | OFF migrationState_)
    | tableOperationOption
    | distributionOption
    | dataWareHouseTableOption
    | dataDelectionOption
    | dataWareHousePartitionOption
    ;

dataDelectionOption
    : DATA_DELETION EQ_ ON (LP_ FILTER_COLUMN EQ_ columnName COMMA_ RETENTION_PERIOD EQ_ historyRetentionPeriod)
    ;

tableStretchOptions
    : LP_ tableStretchOption (COMMA_ tableStretchOption)* RP_
    ;

tableStretchOption
    : (FILTER_PREDICATE EQ_ (NULL | functionCall) COMMA_)? MIGRATION_STATE EQ_ (OUTBOUND | INBOUND | PAUSED)
    ;

migrationState_
    : LP_ MIGRATION_STATE EQ_ PAUSED RP_
    ;

tableOperationOption
    : (MEMORY_OPTIMIZED EQ_ ON) | (DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA)) | (SYSTEM_VERSIONING EQ_ ON onHistoryTableClause?)
    ;

distributionOption
    : DISTRIBUTION EQ_ (HASH LP_ columnName RP_ | ROUND_ROBIN | REPLICATE) 
    ;

dataWareHouseTableOption
    : CLUSTERED COLUMNSTORE INDEX | CLUSTERED COLUMNSTORE INDEX ORDER columnNames | HEAP | CLUSTERED INDEX LP_ (columnName (ASC | DESC)?) (COMMA_ (columnName (ASC | DESC)?))* RP_
    ;

dataWareHousePartitionOption
    : (PARTITION LP_ columnName RANGE (LEFT | RIGHT)? FOR VALUES LP_ simpleExpr (COMMA_ simpleExpr)* RP_ RP_)
    ;

createIndexSpecification
    : UNIQUE? clusterOption?
    ;

alterDefinitionClause
    : addColumnSpecification | modifyColumnSpecification | alterDrop | alterCheckConstraint | alterTableTrigger | alterSwitch | alterSet | alterTableOption | REBUILD
    ;

addColumnSpecification
    : (WITH (CHECK | NOCHECK))? ADD (alterColumnAddOptions | generatedColumnNamesClause)
    ;

modifyColumnSpecification
    : alterColumnOperation dataType (COLLATE collationName)? (NULL | NOT NULL)? SPARSE?
    ;

alterColumnOperation
    : ALTER COLUMN columnName
    ;

alterColumnAddOptions
    : alterColumnAddOption (COMMA_ alterColumnAddOption)*
    ;

alterColumnAddOption
    : columnDefinition
    | computedColumnDefinition
    | columnSetDefinition
    | tableConstraint
    | alterTableTableIndex
    | constraintForColumn
    ;

constraintForColumn
    : (CONSTRAINT constraintName)? DEFAULT simpleExpr FOR columnName
    ;

generatedColumnNamesClause
    : generatedColumnNameClause COMMA_ periodClause | periodClause COMMA_ generatedColumnNameClause
    ;

generatedColumnNameClause
    : generatedColumnName DEFAULT simpleExpr (WITH VALUES)? COMMA_ generatedColumnName
    ;

generatedColumnName
    : columnName dataTypeName GENERATED ALWAYS AS ROW (START | END)? HIDDEN_? (NOT NULL)? (CONSTRAINT ignoredIdentifier)?
    ;

alterDrop
    : DROP (alterTableDropConstraint | dropColumnSpecification | dropIndexSpecification | PERIOD FOR SYSTEM_TIME)
    ;

alterTableDropConstraint
    : CONSTRAINT? ifExists? dropConstraintName (COMMA_ dropConstraintName)*
    ;

dropConstraintName
    : constraintName dropConstraintWithClause?
    ;

dropConstraintWithClause
    : WITH LP_ dropConstraintOption (COMMA_ dropConstraintOption)* RP_
    ;

dropConstraintOption
    : (MAXDOP EQ_ NUMBER_ | ONLINE EQ_ onOffOption | MOVE TO (schemaName LP_ columnName RP_ | ignoredIdentifier | STRING_))
    ;

onOffOption
    : ON | OFF
    ;

dropColumnSpecification
    : COLUMN ifExists? columnName (COMMA_ columnName)*
    ;

dropIndexSpecification
    : INDEX ifExists? indexName (COMMA_ indexName)*
    ;

alterCheckConstraint 
    : WITH? (CHECK | NOCHECK) CONSTRAINT (ALL | constraintName)
    ;

alterTableTrigger
    : (ENABLE| DISABLE) TRIGGER (ALL | ignoredIdentifiers)
    ;

alterSwitch
    : SWITCH (PARTITION expr)? TO tableName (PARTITION expr)? (WITH LP_ lowPriorityLockWait RP_)?
    ;

alterSet
    : SET LP_ (setFileStreamClause | setSystemVersionClause) RP_ 
    ;

setFileStreamClause
    : FILESTREAM_ON EQ_ (schemaName | ignoredIdentifier | STRING_)
    ;

setSystemVersionClause
    : SYSTEM_VERSIONING EQ_ (OFF | ON alterSetOnClause?)
    ;

alterSetOnClause
    : LP_ (HISTORY_TABLE EQ_ tableName)? dataConsistencyCheckClause? historyRetentionPeriodClause? RP_
    ;

dataConsistencyCheckClause
    : COMMA_? DATA_CONSISTENCY_CHECK EQ_ onOffOption
    ;

historyRetentionPeriodClause
    : COMMA_? HISTORY_RETENTION_PERIOD EQ_ historyRetentionPeriod
    ;

historyRetentionPeriod
    : INFINITE | (NUMBER_ (DAY | DAYS | WEEK | WEEKS | MONTH | MONTHS | YEAR | YEARS))
    ;

alterTableTableIndex
    : indexWithName (indexNonClusterClause | indexClusterClause)
    ;

indexWithName
    : INDEX indexName
    ;

indexNonClusterClause
    : NONCLUSTERED (hashWithBucket | columnNamesWithSort alterTableIndexOnClause?)
    ;

alterTableIndexOnClause
    : ON ignoredIdentifier | DEFAULT
    ;

indexClusterClause
    : CLUSTERED COLUMNSTORE (WITH COMPRESSION_DELAY EQ_ NUMBER_ MINUTES?)? indexOnClause?
    ;

alterTableOption
    : SET LP_ LOCK_ESCALATION EQ_ (AUTO | TABLE | DISABLE) RP_
    | MEMORY_OPTIMIZED EQ_ ON
    | DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA) 
    | SYSTEM_VERSIONING EQ_ ON onHistoryTableClause?
    ;

onHistoryTableClause
    : LP_ HISTORY_TABLE EQ_ tableName (COMMA_ DATA_CONSISTENCY_CHECK EQ_ onOffOption)? RP_
    ;

ifExists
    : IF EXISTS
    ;

createDatabaseClause
    : (CONTAINMENT EQ_ (NONE | PARTIAL))? fileDefinitionClause? (COLLATE ignoredIdentifier)? (WITH databaseOption (COMMA_ databaseOption)*)?
    ;

fileDefinitionClause
    : ON PRIMARY? fileSpec (COMMA_ fileSpec)* (COMMA_ databaseFileGroup)* databaseLogOns
    ;

databaseOption
    : FILESTREAM fileStreamOption (COMMA_ fileStreamOption)*
    | DEFAULT_FULLTEXT_LANGUAGE EQ_ ignoredIdentifier
    | DEFAULT_LANGUAGE EQ_ ignoredIdentifier
    | NESTED_TRIGGERS EQ_ (OFF | ON)
    | TRANSFORM_NOISE_WORDS EQ_ (OFF | ON)
    | TWO_DIGIT_YEAR_CUTOFF EQ_ ignoredIdentifier
    | DB_CHAINING (OFF | ON)
    | TRUSTWORTHY (OFF | ON)
    | PERSISTENT_LOG_BUFFER EQ_ ON (DIRECTORY_NAME EQ_ ignoredIdentifier)
    ;

fileStreamOption
    : NON_TRANSACTED_ACCESS EQ_ ( OFF | READ_ONLY | FULL )
    | DIRECTORY_NAME EQ_ ignoredIdentifier
    ;

fileSpec
    : LP_ NAME EQ_ ignoredIdentifier COMMA_ FILENAME EQ_ STRING_ databaseFileSpecOption RP_
    ;

databaseFileSpecOption
    : (COMMA_ SIZE EQ_ numberLiterals (KB | MB | GB | TB)?)?
    (COMMA_ MAXSIZE EQ_ (numberLiterals (KB | MB | GB | TB)? | UNLIMITED))?
    (COMMA_ FILEGROWTH EQ_ numberLiterals (KB | MB | GB | TB | MOD_)?)?
    ;

databaseFileGroup
    : FILEGROUP ignoredIdentifier databaseFileGroupContains? fileSpec (COMMA_ fileSpec)*
    ;

databaseFileGroupContains
    : (CONTAINS FILESTREAM)? DEFAULT? | CONTAINS MEMORY_OPTIMIZED_DATA
    ;

databaseLogOns
    : (LOG ON fileSpec (COMMA_ fileSpec)*)?
    ;

declareVariable
    : DECLARE (variable (COMMA_ variable)* | tableVariable)
    ;

variable
    : variableName AS? dataType (EQ_ simpleExpr)?
    | variableName CURSOR
    ;

tableVariable
    : variableName AS? variTableTypeDefinition
    ;

variTableTypeDefinition
    : TABLE LP_ tableVariableClause (COMMA_ tableVariableClause)* RP_
    ;

tableVariableClause
    : variableTableColumnDefinition | variableTableConstraint
    ;

variableTableColumnDefinition
    : columnName (dataTypeName | AS expr) (COLLATE collationName)? ((DEFAULT expr)? | IDENTITY (LP_ NUMBER_ COMMA_ NUMBER_ RP_)?) ROWGUIDCOL? variableTableColumnConstraint
    ;

variableTableColumnConstraint
    : (NULL | NOT NULL)?
    | (PRIMARY KEY | UNIQUE)?
    | CHECK LP_ expr RP_
    | WITH indexOption
    ;

variableTableConstraint
    : (PRIMARY KEY | UNIQUE) LP_ columnName (COMMA_ columnName)* RP_
    | CHECK expr
    ;

setVariable
    : SET variableName setVariableClause
    ;

setVariableClause
    : (DOT_ identifier)? EQ_ (expr | identifier DOT_ identifier | NCHAR_TEXT)
    | compoundOperation expr
    | EQ_ cursorVariable
    | EQ_ LP_ select RP_
    ;

cursorVariable
    : variableName
    | CURSOR cursorClause FOR select (FOR (READ_ONLY | UPDATE (OF name (COMMA_ name)*)))
    ;

cursorClause
    : (FORWARD_ONLY | SCROLL)? (STATIC | KEYSET | DYNAMIC | FAST_FORWARD)? (READ_ONLY | SCROLL_LOCKS | OPTIMISTIC)? (TYPE_WARNING)?
    ;

compoundOperation
    : PLUS_ EQ_
    | MINUS_ EQ_
    | ASTERISK_ EQ_
    | SLASH_ EQ_
    | MOD_ EQ_
    | AMPERSAND_ EQ_
    | CARET_ EQ_
    | VERTICAL_BAR_ EQ_
    ;

funcParameters
    : LP_ (variableName AS? (owner DOT_)? dataType (EQ_ ignoredIdentifier)? READONLY?)* RP_
    ;

funcReturns
    : funcScalarReturn | funcInlineReturn | funcMutiReturn
    ;

funcMutiReturn
    : RETURNS variableName TABLE createTableDefinitions (WITH functionOption (COMMA_ functionOption)*)? AS? BEGIN compoundStatement RETURN END
    ;

funcInlineReturn
    : RETURNS TABLE (WITH functionOption (COMMA_ functionOption)*)? AS? RETURN LP_? select RP_?
    ;

funcScalarReturn
    : RETURNS dataType (WITH functionOption (COMMA_ functionOption)*)? AS? BEGIN compoundStatement RETURN expr SEMI_ END
    ;

tableTypeDefinition
    : (columnDefinition columnConstraint | computedColumnDefinition) tableConstraint*
    ;

compoundStatement
    : validStatement*
    ;

functionOption
    : ENCRYPTION?
    | SCHEMABINDING?
    | (RETURNS NULL ON NULL INPUT | CALLED ON NULL INPUT)?
    | executeAsClause?
    | (INLINE EQ_ ( ON | OFF ))?
    ;

validStatement
    : (createTable | alterTable | dropTable | truncateTable | insert
    | update | delete | select | setVariable | declareVariable) SEMI_?
    ;

procParameters
    : (procParameter (COMMA_ procParameter)*)?
    ;

procParameter
    : variable VARYING? (EQ_ literals)? (OUT | OUTPUT | READONLY)?
    ;

createOrAlterProcClause
    : withCreateProcOption? (FOR REPLICATION)? AS procAsClause
    ;

withCreateProcOption
    : WITH (procOption (COMMA_ procOption)*)?
    ;

procOption
    : ENCRYPTION
    | RECOMPILE
    | executeAsClause
    | NATIVE_COMPILATION
    | SCHEMABINDING
    ;

procAsClause
    : BEGIN? compoundStatement END?
    | EXTERNAL NAME (owner DOT_)? (owner DOT_)? name
    | BEGIN ATOMIC WITH procSetOption (COMMA_ procSetOption)* compoundStatement END?
    ;

procSetOption
    : LANGUAGE EQ_ stringLiterals
    | TRANSACTION ISOLATION LEVEL EQ_ ( SNAPSHOT | REPEATABLE READ | SERIALIZABLE )
    | DATEFIRST EQ_ numberLiterals
    | DATEFORMAT EQ_ stringLiterals
    | DELAYED_DURABILITY EQ_ (OFF | ON )
    ;

createOrAlterViewClause
    : (WITH viewAttribute (COMMA_ viewAttribute)*)? AS withCommonTableExpr? select (WITH CHECK OPTION)?
    ;

viewAttribute
    : ENCRYPTION
    | SCHEMABINDING
    | VIEW_METADATA
    ;

withCommonTableExpr
    : WITH commonTableExpr (COMMA_ commonTableExpr)*
    ;

commonTableExpr
    : name (LP_ columnName (COMMA_ columnName)* RP_)? AS LP_ select RP_
    ;

createTriggerClause
    : (WITH dmlTriggerOption COMMA_ dmlTriggerOption)? (FOR | AFTER | INSTEAD OF)
    INSERT? COMMA_? UPDATE? COMMA_? DELETE? COMMA_? (WITH APPEND)? (NOT FOR REPLICATION)?
    AS (compoundStatement | EXTERNAL NAME methodSpecifier)
    ;

dmlTriggerOption
    : ENCRYPTION | executeAsClause | NATIVE_COMPILATION | SCHEMABINDING |
    ;

methodSpecifier
    : name DOT_ name DOT_ name
    ;

triggerTarget
    : tableName | viewName | ALL SERVER | DATABASE
    ;

createOrAlterSequenceClause
    : AS dataType
    | (START | RESTART) WITH expr
    | INCREMENT BY expr
    | MINVALUE expr? | NO MINVALUE
    | MAXVALUE expr? | NO MAXVALUE
    | CACHE expr | NO CACHE
    | NO? CYCLE
    ;

createIndexClause
    : (INCLUDE columnNamesWithSort)? (WHERE filterPredicate)? (WITH LP_ relationalIndexOption (COMMA_ relationalIndexOption)* RP_)?
    (ON (schemaName LP_ columnName RP_ | name))? (FILESTREAM_ON (name | stringLiterals))?
    ;

filterPredicate
    : conjunct (AND conjunct)*
    ;

conjunct
    : columnName IN LP_ expr (COMMA_ expr)* RP_
    | columnName comparisonOperator expr
    ;

alterIndexClause
    : REBUILD (PARTITION EQ_ (ALL | expr))? (WITH LP_ relationalIndexOption (COMMA_ relationalIndexOption)* RP_)?
    | DISABLE
    | REORGANIZE (PARTITION EQ_ expr)? (WITH LP_ reorganizeOption RP_)?
    | SET LP_ setIndexOption (COMMA_ setIndexOption) RP_
    | RESUME (WITH LP_ resumableIndexOptions (COMMA_ resumableIndexOptions)* RP_)?
    | PAUSE
    | ABORT
    ;

relationalIndexOption
    : PAD_INDEX EQ_ (ON | OFF)
    | FILLFACTOR EQ_ expr
    | SORT_IN_TEMPDB EQ_ (ON | OFF)
    | IGNORE_DUP_KEY EQ_ (ON | OFF)
    | STATISTICS_NORECOMPUTE EQ_ (ON | OFF)
    | STATISTICS_INCREMENTAL EQ_ (ON | OFF)
    | DROP_EXISTING EQ_ (ON | OFF)
    | ONLINE EQ_ (ON lowPriorityLockWait? | OFF)
    | RESUMABLE EQ_ (ON | OFF)
    | MAX_DURATION EQ_ expr MINUTES?
    | ALLOW_ROW_LOCKS EQ_ (ON | OFF)
    | ALLOW_PAGE_LOCKS EQ_ (ON | OFF)
    | OPTIMIZE_FOR_SEQUENTIAL_KEY EQ_ (ON | OFF)
    | MAXDOP EQ_ expr
    | DATA_COMPRESSION EQ_ (NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE) (ON PARTITIONS LP_ partitionNumberRange (COMMA_ partitionNumberRange)*)?
    ;

partitionNumberRange
    : expr (TO expr)?
    ;

reorganizeOption
    : LOB_COMPACTION EQ_ (ON | OFF)
    | COMPRESS_ALL_ROW_GROUPS EQ_ (ON | OFF)
    ;

setIndexOption
    : ALLOW_ROW_LOCKS EQ_ (ON | OFF)
    | ALLOW_PAGE_LOCKS EQ_ (ON | OFF)
    | OPTIMIZE_FOR_SEQUENTIAL_KEY EQ_ (ON | OFF)
    | IGNORE_DUP_KEY EQ_ (ON | OFF)
    | STATISTICS_NORECOMPUTE EQ_ (ON | OFF)
    | COMPRESSION_DELAY EQ_ (expr MINUTES?)
    ;

resumableIndexOptions
    : MAXDOP EQ_ expr
    | MAX_DURATION EQ_ expr MINUTES?
    | lowPriorityLockWait
    ;

alterDatabaseClause
    : MODIFY NAME EQ_ databaseName
    | COLLATE ignoredIdentifier
    | fileAndFilegroupOptions
    | SET alterDatabaseOptionSpec (COMMA_ alterDatabaseOptionSpec)* (WITH termination)?
    | MODIFY LP_ editionOptions (COMMA_ editionOptions)* RP_
    | MODIFY BACKUP_STORAGE_REDUNDANCY EQ_ STRING_
    | ADD SECONDARY ON SERVER ignoredIdentifier (WITH addSecondaryOption (COMMA_ addSecondaryOption)*)?
    | FAILOVER
    | FORCE_FAILOVER_ALLOW_DATA_LOSS
    ;

addSecondaryOption
    : ALLOW_CONNECTIONS EQ_ (ALL | NO)
    | SERVICE_OBJECTIVE EQ_ (serviceObjective | DATABASE_NAME EQ_ databaseName | SECONDARY_TYPE = (GEO | NAMED))
    ;

editionOptions
    : MAXSIZE EQ_ NUMBER_ (MB | GB)
    | EDITION EQ_ STRING_
    | SERVICE_OBJECTIVE EQ_ (STRING_ | serviceObjective)
    ;

serviceObjective
    : STRING_ | ELASTIC_POOL LP_ ignoredIdentifier EQ_ STRING_ RP_
    ;

alterDatabaseOptionSpec
    : acceleratedDatabaseRecovery
    | autoOption
    | automaticTuningOption
    | changeTrackingOption
    | CONTAINMENT EQ_ (NONE | PARTIAL)
    | cursorOption
    | DATE_CORRELATION_OPTIMIZATION (ON | OFF)
    | ENCRYPTION (ON | OFF | SUSPEND | RESUME)
    | (ONLINE | OFFLINE | EMERGENCY)
    | (READ_ONLY | READ_WRITE)
    | (SINGLE_USER | RESTRICTED_USER | MULTI_USER)
    | DELAYED_DURABILITY EQ_ (DISABLED | ALLOWED | FORCED)
    | externalAccessOption
    | FILESTREAM LP_ fileStreamOption RP_
    | ALTER DATABASE SET HADR
    | MIXED_PAGE_ALLOCATION (OFF | ON)
    | PARAMETERIZATION (SIMPLE | FORCED)
    | queryStoreOptions
    | recoveryOption
    | serviceBrokerOption
    | snapshotOption
    | sqlOption
    | targetRecoveryTimeOption
    | termination
    | TEMPORAL_HISTORY_RETENTION (ON | OFF)
    | DATA_RETENTION (ON | OFF)
    ;

fileAndFilegroupOptions
    : addOrModifyFiles
    | fileSpec
    | addOrModifyFilegroups
    | filegroupUpdatabilityOption
    ;

addOrModifyFilegroups
    : ADD FILEGROUP ignoredIdentifier (CONTAINS FILESTREAM | CONTAINS MEMORY_OPTIMIZED_DATA)?
    | REMOVE FILEGROUP ignoredIdentifier
    | MODIFY FILEGROUP ignoredIdentifier filegroupUpdatabilityOption
    | DEFAULT
    | NAME EQ_ ignoredIdentifier
    | (AUTOGROW_SINGLE_FILE | AUTOGROW_ALL_FILES)
    ;

filegroupUpdatabilityOption
    : (READONLY | READWRITE) | (READ_ONLY | READ_WRITE)
    ;

addOrModifyFiles
    : ADD FILE fileSpec (COMMA_ fileSpec)* (TO FILEGROUP ignoredIdentifier)?
    | ADD LOG FILE fileSpec (COMMA_ fileSpec)*
    | REMOVE FILE STRING_
    | MODIFY FILE fileSpec
    ;

acceleratedDatabaseRecovery
    : ACCELERATED_DATABASE_RECOVERY EQ_ (ON | OFF) (LP_ PERSISTENT_VERSION_STORE_FILEGROUP EQ_ ignoredIdentifier RP_)?
    ;

autoOption
    : AUTO_CLOSE (ON | OFF)
    | AUTO_CREATE_STATISTICS (OFF | ON (LP_ INCREMENTAL EQ_ (ON | OFF) RP_ )?)
    | AUTO_SHRINK (ON | OFF)
    | AUTO_UPDATE_STATISTICS (ON | OFF)
    | AUTO_UPDATE_STATISTICS_ASYNC (ON | OFF)
    ;

automaticTuningOption
    : AUTOMATIC_TUNING LP_ FORCE_LAST_GOOD_PLAN EQ_ (ON | OFF) RP_
    ;

changeTrackingOption
    : CHANGE_TRACKING (EQ_ OFF | (EQ_ ON)? (LP_ changeTrackingOptionList (COMMA_ changeTrackingOptionList)* RP_)?)
    ;

changeTrackingOptionList
    : AUTO_CLEANUP EQ_ (ON | OFF)
    | CHANGE_RETENTION EQ_ NUMBER_ (DAYS | HOURS | MINUTES)
    ;

cursorOption
    : CURSOR_CLOSE_ON_COMMIT (ON | OFF)
    | CURSOR_DEFAULT (LOCAL | GLOBAL)
    ;

externalAccessOption
    : DB_CHAINING (ON | OFF)
    | TRUSTWORTHY (ON | OFF)
    | DEFAULT_FULLTEXT_LANGUAGE EQ_ STRING_
    | DEFAULT_LANGUAGE EQ_ STRING_
    | NESTED_TRIGGERS EQ_ (OFF | ON)
    | TRANSFORM_NOISE_WORDS EQ_ (OFF | ON)
    | TWO_DIGIT_YEAR_CUTOFF EQ_ NUMBER_
    ;

queryStoreOptions
    : QUERY_STORE (EQ_ OFF | (EQ_ ON)? (LP_ queryStoreOptionList (COMMA_ queryStoreOptionList)* RP_)?)
    ;

queryStoreOptionList
    : OPERATION_MODE EQ_ (READ_WRITE | READ_ONLY)
    | CLEANUP_POLICY EQ_ LP_ STALE_QUERY_THRESHOLD_DAYS EQ_ NUMBER_ RP_
    | DATA_FLUSH_INTERVAL_SECONDS EQ_ NUMBER_
    | MAX_STORAGE_SIZE_MB EQ_ NUMBER_
    | INTERVAL_LENGTH_MINUTES EQ_ NUMBER_
    | SIZE_BASED_CLEANUP_MODE EQ_ (AUTO | OFF)
    | QUERY_CAPTURE_MODE EQ_ (ALL | AUTO | CUSTOM | NONE)
    | MAX_PLANS_PER_QUERY EQ_ NUMBER_
    | WAIT_STATS_CAPTURE_MODE EQ_ (ON | OFF)
    | QUERY_CAPTURE_POLICY EQ_ LP_ queryCapturePolicyOptionList (COMMA_ queryCapturePolicyOptionList)* RP_
    ;

queryCapturePolicyOptionList
    : STALE_CAPTURE_POLICY_THRESHOLD EQ_ NUMBER_ (DAYS | HOURS)
    | EXECUTION_COUNT EQ_ NUMBER_
    | TOTAL_COMPILE_CPU_TIME_MS EQ_ NUMBER_
    | TOTAL_EXECUTION_CPU_TIME_MS EQ_ NUMBER_
    ;

recoveryOption
    : RECOVERY (FULL | BULK_LOGGED | SIMPLE)
    | TORN_PAGE_DETECTION (ON | OFF)
    | PAGE_VERIFY (CHECKSUM | TORN_PAGE_DETECTION | NONE)
    ;

sqlOption
    : ANSI_NULL_DEFAULT (ON | OFF)
    | ANSI_NULLS (ON | OFF)
    | ANSI_PADDING (ON | OFF)
    | ANSI_WARNINGS (ON | OFF)
    | ARITHABORT (ON | OFF)
    | COMPATIBILITY_LEVEL EQ_ NUMBER_
    | CONCAT_NULL_YIELDS_NULL (ON | OFF)
    | NUMERIC_ROUNDABORT (ON | OFF)
    | QUOTED_IDENTIFIER (ON | OFF)
    | RECURSIVE_TRIGGERS (ON | OFF)
    ;

snapshotOption
    : ALLOW_SNAPSHOT_ISOLATION (ON | OFF)
    | READ_COMMITTED_SNAPSHOT (ON | OFF)
    | MEMORY_OPTIMIZED_ELEVATE_TO_SNAPSHOT EQ_ (ON | OFF)
    ;

serviceBrokerOption
    : ENABLE_BROKER
    | DISABLE_BROKER
    | NEW_BROKER
    | ERROR_BROKER_CONVERSATIONS
    | HONOR_BROKER_PRIORITY (ON | OFF)
    ;

targetRecoveryTimeOption
    : TARGET_RECOVERY_TIME EQ_ NUMBER_ (SECONDS | MINUTES)
    ;

termination
    : ROLLBACK AFTER NUMBER_ SECONDS?
    | ROLLBACK IMMEDIATE
    | NO_WAIT
    ;

createServiceClause
    : LP_ contractName (COMMA_ contractName)* RP_
    ;

alterServiceClause
    : LP_ alterServiceOptArg (COMMA_ alterServiceOptArg)* RP_
    ;

alterServiceOptArg
    : ADD CONTRACT contractName
    | DROP CONTRACT contractName
    ;

schemaNameClause
    : schemaName
    | AUTHORIZATION ignoredIdentifier
    | schemaName AUTHORIZATION ignoredIdentifier
    ;

schemaElement
    : createTable | createView | grant | revoke | deny
    ;

createTableAsSelectClause
    : createTableAsSelect | createRemoteTableAsSelect
    ;

createTableAsSelect
    : CREATE TABLE tableName columnNames? withDistributionOption AS select optionQueryHintClause
    ;

createRemoteTableAsSelect
    : CREATE REMOTE TABLE tableName AT LP_ stringLiterals RP_ (WITH LP_ BATCH_SIZE EQ_ INT_NUM_ RP_)? AS select
    ;

withDistributionOption
    : WITH LP_ distributionOption (COMMA_ tableOption (COMMA_ tableOption)*)? RP_
    ;

optionQueryHintClause
    : (OPTION LP_ queryHint (COMMA_ queryHint)* RP_)?
    ;
