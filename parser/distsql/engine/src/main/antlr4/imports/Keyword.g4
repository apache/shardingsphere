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

ALL
    : A L L
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

FOR
    : F O R
    ;

FROM
    : F R O M
    ;

TO
    : T O
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

DB
    : D B
    ;

USER
    : U S E R
    ;

PASSWORD
    : P A S S W O R D
    ;

NAME
    : N A M E
    ;

PROPERTIES
    : P R O P E R T I E S
    ;

VARIABLE
    : V A R I A B L E
    ;

VARIABLES
    : V A R I A B L E S
    ;

ENABLE
    : E N A B L E
    ;

DISABLE
    : D I S A B L E
    ;

IGNORE
    : I G N O R E
    ;

SCHEMA
    : S C H E M A
    ;

DATABASE
    : D A T A B A S E
    ;

FULL
    : F U L L
    ;

LOGICAL
    : L O G I C A L
    ;

SINGLE
    : S I N G L E
    ;

TABLES
    : T A B L E S
    ;

LIST
    : L I S T
    ;

TABLE
    : T A B L E
    ;

RULES
    : R U L E S
    ;

REFRESH
    : R E F R E S H
    ;

METADATA
    : M E T A D A T A
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

TYPE
    : T Y P E
    ;

MODE
    : M O D E
    ;

LABEL
    : L A B E L
    ;

RELABEL
    : R E L A B E L
    ;

UNLABEL
    : U N L A B E L
    ;

EXPORT
    : E X P O R T
    ;

IMPORT
    : I M P O R T
    ;

CONVERT
    : C O N V E R T
    ;

YAML
    : Y A M L
    ;

CONFIGURATION
    : C O N F I G U R A T I O N
    ;

FILE
    : F I L E
    ;

USED
    : U S E D
    ;

WITH
    : W I T H
    ;

INFO
    : I N F O
    ;

STORAGE
    : S T O R A G E
    ;

UNIT
    : U N I T
    ;

UNITS
    : U N I T S
    ;

DIST
    : D I S T
    ;

WHERE
    : W H E R E
    ;

COMPUTE
    : C O M P U T E
    ;

NODE
    : N O D E
    ;

NODES
    : N O D E S
    ;

REGISTER
    : R E G I S T E R
    ;

UNREGISTER
    : U N R E G I S T E R
    ;

GOVERNANCE
    : G O V E R N A N C E
    ;

CENTER
    : C E N T E R
    ;

LIKE
    : L I K E
    ;

NOT
    : N O T
    ;

LOCK
    : L O C K
    ;

UNLOCK
    : U N L O C K
    ;

CLUSTER
    : C L U S T E R
    ;

LOCK_STRATEGY
    : L O C K UL_ S T R A T E G Y
    ;

BROADCAST
    : B R O A D C A S T
    ;

PLUGINS
    : P L U G I N S
    ;

OF
    : O F
    ;

KEY
    : K E Y
    ;

GENERATE
    : G E N E R A T E
    ;

LOAD
    : L O A D
    ;

BALANCE
    : B A L A N C E
    ;

ALGORITHM
    : A L G O R I T H M
    ;

FORCE
    : F O R C E
    ;

CHECK_PRIVILEGES
    : C H E C K UL_ P R I V I L E G E S
    ;

TIMEOUT
    : T I M E O U T
    ;

TEMP
    : T E M P
    ;
