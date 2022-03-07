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
    : GRANT (grantClassPrivilegesClause | grantClassTypePrivilegesClause)
    ;

grantClassPrivilegesClause
    : grantClassPrivileges (ON grantOnClassClause)? TO principal (COMMA_ principal)* (WITH GRANT OPTION)? (AS principal)?
    ;

grantClassTypePrivilegesClause
    : grantClassTypePrivileges (ON grantOnClassTypeClause)? TO principal (COMMA_ principal)* (WITH GRANT OPTION)?
    ;

grantClassPrivileges
    : privilegeType columnNames? (COMMA_ privilegeType columnNames?)*
    ;

grantOnClassClause
    : (classItem COLON_ COLON_)? securable
    ;

grantClassTypePrivileges
    : privilegeType (COMMA_ privilegeType)*
    ;

grantOnClassTypeClause
    : (grantClassType COLON_ COLON_)? securable
    ;

securable
    : (owner DOT_)? name
    ;

principal
    : userName
    ;

revoke
    : REVOKE (optionForClause? classPrivilegesClause | classTypePrivilegesClause | roleClause)
    ;

deny
    : DENY (classPrivilegesClause | classTypePrivilegesClause)
    ;

classPrivilegesClause
    : classPrivileges (ON onClassClause)?
    ;

classTypePrivilegesClause
    : classTypePrivileges (ON onClassTypeClause)?
    ;

optionForClause
    : GRANT OPTION FOR
    ;

classPrivileges
    : privilegeType columnNames? (COMMA_ privilegeType columnNames?)*
    ;

onClassClause
    : class_? tableName
    ;

classTypePrivileges
    : privilegeType (COMMA_ privilegeType)*
    ;

onClassTypeClause
    : classType? tableName
    ;

privilegeType
    : ALL PRIVILEGES?
    | assemblyPermission | asymmetricKeyPermission
    | availabilityGroupPermission | certificatePermission
    | objectPermission
    | databasePermission | databasePrincipalPermission
    | databaseScopedCredentialPermission | endpointPermission
    | fullTextPermission
    | schemaPermission | searchPropertyListPermission
    | serverPermission | serverPrincipalPermission
    | serviceBrokerPermission | symmetricKeyPermission
    | typePermission | xmlSchemaCollectionPermission
    ;

objectPermission
    : ALTER | CONTROL | DELETE | EXECUTE | INSERT | RECEIVE | REFERENCES | SELECT | TAKE OWNERSHIP | UPDATE
    | VIEW CHANGE TRACKING | VIEW DEFINITION
    ;

serverPermission
    : ADMINISTER BULK OPERATIONS | ALTER (RESOURCES | SETTINGS | TRACE | SERVER STATE)
    | ALTER ANY (AVAILABILITY GROUP | CONNECTION | CREDENTIAL | DATABASE | ENDPOINT | EVENT NOTIFICATION | EVENT SESSION | LINKED SERVER | LOGIN | SERVER AUDIT | SERVER ROLE)
    | AUTHENTICATE SERVER | CONNECT ANY DATABASE | CONNECT SQL | CONTROL SERVER | CREATE ANY DATABASE
    | CREATE (AVAILABILITY GROUP | DDL EVENT NOTIFICATION | ENDPOINT | SERVER ROLE | TRACE EVENT NOTIFICATION)
    | EXTERNAL ACCESS ASSEMBLY | IMPERSONATE ANY LOGIN | SELECT ALL USER SECURABLES | SHUTDOWN | UNSAFE ASSEMBLY
    | VIEW ANY (DATABASE | DEFINITION) | VIEW SERVER STATE
    ;

serverPrincipalPermission
    : CONTROL SERVER? | IMPERSONATE | VIEW ANY? DEFINITION | ALTER | ALTER ANY (LOGIN | SERVER ROLE)
    ;

