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

grammar RQLStatement;

import Keyword, Literals, Symbol;

showResources
    : SHOW RESOURCES (FROM schemaName)?
    ;

showShardingTableRules
    : SHOW SHARDING TABLE (tableRule | RULES) (FROM schemaName)?
    ;

showShardingBindingTableRules
    : SHOW SHARDING BINDING TABLE RULES (FROM schemaName)?
    ;

showShardingBroadcastTableRules
    : SHOW SHARDING BROADCAST TABLE RULES (FROM schemaName)?
    ;

showReadwriteSplittingRules
    : SHOW READWRITE_SPLITTING RULES (FROM schemaName)?
    ;

showDatabaseDiscoveryRules
    : SHOW DB_DISCOVERY RULES (FROM schemaName)?
    ;

showEncryptRules
    : SHOW ENCRYPT (TABLE tableRule | RULES) (FROM schemaName)?
    ;

tableRule
    : RULE tableName
    ;

schemaName
    : IDENTIFIER
    ;

tableName
    : IDENTIFIER
    ;
