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

grammar PLSQL;

import Keyword, BaseRule, DDLStatement, DMLStatement, TCLStatement;

@parser::members {
    private boolean isNotPlsqlBlockTerminator() {
        switch (_input.LA(1)) {
            case END:
            case ELSE:
            case ELSIF:
            case EXCEPTION:
            case WHEN:
                return false;
            default:
                return true;
        }
    }
}

call
    : CALL (schemaName DOT_)? procedureName (LP_ (callArgument (COMMA_ callArgument)*)? RP_)? callIntoClause?
    ;

callArgument
    : expression
    ;

callIntoClause
    : INTO (placeholder | variableName)
    ;

alterProcedure
    : ALTER PROCEDURE (schemaName DOT_)? procedureName (procedureCompileClause | (EDITIONABLE | NONEDITIONABLE))
    ;

procedureCompileClause
    : COMPILE DEBUG? (compilerParametersClause)* (REUSE SETTINGS)?
    ;

compilerParametersClause
    : parameterName EQ_ parameterName
    ;

dropProcedure
    : DROP PROCEDURE (schemaName DOT_)? procedureName
    ;

createProcedure
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? PROCEDURE plsqlProcedureSource
    ;

plsqlProcedureSource
    : (schemaName DOT_)? procedureName (LP_ parameterDeclaration (COMMA_ parameterDeclaration)* RP_)? sharingClause?
    ((defaultCollationClause | invokerRightsClause | accessibleByClause)*)? (IS | AS) (callSpec | declareSection? body)
    ;

createFunction
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? FUNCTION plsqlFunctionSource
    ;

createTrigger
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? TRIGGER plsqlTriggerSource
    ;

plsqlFunctionSource
    : function (LP_ parameterDeclaration (COMMA_ parameterDeclaration)* RP_)? returnDateType?
    sharingClause? (invokerRightsClause
    | accessibleByClause
    | defaultCollationoOptionClause
    | deterministicClause
    | parallelEnableClause
    | resultCacheClause
    | aggregateClause
    | pipelinedClause
    | sqlMacroClause)*
    (IS | AS) (callSpec | declareSection? body)
    ;

returnDateType
    : RETURN dataType
    ;

body
    : BEGIN plsqlStatements (EXCEPTION (exceptionHandler)+)? END (identifier)? SEMI_?
    ;

plsqlStatements
    : {isNotPlsqlBlockTerminator()}? statement ({isNotPlsqlBlockTerminator()}? statement)*
    ;

statement
    : {isNotPlsqlBlockTerminator()}? (SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_ (SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_) *)?
        (assignStatement
        | basicLoopStatement
        | caseStatement
        | closeStatement
        | continueStatement
        | collectionMethodStatement
        | cursorForLoopStatement
        | executeImmediateStatement
        | exitStatement
        | fetchStatement
        | forLoopStatement
        | forallStatement
        | gotoStatement
        | ifStatement
        | inlinePragma
        | modifyingStatement
        | nullStatement
        | openStatement
        | openForStatement
        | pipeRowStatement
        | plsqlBlock
        | raiseStatement
        | returnStatement
        | selectIntoStatement
        | sqlStatementInPlsql
        | procedureCall
        | whileLoopStatement
        )
    ;

assignStatement
    : assignStatementTarget ASSIGNMENT_OPERATOR_ expression SEMI_
    ;

assignStatementTarget
    : placeholder
    | hostCursorVariable
    | plsqlVariableTarget
    ;

plsqlVariableTarget
    : name plsqlTargetSuffix*
    ;

plsqlTargetSuffix
    : LP_ (expression (COMMA_ expression)*)? RP_
    | DOT_ identifier
    ;

placeholder
    : COLON_ hostVariable=name (DOT_ columnName)? (COLON_ indicatorVariable=name)?
    ;

expression
    : expr
    ;

