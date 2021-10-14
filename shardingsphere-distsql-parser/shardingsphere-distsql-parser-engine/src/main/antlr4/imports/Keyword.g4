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
