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

grammar PostgreSQLDDLStatement;

import Symbol, PostgreSQLKeyword, Keyword, Literals, PostgreSQLBase, BaseRule;

createIndex
    : CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName 
    ;

dropIndex
    : DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexName (COMMA_ indexName)*
    ;

alterIndex
    : alterIndexName renameIndexSpecification | alterIndexDependsOnExtension | alterIndexSetTableSpace
    ;

createTable
    : createTableHeader createDefinitions inheritClause?
    ;

alterTable
    : alterTableNameWithAsterisk (alterTableActions | renameColumnSpecification | renameConstraint) | alterTableNameExists renameTableSpecification
    ;

truncateTable
    : TRUNCATE TABLE? ONLY? tableNameParts
    ;

dropTable
    : DROP TABLE (IF EXISTS)? tableName (COMMA_ tableName)*
    ;

alterIndexName
    : ALTER INDEX (IF EXISTS)? indexName
    ;

renameIndexSpecification
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
    : addColumnSpecification
    | dropColumnSpecification
    | modifyColumnSpecification
    | addConstraintSpecification
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

addColumnSpecification
    : ADD COLUMN? (IF NOT EXISTS)? columnDefinition
    ;

dropColumnSpecification
    : DROP COLUMN? (IF EXISTS)? columnName (RESTRICT | CASCADE)?
    ;

modifyColumnSpecification
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
    : IDENTIFIER_ EQ_ simpleExpr
    ;

addConstraintSpecification
    : ADD (tableConstraint (NOT VALID)? | tableConstraintUsingIndex)
    ;

renameColumnSpecification
    : RENAME COLUMN? columnName TO columnName
    ;

renameConstraint
    : RENAME CONSTRAINT ignoredIdentifier_ TO ignoredIdentifier_
    ;

storageParameterWithValue
    : storageParameter EQ_ simpleExpr
    ;

storageParameter
    : IDENTIFIER_
    ;

alterTableNameExists
    : ALTER TABLE (IF EXISTS)? tableName
    ;

renameTableSpecification
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
    | UNIQUE columnNames indexParameters
    | primaryKey columnNames indexParameters
    | FOREIGN KEY columnNames REFERENCES tableName columnNames (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? foreignKeyOnAction*
    ;

excludeElement
    : (columnName | expr) ignoredIdentifier_? (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;
