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

package org.apache.shardingsphere.test.e2e.mcp.llm.chat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLMChatModelClientTest {
    
    @TempDir
    private Path tempDir;
    
    private HttpServer httpServer;
    
    @AfterEach
    void tearDown() {
        if (null != httpServer) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
    
    @Test
    void assertWaitUntilReady() throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/models", exchange -> writeResponse(exchange, 200, "{\"data\":[{\"id\":\"qwen3:1.7b\"}]}"));
        httpServer.start();
        
        createClient().waitUntilReady();
    }
    
    @Test
    void assertWaitUntilReadyWithMissingModel() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/models", exchange -> writeResponse(exchange, 200, "{\"data\":[{\"id\":\"other-model\"}]}"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> createClient(1).waitUntilReady());
        
        assertThat(actual.getMessage(), is("Model service is not ready for `qwen3:1.7b`."));
    }
    
    @Test
    void assertWaitUntilReadyAfterRetry() throws IOException, InterruptedException {
        AtomicInteger requestCount = new AtomicInteger();
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/models", exchange -> {
            if (1 == requestCount.incrementAndGet()) {
                writeResponse(exchange, 503, "{\"error\":\"warming\"}");
                return;
            }
            writeResponse(exchange, 200, "{\"data\":[{\"id\":\"qwen3:1.7b\"}]}");
        });
        httpServer.start();
        
        createClient(3).waitUntilReady();
        assertThat(requestCount.get(), is(2));
    }
    
    @Test
    void assertWaitUntilReadyWithRequestFailure() throws IOException {
        int closedPort;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            closedPort = serverSocket.getLocalPort();
        }
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> createClient("http://127.0.0.1:" + closedPort + "/v1", 1).waitUntilReady());
        assertThat(actual.getMessage(), is("Model service is not ready for `qwen3:1.7b`."));
        assertThat(actual.getCause(), isA(IOException.class));
    }
    
    @Test
    void assertWaitUntilReadyWithMalformedResponseBody() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/models", exchange -> writeResponse(exchange, 200, "not-json"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> createClient(1).waitUntilReady());
        
        assertThat(actual.getMessage(), is("Model service is not ready for `qwen3:1.7b`."));
        assertThat(actual.getCause(), isA(IllegalStateException.class));
        assertThat(actual.getCause().getMessage(), is("Failed to parse model-list response."));
    }
    
    @Test
    void assertComplete() throws IOException, InterruptedException {
        AtomicReference<String> requestBody = new AtomicReference<>("");
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            writeResponse(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"\",\"tool_calls\":[{\"id\":\"call-1\",\"function\":{\"name\":\"list_tables\","
                    + "\"arguments\":\"{\\\"database\\\":\\\"logic_db\\\",\\\"schema\\\":\\\"public\\\"}\"}}]}}]}");
        });
        httpServer.start();
        
        LLMChatCompletion actual = createClient().complete(List.of(LLMChatMessage.user("Run the smoke.")),
                List.of(Map.of("type", "function", "function", Map.of("name", "list_tables", "parameters", Map.of("type", "object")))), "auto", true);
        
        assertThat(actual.getToolCalls().size(), is(1));
        assertThat(actual.getToolCalls().get(0).getName(), is("list_tables"));
        assertThat(actual.getToolCalls().get(0).getArgumentsJson(), containsString("\"logic_db\""));
        assertThat(requestBody.get(), containsString("\"response_format\""));
        assertThat(requestBody.get(), containsString("\"tools\""));
    }
    
    @Test
    void assertCompleteWithoutToolCalls() throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> writeResponse(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"{\\\"database\\\":\\\"logic_db\\\"}\"}}]}"));
        httpServer.start();
        
        LLMChatCompletion actual = createClient().complete(List.of(LLMChatMessage.user("Return JSON.")), List.of(), "none", true);
        
        assertThat(actual.getContent(), is("{\"database\":\"logic_db\"}"));
        assertThat(actual.getToolCalls(), is(List.of()));
    }
    
    @Test
    void assertCompleteWithConversationMessages() throws IOException, InterruptedException {
        AtomicReference<String> requestBody = new AtomicReference<>("");
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            writeResponse(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"database\\\":\\\"logic_db\\\"}\"}}]}");
        });
        httpServer.start();
        
        LLMChatCompletion actual = createClient().complete(List.of(
                LLMChatMessage.user("Locate orders."),
                LLMChatMessage.assistant("", List.of(new LLMToolCall("call-1", "search_metadata",
                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"))),
                LLMChatMessage.tool("call-1", "{\"items\":[{\"name\":\"orders\"}]}")), List.of(), "auto", false);
        
        assertThat(actual.getContent(), is("{\"database\":\"logic_db\"}"));
        assertThat(requestBody.get(), containsString("\"role\":\"assistant\""));
        assertThat(requestBody.get(), containsString("\"tool_calls\":[{"));
        assertThat(requestBody.get(), containsString("\"id\":\"call-1\""));
        assertThat(requestBody.get(), containsString("\"name\":\"search_metadata\""));
        assertThat(requestBody.get(), containsString("\"role\":\"tool\""));
        assertThat(requestBody.get(), containsString("\"tool_call_id\":\"call-1\""));
    }
    
    @Test
    void assertCompleteWithFailureStatus() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> writeResponse(exchange, 500, "{\"error\":\"boom\"}"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> createClient().complete(List.of(LLMChatMessage.user("Run the smoke.")), List.of(), "none", true));
        
        assertThat(actual.getMessage(), is("Model completion request failed with status 500."));
    }
    
    @Test
    void assertCompleteWithMissingChoices() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> writeResponse(exchange, 200, "{\"choices\":[]}"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> createClient().complete(List.of(LLMChatMessage.user("Run the smoke.")), List.of(), "none", true));
        
        assertThat(actual.getMessage(), is("Model completion response does not contain choices."));
    }
    
    @Test
    void assertCompleteWithMalformedResponseBody() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> writeResponse(exchange, 200, "not-json"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> createClient().complete(List.of(LLMChatMessage.user("Run the smoke.")), List.of(), "none", true));
        
        assertThat(actual.getMessage(), is("Failed to parse model completion response."));
    }
    
    @Test
    void assertCompleteWithMissingMessage() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/v1/chat/completions", exchange -> writeResponse(exchange, 200, "{\"choices\":[{}]}"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> createClient().complete(List.of(LLMChatMessage.user("Run the smoke.")), List.of(), "none", true));
        
        assertThat(actual.getMessage(), is("Model completion response does not contain one assistant message."));
    }
    
    private LLMChatModelClient createClient() {
        return createClient(2);
    }
    
    private LLMChatModelClient createClient(final int readyTimeoutSeconds) {
        return createClient("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/v1", readyTimeoutSeconds);
    }
    
    private LLMChatModelClient createClient(final String baseUrl, final int readyTimeoutSeconds) {
        LLME2EConfiguration configuration = new LLME2EConfiguration(true, baseUrl, "qwen3:1.7b", "ollama",
                readyTimeoutSeconds, 2, 6, tempDir, "run-a");
        return new LLMChatModelClient(configuration, HttpClient.newHttpClient());
    }
    
    private void writeResponse(final HttpExchange exchange, final int statusCode, final String responseBody) throws IOException {
        byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
