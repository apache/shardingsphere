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

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule;

createTable
    : CREATE createTableSpecification_ TABLE tableNotExistClause_ tableName createDefinitionClause inheritClause_
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX concurrentlyClause_ (indexNotExistClause_ indexName)? ON onlyClause_ tableName 
    ;

alterTable
    : ALTER TABLE tableExistClause_ onlyClause_ tableNameClause alterDefinitionClause
    ;

alterIndex
    : ALTER INDEX indexExistClause_ indexName alterIndexDefinitionClause_
    ;

dropTable
    : DROP TABLE tableExistClause_ tableNames
    ;

dropIndex
    : DROP INDEX concurrentlyClause_ indexExistClause_ indexNames
    ;
    
truncateTable
    : TRUNCATE TABLE? onlyClause_ tableNamesClause
    ;

createTableSpecification_
    : ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)?
    ;

tableNotExistClause_
    : (IF NOT EXISTS)?
    ;

createDefinitionClause
    : LP_ (createDefinition (COMMA_ createDefinition)*)? RP_
    ;

createDefinition
    : columnDefinition | tableConstraint | LIKE tableName likeOption*
    ;

columnDefinition
    : columnName dataType collateClause_? columnConstraint*
    ;

columnConstraint
    : constraintClause? columnConstraintOption constraintOptionalParam
    ;

constraintClause
    : CONSTRAINT ignoredIdentifier_
    ;

columnConstraintOption
    : NOT? NULL
    | checkOption
    | DEFAULT defaultExpr
    | GENERATED (ALWAYS | BY DEFAULT) AS IDENTITY (LP_ sequenceOptions RP_)?
    | UNIQUE indexParameters
    | primaryKey indexParameters
    | REFERENCES tableName columnNames? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (DELETE | UPDATE) action)*
    ;

checkOption
    : CHECK aExpr (NO INHERIT)?
    ;

defaultExpr
    : CURRENT_TIMESTAMP | aExpr
    ;

sequenceOptions
    : sequenceOption+
    ;

sequenceOption
    : START WITH? NUMBER_
    | INCREMENT BY? NUMBER_
    | MAXVALUE NUMBER_
    | NO MAXVALUE
    | MINVALUE NUMBER_
    | NO MINVALUE
    | CYCLE
    | NO CYCLE
    | CACHE NUMBER_
    | OWNED BY
    ;

indexParameters
    : (USING INDEX TABLESPACE ignoredIdentifier_)?
    | INCLUDE columnNames
    | WITH
    ;

action
    : NO ACTION | RESTRICT | CASCADE | SET (NULL | DEFAULT)
    ;

constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED | IMMEDIATE))?
    ;

likeOption
    : (INCLUDING | EXCLUDING) (COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
    ;

tableConstraint
    : constraintClause? tableConstraintOption constraintOptionalParam
    ;

tableConstraintOption
    : checkOption
    | UNIQUE columnNames indexParameters
    | primaryKey columnNames indexParameters
    | EXCLUDE (USING ignoredIdentifier_)?
    | FOREIGN KEY columnNames REFERENCES tableName columnNames? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (DELETE | UPDATE) action)*
    ;

inheritClause_
    : (INHERITS tableNames)?
    ;

createIndexSpecification_
    : UNIQUE?
    ;

concurrentlyClause_
    : CONCURRENTLY?
    ;

indexNotExistClause_
    : (IF NOT EXISTS)?
    ;

onlyClause_
    : ONLY?
    ;

tableExistClause_
    : (IF EXISTS)?
    ;

asteriskClause_
    : ASTERISK_?
    ;

alterDefinitionClause
    : alterTableActions | renameColumnSpecification | renameConstraint | renameTableSpecification_
    ;

alterIndexDefinitionClause_
    : renameIndexSpecification | alterIndexDependsOnExtension | alterIndexSetTableSpace
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

tableNamesClause
    : tableNameClause (COMMA_ tableNameClause)*
    ;

tableNameClause
    : tableName ASTERISK_?
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
    | DROP CONSTRAINT indexExistClause_ ignoredIdentifier_ (RESTRICT | CASCADE)?
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
    | OF dataTypeName
    | NOT OF
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING)
    ;

addColumnSpecification
    : ADD COLUMN? (IF NOT EXISTS)? columnDefinition
    ;

dropColumnSpecification
    : DROP COLUMN? columnExistClause_ columnName (RESTRICT | CASCADE)?
    ;

columnExistClause_
    : (IF EXISTS)?
    ;
    
modifyColumnSpecification
    : modifyColumn (SET DATA)? TYPE dataType collateClause_? (USING aExpr)?
    | modifyColumn SET DEFAULT aExpr
    | modifyColumn DROP DEFAULT
    | modifyColumn (SET | DROP) NOT NULL
    | modifyColumn ADD GENERATED (ALWAYS | (BY DEFAULT)) AS IDENTITY (LP_ sequenceOptions RP_)?
    | modifyColumn alterColumnSetOption alterColumnSetOption*
    | modifyColumn DROP IDENTITY columnExistClause_
    | modifyColumn SET STATISTICS NUMBER_
    | modifyColumn SET LP_ attributeOptions RP_
    | modifyColumn RESET LP_ attributeOptions RP_
    | modifyColumn SET STORAGE (PLAIN | EXTERNAL | EXTENDED | MAIN)
    ;

modifyColumn
    : ALTER COLUMN? columnName
    ;

alterColumnSetOption
    : SET (GENERATED (ALWAYS | BY DEFAULT) | sequenceOption) | RESTART (WITH? NUMBER_)?
    ;

attributeOptions
    : attributeOption (COMMA_ attributeOption)*
    ;

attributeOption
    : IDENTIFIER_ EQ_ aExpr
    ;

addConstraintSpecification
    : ADD (tableConstraint (NOT VALID)? | tableConstraintUsingIndex)
    ;

tableConstraintUsingIndex
    : (CONSTRAINT ignoredIdentifier_)? (UNIQUE | primaryKey) USING INDEX indexName constraintOptionalParam
    ;

storageParameterWithValue
    : storageParameter EQ_ aExpr
    ;

storageParameter
    : IDENTIFIER_
    ;

renameColumnSpecification
    : RENAME COLUMN? columnName TO columnName
    ;

renameConstraint
    : RENAME CONSTRAINT ignoredIdentifier_ TO ignoredIdentifier_
    ;

renameTableSpecification_
    : RENAME TO identifier
    ;

indexExistClause_
    : (IF EXISTS)?
    ;

indexNames
    : indexName (COMMA_ indexName)*
    ;
