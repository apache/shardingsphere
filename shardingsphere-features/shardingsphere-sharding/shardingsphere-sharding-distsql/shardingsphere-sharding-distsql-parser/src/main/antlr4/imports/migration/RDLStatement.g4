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

createShardingScalingRule
    : CREATE SHARDING SCALING RULE scalingName scalingRuleDefinition?
    ;

dropShardingScalingRule
    : DROP SHARDING SCALING RULE ifExists? scalingName
    ;

enableShardingScalingRule
    : ENABLE SHARDING SCALING RULE scalingName
    ;

disableShardingScalingRule
    : DISABLE SHARDING SCALING RULE scalingName
    ;

scalingName
    : IDENTIFIER
    ;

scalingRuleDefinition
    : LP inputDefinition? (COMMA? outputDefinition)? (COMMA? streamChannel)? (COMMA? completionDetector)? (COMMA? dataConsistencyChecker)? RP
    ;

inputDefinition
    : INPUT LP workerThread? (COMMA? batchSize)? (COMMA? shardingSize)? (COMMA? rateLimiter)? RP
    ;

outputDefinition
    : OUTPUT LP workerThread? (COMMA? batchSize)? (COMMA? rateLimiter)? RP
    ;

completionDetector
    : COMPLETION_DETECTOR LP algorithmDefinition RP
    ;

dataConsistencyChecker
    : DATA_CONSISTENCY_CHECKER LP algorithmDefinition RP
    ;

workerThread
    : WORKER_THREAD EQ intValue
    ;

batchSize
    : BATCH_SIZE EQ intValue
    ;

shardingSize
    : SHARDING_SIZE EQ intValue
    ;

rateLimiter
    : RATE_LIMITER LP algorithmDefinition RP
    ;

streamChannel
    : STREAM_CHANNEL LP algorithmDefinition RP
    ;

intValue
    : INT
    ;

ifExists
    : IF EXISTS
    ;
