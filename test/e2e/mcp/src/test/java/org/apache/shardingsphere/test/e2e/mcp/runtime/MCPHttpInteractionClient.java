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

package org.apache.shardingsphere.test.e2e.mcp.runtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP MCP tool client.
 */
public final class MCPHttpInteractionClient implements MCPInteractionClient {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final String PROTOCOL_VERSION = "2025-11-25";
    
    private final URI endpointUri;
    
    private final HttpClient httpClient;
    
    private String sessionId;
    
    private String actualProtocolVersion;
    
    public MCPHttpInteractionClient(final URI endpointUri, final HttpClient httpClient) {
        this.endpointUri = endpointUri;
        this.httpClient = httpClient;
    }
    
    @Override
    public void open() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(endpointUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "llm-init-1",
                        "method", "initialize",
                        "params", createInitializeRequestParams()))))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (200 != response.statusCode()) {
            throw new IllegalStateException("Failed to initialize MCP session.");
        }
        sessionId = response.headers().firstValue("MCP-Session-Id")
                .orElseThrow(() -> new IllegalStateException("MCP initialize response does not contain MCP-Session-Id header."));
        actualProtocolVersion = response.headers().firstValue("MCP-Protocol-Version").orElse(PROTOCOL_VERSION);
    }
    
    @Override
    public MCPInteractionResponse call(final String actionName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        ensureOpened();
        HttpResponse<String> response = sendPostRequest(actionName + "-1", "tools/call", Map.of("name", actionName, "arguments", arguments));
        return new MCPInteractionResponse(getStructuredContent(response.body()), response.body());
    }
    
    @Override
    public MCPInteractionResponse listResources() throws IOException, InterruptedException {
        ensureOpened();
        HttpResponse<String> response = sendPostRequest("resources-list-1", "resources/list", Map.of());
        return new MCPInteractionResponse(getJsonRpcResult(response.body()), response.body());
    }
    
    @Override
    public MCPInteractionResponse readResource(final String resourceUri) throws IOException, InterruptedException {
        ensureOpened();
        HttpResponse<String> response = sendPostRequest("resources-read-1", "resources/read", Map.of("uri", resourceUri));
        return new MCPInteractionResponse(getFirstResourcePayload(response.body()), response.body());
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
        result.put("clientInfo", Map.of("name", "llm-e2e", "version", "1.0.0"));
        return result;
    }
    
    private void ensureOpened() {
        if (null == sessionId) {
            throw new IllegalStateException("MCP session is not initialized.");
        }
    }
    
    private Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> payload = parseJsonPayload(responseBody);
        Map<String, Object> result = payload.containsKey("result") ? castToMap(payload.get("result")) : Map.of();
        if (result.containsKey("structuredContent")) {
            return castToMap(result.get("structuredContent"));
        }
        if (payload.containsKey("error")) {
            return createJsonRpcErrorPayload(payload.get("error"));
        }
        return Map.of();
    }
    
    private Map<String, Object> getJsonRpcResult(final String responseBody) {
        Map<String, Object> payload = parseJsonPayload(responseBody);
        if (payload.containsKey("error")) {
            return createJsonRpcErrorPayload(payload.get("error"));
        }
        return payload.containsKey("result") ? castToMap(payload.get("result")) : Map.of();
    }
    
    private Map<String, Object> getFirstResourcePayload(final String responseBody) {
        Map<String, Object> payload = parseJsonPayload(responseBody);
        if (payload.containsKey("error")) {
            return createJsonRpcErrorPayload(payload.get("error"));
        }
        Map<String, Object> result = payload.containsKey("result") ? castToMap(payload.get("result")) : Map.of();
        Object contents = result.get("contents");
        List<Map<String, Object>> parsedContents = castToList(contents);
        if (null == parsedContents || parsedContents.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> firstContent = parsedContents.get(0);
        try {
            return OBJECT_MAPPER.readValue(String.valueOf(firstContent.get("text")), new TypeReference<>() {
            });
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to parse MCP resource payload.", ex);
        }
    }
    
    private String normalizeJsonBody(final String responseBody) {
        String result = responseBody.trim();
        if (result.startsWith("{") || result.startsWith("[")) {
            return result;
        }
        final StringBuilder stringBuilder = new StringBuilder();
        boolean hasDataLine = false;
        for (String each : result.split("\\R")) {
            String line = each.trim();
            if (!line.startsWith("data:")) {
                continue;
            }
            if (hasDataLine) {
                stringBuilder.append(System.lineSeparator());
            }
            stringBuilder.append(line.substring("data:".length()).trim());
            hasDataLine = true;
        }
        return hasDataLine ? stringBuilder.toString() : result;
    }
    
    private Map<String, Object> castToMap(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> parseJsonPayload(final String responseBody) {
        try {
            return OBJECT_MAPPER.readValue(normalizeJsonBody(responseBody), new TypeReference<>() {
            });
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to parse MCP response body.", ex);
        }
    }
    
    private Map<String, Object> createJsonRpcErrorPayload(final Object rawError) {
        Map<String, Object> error = castToMap(rawError);
        return Map.of(
                "error_code", "json_rpc_error",
                "message", String.valueOf(error.getOrDefault("message", "Unknown JSON-RPC error.")));
    }
    
    private List<Map<String, Object>> castToList(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
}
