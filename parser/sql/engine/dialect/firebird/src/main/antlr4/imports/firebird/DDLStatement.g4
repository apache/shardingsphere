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

import DMLStatement;

createTable
    : CREATE createTemporaryTable? TABLE tableName createDefinitionClause sqlSecurity?
    ;

createCollation
    : CREATE COLLATION collationName FOR characterSetName fromCollationClause? paddingClause? caseSensitivityClause? accentSensitivityClause? attributeClause?
    ;

fromCollationClause
    : FROM baseSortName | FROM EXTERNAL LP_ STRING_ RP_
    ;

paddingClause
    : NO PAD | PAD SPACE
    ;

caseSensitivityClause
    : CASE SENSITIVE | CASE INSENSITIVE
    ;

accentSensitivityClause
    : ACCENT SENSITIVE | ACCENT INSENSITIVE
    ;

attributeClause
    : attributeCollation (SEMI_ attributeCollation)*
    ;

createDomain
    : CREATE DOMAIN domainName AS? dataType defaultClause? notNullClause? checkClause? characterSetClause? collateClause?
    ;

defaultClause
    : DEFAULT defaultValue?
    ;

notNullClause
    : NOT NULL
    ;

checkClause
    : CHECK LP_ expr RP_
    ;

characterSetClause
    : CHARACTER SET characterSetName collateClause?
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause
    ;

alterSequence
    : ALTER SEQUENCE tableName sequenceRestartClause? sequenceIncrementClause?
    ;

createSequence
    : CREATE (GENERATOR | SEQUENCE) tableName sequenceRestartClause? sequenceIncrementClause?
    ;

alterDomain
    : ALTER DOMAIN domainName toTableClause? defaultAlterDomainClause? notNullAlterDomainClause? constraintClause? typeClause?
    ;

toTableClause
    : TO tableName
    ;

defaultAlterDomainClause
    : (SET DEFAULT defaultValue | DROP DEFAULT)
    ;

notNullAlterDomainClause
    : (SET | DROP) NOT NULL
    ;

constraintClause
    : (ADD CONSTRAINT? CHECK LP_ expr RP_ | DROP CONSTRAINT)
    ;

typeClause
    : TYPE dataType (CHARACTER SET literals (COLLATE sortOrder)?)?
    ;

dropTable
    : DROP TABLE tableNames dropBehaviour
    ;

createFunction
    : CREATE FUNCTION functionName
      inputArgumentClause?
      RETURNS typeDescriptionArgument
      collateClause?
      DETERMINISTIC?
      (
          EXTERNAL NAME externalModuleName ENGINE engineName
      |
          (SQL SECURITY (DEFINER | INVOKER))?
          AS
          announcementClause?
          BEGIN
              statementBlock
          END
      )
    ;

statementBlock
    : (statement SEMI_?)*
    ;

statement
    : select
    | insert
    | update
    | delete
    | returnStatement
    | cursorOpenStatement
    | cursorCloseStatement
    | assignmentStatement
    | transferStatement
    | fetchStatement
    | whileStatement
    | ifStatement
    | executeStmt
    ;

cursorOpenStatement
    : OPEN cursorName
    ;

cursorCloseStatement
    : CLOSE cursorName
    ;

announcementClause
    : announcement (COMMA_ announcement)*
    ;

announcement
    : localVariableOrCursorAnnouncement
    | procedureAnnouncement
    | functioneAnnouncement
    ;

localVariableOrCursorAnnouncement
    : DECLARE VARIABLE? (
    localVariableDeclarationName typeDescriptionArgument
    (NOT NULL)?
    collateClause?
    ((EQ_ | DEFAULT) defaultValue)?
    | cursorName
          CURSOR FOR (SCROLL | NO SCROLL)? LP_ select RP_ SEMI_? )
    ;

procedureAnnouncement
    : PROCEDURE procedureName inputArgumentClause? (RETURNS inputArgumentClause)?
    ;

functioneAnnouncement
    : FUNCTION functionName inputArgumentClause? RETURNS typeDescriptionArgument collateClause? DETERMINISTIC?
    ;

inputArgument
    : announcementArgument ((EQ_ | DEFAULT) defaultValue)?
    ;

inputArgumentClause
    : LP_ (inputArgument (COMMA_ inputArgument)*)? RP_
    ;

createDatabase
    : CREATE SCHEMA schemaName createDatabaseSpecification_*
    ;

dropDatabase
    : DROP SCHEMA schemaName dropBehaviour
    ;

