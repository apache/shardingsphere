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

SHADOW
    : S H A D O W
    ;

SOURCE
    : S O U R C E
    ;

RULE
    :  R U L E
    ;

FROM
    : F R O M
    ;

RESOURCES
    : R E S O U R C E S
    ;

TABLE
    : T A B L E
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

ALGORITHM
    : A L G O R I T H M
    ;

ALGORITHMS
    : A L G O R I T H M S
    ;

SET
    : S E T
    ;

ADD
    : A D D
    ;

DATABASE_VALUE
    : D A T A B A S E UL_ V A L U E
    ;

TABLE_VALUE
    : T A B L E UL_ V A L U E
    ;

STATUS
    : S T A T U S
    ;

CLEAR
    : C L E A R
    ;

DEFAULT
    : D E F A U L T
    ;

IF  
    : I F
    ;
    
EXISTS
    : E X I S T S
    ;
