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

grammar RDLStatement;

import BaseRule;

createEncryptRule
    : CREATE ENCRYPT RULE ifNotExists? encryptRuleDefinition (COMMA_ encryptRuleDefinition)*
    ;

alterEncryptRule
    : ALTER ENCRYPT RULE encryptRuleDefinition (COMMA_ encryptRuleDefinition)*
    ;

dropEncryptRule
    : DROP ENCRYPT RULE ifExists? tableName (COMMA_ tableName)*
    ;

encryptRuleDefinition
    : tableName encryptTableRuleDefinition
    ;

encryptTableRuleDefinition
    : LP_ COLUMNS LP_ encryptColumnDefinition (COMMA_ encryptColumnDefinition)* RP_ RP_
    ;

encryptColumnDefinition
    : LP_ columnDefinition COMMA_ cipherColumnDefinition (COMMA_ assistedQueryColumnDefinition)? (COMMA_ likeQueryColumnDefinition)? COMMA_ encryptAlgorithm (COMMA_ assistedQueryAlgorithm)? (COMMA_ likeQueryAlgorithm)? RP_
    ;

columnDefinition
    : NAME EQ_ columnName
    ;

cipherColumnDefinition
    :  CIPHER EQ_ cipherColumnName
    ;

cipherColumnName
    : IDENTIFIER_
    ;

assistedQueryColumnDefinition
    : (ASSISTED_QUERY | ASSISTED_QUERY_COLUMN) EQ_ assistedQueryColumnName
    ;

assistedQueryColumnName
    : IDENTIFIER_
    ;

likeQueryColumnDefinition
    : (LIKE_QUERY | LIKE_QUERY_COLUMN) EQ_ likeQueryColumnName
    ;

likeQueryColumnName
    : IDENTIFIER_
    ;

encryptAlgorithm
    : ENCRYPT_ALGORITHM LP_ algorithmDefinition RP_
    ;

assistedQueryAlgorithm
    : ASSISTED_QUERY_ALGORITHM LP_ algorithmDefinition RP_
    ;

likeQueryAlgorithm
    : LIKE_QUERY_ALGORITHM LP_ algorithmDefinition RP_
    ;

