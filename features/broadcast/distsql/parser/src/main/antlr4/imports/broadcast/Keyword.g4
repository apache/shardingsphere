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

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;

CREATE
    : C R E A T E
    ;

DROP
    : D R O P
    ;

SHOW
    : S H O W
    ;

BROADCAST
    : B R O A D C A S T
    ;

RULE
    :  R U L E
    ;

RULES
    : R U L E S
    ;

FROM
    : F R O M
    ;

TABLE
    : T A B L E
    ;

IF
    : I F
    ;

NOT
    : N O T
    ;

EXISTS
    : E X I S T S
    ;

COUNT
    : C O U N T
    ;
