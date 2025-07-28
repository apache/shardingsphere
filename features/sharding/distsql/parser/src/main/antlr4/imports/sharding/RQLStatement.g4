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

grammar RQLStatement;

import BaseRule;

showShardingTableRules
    : SHOW SHARDING TABLE (tableRule | RULES) (FROM databaseName)?
    ;

showShardingTableReferenceRules
    : SHOW SHARDING TABLE REFERENCE (RULE ruleName | RULES) (FROM databaseName)?
    ;

showShardingAlgorithms
    : SHOW SHARDING ALGORITHMS (FROM databaseName)?
    ;

showShardingAuditors
    : SHOW SHARDING AUDITORS (FROM databaseName)?
    ;

showShardingTableNodes
    : SHOW SHARDING TABLE NODES tableName? (FROM databaseName)?
    ;

showShardingKeyGenerators
    : SHOW SHARDING KEY GENERATORS (FROM databaseName)?
    ;

showDefaultShardingStrategy
    : SHOW DEFAULT SHARDING STRATEGY (FROM databaseName)?
    ;

showUnusedShardingAlgorithms
    : SHOW UNUSED SHARDING ALGORITHMS (FROM databaseName)?
    ;

showUnusedShardingKeyGenerators
    : SHOW UNUSED SHARDING KEY GENERATORS (FROM databaseName)?
    ;

showUnusedShardingAuditors
    : SHOW UNUSED SHARDING AUDITORS (FROM databaseName)?
    ;

showShardingTableRulesUsedAlgorithm
    : SHOW SHARDING TABLE RULES USED ALGORITHM shardingAlgorithmName (FROM databaseName)?
    ;

showShardingTableRulesUsedKeyGenerator
    : SHOW SHARDING TABLE RULES USED KEY GENERATOR keyGeneratorName (FROM databaseName)?
    ;

showShardingTableRulesUsedAuditor
    : SHOW SHARDING TABLE RULES USED AUDITOR auditorName (FROM databaseName)?
    ;

countShardingRule
    : COUNT SHARDING RULE (FROM databaseName)?
    ;

tableRule
    : RULE tableName
    ;
