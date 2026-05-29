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

package org.apache.shardingsphere.mcp.support.security;

import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP runtime protection policy.
 */
public final class MCPRuntimeProtectionPolicy {
    
    public static final int DEFAULT_MAX_TOOL_CALLS_PER_SESSION = 10000;
    
    public static final String MAX_TOOL_CALLS_PER_SESSION_PROPERTY = "shardingsphere.mcp.maxToolCallsPerSession";
    
    public static final int DEFAULT_MAX_ROWS = 100;
    
    public static final int MAX_ROWS_LIMIT = 5000;
    
    public static final int DEFAULT_TIMEOUT_MILLISECONDS = 0;
    
    public static final int MAX_TIMEOUT_MILLISECONDS = 300000;
    
    private MCPRuntimeProtectionPolicy() {
    }
    
    /**
     * Get maximum tool calls per MCP session.
     *
     * @return maximum tool calls per MCP session
     */
    public static int getMaxToolCallsPerSession() {
        Integer configuredValue = Integer.getInteger(MAX_TOOL_CALLS_PER_SESSION_PROPERTY, DEFAULT_MAX_TOOL_CALLS_PER_SESSION);
        return configuredValue > 0 ? configuredValue : DEFAULT_MAX_TOOL_CALLS_PER_SESSION;
    }
    
    /**
     * Create tool call limit payload.
     *
     * @return tool call limit payload
     */
    public static Map<String, Object> createToolCallLimitPayload() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("scope", "session");
        result.put("max_calls", getMaxToolCallsPerSession());
        result.put("property", MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        result.put(MCPPayloadFieldNames.RECOVERY, "Close and recreate the MCP session after the quota is exhausted.");
        return result;
    }
    
    /**
     * Create runtime protection payload.
     *
     * @return runtime protection payload
     */
    public static Map<String, Object> createRuntimeProtectionPayload() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("tool_call_limit", createToolCallLimitPayload());
        result.put("sql_execution_limits", createSQLExecutionLimitsPayload());
        return result;
    }
    
    private static Map<String, Object> createSQLExecutionLimitsPayload() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("max_rows", createMaxRowsPayload());
        result.put("timeout_ms", createTimeoutPayload());
        return result;
    }
    
    private static Map<String, Object> createMaxRowsPayload() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("default_value", DEFAULT_MAX_ROWS);
        result.put("maximum_value", MAX_ROWS_LIMIT);
        result.put("applied_field", "applied_max_rows");
        result.put("truncation_field", "truncated");
        result.put(MCPPayloadFieldNames.RECOVERY, "Retry with a narrower SELECT, stronger WHERE clause, or smaller projection when rows are truncated.");
        return result;
    }
    
    private static Map<String, Object> createTimeoutPayload() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("default_value", DEFAULT_TIMEOUT_MILLISECONDS);
        result.put("maximum_value", MAX_TIMEOUT_MILLISECONDS);
        result.put("applied_field", "applied_timeout_ms");
        result.put("zero_means", "server_default");
        result.put(MCPPayloadFieldNames.RECOVERY, "Retry with a bounded timeout_ms value or omit timeout_ms to use the server default.");
        return result;
    }
}
