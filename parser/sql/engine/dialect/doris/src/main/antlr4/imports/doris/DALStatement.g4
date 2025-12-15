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

import DMLStatement;

use
    : USE databaseName
    ;

help
    : HELP textOrIdentifier
    ;

explain
    : (DESC | DESCRIBE | EXPLAIN)
    (tableName (columnRef | textString)?
    | explainType? (explainableStatement | FOR CONNECTION connectionId)
    | ANALYZE (FORMAT EQ_ TREE)? (select | delete | update | insert))
    ;

fromDatabase
    : (FROM | IN) databaseName
    ;

fromTable
    : (FROM | IN) tableName
    ;

showLike
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
    : internalVariableName equal setExprOrDefault
    | userVariable equal expr
    | setSystemVariable equal setExprOrDefault
    | NAMES (equal expr | charsetName collateClause? | DEFAULT)
    ;

equal
    : EQ_ | ASSIGNMENT_
    ;

optionValue
    : optionType internalVariableName EQ_ setExprOrDefault | optionValueNoOptionType
    ;

showBinaryLogs
    : SHOW (BINARY | MASTER) LOGS
    ;

showBinlogEvents
    : SHOW BINLOG EVENTS (IN logName)? (FROM NUMBER_)? limitClause?
    ;

showCharacterSet
    : SHOW CHARACTER SET showFilter?
    ;

showCollation
    : SHOW COLLATION showFilter?
    ;

showColumns
    : SHOW EXTENDED? FULL? (COLUMNS | FIELDS) fromTable fromDatabase? showFilter?
    ;

showCreateDatabase
    : SHOW CREATE (DATABASE | SCHEMA) ifNotExists? databaseName
    ;

showCreateEvent
    : SHOW CREATE EVENT eventName 
    ;

showCreateFunction
    : SHOW CREATE FUNCTION functionName
    ;

showCreateProcedure
    : SHOW CREATE PROCEDURE procedureName
    ;

showCreateTable
    : SHOW CREATE TABLE tableName
    ;

showCreateTrigger
    : SHOW CREATE TRIGGER triggerName
    ;

showCreateUser
    : SHOW CREATE USER username
    ;

showCreateView
    : SHOW CREATE VIEW viewName
    ;

showDatabases
    : SHOW (DATABASES | SCHEMAS) showFilter?
    ;

showEngine
    : SHOW ENGINE engineRef (STATUS | MUTEX)
    ;

showEngines
    : SHOW STORAGE? ENGINES
    ;

showErrors
    : SHOW (COUNT LP_ ASTERISK_ RP_)? ERRORS limitClause?
    ;

showEvents
    : SHOW EVENTS fromDatabase? showFilter?
    ;

showFunctionCode
    : SHOW FUNCTION CODE functionName
    ;

showFunctionStatus
    : SHOW FUNCTION STATUS showFilter?
    ;

showGrants
    : SHOW GRANTS (FOR (username | roleName) (USING roleName (COMMA_ roleName)*)?)?
    ;

showIndex
    : SHOW EXTENDED? (INDEX | INDEXES | KEYS) fromTable fromDatabase? showWhereClause?
    ;

showMasterStatus
    : SHOW MASTER STATUS
    ;

showOpenTables
    : SHOW OPEN TABLES fromDatabase? showFilter?
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
    : SHOW PROCEDURE STATUS showFilter?
    ;

showProcesslist
    : SHOW FULL? PROCESSLIST
    ;

showProfile
    : SHOW PROFILE (showProfileType (COMMA_ showProfileType)*)? (FOR QUERY NUMBER_)? limitClause?
    ;

showProfiles
    : SHOW PROFILES
    ;

showRelaylogEvent
    : SHOW RELAYLOG EVENTS (IN logName)? (FROM NUMBER_)? limitClause? (FOR CHANNEL channelName)?
    ;

showReplicas
    : SHOW REPLICAS
    ;

showSlaveHosts
    : SHOW SLAVE HOSTS
    ;

showReplicaStatus
    : SHOW REPLICA STATUS (FOR CHANNEL channelName)?
    ;

showSlaveStatus
    : SHOW SLAVE STATUS (FOR CHANNEL channelName)?
    ;

