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
    : HELP STRING_
    ;

explain
    : (DESC | DESCRIBE | EXPLAIN)
    (tableName (columnName | pattern)?
    | explainType? (explainableStatement | FOR CONNECTION connectionId_)
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
    : SHOW EXTENDED? FULL? (COLUMNS | FIELDS) fromTable fromSchema?  (showColumnLike_ | showWhereClause_)?
    ;

showIndex
    : SHOW EXTENDED? (INDEX | INDEXES | KEYS) fromTable fromSchema? showWhereClause_?
    ;

showCreateTable
    : SHOW CREATE TABLE tableName
    ;

showOther
    : SHOW
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

showColumnLike_
    : LIKE stringLiterals
    ;

showWhereClause_
    : WHERE expr
    ;

showFilter
    : showLike | showWhereClause_
    ;

showProfileType
    : ALL | BLOCK IO | CONTEXT SWITCHES | CPU | IPC | MEMORY | PAGE FAULTS | SOURCE | SWAPS
    ;

setVariable
    : SET variableAssign (COMMA_ variableAssign)*
    ;

variableAssign
    : variable EQ_ expr
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
    : SHOW CREATE (DATABASE | SCHEMA) notExistClause_ schemaName
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
    : SHOW ENGINE engineName (STATUS | MUTEX)
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
    : SHOW GRANTS (FOR userOrRole (USING roleName (COMMA_ roleName)+)?)?
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
    : SET (CHARACTER SET | CHARSET) (characterSetName_ | DEFAULT)
    ;

setName
    : SET NAMES (characterSetName_ (COLLATE collationName_)? | DEFAULT)
    ;

clone
    : CLONE cloneAction_
    ;

cloneAction_
    : LOCAL DATA DIRECTORY EQ_? cloneDir SEMI_
    | INSTANCE FROM cloneInstance IDENTIFIED BY STRING_ (DATA DIRECTORY EQ_? cloneDir)? (REQUIRE NO? SSL)?
    ;

createUdf
    : CREATE AGGREGATE? FUNCTION functionName RETURNS (STRING | INTEGER | REAL | DECIMAL) SONAME shardLibraryName
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
    : ANALYZE (NO_WRITE_TO_BINLOG | LOCAL)? TABLE (tableNames 
    | tableName UPDATE HISTOGRAM ON columnNames (WITH NUMBER_ BUCKETS)
    | tableName DROP HISTOGRAM ON columnNames)
    ;

checkTable
    : CHECK TABLE tableNames checkTableOption_
    ;

checkTableOption_
    : FOR UPGRADE | QUICK | FAST | MEDIUM | EXTENDED | CHANGE
    ;

checksumTable
    : CHECKSUM TABLE tableNames (QUICK | EXTENDED)
    ;
optimizeTable
    : OPTIMIZE (NO_WRITE_TO_BINLOG | LOCAL)? TABLE tableNames
    ;

repairTable
    : REPAIR (NO_WRITE_TO_BINLOG | LOCAL)? TABLE tableNames QUICK? EXTENDED? USE_FRM?
    ;

alterResourceGroup
    : ALTER RESOURCE GROUP groupName (VCPU EQ_? vcpuSpec_ (COMMA_ vcpuSpec_)*)? (THREAD_PRIORITY EQ_? NUMBER_)?
    (ENABLE | DISABLE FORCE?)?
    ;

vcpuSpec_
    : NUMBER_ | NUMBER_ MINUS_ NUMBER_
    ;

createResourceGroup
    : CREATE RESOURCE GROUP groupName TYPE EQ_ (SYSTEM | USER) (VCPU EQ_? vcpuSpec_ (COMMA_ vcpuSpec_)*)? 
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
    : CACHE INDEX (tableIndexList (COMMA_ tableIndexList)* | tableName PARTITION LP_ partitionList RP_) IN IDENTIFIER_
    ;

tableIndexList
    : tableName (PARTITION LP_ partitionList RP_)? ((INDEX | KEY) LP_ indexName (COMMA_ indexName)* RP_)? (IGNORE LEAVES)?
    ;

partitionList
    : partitionName (COMMA_ partitionName)* | ALL
    ;

flush
    : FLUSH (NO_WRITE_TO_BINLOG | LOCAL)? (flushOption_ (COMMA_ flushOption_)* | tablesOption_)
    ;

flushOption_
    : BINARY LOGS | ENGINE LOGS | ERROR LOGS | GENERAL LOGS | HOSTS | LOGS | PRIVILEGES | OPTIMIZER_COSTS
    | RELAY LOGS (FOR CHANNEL channelName)? | SLOW LOGS | STATUS | USER_RESOURCES 
    ;

tablesOption_
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
    : RESET resetOption_ (COMMA_ resetOption_)*
    ;

resetOption_
    : MASTER | SLAVE | QUERY CACHE
    ;

resetPersist
    : RESET PERSIST (existClause_ IDENTIFIER_)
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
    : select | tableStatement | delete | insert | replace | update
    ;

formatName
    : TRADITIONAL | JSON | TREE
    ;
