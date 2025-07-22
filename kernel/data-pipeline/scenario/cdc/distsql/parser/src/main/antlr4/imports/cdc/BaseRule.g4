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

algorithmDefinition
    : TYPE LP_ NAME EQ_ algorithmTypeName (COMMA_ propertiesDefinition)? RP_
    ;

algorithmTypeName
    : STRING_
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

literal
    : STRING_ | (MINUS_)? INT_ | TRUE | FALSE
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;

intValue
    : INT_
    ;
