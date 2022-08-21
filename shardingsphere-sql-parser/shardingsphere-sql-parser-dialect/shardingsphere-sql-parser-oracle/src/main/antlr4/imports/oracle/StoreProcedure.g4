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

grammar StoreProcedure;

import Keyword, BaseRule, DDLStatement, DMLStatement;

call
    : CALL 
    ;

createProcedure
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? PROCEDURE plsqlProcedureSource
    ;

plsqlProcedureSource
    : (schemaName DOT)? procedureName ( LP_ parameterDeclaration ( COMMA_ parameterDeclaration )* RP_)? sharingClause?
    ((defaultCollationClause | invokerRightsClause | accessibleByClause)*)? (IS | AS) (callSpec | declareSection? body) SEMI_
    ;

declareSection
    : itemList1 itemList2?
    | itemList2
    ;

itemList2
    : cursorDeclaration | cursorDefinition | functionDeclaration | functionDefinition | procedureDeclaration | procedureDefinition
    ;

cursorDefinition
    : CURSOR variableName ( LP_ cursorParameterDec ( COMMA_ cursorParameterDec )* RP_)? ( RETURN rowtype)? IS select SEMI_
    ;

functionDefinition
    : functionHeading ( DETERMINISTIC | PIPELINED | PARALLEL_ENABLE | resultCacheClause )+  ( IS | AS ) ( declareSection ? body | callSpec )
    ;

procedureDefinition
    : procedureDeclaration (IS | AS) (callSpec | declareSection? body)
    ;

itemList1
    :( typeDefinition | cursorDeclaration | itemDeclaration | functionDeclaration | procedureDeclaration )*
    ;

cursorDeclaration
    : CURSOR variableName ( ( cursorParameterDec (COMMA_ cursorParameterDec )* ) )? RETURN rowtype SEMI_
    ;

cursorParameterDec
    : variableName IN? dataType ( (COLON_ EQ_ | DEFAULT) expr )?
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
      typeName ( COLON_ EQ_ ( qualifiedExpression | functionCall | variableName ) )?
      | typeName ( COLON_ EQ_  ( collectionConstructor | variableName ) )?
      | typeName COMA_ TYPE
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
    : typeName LP_ ( identifier (COMMA_ identifier)* )? RP_
    ;

constantDeclaration
    : variableName CONSTANT dataType ( NOT NULL )? ( COLON_ EQ_ | DEFAULT ) expr SEMI_
    ;

cursorVariableDeclaration
    : variableName typeName SEMI_
    ;

exceptionDeclaration
    : variableName EXCEPTION SEMI_
    ;

recordVariableDeclaration
    : variableName ( typeName | rowtypeAttribute | typeName MOD_ TYPE ) SEMI_
    ;

variableDeclaration
    : variableName dataType ( ( NOT NULL )? ( COLON_ EQ_ | DEFAULT ) expr )? SEMI_
    ;

typeDefinition
    : collectionTypeDefinition | recordTypeDefinition | refCursorTypeDefinition | subtypeDefinition
    ;

recordTypeDefinition
    : TYPE typeName IS RECORD  LP_ fieldDefinition ( COMMA_ fieldDefinition )* RP_ SEMI
    ;

fieldDefinition
    : typeName dataType ( ( NOT NULL )? ( COLON_ EQ_ | DEFAULT ) expr )?
    ;

refCursorTypeDefinition
    : TYPE typeName IS REF CURSOR ( RETURN (
    (typeName MOD_ ROWTYPE)
    | (typeName (MOD_ TYPE)?)
    ) )? SEMI_
    ;

subtypeDefinition
    : SUBTYPE typeName IS dataType ( constraint | characterSetClause )? ( NOT NULL )?
    ;

constraint
    : (INTEGER_ COMMA_ INTEGER_) | (RANGE NEMBER_ DOT_ DOT_ NEMBER_)
    ;

collectionTypeDefinition
    : TYPE typeName IS ( assocArrayTypeDef | varrayTypeDef | nestedTableTypeDef ) SEMI
    ;

varrayTypeDef
    : ( VARRAY | (VARYING? ARRAY) ) LP_ INTEGER_ RP_ OF dataType ( NOT NULL )?
    ;

nestedTableTypeDef
    : TABLE OF dataType ( NOT NULL )?
    ;

assocArrayTypeDef
    : TABLE OF dataType ( NOT NULL )?  INDEX BY ( PLS_INTEGER | BINARY_INTEGER | ( VARCHAR2 | VARCHAR2 | STRING ) LP_ INTEGER_ RP_ | LONG | typeAttribute | rowtypeAttribute )
    ;

typeAttribute
    : ( variableName | objectName ) MOD_ TYPE
    ;

rowtypeAttribute
    : ( variableName | objectName ) MOD_ ROWTYPE
    ;
