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
    : CREATE REMOTE? (DATABASE | SCHEMA) ifNotExists? identifier (COMMENT string_)? (LOCATION string_)? (MANAGEDLOCATION string_)? (WITH DBPROPERTIES LP_ dbProperties RP_)?
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
    : createTableCommonClause createDefinitionClause? tableComment? partitionedBy? clusteredBy? skewedBy? rowFormat? storedAs? storageLocation? tblProperties? (AS select)?
    | createTableCommonClause LIKE existingTableName storageLocation?
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
    | alterTableCommonClause partitionSpec? SET SERDE string_ (WITH SERDEPROPERTIES LP_ propertyList RP_)?
    | alterTableCommonClause partitionSpec? SET SERDEPROPERTIES LP_ propertyList RP_
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
    ;

createView
    : CREATE VIEW ifNotExists? viewNameWithDb (LP_ columnName (COMMENT string_)? (COMMA_ columnName (COMMENT string_)?)* RP_)? (COMMENT string_)? tblProperties? AS select
    ;

dropView
    : DROP VIEW ifExists? viewNameWithDb
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
    : columnName dataTypeClause columnConstraintSpecification* (COMMENT string_)?
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
    : identifier COLON_ dataTypeClause (COMMENT string_)?
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
    : PRIMARY KEY LP_ columnNames RP_ constraintAttributes?
    | UNIQUE LP_ columnNames RP_ constraintAttributes?
    | CONSTRAINT identifier FOREIGN KEY LP_ columnNames RP_ REFERENCES tableName LP_ columnNames RP_ constraintAttributes?
    | CONSTRAINT identifier CHECK LP_ expr RP_ constraintAttributes?
    ;

tableComment
    : COMMENT string_
    ;

partitionedBy
    : PARTITIONED BY LP_ partitionColumn (COMMA_ partitionColumn)* RP_
    ;

partitionColumn
    : columnName dataTypeClause (COMMENT string_)?
    ;

clusteredBy
    : CLUSTERED BY LP_ columnNames RP_ (SORTED BY LP_ sortedByItem (COMMA_ sortedByItem)* RP_)? INTO NUMBER_ BUCKETS
    ;

sortedByItem
    : columnName (ASC | DESC)?
    ;

skewedBy
    : SKEWED BY LP_ columnNames RP_ ON LP_ skewedValueList RP_ (STORED AS DIRECTORIES)?
    ;

skewedValueList
    : skewedValue (COMMA_ skewedValue)*
    ;

skewedValue
    : LP_ literals (COMMA_ literals)* RP_
    | literals
    ;

rowFormat
    : ROW FORMAT rowFormatType
    ;

rowFormatType
    : DELIMITED rowFormatDelimited
    | SERDE string_ (WITH SERDEPROPERTIES LP_ propertyList RP_)?
    ;

rowFormatDelimited
    : (COLUMNS TERMINATED BY string_ (ESCAPED BY string_)?)?
      (COLLECTION ITEMS TERMINATED BY string_)?
      (MAP KEYS TERMINATED BY string_)?
      (LINES TERMINATED BY string_)?
      (NULL DEFINED AS string_)?
    ;

storedAs
    : STORED AS fileFormat
    | STORED BY string_ (WITH SERDEPROPERTIES LP_ propertyList RP_)?
    ;

fileFormat
    : SEQUENCEFILE
    | TEXTFILE
    | RCFILE
    | ORC
    | PARQUET
    | AVRO
    | JSONFILE
    | INPUTFORMAT string_ OUTPUTFORMAT string_
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
    : TBLPROPERTIES LP_ propertyList RP_
    ;

propertyList
    : property (COMMA_ property)*
    ;

property
    : string_ EQ_ string_
    ;

addConstraint
    : ADD CONSTRAINT constraintName
    ;

changeColumn
    : CHANGE COLUMN constraintName constraintName dataTypeClause CONSTRAINT constraintName
    | CHANGE COLUMN? columnName columnName dataTypeClause (COMMENT string_)? (FIRST | AFTER columnName)? (CASCADE | RESTRICT)?
    ;

alterTableConstrintClause
    : addConstraint PRIMARY KEY LP_ columnNames RP_ DISABLE NOVALIDATE
    | addConstraint FOREIGN KEY LP_ columnNames RP_ REFERENCES tableName LP_ columnNames RP_ DISABLE NOVALIDATE RELY
    | addConstraint UNIQUE LP_ columnNames RP_ DISABLE NOVALIDATE
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
