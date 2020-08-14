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
    : GRANT (privilegeClause | roleClause_)
    ;

revoke
    : REVOKE optionForClause_? (privilegeClause | roleClause_)
    ;

privilegeClause
    : privileges_ ON onObjectClause
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

onObjectClause
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
    : CREATE USER roleSpec WITH? createOptRoleElem*
    ;

createOptRoleElem
    : alterOptRoleElem
    | SYSID NUMBER_
    | ADMIN roleList
    | ROLE roleList
    | IN ROLE roleList
    | IN GROUP roleList
    ;

alterOptRoleElem
    : PASSWORD STRING_
    | PASSWORD NULL
    | ENCRYPTED PASSWORD STRING_
    | UNENCRYPTED PASSWORD STRING_
    | INHERIT
    | CONNECTION LIMIT signedIconst
    | VALID UNTIL STRING_
    | USER roleList
    | identifier
    ;

dropUser
    : DROP USER (IF EXISTS)? roleList
    ;

alterUser
    : ALTER USER alterUserClauses
    ;

alterUserClauses
    : roleSpec WITH? alterOptRoleList
    | roleSpec (IN DATABASE name)? setResetClause
    | ALL (IN DATABASE name)? setResetClause
    | roleSpec RENAME TO roleSpec
    ;

alterOptRoleList
    : alterOptRoleElem*
    ;

createRole
    : CREATE ROLE roleSpec WITH? createOptRoleElem*
    ;

dropRole
    : DROP ROLE (IF EXISTS)? roleList
    ;

alterRole
    : ALTER ROLE alterUserClauses
    ;

alterSchema
    : ALTER SCHEMA name (RENAME TO name | OWNER TO roleSpec)
    ;

