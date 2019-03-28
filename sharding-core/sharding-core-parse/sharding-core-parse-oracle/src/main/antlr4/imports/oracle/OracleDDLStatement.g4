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

grammar OracleDDLStatement;

import Symbol, OracleKeyword, Keyword, DataType, OracleBase, BaseRule;

createTable
    : CREATE (GLOBAL TEMPORARY)? TABLE tableName relationalTable
    ;

alterTable
    : ALTER TABLE tableName (alterTableProperties | columnClauses | constraintClauses | alterExternalTable)?
    ;

dropTable
    : DROP TABLE tableName
    ;

truncateTable
    : TRUNCATE TABLE tableName
    ;

tablespaceClauseWithParen
    : LP_ tablespaceClause RP_
    ;

tablespaceClause
    : TABLESPACE ignoredIdentifier_
    ;

domainIndexClause
    : indexTypeName
    ;

relationalTable
    : (LP_ relationalProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)? tableProperties
    ;

relationalProperties
    : relationalProperty (COMMA_ relationalProperty)*
    ;

relationalProperty
    : columnDefinition | virtualColumnDefinition | outOfLineConstraint | outOfLineRefConstraint
    ;

tableProperties
    : columnProperties? (AS unionSelect)?
    ;

unionSelect
    : matchNone
    ;

alterTableProperties
    : renameTableSpecification | REKEY encryptionSpec
    ;

renameTableSpecification
    : RENAME TO tableName
    ;

columnClauses
    : opColumnClause+ | renameColumnSpecification
    ;

opColumnClause
    : addColumnSpecification | modifyColumnSpecification | dropColumnClause
    ;

addColumnSpecification
    : ADD columnOrVirtualDefinitions columnProperties?
    ;

columnOrVirtualDefinitions
    : LP_ columnOrVirtualDefinition (COMMA_ columnOrVirtualDefinition)* RP_ | columnOrVirtualDefinition
    ;

columnOrVirtualDefinition
    : columnDefinition | virtualColumnDefinition
    ;

modifyColumnSpecification
    : MODIFY (LP_? modifyColProperties (COMMA_ modifyColProperties)* RP_? | modifyColSubstitutable)
    ;

modifyColProperties
    : columnName dataType? (DEFAULT expr)? (ENCRYPT encryptionSpec | DECRYPT)? inlineConstraint* 
    ;

modifyColSubstitutable
    : COLUMN columnName NOT? SUBSTITUTABLE AT ALL LEVELS FORCE?
    ;

dropColumnClause
    : SET UNUSED columnOrColumnList cascadeOrInvalidate* | dropColumnSpecification
    ;

dropColumnSpecification
    : DROP columnOrColumnList cascadeOrInvalidate* checkpointNumber?
    ;

columnOrColumnList
    : COLUMN columnName | LP_ columnName (COMMA_ columnName)* RP_
    ;

cascadeOrInvalidate
    : CASCADE CONSTRAINTS | INVALIDATE
    ;

checkpointNumber
    : CHECKPOINT NUMBER_
    ;

renameColumnSpecification
    : RENAME COLUMN columnName TO columnName
    ;

constraintClauses
    : addConstraintSpecification | modifyConstraintClause | renameConstraintClause | dropConstraintClause+
    ;

addConstraintSpecification
    : ADD (outOfLineConstraint+ | outOfLineRefConstraint)
    ;

modifyConstraintClause
    : MODIFY constraintOption constraintState+ CASCADE?
    ;

constraintWithName
    : CONSTRAINT ignoredIdentifier_
    ;

constraintOption
    : constraintWithName | constraintPrimaryOrUnique
    ;

constraintPrimaryOrUnique
    : primaryKey | UNIQUE columnNames
    ;

renameConstraintClause
    : RENAME constraintWithName TO ignoredIdentifier_
    ;

dropConstraintClause
    : DROP
    (
    constraintPrimaryOrUnique CASCADE? ((KEEP | DROP) INDEX)? | (CONSTRAINT ignoredIdentifier_ CASCADE?)
    ) 
    ;

alterExternalTable
    : (addColumnSpecification | modifyColumnSpecification | dropColumnSpecification)+
    ;

