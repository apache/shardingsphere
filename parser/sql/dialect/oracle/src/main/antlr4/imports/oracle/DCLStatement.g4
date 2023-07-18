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
    : GRANT (objectPrivilegeClause | systemPrivilegeClause | roleClause)
    ;

revoke
    : REVOKE (objectPrivilegeClause | systemPrivilegeClause | roleClause)
    ;

objectPrivilegeClause
    : objectPrivileges ON onObjectClause
    ;

systemPrivilegeClause
    : systemPrivilege
    ;
    
roleClause
    : ignoredIdentifiers
    ;

objectPrivileges
    : objectPrivilegeType columnNames? (COMMA_ objectPrivilegeType columnNames?)*
    ;

objectPrivilegeType
    : ALL PRIVILEGES?
    | SELECT
    | INSERT
    | DELETE
    | UPDATE
    | ALTER
    | READ
    | WRITE
    | EXECUTE
    | USE
    | INDEX
    | REFERENCES
    | DEBUG
    | UNDER
    | FLASHBACK ARCHIVE
    | ON COMMIT REFRESH
    | QUERY REWRITE
    | KEEP SEQUENCE
    | INHERIT PRIVILEGES
    | TRANSLATE SQL
    | MERGE VIEW
    ;

onObjectClause
    : USER | DIRECTORY | EDITION | MINING MODEL | SQL TRANSLATION PROFILE
    | JAVA (SOURCE | RESOURCE) tableName
    | tableName
    ;

systemPrivilege
    : ALL PRIVILEGES
    | advisorFrameworkSystemPrivilege
    | clustersSystemPrivilege
    | contextsSystemPrivilege
    | dataRedactionSystemPrivilege
    | databaseSystemPrivilege
    | databaseLinksSystemPrivilege
    | debuggingSystemPrivilege
    | dictionariesSystemPrivilege
    | dimensionsSystemPrivilege
    | directoriesSystemPrivilege
    | editionsSystemPrivilege
    | flashbackDataArchivesPrivilege
    | indexesSystemPrivilege
    | indexTypesSystemPrivilege
    | jobSchedulerObjectsSystemPrivilege
    | keyManagementFrameworkSystemPrivilege
    | librariesFrameworkSystemPrivilege
    | logminerFrameworkSystemPrivilege
    | materizlizedViewsSystemPrivilege
    | miningModelsSystemPrivilege
    | olapCubesSystemPrivilege
    | olapCubeMeasureFoldersSystemPrivilege
    | olapCubeDiminsionsSystemPrivilege
    | olapCubeBuildProcessesSystemPrivilege
    | operatorsSystemPrivilege
    | outlinesSystemPrivilege
    | planManagementSystemPrivilege
    | pluggableDatabasesSystemPrivilege
    | proceduresSystemPrivilege
    | profilesSystemPrivilege
    | rolesSystemPrivilege
    | rollbackSegmentsSystemPrivilege
    | sequencesSystemPrivilege
    | sessionsSystemPrivilege
    | sqlTranslationProfilesSystemPrivilege
    | synonymsSystemPrivilege
    | tablesSystemPrivilege
    | tablespacesSystemPrivilege
    | triggersSystemPrivilege
    | typesSystemPrivilege
    | usersSystemPrivilege
    | viewsSystemPrivilege
    | miscellaneousSystemPrivilege
    ;

systemPrivilegeOperation
    : (CREATE | ALTER | DROP | SELECT | INSERT | UPDATE | DELETE | EXECUTE) ANY?
    ;

advisorFrameworkSystemPrivilege
    : systemPrivilegeOperation? SQL PROFILE | ADVISOR | ADMINISTER ANY? SQL (TUNING SET | MANAGEMENT OBJECT)
    ;

clustersSystemPrivilege
    : systemPrivilegeOperation CLUSTER
    ;

contextsSystemPrivilege
    : systemPrivilegeOperation CONTEXT
    ;

dataRedactionSystemPrivilege
    : EXEMPT REDACTION POLICY
    ;

databaseSystemPrivilege
    : ALTER (DATABASE | SYSTEM) | AUDIT SYSTEM
    ;

databaseLinksSystemPrivilege
    : (CREATE | ALTER | DROP) PUBLIC? DATABASE LINK
    ;

debuggingSystemPrivilege
    : DEBUG (CONNECT SESSION | ANY PROCEDURE)
    ;

dictionariesSystemPrivilege
    : ANALYZE ANY DICTIONARY
    ;

dimensionsSystemPrivilege
    : systemPrivilegeOperation DIMENSION
    ;

directoriesSystemPrivilege
    : systemPrivilegeOperation DIRECTORY
    ;

editionsSystemPrivilege
    : systemPrivilegeOperation EDITION
    ;

flashbackDataArchivesPrivilege
    : FLASHBACK ARCHIVE ADMINISTER
    ;

indexesSystemPrivilege
    : systemPrivilegeOperation INDEX
    ;

indexTypesSystemPrivilege
    : systemPrivilegeOperation INDEXTYPE
    ;

