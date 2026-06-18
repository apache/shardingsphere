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
    : GRANT privilegeClause TO grantee (COMMA_ grantee)* (WITH GRANT OPTION)?
    ;

revoke
    : REVOKE (GRANT OPTION FOR)? privilegeClause FROM grantee (COMMA_ grantee)* dropBehaviour
    ;

privilegeClause
    : privileges ON onObjectClause
    ;

privileges
    : privilegeType (COMMA_ privilegeType)*
    ;

privilegeType
    : ALL PRIVILEGES
    | SELECT
    | DELETE
    | INSERT
    | UPDATE
    | REFERENCES
    | USAGE
    ;

grantee
    : objectRecepient | userRecepient
    ;

onObjectClause
    : objectType? privilegeLevel
    ;

objectType
    : TABLE
    | VIEW
    | PROCEDURE
    | FUNCTION
    | PACKAGE
    | GENERATOR
    | SEQUENCE
    | DOMAIN
    | EXCEPTION
    | ROLE
    | CHARACTER SET
    | COLLATION
    | FILTER
    ;

objectRecepient
    : PROCEDURE procedureName
    | FUNCTION functionName
    | PACKAGE packageName
    | TRIGGER triggerName
    | VIEW viewName
    ;

userRecepient
    : USER? identifier
    | ROLE? roleName
    | GROUP identifier
    ;

privilegeLevel
    : tableName
    ;

createRole
    : CREATE ROLE roleName
    ;

createUser
    : CREATE USER login PASSWORD password
    firstNameClause? middleNameClause? lastNameClause?
    activeClause? usingPluginClause?
    tagsAttributeClause? grantAdminRoleClause?
    ;

firstNameClause
    : FIRSTNAME STRING_
    ;

middleNameClause
    : MIDDLENAME STRING_
    ;

lastNameClause
    : LASTNAME STRING_
    ;

activeClause
    : ACTIVE | INACTIVE
    ;

usingPluginClause
    : USING PLUGIN STRING_
    ;

tagsAttributeClause
    : TAGS LP_ attributeClause RP_
    ;

grantAdminRoleClause
    : GRANT ADMIN ROLE
    ;

//createUser
//    : CREATE USER identifier password (userOptions)?
//    ;
//
//alterUser
//    : ALTER USER identifier (password)? (userOptions)?
//    ;
//
//dropUser
//    : DROP username
//    ;
//
//username
//    : USER identifier
//    ;
//
//password
//    : PASSWORD STRING_
//    ;
//
//userOptions
//    : (FIRSTNAME STRING_)? (MIDDLENAME STRING_)? (LASTNAME STRING_)? ((GRANT | REVOKE) ADMIN ROLE)?
//    ;
