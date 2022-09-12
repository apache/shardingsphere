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

showMigrationProcessConfiguration
    : SHOW MIGRATION PROCESS CONFIGURATION
    ;

createMigrationProcessConfiguration
    : CREATE MIGRATION PROCESS CONFIGURATION inventoryIncrementalProcessConfiguration?
    ;

alterMigrationProcessConfiguration
    : ALTER MIGRATION PROCESS CONFIGURATION inventoryIncrementalProcessConfiguration?
    ;

dropMigrationProcessConfiguration
    : DROP MIGRATION PROCESS CONFIGURATION confPath
    ;

inventoryIncrementalProcessConfiguration
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
