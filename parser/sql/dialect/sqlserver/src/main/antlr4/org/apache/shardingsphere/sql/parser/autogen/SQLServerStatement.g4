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

grammar SQLServerStatement;

import Comments, TCLStatement, StoreProcedure, DALStatement;

execute
    : (select
    | insert
    | update
    | delete
    | createIndex
    | alterIndex
    | dropIndex
    | createTable
    | createDatabase
    | createProcedure
    | createView
    | createTrigger
    | createSequence
    | createService
    | createSchema
    | alterTable
    | alterTrigger
    | alterSequence
    | alterDatabase
    | alterService
    | alterSchema
    | alterView
    | dropTable
    | dropDatabase
    | dropFunction
    | dropProcedure
    | dropView
    | dropTrigger
    | dropSequence
    | dropService
    | dropSchema
    | truncateTable
    | createFunction
    | setTransaction
    | beginTransaction
    | beginDistributedTransaction
    | setImplicitTransactions
    | commit
    | commitWork
    | rollback
    | rollbackWork
    | savepoint
    | grant
    | revoke
    | deny
    | createUser
    | dropUser
    | alterUser
    | createRole
    | dropRole
    | alterRole
    | createLogin
    | dropLogin
    | alterLogin
    | call
    | explain
    | setUser
    | revert
    | updateStatistics
    | merge
    ) SEMI_? EOF
    ;
