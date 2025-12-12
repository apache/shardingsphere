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

import BaseRule, DMLStatement;

createDatabase
    : CREATE REMOTE? (DATABASE | SCHEMA) ifNotExists? identifier commentClause? storageLocation? (MANAGEDLOCATION string_)? (WITH DBPROPERTIES LP_ dbProperties RP_)?
    ;

dropDatabase
    : DROP (DATABASE | SCHEMA) ifExists? identifier (RESTRICT | CASCADE)?
    ;

alterDatabase
    : ALTER (DATABASE | SCHEMA) identifier alterDatabaseSpecification_*
    ;

use
    : USE (identifier | DEFAULT)
    ;

createTable
    : createTableCommonClause createDefinitionClause? commentClause? partitionedBy? clusteredBy? skewedBy? rowFormat? storedByIceberg? storedClause? storageLocation? tblProperties? (AS select)?
    | createTableCommonClause LIKE existingTableName storedByIceberg? storageLocation?
    ;

dropTable
    : DROP TABLE ifExists? tableNameWithDb (PURGE)?
    ;

truncateTable
    : TRUNCATE (TABLE)? tableNameWithDb partitionSpec?
    ;

alterTable
    : alterTableCommonClause RENAME TO tableName
    | alterTableCommonClause SET tblProperties
    | alterTableCommonClause partitionSpec? SET SERDE string_ (WITH SERDEPROPERTIES propertyListCommonClause)?
    | alterTableCommonClause partitionSpec? SET SERDEPROPERTIES propertyListCommonClause
    | alterTableCommonClause partitionSpec? UNSET SERDEPROPERTIES LP_ string_ RP_
    | alterTableCommonClause clusteredBy
    | alterTableCommonClause skewedBy
    | alterTableCommonClause NOT SKEWED
    | alterTableCommonClause NOT STORED AS DIRECTORIES
    | alterTableCommonClause SET SKEWED storageLocation
    | alterTableCommonClause SET SKEWED skewedLocationClause
    | alterTableCommonClause alterTableConstrintClause
    | alterTableCommonClause partitionSpec? SET FILEFORMAT fileFormat
    | alterTableCommonClause partitionSpec? SET storageLocation
    | alterTableCommonClause TOUCH partitionSpec?
    | alterTableCommonClause partitionSpec? COMPACT string_ (AND WAIT)? clusteredIntoClause? orderByClause? poolClause? tblpropertiesClause?
    | alterTableCommonClause partitionSpec? CONCATENATE
    | alterTableCommonClause partitionSpec? UPDATE COLUMNS
    | alterTableCommonClause ADD ifNotExists? partitionSpec storageLocation? (COMMA_ partitionSpec storageLocation?)*
    | alterTableCommonClause DROP ifExists? partitionSpec (COMMA_ partitionSpec)* (IGNORE PROTECTION)? PURGE?
    | alterTableCommonClause partitionSpec RENAME TO partitionSpec
    | alterTableCommonClause EXCHANGE partitionSpec WITH TABLE tableName
    | alterTableCommonClause ARCHIVE partitionSpec
    | alterTableCommonClause UNARCHIVE partitionSpec
    | alterTableCommonClause RECOVER PARTITIONS
    | alterTableCommonClause partitionSpec? changeColumn
    | alterTableCommonClause partitionSpec? addColumns
    | alterTableCommonClause partitionSpec? replaceColumns
    | alterTableCommonClause createBranch
    | alterTableCommonClause createTag
    | alterTableCommonClause dropBranch
    | alterTableCommonClause dropTag
    ;

createView
    : CREATE VIEW ifNotExists? viewNameWithDb (LP_ columnName commentClause? (COMMA_ columnName commentClause?)* RP_)? commentClause? tblProperties? AS select
    ;

dropView
    : DROP VIEW ifExists? viewNameWithDb
    ;

alterView
    : alterViewCommonClause SET tblProperties
    | alterViewCommonClause AS select
    ;

createMaterializedView
    : CREATE MATERIALIZED VIEW ifNotExists? viewNameWithDb materializedViewOptions? AS select
    ;

dropMaterializedView
    : DROP MATERIALIZED VIEW viewNameWithDb
    ;

alterMaterializedView
    : ALTER MATERIALIZED VIEW viewNameWithDb (ENABLE | DISABLE) REWRITE
    ;

createIndex
    : CREATE INDEX indexName ON TABLE tableNameWithDb columnNamesCommonClause AS indexType createIndexOptions?
    ;

dropIndex
    : DROP INDEX ifExists? indexName ON tableNameWithDb
    ;

alterIndex
    : ALTER INDEX indexName ON tableNameWithDb partitionSpec? REBUILD
    ;

createMacro
    : CREATE TEMPORARY MACRO macroName LP_ macroParameterList? RP_ expr
    ;

dropMacro
    : DROP TEMPORARY MACRO ifExists? macroName
    ;

