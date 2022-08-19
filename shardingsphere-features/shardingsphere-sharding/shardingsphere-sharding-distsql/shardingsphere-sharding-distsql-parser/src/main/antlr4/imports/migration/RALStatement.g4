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

grammar RALStatement;

import BaseRule;

migrateTable
    : MIGRATE TABLE sourceTableName INTO targetTableName
    ;

showScalingList
    : SHOW SCALING LIST
    ;

showScalingStatus
    : SHOW SCALING STATUS jobId
    ;

startScaling
    : START SCALING jobId
    ;

stopScaling
    : STOP SCALING jobId
    ;

cleanScaling
    : CLEAN SCALING jobId
    ;

resetScaling
    : RESET SCALING jobId
    ;

checkScaling
    : CHECK SCALING jobId (BY algorithmDefinition)?
    ;

showScalingCheckAlgorithms
    : SHOW SCALING CHECK ALGORITHMS
    ;

stopScalingSourceWriting
    : STOP SCALING SOURCE WRITING jobId
    ;

restoreScalingSourceWriting
    : RESTORE SCALING SOURCE WRITING jobId
    ;

applyScaling
    : APPLY SCALING jobId
    ;

jobId
    : INT | STRING
    ;

sourceTableName
    : (owner DOT)? name
    ;

targetTableName
    : (owner DOT)? name
    ;

owner
    : identifier
    ;

name
    : identifier
    ;

identifier
    : IDENTIFIER
    ;

resourceDefinition
    : resourceName LP (simpleSource | urlSource) COMMA USER EQ user (COMMA PASSWORD EQ password)? (COMMA propertiesDefinition)? RP
    ;

resourceName
    : IDENTIFIER
    ;

simpleSource
    : HOST EQ hostname COMMA PORT EQ port COMMA DB EQ dbName
    ;

urlSource
    : URL EQ url
    ;

hostname
    : STRING
    ;

port
    : INT
    ;

dbName
    : STRING
    ;

url
    : STRING
    ;

user
    : STRING
    ;

password
    : STRING
    ;

addMigrationSourceResource
    : ADD MIGRATION SOURCE RESOURCE resourceDefinition (COMMA resourceDefinition)*
    ;
