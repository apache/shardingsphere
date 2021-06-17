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

createReadwriteSplittingRule
    : CREATE READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition (COMMA readwriteSplittingRuleDefinition)*
    ;

alterReadwriteSplittingRule
    : ALTER READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition (COMMA readwriteSplittingRuleDefinition)*
    ;

dropReadwriteSplittingRule
    : DROP READWRITE_SPLITTING RULE IDENTIFIER (COMMA IDENTIFIER)*
    ;

readwriteSplittingRuleDefinition
    : ruleName LP (staticReadwriteSplittingRuleDefinition | dynamicReadwriteSplittingRuleDefinition) (COMMA algorithmDefinition)? RP
    ;

staticReadwriteSplittingRuleDefinition
    : WRITE_RESOURCE EQ writeResourceName COMMA READ_RESOURCES LP resourceName (COMMA resourceName)* RP
    ;

dynamicReadwriteSplittingRuleDefinition
    : AUTO_AWARE_RESOURCE EQ IDENTIFIER
    ;

writeResourceName
    : resourceName
    ;

resourceName
    : IDENTIFIER
    ;

ruleName
    : IDENTIFIER
    ;

algorithmDefinition
    : TYPE LP NAME EQ algorithmName (COMMA PROPERTIES LP algorithmProperties? RP)? RP
    ;

algorithmName
    : IDENTIFIER
    ;

algorithmProperties
    : algorithmProperty (COMMA algorithmProperty)*
    ;

algorithmProperty
    : key=(IDENTIFIER | STRING) EQ value=(NUMBER | INT | STRING)
    ;
