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

RESOURCE
    : R E S O U R C E
    ;

RESOURCES
    : R E S O U R C E S
    ;

FROM
    : F R O M
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

IP
    : I P
    ;

IGNORE
    : I G N O R E
    ;

SCHEMA
    : S C H E M A
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

TRANSACTION
    : T R A N S A C T I O N
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

SQL_PARSER
    : S Q L UL_ P A R S E R
    ;

AUTHORITY
    : A U T H O R I T Y
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;

SQL_COMMENT_PARSE_ENABLE
    : S Q L UL_ C O M M E N T UL_ P A R S E UL_ E N A B L E
    ;

PARSE_TREE_CACHE
    : P A R S E UL_ T R E E UL_ C A C H E
    ;

SQL_STATEMENT_CACHE
    : S Q L UL_ S T A T E M E N T UL_ C A C H E
    ;

INITIAL_CAPACITY
    : I N I T I A L UL_ C A P A C I T Y
    ;

MAXIMUM_SIZE
    : M A X I M U M UL_ S I Z E
    ;

CONCURRENCY_LEVEL
    : C O N C U R R E N C Y UL_ L E V E L
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

TRAFFIC
    : T R A F F I C
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

LABELS
    : L A B E L S
    ;

TRAFFIC_ALGORITHM
    : T R A F F I C UL_ A L G O R I T H M
    ;

LOAD_BALANCER
    : L O A D UL_ B A L A N C E R
    ;

EXPORT
    : E X P O R T
    ;

IMPORT
    : I M P O R T
    ;

CONFIGURATION
    : C O N F I G U R A T I O N
    ;

CONFIG
    : C O N F I G
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
