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
    
    CIRCUIT_BREAK_MODE(1000, "C1000", "Circuit break mode is ON."),
    
    SHARDING_TABLE_RULES_NOT_EXISTED(1101, "C1101", "Sharding table rules %s do not exist in schema %s."),
    
    SHARDING_TABLE_RULES_IN_USED_BY_BINDING_TABLE(1102, "C1102", "Sharding table rules %s are still used by binding table rule."),

    RESOURCE_IN_USED(1103, "C1103", "Resources %s in the rule are still in used."),
    
    RESOURCE_NOT_EXIST(1104, "C1104", "Resources %s do not exist."),
    
    READWRITE_SPLITTING_RULES_NOT_EXIST(1105, "C1105", "Readwrite splitting rules %s do not exist in schema %s."),
    
    ADD_REPLICA_QUERY_RULE_DATA_SOURCE_EXIST(1107, "C1107", "Data sources %s in replica query rule already exists."),
    
    INVALID_RESOURCE(1111, "C1111", "Can not add invalid resources %s."),
    
    DUPLICATE_RESOURCE(1112, "C1112", "Duplicate resource names %s."),
    
    DUPLICATE_TABLE(1113, "C1113", "Duplicate table names %s."),
    
    SHARDING_BROADCAST_EXIST(1114, "C1114", "Sharding broadcast table rules already exists in schema %s."),

    SHARDING_BINDING_TABLE_RULES_NOT_EXIST(1115, "C1115", "Sharding binding table rules do not exist in schema %s."),

    SHARDING_BROADCAST_TABLE_RULES_NOT_EXIST(1116, "C1116", "Sharding broadcast table rules do not exist in schema %s."),

    INVALID_LOAD_BALANCERS(1117, "C1117", "Invalid load balancers %s."),

    DATABASE_DISCOVERY_RULE_EXIST(1118, "C1118", "Database discovery rule already exists in schema %s."),

    INVALID_DATABASE_DISCOVERY_TYPES(1119, "C1119", "Invalid database discovery types %s."),

    DATABASE_DISCOVERY_RULES_NOT_EXIST(1120, "C1120", "Database discovery rules %s do not exist in schema %s."),

    ENCRYPT_RULE_EXIST(1122, "C1122", "Encrypt rule already exists in schema %s."),

    INVALID_ENCRYPTORS(1123, "C1123", "Invalid encryptors %s."),

    ENCRYPT_RULES_NOT_EXIST(1124, "C1124", "Encrypt rules %s do not exist in schema %s."),

    DUPLICATE_RULE_NAMES(1125, "C1125", "Duplicate rule names %s in schema %s"),

    SCALING_JOB_NOT_EXIST(1201, "C1201", "Scaling job %s does not exist."),
    
    SCALING_OPERATE_FAILED(1209, "C1209", "Scaling Operate Failed: [%s]"),
    
    TABLE_LOCK_WAIT_TIMEOUT(1301, "C1301", "The table %s of schema %s lock wait timeout of %s ms exceeded"),
    
    TABLE_LOCKED(1302, "C1302", "The table %s of schema %s is locked"),
    
    UNSUPPORTED_COMMAND(1998, "C1998", "Unsupported command: [%s]"),
    
    UNKNOWN_EXCEPTION(1999, "C1999", "Unknown exception: [%s]");
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
}