createFunction
    : CREATE TEMPORARY FUNCTION functionName AS className
    | CREATE FUNCTION functionName AS className createFunctionOptions?
    ;

dropFunction
    : DROP TEMPORARY FUNCTION ifExists? functionName
    | DROP FUNCTION ifExists? functionName
    ;

reloadFunction
    : RELOAD (FUNCTIONS | FUNCTION)
    ;

alterDatabaseSpecification_
    : SET DBPROPERTIES LP_ dbProperties RP_
    | SET OWNER (USER | ROLE) identifier
    | SET LOCATION string_
    | SET MANAGEDLOCATION string_
    ;

ifNotExists
    : IF NOT EXISTS
    ;

dbProperties
    : dbProperty (COMMA_ dbProperty)*
    ;

dbProperty
    : string_ EQ_ string_
    ;

ifExists
    : IF EXISTS
    ;

createTableCommonClause
    : CREATE createTableSpecification? TABLE ifNotExists? tableNameWithDb
    ;

alterTableCommonClause
    : ALTER TABLE tableName
    ;

alterViewCommonClause
    : ALTER VIEW viewNameWithDb
    ;

createTableSpecification
    : TEMPORARY? EXTERNAL?
    ;

tableNameWithDb
    : (identifier DOT_)? identifier
    ;

viewNameWithDb
    : (identifier DOT_)? identifier
    ;

existingTableName
    : (identifier DOT_)? identifier
    ;

createDefinitionClause
    : LP_ columnDefinition (COMMA_ columnDefinition)* (COMMA_ tableConstraint)* RP_
    ;

columnDefinition
    : columnName dataTypeClause columnConstraintSpecification* commentClause?
    ;

columnName
    : identifier
    ;

dataTypeClause
    : primitiveType
    | arrayType
    | mapType
    | structType
    | unionType
    ;

primitiveType
    : TINYINT
    | SMALLINT
    | INT
    | BIGINT
    | BOOLEAN
    | FLOAT
    | DOUBLE (PRECISION)?
    | STRING
    | BINARY
    | TIMESTAMP
    | DECIMAL (LP_ NUMBER_ (COMMA_ NUMBER_)? RP_)?
    | DATE
    | VARCHAR (LP_ NUMBER_ RP_)?
    | CHAR (LP_ NUMBER_ RP_)?
    ;

arrayType
    : ARRAY LT_ dataTypeClause GT_
    ;

mapType
    : MAP LT_ primitiveType COMMA_ dataTypeClause GT_
    ;

structType
    : STRUCT LT_ structField (COMMA_ structField)* GT_
    ;

structField
    : identifier COLON_ dataTypeClause commentClause?
    ;

unionType
    : UNIONTYPE LT_ dataTypeClause (COMMA_ dataTypeClause)* GT_
    ;

columnConstraintSpecification
    : primaryKeyConstraint
    | uniqueConstraint
    | notNullConstraint
    | defaultConstraint
    | checkConstraint
    ;

primaryKeyConstraint
    : PRIMARY KEY constraintAttributes?
    ;

uniqueConstraint
    : UNIQUE constraintAttributes?
    ;

notNullConstraint
    : NOT NULL
    ;

defaultConstraint
    : DEFAULT defaultValue
    ;

defaultValue
    : literals
    | CURRENT_USER LP_ RP_
    | CURRENT_DATE LP_ RP_
    | CURRENT_TIMESTAMP LP_ RP_
    | NULL
    ;

checkConstraint
    : CHECK LP_ expr RP_ constraintAttributes?
    ;

constraintAttributes
    : (ENABLE | DISABLE) NOVALIDATE (RELY | NORELY)?
    ;

tableConstraint
    : (CONSTRAINT constraintName)? tableConstraintOption
    ;

constraintName
    : identifier
    ;

tableName
    : identifier
    ;

tableConstraintOption
    : PRIMARY KEY columnNamesCommonClause constraintAttributes?
    | UNIQUE columnNamesCommonClause constraintAttributes?
    | CONSTRAINT identifier FOREIGN KEY columnNamesCommonClause REFERENCES tableName columnNamesCommonClause constraintAttributes?
    | CONSTRAINT identifier CHECK LP_ expr RP_ constraintAttributes?
    ;

commentClause
    : COMMENT string_
    ;

partitionedBy
    : PARTITIONED BY LP_ partitionColumn (COMMA_ partitionColumn)* RP_
    ;

partitionColumn
    : columnName dataTypeClause commentClause?
    ;

clusteredBy
    : CLUSTERED BY columnNamesCommonClause (SORTED BY LP_ sortedByItem (COMMA_ sortedByItem)* RP_)? INTO NUMBER_ BUCKETS
    ;

sortedByItem
    : columnName (ASC | DESC)?
    ;

