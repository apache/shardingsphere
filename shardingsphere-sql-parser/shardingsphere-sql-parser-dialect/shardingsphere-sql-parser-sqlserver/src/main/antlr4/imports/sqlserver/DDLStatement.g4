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

import Symbol, Keyword, SQLServerKeyword, Literals, BaseRule;

createTable
    : CREATE TABLE tableName fileTableClause_ createDefinitionClause
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX indexName ON tableName columnNameWithSort
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause (COMMA_ alterDefinitionClause)*
    ;

alterIndex
    : ALTER INDEX (indexName | ALL) ON tableName
    ;

dropTable
    : DROP TABLE ifExist_? tableNames
    ;

dropIndex
    : DROP INDEX ifExist_? indexName ON tableName
    ;

truncateTable
    : TRUNCATE TABLE tableName
    ;

fileTableClause_
    : (AS FILETABLE)?
    ;

createDefinitionClause
    : createTableDefinitions partitionScheme_ fileGroup_
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
    | (CONSTRAINT ignoredIdentifier_)? DEFAULT expr
    | IDENTITY (LP_ NUMBER_ COMMA_ NUMBER_ RP_)?
    | NOT FOR REPLICATION
    | GENERATED ALWAYS AS ROW (START | END) HIDDEN_?
    | NOT? NULL
    | ROWGUIDCOL 
    | ENCRYPTED WITH encryptedOptions_
    | columnConstraint (COMMA_ columnConstraint)*
    | columnIndex
    ;

encryptedOptions_
    : LP_ COLUMN_ENCRYPTION_KEY EQ_ ignoredIdentifier_ COMMA_ ENCRYPTION_TYPE EQ_ (DETERMINISTIC | RANDOMIZED) COMMA_ ALGORITHM EQ_ STRING_ RP_
    ;

columnConstraint
    : (CONSTRAINT ignoredIdentifier_)? (primaryKeyConstraint | columnForeignKeyConstraint | checkConstraint)
    ;

primaryKeyConstraint
    : (primaryKey | UNIQUE) (diskTablePrimaryKeyConstraintOption | memoryTablePrimaryKeyConstraintOption)
    ;

diskTablePrimaryKeyConstraintOption
    : clusterOption_? primaryKeyWithClause? primaryKeyOnClause?
    ;

clusterOption_
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
    : ON ignoredIdentifier_
    ;

onString
    : ON STRING_
    ;

memoryTablePrimaryKeyConstraintOption
    : CLUSTERED withBucket?
    ;

withBucket
    : WITH LP_ BUCKET_COUNT EQ_ NUMBER_ RP_
    ;

columnForeignKeyConstraint
    : (FOREIGN KEY)? REFERENCES tableName LP_ columnName RP_ foreignKeyOnAction*
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
    : INDEX indexName clusterOption_? withIndexOption_? indexOnClause? fileStreamOn_?
    ;

withIndexOption_
    : WITH LP_ indexOption (COMMA_ indexOption)* RP_
    ;

indexOnClause
    : onSchemaColumn | onFileGroup | onDefault
    ;

onDefault
    : ON DEFAULT
    ;

fileStreamOn_
    : FILESTREAM_ON (ignoredIdentifier_ | schemaName | STRING_)
    ;

columnConstraints
    : (columnConstraint(COMMA_ columnConstraint)*)?
    ;

computedColumnDefinition
    : columnName AS expr (PERSISTED(NOT NULL)?)? columnConstraint?
    ;

columnSetDefinition 
    : ignoredIdentifier_ IDENTIFIER_ COLUMN_SET FOR ALL_SPARSE_COLUMNS
    ;

tableConstraint 
    : (CONSTRAINT ignoredIdentifier_)? (tablePrimaryConstraint | tableForeignKeyConstraint | checkConstraint)
    ;

tablePrimaryConstraint
    : primaryKeyUnique (diskTablePrimaryConstraintOption | memoryTablePrimaryConstraintOption)
    ;

primaryKeyUnique
    : primaryKey | UNIQUE
    ;

diskTablePrimaryConstraintOption
    : clusterOption_? columnNames primaryKeyWithClause? primaryKeyOnClause?
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
    : INDEX indexName indexNameOption_ (WITH indexOptions_)? indexOnClause? fileStreamOn_?
    ;

indexNameOption_
    : clusterOption_? columnNames | CLUSTERED COLUMNSTORE | NONCLUSTERED? COLUMNSTORE columnNames
    ;

indexOptions_
    : LP_ indexOption (COMMA_ indexOption)* RP_
    ;

periodClause
    : PERIOD FOR SYSTEM_TIME LP_ columnName COMMA_ columnName RP_
    ;

partitionScheme_
    : (ON (schemaName LP_ columnName RP_ | ignoredIdentifier_ | STRING_))?
    ;

fileGroup_
    : (TEXTIMAGE_ON (ignoredIdentifier_ | STRING_))? ((FILESTREAM_ON (schemaName) | ignoredIdentifier_ STRING_))? (WITH tableOptions)?
    ;

tableOptions
    : LP_ tableOption (COMMA_ tableOption)* RP_
    ;

