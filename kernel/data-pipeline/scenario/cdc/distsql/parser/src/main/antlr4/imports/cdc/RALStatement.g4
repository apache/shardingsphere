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

showStreamingRule
    : SHOW STREAMING RULE
    ;

alterStreamingRule
    : ALTER STREAMING RULE transmissionRule
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

showStreamingList
    : SHOW STREAMING LIST
    ;

showStreamingStatus
    : SHOW STREAMING STATUS jobId
    ;

dropStreaming
    : DROP STREAMING jobId
    ;

jobId
    : INT_ | IDENTIFIER_ | STRING_
    ;
