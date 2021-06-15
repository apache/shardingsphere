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

resources
    : RESOURCES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

ruleName
    : IDENTIFIER
    ;

functionDefinition
    : TYPE LP NAME EQ functionName (COMMA PROPERTIES LP algorithmProperties? RP)? RP
    ;

functionName
    : IDENTIFIER
    ;

algorithmProperties
    : algorithmProperty (COMMA algorithmProperty)*
    ;

algorithmProperty
    : key=(IDENTIFIER | STRING) EQ value=(NUMBER | INT | STRING)
    ;

createDatabaseDiscoveryRule
    : CREATE DB_DISCOVERY RULE databaseDiscoveryRuleDefinition  (COMMA databaseDiscoveryRuleDefinition)*
    ;

databaseDiscoveryRuleDefinition
    : ruleName LP resources COMMA functionDefinition RP
    ;

alterDatabaseDiscoveryRule
    : ALTER DB_DISCOVERY RULE databaseDiscoveryRuleDefinition  (COMMA databaseDiscoveryRuleDefinition)*
    ;

dropDatabaseDiscoveryRule
    : DROP DB_DISCOVERY RULE IDENTIFIER (COMMA IDENTIFIER)*
    ;
