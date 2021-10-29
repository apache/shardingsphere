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
    : CREATE SHARDING BINDING TABLE RULES bindTableRulesDefinition (COMMA bindTableRulesDefinition)*
    ;

createShardingBroadcastTableRules
    : CREATE SHARDING BROADCAST TABLE RULES LP tableName (COMMA tableName)* RP
    ;

createShardingAlgorithm
    : CREATE SHARDING ALGORITHM shardingAlgorithmDefinition (COMMA  shardingAlgorithmDefinition)*
    ;

createDefaultShardingStrategy
    : CREATE DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY shardingStrategy
    ;

alterShardingTableRule
    : ALTER SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

alterShardingBindingTableRules
    : ALTER SHARDING BINDING TABLE RULES bindTableRulesDefinition (COMMA bindTableRulesDefinition)*
    ;

alterShardingBroadcastTableRules
    : ALTER SHARDING BROADCAST TABLE RULES LP tableName (COMMA tableName)* RP
    ;

dropShardingTableRule
    : DROP SHARDING TABLE RULE tableName (COMMA tableName)*
    ;

dropShardingBindingTableRules
    : DROP SHARDING BINDING TABLE RULES (bindTableRulesDefinition (COMMA bindTableRulesDefinition)*)?
    ;

dropShardingBroadcastTableRules
    : DROP SHARDING BROADCAST TABLE RULES (tableName (COMMA tableName)*)?
    ;
    
dropShardingAlgorithm
    : DROP SHARDING ALGORITHM algorithmName (COMMA algorithmName)*
    ;

shardingTableRuleDefinition
    : tableName LP resources (COMMA shardingColumn)? (COMMA algorithmDefinition)? (COMMA keyGenerateStrategy)? RP
    ;

resources
    : RESOURCES LP resource (COMMA resource)* RP
    ;

resource
    : IDENTIFIER | STRING
    ;

shardingColumn
    : SHARDING_COLUMN EQ columnName
    ;

shardingAlgorithm
    : SHARDING_ALGORITHM EQ shardingAlgorithmName
    ;

shardingStrategy
    :  LP TYPE EQ strategyType COMMA shardingColumn COMMA shardingAlgorithm RP
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

shardingAlgorithmDefinition
    : shardingAlgorithmName LP algorithmDefinition RP
    ;

algorithmDefinition
    : TYPE LP NAME EQ algorithmName (COMMA PROPERTIES LP algorithmProperties? RP)? RP
    ;

algorithmName
    : IDENTIFIER
    ;

shardingAlgorithmName
    : IDENTIFIER
    ;

strategyType
    : IDENTIFIER
    ;

algorithmProperties
    : algorithmProperty (COMMA algorithmProperty)*
    ;

algorithmProperty
    : key=(IDENTIFIER | STRING) EQ value=(NUMBER | INT | IDENTIFIER | STRING)
    ;
