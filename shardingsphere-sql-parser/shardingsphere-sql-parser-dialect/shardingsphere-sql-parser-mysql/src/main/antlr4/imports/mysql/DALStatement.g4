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

grammar DALStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule, DMLStatement;

use
    : USE schemaName
    ;

help
    : HELP string_
    ;

explain
    : (DESC | DESCRIBE | EXPLAIN)
    (tableName (columnRef | textString)?
    | explainType? (explainableStatement | FOR CONNECTION connectionId)
    | ANALYZE select)
    ;

showDatabases
    : SHOW (DATABASES | SCHEMAS) showFilter?
    ;

showTables
    : SHOW EXTENDED? FULL? TABLES fromSchema? showFilter?
    ;

showTableStatus
    : SHOW TABLE STATUS fromSchema? showFilter?
    ;

showColumns
    : SHOW EXTENDED? FULL? (COLUMNS | FIELDS) fromTable fromSchema? (showColumnLike | showWhereClause)?
    ;

showIndex
    : SHOW EXTENDED? (INDEX | INDEXES | KEYS) fromTable fromSchema? showWhereClause?
    ;

showCreateTable
    : SHOW CREATE TABLE tableName
    ;

fromSchema
    : (FROM | IN) schemaName
    ;

fromTable
    : (FROM | IN) tableName
    ;

showLike
    : LIKE stringLiterals
    ;

showColumnLike
    : LIKE stringLiterals
    ;

showWhereClause
    : WHERE expr
    ;

showFilter
    : showLike | showWhereClause
    ;

showProfileType
    : ALL | BLOCK IO | CONTEXT SWITCHES | CPU | IPC | MEMORY | PAGE FAULTS | SOURCE | SWAPS
    ;

setVariable
    : SET optionValueList
    ;

optionValueList
    : optionValueNoOptionType (COMMA_ optionValue)*
    | optionType (internalVariableName EQ_ setExprOrDefault) (COMMA_ optionValue)*
    ;

optionValueNoOptionType
    : internalVariableName EQ_ setExprOrDefault
    | userVariable EQ_ expr
    | setSystemVariable EQ_ setExprOrDefault
    | NAMES (EQ_ expr | charsetName collateClause? | DEFAULT)
    ;

optionValue
    : optionType internalVariableName EQ_ setExprOrDefault | optionValueNoOptionType
    ;

showBinaryLogs
    : SHOW (BINARY | MASTER) LOGS
    ;

showBinlogEvents
    : SHOW BINLOG EVENTS (IN DEFINER)? (FROM NUMBER_)? (LIMIT (NUMBER_ COMMA_)? NUMBER_)?
    ;

showCharacterSet
    : SHOW CHARACTER SET showFilter?
    ;

showCollation
    : SHOW COLLATION showFilter?
    ;

showCreateDatabase
    : SHOW CREATE (DATABASE | SCHEMA) notExistClause? schemaName
    ;

showCreateEvent
    : SHOW CREATE EVENT eventName 
    ;

showCreateFunction
    : SHOW CREATE FUNCTION functionName
    ;

showCreateProcedure
    : SHOW CREATE PROCEDURE functionName
    ;

showCreateTrigger
    : SHOW CREATE TRIGGER triggerName
    ;

showCreateUser
    : SHOW CREATE USER userName
    ;

showCreateView
    : SHOW CREATE VIEW viewName
    ;

showEngine
    : SHOW ENGINE engineRef (STATUS | MUTEX)
    ;

showEngines
    : SHOW STORAGE? ENGINES
    ;

showErrors
    : SHOW (COUNT LP_ ASTERISK_ RP_)? ERRORS (LIMIT (NUMBER_ COMMA_)? NUMBER_)?
    ;

showEvents
    : SHOW EVENTS fromSchema? showFilter?
    ;

showFunctionCode
    : SHOW FUNCTION CODE functionName
    ;

showFunctionStatus
    : SHOW FUNCTION STATUS showFilter?
    ;

