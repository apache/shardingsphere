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

grammar HiveStatement;

import Comments, DMLStatement, DDLStatement, DALStatement, TCLStatement;

// TODO correct hive SQL parsing according to official documentation
execute
    : (select
    | insert
    | update
    | delete
    | merge
    | loadStatement
    | createDatabase
    | dropDatabase
    | alterDatabase
    | use
    | createTable
    | dropTable
    | truncateTable
    | msckStatement
    | alterTable
    | createView
    | dropView
    | alterView
    | createMaterializedView
    | dropMaterializedView
    | alterMaterializedView
    | createIndex
    | dropIndex
    | alterIndex
    | createMacro
    | dropMacro
    | createFunction
    | dropFunction
    | reloadFunction
    | show
    | describe
    | abort
    ) (SEMI_ EOF? | EOF)
    | EOF
    ;
