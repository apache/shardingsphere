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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LLMChatModelClientTest {
    
    private static final String REQUIRED_MODEL = "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M";
    
    private static final LLME2EConfiguration.ModelMetadata MODEL_METADATA = new LLME2EConfiguration.ModelMetadata(
            "ggml-org/Qwen3-1.7B-GGUF", "Qwen3-1.7B-Q4_K_M.gguf", "Q4_K_M", "daeb8e2d528a760970442092f6bf1e55c3b659eb", 1282439264L, "configured-model-sha256");
    
    @Test
    void assertWaitUntilReady() throws IOException, InterruptedException {
        List<String> actualBodies = new LinkedList<>();
        HttpServer server = startModelServer(REQUIRED_MODEL, actualBodies);
        try {
            new LLMChatModelClient(createConfiguration(createBaseUrl(server), 1), HttpClient.newHttpClient()).waitUntilReady();
            assertThat(actualBodies.size(), is(4));
            assertCoreCompletionPayload(readPayload(actualBodies.get(0)));
            assertRequiredToolPayload(readPayload(actualBodies.get(1)));
            assertAutoToolPayload(readPayload(actualBodies.get(2)));
            assertFinalAnswerPayload(readPayload(actualBodies.get(3)));
        } finally {
            server.stop(0);
        }
    }
    
    @Test
    void assertWaitUntilReadyReportsProbeFailure() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> modelListResponse = createResponse(200, String.format("{\"data\":[{\"id\":\"%s\"}]}", REQUIRED_MODEL));
        HttpResponse<String> completionResponse = createResponse(401, "{\"error\":{\"code\":\"unauthorized\"}}");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(modelListResponse, completionResponse);
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                () -> new LLMChatModelClient(createConfiguration("http://127.0.0.1:8080/v1", 1), httpClient).waitUntilReady());
        assertTrue(actualException.getMessage().startsWith(
                String.format("Model service is not ready for `%s` after 1 readiness attempt(s), elapsedMillis=", REQUIRED_MODEL)));
        assertTrue(actualException.getMessage().endsWith(
                "timeoutSeconds=1. Last readiness failure: completion readiness request returned HTTP 401 with error code `unauthorized`."));
    }
    
    @Test
    void assertComplete() throws IOException, InterruptedException {
        List<String> actualBodies = new LinkedList<>();
        HttpServer server = startCompletionServer(actualBodies, createCompletionResponse("done", true));
        try {
            LLMChatCompletion actual = new LLMChatModelClient(createConfiguration(createBaseUrl(server), 1), HttpClient.newHttpClient()).complete(
                    List.of(
                            LLMChatMessage.system("system"),
                            LLMChatMessage.user("user"),
                            LLMChatMessage.assistant("", List.of(new LLMToolCall("call_0", "mcp_read_resource", "{\"uri\":\"mcp://test-resource\"}"))),
                            LLMChatMessage.tool("call_0", "tool result")),
                    createToolDefinitions(), "required", true);
            assertThat(actual.getContent(), is("done"));
            assertThat(actual.getToolCalls().size(), is(1));
            assertThat(actual.getToolCalls().get(0).getName(), is("mcp_read_resource"));
            assertCompletePayload(readPayload(actualBodies.get(0)));
        } finally {
            server.stop(0);
        }
    }
    
    @Test
    void assertCompleteWithHttpFailure() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = createResponse(500, "{\"error\":{\"code\":\"server_error\"}}");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                () -> new LLMChatModelClient(createConfiguration("http://127.0.0.1:8080/v1", 1), httpClient).complete(
                        List.of(LLMChatMessage.user("user")), List.of(), "", false));
        assertThat(actualException.getMessage(), is("Model completion request failed with status 500 with error code `server_error`."));
    }
    
    private void assertCoreCompletionPayload(final Map<String, Object> actual) {
        assertCoreCompletionPayload(actual, 64);
    }
    
    private void assertCoreCompletionPayload(final Map<String, Object> actual, final int maxTokens) {
        assertThat(actual.get("model"), is(REQUIRED_MODEL));
        assertFalse((boolean) actual.get("stream"));
        assertThat(actual.get("temperature"), is(0));
        assertThat(actual.get("seed"), is(1));
        assertThat(actual.get("reasoning_effort"), is("none"));
        assertThat(actual.get("max_tokens"), is(maxTokens));
    }
    
    private void assertRequiredToolPayload(final Map<String, Object> actual) {
        assertCoreCompletionPayload(actual);
        assertThat(actual.get("tool_choice"), is("required"));
        assertThat(castToList(actual.get("tools")).size(), is(1));
    }
    
    private void assertAutoToolPayload(final Map<String, Object> actual) {
        assertCoreCompletionPayload(actual);
        assertThat(actual.get("tool_choice"), is("auto"));
        assertThat(castToList(actual.get("tools")).size(), is(1));
    }
    
    private void assertFinalAnswerPayload(final Map<String, Object> actual) {
        assertCoreCompletionPayload(actual);
        assertThat(actual.get("tool_choice"), is("none"));
        assertThat(castToMap(actual.get("response_format")).get("type"), is("json_object"));
    }
    
    private void assertCompletePayload(final Map<String, Object> actual) {
        assertCoreCompletionPayload(actual, 512);
        assertThat(actual.get("tool_choice"), is("required"));
        assertThat(castToList(actual.get("tools")).size(), is(1));
        assertThat(castToMap(actual.get("response_format")).get("type"), is("json_object"));
        List<Map<String, Object>> messages = castToList(actual.get("messages"));
        assertThat(messages.size(), is(4));
        assertThat(castToList(messages.get(2).get("tool_calls")).size(), is(1));
        assertThat(messages.get(3).get("tool_call_id"), is("call_0"));
    }
    
    private LLME2EConfiguration createConfiguration(final String baseUrl, final int readyTimeoutSeconds) {
        return new LLME2EConfiguration(baseUrl, "openai-compatible", REQUIRED_MODEL, "mcp-llm-score", readyTimeoutSeconds, 30, 10,
                Path.of("target/llm-e2e"), "run-id", RuntimeMode.DOCKER, "llama.cpp", "apache/shardingsphere-mcp-llm-runtime:local", "ghcr.io/ggml-org/llama.cpp:server",
                "test-base-server-image-digest", MODEL_METADATA);
    }
    
    private HttpServer startModelServer(final String modelName, final List<String> requestBodies) throws IOException {
        HttpServer result = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        result.createContext("/v1/models", exchange -> writeResponse(exchange, String.format("{\"data\":[{\"id\":\"%s\"}]}", modelName)));
        result.createContext("/v1/chat/completions", exchange -> {
            String requestBody = readRequestBody(exchange);
            requestBodies.add(requestBody);
            writeResponse(exchange, createReadinessResponse(requestBody));
        });
        result.start();
        return result;
    }
    
    private HttpServer startCompletionServer(final List<String> requestBodies, final String responseBody) throws IOException {
        HttpServer result = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        result.createContext("/v1/chat/completions", exchange -> {
            requestBodies.add(readRequestBody(exchange));
            writeResponse(exchange, responseBody);
        });
        result.start();
        return result;
    }
    
    private String createReadinessResponse(final String requestBody) {
        if (requestBody.contains("\"tool_choice\":\"required\"")) {
            return createCompletionResponse("", true);
        }
        return requestBody.contains("\"response_format\"")
                ? "{\"choices\":[{\"message\":{\"content\":\"{\\\"status\\\":\\\"ok\\\"}\"}}]}"
                : createCompletionResponse("ok", false);
    }
    
    private String createCompletionResponse(final String content, final boolean toolCall) {
        String toolCalls = toolCall
                ? ",\"tool_calls\":[{\"id\":\"call_1\",\"type\":\"function\",\"function\":{\"name\":\"mcp_read_resource\",\"arguments\":\"{\\\"uri\\\":\\\"mcp://readiness\\\"}\"}}]"
                : "";
        return String.format("{\"choices\":[{\"message\":{\"content\":\"%s\"%s}}]}", content, toolCalls);
    }
    
    private List<Map<String, Object>> createToolDefinitions() {
        return List.of(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "mcp_read_resource",
                        "description", "Read one MCP resource.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of("uri", Map.of("type", "string")),
                                "required", List.of("uri")))));
    }
    
    private String readRequestBody(final HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    private void writeResponse(final HttpExchange exchange, final String responseBody) throws IOException {
        byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }
    
    private String createBaseUrl(final HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }
    
    private Map<String, Object> readPayload(final String requestBody) {
        return JsonUtils.fromJsonString(requestBody, new TypeReference<>() {
        });
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castToList(final Object value) {
        return (List<Map<String, Object>>) value;
    }
    
    @SuppressWarnings("unchecked")
    private HttpResponse<String> createResponse(final int statusCode, final String body) {
        HttpResponse<String> result = mock(HttpResponse.class);
        when(result.statusCode()).thenReturn(statusCode);
        when(result.body()).thenReturn(body);
        return result;
    }
}
