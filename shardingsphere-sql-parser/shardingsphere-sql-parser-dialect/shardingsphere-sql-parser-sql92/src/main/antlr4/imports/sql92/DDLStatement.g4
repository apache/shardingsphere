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

import Symbol, Keyword, SQL92Keyword, Literals, BaseRule, DMLStatement;

createTable
    : CREATE createTableSpecification? TABLE tableName (createDefinitionClause | createLikeClause)
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause
    ;

dropTable
    : DROP TABLE tableNames dropBehaviour
    ;

createDatabase
    : CREATE SCHEMA schemaName createDatabaseSpecification_*
    ;

dropDatabase
    : DROP SCHEMA schemaName dropBehaviour
    ;

createView
    : CREATE VIEW viewName (LP_ identifier (COMMA_ identifier)* RP_)?
      AS select
      (WITH (CASCADED | LOCAL)? CHECK OPTION)?
    ;

dropView
    : DROP VIEW viewName dropBehaviour
    ;

createTableSpecification
    : (GLOBAL | LOCAL) TEMPORARY
    ;

createDefinitionClause
    : LP_ createDefinition (COMMA_ createDefinition)* RP_
    ;

createDatabaseSpecification_
    : DEFAULT CHARACTER SET EQ_? characterSetName
    ;

createDefinition
    : columnDefinition | constraintDefinition | checkConstraintDefinition
    ;

columnDefinition
    : columnName dataType dataTypeOption*
    ;

dataTypeOption
    : primaryKey 
    | UNIQUE KEY? 
    | NOT? NULL 
    | collateClause
    | checkConstraintDefinition
    | referenceDefinition 
    | DEFAULT (literals | expr) 
    | STRING_
    ;

checkConstraintDefinition
    : (CONSTRAINT ignoredIdentifier?)? CHECK expr
    ;

referenceDefinition
    : REFERENCES tableName keyParts (MATCH FULL | MATCH PARTIAL | MATCH UNIQUE)? (ON (UPDATE | DELETE) referenceOption)*
    ;

referenceOption
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

keyParts
    : LP_ keyPart (COMMA_ keyPart)* RP_
    ;

keyPart
    : (columnName (LP_ NUMBER_ RP_)? | expr) (ASC | DESC)?
    ;

constraintDefinition
    : (CONSTRAINT ignoredIdentifier?)? (primaryKeyOption | uniqueOption | foreignKeyOption)
    ;

primaryKeyOption
    : primaryKey columnNames
    ;

primaryKey
    : PRIMARY KEY
    ;

uniqueOption
    : UNIQUE keyParts
    ;

foreignKeyOption
    : FOREIGN KEY columnNames referenceDefinition
    ;

createLikeClause
    : LP_? LIKE tableName RP_?
    ;


alterDefinitionClause
    : addColumnSpecification
    | modifyColumnSpecification
    | dropColumnSpecification
    | addConstraintSpecification
    | dropConstraintSpecification
    ;

addColumnSpecification
    : ADD COLUMN? columnDefinition
    ;

modifyColumnSpecification
    : ALTER COLUMN? columnDefinition
    ;

dropColumnSpecification
    : DROP COLUMN? columnName
    ;

addConstraintSpecification
    : ADD constraintDefinition
    ;

dropConstraintSpecification
    : DROP constraintDefinition
    ;