databasePermission
    : ADMINISTER DATABASE BULK OPERATIONS | ALTER | ALTER TRACE
    | ALTER ANY (APPLICATION ROLE | ASSEMBLY | (SYMMETRIC | ASYMMETRIC | COLUMN ENCRYPTION) KEY | CERTIFICATE
    | CONNECTION | COLUMN MASTER KEY DEFINITION | CONTRACT
    | DATABASE (AUDIT | DDL TRIGGER | EVENT NOTIFICATION | EVENT SESSION | SCOPED CONFIGURATION)?
    | DATASPACE | EVENT (NOTIFICATION | SESSION) | EXTERNAL (DATA SOURCE | FILE FORMAT | LIBRARY) | FULLTEXT CATALOG | MASK | MESSAGE TYPE
    | REMOTE SERVICE BINDING | ROLE | ROUTE | SERVER AUDIT | SCHEMA | SECURITY POLICY | SERVICE | USER)
    | AUTHENTICATE SERVER? | BACKUP (DATABASE | LOG) | CHECKPOINT | CONNECT | CONNECT REPLICATION? | CONTROL SERVER?
    | CREATE (AGGREGATE | ASSEMBLY | (SYMMETRIC | ASYMMETRIC) KEY | CERTIFICATE | CONTRACT | DATABASE | DATABASE? DDL EVENT NOTIFICATION
    | DEFAULT | FULLTEXT CATALOG | FUNCTION | MESSAGE TYPE | PROCEDURE | QUEUE | REMOTE SERVICE BINDING | ROLE | ROUTE | RULE | SCHEMA
    | SERVICE | SYNONYM | TABLE | TYPE | VIEW | XML SCHEMA COLLECTION)
    | DELETE | EXECUTE | EXECUTE ANY? EXTERNAL SCRIPT | INSERT | KILL DATABASE CONNECTION | REFERENCES | SELECT | SHOWPLAN | SUBSCRIBE QUERY NOTIFICATIONS
    | TAKE OWNERSHIP | UNMASK | UPDATE
    | VIEW ANY COLUMN (MASTER | ENCRYPTION) KEY DEFINITION
    | CREATE ANY (DATABASE | EXTERNAL LIBRARY)
    | VIEW (DATABASE | SERVER) STATE | VIEW ANY? DEFINITION
    | 
    ;

databasePrincipalPermission
    : databaseUserPermission | databaseRolePermission | applicationRolePermission
    ;

databaseUserPermission
    : CONTROL | IMPERSONATE | ALTER | VIEW DEFINITION | ALTER ANY USER
    ;

databaseRolePermission
    : CONTROL | TAKE OWNERSHIP | ALTER | VIEW DEFINITION | ALTER ANY ROLE
    ;

applicationRolePermission
    : CONTROL | ALTER | VIEW DEFINITION | ALTER ANY APPLICATION ROLE
    ;

databaseScopedCredentialPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION
    ;

schemaPermission
    : ALTER | CONTROL | CREATE SEQUENCE | DELETE | EXECUTE | INSERT | REFERENCES | SELECT | TAKE OWNERSHIP
    | UPDATE | VIEW CHANGE TRACKING | VIEW DEFINITION | ALTER ANY SCHEMA
    ;

searchPropertyListPermission
    : ALTER | CONTROL | REFERENCES | TAKE OWNERSHIP | VIEW DEFINITION | ALTER ANY FULLTEXT CATALOG
    ;

serviceBrokerPermission
    : serviceBrokerContractsPermission | serviceBrokerMessageTypesPermission | serviceBrokerRemoteServiceBindingsPermission
    | serviceBrokerRoutesPermission | serviceBrokerServicesPermission
    ;

serviceBrokerContractsPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION | ALTER ANY CONTRACT
    ;

serviceBrokerMessageTypesPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION | ALTER ANY MESSAGE TYPE
    ;

serviceBrokerRemoteServiceBindingsPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | VIEW DEFINITION | ALTER ANY REMOTE SERVICE BINDING
    ;

serviceBrokerRoutesPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | VIEW DEFINITION | ALTER ANY ROUTE
    ;

serviceBrokerServicesPermission
    : CONTROL | TAKE OWNERSHIP | SEND | ALTER | VIEW DEFINITION | ALTER ANY SERVICE
    ;

endpointPermission
    : ALTER | CONNECT | CONTROL SERVER? | TAKE OWNERSHIP | VIEW ANY? DEFINITION | ALTER ANY ENDPOINT
    ;

certificatePermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION | ALTER ANY CERTIFICATE
    ;

symmetricKeyPermission
    : ALTER | CONTROL | REFERENCES | TAKE OWNERSHIP | VIEW DEFINITION | ALTER ANY SYMMETRIC KEY
    ;

asymmetricKeyPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION | ALTER ANY ASYMMETRIC KEY
    ;

assemblyPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION | ALTER ANY ASSEMBLY
    ;

availabilityGroupPermission
    : ALTER | CONNECT | CONTROL SERVER? | TAKE OWNERSHIP | VIEW ANY? DEFINITION | ALTER ANY AVAILABILITY GROUP
    ;

fullTextPermission
    : fullTextCatalogPermission | fullTextStoplistPermission
    ;

fullTextCatalogPermission
    : CONTROL | TAKE OWNERSHIP | ALTER | REFERENCES | VIEW DEFINITION | ALTER ANY FULLTEXT CATALOG
    ;

