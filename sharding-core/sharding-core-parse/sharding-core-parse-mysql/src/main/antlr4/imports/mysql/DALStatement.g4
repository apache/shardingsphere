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

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

use
    : USE schemaName
    ;

desc
    : (DESC | DESCRIBE) tableName
    ;

showDatabases
    : SHOW (DATABASES | SCHEMAS) (showLike | showWhereClause_)?
    ;

showTables
    : SHOW EXTENDED? FULL? TABLES fromSchema? (showLike | showWhereClause_)?
    ;

showTableStatus
    : SHOW TABLE STATUS fromSchema? (showLike | showWhereClause_)?
    ;

showColumns
    : SHOW EXTENDED? FULL? (COLUMNS | FIELDS) fromTable_ fromSchema? (showLike | showWhereClause_)?
    ;

showIndex
    : SHOW EXTENDED? (INDEX | INDEXES | KEYS) fromTable_ fromSchema? showWhereClause_?
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

fromTable_
    : (FROM | IN) tableName
    ;

showLike
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
    : SET variable_?
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
    : SHOW CREATE (DATABASE | SCHEMA) (IF NOT EXISTS)? schemaName
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
    : SHOW ((ERRORS (LIMIT (NUMBER_ COMMA_)? NUMBER_)?) | (COUNT LP_ ASTERISK_ RP_ ERRORS))
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
    : SHOW GRANTS ( FOR (userName | roleName) (USING roleName (COMMA_ roleName)+)?)
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
    : SHOW PROFILE ( showProfileType (COMMA_ showProfileType)*)? (FOR QUERY NUMBER_)? (LIMIT NUMBER_ (OFFSET NUMBER_))?
    ;

showProfiles
    : SHOW PROFILES
    ;

showRelaylogEvent
    : SHOW RELAYLOG EVENTS (IN logName)? (FROM NUMBER_)? (LIMIT (NUMBER_ COMMA_)? NUMBER_) FOR CHANNEL channelName
    ;

showSlavehost
    : SHOW SLAVE HOST
    ;

showSlaveStatus
    : SHOW SLAVE STATUS (FOR CHANNEL channelName)
    ;

showStatus
    : SHOW (GLOBAL | SESSION) STATUS showFilter?
    ;

showTrriggers
    : SHOW TRIGGER fromSchema? showFilter?
    ;

showVariables
    : SHOW (GLOBAL | SESSION)? VARIABLES showFilter
    ;

showWarnings
    : SHOW ((WARNINGS (LIMIT (NUMBER_ COMMA_)? NUMBER_)?) | (COUNT LP_ ASTERISK_ RP_ WARNINGS))
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
    | INSTANCE FROM cloneInstance IDENTIFIED BY STRING_ (DATA DIRECTORY EQ_ cloneDir)? (REQUIRE NO? SSL)?
    ;

createUdf
    : CREATE AGGREGATE FUNCTION functionName RETURNS ( STRING | INTEGER | REAL | DECIMAL) SONAME shardLibraryName
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

repaidTable
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

alterUser
    : ALTER USER (IF EXISTS)? userName userAuthOption_? (COMMA_ userName userAuthOption_?)* 
    (REQUIRE (NONE | tlsOption_ (AND? tlsOption_)*))? (WITH resourceOption_ resourceOption_*)? (passwordOption_ | lockOption_)*   
    | ALTER USER (IF EXISTS)? USER LP_ RP_ userFuncAuthOption_
    | ALTER USER (IF EXISTS)? userName DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*)
    ;

userAuthOption_
    : IDENTIFIED BY STRING_ (REPLACE STRING_)? (RETAIN CURRENT PASSWORD)?
    | IDENTIFIED BY RANDOM PASSWORD (REPLACE STRING_)? (RETAIN CURRENT PASSWORD)?
    | IDENTIFIED WITH pluginName
    | IDENTIFIED WITH pluginName BY stringLiterals (REPLACE stringLiterals)? (RETAIN CURRENT PASSWORD)?
    | IDENTIFIED WITH pluginName BY RANDOM PASSWORD (REPLACE stringLiterals)? (RETAIN CURRENT PASSWORD)?
    | IDENTIFIED WITH pluginName AS stringLiterals
    | DISCARD OLD PASSWORD
    ;

lockOption_
    : ACCOUNT LOCK | ACCOUNT UNLOCK
    ;


passwordOption_
    : PASSWORD EXPIRE (DEFAULT | NEVER | INTERVAL NUMBER_ DAY)?
    | PASSWORD HISTORY (DEFAULT | NUMBER_)
    | PASSWORD REUSE INTERVAL (DEFAULT | NUMBER_ DAY)
    | PASSWORD REQUIRE CURRENT (DEFAULT | OPTIONAL)
    ;

