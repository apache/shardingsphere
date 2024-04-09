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
OR_:                 '||';
NOT_:                '!';
TILDE_:              '~';
VERTICAL_BAR_:       '|';
AMPERSAND_:          '&';
SIGNED_LEFT_SHIFT_:  '<<';
SIGNED_RIGHT_SHIFT_: '>>';
CARET_:              '^';
MOD_:                '%';
COLON_:              ':';
PLUS_:               '+';
MINUS_:              '-';
ASTERISK_:           '*';
SLASH_:              '/';
BACKSLASH_:          '\\';
DOT_:                '.';
DOT_ASTERISK_:       '.*';
SAFE_EQ_:            '<=>';
DEQ_:                '==';
EQ_:                 '=';
CQ_:                 ':=';
NEQ_:                '<>' | '!=';
GT_:                 '>';
GTE_:                '>=';
LT_:                 '<';
LTE_:                '<=';
POUND_:              '#';
LP_:                 '(';
RP_:                 ')';
LBE_:                '{';
RBE_:                '}';
LBT_:                '[';
RBT_:                ']';
COMMA_:              ',';
DQ_:                 '"';
SQ_ :                '\'';
BQ_:                 '`';
QUESTION_:           '?';
DOLLAR_:             '$';
AT_:                 '@';
SEMI_:               ';';
TILDE_TILDE_:        '~~';
NOT_TILDE_TILDE_:    '!~~';
TYPE_CAST_:          '::';
ILIKE_:              '~~*';
NOT_ILIKE_:          '!~~*';
UNICODE_ESCAPE:      'U&' | 'u&';
JSON_EXTRACT_:       '->';
JSON_EXTRACT_TEXT_:  '->>';
JSON_PATH_EXTRACT_:  '#>';
JSON_PATH_EXTRACT_TEXT_:        '#>>';
JSONB_CONTAIN_RIGHT_:           '@>';
JSONB_CONTAIN_LEFT_:            '<@';
JSONB_CONTAIN_ALL_TOP_KEY_:     '?&';
JSONB_PATH_DELETE_:             '#-';
JSONB_PATH_CONTAIN_ANY_VALUE_:  '@?';
JSONB_PATH_PREDICATE_CHECK_:    '@@';
GEOMETRIC_LENGTH_:              '@-@';
GEOMETRIC_DISTANCE_:            '<->';
GEOMETRIC_EXTEND_RIGHT_:        '&<';
GEOMETRIC_EXTEND_LEFT_:         '&>';
GEOMETRIC_STRICT_BELOW_:        '<<|';
GEOMETRIC_STRICT_ABOVE_:        '|>>';
GEOMETRIC_EXTEND_ABOVE_:        '&<|';
GEOMETRIC_EXTEND_BELOW_:        '|&>';
GEOMETRIC_BELOW_:               '<^';
GEOMETRIC_ABOVE_:               '>^';
GEOMETRIC_INTERSECT_:           '?#';
GEOMETRIC_PERPENDICULAR_:       '?-|';
GEOMETRIC_SAME_AS_:             '~=';
SIGNED_LEFT_SHIFT_E_:           '<<=';
SIGNED_RIGHT_SHIFT_E_:          '>>=';
DN_:                            '!!';
CUBE_ROOT_:                     '||/';
