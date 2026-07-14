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

package org.apache.shardingsphere.mcp.core.tool.response;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPRuntimeDatabaseRecoveryPayloadFactory;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationResult;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * MCP response for runtime database validation.
 */
public final class RuntimeDatabaseValidationResponse implements MCPResponse {
    
    private static final String STATUS_READY = "ready";
    
    private final RuntimeDatabaseValidationResult validationResult;
    
    private final Map<String, Object> recovery;
    
    private RuntimeDatabaseValidationResponse(final RuntimeDatabaseValidationResult validationResult) {
        this.validationResult = validationResult;
        recovery = isReady()
                ? Map.of()
                : MCPRuntimeDatabaseRecoveryPayloadFactory.create(validationResult.getDatabase(), validationResult.getCategory());
    }
    
    /**
     * Create a runtime database validation response.
     *
     * @param validationResult validation result
     * @return validation response
     */
    public static RuntimeDatabaseValidationResponse from(final RuntimeDatabaseValidationResult validationResult) {
        return new RuntimeDatabaseValidationResponse(validationResult);
    }
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("response_mode", MCPResponseMode.VALIDATION);
        result.put(MCPPayloadFieldNames.SUMMARY, createSummary());
        result.put("status", validationResult.getStatus());
        result.put("database", validationResult.getDatabase());
        result.put("checks", createChecksPayload());
        result.put("category", validationResult.getCategory());
        result.put(MCPPayloadFieldNames.RECOVERY, recovery);
        if (recovery.containsKey(MCPPayloadFieldNames.NEXT_ACTIONS)) {
            result.put(MCPPayloadFieldNames.NEXT_ACTIONS, recovery.get(MCPPayloadFieldNames.NEXT_ACTIONS));
        }
        return result;
    }
    
    private String createSummary() {
        return isReady()
                ? String.format("Runtime database `%s` passed validation.", validationResult.getDatabase())
                : String.format("Runtime database `%s` failed validation with category `%s`.", validationResult.getDatabase(), validationResult.getCategory());
    }
    
    private boolean isReady() {
        return STATUS_READY.equals(validationResult.getStatus());
    }
    
    private List<Map<String, Object>> createChecksPayload() {
        List<Map<String, Object>> result = new LinkedList<>();
        for (RuntimeDatabaseValidationCheckResult each : validationResult.getChecks()) {
            Map<String, Object> check = new LinkedHashMap<>(4, 1F);
            check.put("name", each.getName());
            check.put("status", each.getStatus());
            check.put("category", each.getCategory());
            check.put("message", each.getMessage());
            result.add(check);
        }
        return result;
    }
}