showStatus
    : SHOW (GLOBAL | SESSION)? STATUS showFilter?
    ;

showTableStatus
    : SHOW TABLE STATUS fromDatabase? showFilter?
    ;

showTables
    : SHOW EXTENDED? FULL? TABLES fromDatabase? showFilter?
    ;

showTriggers
    : SHOW TRIGGERS fromDatabase? showFilter?
    ;

showVariables
    : SHOW (GLOBAL | SESSION)? VARIABLES showFilter?
    ;

showWarnings
    : SHOW (COUNT LP_ ASTERISK_ RP_)? WARNINGS limitClause?
    ;

showCharset
    : SHOW CHARSET
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

createLoadableFunction
    : CREATE AGGREGATE? FUNCTION functionName RETURNS (STRING | INTEGER | INT | REAL | DECIMAL | DEC) SONAME shardLibraryName
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
    : UPDATE HISTOGRAM ON columnNames (WITH NUMBER_ BUCKETS | USING DATA string_)?
    | DROP HISTOGRAM ON columnNames
    ;

checkTable
    : CHECK tableOrTables tableList checkTableOption?
    ;

checkTableOption
    : FOR UPGRADE | QUICK | FAST | MEDIUM | EXTENDED | CHANGED
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

alterResource
    : ALTER RESOURCE string_ PROPERTIES LP_ propertyAssignments RP_
    ;

propertyAssignments
    : propertyAssignment (COMMA_ propertyAssignment)*
    ;

propertyAssignment
    : string_ EQ_ string_
    ;

vcpuSpec
    : NUMBER_ | NUMBER_ MINUS_ NUMBER_
    ;

createResourceGroup
    : CREATE RESOURCE GROUP groupName TYPE EQ_ (SYSTEM | USER) (VCPU EQ_? vcpuSpec (COMMA_ vcpuSpec)*)?
    (THREAD_PRIORITY EQ_? numberLiterals)? (ENABLE | DISABLE)?
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
    : CACHE INDEX (cacheTableIndexList (COMMA_ cacheTableIndexList)* | tableName PARTITION LP_ partitionList RP_) IN (identifier | DEFAULT)
    ;

cacheTableIndexList
    : tableName ((INDEX | KEY) LP_ indexName (COMMA_ indexName)* RP_)?
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
    : (TABLES | TABLE)
    | (TABLES | TABLE) tableName (COMMA_ tableName)*
    | (TABLES | TABLE) WITH READ LOCK
    | (TABLES | TABLE) tableName (COMMA_ tableName)* WITH READ LOCK
    | (TABLES | TABLE) tableName (COMMA_ tableName)* FOR EXPORT
    ;

kill
    : KILL (CONNECTION | QUERY)? AT_? IDENTIFIER_
    ;

loadIndexInfo
    : LOAD INDEX INTO CACHE loadTableIndexList (COMMA_ loadTableIndexList)*
    ;

loadTableIndexList
    : tableName (PARTITION LP_ partitionList RP_)? ((INDEX | KEY) LP_ indexName (COMMA_ indexName)* RP_)? (IGNORE LEAVES)?
    ;

resetStatement
    : RESET resetOption (COMMA_ resetOption)*
    | resetPersist
    ;

resetOption
    : MASTER (TO binaryLogFileIndexNumber)?
    | SLAVE ALL? channelOption?
    | REPLICA
    | QUERY CACHE
    ;

resetPersist
    : RESET PERSIST (ifExists? identifier)?
    ;

restart
    : RESTART
    ;

shutdown
    : SHUTDOWN
    ;

explainType
    : (FORMAT EQ_ formatName | EXTENDED | PARTITIONS)
    ;

explainableStatement
    : select | delete | insert | replace | update
    ;

formatName
    : TRADITIONAL | JSON | TREE
    ;

delimiter
    : DELIMITER delimiterName
    ;

change
    : changeMasterTo | changeReplicationFilter
    ;

changeMasterTo
    : CHANGE MASTER TO masterDefs  channelOption?
    ;

changeReplicationFilter
    : CHANGE REPLICATION FILTER filterDefs channelOption?
    ;

changeReplicationSourceTo
    : CHANGE REPLICATION SOURCE TO changeReplicationSourceOptionDefs channelOption?
    ;

