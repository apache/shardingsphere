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
    : CREATE createTableSpecification_? TABLE tableName (createDefinitionClause_ | createLikeClause_)
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause_
    ;

dropTable
    : DROP TABLE tableNames dropBehaviour_
    ;

createDatabase
    : CREATE SCHEMA schemaName createDatabaseSpecification_*
    ;

dropDatabse
    : DROP SCHEMA schemaName dropBehaviour_
    ;

createView
    : CREATE VIEW viewName (LP_ identifier (COMMA_ identifier)* RP_)?
      AS select
      (WITH (CASCADED | LOCAL)? CHECK OPTION)?
    ;

dropView
    : DROP VIEW viewName dropBehaviour_
    ;

createTableSpecification_
    : (GLOBAL | LOCAL) TEMPORARY
    ;

createDefinitionClause_
    : LP_ createDefinitions_ RP_
    ;

createDatabaseSpecification_
    : DEFAULT CHARACTER SET EQ_? characterSetName_
    ;

createDefinitions_
    : createDefinition_ (COMMA_ createDefinition_)*
    ;

createDefinition_
    : columnDefinition | constraintDefinition_ | checkConstraintDefinition_
    ;

columnDefinition
    : columnName dataType (inlineDataType_* | generatedDataType_*)
    ;

inlineDataType_
    : commonDataTypeOption_
    | DEFAULT (literals | expr)
    ;

commonDataTypeOption_
    : primaryKey | UNIQUE KEY? | NOT? NULL | collateClause_ | checkConstraintDefinition_ | referenceDefinition_ | STRING_
    ;

checkConstraintDefinition_
    : (CONSTRAINT ignoredIdentifier_?)? CHECK expr
    ;

referenceDefinition_
    : REFERENCES tableName keyParts_ (MATCH FULL | MATCH PARTIAL | MATCH UNIQUE)? (ON (UPDATE | DELETE) referenceOption_)*
    ;

referenceOption_
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

generatedDataType_
    : commonDataTypeOption_
    ;

keyParts_
    : LP_ keyPart_ (COMMA_ keyPart_)* RP_
    ;

keyPart_
    : (columnName (LP_ NUMBER_ RP_)? | expr) (ASC | DESC)?
    ;

constraintDefinition_
    : (CONSTRAINT ignoredIdentifier_?)? (primaryKeyOption_ | uniqueOption_ | foreignKeyOption_)
    ;

primaryKeyOption_
    : primaryKey columnNames
    ;

primaryKey
    : PRIMARY KEY
    ;

uniqueOption_
    : UNIQUE keyParts_
    ;

foreignKeyOption_
    : FOREIGN KEY columnNames referenceDefinition_
    ;

createLikeClause_
    : LP_? LIKE tableName RP_?
    ;

alterDefinitionClause_
    : alterSpecification_
    ;

alterSpecification_
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
    : ADD constraintDefinition_
    ;

dropConstraintSpecification
    : DROP constraintDefinition_
    ;

