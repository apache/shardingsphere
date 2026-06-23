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
    : [A-Za-z\u0080-\u2FFF\u3001-\uFF0B\uFF0D-\uFFFF]+[A-Za-z_$#0-9\u0080-\u2FFF\u3001-\uFF0B\uFF0D-\uFFFF]*
    ;

STRING_
    : SINGLE_QUOTED_TEXT
    | ALTERNATIVE_QUOTED_TEXT
    ;

SINGLE_QUOTED_TEXT
    : (SQ_ ('\'\'' | ~'\'')* SQ_)
    ;

ALTERNATIVE_QUOTED_TEXT
    : Q SQ_ LBT_ .*? RBT_ SQ_
    | Q SQ_ RBT_ .*? RBT_ SQ_
    | Q SQ_ LBE_ .*? RBE_ SQ_
    | Q SQ_ RBE_ .*? RBE_ SQ_
    | Q SQ_ LP_ .*? RP_ SQ_
    | Q SQ_ RP_ .*? RP_ SQ_
    | Q SQ_ LT_ .*? GT_ SQ_
    | Q SQ_ NOT_ .*? NOT_ SQ_
    | Q SQ_ TILDE_ .*? TILDE_ SQ_
    | Q SQ_ CARET_ .*? CARET_ SQ_
    | Q SQ_ VERTICAL_BAR_ .*? VERTICAL_BAR_ SQ_
    | Q SQ_ AMPERSAND_ .*? AMPERSAND_ SQ_
    | Q SQ_ MOD_ .*? MOD_ SQ_
    | Q SQ_ COLON_ .*? COLON_ SQ_
    | Q SQ_ PLUS_ .*? PLUS_ SQ_
    | Q SQ_ MINUS_ .*? MINUS_ SQ_
    | Q SQ_ ASTERISK_ .*? ASTERISK_ SQ_
    | Q SQ_ SLASH_ .*? SLASH_ SQ_
    | Q SQ_ BACKSLASH_ .*? BACKSLASH_ SQ_
    | Q SQ_ DOT_ .*? DOT_ SQ_
    | Q SQ_ EQ_ .*? EQ_ SQ_
    | Q SQ_ GT_ .*? GT_ SQ_
    | Q SQ_ LT_ .*? LT_ SQ_
    | Q SQ_ POUND_ .*? POUND_ SQ_
    | Q SQ_ COMMA_ .*? COMMA_ SQ_
    | Q SQ_ DQ_ .*? DQ_ SQ_
    | Q SQ_ BQ_ .*? BQ_ SQ_
    | Q SQ_ QUESTION_ .*? QUESTION_ SQ_
    | Q SQ_ AT_ .*? AT_ SQ_
    | Q SQ_ SEMI_ .*? SEMI_ SQ_
    | Q SQ_ DOLLAR_ .*? DOLLAR_ SQ_
    | Q SQ_ UL_ .*? UL_ SQ_
    | Q SQ_ A .*? A SQ_
    | Q SQ_ B .*? B SQ_
    | Q SQ_ C .*? C SQ_
    | Q SQ_ D .*? D SQ_
    | Q SQ_ E .*? E SQ_
    | Q SQ_ F .*? F SQ_
    | Q SQ_ G .*? G SQ_
    | Q SQ_ H .*? H SQ_
    | Q SQ_ I .*? I SQ_
    | Q SQ_ J .*? J SQ_
    | Q SQ_ K .*? K SQ_
    | Q SQ_ L .*? L SQ_
    | Q SQ_ M .*? M SQ_
    | Q SQ_ N .*? N SQ_
    | Q SQ_ O .*? O SQ_
    | Q SQ_ P .*? P SQ_
    | Q SQ_ Q .*? Q SQ_
    | Q SQ_ R .*? R SQ_
    | Q SQ_ S .*? S SQ_
    | Q SQ_ T .*? T SQ_
    | Q SQ_ U .*? U SQ_
    | Q SQ_ V .*? V SQ_
    | Q SQ_ W .*? W SQ_
    | Q SQ_ X .*? X SQ_
    | Q SQ_ Y .*? Y SQ_
    | Q SQ_ Z .*? Z SQ_
    | Q SQ_ '0' .*? '0' SQ_
    | Q SQ_ '1' .*? '1' SQ_
    | Q SQ_ '2' .*? '2' SQ_
    | Q SQ_ '3' .*? '3' SQ_
    | Q SQ_ '4' .*? '4' SQ_
    | Q SQ_ '5' .*? '5' SQ_
    | Q SQ_ '6' .*? '6' SQ_
    | Q SQ_ '7' .*? '7' SQ_
    | Q SQ_ '8' .*? '8' SQ_
    | Q SQ_ '9' .*? '9' SQ_
    ;

DOUBLE_QUOTED_TEXT
    : (DQ_ ( '\\'. | '""' | ~('"'| '\\') )* DQ_)
    ;

INTEGER_
    : INT_
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

NCHAR_TEXT
    : N STRING_
    ;

UCHAR_TEXT
    : U STRING_
    ;

fragment INT_
    : [0-9]+
    ;

fragment HEX_
    : [0-9a-fA-F]
    ;
