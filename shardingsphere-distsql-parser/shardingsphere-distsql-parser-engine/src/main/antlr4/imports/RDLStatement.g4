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
    : ADD RESOURCE dataSource (COMMA dataSource)*
    ;

dropResource
    : DROP RESOURCE IDENTIFIER (COMMA IDENTIFIER)*
    ;

dataSource
    : dataSourceName LP HOST EQ hostName COMMA PORT EQ port COMMA DB EQ dbName COMMA USER EQ user (COMMA PASSWORD EQ password)? RP
    ;

dataSourceName
    : IDENTIFIER
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

createShardingTableRule
    : CREATE SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

createShardingBindingTableRules
    : CREATE SHARDING BINDING TABLE RULES LP bindTableRulesDefinition (COMMA bindTableRulesDefinition)* RP
    ;

bindTableRulesDefinition
    : LP tableName (COMMA tableName)* RP
    ;

createShardingBroadcastTableRules
    : CREATE SHARDING BROADCAST TABLE RULES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

alterShardingTableRule
    : ALTER SHARDING TABLE RULE shardingTableRuleDefinition (COMMA shardingTableRuleDefinition)*
    ;

createReplicaQueryRule
    : CREATE REPLICA_QUERY RULE LP replicaQueryRuleDefinition (COMMA replicaQueryRuleDefinition)* RP
    ;

replicaQueryRuleDefinition
    : ruleName LP PRIMARY EQ primary=schemaName COMMA REPLICA EQ schemaNames RP functionDefinition
    ;

alterReplicaQueryRule
    : ALTER REPLICA_QUERY RULE LP alterReplicaQueryRuleDefinition (COMMA alterReplicaQueryRuleDefinition)* RP
    ;

alterReplicaQueryRuleDefinition
    : (MODIFY | ADD) ruleName LP PRIMARY EQ primary=schemaName COMMA REPLICA EQ schemaNames RP functionDefinition?
    ;

alterShardingTableRuleDefinition
    : (MODIFY | ADD) shardingTableRuleDefinition
    ;

shardingTableRuleDefinition
    : tableName LP resources (COMMA shardingColumn)? (COMMA functionDefinition)? (COMMA keyGenerateStrategy)? RP
    ;

resources
    : RESOURCES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

shardingColumn
    : SHARDING_COLUMN EQ columnName
    ;

alterBindingTables
    : alterBindingTable (COMMA alterBindingTable)*
    ;

alterBindingTable
    : (ADD | DROP) bindingTable
    ;

bindingTables
    : bindingTable (COMMA bindingTable)*
    ;

bindingTable
    : BINDING_TABLE LP tableNames RP
    ;

defaultTableStrategy
    : DEFAULT_TABLE_STRATEGY columnName? functionDefinition
    ;

broadcastTables
    : BROADCAST_TABLES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

keyGenerateStrategy
    : GENERATED_KEY LP COLUMN EQ columnName COMMA functionDefinition RP
    ;

actualDataNodes
    : STRING (COMMA STRING)*
    ;

ruleName
    : IDENTIFIER
    ;

tableName
    : IDENTIFIER
    ;

tableNames
    : IDENTIFIER+
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

functionDefinition
    : TYPE LP NAME EQ functionName COMMA PROPERTIES LP algorithmProperties? RP RP
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