resourceOption_
    : MAX_QUERIES_PER_HOUR NUMBER_
    | MAX_UPDATES_PER_HOUR NUMBER_
    | MAX_CONNECTIONS_PER_HOUR NUMBER_
    | MAX_USER_CONNECTIONS NUMBER_
    ;

tlsOption_
    : SSL | X509 | CIPHER STRING_ | ISSUER STRING_ | SUBJECT STRING_
    ;


userFuncAuthOption_
    : IDENTIFIED BY 'auth_string' (REPLACE 'current_auth_string')? (RETAIN CURRENT PASSWORD)?
    | DISCARD OLD PASSWORD
    ;

createRole
    : CREATE ROLE (IF NOT EXISTS)? roleName (COMMA_ roleName)*
    ;

createUser
    : CREATE USER (IF NOT EXISTS)? userName userAuthOption_? (COMMA_ userName userAuthOption_?)*
    DEFAULT ROLE roleName (COMMA_ roleName)* (REQUIRE (NONE | tlsOption_ (AND? tlsOption_)*))?
    (WITH resourceOption_ resourceOption_*)? (passwordOption_ | lockOption_)*
    ;

dropRole
    : DROP ROLE (IF EXISTS)? roleName (COMMA_ roleName)*
    ;

dropUser
    : DROP USER (IF EXISTS)? userName (COMMA_ userName)*
    ;

grant
    : GRANT privilegeType_ columnNames? (COMMA_ privilegeType_ columnNames?)* ON objectType_? privilegeLevel_
    TO userOrRole (COMMA_ userOrRole)* (WITH GRANT OPTION)?
    (AS userName (WITH ROLE DEFAULT | NONE | ALL | ALL EXCEPT roleName (COMMA_ roleName)* )?)?
    | GRANT PROXY ON userOrRole TO userOrRole (COMMA_ userOrRole)* (WITH GRANT OPTION)?
    | GRANT roleName (COMMA_ roleName)* TO userOrRole userOrRole* (WITH GRANT OPTION) 
    ;

objectType_
    : TABLE | FUNCTION | PROCEDURE
    ;

privilegeType_
    : IDENTIFIER_
    ;

privilegeLevel_
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | schemaName DOT_ASTERISK_ | tableName 
    ;

renameUser
    : RENAME USER userName TO userName (COMMA_ userName TO userName)*
    ;

revoke
    : REVOKE privilegeType_ columnNames? (COMMA_ privilegeType_ columnNames?)* ON objectType_? privilegeLevel_
    FROM userOrRole (COMMA_ userOrRole)*
    | REVOKE ALL PRIVILEGES COMMA_ GRANT OPTION FROM userOrRole (COMMA_ userOrRole)*
    | REVOKE PROXY ON userOrRole FROM userOrRole (COMMA_ userOrRole)*
    | REVOKE roleName (COMMA_ roleName)* FROM userOrRole (COMMA_ userOrRole)*
    ;

setDefaultRole
    : SET DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*) TO userName (COMMA_ userName)*
    ;

setPassword
    : SET PASSWORD (FOR userName)? authOption_ (REPLACE stringLiterals)? (RETAIN CURRENT PASSWORD)?
    ;

authOption_
    : EQ_ stringLiterals | TO RANDOM
    ;

setRole
    : SET ROLE (DEFAULT | NONE | ALL | ALL EXCEPT roleName (COMMA_ roleName)* | roleName (COMMA_ roleName)*)
    ;

binlog
    : BINLOG stringLiterals
    ;

cacheIndex
    : CACHE INDEX (tableIndexList (COMMA_ tableIndexList)* | tableName PARTITION LP_ partitionList RP_) IN IDENTIFIER_
    ;

tableIndexList
    : tableName (PARTITION LP_ partitionList RP_)? ((INDEX | KEY) LP_ indexName (COMMA_ indexName)* RP_)?
    ;

partitionList
    : partitionName (COMMA_ partitionName)* | ALL
    ;

flush
    : FLUSH (NO_WRITE_TO_BINLOG | LOCAL)? (flushOption_ (COMMA_ flushOption_) * | tablesOption_)
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
    : MASTER | SLAVE
    ;

resetPersist
    : RESET PERSIST ((IF EXISTS)? IDENTIFIER_)
    ;

restart
    : RESTART
    ;

shutdown
    : SHUTDOWN
    ;