fullTextStoplistPermission
    : ALTER | CONTROL | REFERENCES | TAKE OWNERSHIP | VIEW DEFINITION | ALTER ANY FULLTEXT CATALOG
    ;

typePermission
    : CONTROL | EXECUTE | REFERENCES | TAKE OWNERSHIP | VIEW DEFINITION
    ;

xmlSchemaCollectionPermission
    : ALTER | CONTROL | EXECUTE | REFERENCES | TAKE OWNERSHIP | VIEW DEFINITION
    ;

class_
    : IDENTIFIER_ COLON_ COLON_
    ;

classType
    : (LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER) COLON_ COLON_
    ;

classItem
    : ASSEMBLY | ASYMMETRIC KEY | AVAILABILITY GROUP | CERTIFICATE | USER | ROLE | APPLICATION ROLE
    | DATABASE SCOPED CREDENTIAL | ENDPOINT | FULLTEXT (CATALOG | STOPLIST) | OBJECT | SCHEMA | SEARCH PROPERTY LIST
    | LOGIN | SERVER ROLE | CONTRACT | MESSAGE TYPE | REMOTE SERVICE BINDING | ROUTE | SERVICE | SYMMETRIC KEY
    | SELECT | EXECUTE | TYPE | XML SCHEMA COLLECTION
    ;

grantClassType
    : LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER
    ;

roleClause
    : ignoredIdentifiers
    ;

setUser
    : SETUSER (stringLiterals (WITH NORESET)?)?
    ;

createUser
    : CREATE USER
    (createUserLoginClause 
    | createUserWindowsPrincipalClause 
    | createUserLoginWindowsPrincipalClause
    | createUserWithoutLoginClause
    | createUserFromExternalProviderClause
    | createUserWithDefaultSchema
    | createUserWithAzureActiveDirectoryPrincipalClause
    | userName)?
    ;

createUserLoginClause
    : userName ((FOR | FROM) LOGIN identifier)? (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    ;

createUserWindowsPrincipalClause
    : windowsPrincipal (WITH optionsList (COMMA_ optionsList)*)?
    | userName WITH PASSWORD EQ_ stringLiterals (COMMA_ optionsList (COMMA_ optionsList)*)?
    | azureActiveDirectoryPrincipal FROM EXTERNAL PROVIDER
    ;

createUserLoginWindowsPrincipalClause
    : ((windowsPrincipal ((FOR | FROM) LOGIN windowsPrincipal)?) | (userName (FOR | FROM) LOGIN windowsPrincipal))
    (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    ;

createUserWithoutLoginClause
    : userName (WITHOUT LOGIN (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    | (FOR | FROM) CERTIFICATE identifier
    | (FOR | FROM) ASYMMETRIC KEY identifier)
    ;

optionsList
    : DEFAULT_SCHEMA EQ_ schemaName
    | DEFAULT_LANGUAGE EQ_ (NONE | identifier)
    | SID EQ_ (NCHAR_TEXT | HEX_DIGIT_)
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
    ;

limitedOptionsList
    : DEFAULT_SCHEMA EQ_ schemaName
    | DEFAULT_LANGUAGE EQ_ (NONE | identifier)
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
    ;

createUserFromExternalProviderClause
    : userName ((FOR | FROM) LOGIN identifier)? | FROM EXTERNAL PROVIDER (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    ;

createUserWithDefaultSchema
    : userName ((FOR | FROM) LOGIN identifier | WITHOUT LOGIN)? (WITH DEFAULT_SCHEMA EQ_ schemaName)?
    ;

createUserWithAzureActiveDirectoryPrincipalClause
    : azureActiveDirectoryPrincipal FROM EXTERNAL PROVIDER (WITH DEFAULT_SCHEMA EQ_ schemaName)?
    ;

windowsPrincipal
    : userName
    ;

azureActiveDirectoryPrincipal
    : userName
    ;

userName
    : ignoredNameIdentifier
    ;

ignoredNameIdentifier
    : identifier (DOT_ identifier)?
    ;

dropUser
    : DROP USER
    ;

alterUser
    : ALTER USER userName (WITH setItem (COMMA_ setItem)* | FROM EXTERNAL PROVIDER)
    ;

setItem
    : NAME EQ_ userName
    | DEFAULT_SCHEMA EQ_ (schemaName | NULL)
    | LOGIN EQ_ identifier
    | PASSWORD EQ_ stringLiterals (OLD_PASSWORD EQ_ stringLiterals)?
    | DEFAULT_LANGUAGE EQ_ (NONE | identifier)
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
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

createLogin
    : CREATE LOGIN
    ;

dropLogin
    : DROP LOGIN
    ;

alterLogin
    : ALTER LOGIN
    ;
