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

IDENTIFIER_: IDENTIFIER_START_CHAR_ IDENTIFIER_CHAR_* | DQ_ (DQ_ DQ_ | ~'"')+ DQ_;
STRING_: E ESCAPE_STRING_LITERAL_ (STRING_CONTINUATION_ ESCAPE_STRING_LITERAL_)* | STRING_LITERAL_ (STRING_CONTINUATION_ STRING_LITERAL_)*;
NUMBER_: (INT_ (DOT_ INT_?)? | DOT_ INT_) (E (PLUS_ | MINUS_)? INT_)?;
HEX_DIGIT_: X SQ_ HEX_* SQ_ (STRING_CONTINUATION_ SQ_ HEX_* SQ_)*;
BIT_NUM_: B SQ_ [01]* SQ_ (STRING_CONTINUATION_ SQ_ [01]* SQ_)*;
HEX_NUMBER_: '0' X '_'? HEX_ ('_'? HEX_)*;
BINARY_NUMBER_: '0' B '_'? [01] ('_'? [01])*;
OCTAL_NUMBER_: '0' O '_'? [0-7] ('_'? [0-7])*;

fragment INT_: [0-9] ('_'? [0-9])*;
fragment HEX_: [0-9a-fA-F];
fragment STRING_CONTENT_: '\'\'' | ~'\'';
fragment ESCAPE_STRING_CONTENT_: '\\' . | '\'\'' | ~('\'' | '\\');
fragment STRING_LITERAL_: SQ_ STRING_CONTENT_* SQ_;
fragment ESCAPE_STRING_LITERAL_: SQ_ ESCAPE_STRING_CONTENT_* SQ_;
fragment STRING_CONTINUATION_: [ \t\f]* ('\r'? '\n' | '\r') [ \t\r\n\f]*;

fragment IDENTIFIER_START_CHAR_
   : [a-zA-Z_]
   | [\u00AA\u00B5\u00BA\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u00FF]
   | [\u0100-\uD7FF\uE000-\uFFFF]
   | [\uD800-\uDBFF] [\uDC00-\uDFFF]
   ;

fragment IDENTIFIER_CHAR_
   : STRICT_IDENTIFIER_CHAR_
   | DOLLAR_
   ;

fragment STRICT_IDENTIFIER_CHAR_
   : IDENTIFIER_START_CHAR_
   | [0-9]
   ;

DEFAULT_DOES_NOT_MATCH_ANYTHING
   : 'Default does not match anything'
   ;

APOSTROPHE_SKIP
   : 'skip'
   ;
