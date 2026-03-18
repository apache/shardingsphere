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
    : SHOW TEMP? DIST VARIABLES showLike?
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
    : FORCE? REFRESH DATABASE METADATA databaseName?
    ;

refreshTableMetadata
    : REFRESH TABLE METADATA refreshScope?
    ;

showTableMetadata
    : SHOW TABLE METADATA tableName (COMMA_ tableName)* (FROM databaseName)?
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

lockCluster
    : LOCK CLUSTER WITH lockStrategy (TIMEOUT INT_)?
    ;

unlockCluster
    : UNLOCK CLUSTER (TIMEOUT INT_)?
    ;

showPluginImplementations
    : SHOW PLUGINS OF pluginClass
    ;

showKeyGenerateAlgorithmPlugins
    : SHOW KEY GENERATE ALGORITHM PLUGINS
    ;

showLoadBalanceAlgorithmPlugins
    : SHOW LOAD BALANCE ALGORITHM PLUGINS
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

showLike
    : LIKE likePattern
    ;

likePattern
    : STRING_
    ;

pluginClass
    : STRING_
    ;
