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

createTrafficRule
    : CREATE TRAFFIC RULE trafficRuleDefinition (COMMA trafficRuleDefinition)* 
    ;

alterTrafficRule
    : ALTER TRAFFIC RULE trafficRuleDefinition (COMMA trafficRuleDefinition)* 
    ;

dropTrafficRule
    : DROP TRAFFIC RULE ifExists? ruleName (COMMA ruleName)*
    ;

showTrafficRules
    : SHOW TRAFFIC (RULES | RULE ruleName)
    ;

trafficRuleDefinition
    : ruleName LP (labelDefinition COMMA)? trafficAlgorithmDefinition (COMMA loadBalancerDefinition)? RP
    ;

labelDefinition
    : LABELS LP label (COMMA label)* RP
    ;

trafficAlgorithmDefinition
    : TRAFFIC_ALGORITHM LP algorithmDefinition RP 
    ;

algorithmDefinition
    : TYPE LP NAME EQ algorithmTypeName (COMMA propertiesDefinition)? RP
    ;

loadBalancerDefinition
    : LOAD_BALANCER LP algorithmDefinition RP
    ;

algorithmTypeName
    : STRING
    ;

label
    : IDENTIFIER
    ;

ruleName
    : IDENTIFIER
    ;

ifExists
    : IF EXISTS
    ;
