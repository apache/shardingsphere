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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP client-facing safety policy.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPClientSafetyPolicy {
    
    public static final int DEFAULT_MAX_TOOL_CALLS_PER_SESSION = 10000;
    
    public static final String MAX_TOOL_CALLS_PER_SESSION_PROPERTY = "shardingsphere.mcp.maxToolCallsPerSession";
    
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
     * Create model-facing safety policy payload.
     *
     * @return model-facing safety policy payload
     */
    public static Map<String, Object> createModelFacingPayload() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("identity_scope", "mcp_session");
        result.put("authorization_scope", "HTTP bearer token when configured; STDIO inherits the local process boundary.");
        result.put("tool_call_limit", createToolCallLimitPayload());
        result.put("abuse_guard", "Every tool call is counted before dispatch, including invalid calls, so runaway model loops stop at the session quota.");
        result.put("external_model_boundary", "The MCP runtime never calls external model providers; live LLM E2E clients call configured endpoints outside the server.");
        return result;
    }
    
    private static Map<String, Object> createToolCallLimitPayload() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("scope", "session");
        result.put("max_calls", getMaxToolCallsPerSession());
        result.put("property", MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        result.put("recovery", "Close and recreate the MCP session after the quota is exhausted.");
        return result;
    }
}