booleanExpression
    : NOT? booleanPrimary ((AND | OR) NOT? booleanPrimary)*
    ;

basicLoopStatement
    : (SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_)?
    LOOP plsqlStatements END LOOP label? SEMI_
    ;

caseStatement
    : simpleCaseStatement | searchedCaseStatement
    ;

simpleCaseStatement
    : (SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_)?
    CASE selector=expression
    (WHEN booleanExpression THEN plsqlStatements)+
    (ELSE plsqlStatements)?
    END CASE label? SEMI_
    ;

searchedCaseStatement
    : (SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_)?
    CASE
    (WHEN booleanExpression THEN plsqlStatements)+
    (ELSE plsqlStatements)?
    END CASE label? SEMI_
    ;

closeStatement
    : CLOSE (cursor | cursorVariable | hostCursorVariable) SEMI_
    ;

continueStatement
    : CONTINUE label? (WHEN booleanExpression)? SEMI_
    ;

cursorForLoopStatement
    : FOR record IN
    (cursor (LP_ actualCursorParameter (COMMA_? actualCursorParameter)* RP_)?
    | LP_ select RP_
    )
    LOOP plsqlStatements END LOOP label? SEMI_
    ;

executeImmediateStatement
    : EXECUTE IMMEDIATE dynamicSqlStmt
        ((selectIntoClause | bulkCollectIntoClause) plsqlUsingClause?
        | plsqlUsingClause dynamicReturningClause?
        | dynamicReturningClause
        )? SEMI_
    ;

dynamicReturningClause
    : (RETURNING | RETURN) (selectIntoClause | bulkCollectIntoClause)
    ;

exitStatement
    : EXIT label? (WHEN booleanExpression)? SEMI_
    ;

fetchStatement
    : FETCH (cursor | cursorVariable | hostCursorVariable)
    (selectIntoClause | bulkCollectIntoClause (LIMIT expression)?) SEMI_
    ;

forLoopStatement
    : (SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_)?
    FOR iterator
        LOOP plsqlStatements
    END LOOP label? SEMI_
    ;

iterator
    : iterandDecl (COMMA_ iterandDecl)* IN iterationCtlSeq
    ;

iterandDecl
    : plsIdentifier=identifier (MUTABLE | IMMUTABLE)? constrainedType=dataType?
    ;

iterationCtlSeq
    : qualIterationCtl (COMMA_ qualIterationCtl)*
    ;

modifyingExpression: INSERTING | DELETING | UPDATING;

qualIterationCtl
    : REVERSE? iterationCcontrol predClauseSeq
    ;

iterationCcontrol
    : steppedControl
    | singleExpressionControl
    | valuesOfControl
    | indicesOfControl
    | pairsOfControl
    | cursorIterationControl
    ;

predClauseSeq
    : (WHILE booleanExpression)? (WHEN booleanExpression)?
    ;

steppedControl
    : lowerBound RANGE_OPERATOR_ upperBound (BY step=expression)?
    ;

singleExpressionControl
    : REPEAT? expression
    ;

valuesOfControl
    : VALUES OF
    (expression
    | cursorVariable
    | LP_ cursorObject | dynamicSql | sqlStatementInPlsql RP_
    )
    ;

indicesOfControl
    : INDICES OF
    (expression
    | cursorVariable
    | LP_ cursorObject | cursorVariable | dynamicSql | sqlStatementInPlsql RP_
    )
    ;

pairsOfControl
    : PAIRS OF
    (expression
    | cursorVariable
    | LP_ cursorObject | dynamicSql | sqlStatementInPlsql RP_
    )
    ;

cursorIterationControl
    : LP_ cursorObject | cursorVariable | dynamicSql | sqlStatementInPlsql RP_
    ;

dynamicSql
    : EXECUTE IMMEDIATE dynamicSqlStmt (USING IN? (bindArgument COMMA_?)* )?
    ;

cursorObject
    : variableName
    ;

