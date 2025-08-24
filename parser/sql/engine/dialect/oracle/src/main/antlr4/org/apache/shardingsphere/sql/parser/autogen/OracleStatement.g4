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

grammar OracleStatement;

import DMLStatement, DCLStatement, DDLStatement, TCLStatement, LCLStatement, DALStatement, PLSQL;

execute
    : (select
    | insert
    | update
    | delete
    | createView
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | lock
    | createIndex
    | dropIndex
    | alterIndex
    | commit
    | rollback
    | setTransaction
    | savepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | createRole
    | dropRole
    | alterRole
    | setRole
    | call
    | merge
    | alterSynonym
    | alterSession
    | alterDatabase
    | alterSystem
    | setConstraints
    | analyze
    | associateStatistics
    | disassociateStatistics
    | audit
    | noAudit
    | comment
    | flashbackDatabase
    | flashbackTable
    | purge
    | rename
    | createDatabase
    | createDatabaseLink
    | createDimension
    | alterDimension
    | dropDimension
    | createFunction
    | dropDatabaseLink
    | dropDirectory
    | dropView
    | dropTrigger
    | alterView
    | alterTrigger
    | createEdition
    | alterDatabaseLink
    | alterDatabaseDictionary
    | createSynonym
    | createDirectory
    | dropSynonym
    | dropPackage
    | dropEdition
    | dropTableSpace
    | dropOutline
    | dropDatabase
    | alterOutline
    | alterAnalyticView
    | alterAttributeDimension
    | createSequence
    | alterSequence
    | alterPackage
    | createContext
    | createSPFile
    | createPFile
    | createControlFile
    | createFlashbackArchive
    | alterFlashbackArchive
    | dropFlashbackArchive
    | createDiskgroup
    | dropDiskgroup
    | createRollbackSegment
    | dropRollbackSegment
    | createLockdownProfile
    | dropLockdownProfile
    | createInmemoryJoinGroup
    | alterInmemoryJoinGroup
    | dropInmemoryJoinGroup
    | createRestorePoint
    | dropRestorePoint
    | dropOperator
    | dropType
    | alterLibrary
    | alterMaterializedZonemap
    | alterJava
    | alterAuditPolicy
    | alterCluster
    | alterOperator
    | alterProfile
    | alterRollbackSegment
    | alterDiskgroup
    | alterIndexType
    | createMaterializedView
    | createMaterializedViewLog
    | alterMaterializedView
    | alterMaterializedViewLog
    | alterFunction
    | alterHierarchy
    | alterLockdownProfile
    | alterPluggableDatabase
    | explain
    | createProcedure
    | dropProcedure
    | alterProcedure
    | dropIndexType
    | dropPluggableDatabase
    | dropJava
    | dropLibrary
    | dropMaterializedView
    | dropMaterializedViewLog
    | dropMaterializedZonemap
    | dropContext
    | alterResourceCost
    | alterRole
    | createTablespace
    | alterTablespace
    | dropSequence
    | dropProfile
    | dropFunction
    | dropCluster
    | systemAction
    | alterType
    | createType
    | createCluster
    | createJava
    | plsqlBlock
    | createLibrary
    | switch
    | createProfile
    | createTrigger
    | show
    | spool
    | createOperator
    | createOutline
    ) SEMI_? SLASH_? EOF
    ;
