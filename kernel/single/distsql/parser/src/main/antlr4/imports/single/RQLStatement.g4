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

grammar RQLStatement;

import BaseRule;

showDefaultSingleTableStorageUnit
    : SHOW DEFAULT SINGLE TABLE STORAGE UNIT (FROM databaseName)?
    ;

showSingleTable
    : SHOW SINGLE (TABLES showLike? | TABLE tableName) (FROM databaseName)?
    ;

showUnloadedSingleTables
    : SHOW UNLOADED SINGLE TABLES (FROM fromClause)?
    ;

fromClause
    : databaseName (STORAGE UNIT storageUnitName (SCHEMA schemaName)?)?
    | STORAGE UNIT storageUnitName (SCHEMA schemaName)?
    ;

countSingleTable
    : COUNT SINGLE TABLE (FROM databaseName)?
    ;

showLike
    : LIKE likePattern
    ;

likePattern
    : STRING_
    ;