showGrant
    : SHOW GRANTS (FOR userOrRole (USING userName (COMMA_ userName)+)?)?
    ;

showMasterStatus
    : SHOW MASTER STATUS
    ;

showOpenTables
    : SHOW OPEN TABLES fromSchema? showFilter?
    ;

showPlugins
    : SHOW PLUGINS
    ;

showPrivileges
    : SHOW PRIVILEGES
    ;

showProcedureCode
    : SHOW PROCEDURE CODE functionName
    ;

showProcedureStatus
    : SHOW PROCEDURE STATUS showFilter
    ;

showProcesslist
    : SHOW FULL? PROCESSLIST
    ;

showProfile
    : SHOW PROFILE ( showProfileType (COMMA_ showProfileType)*)? (FOR QUERY NUMBER_)? (LIMIT NUMBER_ (OFFSET NUMBER_)?)?
    ;

showProfiles
    : SHOW PROFILES
    ;

showRelaylogEvent
    : SHOW RELAYLOG EVENTS (IN logName)? (FROM NUMBER_)? (LIMIT (NUMBER_ COMMA_)? NUMBER_)? FOR CHANNEL channelName
    ;

showSlavehost
    : SHOW SLAVE HOST
    ;

showSlaveStatus
    : SHOW SLAVE STATUS (FOR CHANNEL channelName)?
    ;

showStatus
    : SHOW (GLOBAL | SESSION)? STATUS showFilter?
    ;

showTrriggers
    : SHOW TRIGGER fromSchema? showFilter?
    ;

showVariables
    : SHOW (GLOBAL | SESSION)? VARIABLES showFilter?
    ;

showWarnings
    : SHOW (COUNT LP_ ASTERISK_ RP_)? WARNINGS (LIMIT (NUMBER_ COMMA_)? NUMBER_)?
    ;

setCharacter
    : SET (CHARACTER SET | CHARSET) (charsetName | DEFAULT)
    ;

clone
    : CLONE cloneAction
    ;

cloneAction
    : LOCAL DATA DIRECTORY EQ_? cloneDir
    | INSTANCE FROM cloneInstance IDENTIFIED BY string_ (DATA DIRECTORY EQ_? cloneDir)? (REQUIRE NO? SSL)?
    ;

createUdf
    : CREATE AGGREGATE? FUNCTION functionName RETURNS (STRING | INTEGER | REAL | DECIMAL) SONAME shardLibraryName
    ;

install
    : installComponent | installPlugin
    ;

uninstall
    :uninstallComponent | uninstallPlugin
    ;

installComponent
    : INSTALL COMPONENT componentName (COMMA_ componentName)*
    ;

installPlugin
    : INSTALL PLUGIN pluginName SONAME shardLibraryName
    ;

uninstallComponent
    : UNINSTALL COMPONENT componentName (COMMA_ componentName)*
    ;

uninstallPlugin
    : UNINSTALL PLUGIN pluginName
    ;

analyzeTable
    : ANALYZE (NO_WRITE_TO_BINLOG | LOCAL)? tableOrTables tableList histogram?
    ;

histogram
    : UPDATE HISTOGRAM ON columnNames (WITH NUMBER_ BUCKETS)?
    | DROP HISTOGRAM ON columnNames
    ;

checkTable
    : CHECK tableOrTables tableList checkTableOption?
    ;

checkTableOption
    : FOR UPGRADE | QUICK | FAST | MEDIUM | EXTENDED | CHANGE
    ;

checksumTable
    : CHECKSUM tableOrTables tableList (QUICK | EXTENDED)?
    ;
optimizeTable
    : OPTIMIZE (NO_WRITE_TO_BINLOG | LOCAL)? tableOrTables tableList
    ;

repairTable
    : REPAIR (NO_WRITE_TO_BINLOG | LOCAL)? tableOrTables tableList QUICK? EXTENDED? USE_FRM?
    ;

