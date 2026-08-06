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

lexer grammar Symbol;

AND_:                '&&';
OR_:                 '||' | '\uFF5C\uFF5C' | '\uFF5C|' | '|\uFF5C';
NOT_:                '!';
TILDE_:              '~';
VERTICAL_BAR_:       '|' | '\uFF5C';
AMPERSAND_:          '&';
SIGNED_LEFT_SHIFT_:  '<<';
SIGNED_RIGHT_SHIFT_: '>>';
CARET_:              '^';
MOD_:                '%';
COLON_:              ':';
PLUS_:               '+' | '\uFF0B';
MINUS_:              '-' | '\uFF0D';
ASTERISK_:           '*' | '\uFF0A';
SLASH_:              '/' | '\uFF0F';
BACKSLASH_:          '\\';
DOT_:                '.';
DOT_ASTERISK_:       '.*' | '.\uFF0A';
SAFE_EQ_:            '<=>';
DEQ_:                '==';
EQ_:                 '=' | '\uFF1D';
NEQ_:                '<>' | '<\uFF1E' | '\uFF1C>' | '\uFF1C\uFF1E' | '!=' | '!\uFF1D' | '\uFF01=' | '\uFF01\uFF1D' | '^=';
GT_:                 '>' | '\uFF1E';
GTE_:                '>=' | '>\uFF1D' | '\uFF1E=' | '\uFF1E\uFF1D';
LT_:                 '<' | '\uFF1C';
LTE_:                '<=' | '<\uFF1D' | '\uFF1C=' | '\uFF1C\uFF1D';
POUND_:              '#';
LP_:                 '(' | '\uFF08';
RP_:                 ')' | '\uFF09';
LBE_:                '{';
RBE_:                '}';
LBT_:                '[';
RBT_:                ']';
COMMA_:              ',' | '\uFF0C';
DQ_:                 '"';
SQ_ :                '\'';
BQ_:                 '`';
QUESTION_:           '?';
AT_:                 '@';
SEMI_:               ';';
DOLLAR_:             '$';
ASSIGNMENT_OPERATOR_:':=';
ARROW_:              '=>';
EXPONENT_:           '**';
RANGE_OPERATOR_:     '..';
