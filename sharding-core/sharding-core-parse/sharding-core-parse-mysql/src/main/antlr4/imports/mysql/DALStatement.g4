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

grammar DALStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

use
    : USE schemaName
    ;

desc
    : (DESC | DESCRIBE) tableName
    ;

showDatabases
    : SHOW (DATABASES | SCHEMAS) (showLike | showWhereClause_)?
    ;

showTables
    : SHOW EXTENDED? FULL? TABLES fromSchema? (showLike | showWhereClause_)?
    ;

showTableStatus
    : SHOW TABLE STATUS fromSchema? (showLike | showWhereClause_)?
    ;

showColumns
    : SHOW EXTENDED? FULL? (COLUMNS | FIELDS) fromTable_ fromSchema? (showLike | showWhereClause_)?
    ;

showIndex
    : SHOW EXTENDED? (INDEX | INDEXES | KEYS) fromTable_ fromSchema? showWhereClause_?
    ;

showCreateTable
    : SHOW CREATE TABLE tableName
    ;

showOther
    : SHOW
    ;

fromSchema
    : (FROM | IN) schemaName
    ;

fromTable_
    : (FROM | IN) tableName
    ;

showLike
    : LIKE stringLiterals
    ;

showWhereClause_
    : WHERE expr
    ;

setVariable
    : SET
    ;