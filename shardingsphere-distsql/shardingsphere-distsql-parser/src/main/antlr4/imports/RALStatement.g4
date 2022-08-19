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

createTrafficRule
    : CREATE TRAFFIC RULE trafficRuleDefinition (COMMA trafficRuleDefinition)* 
    ;

alterTrafficRule
    : ALTER TRAFFIC RULE trafficRuleDefinition (COMMA trafficRuleDefinition)* 
    ;

dropTrafficRule
    : DROP TRAFFIC RULE ifExists? ruleName (COMMA ruleName)*
    ;

labelInstance
    : (LABEL | RELABEL) INSTANCE instanceId WITH label (COMMA label)*
    ;

unlabelInstance
    : UNLABEL INSTANCE instanceId (WITH label (COMMA label)*)?
    ;

trafficRuleDefinition
    : ruleName LP (labelDefinition COMMA)? trafficAlgorithmDefinition (COMMA loadBalancerDefinition)? RP
    ;

labelDefinition
    : LABELS LP label (COMMA label)* RP
    ;

trafficAlgorithmDefinition
    : TRAFFIC_ALGORITHM LP algorithmDefinition RP 
    ;

loadBalancerDefinition
    : LOAD_BALANCER LP algorithmDefinition RP
    ;

algorithmDefinition
    : TYPE LP NAME EQ typeName (COMMA propertiesDefinition)? RP
    ;

typeName
    : IDENTIFIER
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
    : IDENTIFIER
    ;

providerName
    : IDENTIFIER
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

ruleName
    : IDENTIFIER
    ;

label
    : IDENTIFIER
    ;

ifExists
    : IF EXISTS
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
