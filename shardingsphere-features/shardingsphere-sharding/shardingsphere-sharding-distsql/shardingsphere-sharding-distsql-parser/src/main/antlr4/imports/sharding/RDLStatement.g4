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

createShardingTableRule
    : CREATE SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

createShardingBindingTableRules
    : CREATE SHARDING BINDING TABLE RULES LP bindTableRulesDefinition (COMMA bindTableRulesDefinition)* RP
    ;

createShardingBroadcastTableRules
    : CREATE SHARDING BROADCAST TABLE RULES LP tableName (COMMA tableName)* RP
    ;

alterShardingTableRule
    : ALTER SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

alterShardingBindingTableRules
    : ALTER SHARDING BINDING TABLE RULES LP bindTableRulesDefinition (COMMA bindTableRulesDefinition)* RP
    ;

alterShardingBroadcastTableRules
    : ALTER SHARDING BROADCAST TABLE RULES LP tableName (COMMA tableName)* RP
    ;

dropShardingTableRule
    : DROP SHARDING TABLE RULE tableName (COMMA tableName)*
    ;

dropShardingBindingTableRules
    : DROP SHARDING BINDING TABLE RULES
    ;

dropShardingBroadcastTableRules
    : DROP SHARDING BROADCAST TABLE RULES
    ;
    
dropShardingAlgorithm
    : DROP SHARDING ALGORITHM algorithmName (COMMA algorithmName)*
    ;

shardingTableRuleDefinition
    : tableName LP resources (COMMA shardingColumn)? (COMMA algorithmDefinition)? (COMMA keyGenerateStrategy)? RP
    ;

resources
    : RESOURCES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

shardingColumn
    : SHARDING_COLUMN EQ columnName
    ;

keyGenerateStrategy
    : GENERATED_KEY LP COLUMN EQ columnName COMMA algorithmDefinition RP
    ;

tableName
    : IDENTIFIER
    ;

columnName
    : IDENTIFIER
    ;

bindTableRulesDefinition
    : LP tableName (COMMA tableName)* RP
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