skewedBy
    : SKEWED BY columnNamesCommonClause ON LP_ skewedValueList RP_ (STORED AS DIRECTORIES)?
    ;

skewedValueList
    : skewedValue (COMMA_ skewedValue)*
    ;

skewedValue
    : LP_ literals (COMMA_ literals)* RP_
    | literals
    ;

storageLocation
    : LOCATION string_
    ;

skewedLocationClause
    : LOCATION LP_ skewedLocationPair (COMMA_ skewedLocationPair)* RP_
    ;

skewedLocationPair
    : identifier EQ_ string_
    ;

tblProperties
    : TBLPROPERTIES propertyListCommonClause
    ;

storedByIceberg
    : STORED BY ICEBERG
    ;

addConstraint
    : ADD CONSTRAINT constraintName
    ;

changeColumn
    : CHANGE COLUMN constraintName constraintName dataTypeClause CONSTRAINT constraintName
    | CHANGE COLUMN? columnName columnName dataTypeClause commentClause? (FIRST | AFTER columnName)? (CASCADE | RESTRICT)?
    ;

alterTableConstrintClause
    : addConstraint PRIMARY KEY columnNamesCommonClause DISABLE NOVALIDATE
    | addConstraint FOREIGN KEY columnNamesCommonClause REFERENCES tableName columnNamesCommonClause DISABLE NOVALIDATE RELY
    | addConstraint UNIQUE columnNamesCommonClause DISABLE NOVALIDATE
    | changeColumn NOT NULL ENABLE
    | changeColumn DEFAULT string_ ENABLE
    | changeColumn CHECK LP_ expr RP_ ENABLE
    | DROP CONSTRAINT constraintName
    ;

clusteredIntoClause
    : CLUSTERED INTO NUMBER_ BUCKETS
    ;

orderByClause
    : ORDER BY columnNames
    ;

poolClause
    : POOL string_
    ;

tblpropertiesClause
    : WITH OVERWRITE tblProperties
    ;

msckStatement
    : MSCK REPAIR? TABLE tableName msckAction?
    ;

msckAction
    : ADD PARTITIONS
    | DROP PARTITIONS
    | SYNC PARTITIONS
    ;

addColumns
    : ADD COLUMNS LP_ columnDefinition (COMMA_ columnDefinition)* RP_ (CASCADE | RESTRICT)?
    ;

replaceColumns
    : REPLACE COLUMNS LP_ columnDefinition (COMMA_ columnDefinition)* RP_ (CASCADE | RESTRICT)?
    ;

materializedViewOptions
    : materializedViewOption*
    ;

materializedViewOption
    : DISABLE REWRITE
    | commentClause
    | PARTITIONED ON columnNamesCommonClause
    | clusteredOrDistrbutedClause
    | rowFormat
    | storedClause
    | storageLocation
    | tblProperties
    ;

clusteredOrDistrbutedClause
    : CLUSTERED ON columnNamesCommonClause
    | DISTRIBUTED ON columnNamesCommonClause SORTED ON columnNamesCommonClause
    ;

createIndexOptions
    : createIndexOption*
    ;

createIndexOption
    : WITH DEFERRED REBUILD
    | IDXPROPERTIES propertyListCommonClause
    | IN TABLE indexTableName
    | rowFormat? storedClause
    | storedClause
    | storageLocation
    | tblProperties
    | commentClause
    ;

indexType
    : string_
    ;

indexTableName
    : tableNameWithDb
    ;

columnNamesCommonClause
    : LP_ columnNames RP_
    ;

macroParameterList
    : macroParameter (COMMA_ macroParameter)*
    ;

macroParameter
    : columnName dataTypeClause
    ;

className
    : string_
    ;

resourcePath
    : string_
    ;

resourceClause
    : JAR resourcePath
    | FILE resourcePath
    | ARCHIVE resourcePath
    ;

createFunctionOption
    : (COMMA_ resourceClause)*
    ;

createFunctionOptions
    : USING resourceClause createFunctionOption
    ;

createBranch
    : CREATE BRANCH identifier branchCreationTail?
    ;

branchCreationTail
    : systemVersionTail branchSnapshotRetentionTail?
    | systemTimeTail
    | branchTagTail
    ;

systemVersionTail
    : FOR SYSTEM_VERSION AS OF (NUMBER_ | identifier)
    ;

systemTimeTail
    : FOR SYSTEM_TIME AS OF string_
    ;

branchTagTail
    : FOR TAG AS OF identifier
    ;

branchSnapshotRetentionTail
    : WITH SNAPSHOT RETENTION NUMBER_ SNAPSHOTS
    ;

createTag
    : CREATE TAG identifier tagCreationTail?
    ;

tagCreationTail
    : systemVersionTail
    | systemTimeTail
    ;

dropBranch
    : DROP BRANCH ifExists? identifier
    ;

dropTag
    : DROP TAG ifExists? identifier
    ;