tableOption
    : DATA_COMPRESSION EQ_ (NONE | ROW | PAGE) (ON PARTITIONS LP_ partitionExpressions RP_)?
    | FILETABLE_DIRECTORY EQ_ ignoredIdentifier_ 
    | FILETABLE_COLLATE_FILENAME EQ_ (collationName | DATABASE_DEAULT)
    | FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME EQ_ ignoredIdentifier_
    | FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME EQ_ ignoredIdentifier_
    | FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME EQ_ ignoredIdentifier_
    | SYSTEM_VERSIONING EQ_ ON onHistoryTableClause?
    | REMOTE_DATA_ARCHIVE EQ_ (ON tableStretchOptions? | OFF migrationState_)
    | tableOperationOption
    | distributionOption
    | dataWareHouseTableOption
    ;

tableStretchOptions
    : LP_ tableStretchOptions (COMMA_ tableStretchOptions)* RP_
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
    : CLUSTERED COLUMNSTORE INDEX | HEAP | dataWareHousePartitionOption
    ;

dataWareHousePartitionOption
    : (PARTITION LP_ columnName RANGE (LEFT | RIGHT)? FOR VALUES LP_ simpleExpr (COMMA_ simpleExpr)* RP_ RP_)
    ;

createIndexSpecification_
    : UNIQUE? clusterOption_?
    ;

alterDefinitionClause
    : addColumnSpecification | modifyColumnSpecification | alterDrop | alterCheckConstraint | alterTrigger | alterSwitch | alterSet | alterTableOption | REBUILD
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
    : (CONSTRAINT ignoredIdentifier_)? DEFAULT simpleExpr FOR columnName
    ;

columnNameWithSortsWithParen
    : LP_ columnNameWithSort (COMMA_ columnNameWithSort)* RP_
    ;

columnNameWithSort
    : columnName (ASC | DESC)?
    ;

generatedColumnNamesClause
    : generatedColumnNameClause COMMA_ periodClause | periodClause COMMA_ generatedColumnNameClause
    ;

generatedColumnNameClause
    : generatedColumnName DEFAULT simpleExpr (WITH VALUES)? COMMA_ generatedColumnName
    ;

generatedColumnName
    : columnName dataTypeName GENERATED ALWAYS AS ROW (START | END)? HIDDEN_? (NOT NULL)? (CONSTRAINT ignoredIdentifier_)?
    ;

alterDrop
    : DROP (alterTableDropConstraint | dropColumnSpecification | dropIndexSpecification | PERIOD FOR SYSTEM_TIME)
    ;

alterTableDropConstraint
    : CONSTRAINT? ifExist_? dropConstraintName (COMMA_ dropConstraintName)*
    ;

dropConstraintName
    : ignoredIdentifier_ dropConstraintWithClause?
    ;

dropConstraintWithClause
    : WITH LP_ dropConstraintOption (COMMA_ dropConstraintOption)* RP_
    ;

dropConstraintOption
    : (MAXDOP EQ_ NUMBER_ | ONLINE EQ_ onOffOption_ | MOVE TO (schemaName LP_ columnName RP_ | ignoredIdentifier_ | STRING_))
    ;

onOffOption_
    : ON | OFF
    ;

dropColumnSpecification
    : COLUMN ifExist_? columnName (COMMA_ columnName)*
    ;

dropIndexSpecification
    : INDEX ifExist_? indexName (COMMA_ indexName)*
    ;

alterCheckConstraint 
    : WITH? (CHECK | NOCHECK) CONSTRAINT (ALL | ignoredIdentifiers_)
    ;

alterTrigger 
    : (ENABLE| DISABLE) TRIGGER (ALL | ignoredIdentifiers_)
    ;

alterSwitch
    : SWITCH (PARTITION expr)? TO tableName (PARTITION expr)? (WITH LP_ lowPriorityLockWait RP_)?
    ;

alterSet
    : SET LP_ (setFileStreamClause | setSystemVersionClause) RP_ 
    ;

setFileStreamClause
    : FILESTREAM_ON EQ_ (schemaName | ignoredIdentifier_ | STRING_)
    ;

setSystemVersionClause
    : SYSTEM_VERSIONING EQ_ (OFF | ON alterSetOnClause?)
    ;

alterSetOnClause
    : LP_ (HISTORY_TABLE EQ_ tableName)? dataConsistencyCheckClause_? historyRetentionPeriodClause_? RP_
    ;

dataConsistencyCheckClause_
    : COMMA_? DATA_CONSISTENCY_CHECK EQ_ onOffOption_
    ;

historyRetentionPeriodClause_
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
    : NONCLUSTERED (hashWithBucket | columnNameWithSortsWithParen alterTableIndexOnClause?) 
    ;

alterTableIndexOnClause
    : ON ignoredIdentifier_ | DEFAULT
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
    : LP_ HISTORY_TABLE EQ_ tableName (COMMA_ DATA_CONSISTENCY_CHECK EQ_ onOffOption_)? RP_
    ;

ifExist_
    : IF EXISTS
    ;
