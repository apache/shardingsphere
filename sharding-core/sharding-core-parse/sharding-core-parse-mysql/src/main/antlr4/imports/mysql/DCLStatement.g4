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

import Symbol, Keyword, Literals, BaseRule;

grant
    : GRANT (PROXY ON | privileges_ ON onObjectClause_ | ignoredIdentifiers_)
    ;

revoke
    : REVOKE (ALL PRIVILEGES? COMMA_ GRANT OPTION | PROXY ON | privileges_ ON onObjectClause_ | ignoredIdentifiers_)
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
    | AUDIT_ADMIN
    | BINLOG_ADMIN
    | CONNECTION_ADMIN
    | ENCRYPTION_KEY_ADMIN
    | FIREWALL_ADMIN
    | FIREWALL_USER
    | GROUP_REPLICATION_ADMIN
    | REPLICATION_SLAVE_ADMIN
    | ROLE_ADMIN
    | SET_USER_ID
    | SYSTEM_VARIABLES_ADMIN
    | VERSION_TOKEN_ADMIN
    ;

onObjectClause_
    : objectType_? privilegeLevel_
    ;

objectType_
    : TABLE | FUNCTION | PROCEDURE
    ;

privilegeLevel_
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | identifier_ DOT_ASTERISK_ | tableName
    ;

createUser
    : CREATE USER
    ;

dropUser
    : DROP USER
    ;

alterUser
    : ALTER USER
    ;

renameUser
    : RENAME USER
    ;

createRole
    : CREATE ROLE
    ;

dropRole
    : DROP ROLE
    ;

setRole
    : SET DEFAULT? ROLE
    ;

setPassword
    : SET PASSWORD
    ;
