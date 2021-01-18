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

package org.apache.shardingsphere.db.protocol.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common error code.
 */
@RequiredArgsConstructor
@Getter
public enum CommonErrorCode implements SQLErrorCode {
    
    CIRCUIT_BREAK_MODE(10000, "C10000", "Circuit break mode is ON."),
    
    SHARDING_TABLE_RULES_NOT_EXISTED(11001, "C11001", "Sharding table rule %s is not exist."),
    
    TABLES_IN_USED(11002, "C11002", "Can not drop rule, tables %s in the rule are still in used."),

    RESOURCE_IN_USED(11003, "C11003", "Can not drop resources, resources %s in the rule are still in used."),
    
    RESOURCE_NOT_EXIST(11004, "C11004", "Can not drop resources, resources %s do not exist."),
    
    REPLICA_QUERY_RULE_NOT_EXIST(11005, "C11005", "Replica query rule does not exist."),
    
    REPLICA_QUERY_RULE_DATA_SOURCE_NOT_EXIST(11006, "C11006", "Data sources %s in replica query rule do not exist."),
    
    ADD_REPLICA_QUERY_RULE_DATA_SOURCE_EXIST(11007, "C11007", "Can not add replica query rule, data sources %s in replica query rule already exists."),
    
    REPLICA_QUERY_RULE_EXIST(11008, "C11008", "Replica query rule already exists."),
    
    UNSUPPORTED_COMMAND(19998, "C19998", "Unsupported command: [%s]"),
    
    UNKNOWN_EXCEPTION(19999, "C19999", "Unknown exception: [%s]");
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
}
