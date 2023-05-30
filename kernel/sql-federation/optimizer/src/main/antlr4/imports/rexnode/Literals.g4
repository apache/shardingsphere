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

lexer grammar Literals;

import Alphabet, Symbol;

PLACEHOLDER_
    :  QUESTION_ INTEGER_
    ;

STRING_ 
    : (DQ_ ( '\\'. | '""' | ~('"'| '\\') )* DQ_) | (SQ_ ('\\'. | '\'\'' | ~('\'' | '\\'))* SQ_)
    ;

INTEGER_
    : INT_
    ;

DATE_
    : YEAR_ MINUS_ MONTH_ MINUS_ DAY_
    ;

NEGETIVE_INFINITY_:  '-∞';

POSITIVE_INFINITY_:  '+∞';

INT_
    : [0-9]+
    ;

HEX_
    : [0-9a-fA-F]
    ;

YEAR_
    : [0-9]+
    ;

MONTH_
    : [0-9]+
    ;
DAY_
    : [0-9]+
    ;

UTF_: UL_ U T F ML_ EIGHT_;

