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

createDatabaseDiscoveryRule
    : CREATE DB_DISCOVERY RULE databaseDiscoveryRule (COMMA databaseDiscoveryRule)*
    ;

alterDatabaseDiscoveryRule
    : ALTER DB_DISCOVERY RULE databaseDiscoveryRule (COMMA databaseDiscoveryRule)*
    ;

dropDatabaseDiscoveryRule
    : DROP DB_DISCOVERY RULE existClause? ruleName (COMMA ruleName)*
    ;

createDatabaseDiscoveryType
    : CREATE DB_DISCOVERY TYPE databaseDiscoveryTypeDefinition (COMMA databaseDiscoveryTypeDefinition)*
    ;

alterDatabaseDiscoveryType
    : ALTER DB_DISCOVERY TYPE databaseDiscoveryTypeDefinition (COMMA databaseDiscoveryTypeDefinition)*
    ;

dropDatabaseDiscoveryType
    : DROP DB_DISCOVERY TYPE existClause? discoveryTypeName (COMMA discoveryTypeName)*
    ;

createDatabaseDiscoveryHeartbeat
    : CREATE DB_DISCOVERY HEARTBEAT heartbeatDefinition (COMMA heartbeatDefinition)*
    ;

alterDatabaseDiscoveryHeartbeat
    : ALTER DB_DISCOVERY HEARTBEAT heartbeatDefinition (COMMA heartbeatDefinition)*
    ;

dropDatabaseDiscoveryHeartbeat
    : DROP DB_DISCOVERY HEARTBEAT existClause? discoveryHeartbeatName (COMMA discoveryHeartbeatName)*
    ;

databaseDiscoveryRule
    : (databaseDiscoveryRuleDefinition | databaseDiscoveryRuleConstruction)
    ;

databaseDiscoveryRuleDefinition
    : ruleName LP resources COMMA typeDefinition COMMA discoveryHeartbeat RP
    ;

databaseDiscoveryRuleConstruction
    : ruleName LP resources COMMA TYPE EQ discoveryTypeName COMMA HEARTBEAT EQ discoveryHeartbeatName RP
    ;

databaseDiscoveryTypeDefinition
    : discoveryTypeName LP typeDefinition RP
    ;

heartbeatDefinition
    : discoveryHeartbeatName LP PROPERTIES LP properties RP RP  
    ;

ruleName
    : IDENTIFIER
    ;

resources
    : RESOURCES LP resourceName (COMMA resourceName)* RP
    ;

resourceName
    : IDENTIFIER
    ;

typeDefinition
    : TYPE LP NAME EQ discoveryTypeName (COMMA PROPERTIES LP properties RP)? RP
    ;

discoveryHeartbeat
    : HEARTBEAT LP PROPERTIES LP properties RP RP
    ;

properties
    : property (COMMA property)*
    ;

property
    : key=(IDENTIFIER | STRING) EQ value=(NUMBER | INT | STRING)
    ;

discoveryTypeName
    : IDENTIFIER
    ;

discoveryHeartbeatName
    : IDENTIFIER
    ;

existClause
    : IF EXISTS
    ;
