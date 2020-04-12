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

import Symbol, Keyword, OracleKeyword, Literals, BaseRule;

grant
    : GRANT (objectPrivilegeClause | systemPrivilegeClause_ | roleClause_) 
    ;

revoke
    : REVOKE (objectPrivilegeClause | systemPrivilegeClause_ | roleClause_)
    ;

objectPrivilegeClause
    : objectPrivileges_ ON onObjectClause
    ;

systemPrivilegeClause_
    : systemPrivilege_
    ;
    
roleClause_
    : ignoredIdentifiers_
    ;

objectPrivileges_
    : objectPrivilegeType_ columnNames? (COMMA_ objectPrivilegeType_ columnNames?)*
    ;

objectPrivilegeType_
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

systemPrivilege_
    : ALL PRIVILEGES
    | advisorFrameworkSystemPrivilege_
    | clustersSystemPrivilege_
    | contextsSystemPrivilege_
    | dataRedactionSystemPrivilege_
    | databaseSystemPrivilege_
    | databaseLinksSystemPrivilege_
    | debuggingSystemPrivilege_
    | dictionariesSystemPrivilege_
    | dimensionsSystemPrivilege_
    | directoriesSystemPrivilege_
    | editionsSystemPrivilege_
    | flashbackDataArchivesPrivilege_
    | indexesSystemPrivilege_
    | indexTypesSystemPrivilege_
    | jobSchedulerObjectsSystemPrivilege_
    | keyManagementFrameworkSystemPrivilege_
    | librariesFrameworkSystemPrivilege_
    | logminerFrameworkSystemPrivilege_
    | materizlizedViewsSystemPrivilege_
    | miningModelsSystemPrivilege_
    | olapCubesSystemPrivilege_
    | olapCubeMeasureFoldersSystemPrivilege_
    | olapCubeDiminsionsSystemPrivilege_
    | olapCubeBuildProcessesSystemPrivilege_
    | operatorsSystemPrivilege_
    | outlinesSystemPrivilege_
    | planManagementSystemPrivilege_
    | pluggableDatabasesSystemPrivilege_
    | proceduresSystemPrivilege_
    | profilesSystemPrivilege_
    | rolesSystemPrivilege_
    | rollbackSegmentsSystemPrivilege_
    | sequencesSystemPrivilege_
    | sessionsSystemPrivilege_
    | sqlTranslationProfilesSystemPrivilege_
    | synonymsSystemPrivilege_
    | tablesSystemPrivilege_
    | tablespacesSystemPrivilege_
    | triggersSystemPrivilege_
    | typesSystemPrivilege_
    | usersSystemPrivilege_
    | viewsSystemPrivilege_
    | miscellaneousSystemPrivilege_
    ;

systemPrivilegeOperation_
    : (CREATE | ALTER | DROP | SELECT | INSERT | UPDATE | DELETE | EXECUTE) ANY?
    ;

advisorFrameworkSystemPrivilege_
    : systemPrivilegeOperation_? SQL PROFILE | ADVISOR | ADMINISTER ANY? SQL (TUNING SET | MANAGEMENT OBJECT)
    ;

clustersSystemPrivilege_
    : systemPrivilegeOperation_ CLUSTER
    ;

contextsSystemPrivilege_
    : systemPrivilegeOperation_ CONTEXT
    ;

dataRedactionSystemPrivilege_
    : EXEMPT REDACTION POLICY
    ;

databaseSystemPrivilege_
    : ALTER (DATABASE | SYSTEM) | AUDIT SYSTEM
    ;

databaseLinksSystemPrivilege_
    : (CREATE | ALTER | DROP) PUBLIC? DATABASE LINK
    ;

debuggingSystemPrivilege_
    : DEBUG (CONNECT SESSION | ANY PROCEDURE)
    ;

dictionariesSystemPrivilege_
    : ANALYZE ANY DICTIONARY
    ;

dimensionsSystemPrivilege_
    : systemPrivilegeOperation_ DIMENSION
    ;

directoriesSystemPrivilege_
    : systemPrivilegeOperation_ DIRECTORY
    ;

editionsSystemPrivilege_
    : systemPrivilegeOperation_ EDITION
    ;

flashbackDataArchivesPrivilege_
    : FLASHBACK ARCHIVE ADMINISTER
    ;

indexesSystemPrivilege_
    : systemPrivilegeOperation_ INDEX
    ;

