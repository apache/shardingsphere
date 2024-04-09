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
 
grammar BaseRule;

import Symbol, Keyword, Literals;

literal
    : STRING_ | (MINUS_)? INT_ | TRUE | FALSE
    ;

algorithmDefinition
    : TYPE LP_ NAME EQ_ algorithmTypeName (COMMA_ propertiesDefinition)? RP_
    ;

algorithmTypeName
    : STRING_ | buildInMaskAlgorithmType
    ;

buildInMaskAlgorithmType
    : MD5
    | KEEP_FIRST_N_LAST_M
    | KEEP_FROM_X_TO_Y
    | MASK_FIRST_N_LAST_M
    | MASK_FROM_X_TO_Y
    | MASK_BEFORE_SPECIAL_CHARS
    | MASK_AFTER_SPECIAL_CHARS
    | GENERIC_TABLE_RANDOM_REPLACE
    ;

propertiesDefinition
    : PROPERTIES LP_ properties? RP_
    ;

properties
    : property (COMMA_ property)*
    ;

property
    : key=STRING_ EQ_ value=literal
    ;

ruleName
    : IDENTIFIER_
    ;
