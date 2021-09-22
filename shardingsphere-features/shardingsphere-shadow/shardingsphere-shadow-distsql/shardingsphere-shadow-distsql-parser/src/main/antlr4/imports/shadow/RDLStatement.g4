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

import Keyword, Literals, Symbol;

createShadowRule
    : CREATE SHADOW RULE shadowRuleDefinition  (COMMA shadowRuleDefinition)*
    ;

alterShadowRule
    : ALTER SHADOW RULE shadowRuleDefinition (COMMA shadowRuleDefinition)*
    ;

dropShadowRule
    : DROP SHADOW RULE ruleName (COMMA ruleName)*
    ;

alterShadowAlgorithm
    : ALTER SHADOW ALGORITHM shadowAlgorithmDefinition (COMMA shadowAlgorithmDefinition)*
    ;

dropShadowAlgorithm
    : DROP SHADOW ALGORITHM algorithmName (COMMA algorithmName)*
    ;

shadowRuleDefinition
    :  ruleName LP SOURCE EQ source COMMA SHADOW EQ shadow COMMA shadowTableRule (COMMA shadowTableRule)* RP
    ;

shadowTableRule
    : tableName LP shadowAlgorithmDefinition (COMMA shadowAlgorithmDefinition)* RP
    ;

ruleName
    : IDENTIFIER
    ;

source
    : IDENTIFIER | STRING
    ;

shadow
    : IDENTIFIER | STRING
    ;

tableName
    : IDENTIFIER
    ;

shadowAlgorithmDefinition
    :  LP (algorithmName COMMA) ? TYPE LP NAME EQ shadowAlgorithmType COMMA PROPERTIES LP algorithmProperties RP RP RP
    ;

algorithmName
    : IDENTIFIER
    ;

shadowAlgorithmType
    : IDENTIFIER
    ;

algorithmProperties
    : algorithmProperty (COMMA algorithmProperty)*
    ;

algorithmProperty
    : key = (SHADOW | IDENTIFIER | STRING) EQ value = (NUMBER | INT | STRING)
    ;