createView
    : (CREATE (OR ALTER)? VIEW)
    viewName viewAliasClause?
      AS select
      (WITH (CASCADED | LOCAL)? CHECK OPTION)?
    ;

viewAliasClause
    : LP_ viewAlias (COMMA_ viewAlias)* RP_
    ;

viewAlias
    : columnName (AS alias)?
    ;

dropView
    : DROP VIEW viewName dropBehaviour
    ;

createTemporaryTable
    : GLOBAL TEMPORARY
    ;

sqlSecurity
    : SQL SECURITY (DEFINER | INVOKER)
    ;

createDefinitionClause
    : LP_ createDefinition (COMMA_ createDefinition)* RP_
    ;

sequenceRestartClause
    : RESTART (WITH bitExpr)?
    ;

sequenceIncrementClause
    : INCREMENT BY? NUMBER_
    ;

createDatabaseSpecification_
    : DEFAULT CHARACTER SET EQ_? characterSetName
    ;

createDefinition
    : columnDefinition | constraintDefinition | checkConstraintDefinition
    ;

columnDefinition
    : columnName dataType? (GENERATED BY DEFAULT AS IDENTITY ( LP_ START WITH NUMBER_ RP_)?)? dataTypeOption*
    ;


dataTypeOption
    : primaryKey usingDefinition?
    | UNIQUE usingDefinition?
    | NOT? NULL
    | collateClause
    | checkConstraintDefinition
    | referenceDefinition
    | DEFAULT (literals | expr)
    | STRING_
    | (COMPUTED BY? | GENERATED ALWAYS AS) LP_ expr RP_
    ;

checkConstraintDefinition
    : (CONSTRAINT ignoredIdentifier?)? checkClause
    ;

referenceDefinition
    : REFERENCES tableName columnNames? usingDefinition? (ON (UPDATE | DELETE) referenceOption)*
    ;

referenceOption
    : CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

usingDefinition
    : USING (ASC(ENDING)? | DESC(ENDING)?)? INDEX identifier
    ;

constraintDefinition
    : (CONSTRAINT constraintName?)? (primaryKeyOption | uniqueOption | foreignKeyOption)?
    ;

primaryKeyOption
    : primaryKey columnNames usingDefinition?
    ;

primaryKey
    : PRIMARY KEY
    ;

uniqueOption
    : UNIQUE columnNames usingDefinition?
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
    : modifyColumn (TO tableName
                  | POSITION expr
                  | TYPE (dataType | domainName)
                  | SET DEFAULT defaultValue
                  | DROP DEFAULT
                  | SET NOT NULL
                  | DROP NOT NULL
                  | (TYPE dataType)? (GENERATED ALWAYS AS | COMPUTED BY?) LP_ expr RP_
                  | RESTART (WITH NUMBER_)?
                  )
    ;

modifyColumn
    : ALTER COLUMN? columnName
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

returnStatement
    : RETURN expr
    ;

createProcedure
    : (CREATE (OR ALTER)? PROCEDURE) procedureClause
    ;

alterProcedure
    : ALTER PROCEDURE procedureClause
    ;

procedureClause
    : procedureName (AUTHID (OWNER | CALLER))?
              inputArgumentClause?
              (RETURNS LP_ outputArgumentList RP_)?
              (
                  EXTERNAL NAME externalModuleName ENGINE engineName
              |
                  (SQL SECURITY (DEFINER | INVOKER))?
                  AS
                  announcementClause?
                  BEGIN
                      statementBlock
                  END
              )
    ;

executeStmt
    : executeProcedure | executeBlock
    ;

executeProcedure
    : EXECUTE PROCEDURE procedureName exprClause? returningValuesClause?
    ;

exprClause
    : LP_ expr (COMMA_ expr)* RP_
    ;

returningValuesClause
    : RETURNING_VALUES exprClause SEMI_
    ;

createTrigger
    : (CREATE (OR ALTER)? TRIGGER) triggerName triggerClause
    ;

alterTrigger
    : ALTER TRIGGER triggerName (ACTIVE | INACTIVE)? ((BEFORE | AFTER) eventListTable)? (POSITION expr)? triggerClause
    ;

announcmentTriggerClause
    : (
                announcmentTableTrigger |
                announcmentTableTriggerSQL_2003Standart |
                announcmentDataBaseTrigger |
                announcmentDDLTrigger
                )
    ;

triggerClause
    : announcmentTriggerClause?
          (
                EXTERNAL NAME externalModuleName ENGINE engineName
            |
                (SQL SECURITY (DEFINER | INVOKER) | DROP SQL SECURITY)?
                AS
                announcementClause?
                BEGIN
                    statementBlock
                END
          )
    ;

