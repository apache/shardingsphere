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

grammar RALStatement;

import BaseRule;

setVariable
    : SET VARIABLE variableName EQ variableValue
    ;

showVariable
    : SHOW VARIABLE variableName
    ;

showAllVariables
    : SHOW ALL VARIABLES
    ;

alterInstance
    : ALTER INSTANCE instanceId SET variableName EQ variableValues
    ;

enableInstance
    : ENABLE INSTANCE instanceId
    ;

disableInstance
    : DISABLE INSTANCE instanceId
    ;

showInstanceList
    : SHOW INSTANCE LIST
    ;

clearHint
    : CLEAR HINT
    ;

refreshTableMetadata
    : REFRESH TABLE METADATA refreshScope?
    ;

showTableMetadata
    : SHOW TABLE METADATA tableName (COMMA tableName*)? (FROM databaseName)?
    ;

showTransactionRule
    : SHOW TRANSACTION RULE
    ;

alterTransactionRule
    : ALTER TRANSACTION RULE transactionRuleDefinition
    ;

showSQLParserRule
    : SHOW SQL_PARSER RULE
    ;

alterSQLParserRule
    : ALTER SQL_PARSER RULE sqlParserRuleDefinition
    ;

showInstanceInfo
    : SHOW INSTANCE INFO
    ;

showModeInfo
    : SHOW MODE INFO
    ;

labelInstance
    : (LABEL | RELABEL) INSTANCE instanceId WITH label (COMMA label)*
    ;

unlabelInstance
    : UNLABEL INSTANCE instanceId (WITH label (COMMA label)*)?
    ;

exportDatabaseConfiguration
    : EXPORT DATABASE (CONFIGURATION | CONFIG) (FROM databaseName)? (COMMA? FILE EQ filePath)?
    ;

importDatabaseConfiguration
    : IMPORT DATABASE (CONFIGURATION | CONFIG) FILE EQ filePath
    ;

convertYamlConfiguration
    : CONVERT YAML (CONFIGURATION | CONFIG) FILE EQ filePath
    ;

showSQLTranslatorRule
    : SHOW SQL_TRANSLATOR RULE
    ;

showMigrationProcessConfiguration
    : SHOW MIGRATION PROCESS CONFIGURATION
    ;

createMigrationProcessConfiguration
    : CREATE MIGRATION PROCESS CONFIGURATION migrationProcessConfiguration?
    ;

alterMigrationProcessConfiguration
    : ALTER MIGRATION PROCESS CONFIGURATION migrationProcessConfiguration?
    ;

dropMigrationProcessConfiguration
    : DROP MIGRATION PROCESS CONFIGURATION confPath
    ;

migrationProcessConfiguration
    : LP readDefinition? (COMMA? writeDefinition)? (COMMA? streamChannel)? RP
    ;

readDefinition
    : READ LP workerThread? (COMMA? batchSize)? (COMMA? shardingSize)? (COMMA? rateLimiter)? RP
    ;

writeDefinition
    : WRITE LP workerThread? (COMMA? batchSize)? (COMMA? rateLimiter)? RP
    ;

workerThread
    : WORKER_THREAD EQ intValue
    ;

batchSize
    : BATCH_SIZE EQ intValue
    ;

shardingSize
    : SHARDING_SIZE EQ intValue
    ;

rateLimiter
    : RATE_LIMITER LP algorithmDefinition RP
    ;

streamChannel
    : STREAM_CHANNEL LP algorithmDefinition RP
    ;

confPath
    : STRING
    ;

filePath
    : STRING
    ;

transactionRuleDefinition
    : LP DEFAULT EQ defaultType (COMMA providerDefinition)?
    ;

providerDefinition
    : TYPE LP NAME EQ providerName (COMMA propertiesDefinition)? RP
    ;

defaultType
    : STRING
    ;

providerName
    : STRING
    ;

sqlParserRuleDefinition
    : SQL_COMMENT_PARSE_ENABLE EQ sqlCommentParseEnable (COMMA PARSE_TREE_CACHE LP parseTreeCache RP)? (COMMA SQL_STATEMENT_CACHE LP sqlStatementCache RP)?
    ;

variableName
    : IDENTIFIER
    ;

variableValues
    : variableValue (COMMA variableValue)*
    ;

variableValue
    : STRING | (MINUS)? INT | TRUE | FALSE
    ;

instanceId
    : IDENTIFIER | STRING
    ;

refreshScope
    : tableName? fromSegment?
    ;

fromSegment
    : FROM RESOURCE resourceName (SCHEMA schemaName)?
    ;

sqlCommentParseEnable
    : TRUE | FALSE
    ;

parseTreeCache
    : cacheOption
    ;

sqlStatementCache
    : cacheOption
    ;

cacheOption
    : (INITIAL_CAPACITY EQ initialCapacity)? (COMMA? MAXIMUM_SIZE EQ maximumSize)? (COMMA? CONCURRENCY_LEVEL EQ concurrencyLevel)? 
    ;

initialCapacity
    : INT
    ;

maximumSize
    : INT
    ;

concurrencyLevel
    : INT
    ;

label
    : IDENTIFIER
    ;

intValue
    : INT
    ;

prepareDistSQL
    : PREPARE DISTSQL
    ;

applyDistSQL
    : APPLY DISTSQL
    ;

discardDistSQL
    : DISCARD DISTSQL
    ;
