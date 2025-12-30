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

grammar DorisStatement;

import Comments, DDLStatement, TCLStatement, LCLStatement, DCLStatement;

execute
    : (select
    | insert
    | update
    | delete
    | replace
    | binlog
    | createTable
    | alterStatement
    | repairTable
    | dropTable
    | truncateTable
    | createIndex
    | dropIndex
    | createProcedure
    | dropProcedure
    | createFunction
    | dropFunction
    | createDatabase
    | dropDatabase
    | createEvent
    | dropEvent
    | createLogfileGroup
    | dropLogfileGroup
    | createServer
    | dropServer
    | dropEncryptKey
    | createView
    | dropView
    | createTrigger
    | dropTrigger
    | alterResourceGroup
    | alterResource
    | createResourceGroup
    | dropResourceGroup
    | prepare
    | executeStmt
    | deallocate
    | setTransaction
    | beginTransaction
    | setAutoCommit
    | commit
    | rollback
    | savepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | renameUser
    | createRole
    | dropRole
    | setDefaultRole
    | setRole
    | createSRSStatement
    | dropSRSStatement
    | flush
    | getDiagnosticsStatement
    | groupReplication
    | handlerStatement
    | help
    | importStatement
    | install
    | kill
    | loadStatement
    | cacheIndex
    | loadIndexInfo
    | optimizeTable
    | purgeBinaryLog
    | releaseSavepoint
    | resetStatement
    | setPassword
    | setTransaction
    | setResourceGroup
    | resignalStatement
    | signalStatement
    | restart
    | shutdown
    | begin
    | use
    | switchCatalog
    | explain
    | doStatement
    | show
    | refresh
    | setVariable
    | setCharacter
    | call
    | changeMasterTo
    | changeReplicationFilter
    | checkTable
    | checksumTable
    | clone
    | changeReplicationSourceTo
    | startSlave
    | stopSlave
    | analyzeTable
    | renameTable
    | uninstall
    | xaBegin
    | xaPrepare
    | xaCommit
    | xaRollback
    | xaEnd
    | xaRecovery
    | lock
    | unlock
    | createLoadableFunction
    | createTablespace
    | alterTablespace
    | dropTablespace
    | delimiter
    | startReplica
    | createMaterializedView
    | resumeJob
    | dorisAlterSystem
    | createSqlBlockRule
    // TODO consider refactor following sytax to SEMI_? EOF
    ) (SEMI_ EOF? | EOF)
    | EOF
    ;
