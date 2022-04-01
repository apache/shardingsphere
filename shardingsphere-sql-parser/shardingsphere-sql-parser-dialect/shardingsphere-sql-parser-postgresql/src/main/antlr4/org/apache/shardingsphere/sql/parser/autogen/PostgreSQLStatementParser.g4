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

parser grammar PostgreSQLStatementParser;

import TCLStatement, DCLStatement, DALStatement, StoreProcedure;

options {
    tokenVocab = PostgreSQLStatementLexer;
}

execute
    : (select
    | insert
    | update
    | delete
    | createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | beginTransaction
    | startTransaction
    | end
    | commit
    | rollback
    | abort
    | savepoint
    | releaseSavepoint
    | rollbackToSavepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | createRole
    | dropRole
    | alterRole
    | show
    | set
    | resetParameter
    | call
    | alterAggregate
    | alterCollation
    | alterFunction
    | alterDatabase
    | alterDomain
    | alterDefaultPrivileges
    | alterForeignDataWrapper
    | alterForeignTable
    | alterGroup
    | alterMaterializedView
    | alterProcedure
    | alterServer
    | alterSequence
    | createDatabase
    | createFunction
    | createProcedure
    | createServer
    | createTrigger
    | createView
    | createSequence
    | createDomain
    | createRule
    | createSchema
    | createType
    | createTextSearch
    | declare
    | discard
    | dropDatabase
    | dropFunction
    | dropProcedure
    | dropServer
    | dropTrigger
    | dropView
    | dropSequence
    | dropDomain
    | dropSchema
    | vacuum
    | prepare
    | executeStmt
    | deallocate
    | explain
    | analyzeTable
    | load
    | createTablespace
    | alterTablespace
    | dropTablespace
    | setConstraints
    | copy
    | createLanguage
    | alterLanguage
    | dropLanguage
    | createConversion
    | alterConversion
    | dropConversion
    | alterTextSearchDictionary
    | alterTextSearchTemplate
    | alterTextSearchParser
    | createExtension
    | alterExtension
    | dropExtension
    | dropPolicy
    | dropOwned
    | dropForeignTable
    | dropOperator
    | dropMaterializedView
    | dropGroup
    | dropCast
    | dropEventTrigger
    | dropAggregate
    | dropCollation
    ) SEMI_?
    ;
