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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionResponse;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP MCP tool client.
 */
@RequiredArgsConstructor
public final class MCPHttpInteractionClient implements MCPInteractionClient {
    
    private static final String PROTOCOL_VERSION = "2025-11-25";
    
    private static final String INITIALIZE_REQUEST_ID = "init-1";
    
    private static final String CLIENT_NAME = "mcp-e2e-http";
    
    private final URI endpointUri;
    
    private final HttpClient httpClient;
    
    private String sessionId;
    
    private String actualProtocolVersion;
    
    @Override
    public void open() throws IOException, InterruptedException {
        if (null != sessionId) {
            return;
        }
        final HttpRequest request = HttpRequest.newBuilder(endpointUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", INITIALIZE_REQUEST_ID,
                        "method", "initialize",
                        "params", createInitializeRequestParams()))))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (200 != response.statusCode()) {
            throw new IllegalStateException("Failed to initialize MCP session.");
        }
        Map<String, Object> initializePayload = MCPInteractionPayloads.parseJsonPayload(response.body());
        if (MCPInteractionPayloads.hasJsonRpcError(initializePayload)) {
            throw new IllegalStateException("Failed to initialize MCP session: "
                    + MCPInteractionPayloads.getJsonRpcErrorPayload(initializePayload).get("message"));
        }
        sessionId = response.headers().firstValue("MCP-Session-Id")
                .orElseThrow(() -> new IllegalStateException("MCP initialize response does not contain MCP-Session-Id header."));
        actualProtocolVersion = response.headers().firstValue("MCP-Protocol-Version").orElse(PROTOCOL_VERSION);
    }
    
    @Override
    public MCPInteractionResponse call(final String actionName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        ensureOpened();
        HttpResponse<String> response = sendPostRequest(actionName + "-1", "tools/call", Map.of("name", actionName, "arguments", arguments));
        return new MCPInteractionResponse(MCPInteractionPayloads.getStructuredContent(MCPInteractionPayloads.parseJsonPayload(response.body())));
    }
    
    @Override
    public MCPInteractionResponse listResources() throws IOException, InterruptedException {
        ensureOpened();
        HttpResponse<String> response = sendPostRequest("resources-list-1", "resources/list", Map.of());
        return new MCPInteractionResponse(MCPInteractionPayloads.getListResourcesPayload(MCPInteractionPayloads.parseJsonPayload(response.body())));
    }
    
    @Override
    public MCPInteractionResponse readResource(final String resourceUri) throws IOException, InterruptedException {
        ensureOpened();
        HttpResponse<String> response = sendPostRequest("resources-read-1", "resources/read", Map.of("uri", resourceUri));
        return new MCPInteractionResponse(MCPInteractionPayloads.getFirstResourcePayload(MCPInteractionPayloads.parseJsonPayload(response.body())));
    }
    
    @Override
    public void close() throws IOException, InterruptedException {
        if (null == sessionId) {
            return;
        }
        HttpRequest request = HttpRequest.newBuilder(endpointUri)
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", actualProtocolVersion)
                .DELETE()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        sessionId = null;
        actualProtocolVersion = null;
    }
    
    private HttpRequest.Builder createJsonRequestBuilder() {
        return HttpRequest.newBuilder(endpointUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", actualProtocolVersion);
    }
    
    private HttpResponse<String> sendPostRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", requestId,
                        "method", method,
                        "params", params))))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (200 != response.statusCode()) {
            throw new IllegalStateException("MCP request failed with status " + response.statusCode() + ".");
        }
        return response;
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", CLIENT_NAME, "version", "1.0.0"));
        return result;
    }
    
    private void ensureOpened() {
        if (null == sessionId) {
            throw new IllegalStateException("MCP session is not initialized.");
        }
    }
}
