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
    : CREATE ENCRYPT RULE encryptRuleDefinition (COMMA encryptRuleDefinition)*
    ;

alterEncryptRule
    : ALTER ENCRYPT RULE encryptRuleDefinition (COMMA encryptRuleDefinition)*
    ;

dropEncryptRule
    : DROP ENCRYPT RULE ifExists? tableName (COMMA tableName)*
    ;

encryptRuleDefinition
    : tableName LP (resourceDefinition COMMA)? COLUMNS LP encryptColumnDefinition (COMMA encryptColumnDefinition)* RP (COMMA QUERY_WITH_CIPHER_COLUMN EQ queryWithCipherColumn)? RP
    ;

resourceDefinition
    : RESOURCE EQ resourceName 
    ;

resourceName
    : IDENTIFIER
    ;

encryptColumnDefinition
    : LP columnDefinition (COMMA plainColumnDefinition)? COMMA cipherColumnDefinition (COMMA assistedQueryColumnDefinition)? (COMMA fuzzyQueryColumnDefinition)? COMMA algorithmDefinition (COMMA algorithmDefinition)? (COMMA algorithmDefinition)? RP
    ;

columnDefinition
    : NAME EQ columnName (COMMA DATA_TYPE EQ dataType)?
    ;

columnName
    : IDENTIFIER
    ;

dataType
    : STRING 
    ;

plainColumnDefinition
    : PLAIN EQ plainColumnName (COMMA PLAIN_DATA_TYPE EQ dataType)?
    ;

plainColumnName
    : IDENTIFIER
    ;

cipherColumnDefinition
    :  CIPHER EQ cipherColumnName (COMMA CIPHER_DATA_TYPE EQ dataType)?
    ;

cipherColumnName
    : IDENTIFIER
    ;

assistedQueryColumnDefinition
    : ASSISTED_QUERY_COLUMN EQ assistedQueryColumnName (COMMA ASSISTED_QUERY_DATA_TYPE EQ dataType)?
    ;

assistedQueryColumnName
    : IDENTIFIER
    ;

fuzzyQueryColumnDefinition
    : FUZZY_QUERY_COLUMN EQ fuzzyQueryColumnName (COMMA FUZZY_QUERY_DATA_TYPE EQ dataType)?
    ;

fuzzyQueryColumnName
    : IDENTIFIER
    ;
    
queryWithCipherColumn
    : TRUE | FALSE
    ;

ifExists
    : IF EXISTS
    ;
