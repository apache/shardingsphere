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

showMigrationRule
    : SHOW MIGRATION RULE
    ;

alterMigrationRule
    : ALTER MIGRATION RULE transmissionRule
    ;

transmissionRule
    : LP_ readDefinition? (COMMA_? writeDefinition)? (COMMA_? streamChannel)? RP_
    ;

readDefinition
    : READ LP_ workerThread? (COMMA_? batchSize)? (COMMA_? shardingSize)? (COMMA_? rateLimiter)? RP_
    ;

writeDefinition
    : WRITE LP_ workerThread? (COMMA_? batchSize)? (COMMA_? rateLimiter)? RP_
    ;

workerThread
    : WORKER_THREAD EQ_ intValue
    ;

batchSize
    : BATCH_SIZE EQ_ intValue
    ;

shardingSize
    : SHARDING_SIZE EQ_ intValue
    ;

rateLimiter
    : RATE_LIMITER LP_ algorithmDefinition RP_
    ;

streamChannel
    : STREAM_CHANNEL LP_ algorithmDefinition RP_
    ;

migrateTable
    : MIGRATE TABLE sourceTableName INTO targetTableName
    ;

showMigrationList
    : SHOW MIGRATION LIST
    ;

showMigrationStatus
    : SHOW MIGRATION STATUS jobId
    ;

startMigration
    : START MIGRATION jobId
    ;

stopMigration
    : STOP MIGRATION jobId
    ;

rollbackMigration
    : ROLLBACK MIGRATION jobId
    ;

commitMigration
    : COMMIT MIGRATION jobId
    ;

checkMigration
    : CHECK MIGRATION jobId (BY algorithmDefinition)?
    ;

showMigrationCheckStatus
    : SHOW MIGRATION CHECK STATUS jobId
    ;

stopMigrationCheck
    : STOP MIGRATION CHECK jobId
    ;

startMigrationCheck
    : START MIGRATION CHECK jobId
    ;

dropMigrationCheck
    : DROP MIGRATION CHECK jobId
    ;

showMigrationCheckAlgorithms
    : SHOW MIGRATION CHECK ALGORITHMS
    ;

jobId
    : INT_ | IDENTIFIER_ | STRING_
    ;

sourceTableName
    : owner DOT_ (schema DOT_)? name
    ;

targetTableName
    : (owner DOT_)? name
    ;

owner
    : identifier
    ;

schema
    : identifier
    ;

name
    : identifier
    ;

identifier
    : IDENTIFIER_
    ;

storageUnitDefinition
    : storageUnitName LP_ (simpleSource | urlSource) COMMA_ USER EQ_ user (COMMA_ PASSWORD EQ_ password)? (COMMA_ propertiesDefinition)? RP_
    ;

storageUnitName
    : IDENTIFIER_
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

registerMigrationSourceStorageUnit
    : REGISTER MIGRATION SOURCE STORAGE UNIT storageUnitDefinition (COMMA_ storageUnitDefinition)*
    ;

unregisterMigrationSourceStorageUnit
    : UNREGISTER MIGRATION SOURCE STORAGE UNIT storageUnitName (COMMA_ storageUnitName)*
    ;

intValue
    : INT_
    ;
