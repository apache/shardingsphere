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

alterShardingBindingTableRules
    : ALTER SHARDING BINDING TABLE RULES LP bindTableRulesDefinition (COMMA bindTableRulesDefinition)* RP
    ;

alterShardingBroadcastTableRules
    : ALTER SHARDING BROADCAST TABLE RULES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

createReadwriteSplittingRule
    : CREATE READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition (COMMA readwriteSplittingRuleDefinition)*
    ;

readwriteSplittingRuleDefinition
    : ruleName LP (staticReadwriteSplittingRuleDefinition | dynamicReadwriteSplittingRuleDefinition) (COMMA functionDefinition)? RP
    ;

staticReadwriteSplittingRuleDefinition
    : WRITE_RESOURCE EQ writeResourceName COMMA READ_RESOURCES LP resourceName (COMMA resourceName)* RP
    ;

dynamicReadwriteSplittingRuleDefinition
    : AUTO_AWARE_RESOURCE EQ IDENTIFIER
    ;

alterReadwriteSplittingRule
    : ALTER READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition (COMMA readwriteSplittingRuleDefinition)*
    ;

shardingTableRuleDefinition
    : tableName LP resources (COMMA shardingColumn)? (COMMA functionDefinition)? (COMMA keyGenerateStrategy)? RP
    ;

resources
    : RESOURCES LP IDENTIFIER (COMMA IDENTIFIER)* RP
    ;

writeResourceName
    : resourceName
    ;

resourceName
    : IDENTIFIER
    ;

shardingColumn
    : SHARDING_COLUMN EQ columnName
    ;

keyGenerateStrategy
    : GENERATED_KEY LP COLUMN EQ columnName COMMA functionDefinition RP
    ;

ruleName
    : IDENTIFIER
    ;

tableName
    : IDENTIFIER
    ;

columnName
    : IDENTIFIER
    ;

dropReadwriteSplittingRule
    : DROP READWRITE_SPLITTING RULE IDENTIFIER (COMMA IDENTIFIER)*
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

schemaNames
    : schemaName (COMMA schemaName)*
    ;

schemaName
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

createEncryptRule
    : CREATE ENCRYPT RULE encryptRuleDefinition (COMMA encryptRuleDefinition)*
    ;

encryptRuleDefinition
    : tableName LP (RESOURCE EQ resourceName COMMA)? COLUMNS LP columnDefinition (COMMA columnDefinition)*  RP RP
    ;

columnDefinition
    : LP NAME EQ columnName (COMMA PLAIN EQ plainColumnName)? COMMA CIPHER EQ cipherColumnName COMMA functionDefinition RP
    ;

alterEncryptRule
    : ALTER ENCRYPT RULE encryptRuleDefinition (COMMA encryptRuleDefinition)*
    ;

dropEncryptRule
    : DROP ENCRYPT RULE IDENTIFIER (COMMA IDENTIFIER)*
    ;

plainColumnName
    : IDENTIFIER
    ;

cipherColumnName
    : IDENTIFIER
    ;
