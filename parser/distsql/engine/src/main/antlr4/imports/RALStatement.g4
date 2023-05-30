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

setDistVariable
    : SET DIST VARIABLE variableName EQ_ variableValue
    ;

showDistVariable
    : SHOW DIST VARIABLE WHERE NAME EQ_ variableName
    ;

showDistVariables
    : SHOW DIST VARIABLES showLike?
    ;

alterComputeNode
    : ALTER COMPUTE NODE instanceId SET variableName EQ_ variableValues
    ;

enableComputeNode
    : ENABLE COMPUTE NODE instanceId
    ;

disableComputeNode
    : DISABLE COMPUTE NODE instanceId
    ;

showComputeNodes
    : SHOW COMPUTE NODES
    ;

refreshDatabaseMetadata
    : REFRESH DATABASE METADATA databaseName? FROM GOVERNANCE CENTER
    ;

refreshTableMetadata
    : REFRESH TABLE METADATA refreshScope?
    ;

showTableMetadata
    : SHOW TABLE METADATA tableName (COMMA_ tableName*)? (FROM databaseName)?
    ;

showComputeNodeInfo
    : SHOW COMPUTE NODE INFO
    ;

showComputeNodeMode
    : SHOW COMPUTE NODE MODE
    ;

labelComputeNode
    : (LABEL | RELABEL) COMPUTE NODE instanceId WITH label (COMMA_ label)*
    ;

unlabelComputeNode
    : UNLABEL COMPUTE NODE instanceId (WITH label (COMMA_ label)*)?
    ;

exportDatabaseConfiguration
    : EXPORT DATABASE CONFIGURATION (FROM databaseName)? (TO FILE filePath)?
    ;

importDatabaseConfiguration
    : IMPORT DATABASE CONFIGURATION FROM FILE filePath
    ;

exportMetaData
    : EXPORT METADATA (TO FILE filePath)?
    ;

importMetaData
    : IMPORT METADATA (metaDataValue | FROM FILE filePath)
    ;

exportStorageNodes
    : EXPORT STORAGE NODES (FROM databaseName)? (TO FILE filePath)?
    ;

convertYamlConfiguration
    : CONVERT YAML CONFIGURATION FROM FILE filePath
    ;

showMigrationRule
    : SHOW MIGRATION RULE
    ;

alterMigrationRule
    : ALTER MIGRATION RULE inventoryIncrementalRule?
    ;

lockCluster
    : LOCK CLUSTER WITH lockStrategy
    ;

unlockCluster
    : UNLOCK CLUSTER
    ;

inventoryIncrementalRule
    : LP_ readDefinition? (COMMA_? writeDefinition)? (COMMA_? streamChannel)? RP_
    ;

readDefinition
    : READ LP_ workerThread? (COMMA_? batchSize)? (COMMA_? shardingSize)? (COMMA_? rateLimiter)? RP_
    ;

writeDefinition
    : WRITE LP_ workerThread? (COMMA_? batchSize)? (COMMA_? rateLimiter)? RP_
    ;

workerThread
    : WORKER_THREAD EQ_ intValue
    ;

batchSize
    : BATCH_SIZE EQ_ intValue
    ;

shardingSize
    : SHARDING_SIZE EQ_ intValue
    ;

rateLimiter
    : RATE_LIMITER LP_ algorithmDefinition RP_
    ;

streamChannel
    : STREAM_CHANNEL LP_ algorithmDefinition RP_
    ;

confPath
    : STRING_
    ;

filePath
    : STRING_
    ;

metaDataValue
    : STRING_
    ;

variableName
    : IDENTIFIER_ | STRING_
    ;

variableValues
    : variableValue (COMMA_ variableValue)*
    ;

variableValue
    : literal
    ;

instanceId
    : IDENTIFIER_ | STRING_
    ;

refreshScope
    : tableName? fromSegment?
    ;

fromSegment
    : FROM STORAGE UNIT storageUnitName (SCHEMA schemaName)?
    ;

lockStrategy
    : LOCK_STRATEGY LP_ algorithmDefinition RP_
    ;

label
    : IDENTIFIER_
    ;

intValue
    : INT_
    ;

showLike
    : LIKE likePattern
    ;

likePattern
    : STRING_
    ;
