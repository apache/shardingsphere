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

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule, DDLStatement;

grant
    : GRANT (privilegeClause | roleClause)
    ;

revoke
    : REVOKE optionForClause? (privilegeClause | roleClause) (CASCADE | RESTRICT)?
    ;

privilegeClause
    : privileges ON onObjectClause (FROM | TO) granteeList (WITH GRANT OPTION)?
    ;
    
roleClause
    : privilegeList (FROM | TO) roleList (WITH ADMIN OPTION)? (GRANTED BY roleSpec)?
    ;

optionForClause
    : (GRANT | ADMIN) OPTION FOR
    ;

privileges
    : privilegeType columnNames? (COMMA_ privilegeType columnNames?)*
    ;

privilegeType
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
    : DATABASE nameList
    | SCHEMA nameList
    | DOMAIN anyNameList
    | FUNCTION functionWithArgtypesList
    | PROCEDURE functionWithArgtypesList
    | ROUTINE functionWithArgtypesList
    | LANGUAGE nameList
    | LARGE OBJECT numericOnlyList
    | TABLESPACE nameList
    | TYPE anyNameList
    | SEQUENCE qualifiedNameList
    | TABLE? privilegeLevel
    | FOREIGN DATA WRAPPER nameList
    | FOREIGN SERVER nameList
    | ALL TABLES IN SCHEMA nameList
    | ALL SEQUENCES IN SCHEMA nameList
    | ALL FUNCTIONS IN SCHEMA nameList
    | ALL PROCEDURES IN SCHEMA nameList
    | ALL ROUTINES IN SCHEMA nameList
    ;

privilegeLevel
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | identifier DOT_ASTERISK_ | tableNames | schemaName DOT_ routineName
    ;

routineName
    : identifier
    ;

numericOnlyList
    : numericOnly (COMMA_ numericOnly)*
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

createGroup
    : CREATE GROUP roleSpec WITH? createOptRoleElem*
    ;

createSchema
    : CREATE SCHEMA (IF NOT EXISTS)? createSchemaClauses
    ;

createSchemaClauses
    : colId? AUTHORIZATION roleSpec schemaEltList
    | colId schemaEltList
    ;

schemaEltList
    : schemaStmt*
    ;

schemaStmt
    : createTable | createIndex | createSequence | createTrigger | grant | createView
    ;

dropDroup
    : DROP GROUP (IF EXISTS)? roleList
    ;

dropSchema
    : DROP SCHEMA (IF EXISTS)? nameList dropBehavior?
    ;

reassignOwned
    : REASSIGN OWNED BY roleList TO roleSpec
    ;

