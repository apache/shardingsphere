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

IDENTIFIER_
    : IDENTIFIER_START_CHAR IDENTIFIER_CHAR*
    | DQ_ ~'"'+ DQ_
    ;

STRING_
    : SQ_ ('\\'. | '\'\'' | ~('\'' | '\\'))* SQ_
    ;

NUMBER_
    : INT_? DOT_? INT_ (E (PLUS_ | MINUS_)? INT_)?
    ;

HEX_DIGIT_
    : '0x' HEX_+ | 'X' SQ_ HEX_+ SQ_
    ;

BIT_NUM_
    : '0b' ('0' | '1')+ | B SQ_ ('0' | '1')+ SQ_
    ;

fragment INT_
    : [0-9]+
    ;

fragment HEX_
    : [0-9a-fA-F]
    ;
    
fragment IDENTIFIER_START_CHAR
   : [a-zA-Z_]
   | [\u00AA\u00B5\u00BA\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u00FF]
   | [\u0100-\uD7FF\uE000-\uFFFF]
   | [\uD800-\uDBFF] [\uDC00-\uDFFF]
   ;

fragment IDENTIFIER_CHAR
   : STRICT_IDENTIFIER_CHAR
   | DOLLAR_
   ;
   
fragment STRICT_IDENTIFIER_CHAR
   : IDENTIFIER_START_CHAR
   | [0-9]
   ;
   
DEFAULT_DOES_NOT_MATCH_ANYTHING
   : 'Default does not match anything'
   ;

APOSTROPHE_SKIP
   : 'skip'
   ;
