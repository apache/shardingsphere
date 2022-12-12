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

SQL_PARSER
    : S Q L UL_ P A R S E R
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

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;