forallStatement
    : FORALL index=name IN boundsClause (SAVE EXCEPTIONS)? dmlStatement SEMI_
    ;

boundsClause
    : lowerBound RANGE_OPERATOR_ upperBound
    | INDICES OF collection=name (BETWEEN lowerBound AND upperBound)?
    | VALUES OF indexCollection=name
    ;

lowerBound
    : expression
    ;

upperBound
    : expression
    ;

dmlStatement
    : insert | update | delete | merge | dynamicSqlStmt
    ;

dynamicSqlStmt
    : expression
    ;

gotoStatement
    : GOTO label SEMI_
    ;

ifStatement
    : IF booleanExpression THEN plsqlStatements
    (ELSIF booleanExpression THEN plsqlStatements)*
    (ELSE plsqlStatements)?
    END IF SEMI_
    ;

modifyingStatement: IF modifyingExpression THEN plsqlStatements (ELSIF modifyingExpression THEN plsqlStatements)* (ELSE plsqlStatements)? END IF SEMI_;

nullStatement
    : NULL SEMI_
    ;

openStatement
    : OPEN cursor (LP_ actualCursorParameter (COMMA_? actualCursorParameter)* RP_)? SEMI_
    ;

cursor
    : variableName
    ;

collectionMethodStatement
    : collectionMethodCall SEMI_
    ;

collectionMethodCall
    : name DOT_ collectionMethodName (LP_ (expression (COMMA_ expression)*)? RP_)?
    ;

collectionMethodName
    : DELETE | EXISTS | COUNT | LIMIT | FIRST | LAST | PRIOR | NEXT | EXTEND | TRIM
    ;

openForStatement
    : OPEN (cursorVariable | hostCursorVariable) FOR (select | dynamicSqlStmt) plsqlUsingClause? SEMI_
    ;

cursorVariable
    : variableName
    ;

plsqlUsingClause
    : USING (IN | OUT | IN OUT)? bindArgument (COMMA_? (IN | OUT | IN OUT)? bindArgument)*
    ;

bindArgument
    : expression
    ;

pipeRowStatement
    : PIPE ROW LP_ row=expression RP_ SEMI_
    ;

plsqlBlock
    : ((SIGNED_LEFT_SHIFT_ label SIGNED_RIGHT_SHIFT_)*)? (DECLARE declareSection)? body
    ;

procedureCall
    : (packageName DOT_)? procedureName (LP_ (procedureCallParameter (COMMA_ procedureCallParameter)*)? RP_)? SEMI_
    ;

procedureCallParameter
    : (identifier EQ_ GT_)? expression
    ;

raiseStatement
    : RAISE name? SEMI_
    ;

returnStatement
    : RETURN expression? SEMI_
    ;

selectIntoStatement
    : SELECT (DISTINCT | UNIQUE | ALL)? selectList (selectIntoClause | bulkCollectIntoClause) FROM fromClauseList whereClause? hierarchicalQueryClause? groupByClause? modelClause? windowClause? orderByClause? rowLimitingClause? SEMI_
    ;

selectIntoClause
    : INTO plsqlIntoTarget (COMMA_ plsqlIntoTarget)*
    ;

plsqlIntoTarget
    : plsqlVariableTarget
    | placeholder
    | hostArray
    ;

record
    : name
    ;

bulkCollectIntoClause
    : BULK COLLECT INTO plsqlIntoTarget (COMMA_ plsqlIntoTarget)*
    ;

hostArray
    : COLON_ variableName
    ;

hostCursorVariable
    : COLON_ variableName
    ;

actualCursorParameter
    : expression
    ;

sqlStatementInPlsql
    : (commit
    | delete
    | insert
    | lock
    | merge
    | rollback
    | savepoint
    | setTransaction
    | update
    ) SEMI_
    ;

whileLoopStatement
    : WHILE booleanExpression
    LOOP plsqlStatements END LOOP label? SEMI_
    ;

