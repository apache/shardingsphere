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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared JSON-RPC protocol support for MCP E2E clients.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPInteractionProtocolSupport {
    
    public static final String PROTOCOL_VERSION = "2025-11-25";
    
    /**
     * Create initialize request parameters for the given client name.
     *
     * @param clientName client name
     * @return initialize request parameters
     */
    public static Map<String, Object> createInitializeRequestParams(final String clientName) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", clientName, "version", "1.0.0"));
        return result;
    }
    
    /**
     * Create a JSON-RPC request payload.
     *
     * @param requestId request identifier
     * @param method request method
     * @param params request parameters
     * @return JSON-RPC request payload
     */
    public static Map<String, Object> createJsonRpcRequest(final String requestId, final String method, final Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("jsonrpc", "2.0");
        result.put("id", requestId);
        result.put("method", method);
        result.put("params", params);
        return result;
    }
    
    /**
     * Create a JSON-RPC notification payload.
     *
     * @param method notification method
     * @param params notification parameters
     * @return JSON-RPC notification payload
     */
    public static Map<String, Object> createJsonRpcNotification(final String method, final Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("jsonrpc", "2.0");
        result.put("method", method);
        result.put("params", params);
        return result;
    }
    
    /**
     * Create a JSON-RPC request body.
     *
     * @param requestId request identifier
     * @param method request method
     * @param params request parameters
     * @return JSON request body
     */
    public static String createJsonRpcRequestBody(final String requestId, final String method, final Map<String, Object> params) {
        return JsonUtils.toJsonString(createJsonRpcRequest(requestId, method, params));
    }
}