startSlave
    : START SLAVE threadTypes? utilOption? connectionOptions channelOption?
    ;

stopSlave
    : STOP SLAVE threadTypes channelOption*
    ;

startReplica
    : START REPLICA threadTypes? utilOption? connectionOptions? channelOption?
    ;

groupReplication
    : startGroupReplication | stopGroupReplication
    ;

startGroupReplication
    : START GROUP_REPLICATION
    ;

stopGroupReplication
    : STOP GROUP_REPLICATION
    ;

purgeBinaryLog
    : PURGE (BINARY | MASTER) LOGS (TO logName | BEFORE datetimeExpr)
    ;

threadTypes
    : threadType+
    ;

threadType
    : RELAY_THREAD | SQL_THREAD
    ;

utilOption
    : UNTIL ((SQL_BEFORE_GTIDS | SQL_AFTER_GTIDS) EQ_ identifier
    | MASTER_LOG_FILE EQ_ string_ COMMA_ MASTER_LOG_POS EQ_ NUMBER_
    | RELAY_LOG_FILE EQ_ string_ COMMA_ RELAY_LOG_POS  EQ_ NUMBER_
    | SQL_AFTER_MTS_GAPS)
    ;

connectionOptions
    : (USER EQ_ string_)? (PASSWORD EQ_ string_)? (DEFAULT_AUTH EQ_ string_)? (PLUGIN_DIR EQ_ string_)?
    ;

masterDefs
    : masterDef (COMMA_ masterDef)*
    ;

masterDef
    : MASTER_BIND EQ_ string_
    | MASTER_HOST EQ_ string_
    | MASTER_USER EQ_ string_
    | MASTER_PASSWORD EQ_ string_
    | MASTER_PORT EQ_ NUMBER_
    | PRIVILEGE_CHECKS_USER EQ_ (ACCOUNT | NULL)
    | REQUIRE_ROW_FORMAT EQ_ NUMBER_
    | MASTER_CONNECT_RETRY EQ_ NUMBER_
    | MASTER_RETRY_COUNT EQ_ NUMBER_
    | MASTER_DELAY EQ_ NUMBER_
    | MASTER_HEARTBEAT_PERIOD EQ_ NUMBER_
    | MASTER_LOG_FILE EQ_ string_
    | MASTER_LOG_POS EQ_ NUMBER_
    | MASTER_AUTO_POSITION EQ_ NUMBER_
    | RELAY_LOG_FILE EQ_ string_
    | RELAY_LOG_POS EQ_ NUMBER_
    | MASTER_COMPRESSION_ALGORITHM EQ_ string_
    | MASTER_ZSTD_COMPRESSION_LEVEL EQ_ NUMBER_
    | MASTER_SSL EQ_ NUMBER_
    | MASTER_SSL_CA EQ_ string_
    | MASTER_SSL_CAPATH EQ_ string_
    | MASTER_SSL_CERT EQ_ string_
    | MASTER_SSL_CRL EQ_ string_
    | MASTER_SSL_CRLPATH EQ_ string_
    | MASTER_SSL_KEY EQ_ string_
    | MASTER_SSL_CIPHER EQ_ string_
    | MASTER_SSL_VERIFY_SERVER_CERT EQ_ NUMBER_
    | MASTER_TLS_VERSION EQ_ string_
    | MASTER_TLS_CIPHERSUITES EQ_ string_
    | MASTER_PUBLIC_KEY_PATH EQ_ string_
    | GET_MASTER_PUBLIC_KEY EQ_ NUMBER_
    | IGNORE_SERVER_IDS EQ_ LP_ ignoreServerIds? RP_
    ;

ignoreServerIds
    : ignoreServerId (COMMA_ ignoreServerId)
    ;

ignoreServerId
    : NUMBER_
    ;

filterDefs
    : filterDef (COMMA_ filterDef)*
    ;

