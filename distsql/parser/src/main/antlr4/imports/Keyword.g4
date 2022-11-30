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

DEFAULT
    : D E F A U L T
    ;

ALTER
    : A L T E R
    ;

CLEAR
    : C L E A R
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

HINT
    : H I N T
    ;

ENABLE
    : E N A B L E
    ;

DISABLE
    : D I S A B L E
    ;

INSTANCE
    : I N S T A N C E
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

RULE
    : R U L E
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

COUNT
    : C O U N T
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

UNUSED
    : U N U S E D
    ;

PREPARE
    : P R E P A R E
    ;

DISTSQL
    : D I S T S Q L
    ;

APPLY
    : A P P L Y
    ;

DISCARD
    : D I S C A R D
    ;

SINGLE_TABLE
    : S I N G L E UL_ T A B L E
    ;

INFO
    : I N F O
    ;

MIGRATION
    : M I G R A T I O N
    ;

READ
    : R E A D
    ;

WRITE
    : W R I T E
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

STORAGE
    : S T O R A G E
    ;

UNIT
    : U N I T
    ;

UNITS
    : U N I T S
    ;

RANDOM
    : R A N D O M
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

USAGE_COUNT
    : U S A G E UL_ C O U N T
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
