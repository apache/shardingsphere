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
    : GRANT (proxyClause | privilegeClause | roleClause)
    ;

revoke
    : REVOKE (proxyClause | privilegeClause | allClause | roleClause)
    ;

proxyClause
    : PROXY ON userOrRole TO userOrRoles withGrantOption?
    ;

privilegeClause
    : privileges ON onObjectClause (TO | FROM) userOrRoles withGrantOption? grantOption?
    ;

roleClause
    : roles ( TO| FROM) userOrRoles withGrantOption?
    ;

allClause
    : ALL PRIVILEGES? COMMA_ GRANT OPTION FROM userOrRoles
    ;

privileges
    : privilege (COMMA_ privilege)*
    ;

privilege
    : privilegeType (LP_ columnNames RP_)?
    ;

privilegeType
    : ALL PRIVILEGES?
    | ALTER ROUTINE?
    | CREATE
    | CREATE ROUTINE
    | CREATE TABLESPACE
    | CREATE TEMPORARY TABLES
    | CREATE USER
    | CREATE VIEW
    | DELETE
    | DROP
    | DROP ROLE
    | EVENT
    | EXECUTE
    | FILE
    | GRANT OPTION
    | INDEX
    | INSERT
    | LOCK TABLES
    | PROCESS
    | PROXY
    | REFERENCES
    | RELOAD
    | REPLICATION CLIENT
    | REPLICATION SLAVE
    | SELECT
    | SHOW DATABASES
    | SHOW VIEW
    | SHUTDOWN
    | SUPER
    | TRIGGER
    | UPDATE
    | USAGE
    | identifier
    ;

onObjectClause
    : objectType? privilegeLevel
    ;

objectType
    : TABLE | FUNCTION | PROCEDURE
    ;

privilegeLevel
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | identifier DOT_ASTERISK_ | tableName  | schemaName DOT_ routineName
    ;

createUser
    : CREATE USER (IF NOT EXISTS)? alterUserList defaultRoleClause? requireClause? connectOptions? accountLockPasswordExpireOptions?
    ;

defaultRoleClause
    : DEFAULT ROLE roleName (COMMA_ roleName)*
    ;

requireClause
    : REQUIRE (NONE | tlsOption (AND? tlsOption)*)
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

grantOption
    : AS userName (WITH ROLE DEFAULT | NONE | ALL | ALL EXCEPT roles | roles )?
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
    : SSL | X509 | CIPHER string_ | ISSUER string_ | SUBJECT string_
    ;

userFuncAuthOption
    : identifiedBy | DISCARD OLD PASSWORD
    ;
