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
    : GRANT (proxyClause_ | privilegeClause | roleClause_)
    ;

revoke
    : REVOKE (proxyClause_ | privilegeClause | allClause_ | roleClause_)
    ;

proxyClause_
    : PROXY ON userOrRole TO userOrRoles_ withGrantOption_?
    ;

privilegeClause
    : privileges_ ON onObjectClause (TO | FROM) userOrRoles_ withGrantOption_? grantOption_?
    ;

roleClause_
    : roles_ ( TO| FROM) userOrRoles_ withGrantOption_?
    ;

allClause_
    : ALL PRIVILEGES? COMMA_ GRANT OPTION FROM userOrRoles_
    ;

privileges_
    : privilegeType_ columnNames? (COMMA_ privilegeType_ columnNames?)*
    ;

privilegeType_
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
    : objectType_? privilegeLevel
    ;

objectType_
    : TABLE | FUNCTION | PROCEDURE
    ;

privilegeLevel
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | identifier DOT_ASTERISK_ | tableName  | schemaName DOT_ routineName
    ;

createUser
    : CREATE USER (IF NOT EXISTS)? userName userAuthOption_? (COMMA_ userName userAuthOption_?)*
    defaultRoleClause? requireClause? connectOption? accountLockPasswordExpireOptions?
    ;

defaultRoleClause
    : DEFAULT ROLE roleName (COMMA_ roleName)*
    ;

requireClause
    : REQUIRE (NONE | tlsOption_ (AND? tlsOption_)*)
    ;

connectOption
    : WITH resourceOption_ resourceOption_*
    ;

accountLockPasswordExpireOptions
    : accountLockPasswordExpireOption+
    ;

accountLockPasswordExpireOption
    : passwordOption_ | lockOption_
    ;

alterUser
    : ALTER USER (IF EXISTS)? userName userAuthOption_? (COMMA_ userName userAuthOption_?)*
    (REQUIRE (NONE | tlsOption_ (AND? tlsOption_)*))? (WITH resourceOption_ resourceOption_*)? (passwordOption_ | lockOption_)*
    | ALTER USER (IF EXISTS)? USER LP_ RP_ userFuncAuthOption_
    | ALTER USER (IF EXISTS)? userName DEFAULT ROLE (NONE | ALL | roleName (COMMA_ roleName)*)
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
    : SET ROLE (DEFAULT | NONE | ALL | ALL EXCEPT roles_ | roles_)
    ;

setPassword
    : SET PASSWORD (FOR userName)? authOption_ (REPLACE STRING_)? (RETAIN CURRENT PASSWORD)?
    ;

authOption_
    : EQ_ stringLiterals | TO RANDOM
    ;

withGrantOption_
    : WITH GRANT OPTION
    ;

userOrRoles_
    : userOrRole (COMMA_ userOrRole)*
    ;

roles_
    : roleName (COMMA_ roleName)*
    ;

grantOption_
    : AS userName (WITH ROLE DEFAULT | NONE | ALL | ALL EXCEPT roles_ | roles_ )?
    ;

userAuthOption_
    : identifiedBy_
    | identifiedWith_
    | DISCARD OLD PASSWORD
    ;

identifiedBy_
    : IDENTIFIED BY (STRING_ | RANDOM PASSWORD) (REPLACE STRING_)? (RETAIN CURRENT PASSWORD)?
    ;

identifiedWith_
    : IDENTIFIED WITH pluginName (BY |AS) (STRING_ | RANDOM PASSWORD)
      (REPLACE stringLiterals)? (RETAIN CURRENT PASSWORD)?
    ;

lockOption_
    : ACCOUNT LOCK | ACCOUNT UNLOCK
    ;


passwordOption_
    : PASSWORD EXPIRE (DEFAULT | NEVER | INTERVAL NUMBER_ DAY)?
    | PASSWORD HISTORY (DEFAULT | NUMBER_)
    | PASSWORD REUSE INTERVAL (DEFAULT | NUMBER_ DAY)
    | PASSWORD REQUIRE CURRENT (DEFAULT | OPTIONAL)?
    | FAILED_LOGIN_ATTEMPTS NUMBER_
    | PASSWORD_LOCK_TIME (NUMBER_ | UNBOUNDED)
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
    : identifiedBy_ | DISCARD OLD PASSWORD
    ;
