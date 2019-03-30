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

grammar PostgreSQLDCLStatement;

import Symbol, Keyword, Literals, PostgreSQLBase, BaseRule;

grant
    : GRANT (privileges_ ON onObjectClause_ | ignoredIdentifiers_)
    ;

revoke
    : REVOKE (GRANT OPTION FOR)? (privileges_ ON onObjectClause_ | ignoredIdentifiers_)
    ;

privileges_
    : privilegeType_ columnNames? (COMMA_ privilegeType_ columnNames?)*
    ;

privilegeType_
    : ALL PRIVILEGES?
    | SELECT
    | INSERT
    | UPDATE
    | DELETE
    | TRUNCATE
    | REFERENCES
    | TRIGGER
    | CREATE
    | CONNECT
    | TEMPORARY
    | TEMP
    | EXECUTE
    | USAGE
    ;

onObjectClause_
    : SEQUENCE
    | DATABASE
    | DOMAIN
    | FOREIGN
    | FUNCTION
    | PROCEDURE
    | ROUTINE
    | ALL
    | LANGUAGE
    | LARGE OBJECT
    | SCHEMA
    | TABLESPACE
    | TYPE
    | TABLE? tableName (COMMA_ tableName)*
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

createRole
    : CREATE ROLE
    ;

dropRole
    : DROP ROLE
    ;

alterRole
    : ALTER ROLE
    ;
