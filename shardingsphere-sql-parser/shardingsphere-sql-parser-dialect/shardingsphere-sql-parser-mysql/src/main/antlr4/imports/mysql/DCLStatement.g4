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

grammar DCLStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

grant
    : GRANT roleOrPrivileges TO userList withGrantOption? # grantRoleOrPrivilegeTo
    | GRANT roleOrPrivileges ON aclType? grantIdentifier TO userList withGrantOption? grantAs? # grantRoleOrPrivilegeOnTo
    | GRANT ALL PRIVILEGES? ON aclType? grantIdentifier TO userList withGrantOption? grantAs? # grantRoleOrPrivilegeOnTo
    | GRANT PROXY ON userName TO userList withGrantOption? # grantProxy
    ;

revoke
    : REVOKE roleOrPrivileges FROM userList # revokeFrom
    | REVOKE roleOrPrivileges ON aclType? grantIdentifier FROM userList # revokeOnFrom
    | REVOKE ALL PRIVILEGES? ON aclType? grantIdentifier FROM userList # revokeOnFrom
    | REVOKE ALL PRIVILEGES? COMMA_ GRANT OPTION FROM userList # revokeFrom
    | REVOKE PROXY ON userName FROM userList # revokeOnFrom
    ;

userList
    : userName (COMMA_ userName)*
    ;

roleOrPrivileges
    : roleOrPrivilege (COMMA_ roleOrPrivilege)*
    ;

roleOrPrivilege
    : roleIdentifierOrText (LP_ columnNames RP_)? # roleOrDynamicPrivilege
    | roleIdentifierOrText AT_ textOrIdentifier  # roleAtHost
    | SELECT (LP_ columnNames RP_)?  # staticPrivilegeSelect
    | INSERT (LP_ columnNames RP_)?  # staticPrivilegeInsert
    | UPDATE (LP_ columnNames RP_)?  # staticPrivilegeUpdate
    | REFERENCES (LP_ columnNames RP_)?  # staticPrivilegeReferences
    | DELETE  # staticPrivilegeDelete
    | USAGE  # staticPrivilegeUsage
    | INDEX  # staticPrivilegeIndex
    | ALTER  # staticPrivilegeAlter
    | CREATE  # staticPrivilegeCreate
    | DROP  # staticPrivilegeDrop
    | EXECUTE  # staticPrivilegeExecute
    | RELOAD  # staticPrivilegeReload
    | SHUTDOWN  # staticPrivilegeShutdown
    | PROCESS  # staticPrivilegeProcess
    | FILE  # staticPrivilegeFile
    | GRANT OPTION  # staticPrivilegeGrant
    | SHOW DATABASES  # staticPrivilegeShowDatabases
    | SUPER  # staticPrivilegeSuper
    | CREATE TEMPORARY TABLES  # staticPrivilegeCreateTemporaryTables
    | LOCK TABLES  # staticPrivilegeLockTables
    | REPLICATION SLAVE  # staticPrivilegeReplicationSlave
    | REPLICATION CLIENT  # staticPrivilegeReplicationClient
    | CREATE VIEW  # staticPrivilegeCreateView
    | SHOW VIEW  # staticPrivilegeShowView
    | CREATE ROUTINE  # staticPrivilegeCreateRoutine
    | ALTER ROUTINE  # staticPrivilegeAlterRoutine
    | CREATE USER  # staticPrivilegeCreateUser
    | EVENT  # staticPrivilegeEvent
    | TRIGGER  # staticPrivilegeTrigger
    | CREATE TABLESPACE  # staticPrivilegeCreateTablespace
    | CREATE ROLE  # staticPrivilegeCreateRole
    | DROP ROLE  # staticPrivilegeDropRole
    ;

aclType
    : TABLE | FUNCTION | PROCEDURE
    ;

grantIdentifier
    : ASTERISK_ # grantLevelGlobal
    | ASTERISK_ DOT_ASTERISK_ # grantLevelGlobal
    | schemaName DOT_ASTERISK_ # grantLevelSchemaGlobal
    | tableName # grantLevelTable
    ;

createUser
    : CREATE USER (IF NOT EXISTS)? createUserList defaultRoleClause? requireClause? connectOptions? accountLockPasswordExpireOptions?
    ;

