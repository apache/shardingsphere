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

ALGORITHMS
    : A L G O R I T H M S
    ;

DROP
    : D R O P
    ;

SHARDING
    : S H A R D I N G
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

RESTORE
    : R E S T O R E
    ;

CHECK
    : C H E C K
    ;

APPLY
    : A P P L Y
    ;

SCALING
    : S C A L I N G
    ;

RULE
    :  R U L E
    ;

RULES
    :  R U L E S
    ;

FROM
    : F R O M
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

SOURCE
    : S O U R C E
    ;

WRITING
    : W R I T I N G
    ;

BY
    : B Y
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

COMPLETION_DETECTOR
    : C O M P L E T I O N UL_ D E T E C T O R
    ;

DATA_CONSISTENCY_CHECKER
    : D A T A UL_ C O N S I S T E N C Y UL_ C H E C K E R
    ;

INPUT
    : I N P U T
    ;

OUTPUT
    : O U T P U T
    ;

WORKER_THREAD
    : W O R K E R UL_ T H R E A D
    ;

BATCH_SIZE
    : B A T C H UL_ S I Z E
    ;

SHARDING_SIZE
    : S H A R D I N G UL_ S I Z E
    ;

RATE_LIMITER
    : R A T E UL_ L I M I T E R
    ;

STREAM_CHANNEL
    : S T R E A M UL_ C H A N N E L
    ;

ENABLE
    : E N A B L E
    ;

DISABLE
    : D I S A B L E
    ;

IF
    : I F
    ;

EXISTS
    : E X I S T S
    ;

MIGRATE
    : M I G R A T E
    ;

TABLE
    : T A B L E
    ;

INTO
    : I N T O
    ;

DB
    : D B
    ;

USER
    : U S E R
    ;

MIGRATION 
    : M I G R A T I O N
    ;

PASSWORD
    : P A S S W O R D
    ;

URL
    : U R L
    ;

HOST
    : H O S T
    ;

PORT
    : P O R T
    ;

ADD
    : A D D
    ;

RESOURCE
    : R E S O U R C E
    ;

CLEAN
    : C L E A N
    ;