filterDef
    : REPLICATE_DO_DB EQ_ LP_ databaseNames? RP_
    | REPLICATE_IGNORE_DB EQ_ LP_ databaseNames? RP_
    | REPLICATE_DO_TABLE EQ_ LP_ tableList? RP_
    | REPLICATE_IGNORE_TABLE EQ_ LP_ tableList? RP_
    | REPLICATE_WILD_DO_TABLE EQ_ LP_ wildTables? RP_
    | REPLICATE_WILD_IGNORE_TABLE EQ_ LP_ wildTables? RP_
    | REPLICATE_REWRITE_DB EQ_ LP_ databasePairs? RP_
    ;

wildTables
    : wildTable (COMMA_ wildTable)*
    ;

wildTable
    : string_
    ;

changeReplicationSourceOptionDefs
    : changeReplicationSourceOption (COMMA_ changeReplicationSourceOption)*
    ;

changeReplicationSourceOption
    : SOURCE_BIND EQ_ string_
    | SOURCE_HOST EQ_ string_
    | SOURCE_USER EQ_ string_
    | SOURCE_PASSWORD EQ_ string_
    | SOURCE_PORT EQ_ NUMBER_
    | PRIVILEGE_CHECKS_USER EQ_ (NULL | ACCOUNT)
    | REQUIRE_ROW_FORMAT EQ_ NUMBER_
    | REQUIRE_TABLE_PRIMARY_KEY_CHECK EQ_ tablePrimaryKeyCheckDef
    | ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS EQ_ assignGtidsToAnonymousTransactionsDef
    | SOURCE_LOG_FILE EQ_ string_
    | SOURCE_LOG_POS EQ_ NUMBER_
    | SOURCE_AUTO_POSITION EQ_ NUMBER_
    | RELAY_LOG_FILE EQ_ string_
    | RELAY_LOG_POS EQ_ NUMBER_
    | SOURCE_HEARTBEAT_PERIOD EQ_ NUMBER_
    | SOURCE_CONNECT_RETRY EQ_ NUMBER_
    | SOURCE_RETRY_COUNT EQ_ NUMBER_
    | SOURCE_CONNECTION_AUTO_FAILOVER EQ_ NUMBER_
    | SOURCE_DELAY EQ_ NUMBER_
    | SOURCE_COMPRESSION_ALGORITHMS EQ_ string_
    | SOURCE_ZSTD_COMPRESSION_LEVEL EQ_ NUMBER_
    | SOURCE_SSL EQ_ NUMBER_
    | SOURCE_SSL_CA EQ_ string_
    | SOURCE_SSL_CAPATH EQ_ string_
    | SOURCE_SSL_CERT EQ_ string_
    | SOURCE_SSL_CRL EQ_ string_
    | SOURCE_SSL_CRLPATH EQ_ string_
    | SOURCE_SSL_KEY EQ_ string_
    | SOURCE_SSL_CIPHER EQ_ string_
    | SOURCE_SSL_VERIFY_SERVER_CERT EQ_ NUMBER_
    | SOURCE_TLS_VERSION EQ_ string_
    | SOURCE_TLS_CIPHERSUITES EQ_ string_
    | SOURCE_PUBLIC_KEY_PATH EQ_ string_
    | GET_SOURCE_PUBLIC_KEY EQ_ NUMBER_
    | NETWORK_NAMESPACE EQ_ string_
    | IGNORE_SERVER_IDS EQ_ LP_ ignoreServerIds? RP_
    | GTID_ONLY EQ_ NUMBER_
    ;

tablePrimaryKeyCheckDef
    : (STREAM | ON | OFF | GENERATE)
    ;

assignGtidsToAnonymousTransactionsDef
    : (OFF | LOCAL | string_)
    ;

refresh
    : REFRESH (LDAP (ALL | (FOR identifier)?)? | CATALOG identifier | DATABASE (identifier DOT_)? identifier | TABLE ((identifier DOT_)? (identifier DOT_)?)? identifier)
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
    | showCharset
    | showErrors
    | showEvents
    | showFunctionCode
    | showFunctionStatus
    | showGrants
    | showMasterStatus
    | showPlugins
    | showOpenTables
    | showPrivileges
    | showProcedureCode
    | showProcesslist
    | showProfile
    | showProcedureStatus
    | showProfiles
    | showSlaveHosts
    | showSlaveStatus
    | showRelaylogEvent
    | showStatus
    | showTriggers
    | showWarnings
    | showVariables
    | showReplicas
    | showReplicaStatus
    ;