jobSchedulerObjectsSystemPrivilege
    : CREATE (ANY | EXTERNAL)? JOB | EXECUTE ANY (CLASS | PROGRAM) | MANAGE SCHEDULER
    ;

keyManagementFrameworkSystemPrivilege
    : ADMINISTER KEY MANAGEMENT
    ;

librariesFrameworkSystemPrivilege
    : systemPrivilegeOperation LIBRARY
    ;

logminerFrameworkSystemPrivilege
    : LOGMINING
    ;

materizlizedViewsSystemPrivilege
    : systemPrivilegeOperation MATERIALIZED VIEW | GLOBAL? QUERY REWRITE | ON COMMIT REFRESH | FLASHBACK ANY TABLE
    ;

miningModelsSystemPrivilege
    : (systemPrivilegeOperation | COMMENT ANY) MINING MODEL
    ;

olapCubesSystemPrivilege
    : systemPrivilegeOperation CUBE
    ;

olapCubeMeasureFoldersSystemPrivilege
    : systemPrivilegeOperation MEASURE FOLDER
    ;

olapCubeDiminsionsSystemPrivilege
    : systemPrivilegeOperation CUBE DIMENSION
    ;

olapCubeBuildProcessesSystemPrivilege
    : systemPrivilegeOperation CUBE BUILD PROCESS
    ;

operatorsSystemPrivilege
    : systemPrivilegeOperation OPERATOR
    ;

outlinesSystemPrivilege
    : systemPrivilegeOperation OUTLINE
    ;

planManagementSystemPrivilege
    : ADMINISTER SQL MANAGEMENT OBJECT
    ;

pluggableDatabasesSystemPrivilege
    : CREATE PLUGGABLE DATABASE | SET CONTAINER
    ;

proceduresSystemPrivilege
    : systemPrivilegeOperation PROCEDURE 
    ;

profilesSystemPrivilege
    : systemPrivilegeOperation PROFILE 
    ;

rolesSystemPrivilege
    : (systemPrivilegeOperation | GRANT ANY) ROLE 
    ;

rollbackSegmentsSystemPrivilege
    : systemPrivilegeOperation ROLLBACK SEGMENT 
    ;

sequencesSystemPrivilege
    : systemPrivilegeOperation SEQUENCE 
    ;

sessionsSystemPrivilege
    : (CREATE | ALTER | RESTRICTED) SESSION | ALTER RESOURCE COST
    ;

sqlTranslationProfilesSystemPrivilege
    : (systemPrivilegeOperation | USE ANY) SQL TRANSLATION PROFILE | TRANSLATE ANY SQL
    ;

synonymsSystemPrivilege
    : systemPrivilegeOperation SYNONYM | DROP PUBLIC SYNONYM
    ;

tablesSystemPrivilege
    : (systemPrivilegeOperation | (BACKUP | LOCK | READ | FLASHBACK) ANY) TABLE
    ;

tablespacesSystemPrivilege
    : (systemPrivilegeOperation | MANAGE | UNLIMITED) TABLESPACE
    ;

triggersSystemPrivilege
    : systemPrivilegeOperation TRIGGER | ADMINISTER DATABASE TRIGGER
    ;

typesSystemPrivilege
    : (systemPrivilegeOperation | UNDER ANY) TYPE
    ;

usersSystemPrivilege
    : systemPrivilegeOperation USER
    ;

viewsSystemPrivilege
    : (systemPrivilegeOperation | (UNDER | MERGE) ANY) VIEW
    ;

miscellaneousSystemPrivilege
    : ANALYZE ANY | AUDIT ANY | BECOME USER | CHANGE NOTIFICATION | COMMENT ANY TABLE | EXEMPT ACCESS POLICY | FORCE ANY? TRANSACTION
    | GRANT ANY OBJECT? PRIVILEGE | INHERIT ANY PRIVILEGES | KEEP DATE TIME | KEEP SYSGUID | PURGE DBA_RECYCLEBIN | RESUMABLE
    | SELECT ANY (DICTIONARY | TRANSACTION) | SYSBACKUP | SYSDBA | SYSDG | SYSKM | SYSOPER
    ;

createUser
    : CREATE USER
    ;

dropUser
    : DROP USER username CASCADE?
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
    : ALTER ROLE roleName ( NOT IDENTIFIED | IDENTIFIED (
    | BY password 
    | USING packageName 
    | EXTERNALLY 
    | GLOBALLY AS (STRING_ | SQ_ AZURE_ROLE EQ_ identifier SQ_ | SQ_ IAM_GROUP_NAME EQ_ identifier SQ_) ) ) (CONTAINER EQ_ (CURRENT | ALL))?
    ;

setRole
    : SET ROLE (roleAssignment | allClause | NONE)
    ;

roleAssignment
    : roleName (IDENTIFIED BY password)? (COMMA_ roleName (IDENTIFIED BY password)? )*
    ;

allClause
    : ALL (EXCEPT roleName (COMMA_ roleName)*)?
    ;
