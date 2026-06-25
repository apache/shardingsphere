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

package org.apache.shardingsphere.mcp.support.database.tool.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime database validation result.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class RuntimeDatabaseValidationResult implements MCPResponse {
    
    private final String status;
    
    private final String database;
    
    private final List<RuntimeDatabaseValidationCheckResult> checks;
    
    private final String category;
    
    private final Map<String, Object> recovery;
    
    /**
     * Create a success result.
     *
     * @param database database name
     * @param checks check results
     * @return validation result
     */
    public static RuntimeDatabaseValidationResult ready(final String database, final List<RuntimeDatabaseValidationCheckResult> checks) {
        return new RuntimeDatabaseValidationResult("ready", Objects.toString(database, ""), checks, "ready", Map.of());
    }
    
    /**
     * Create a failure result.
     *
     * @param database database name
     * @param checks check results
     * @param category failure category
     * @param recovery recovery payload
     * @return validation result
     */
    public static RuntimeDatabaseValidationResult failed(final String database, final List<RuntimeDatabaseValidationCheckResult> checks, final String category, final Map<String, Object> recovery) {
        return new RuntimeDatabaseValidationResult("failed", Objects.toString(database, ""), checks, category, null == recovery ? Map.of() : recovery);
    }
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_mode", MCPResponseMode.VALIDATION);
        result.put("status", status);
        result.put("database", database);
        result.put("checks", createChecksPayload());
        result.put("category", category);
        result.put(MCPPayloadFieldNames.RECOVERY, recovery);
        return result;
    }
    
    private List<Map<String, Object>> createChecksPayload() {
        List<Map<String, Object>> result = new LinkedList<>();
        for (RuntimeDatabaseValidationCheckResult each : checks) {
            result.add(each.toPayload());
        }
        return result;
    }
}
