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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class MCPHttpInteractionClientTest {
    
    private HttpServer httpServer;
    
    @AfterEach
    void tearDown() {
        if (null != httpServer) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
    
    @Test
    void assertOpenCallAndClose() throws IOException, InterruptedException {
        final AtomicReference<String> toolSessionId = new AtomicReference<>("");
        final AtomicReference<String> resourceSessionId = new AtomicReference<>("");
        final AtomicReference<String> deleteSessionId = new AtomicReference<>("");
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", exchange -> handleGatewayRequest(exchange, toolSessionId, resourceSessionId, deleteSessionId));
        httpServer.start();
        
        final URI endpointUri = URI.create("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/gateway");
        final MCPHttpInteractionClient actual = new MCPHttpInteractionClient(endpointUri, HttpClient.newHttpClient());
        actual.open();
        final MCPInteractionResponse response = actual.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1"));
        final MCPInteractionResponse listResponse = actual.listResources();
        final MCPInteractionResponse resourceResponse = actual.readResource("shardingsphere://capabilities");
        actual.close();
        
        assertThat(String.valueOf(response.structuredContent().get("result_kind")), is("result_set"));
        assertThat(String.valueOf(listResponse.structuredContent().get("resources")), containsString("shardingsphere://capabilities"));
        assertThat(String.valueOf(resourceResponse.structuredContent().get("supportedTools")), containsString("execute_query"));
        assertThat(response.rawResponse(), containsString("structuredContent"));
        assertThat(toolSessionId.get(), is("session-1"));
        assertThat(resourceSessionId.get(), is("session-1"));
        assertThat(deleteSessionId.get(), is("session-1"));
    }
    
    private void handleGatewayRequest(final HttpExchange exchange, final AtomicReference<String> toolSessionId, final AtomicReference<String> resourceSessionId,
                                      final AtomicReference<String> deleteSessionId) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "POST":
                handlePost(exchange, toolSessionId, resourceSessionId);
                break;
            case "DELETE":
                deleteSessionId.set(exchange.getRequestHeaders().getFirst("MCP-Session-Id"));
                writeResponse(exchange, 200, "");
                break;
            default:
                writeResponse(exchange, 405, "");
                break;
        }
    }
    
    private void handlePost(final HttpExchange exchange, final AtomicReference<String> toolSessionId,
                            final AtomicReference<String> resourceSessionId) throws IOException {
        final String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        if (body.contains("\"method\":\"resources/list\"")) {
            resourceSessionId.set(exchange.getRequestHeaders().getFirst("MCP-Session-Id"));
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"resources-list-1\",\"result\":{\"resources\":[{\"uri\":\"shardingsphere://capabilities\"}]}}");
            return;
        }
        if (body.contains("\"method\":\"resources/read\"")) {
            resourceSessionId.set(exchange.getRequestHeaders().getFirst("MCP-Session-Id"));
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"resources-read-1\",\"result\":{\"contents\":[{\"uri\":\"shardingsphere://capabilities\","
                    + "\"mimeType\":\"application/json\",\"text\":\"{\\\"supportedTools\\\":[\\\"execute_query\\\"]}\"}]}}");
            return;
        }
        toolSessionId.set(exchange.getRequestHeaders().getFirst("MCP-Session-Id"));
        writeResponse(exchange, 200, "data: {\"jsonrpc\":\"2.0\",\"id\":\"execute_query-1\",\"result\":{\"structuredContent\":"
                + "{\"result_kind\":\"result_set\",\"rows\":[[1]]}}}");
    }
    
    private void writeResponse(final HttpExchange exchange, final int statusCode, final String responseBody) throws IOException {
        final byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