exceptionHandler
    : WHEN ((typeName (OR typeName)*)| OTHERS) THEN plsqlStatements
    ;

declareSection
    : declareItem+
    ;

declareItem
    : typeDefinition
    | cursorDeclaration
    | itemDeclaration
    | functionDeclaration
    | procedureDeclaration
    | cursorDefinition
    | functionDefinition
    | procedureDefinition
    | pragma
    ;

cursorDefinition
    : CURSOR variableName (LP_ cursorParameterDec (COMMA_ cursorParameterDec)* RP_)? (RETURN rowtype)? IS select SEMI_
    ;

functionDefinition
    : functionHeading (DETERMINISTIC | PIPELINED | PARALLEL_ENABLE | resultCacheClause)*  (IS | AS) (declareSection ? body | callSpec)
    ;

procedureDefinition
    : procedureDeclaration (IS | AS) (callSpec | declareSection? body)
    ;

cursorDeclaration
    : CURSOR variableName ((cursorParameterDec (COMMA_ cursorParameterDec)*))? RETURN rowtype SEMI_
    ;

cursorParameterDec
    : variableName IN? dataType ((ASSIGNMENT_OPERATOR_ | DEFAULT) expr)?
    ;

rowtype
    : typeName MOD_ ROWTYPE
    | typeName (MOD_ TYPE)?
    ;

itemDeclaration
    : collectionVariableDecl | constantDeclaration | cursorVariableDeclaration | exceptionDeclaration | recordVariableDeclaration | variableDeclaration
    ;

collectionVariableDecl
    : variableName
      (
      typeName (ASSIGNMENT_OPERATOR_ (qualifiedExpression | functionCall | variableName))?
      | typeName (ASSIGNMENT_OPERATOR_  (collectionConstructor | variableName))?
      | typeName MOD_ TYPE
      )
      SEMI_
    ;

qualifiedExpression
    : typemark LP_ aggregate RP_
    ;

aggregate
    : positionalChoiceList? explicitChoiceList?
    ;

explicitChoiceList
    : namedChoiceList | indexedChoiceList
    ;

namedChoiceList
    : identifier EQ_ GT_ expr (COMMA_ identifier EQ_ GT_ expr)*
    ;

indexedChoiceList
    : expr EQ_ GT_ expr (COMMA_ expr EQ_ GT_ expr)*
    ;

positionalChoiceList
    : expr (COMMA_ expr)*
    ;

typemark
    : typeName
    ;

collectionConstructor
    : typeName LP_ (identifier (COMMA_ identifier)*)? RP_
    ;

constantDeclaration
    : variableName CONSTANT dataType (NOT NULL)? (ASSIGNMENT_OPERATOR_ | DEFAULT) expr SEMI_
    ;

cursorVariableDeclaration
    : variableName typeName SEMI_
    ;

exceptionDeclaration
    : variableName EXCEPTION SEMI_
    ;

recordVariableDeclaration
    : variableName (typeName | rowtypeAttribute | typeName MOD_ TYPE) SEMI_
    ;

variableDeclaration
    : variableName dataType ((NOT NULL)? (ASSIGNMENT_OPERATOR_ | DEFAULT) expr)? SEMI_
    ;

typeDefinition
    : collectionTypeDefinition | recordTypeDefinition | refCursorTypeDefinition | subtypeDefinition
    ;

recordTypeDefinition
    : TYPE typeName IS RECORD  LP_ fieldDefinition (COMMA_ fieldDefinition)* RP_ SEMI_
    ;

fieldDefinition
    : typeName dataType ((NOT NULL)? (ASSIGNMENT_OPERATOR_ | DEFAULT) expr)?
    ;

refCursorTypeDefinition
    : TYPE typeName IS REF CURSOR (RETURN (
    (typeName MOD_ ROWTYPE)
    | (typeName (MOD_ TYPE)?)
    ))? SEMI_
    ;

