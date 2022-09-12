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

createShardingTableRule
    : CREATE SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

alterShardingTableRule
    : ALTER SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

dropShardingTableRule
    : DROP SHARDING TABLE RULE ifExists? tableName (COMMA tableName)* withUnusedAlgorithmsClause?
    ;

createShardingBindingTableRules
    : CREATE SHARDING BINDING TABLE RULES bindTableRulesDefinition (COMMA bindTableRulesDefinition)*
    ;

alterShardingBindingTableRules
    : ALTER SHARDING BINDING TABLE RULES bindTableRulesDefinition (COMMA bindTableRulesDefinition)*
    ;

dropShardingBindingTableRules
    : DROP SHARDING BINDING TABLE RULES ifExists? (bindTableRulesDefinition (COMMA bindTableRulesDefinition)*)?
    ;

createShardingBroadcastTableRules
    : CREATE SHARDING BROADCAST TABLE RULES LP tableName (COMMA tableName)* RP
    ;

alterShardingBroadcastTableRules
    : ALTER SHARDING BROADCAST TABLE RULES LP tableName (COMMA tableName)* RP
    ;

dropShardingBroadcastTableRules
    : DROP SHARDING BROADCAST TABLE RULES ifExists? (tableName (COMMA tableName)*)?
    ;

createShardingAlgorithm
    : CREATE SHARDING ALGORITHM shardingAlgorithmDefinition (COMMA shardingAlgorithmDefinition)*
    ;

alterShardingAlgorithm
    : ALTER SHARDING ALGORITHM shardingAlgorithmDefinition (COMMA shardingAlgorithmDefinition)*
    ;

dropShardingAlgorithm
    : DROP SHARDING ALGORITHM ifExists? shardingAlgorithmName (COMMA shardingAlgorithmName)*
    ;

createDefaultShardingStrategy
    : CREATE DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY LP shardingStrategy RP
    ;

alterDefaultShardingStrategy
    : ALTER DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY LP shardingStrategy RP
    ;

dropDefaultShardingStrategy
    : DROP DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY ifExists?
    ;

createShardingKeyGenerator
    : CREATE SHARDING KEY GENERATOR keyGeneratorDefinition (COMMA keyGeneratorDefinition)*
    ;

alterShardingKeyGenerator
    : ALTER SHARDING KEY GENERATOR keyGeneratorDefinition (COMMA keyGeneratorDefinition)*
    ;

dropShardingKeyGenerator
    : DROP SHARDING KEY GENERATOR ifExists? keyGeneratorName (COMMA keyGeneratorName)*
    ;

createShardingAuditor
    : CREATE SHARDING AUDITOR auditorDefinition (COMMA auditorDefinition)*
    ;

alterShardingAuditor
    : ALTER SHARDING AUDITOR auditorDefinition (COMMA auditorDefinition)*
    ;

dropShardingAuditor
    : DROP SHARDING AUDITOR ifExists? auditorName (COMMA auditorName)*
    ;

shardingTableRuleDefinition
    : (shardingAutoTableRule | shardingTableRule)
    ;

shardingAutoTableRule
    : tableName LP resources COMMA autoShardingColumnDefinition COMMA algorithmDefinition (COMMA keyGenerateDeclaration)? RP
    ;

shardingTableRule
    : tableName LP dataNodes (COMMA databaseStrategy)? (COMMA tableStrategy)? (COMMA keyGenerateDeclaration)? (COMMA auditDeclaration)? RP
    ;

keyGeneratorDefinition
    : keyGeneratorName LP algorithmDefinition RP
    ;

keyGeneratorName
    : IDENTIFIER
    ;

auditorDefinition
    : auditorName LP algorithmDefinition RP
    ;

auditorName
    : IDENTIFIER
    ;

resources
    : RESOURCES LP resource (COMMA resource)* RP
    ;

resource
    : IDENTIFIER | STRING
    ;

dataNodes
    : DATANODES LP dataNode (COMMA dataNode)* RP
    ;

dataNode
    : STRING
    ;

autoShardingColumnDefinition
    : shardingColumn
    ;

shardingColumnDefinition
    : shardingColumn | shardingColumns
    ;

shardingColumn
    : SHARDING_COLUMN EQ columnName
    ;

shardingColumns
    : SHARDING_COLUMNS EQ columnName COMMA columnName (COMMA columnName)*
    ;

shardingAlgorithm
    : existingAlgorithm | autoCreativeAlgorithm
    ;

existingAlgorithm
    : SHARDING_ALGORITHM EQ shardingAlgorithmName
    ;

autoCreativeAlgorithm
    : SHARDING_ALGORITHM LP algorithmDefinition RP
    ;

keyGenerator
    : KEY_GENERATOR EQ shardingAlgorithmName
    ;

shardingStrategy
    : TYPE EQ strategyType COMMA shardingColumnDefinition COMMA shardingAlgorithm 
    ;

databaseStrategy
    : DATABASE_STRATEGY LP shardingStrategy RP
    ;

tableStrategy
    : TABLE_STRATEGY LP shardingStrategy RP
    ;

keyGenerateDeclaration
    : keyGenerateDefinition | keyGenerateStrategy
    ;

keyGenerateDefinition
    : KEY_GENERATE_STRATEGY LP COLUMN EQ columnName COMMA algorithmDefinition RP
    ;

keyGenerateStrategy
    : KEY_GENERATE_STRATEGY LP COLUMN EQ columnName COMMA keyGenerator RP
    ;

auditDeclaration
    : auditDefinition | auditStrategy
    ;

auditDefinition
    : AUDIT_STRATEGY LP AUDITORS EQ columnName (COMMA dataNode)* COMMA algorithmDefinition RP
    ;

auditStrategy
    : AUDIT_STRATEGY LP AUDITORS EQ LBT auditorNames RBT COMMA ALLOW_HINT_DISABLE EQ auditAllowHintDisable RP
    ;

auditorNames
    : auditorName (COMMA auditorName)*
    ;

auditAllowHintDisable
    : TRUE | FALSE
    ;

algorithmDefinition
    : TYPE LP NAME EQ algorithmTypeName (COMMA PROPERTIES LP algorithmProperties? RP)? RP
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

algorithmTypeName
    : STRING
    ;

strategyType
    : STRING
    ;

algorithmProperties
    : algorithmProperty (COMMA algorithmProperty)*
    ;

algorithmProperty
    : key=STRING EQ value=STRING
    ;

ifExists
    : IF EXISTS
    ;

withUnusedAlgorithmsClause
    : WITH UNUSED ALGORITHMS
    ;
