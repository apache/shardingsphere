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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.runtime.AbstractLaunchedRuntimeE2ETest;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractProductionRuntimeE2ETest extends AbstractLaunchedRuntimeE2ETest {
    
    private static final String PROTOCOL_VERSION = "2025-11-25";
    
    protected final void launchProductionRuntime() throws IOException {
        launchRuntime();
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of());
        
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", toolName + "-1",
                        "method", "tools/call",
                        "params", Map.of("name", toolName, "arguments", arguments)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final String sessionId,
                                                                 final String resourceUri) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "resource-1",
                        "method", "resources/read",
                        "params", Map.of("uri", resourceUri)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(getEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> result = getJsonRpcResult(responseBody);
        if (result.containsKey("structuredContent")) {
            return castToMap(result.get("structuredContent"));
        }
        List<Map<String, Object>> content = castToList(result.get("content"));
        if (null != content && !content.isEmpty()) {
            return parseJsonBody(String.valueOf(content.get(0).get("text")));
        }
        return getJsonRpcError(responseBody);
    }
    
    protected final Map<String, Object> getResourcePayload(final String responseBody) {
        List<Map<String, Object>> contents = castToList(getJsonRpcResult(responseBody).get("contents"));
        if (null == contents || contents.isEmpty()) {
            return getJsonRpcError(responseBody);
        }
        return parseJsonBody(String.valueOf(contents.get(0).get("text")));
    }
    
    protected final List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return castToList(payload.get("items"));
    }
    
    private HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> requestHeaders) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(getEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", createInitializeRequestParams()))));
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpRequest.Builder createJsonRequestBuilder(final String sessionId) {
        return HttpRequest.newBuilder(getEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION);
    }
    
    private Map<String, Object> parseJsonBody(final String responseBody) {
        return JsonUtils.fromJsonString(normalizeJsonBody(responseBody), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> getJsonRpcResult(final String responseBody) {
        Map<String, Object> payload = parseJsonBody(responseBody);
        return payload.containsKey("result") ? castToMap(payload.get("result")) : Map.of();
    }
    
    private Map<String, Object> getJsonRpcError(final String responseBody) {
        Map<String, Object> payload = parseJsonBody(responseBody);
        if (!payload.containsKey("error")) {
            return Map.of();
        }
        Map<String, Object> error = castToMap(payload.get("error"));
        return Map.of(
                "error_code", "json_rpc_error",
                "message", String.valueOf(error.getOrDefault("message", "Unknown JSON-RPC error.")));
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", "production-runtime-e2e", "version", "1.0.0"));
        return result;
    }
    
    private Map<String, Object> castToMap(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private List<Map<String, Object>> castToList(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private String normalizeJsonBody(final String responseBody) {
        String trimmedResponseBody = responseBody.trim();
        if (trimmedResponseBody.startsWith("{") || trimmedResponseBody.startsWith("[")) {
            return trimmedResponseBody;
        }
        StringBuilder result = new StringBuilder();
        boolean hasDataLine = false;
        for (String each : trimmedResponseBody.split("\\R")) {
            String currentLine = each.trim();
            if (!currentLine.startsWith("data:")) {
                continue;
            }
            if (hasDataLine) {
                result.append(System.lineSeparator());
            }
            result.append(currentLine.substring("data:".length()).trim());
            hasDataLine = true;
        }
        return hasDataLine ? result.toString() : trimmedResponseBody;
    }
}
