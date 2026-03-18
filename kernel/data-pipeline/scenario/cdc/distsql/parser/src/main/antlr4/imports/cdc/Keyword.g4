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

STREAMING
    : S T R E A M I N G
    ;

LIST
    : L I S T
    ;

STATUS
    : S T A T U S
    ;

DROP
    : D R O P
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

ALTER
    : A L T E R
    ;

RULE
    :  R U L E
    ;

READ
    : R E A D
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

WRITE
    : W R I T E
    ;
