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

SQL_FEDERATION
    : S Q L UL_ F E D E R A T I O N
    ;

SQL_FEDERATION_ENABLED
    : S Q L UL_ F E D E R A T I O N UL_ E N A B L E D
    ;

ALL_QUERY_USE_SQL_FEDERATION
    : A L L UL_ Q U E R Y UL_ U S E UL_ S Q L UL_ F E D E R A T I O N
    ;

EXECUTION_PLAN_CACHE
    : E X E C U T I O N UL_ P L A N UL_ C A C H E
    ;

INITIAL_CAPACITY
    : I N I T I A L UL_ C A P A C I T Y
    ;

MAXIMUM_SIZE
    : M A X I M U M UL_ S I Z E
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;
