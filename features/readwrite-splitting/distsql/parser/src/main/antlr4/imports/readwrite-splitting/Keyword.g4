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

SET
    : S E T
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

READWRITE_SPLITTING
    : R E A D W R I T E UL_ S P L I T T I N G
    ;

WRITE_STORAGE_UNIT
    : W R I T E UL_ S T O R A G E UL_ U N I T
    ;

READ_STORAGE_UNITS
    : R E A D UL_ S T O R A G E UL_ U N I T S
    ;

AUTO_AWARE_RESOURCE
    : A U T O UL_ A W A R E UL_ R E S O U R C E
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

SOURCE
    : S O U R C E
    ;

RESOURCES
    : R E S O U R C E S
    ;

STATUS
    : S T A T U S
    ;

HINT
    : H I N T
    ;

CLEAR
    : C L E A R
    ;

ENABLE
    : E N A B L E
    ;

DISABLE
   : D I S A B L E
   ;

READ
   : R E A D
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

WRITE_DATA_SOURCE_QUERY_ENABLED
    : W R I T E UL_ D A T A UL_ S O U R C E UL_ Q U E R Y UL_ E N A B L E D
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;
