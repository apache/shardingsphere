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

grammar MySQLStatement;

import Symbol, Comments, DMLStatement, DDLStatement, TCLStatement, DCLStatement, DALStatement, RLStatement;

execute
    : (select
    | insert
    | update
    | delete
    | replace
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
    | createView
    | dropView
    | createTrigger
    | dropTrigger
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
    | setPassword
    | use
    | explain
    | showDatabases
    | showTables
    | showTableStatus
    | showColumns
    | showIndex
    | showCreateTable
    | showOther
    | setVariable
    | setName
    | setCharacter
    | call
    | changeMasterTo
    | startSlave
    | stopSlave
    | showCreateFunction
    | showCreateProcedure
    | showCreateView
    | showCreateUser
    ) SEMI_?
    ;
