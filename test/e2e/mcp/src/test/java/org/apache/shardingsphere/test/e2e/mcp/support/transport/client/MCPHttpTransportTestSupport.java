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

package org.apache.shardingsphere.test.e2e.mcp.support.transport.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

/**
 * HTTP transport support for MCP E2E tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPHttpTransportTestSupport {
    
    public static final String PROTOCOL_VERSION = MCPInteractionProtocolSupport.PROTOCOL_VERSION;
    
    private static final String CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT = "application/json, text/event-stream";
    
    /**
     * Create a JSON request builder for the given MCP endpoint.
     *
     * @param endpointUri MCP endpoint URI
     * @return JSON request builder
     */
    public static HttpRequest.Builder createJsonRequestBuilder(final URI endpointUri) {
        return HttpRequest.newBuilder(endpointUri)
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", ACCEPT);
    }
    
    /**
     * Create a session-bound JSON request builder for the given MCP endpoint.
     *
     * @param endpointUri MCP endpoint URI
     * @param sessionId MCP session id
     * @param protocolVersion MCP protocol version
     * @return session-bound JSON request builder
     */
    public static HttpRequest.Builder createSessionRequestBuilder(final URI endpointUri, final String sessionId, final String protocolVersion) {
        return createJsonRequestBuilder(endpointUri)
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", protocolVersion);
    }
    
    /**
     * Create initialize request parameters.
     *
     * @param clientName MCP client name
     * @return initialize request parameters
     */
    public static Map<String, Object> createInitializeRequestParams(final String clientName) {
        return MCPInteractionProtocolSupport.createInitializeRequestParams(clientName);
    }
    
    /**
     * Create a JSON-RPC request body.
     *
     * @param requestId request id
     * @param method method name
     * @param params request parameters
     * @return JSON-RPC request body
     */
    public static String createJsonRpcRequestBody(final String requestId, final String method, final Map<String, Object> params) {
        return MCPInteractionProtocolSupport.createJsonRpcRequestBody(requestId, method, params);
    }
}
