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

package org.apache.shardingsphere.test.e2e.mcp.runtime.transport;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.client.MCPHttpInteractionClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        
        assertThat(String.valueOf(response.getStructuredContent().get("result_kind")), is("result_set"));
        assertThat(String.valueOf(listResponse.getStructuredContent().get("resources")), containsString("shardingsphere://capabilities"));
        assertThat(String.valueOf(resourceResponse.getStructuredContent().get("supportedTools")), containsString("execute_query"));
        assertThat(response.getRawResponse(), containsString("structuredContent"));
        assertThat(toolSessionId.get(), is("session-1"));
        assertThat(resourceSessionId.get(), is("session-1"));
        assertThat(deleteSessionId.get(), is("session-1"));
    }
    
    @Test
    void assertOpenWithFailureStatus() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", exchange -> writeResponse(exchange, 500, "{\"jsonrpc\":\"2.0\",\"error\":{\"message\":\"boom\"}}"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> createClient().open());
        
        assertThat(actual.getMessage(), is("Failed to initialize MCP session."));
    }
    
    @Test
    void assertOpenWithMissingSessionIdHeader() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", exchange -> writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}"));
        httpServer.start();
        
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> createClient().open());
        
        assertThat(actual.getMessage(), is("MCP initialize response does not contain MCP-Session-Id header."));
    }
    
    @Test
    void assertOpenWithoutProtocolVersionFallsBackToDefaultProtocolHeader() throws IOException, InterruptedException {
        AtomicReference<String> actualProtocolVersion = new AtomicReference<>("");
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", exchange -> handleGatewayRequestWithoutProtocolHeader(exchange, actualProtocolVersion));
        httpServer.start();
        
        MCPHttpInteractionClient actual = createClient();
        actual.open();
        actual.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1"));
        actual.close();
        
        assertThat(actualProtocolVersion.get(), is("2025-11-25"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRejectOperationWithoutOpenCases")
    void assertRejectOperationWithoutOpen(final String name, final String operation) {
        MCPHttpInteractionClient actual = new MCPHttpInteractionClient(URI.create("http://127.0.0.1:65535/gateway"), HttpClient.newHttpClient());
        IllegalStateException ex;
        if ("call".equals(operation)) {
            ex = assertThrows(IllegalStateException.class, () -> actual.call("execute_query", Map.of("sql", "SELECT 1")));
        } else if ("listResources".equals(operation)) {
            ex = assertThrows(IllegalStateException.class, actual::listResources);
        } else {
            ex = assertThrows(IllegalStateException.class, () -> actual.readResource("shardingsphere://capabilities"));
        }
        assertThat(ex.getMessage(), is("MCP session is not initialized."));
    }
    
    static Stream<Arguments> assertRejectOperationWithoutOpenCases() {
        return Stream.of(
                Arguments.of("call without open", "call"),
                Arguments.of("list resources without open", "listResources"),
                Arguments.of("read resource without open", "readResource"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRequestFailureCases")
    void assertRequestFailure(final String name, final String operation) throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", this::handleFailureGatewayRequest);
        httpServer.start();
        URI endpointUri = URI.create("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/gateway");
        MCPHttpInteractionClient actual = new MCPHttpInteractionClient(endpointUri, HttpClient.newHttpClient());
        actual.open();
        IllegalStateException ex;
        if ("call".equals(operation)) {
            ex = assertThrows(IllegalStateException.class, () -> actual.call("execute_query", Map.of("sql", "SELECT 1")));
        } else if ("listResources".equals(operation)) {
            ex = assertThrows(IllegalStateException.class, actual::listResources);
        } else {
            ex = assertThrows(IllegalStateException.class, () -> actual.readResource("shardingsphere://capabilities"));
        }
        assertThat(ex.getMessage(), is("MCP request failed with status 500."));
    }
    
    static Stream<Arguments> assertRequestFailureCases() {
        return Stream.of(
                Arguments.of("call with failure response", "call"),
                Arguments.of("list resources with failure response", "listResources"),
                Arguments.of("read resource with failure response", "readResource"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertJsonRpcErrorCases")
    void assertJsonRpcError(final String name, final String operation) throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", this::handleJsonRpcErrorGatewayRequest);
        httpServer.start();
        MCPHttpInteractionClient actual = createClient();
        actual.open();
        MCPInteractionResponse response = executeOperation(actual, operation);
        assertThat(String.valueOf(response.getStructuredContent().get("error_code")), is("json_rpc_error"));
        assertThat(String.valueOf(response.getStructuredContent().get("message")), is("Unsupported resource."));
    }
    
    static Stream<Arguments> assertJsonRpcErrorCases() {
        return Stream.of(
                Arguments.of("call with json rpc error", "call"),
                Arguments.of("list resources with json rpc error", "listResources"),
                Arguments.of("read resource with json rpc error", "readResource"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadResourceBoundaryCases")
    void assertReadResourceBoundary(final String name, final String handlerName, final String expectedErrorCode,
                                    final String expectedMessage, final boolean expectedEmptyPayload,
                                    final String expectedExceptionMessage) throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", exchange -> handleReadResourceGatewayRequest(exchange, handlerName));
        httpServer.start();
        MCPHttpInteractionClient actual = createClient();
        actual.open();
        if (!expectedExceptionMessage.isEmpty()) {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> actual.readResource("shardingsphere://capabilities"));
            assertThat(ex.getMessage(), is(expectedExceptionMessage));
            return;
        }
        MCPInteractionResponse response = actual.readResource("shardingsphere://capabilities");
        if (expectedEmptyPayload) {
            assertThat(response.getStructuredContent(), is(Map.of()));
            return;
        }
        assertThat(String.valueOf(response.getStructuredContent().get("error_code")), is(expectedErrorCode));
        assertThat(String.valueOf(response.getStructuredContent().get("message")), is(expectedMessage));
    }
    
    static Stream<Arguments> assertReadResourceBoundaryCases() {
        return Stream.of(
                Arguments.of("empty contents", "emptyContents", "", "", true, ""),
                Arguments.of("missing contents", "missingContents", "", "", true, ""),
                Arguments.of("malformed resource payload", "malformedPayload", "", "", false, "Failed to parse MCP resource payload."));
    }
    
    @Test
    void assertCallWithMalformedResponseBody() throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/gateway", this::handleMalformedResponseGatewayRequest);
        httpServer.start();
        MCPHttpInteractionClient actual = createClient();
        actual.open();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> actual.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1")));
        assertThat(ex.getMessage(), is("Failed to parse MCP response body."));
    }
    
    private MCPHttpInteractionClient createClient() {
        return new MCPHttpInteractionClient(URI.create("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/gateway"), HttpClient.newHttpClient());
    }
    
    private MCPInteractionResponse executeOperation(final MCPHttpInteractionClient actual, final String operation) throws IOException, InterruptedException {
        if ("call".equals(operation)) {
            return actual.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1"));
        }
        if ("listResources".equals(operation)) {
            return actual.listResources();
        }
        return actual.readResource("shardingsphere://capabilities");
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
    
    private void handleGatewayRequestWithoutProtocolHeader(final HttpExchange exchange, final AtomicReference<String> actualProtocolVersion) throws IOException {
        if ("DELETE".equals(exchange.getRequestMethod())) {
            writeResponse(exchange, 200, "");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        actualProtocolVersion.set(exchange.getRequestHeaders().getFirst("MCP-Protocol-Version"));
        writeResponse(exchange, 200, "data: {\"jsonrpc\":\"2.0\",\"id\":\"execute_query-1\",\"result\":{\"structuredContent\":{\"result_kind\":\"result_set\"}}}");
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
    
    private void handleFailureGatewayRequest(final HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        writeResponse(exchange, 500, "{\"jsonrpc\":\"2.0\",\"error\":{\"message\":\"boom\"}}");
    }
    
    private void handleJsonRpcErrorGatewayRequest(final HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"resources-read-1\",\"error\":{\"message\":\"Unsupported resource.\"}}");
    }
    
    private void handleEmptyContentsGatewayRequest(final HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"resources-read-1\",\"result\":{\"contents\":[]}}");
    }
    
    private void handleReadResourceGatewayRequest(final HttpExchange exchange, final String handlerName) throws IOException {
        if ("jsonRpcError".equals(handlerName)) {
            handleJsonRpcErrorGatewayRequest(exchange);
            return;
        }
        if ("emptyContents".equals(handlerName)) {
            handleEmptyContentsGatewayRequest(exchange);
            return;
        }
        if ("missingContents".equals(handlerName)) {
            handleMissingContentsGatewayRequest(exchange);
            return;
        }
        handleMalformedResourcePayloadGatewayRequest(exchange);
    }
    
    private void handleMissingContentsGatewayRequest(final HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"resources-read-1\",\"result\":{}}");
    }
    
    private void handleMalformedResponseGatewayRequest(final HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        writeResponse(exchange, 200, "data: not-json");
    }
    
    private void handleMalformedResourcePayloadGatewayRequest(final HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("\"method\":\"initialize\"")) {
            exchange.getResponseHeaders().add("MCP-Session-Id", "session-1");
            exchange.getResponseHeaders().add("MCP-Protocol-Version", "2025-11-25");
            writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"llm-init-1\",\"result\":{\"serverInfo\":{\"name\":\"mcp\"}}}");
            return;
        }
        writeResponse(exchange, 200, "{\"jsonrpc\":\"2.0\",\"id\":\"resources-read-1\",\"result\":{\"contents\":[{\"text\":\"not-json\"}]}}");
    }
    
    private void writeResponse(final HttpExchange exchange, final int statusCode, final String responseBody) throws IOException {
        final byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
