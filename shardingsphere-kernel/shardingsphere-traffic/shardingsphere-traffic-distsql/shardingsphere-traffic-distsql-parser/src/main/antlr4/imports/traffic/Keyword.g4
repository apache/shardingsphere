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

lexer grammar Keyword;

import Alphabet;

WS
    : [ \t\r\n] + ->skip
    ;

SHOW
    : S H O W
    ;

TRAFFIC
    : T R A F F I C
    ;

RULES
    : R U L E S
    ;

RULE
    : R U L E
    ;

CREATE
    : C R E A T E
    ;

PROPERTIES
    : P R O P E R T I E S
    ;

LABELS
    : L A B E L S
    ;

TRAFFIC_ALGORITHM
    : T R A F F I C UL_ A L G O R I T H M
    ;

TYPE
    : T Y P E
    ;

NAME
    : N A M E
    ;

LOAD_BALANCER
    : L O A D UL_ B A L A N C E R
    ;

ALTER
    : A L T E R
    ;

DROP
    : D R O P
    ;

IF  
    : I F
    ;

EXISTS
    : E X I S T S
    ;
