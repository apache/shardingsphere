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
    : CREATE DB_DISCOVERY RULE ifNotExists? databaseDiscoveryRule (COMMA_ databaseDiscoveryRule)*
    ;

alterDatabaseDiscoveryRule
    : ALTER DB_DISCOVERY RULE databaseDiscoveryRule (COMMA_ databaseDiscoveryRule)*
    ;

dropDatabaseDiscoveryRule
    : DROP DB_DISCOVERY RULE ifExists? ruleName (COMMA_ ruleName)*
    ;

dropDatabaseDiscoveryType
    : DROP DB_DISCOVERY TYPE ifExists? discoveryTypeName (COMMA_ discoveryTypeName)*
    ;

dropDatabaseDiscoveryHeartbeat
    : DROP DB_DISCOVERY HEARTBEAT ifExists? discoveryHeartbeatName (COMMA_ discoveryHeartbeatName)*
    ;

databaseDiscoveryRule
    : ruleName LP_ storageUnits COMMA_ algorithmDefinition COMMA_ discoveryHeartbeat RP_
    ;

ruleName
    : IDENTIFIER_
    ;

storageUnits
    : STORAGE_UNITS LP_ storageUnitName (COMMA_ storageUnitName)* RP_
    ;

storageUnitName
    : IDENTIFIER_
    ;

discoveryHeartbeat
    : HEARTBEAT LP_ propertiesDefinition RP_
    ;

discoveryTypeName
    : IDENTIFIER_
    ;

discoveryHeartbeatName
    : IDENTIFIER_
    ;

ifExists
    : IF EXISTS
    ;

ifNotExists
    : IF NOT EXISTS
    ;
