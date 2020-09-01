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

grammar DALStatement;

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule, DMLStatement, DDLStatement;

show
    : SHOW (varName | TIME ZONE | TRANSACTION ISOLATION LEVEL | SESSION AUTHORIZATION | ALL)
    ;

set
    : SET runtimeScope_?
    (timeZoneClause_
    | configurationParameterClause
    | varName FROM CURRENT
    | TIME ZONE zoneValue
    | CATALOG STRING_
    | SCHEMA STRING_
    | NAMES encoding?
    | ROLE nonReservedWordOrSconst
    | SESSION AUTHORIZATION nonReservedWordOrSconst
    | SESSION AUTHORIZATION DEFAULT
    | XML OPTION documentOrContent)
    ;

runtimeScope_
    : SESSION | LOCAL
    ;

timeZoneClause_
    : TIME ZONE (numberLiterals | LOCAL | DEFAULT)
    ;

configurationParameterClause
    : identifier (TO | EQ_) (identifier | STRING_ | DEFAULT)
    ;

resetParameter
    : RESET (ALL | identifier)
    ;

explain
    : EXPLAIN
    (analyzeKeyword VERBOSE?
    | VERBOSE
    | LP_ explainOptionList RP_)?
    explainableStmt
    ;

explainableStmt
    : select | insert | update | delete | declare | execute | createMaterializedView | refreshMatViewStmt
    ;

explainOptionList
    : explainOptionElem (COMMA_ explainOptionElem)*
    ;

explainOptionElem
    : explainOptionName explainOptionArg?
    ;

explainOptionArg
    : booleanOrString | numericOnly
    ;

explainOptionName
    : nonReservedWord | analyzeKeyword
    ;

analyzeKeyword
    : ANALYZE | ANALYSE
    ;

setConstraints
    : SET CONSTRAINTS constraintsSetList constraintsSetMode
    ;

constraintsSetMode
    : DEFERRED | IMMEDIATE
    ;

constraintsSetList
    : ALL | qualifiedNameList
    ;

analyze
    : analyzeKeyword (VERBOSE? | LP_ vacAnalyzeOptionList RP_) vacuumRelationList?
    ;

vacuumRelationList
    : vacuumRelation (COMMA_ vacuumRelation)*
    ;

vacuumRelation
    : qualifiedName optNameList
    ;

vacAnalyzeOptionList
    : vacAnalyzeOptionElem (COMMA_ vacAnalyzeOptionElem)*
    ;

vacAnalyzeOptionElem
    : vacAnalyzeOptionName vacAnalyzeOptionArg?
    ;

vacAnalyzeOptionArg
    : booleanOrString | numericOnly
    ;

vacAnalyzeOptionName
    : nonReservedWord | analyzeKeyword
    ;

load
    : LOAD fileName
    ;

valuesClause
    : VALUES LP_ exprList RP_
    | valuesClause COMMA_ LP_ exprList RP_
    ;

vacuum
    : VACUUM ((FULL? FREEZE? VERBOSE? ANALYZE?) | (LP_ vacAnalyzeOptionList RP_)) vacuumRelationList?
    ;

