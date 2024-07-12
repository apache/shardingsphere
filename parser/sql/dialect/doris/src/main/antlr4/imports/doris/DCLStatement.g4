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

import BaseRule;

grant
    : GRANT roleOrPrivileges TO userList withGrantOption? # grantRoleOrPrivilegeTo
    | GRANT roleOrPrivileges ON aclType? grantIdentifier TO userList withGrantOption? grantAs? # grantRoleOrPrivilegeOnTo
    | GRANT ALL PRIVILEGES? ON aclType? grantIdentifier TO userList withGrantOption? grantAs? # grantRoleOrPrivilegeOnTo
    | GRANT PROXY ON username TO userList withGrantOption? # grantProxy
    ;

revoke
    : REVOKE roleOrPrivileges FROM userList # revokeFrom
    | REVOKE roleOrPrivileges ON aclType? grantIdentifier FROM userList # revokeOnFrom
    | REVOKE ALL PRIVILEGES? ON aclType? grantIdentifier FROM userList # revokeOnFrom
    | REVOKE ALL PRIVILEGES? COMMA_ GRANT OPTION FROM userList # revokeFrom
    | REVOKE PROXY ON username FROM userList # revokeOnFrom
    ;

userList
    : username (COMMA_ username)*
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
    | databaseName DOT_ASTERISK_ # grantLevelDatabaseGlobal
    | tableName # grantLevelTable
    ;

createUser
    : CREATE USER ifNotExists? createUserList defaultRoleClause? requireClause? connectOptions? accountLockPasswordExpireOptions? createUserOption? (AND userAuthOption)*
    ;

createUserOption
    : COMMENT string_ | ATTRIBUTE jsonAttribute = string_
    ;

createUserEntry
    : username # createUserEntryNoOption
    | username IDENTIFIED BY string_ # createUserEntryIdentifiedBy
    | username IDENTIFIED BY RANDOM PASSWORD # createUserEntryIdentifiedBy
    | username IDENTIFIED WITH textOrIdentifier # createUserEntryIdentifiedWith
    | username IDENTIFIED WITH textOrIdentifier AS string_ # createUserEntryIdentifiedWith
    | username IDENTIFIED WITH textOrIdentifier BY string_ # createUserEntryIdentifiedWith
    | username IDENTIFIED WITH textOrIdentifier BY RANDOM PASSWORD # createUserEntryIdentifiedWith
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
    : ALTER USER ifExists? alterUserList requireClause? connectOptions? accountLockPasswordExpireOptions? alterOperation?
    | ALTER USER ifExists? USER LP_ RP_ userFuncAuthOption
    | ALTER USER ifExists? username DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*)
    ;

alterUserEntry
    : username userAuthOption?
    ;

alterUserList
    : alterUserEntry (COMMA_ alterUserEntry)*
    ;

alterOperation
    : (ADD | MODIFY | DROP | SET) factoryOperation
    ;

factoryOperation
    : NUMBER_ FACTOR (IDENTIFIED WITH authentication_fido)?
    ;

authentication_fido
    : AUTHENTICATION_FIDO
    ;

dropUser
    : DROP USER ifExists? username (COMMA_ username)*
    ;

createRole
    : CREATE ROLE ifNotExists? roleName (COMMA_ roleName)*
    ;

dropRole
    : DROP ROLE ifExists? roleName (COMMA_ roleName)*
    ;

renameUser
    : RENAME USER username TO username (COMMA_ username TO username)*
    ;

setDefaultRole
    : SET DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*) TO username (COMMA_ username)*
    ;

setRole
    : SET ROLE (DEFAULT | NONE | ALL | ALL EXCEPT roles | roles)
    ;

setPassword
    : SET PASSWORD (FOR username)? authOption (REPLACE string_)? (RETAIN CURRENT PASSWORD)?
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
    : AS username withRoles?
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
    : IDENTIFIED WITH (pluginName | authentication_fido)
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
