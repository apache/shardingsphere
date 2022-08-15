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

setShardingHintDatabaseValue
    : SET SHARDING HINT DATABASE_VALUE EQ shardingValue
    ;

addShardingHintDatabaseValue
    : ADD SHARDING HINT DATABASE_VALUE tableName EQ shardingValue
    ;

addShardingHintTableValue
    : ADD SHARDING HINT TABLE_VALUE tableName EQ shardingValue
    ;

showShardingHintStatus
    : SHOW SHARDING HINT STATUS
    ;

clearShardingHint
    : CLEAR SHARDING HINT
    ;

shardingValue
    : INT | STRING
    ;
