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

SET
    : S E T
    ;

DEFAULT
    : D E F A U L T
    ;

SINGLE
    : S I N G L E
    ;

TABLE
    : T A B L E
    ;

TABLES
    : T A B L E S
    ;

ALL
    : A L L
    ;

STORAGE
    : S T O R A G E
    ;

UNIT
    : U N I T
    ;

RANDOM
    : R A N D O M
    ;

FROM
    : F R O M
    ;

COUNT
    : C O U N T
    ;

LIKE
    : L I K E
    ;

SCHEMA
    : S C H E M A
    ;

INTO
    : I N T O
    ;

LOAD
    : L O A D
    ;

UNLOAD
    : U N L O A D
    ;

UNLOADED
    : U N L O A D E D
    ;