subtypeDefinition
    : SUBTYPE typeName IS dataType (constraint | characterSetClause)? (NOT NULL)? SEMI_
    ;

constraint
    : (INTEGER_ COMMA_ INTEGER_) | (RANGE NUMBER_ DOT_ DOT_ NUMBER_)
    ;

collectionTypeDefinition
    : TYPE typeName IS (assocArrayTypeDef | varrayTypeDef | nestedTableTypeDef) SEMI_
    ;

varrayTypeDef
    : (VARRAY | (VARYING? ARRAY)) LP_ INTEGER_ RP_ OF dataType (NOT NULL)?
    ;

nestedTableTypeDef
    : TABLE OF dataType (NOT NULL)?
    ;

assocArrayTypeDef
    : TABLE OF dataType (NOT NULL)?  INDEX BY (PLS_INTEGER | BINARY_INTEGER | (VARCHAR2 | VARCHAR2 | STRING) LP_ INTEGER_ RP_ | LONG | typeAttribute | rowtypeAttribute)
    ;

rowtypeAttribute
    : (variableName | objectName) MOD_ ROWTYPE
    ;

pragma
    : autonomousTransPragma | restrictReferencesPragma | exceptionInitPragma
    | inlinePragma
    | seriallyReusablePragma
    ;

exceptionInitPragma
    : (PRAGMA EXCEPTION_INIT LP_ (exceptionDeclaration | variableName) COMMA_ errorCode RP_ SEMI_)+
    ;

errorCode
    : MINUS_ INTEGER_
    ;

autonomousTransPragma
    : PRAGMA AUTONOMOUS_TRANSACTION SEMI_
    ;

inlinePragma
    : PRAGMA INLINE LP_ name COMMA_ stringLiterals RP_ SEMI_
    ;

seriallyReusablePragma
    : PRAGMA SERIALLY_REUSABLE SEMI_
    ;

plsqlTriggerSource
    : (schemaName DOT_)? triggerName sharingClause? defaultCollationClause? (simpleDmlTrigger | systemTrigger)
    ;

simpleDmlTrigger
    : (BEFORE | AFTER) dmlEventClause (FOR EACH ROW)? triggerBody
    ;

dmlEventClause
    : dmlEventElement (OR dmlEventElement)* ON viewName
    ;

dmlEventElement
    : (DELETE | INSERT | UPDATE) (OF LP_? columnName (COMMA_ columnName)* RP_?)?
    ;

systemTrigger
    : (BEFORE | AFTER | INSTEAD OF) (ddlEvent (OR ddlEvent)* | databaseEvent (OR databaseEvent)* | dmlEvent) ON ((PLUGGABLE? DATABASE) | (schemaName DOT_)? SCHEMA?) tableName? triggerBody
    ;

ddlEvent
    : ALTER
    | ANALYZE
    | ASSOCIATE STATISTICS
    | AUDIT
    | COMMENT
    | CREATE
    | DISASSOCIATE STATISTICS
    | DROP
    | GRANT
    | NOAUDIT
    | RENAME
    | REVOKE
    | TRUNCATE
    | DDL
    | STARTUP
    | SHUTDOWN
    | DB_ROLE_CHANGE
    | LOGON
    | LOGOFF
    | SERVERERROR
    | SUSPEND
    | DATABASE
    | SCHEMA
    | FOLLOWS
    ;

databaseEvent
    : AFTER STARTUP
    | BEFORE SHUTDOWN
    | AFTER DB_ROLE_CHANGE
    | AFTER SERVERERROR
    | AFTER LOGON
    | BEFORE LOGOFF
    | AFTER SUSPEND
    | AFTER CLONE
    | BEFORE UNPLUG
    | (BEFORE | AFTER) SET CONTAINER
    ;

dmlEvent
    : INSERT
    ;

triggerBody
    : plsqlBlock
    ;
