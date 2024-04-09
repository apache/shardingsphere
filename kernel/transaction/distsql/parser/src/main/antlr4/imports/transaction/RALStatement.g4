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

grammar RALStatement;

import BaseRule;

showTransactionRule
    : SHOW TRANSACTION RULE
    ;

alterTransactionRule
    : ALTER TRANSACTION RULE transactionRuleDefinition
    ;

transactionRuleDefinition
    : LP_ DEFAULT EQ_ defaultType (COMMA_ providerDefinition)? RP_
    ;

providerDefinition
    : TYPE LP_ NAME EQ_ providerName (COMMA_ propertiesDefinition)? RP_
    ;

defaultType
    : STRING_ | buildInDefaultTransactionType
    ;

buildInDefaultTransactionType
    : LOCAL | XA | BASE
    ;

providerName
    : STRING_ | buildInProviderTypeName
    ;

buildInProviderTypeName
    : ATOMIKOS | NARAYANA
    ;
