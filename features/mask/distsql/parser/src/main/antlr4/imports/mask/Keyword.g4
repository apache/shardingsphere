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
    : R U L E
    ;

FROM
    : F R O M
    ;

MASK
    : M A S K
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

RULES
    : R U L E S
    ;

TABLE
    : T A B L E
    ;

COLUMNS
    : C O L U M N S
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

NOT
    : N O T
    ;

MD5
    : M D [5]
    ;

KEEP_FIRST_N_LAST_M
    : K E E P UL_ F I R S T UL_ N UL_ L A S T UL_ M
    ;

KEEP_FROM_X_TO_Y
    : K E E P UL_ F R O M UL_ X UL_ T O UL_ Y
    ;

MASK_FIRST_N_LAST_M
    : M A S K UL_ F I R S T UL_ N UL_ L A S T UL_ M
    ;

MASK_FROM_X_TO_Y
    : M A S K UL_ F R O M UL_ X UL_ T O UL_ Y
    ;

MASK_BEFORE_SPECIAL_CHARS
    : M A S K UL_ B E F O R E UL_ S P E C I A L UL_ C H A R S
    ;

MASK_AFTER_SPECIAL_CHARS
    : M A S K UL_ A F T E R UL_ S P E C I A L UL_ C H A R S
    ;

GENERIC_TABLE_RANDOM_REPLACE
    : G E N E R I C UL_ T A B L E UL_ R A N D O M UL_ R E P L A C E
    ;

ADDRESS_RANDOM_REPLACE
    : A D D R E S S UL_ R A N D O M UL_ R E P L A C E
    ;

ALGORITHM
    : A L G O R I T H M
    ;

PLUGINS
    : P L U G I N S
    ;
