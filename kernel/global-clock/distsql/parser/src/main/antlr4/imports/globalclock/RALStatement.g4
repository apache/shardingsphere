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

showGlobalClockRule
    : SHOW GLOBAL CLOCK RULE
    ;

alterGlobalClockRule
    : ALTER GLOBAL CLOCK RULE globalClockRuleDefinition
    ;

globalClockRuleDefinition
    : LP_ typeDefinition COMMA_ providerDefinition COMMA_ enabledDefinition (COMMA_ propertiesDefinition)? RP_
    ;

typeDefinition
    : TYPE EQ_ typeName
    ;

providerDefinition
    : PROVIDER EQ_ providerName
    ;

enabledDefinition
    : ENABLED EQ_ enabled
    ;

typeName
    : STRING_
    ;

providerName
    : STRING_
    ;

enabled
    : TRUE | FALSE
    ;
