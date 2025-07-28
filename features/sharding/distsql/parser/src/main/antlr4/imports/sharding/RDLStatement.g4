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
    : CREATE SHARDING TABLE RULE ifNotExists? shardingTableRuleDefinition (COMMA_ shardingTableRuleDefinition)*
    ;

alterShardingTableRule
    : ALTER SHARDING TABLE RULE shardingTableRuleDefinition (COMMA_ shardingTableRuleDefinition)*
    ;

dropShardingTableRule
    : DROP SHARDING TABLE RULE ifExists? tableName (COMMA_ tableName)*
    ;

createShardingTableReferenceRule
    : CREATE SHARDING TABLE REFERENCE RULE ifNotExists? tableReferenceRuleDefinition (COMMA_ tableReferenceRuleDefinition)*
    ;

alterShardingTableReferenceRule
    : ALTER SHARDING TABLE REFERENCE RULE tableReferenceRuleDefinition (COMMA_ tableReferenceRuleDefinition)*
    ;

dropShardingTableReferenceRule
    : DROP SHARDING TABLE REFERENCE RULE ifExists? ruleName (COMMA_ ruleName)*
    ;

dropShardingAlgorithm
    : DROP SHARDING ALGORITHM ifExists? shardingAlgorithmName (COMMA_ shardingAlgorithmName)*
    ;

createDefaultShardingStrategy
    : CREATE DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY ifNotExists? LP_ shardingStrategy RP_
    ;

alterDefaultShardingStrategy
    : ALTER DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY LP_ shardingStrategy RP_
    ;

dropDefaultShardingStrategy
    : DROP DEFAULT SHARDING type=(DATABASE | TABLE) STRATEGY ifExists?
    ;

dropShardingKeyGenerator
    : DROP SHARDING KEY GENERATOR ifExists? keyGeneratorName (COMMA_ keyGeneratorName)*
    ;

dropShardingAuditor
    : DROP SHARDING AUDITOR ifExists? auditorName (COMMA_ auditorName)*
    ;

shardingTableRuleDefinition
    : (shardingAutoTableRule | shardingTableRule)
    ;

shardingAutoTableRule
    : tableName LP_ storageUnits COMMA_ autoShardingColumnDefinition COMMA_ algorithmDefinition (COMMA_ keyGenerateDefinition)? (COMMA_ auditDefinition)? RP_
    ;

shardingTableRule
    : tableName LP_ dataNodes (COMMA_ databaseStrategy)? (COMMA_ tableStrategy)? (COMMA_ keyGenerateDefinition)? (COMMA_ auditDefinition)? RP_
    ;

keyGeneratorName
    : IDENTIFIER_
    ;

auditorDefinition
    : auditorName LP_ algorithmDefinition RP_
    ;

auditorName
    : IDENTIFIER_
    ;

autoShardingColumnDefinition
    : shardingColumn
    ;

shardingColumnDefinition
    : shardingColumn | shardingColumns
    ;

shardingColumn
    : SHARDING_COLUMN EQ_ columnName
    ;

shardingColumns
    : SHARDING_COLUMNS EQ_ columnName COMMA_ columnName (COMMA_ columnName)*
    ;

shardingAlgorithm
    : SHARDING_ALGORITHM LP_ algorithmDefinition RP_
    ;

shardingStrategy
    : TYPE EQ_ strategyType ((COMMA_ shardingColumnDefinition)? COMMA_ shardingAlgorithm)?
    ;

databaseStrategy
    : DATABASE_STRATEGY LP_ shardingStrategy RP_
    ;

tableStrategy
    : TABLE_STRATEGY LP_ shardingStrategy RP_
    ;

keyGenerateDefinition
    : KEY_GENERATE_STRATEGY LP_ COLUMN EQ_ columnName COMMA_ algorithmDefinition RP_
    ;

auditDefinition
    : AUDIT_STRATEGY LP_ multiAuditDefinition COMMA_ ALLOW_HINT_DISABLE EQ_ auditAllowHintDisable RP_
    ;

multiAuditDefinition
    : singleAuditDefinition (COMMA_ singleAuditDefinition)*
    ;

singleAuditDefinition
    : algorithmDefinition
    ;

auditAllowHintDisable
    : TRUE | FALSE
    ;

tableReferenceRuleDefinition
    : ruleName LP_ tableName (COMMA_ tableName)* RP_
    ;

strategyType
    : STRING_ | buildInStrategyType
    ;

buildInStrategyType
    : STANDARD
    | COMPLEX
    | HINT
    | NONE
    ;