columnDefinition
    : columnName dataType SORT? (DEFAULT (ON NULL)? expr | identityClause)? (ENCRYPT encryptionSpec)? (inlineConstraint+ | inlineRefConstraint)?
    ;

identityClause
    : GENERATED (ALWAYS | BY DEFAULT (ON NULL)?) AS IDENTITY LP_? (identityOptions+)? RP_?
    ;

identityOptions
    : START WITH (NUMBER_ | LIMIT VALUE)
    | INCREMENT BY NUMBER_
    | MAXVALUE NUMBER_
    | NOMAXVALUE
    | MINVALUE NUMBER_
    | NOMINVALUE
    | CYCLE
    | NOCYCLE
    | CACHE NUMBER_
    | NOCACHE
    | ORDER
    | NOORDER
    ;

virtualColumnDefinition
    : columnName dataType? (GENERATED ALWAYS)? AS LP_ expr RP_ VIRTUAL? inlineConstraint*
    ;

inlineConstraint
    : (CONSTRAINT ignoredIdentifier_)? (NOT? NULL | UNIQUE | primaryKey | referencesClause | CHECK LP_ expr RP_) constraintState*
    ;

referencesClause
    : REFERENCES tableName columnNames? (ON DELETE (CASCADE | SET NULL))?
    ;

constraintState
    : notDeferrable 
    | initiallyClause 
    | RELY 
    | NORELY 
    | usingIndexClause 
    | ENABLE 
    | DISABLE 
    | VALIDATE 
    | NOVALIDATE 
    | exceptionsClause
    ;

notDeferrable
    : NOT? DEFERRABLE
    ;

initiallyClause
    : INITIALLY (IMMEDIATE | DEFERRED)
    ;

exceptionsClause
    : EXCEPTIONS INTO
    ;

usingIndexClause
    : USING INDEX (indexName | LP_ createIndex RP_)?
    ;

inlineRefConstraint
    : SCOPE IS tableName | WITH ROWID | (CONSTRAINT ignoredIdentifier_)? referencesClause constraintState*
    ;

outOfLineConstraint
    : (CONSTRAINT ignoredIdentifier_)?
    (
    	UNIQUE columnNames
        | primaryKey columnNames 
        | FOREIGN KEY columnNames referencesClause
        | CHECK LP_ expr RP_
    ) 
    constraintState*
    ;

outOfLineRefConstraint
    : SCOPE FOR LP_ lobItem RP_ IS tableName
    | REF LP_ lobItem RP_ WITH ROWID
    | (CONSTRAINT ignoredIdentifier_)? FOREIGN KEY lobItemList referencesClause constraintState*
    ;

encryptionSpec
    : (USING STRING_)? (IDENTIFIED BY STRING_)? STRING_? (NO? SALT)?
    ;

objectProperties
    : objectProperty (COMMA_ objectProperty)*
    ;

objectProperty
    : (columnName | attributeName) (DEFAULT expr)? (inlineConstraint* | inlineRefConstraint?) | outOfLineConstraint | outOfLineRefConstraint
    ;

columnProperties
    : columnProperty+
    ;

columnProperty
    : objectTypeColProperties
    ;

objectTypeColProperties
    : COLUMN columnName substitutableColumnClause
    ;

substitutableColumnClause
    : ELEMENT? IS OF TYPE? LP_ ONLY? dataTypeName_ RP_ | NOT? SUBSTITUTABLE AT ALL LEVELS
    ;

createIndex
    : CREATE (UNIQUE | BITMAP)? INDEX indexName ON (tableIndexClause_ | bitmapJoinIndexClause_)
    ;

tableIndexClause_
    : tableName alias? LP_ indexExpr_ (COMMA_ indexExpr_)* RP_
    ;

indexExpr_
    : (columnName | expr) (ASC | DESC)?
    ;

bitmapJoinIndexClause_
    : tableName LP_ columnSortClause_ (COMMA_ columnSortClause_)* RP_ FROM tableName alias? (COMMA_ tableName alias?)* WHERE expr
    ;

columnSortClause_
    : tableName alias? columnName (ASC | DESC)?
    ;

dropIndex
    : DROP INDEX indexName
    ;

// TODO hongjun throw exeption when alter index on oracle
alterIndex
    : ALTER INDEX indexName (RENAME TO indexName)?
    ;