announcmentTableTrigger
    : FOR (tableName | viewName)
    (ACTIVE | INACTIVE)?
    (BEFORE | AFTER) eventListTable
    (POSITION expr)?
    ;

eventListTable
    : dmlStatement (OR dmlStatement)*
    ;

listDDLStatement
    : ANY DDL STATEMENT
    | ddlStatement (OR ddlStatement)*
    ;

dmlStatement
    : INSERT | UPDATE | DELETE
    ;

ddlStatement
    : (CREATE | ALTER | DROP) TABLE
    | (CREATE | ALTER | DROP) PROCEDURE
    | (CREATE | ALTER | DROP) FUNCTION
    | (CREATE | ALTER | DROP) TRIGGER
    | (CREATE | ALTER | DROP) EXCEPTION
    | (CREATE | ALTER | DROP) VIEW
    | (CREATE | ALTER | DROP) DOMAIN
    | (CREATE | ALTER | DROP) ROLE
    | (CREATE | ALTER | DROP) SEQUENCE
    | (CREATE | ALTER | DROP) USER
    | (CREATE|ALTER|DROP) INDEX
    | (CREATE | DROP) COLLATION
    | ALTER CHARACTER SET
    | (CREATE | ALTER | DROP) PACKAGE
    | (CREATE | DROP) PACKAGE BODY
    | (CREATE | ALTER | DROP) MAPPING
    ;

announcmentTableTriggerSQL_2003Standart
    : (ACTIVE | INACTIVE)?
      (BEFORE | AFTER) eventListTable
      (POSITION expr)?
      ON (tableName | viewName)
    ;

announcmentDataBaseTrigger
    : (ACTIVE | INACTIVE)?
      ON eventConnectOrTransaction
      (POSITION expr)?
    ;

eventConnectOrTransaction
    : CONNECT
    | DISCONNECT
    | TRANSACTION START
    | TRANSACTION COMMIT
    | TRANSACTION ROLLBACK
    ;

announcmentDDLTrigger
    : (ACTIVE | INACTIVE)?
      (BEFORE | AFTER) listDDLStatement
      (POSITION expr)?
    ;

executeBlock
    : EXECUTE BLOCK
    inputArgumentList?
    (RETURNS LP_ outputArgumentList RP_)?
    AS
        announcementClause?
    BEGIN
        statementBlock
    END SEMI_
    ;

inputArgumentList
    : LP_ announcementArgument EQ_ QUESTION_  (COMMA_ (announcementArgument EQ_ QUESTION_))* RP_
    ;

outputArgumentList
    : announcementArgumentClause
    ;

assignmentStatement
    : variableName EQ_ expr
    ;

transferStatement
    : SUSPEND SEMI_
    ;

whileStatement
    : WHILE LP_ expr RP_ DO compoundStatement
    ;

fetchStatement
    : FETCH cursorName
    (INTO COLON_ variable (COMMA_ (COLON_ variable))* SEMI_)?
    | FETCH (NEXT
             | PRIOR
             | FIRST
             | LAST
             | ABSOLUTE NUMBER_
             | RELATIVE NUMBER_ ) FROM cursorName (INTO LBT_ COLON_ RBT_ variable (COMMA_ (LBT_ COLON_ RBT_ variable))* SEMI_)?
    ;

ifStatement
    : IF LP_ expr RP_
      THEN compoundStatement+
      (ELSE compoundStatement+)?
    ;

compoundStatement
    : (createTable | alterTable | dropTable | dropDatabase | insert | update | delete | select | createView | beginStatement | ifStatement | fetchStatement | leaveStatement | transferStatement | cursorCloseStatement | assignmentStatement) SEMI_?
    ;

beginStatement
    : BEGIN compoundStatement* END SEMI_?
    ;

leaveStatement
    : LEAVE expr? SEMI_
    ;

comment
    : COMMENT ON (
      DATABASE
    | baseTypeComment tableName
    | COLUMN tableName.columnName
    | (PROCEDURE | FUNCTION) PARAMETER (packageName DOT_)? procedureName DOT_ parameterName
    | (PROCEDURE | EXTERNAL? FUNCTION) (packageName DOT_)? procedureName
    ) IS (STRING_ | NULL)
    ;

baseTypeComment
    : CHARACTER SET
    | COLLATION
    | DOMAIN
    | EXCEPTION
    | FILTER
    | GENERATOR
    | INDEX
    | PACKAGE
    | USER
    | ROLE
    | SEQUENCE
    | TABLE
    | TRIGGER
    | VIEW
    ;
