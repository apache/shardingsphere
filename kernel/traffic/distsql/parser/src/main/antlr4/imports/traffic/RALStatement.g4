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

alterTrafficRule
    : ALTER TRAFFIC RULE trafficRuleDefinition (COMMA_ trafficRuleDefinition)*
    ;

showTrafficRules
    : SHOW TRAFFIC (RULES | RULE ruleName)
    ;

trafficRuleDefinition
    : ruleName LP_ (labelDefinition COMMA_)? trafficAlgorithmDefinition (COMMA_ loadBalancerDefinition)? RP_
    ;

labelDefinition
    : LABELS LP_ label (COMMA_ label)* RP_
    ;

trafficAlgorithmDefinition
    : TRAFFIC_ALGORITHM LP_ algorithmDefinition RP_
    ;

algorithmDefinition
    : TYPE LP_ NAME EQ_ algorithmTypeName (COMMA_ propertiesDefinition)? RP_
    ;

loadBalancerDefinition
    : LOAD_BALANCER LP_ algorithmDefinition RP_
    ;

algorithmTypeName
    : buildInTrafficAlgorithmTypeName | buildInLoadBalancerTypeName | STRING_
    ;

buildInTrafficAlgorithmTypeName
    : SQL_MATCH | SQL_HINT
    ;

buildInLoadBalancerTypeName
    : RANDOM | ROUND_ROBIN
    ;

label
    : IDENTIFIER_
    ;

ruleName
    : IDENTIFIER_
    ;

ifExists
    : IF EXISTS
    ;
