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

START
    : S T A R T
    ;

STOP
    : S T O P
    ;

RESET
    : R E S E T
    ;

CHECK
    : C H E C K
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

READWRITE_SPLITTING
    : R E A D W R I T E UL_ S P L I T T I N G
    ;

WRITE_RESOURCE
    : W R I T E UL_ R E S O U R C E
    ;

READ_RESOURCES
    : R E A D UL_ R E S O U R C E S
    ;

AUTO_AWARE_RESOURCE
    : A U T O UL_ A W A R E UL_ R E S O U R C E
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

BINDING_TABLE
    : B I N D I N G UL_ T A B L E
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

SCALING
    : S C A L I N G
    ;

JOB
    : J O B
    ;

LIST
    : L I S T
    ;

STATUS
    : S T A T U S
    ;

HOST
    : H O S T
    ;
    
PORT
    : P O R T
    ;
    
DB
    : D B
    ;
    
USER
    : U S E R
    ;
    
PASSWORD
    : P A S S W O R D
    ;

TABLE
    : T A B L E
    ;

SHARDING_COLUMN
    : S H A R D I N G UL_ C O L U M N
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
    
COLUMN
    : C O L U M N
    ;

BINDING
    : B I N D I N G
    ;

RULES
    : R U L E S
    ;

BROADCAST
    : B R O A D C A S T
    ;
