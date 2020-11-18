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

INNODB_
    : 'INNODB'
    ;
    
REDO_LOG_
    : 'REDO_LOG'
    ;
    
FILESIZE_LITERAL
    : INT_NUM_ ('K'|'M'|'G'|'T')
    ;

IDENTIFIER_
    : [A-Za-z_$0-9]*?[A-Za-z_$]+?[A-Za-z_$0-9]*
    |  BQ_ ~'`'+ BQ_
    ;

STRING_
    : DOUBLE_QUOTED_TEXT | SINGLE_QUOTED_TEXT
    ;

SINGLE_QUOTED_TEXT
    : SQ_ ('\\'. | '\'\'' | ~('\'' | '\\'))* SQ_
    ;

DOUBLE_QUOTED_TEXT
    : DQ_ ( '\\'. | '""' | ~('"'| '\\') )* DQ_
    ;

NCHAR_TEXT
    : N SINGLE_QUOTED_TEXT
    ;

UNDERSCORE_CHARSET
    : UL_ [a-z0-9]+
    ;

NUMBER_
    : INT_NUM_
    | FLOAT_NUM_
    | DECIMAL_NUM_
    ;

INT_NUM_
    : DIGIT+
    ;

FLOAT_NUM_
    : INT_NUM_? DOT_? INT_NUM_ E (PLUS_ | MINUS_)? INT_NUM_
    ;

DECIMAL_NUM_
    : INT_NUM_? DOT_ INT_NUM_
    ;

HEX_DIGIT_
    : '0x' HEX_+ | X SQ_ HEX_+ SQ_
    ;

BIT_NUM_
    : '0b' ('0' | '1')+ | B SQ_ ('0' | '1')+ SQ_
    ;

NOT_SUPPORT_
    : 'not support'
    ;

fragment DIGIT
    : [0-9]
    ;

fragment HEX_
    : [0-9a-fA-F]
    ;
