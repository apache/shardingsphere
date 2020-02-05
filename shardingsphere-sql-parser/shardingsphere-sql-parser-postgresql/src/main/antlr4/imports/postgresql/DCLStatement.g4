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

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule;

grant
    : GRANT (privilegeClause_ | roleClause_)
    ;

revoke
    : REVOKE optionForClause_? (privilegeClause_ | roleClause_)
    ;

privilegeClause_
    : privileges_ ON onObjectClause_
    ;
    
roleClause_
    : ignoredIdentifiers_
    ;

optionForClause_
    : (GRANT | ADMIN) OPTION FOR
    ;

privileges_
    : privilegeType_ columnNames? (COMMA_ privilegeType_ columnNames?)*
    ;

privilegeType_
    : SELECT
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
    | ALL PRIVILEGES?
    ;

onObjectClause_
    : DATABASE 
    | SCHEMA
    | DOMAIN
    | FOREIGN
    | FUNCTION
    | PROCEDURE
    | ROUTINE
    | ALL
    | LANGUAGE
    | LARGE OBJECT
    | TABLESPACE
    | TYPE 
    | SEQUENCE
    | TABLE? tableNames
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
