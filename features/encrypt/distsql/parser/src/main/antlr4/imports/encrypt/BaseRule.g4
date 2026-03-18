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
    : STRING_ | buildInEncryptAlgorithmType
    ;

buildInEncryptAlgorithmType
    : standardEncryptAlgorithm
    | assistedEncryptAlgorithm
    ;

standardEncryptAlgorithm
    : AES
    ;

assistedEncryptAlgorithm
    : MD5
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

databaseName
    : IDENTIFIER_
    ;

tableName
    : IDENTIFIER_
    ;

columnName
    : IDENTIFIER_
    ;

ifExists
    : IF EXISTS
    ;

ifNotExists
    : IF NOT EXISTS
    ;
