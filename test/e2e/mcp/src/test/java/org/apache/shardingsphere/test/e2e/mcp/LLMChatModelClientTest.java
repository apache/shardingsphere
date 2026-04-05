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

package org.apache.shardingsphere.test.e2e.mcp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

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
        
        assertThat(actual.toolCalls().size(), is(1));
        assertThat(actual.toolCalls().get(0).name(), is("list_tables"));
        assertThat(actual.toolCalls().get(0).argumentsJson(), containsString("\"logic_db\""));
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
        
        assertThat(actual.content(), is("{\"database\":\"logic_db\"}"));
        assertThat(actual.toolCalls(), is(List.of()));
    }
    
    private LLMChatModelClient createClient() {
        int port = httpServer.getAddress().getPort();
        LLME2EConfiguration configuration = new LLME2EConfiguration(true, "http://127.0.0.1:" + port + "/v1", "qwen3:1.7b", "ollama", 2, 2, 6, tempDir, "run-a");
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