indexTypesSystemPrivilege_
    : systemPrivilegeOperation_ INDEXTYPE
    ;

jobSchedulerObjectsSystemPrivilege_
    : CREATE (ANY | EXTERNAL)? JOB | EXECUTE ANY (CLASS | PROGRAM) | MANAGE SCHEDULER
    ;

keyManagementFrameworkSystemPrivilege_
    : ADMINISTER KEY MANAGEMENT
    ;

librariesFrameworkSystemPrivilege_
    : systemPrivilegeOperation_ LIBRARY
    ;

logminerFrameworkSystemPrivilege_
    : LOGMINING
    ;

materizlizedViewsSystemPrivilege_
    : systemPrivilegeOperation_ MATERIALIZED VIEW | GLOBAL? QUERY REWRITE | ON COMMIT REFRESH | FLASHBACK ANY TABLE
    ;

miningModelsSystemPrivilege_
    : (systemPrivilegeOperation_ | COMMENT ANY) MINING MODEL
    ;

olapCubesSystemPrivilege_
    : systemPrivilegeOperation_ CUBE
    ;

olapCubeMeasureFoldersSystemPrivilege_
    : systemPrivilegeOperation_ MEASURE FOLDER
    ;

olapCubeDiminsionsSystemPrivilege_
    : systemPrivilegeOperation_ CUBE DIMENSION
    ;

olapCubeBuildProcessesSystemPrivilege_
    : systemPrivilegeOperation_ CUBE BUILD PROCESS
    ;

operatorsSystemPrivilege_
    : systemPrivilegeOperation_ OPERATOR
    ;

outlinesSystemPrivilege_
    : systemPrivilegeOperation_ OUTLINE
    ;

planManagementSystemPrivilege_
    : ADMINISTER SQL MANAGEMENT OBJECT
    ;

pluggableDatabasesSystemPrivilege_
    : CREATE PLUGGABLE DATABASE | SET CONTAINER
    ;

proceduresSystemPrivilege_
    : systemPrivilegeOperation_ PROCEDURE 
    ;

profilesSystemPrivilege_
    : systemPrivilegeOperation_ PROFILE 
    ;

rolesSystemPrivilege_
    : (systemPrivilegeOperation_ | GRANT ANY) ROLE 
    ;

rollbackSegmentsSystemPrivilege_
    : systemPrivilegeOperation_ ROLLBACK SEGMENT 
    ;

sequencesSystemPrivilege_
    : systemPrivilegeOperation_ SEQUENCE 
    ;

sessionsSystemPrivilege_
    : (CREATE | ALTER | RESTRICTED) SESSION | ALTER RESOURCE COST
    ;

sqlTranslationProfilesSystemPrivilege_
    : (systemPrivilegeOperation_ | USE ANY) SQL TRANSLATION PROFILE | TRANSLATE ANY SQL
    ;

synonymsSystemPrivilege_
    : systemPrivilegeOperation_ SYNONYM | DROP PUBLIC SYNONYM
    ;

tablesSystemPrivilege_
    : (systemPrivilegeOperation_ | (BACKUP | LOCK | READ | FLASHBACK) ANY) TABLE
    ;

tablespacesSystemPrivilege_
    : (systemPrivilegeOperation_ | MANAGE | UNLIMITED) TABLESPACE
    ;

triggersSystemPrivilege_
    : systemPrivilegeOperation_ TRIGGER | ADMINISTER DATABASE TRIGGER
    ;

typesSystemPrivilege_
    : (systemPrivilegeOperation_ | UNDER ANY) TYPE
    ;

usersSystemPrivilege_
    : systemPrivilegeOperation_ USER
    ;

viewsSystemPrivilege_
    : (systemPrivilegeOperation_ | (UNDER | MERGE) ANY) VIEW
    ;

miscellaneousSystemPrivilege_
    : ANALYZE ANY | AUDIT ANY | BECOME USER | CHANGE NOTIFICATION | COMMENT ANY TABLE | EXEMPT ACCESS POLICY | FORCE ANY? TRANSACTION
    | GRANT ANY OBJECT? PRIVILEGE | INHERIT ANY PRIVILEGES | KEEP DATE TIME | KEEP SYSGUID | PURGE DBA_RECYCLEBIN | RESUMABLE
    | SELECT ANY (DICTIONARY | TRANSACTION) | SYSBACKUP | SYSDBA | SYSDG | SYSKM | SYSOPER
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