alterResourceGroup
    : ALTER RESOURCE GROUP groupName (VCPU EQ_? vcpuSpec (COMMA_ vcpuSpec)*)? (THREAD_PRIORITY EQ_? NUMBER_)?
    (ENABLE | DISABLE FORCE?)?
    ;

vcpuSpec
    : NUMBER_ | NUMBER_ MINUS_ NUMBER_
    ;

createResourceGroup
    : CREATE RESOURCE GROUP groupName TYPE EQ_ (SYSTEM | USER) (VCPU EQ_? vcpuSpec (COMMA_ vcpuSpec)*)?
    (THREAD_PRIORITY EQ_? NUMBER_)? (ENABLE | DISABLE )?
    ;

dropResourceGroup
    : DROP RESOURCE GROUP groupName FORCE?
    ;

setResourceGroup
    : SET RESOURCE GROUP groupName (FOR NUMBER_ (COMMA_ NUMBER_)*)?
    ;

binlog
    : BINLOG stringLiterals
    ;

cacheIndex
    : CACHE INDEX (tableIndexList (COMMA_ tableIndexList)* | tableName PARTITION LP_ partitionList RP_) IN (identifier | DEFAULT)
    ;

tableIndexList
    : tableName (PARTITION LP_ partitionList RP_)? ((INDEX | KEY) LP_ indexName (COMMA_ indexName)* RP_)? (IGNORE LEAVES)?
    ;

partitionList
    : partitionName (COMMA_ partitionName)* | ALL
    ;

flush
    : FLUSH (NO_WRITE_TO_BINLOG | LOCAL)? (flushOption (COMMA_ flushOption)* | tablesOption)
    ;

flushOption
    : BINARY LOGS | ENGINE LOGS | ERROR LOGS | GENERAL LOGS | HOSTS | LOGS | PRIVILEGES | OPTIMIZER_COSTS
    | RELAY LOGS (FOR CHANNEL channelName)? | SLOW LOGS | STATUS | USER_RESOURCES 
    ;

tablesOption
    : TABLES |TABLES tableName (COMMA_ tableName)* | TABLES WITH READ LOCK | TABLES tableName (COMMA_ tableName)* WITH READ LOCK
    | TABLES tableName (COMMA_ tableName)* FOR EXPORT
    ;

kill
    : KILL (CONNECTION | QUERY)? NUMBER_+
    ;

loadIndexInfo
    : LOAD INDEX INTO CACHE tableIndexList (COMMA_ tableIndexList)*
    ;

resetStatement
    : RESET resetOption (COMMA_ resetOption)*
    | resetPersist
    ;

resetOption
    : MASTER (TO binaryLogFileIndexNumber)?
    | SLAVE ALL? channelOption?
    ;

resetPersist
    : RESET PERSIST (existClause? identifier)?
    ;

restart
    : RESTART
    ;

shutdown
    : SHUTDOWN
    ;

explainType
    : FORMAT EQ_ formatName
    ;

explainableStatement
    : select | delete | insert | replace | update
    ;

formatName
    : TRADITIONAL | JSON | TREE
    ;

show
    : showDatabases
    | showTables
    | showTableStatus
    | showBinaryLogs
    | showColumns
    | showIndex
    | showCreateDatabase
    | showCreateTable
    | showBinlogEvents
    | showCharacterSet
    | showCollation
    | showCreateEvent
    | showCreateFunction
    | showCreateProcedure
    | showCreateTrigger
    | showCreateUser
    | showCreateView
    | showEngine
    | showEngines
    | showErrors
    | showEvents
    | showFunctionCode
    | showFunctionStatus
    | showGrant
    | showMasterStatus
    | showPlugins
    | showOpenTables
    | showPrivileges
    | showProcedureCode
    | showProcesslist
    | showProfile
    | showProcedureStatus
    | showProfiles
    | showSlavehost
    | showSlaveStatus
    | showRelaylogEvent
    | showStatus
    | showTrriggers
    | showWarnings
    | showVariables
    ;
