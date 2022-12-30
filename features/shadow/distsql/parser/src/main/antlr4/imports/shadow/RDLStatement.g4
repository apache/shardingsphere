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

createShadowRule
    : CREATE SHADOW RULE ifNotExists? shadowRuleDefinition (COMMA_ shadowRuleDefinition)*
    ;

alterShadowRule
    : ALTER SHADOW RULE shadowRuleDefinition (COMMA_ shadowRuleDefinition)*
    ;

dropShadowRule
    : DROP SHADOW RULE ifExists? ruleName (COMMA_ ruleName)*
    ;

dropShadowAlgorithm
    : DROP SHADOW ALGORITHM ifExists? algorithmName (COMMA_ algorithmName)*
    ;

createDefaultShadowAlgorithm
    : CREATE DEFAULT SHADOW ALGORITHM ifNotExists? algorithmDefinition
    ;

dropDefaultShadowAlgorithm
    : DROP DEFAULT SHADOW ALGORITHM ifExists?
    ;

alterDefaultShadowAlgorithm
    : ALTER DEFAULT SHADOW ALGORITHM algorithmDefinition
    ;

shadowRuleDefinition
    :  ruleName LP_ SOURCE EQ_ source COMMA_ SHADOW EQ_ shadow COMMA_ shadowTableRule (COMMA_ shadowTableRule)* RP_
    ;

shadowTableRule
    : tableName LP_ algorithmDefinition (COMMA_ algorithmDefinition)* RP_
    ;

source
    : IDENTIFIER_
    ;

shadow
    : IDENTIFIER_
    ;

tableName
    : IDENTIFIER_
    ;

algorithmName
    : IDENTIFIER_
    ;

ifExists
    : IF EXISTS
    ;

ifNotExists
    : IF NOT EXISTS
    ;
