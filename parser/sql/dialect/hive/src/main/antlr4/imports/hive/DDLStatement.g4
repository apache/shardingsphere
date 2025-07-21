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
    : DROP TABLE ifExists? tableList (PURGE)?
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

createTableSpecification
    : TEMPORARY? EXTERNAL?
    ;

tableNameWithDb
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

tblProperties
    : TBLPROPERTIES LP_ propertyList RP_
    ;

propertyList
    : property (COMMA_ property)*
    ;

property
    : string_ EQ_ string_
    ;
