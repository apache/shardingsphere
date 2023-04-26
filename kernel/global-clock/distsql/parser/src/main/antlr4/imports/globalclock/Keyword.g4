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

ALTER
    : A L T E R
    ;

RULE
    : R U L E
    ;

GLOBAL
    : G L O B A L
    ;

CLOCK
    : C L O C K
    ;

TYPE
    : T Y P E
    ;

PROVIDER
    :  P R O V I D E R
    ;

ENABLED
    :  E N A B L E D
    ;

PROPERTIES
    : P R O P E R T I E S
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;
