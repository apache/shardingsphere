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

RULE
    :  R U L E
    ;

FROM
    : F R O M
    ;

ENCRYPT
    : E N C R Y P T
    ;

TYPE
    : T Y P E
    ;

ENCRYPT_ALGORITHM
    : E N C R Y P T UL_ A L G O R I T H M
    ;

ASSISTED_QUERY_ALGORITHM
    : A S S I S T E D UL_ Q U E R Y UL_ A L G O R I T H M
    ;

LIKE_QUERY_ALGORITHM
    : L I K E UL_ Q U E R Y UL_ A L G O R I T H M
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

RULES
    : R U L E S
    ;

TABLE
    : T A B L E
    ;

COLUMNS
    : C O L U M N S
    ;

CIPHER
    : C I P H E R
    ;

ASSISTED_QUERY
    : A S S I S T E D UL_ Q U E R Y
    ;

ASSISTED_QUERY_COLUMN
    : A S S I S T E D UL_ Q U E R Y UL_ C O L U M N
    ;

LIKE_QUERY
    : L I K E UL_ Q U E R Y
    ;

LIKE_QUERY_COLUMN
    : L I K E UL_ Q U E R Y UL_ C O L U M N
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
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

MD5
    : M D [5]
    ;

AES
    : A E S
    ;

NOT
    : N O T
    ;

ALGORITHM
    : A L G O R I T H M 
    ;

PLUGINS
    : P L U G I N S
    ;
