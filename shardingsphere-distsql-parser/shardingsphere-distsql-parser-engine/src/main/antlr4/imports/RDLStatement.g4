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

addResource
    : ADD RESOURCE LP dataSource (COMMA dataSource)* RP
    ;

dropResource
    : DROP RESOURCE LP IDENTIFIER (COMMA IDENTIFIER)* RP
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
    : IDENTIFIER | INT | STRING
    ;

createShardingRule
    : CREATE SHARDING RULE LP shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)* RP
    ;

createReplicaQueryRule
    : CREATE REPLICA_QUERY RULE LP replicaQueryRuleDefinition (COMMA replicaQueryRuleDefinition)* RP
    ;

replicaQueryRuleDefinition
    : ruleName=IDENTIFIER LP PRIMARY EQ primary=schemaName COMMA REPLICA EQ schemaNames RP loadBalancer=IDENTIFIER LP algorithmProperties RP
    ;

alterReplicaQueryRule
    : ALTER REPLICA_QUERY RULE LP alterReplicaQueryRuleDefinition (COMMA alterReplicaQueryRuleDefinition)* RP
    ;

alterReplicaQueryRuleDefinition
    : (MODIFY | ADD) ruleName=IDENTIFIER LP PRIMARY EQ primary=schemaName COMMA REPLICA EQ schemaNames RP (loadBalancer=IDENTIFIER LP algorithmProperties RP)?
    ;

shardingTableRuleDefinition
    : tableName columnName shardingAlgorithmType=IDENTIFIER LP algorithmProperties RP
    ;

tableName
    : IDENTIFIER
    ;

columnName
    : IDENTIFIER
    ;

dropReplicaQueryRule
    : DROP REPLICA_QUERY RULE LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

dropShardingRule
    : DROP SHARDING RULE LP tableName (COMMA tableName)* RP
    ;

showShardingRule
    : SHOW SHARDING RULE (FROM schemaName)?
    ;

schemaNames
    : schemaName (COMMA schemaName)*
    ;

schemaName
    : IDENTIFIER
    ;

algorithmProperties
    : algorithmProperty (COMMA algorithmProperty)*
    ;

algorithmProperty
    : key=IDENTIFIER EQ value=(NUMBER | INT | STRING)
    ;
