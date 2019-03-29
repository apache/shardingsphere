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

grammar MySQLDMLStatement;

import Symbol, MySQLKeyword, Keyword, DataType, MySQLBase, BaseRule, MySQLDQLStatement;

insert
    : INSERT (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE? INTO? tableName (PARTITION ignoredIdentifiers_)? (setClause | columnClause | columnNames? select) onDuplicateKeyClause?
    ;

setClause
    : SET assignmentList
    ;

columnClause
    : columnNames? valueClause
    ;

valueClause
    : (VALUES | VALUE) assignmentValueList (COMMA_ assignmentValueList)*
    ;

onDuplicateKeyClause
    : ON DUPLICATE KEY UPDATE assignmentList
    ;

update
    : updateClause setClause whereClause?
    ;

updateClause
    : UPDATE LOW_PRIORITY? IGNORE? tableReferences
    ;

delete
    : deleteClause whereClause?
    ;

deleteClause
    : DELETE LOW_PRIORITY? QUICK? IGNORE? (fromMulti | fromSingle) 
    ;

fromSingle
    : FROM tableName (PARTITION ignoredIdentifiers_)?
    ;

fromMulti
    : fromMultiTables FROM tableReferences | FROM fromMultiTables USING tableReferences
    ;

fromMultiTables
    : fromMultiTable (COMMA_ fromMultiTable)*
    ;

fromMultiTable
    : tableName DOT_ASTERISK_?
    ;
