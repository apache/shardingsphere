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

CREATE
    : C R E A T E
    ;

ALTER
    : A L T E R
    ;

DROP
    : D R O P
    ;

SHOW
    : S H O W
    ;

STORAGE_UNITS
    : S T O R A G E UL_ U N I T S
    ;

RULE
    :  R U L E
    ;

FROM
    : F R O M
    ;

TYPE
    : T Y P E
    ;

NAME
    : N A M E
    ;

PROPERTIES
    : P R O P E R T I E S
    ;

RULES
    : R U L E S
    ;

TYPES
    : T Y P E S
    ;

HEARTBEATS
    : H E A R T B E A T S
    ;
    
DB_DISCOVERY
    : D B UL_ D I S C O V E R Y
    ;

HEARTBEAT
    : H E A R T B E A T
    ;

IF  
    : I F
    ;

EXISTS
    : E X I S T S
    ;

COUNT
    : C O U N T
    ;
