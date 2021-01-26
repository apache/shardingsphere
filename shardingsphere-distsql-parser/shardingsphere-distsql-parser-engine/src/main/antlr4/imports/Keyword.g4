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

ADD
    : A D D
    ;

CREATE
    : C R E A T E
    ;

ALTER
    : A L T E R
    ;

MODIFY
    : M O D I F Y
    ;

DROP
    : D R O P
    ;

SHOW
    : S H O W
    ;

RESOURCE
    : R E S O U R C E
    ;

RESOURCES
    : R E S O U R C E S
    ;

RULE
    :  R U L E
    ;

FROM
    : F R O M
    ;

SHARDING
    : S H A R D I N G
    ;
   
REPLICA_QUERY
    : R E P L I C A UL_ Q U E R Y
    ;

ENCRYPT
    : E N C R Y P T
    ;

SHADOW
    : S H A D O W
    ;

PRIMARY
    : P R I M A R Y
    ;

REPLICA
    : R E P L I C A
    ;

BINDING_TABLES
    : B I N D I N G UL_ T A B L E S
    ;

BROADCAST_TABLES
    : B R O A D C A S T UL_ T A B L E S
    ;

GENERATED_KEY
    : G E N E R A T E D UL_ K E Y
    ;

DEFAULT_TABLE_STRATEGY
    : D E F A U L T UL_ T A B L E UL_ S T R A T E G Y
    ;
