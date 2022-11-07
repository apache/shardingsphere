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

grammar RDLStatement;

import BaseRule;

createReadwriteSplittingRule
    : CREATE READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition (COMMA readwriteSplittingRuleDefinition)*
    ;

alterReadwriteSplittingRule
    : ALTER READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition (COMMA readwriteSplittingRuleDefinition)*
    ;

dropReadwriteSplittingRule
    : DROP READWRITE_SPLITTING RULE ifExists? ruleName (COMMA ruleName)*
    ;

readwriteSplittingRuleDefinition
    : ruleName LP (staticReadwriteSplittingRuleDefinition | dynamicReadwriteSplittingRuleDefinition) (COMMA algorithmDefinition)? RP
    ;

staticReadwriteSplittingRuleDefinition
    : WRITE_STORAGE_UNIT EQ writeStorageUnitName COMMA READ_STORAGE_UNITS LP readStorageUnitsNames RP
    ;

dynamicReadwriteSplittingRuleDefinition
    : AUTO_AWARE_RESOURCE EQ resourceName (COMMA WRITE_DATA_SOURCE_QUERY_ENABLED EQ writeDataSourceQueryEnabled)?
    ;

ruleName
    : IDENTIFIER
    ;

writeStorageUnitName
    : storageUnitName
    ;

readStorageUnitsNames
    : storageUnitName (COMMA storageUnitName)*
    ;

ifExists
    : IF EXISTS
    ;

writeDataSourceQueryEnabled
    : TRUE | FALSE
    ;
