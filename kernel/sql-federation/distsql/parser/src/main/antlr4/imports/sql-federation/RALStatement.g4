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

import Keyword, Literals;

showSQLFederationRule
    : SHOW SQL_FEDERATION RULE
    ;

alterSQLFederationRule
    : ALTER SQL_FEDERATION RULE sqlFederationRuleDefinition
    ;

sqlFederationRuleDefinition
    : LP_ sqlFederationEnabled? (COMMA_? allQueryUseSQLFederation)? (COMMA_? executionPlanCache)? RP_
    ;

sqlFederationEnabled
    : SQL_FEDERATION_ENABLED EQ_ boolean_
    ;

allQueryUseSQLFederation
    : ALL_QUERY_USE_SQL_FEDERATION EQ_ boolean_
    ;

executionPlanCache
    : EXECUTION_PLAN_CACHE LP_ cacheOption RP_
    ;

boolean_
    : TRUE | FALSE
    ;

cacheOption
    : (INITIAL_CAPACITY EQ_ initialCapacity)? (COMMA_? MAXIMUM_SIZE EQ_ maximumSize)?
    ;

initialCapacity
    : INT_
    ;

maximumSize
    : INT_
    ;
