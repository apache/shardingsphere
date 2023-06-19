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

grammar RDLStatement;

import BaseRule;

setDefaultSingleTableStorageUnit
    : SET DEFAULT SINGLE TABLE STORAGE UNIT EQ_ (storageUnitName | RANDOM)
    ;

loadSingleTable
    : LOAD SINGLE TABLE tableDefinition
    ;

unloadSingleTable
    : UNLOAD SINGLE TABLE tableNames
    | UNLOAD SINGLE TABLE ASTERISK_
    | UNLOAD ALL SINGLE TABLES
    ;

tableDefinition
    : tableIdentifier (COMMA_ tableIdentifier)*
    ;

tableNames
    : tableName (COMMA_ tableName)*
    ;

tableIdentifier
    : ASTERISK_ DOTASTERISK_ # allTables
    | ASTERISK_ DOTASTERISK_ DOTASTERISK_ # allTablesWithSchema
    | storageUnitName DOTASTERISK_ # allTablesFromStorageUnit
    | storageUnitName DOTASTERISK_ DOTASTERISK_ # allSchamesAndTablesFromStorageUnit
    | storageUnitName DOT_ schemaName DOTASTERISK_ # allTablesFromSchema
    | storageUnitName DOT_ tableName # tableFromStorageUnit
    | storageUnitName DOT_ schemaName DOT_ tableName # tableFromSchema
    ;
