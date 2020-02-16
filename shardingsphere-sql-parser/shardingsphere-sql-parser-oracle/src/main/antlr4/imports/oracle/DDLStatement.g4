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

import Symbol, Keyword, OracleKeyword, Literals, BaseRule;

createTable
    : CREATE createTableSpecification_ TABLE tableName createDefinitionClause
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX indexName ON createIndexDefinitionClause
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause
    ;

// TODO hongjun throw exeption when alter index on oracle
alterIndex
    : ALTER INDEX indexName renameIndexClause_
    ;

dropTable
    : DROP TABLE tableName
    ;
 
dropIndex
    : DROP INDEX indexName
    ;

truncateTable
    : TRUNCATE TABLE tableName
    ;

createTableSpecification_
    : (GLOBAL TEMPORARY)?
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

createDefinitionClause
    : (LP_ relationalProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
    ;

relationalProperties
    : relationalProperty (COMMA_ relationalProperty)*
    ;

relationalProperty
    : columnDefinition | virtualColumnDefinition | outOfLineConstraint | outOfLineRefConstraint
    ;

columnDefinition
    : columnName dataType SORT? visibleClause_ (defaultNullClause_ expr | identityClause)? (ENCRYPT encryptionSpecification_)? (inlineConstraint+ | inlineRefConstraint)?
    ;

visibleClause_
    : (VISIBLE | INVISIBLE)?
    ;

defaultNullClause_
    : DEFAULT (ON NULL)?
    ;

identityClause
    : GENERATED (ALWAYS | BY DEFAULT (ON NULL)?) AS IDENTITY identifyOptions
    ;

identifyOptions
    : LP_? (identityOption+)? RP_?
    ;

identityOption
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

encryptionSpecification_
    : (USING STRING_)? (IDENTIFIED BY STRING_)? STRING_? (NO? SALT)?
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
    | RELY | NORELY 
    | usingIndexClause 
    | ENABLE | DISABLE 
    | VALIDATE | NOVALIDATE 
    | exceptionsClause
    ;

notDeferrable
    : NOT? DEFERRABLE
    ;

initiallyClause
    : INITIALLY (IMMEDIATE | DEFERRED)
    ;

exceptionsClause
    : EXCEPTIONS INTO tableName
    ;

usingIndexClause
    : USING INDEX (indexName | createIndexClause_)?
    ;

createIndexClause_ 
    :  LP_ createIndex RP_
    ;

inlineRefConstraint
    : SCOPE IS tableName | WITH ROWID | (CONSTRAINT ignoredIdentifier_)? referencesClause constraintState*
    ;

virtualColumnDefinition
    : columnName dataType? (GENERATED ALWAYS)? AS LP_ expr RP_ VIRTUAL? inlineConstraint*
    ;

outOfLineConstraint
    : (CONSTRAINT ignoredIdentifier_)?
    (UNIQUE columnNames
    | primaryKey columnNames 
    | FOREIGN KEY columnNames referencesClause
    | CHECK LP_ expr RP_
    ) constraintState*
    ;

outOfLineRefConstraint
    : SCOPE FOR LP_ lobItem RP_ IS tableName
    | REF LP_ lobItem RP_ WITH ROWID
    | (CONSTRAINT ignoredIdentifier_)? FOREIGN KEY lobItemList referencesClause constraintState*
    ;

createIndexSpecification_
    : (UNIQUE | BITMAP)?
    ;

tableIndexClause
    : tableName alias? indexExpressions_
    ;

indexExpressions_
    : LP_ indexExpression_ (COMMA_ indexExpression_)* RP_
    ;

indexExpression_
    : (columnName | expr) (ASC | DESC)?
    ;

bitmapJoinIndexClause_
    : tableName columnSortsClause_ FROM tableAlias WHERE expr
    ;

columnSortsClause_
    : LP_ columnSortClause_ (COMMA_ columnSortClause_)* RP_
    ;
    
columnSortClause_
    : (tableName | alias)? columnName (ASC | DESC)?
    ;

createIndexDefinitionClause
    : tableIndexClause | bitmapJoinIndexClause_
    ;

tableAlias
    : tableName alias? (COMMA_ tableName alias?)*
    ;

alterDefinitionClause
    : (alterTableProperties | columnClauses | constraintClauses | alterExternalTable)?
    ;

alterTableProperties
    : renameTableSpecification_ | REKEY encryptionSpecification_
    ;

renameTableSpecification_
    : RENAME TO identifier
    ;

columnClauses
    : operateColumnClause+ | renameColumnClause
    ;

operateColumnClause
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
    : ELEMENT? IS OF TYPE? LP_ ONLY? dataTypeName RP_ | NOT? SUBSTITUTABLE AT ALL LEVELS
    ;

modifyColumnSpecification
    : MODIFY (LP_? modifyColProperties (COMMA_ modifyColProperties)* RP_? | modifyColSubstitutable)
    ;

modifyColProperties
    : columnName dataType? (DEFAULT expr)? (ENCRYPT encryptionSpecification_ | DECRYPT)? inlineConstraint* 
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

renameColumnClause
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

objectProperties
    : objectProperty (COMMA_ objectProperty)*
    ;

objectProperty
    : (columnName | attributeName) (DEFAULT expr)? (inlineConstraint* | inlineRefConstraint?) | outOfLineConstraint | outOfLineRefConstraint
    ;

renameIndexClause_
    : (RENAME TO indexName)?
    ;
