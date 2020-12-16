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

createDataSources
    : CREATE DATASOURCES LP dataSource (COMMA dataSource)* RP
    ;

dataSource
    : dataSourceName EQ dataSourceDefinition
    ;

dataSourceName
    : IDENTIFIER
    ;

dataSourceDefinition
    : hostName COLON port COLON dbName (COLON user (COLON password)?)?
    ;

hostName
    : IDENTIFIER | ip
    ;

ip
    : NUMBER+
    ;

port
    : INT
    ;

dbName
    : IDENTIFIER
    ;

user
    : IDENTIFIER | NUMBER
    ;

password
    : IDENTIFIER | NUMBER | STRING
    ;

createShardingRule
    : CREATE SHARDING RULE LP shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)* RP
    ;

shardingTableRuleDefinition
    : tableName columName shardingAlgorithmDefinition
    ;

shardingAlgorithmDefinition
    : shardingAlgorithmType LP shardingAlgorithmProperties RP
    ;

shardingAlgorithmType
    : IDENTIFIER
    ;

shardingAlgorithmProperties
    : shardingAlgorithmProperty (COMMA shardingAlgorithmProperty)*
    ;

shardingAlgorithmProperty
    : shardingAlgorithmPropertyKey EQ shardingAlgorithmPropertyValue
    ;

shardingAlgorithmPropertyKey
    : IDENTIFIER
    ;

shardingAlgorithmPropertyValue
    : NUMBER | INT | STRING
    ;

tableName
    : IDENTIFIER
    ;

columName
    : IDENTIFIER
    ;

dropShardingRule
    : DROP SHARDING RULE LP tableName (COMMA tableName)* RP
    ;
