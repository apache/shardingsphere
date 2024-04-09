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

registerStorageUnit
    : REGISTER STORAGE UNIT ifNotExists? storageUnitDefinition (COMMA_ storageUnitDefinition)*
    ;

alterStorageUnit
    : ALTER STORAGE UNIT storageUnitDefinition (COMMA_ storageUnitDefinition)*
    ;

unregisterStorageUnit
    : UNREGISTER STORAGE UNIT ifExists? storageUnitName (COMMA_ storageUnitName)* ignoreTables?
    ;

storageUnitDefinition
    : storageUnitName LP_ (simpleSource | urlSource) COMMA_ USER EQ_ user (COMMA_ PASSWORD EQ_ password)? (COMMA_ propertiesDefinition)? RP_
    ;

simpleSource
    : HOST EQ_ hostname COMMA_ PORT EQ_ port COMMA_ DB EQ_ dbName
    ;

urlSource
    : URL EQ_ url
    ;

hostname
    : STRING_
    ;

port
    : INT_
    ;

dbName
    : STRING_
    ;

url
    : STRING_
    ;

user
    : STRING_
    ;

password
    : STRING_
    ;

ignoreTables
    : IGNORE (SINGLE COMMA_ BROADCAST | BROADCAST COMMA_ SINGLE) TABLES # ignoreSingleAndBroadcastTables
    | IGNORE SINGLE TABLES # ignoreSingleTables
    | IGNORE BROADCAST TABLES # ignoreBroadcastTables
    ;

ifExists
    : IF EXISTS
    ;

ifNotExists
    : IF NOT EXISTS
    ;
