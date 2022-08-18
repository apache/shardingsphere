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

itemList1
    :( typeDefinition | cursorDeclaration | itemDeclaration | functionDeclaration | procedureDeclaration )*
    ;

typeDefinition
    : collectionTypeDefinition | recordTypeDefinition | refCursorTypeDefinition | subtypeDefinition
    ;

collectionTypeDefinition
    : TYPE typeName IS ( assocArrayTypeDef | varrayTypeDef | nestedTableTypeDef ) SEMI
    ;

assocArrayTypeDef
    : TABLE OF dataType ( NOT NULL )?  INDEX BY (PLS_INTEGER | BINARY_INTEGER | VARCHAR2 LP_ vSize RP_ | dataType )
    ;

vSize: ' ';

dataType
    :
    ;

recordTypeDefinition
    :
    ;

refCursorTypeDefinition
    :
    ;

subtypeDefinition
    :
    ;

cursorDeclaration
    :
    ;

itemDeclaration
    :
    ;


itemList2
    :
    ;

body
    :
    ;