createUserEntry
    : userName # createUserEntryNoOption
    | userName IDENTIFIED BY string_ # createUserEntryIdentifiedBy
    | userName IDENTIFIED BY RANDOM PASSWORD # createUserEntryIdentifiedBy
    | userName IDENTIFIED WITH textOrIdentifier # createUserEntryIdentifiedWith
    | userName IDENTIFIED WITH textOrIdentifier AS string_ # createUserEntryIdentifiedWith
    | userName IDENTIFIED WITH textOrIdentifier BY string_ # createUserEntryIdentifiedWith
    | userName IDENTIFIED WITH textOrIdentifier BY RANDOM PASSWORD # createUserEntryIdentifiedWith
    ;

createUserList
    : createUserEntry (COMMA_ createUserEntry)*
    ;

defaultRoleClause
    : DEFAULT ROLE roleName (COMMA_ roleName)*
    ;

requireClause
    : REQUIRE (NONE | SSL | X509 | tlsOption (AND? tlsOption)*)
    ;

connectOptions
    : WITH connectOption connectOption*
    ;

accountLockPasswordExpireOptions
    : accountLockPasswordExpireOption+
    ;

accountLockPasswordExpireOption
    : ACCOUNT (LOCK | UNLOCK)
    | PASSWORD EXPIRE (DEFAULT | NEVER | INTERVAL NUMBER_ DAY)?
    | PASSWORD HISTORY (DEFAULT | NUMBER_)
    | PASSWORD REUSE INTERVAL (DEFAULT | NUMBER_ DAY)
    | PASSWORD REQUIRE CURRENT (DEFAULT | OPTIONAL)?
    | FAILED_LOGIN_ATTEMPTS NUMBER_
    | PASSWORD_LOCK_TIME (NUMBER_ | UNBOUNDED)
    ;

alterUser
    : ALTER USER (IF EXISTS)? alterUserList requireClause? connectOptions? accountLockPasswordExpireOptions?
    | ALTER USER (IF EXISTS)? USER LP_ RP_ userFuncAuthOption
    | ALTER USER (IF EXISTS)? userName DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*)
    ;

alterUserEntry
    : userName userAuthOption?
    ;

alterUserList
    : alterUserEntry (COMMA_ alterUserEntry)*
    ;

dropUser
    : DROP USER (IF EXISTS)? userName (COMMA_ userName)*
    ;

createRole
    : CREATE ROLE (IF NOT EXISTS)? roleName (COMMA_ roleName)*
    ;

dropRole
    : DROP ROLE (IF EXISTS)? roleName (COMMA_ roleName)*
    ;

renameUser
    : RENAME USER userName TO userName (COMMA_ userName TO userName)*
    ;

setDefaultRole
    : SET DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*) TO userName (COMMA_ userName)*
    ;

setRole
    : SET ROLE (DEFAULT | NONE | ALL | ALL EXCEPT roles | roles)
    ;

setPassword
    : SET PASSWORD (FOR userName)? authOption (REPLACE string_)? (RETAIN CURRENT PASSWORD)?
    ;

authOption
    : EQ_ stringLiterals | TO RANDOM | EQ_ PASSWORD LP_ stringLiterals RP_
    ;

withGrantOption
    : WITH GRANT OPTION
    ;

userOrRoles
    : userOrRole (COMMA_ userOrRole)*
    ;

roles
    : roleName (COMMA_ roleName)*
    ;

grantAs
    : AS userName withRoles?
    ;

withRoles
    : WITH ROLE (DEFAULT | NONE | ALL | ALL EXCEPT roles | roles)
    ;

userAuthOption
    : identifiedBy
    | identifiedWith
    | DISCARD OLD PASSWORD
    ;

identifiedBy
    : IDENTIFIED BY (string_ | RANDOM PASSWORD) (REPLACE string_)? (RETAIN CURRENT PASSWORD)?
    ;

identifiedWith
    : IDENTIFIED WITH pluginName
    | IDENTIFIED WITH pluginName BY (string_ | RANDOM PASSWORD) (REPLACE stringLiterals)? (RETAIN CURRENT PASSWORD)?
    | IDENTIFIED WITH pluginName AS textStringHash (RETAIN CURRENT PASSWORD)?
    ;

connectOption
    : MAX_QUERIES_PER_HOUR NUMBER_
    | MAX_UPDATES_PER_HOUR NUMBER_
    | MAX_CONNECTIONS_PER_HOUR NUMBER_
    | MAX_USER_CONNECTIONS NUMBER_
    ;

tlsOption
    : CIPHER string_ | ISSUER string_ | SUBJECT string_
    ;

userFuncAuthOption
    : identifiedBy | DISCARD OLD PASSWORD
    ;
