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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.jdbc.config.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamableHttpRuntimeIntegrationTest extends AbstractProductionRuntimeIntegrationTest {
    
    @Test
    void assertLaunchHttpServerWithConfiguredEndpoint() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(createInitializeRequestBody()))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(200));
        assertTrue(initializeResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertThat(initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(""), is(MCPTransportConstants.PROTOCOL_VERSION));
        Map<String, Object> initializePayload = parseJsonBody(initializeResponse.body());
        assertThat(initializePayload.get("jsonrpc"), is("2.0"));
        Map<String, Object> result = castToMap(initializePayload.get("result"));
        assertThat(result.get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
        String sessionId = initializeResponse.headers().firstValue("MCP-Session-Id").orElse("");
        assertFalse(sessionId.isEmpty());
        HttpRequest toolCallRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "tool-1",
                        "method", "tools/call",
                        "params", Map.of("name", "get_capabilities", "arguments", Map.of())))))
                .build();
        HttpResponse<String> toolCallResponse = httpClient.send(toolCallRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(toolCallResponse.statusCode(), is(200));
        assertTrue(toolCallResponse.body().contains("supportedTools"));
        assertTrue(toolCallResponse.body().contains("execute_query"));
        HttpRequest resourceReadRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "resource-1",
                        "method", "resources/read",
                        "params", Map.of("uri", "shardingsphere://capabilities")))))
                .build();
        HttpResponse<String> resourceReadResponse = httpClient.send(resourceReadRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(resourceReadResponse.statusCode(), is(200));
        assertTrue(resourceReadResponse.body().contains("supportedResources"));
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(200));
    }
    
    @Test
    void assertAcceptFollowUpRequestWithLowercaseHeaders() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("mcp-session-id", sessionId)
                .header("mcp-protocol-version", MCPTransportConstants.PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "tool-1",
                        "method", "tools/call",
                        "params", Map.of("name", "get_capabilities", "arguments", Map.of())))))
                .build();
        HttpResponse<String> actualResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actualResponse.statusCode(), is(200));
        assertTrue(actualResponse.body().contains("supportedTools"));
    }
    
    @Test
    void assertLaunchHttpServerWithoutInitializeProtocolVersion() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", Map.of(
                                "capabilities", Map.of(),
                                "clientInfo", Map.of("name", "integration-test", "version", "1.0.0"))))))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(200));
        assertThat(initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(""), is(MCPTransportConstants.PROTOCOL_VERSION));
        Map<String, Object> initializePayload = parseJsonBody(initializeResponse.body());
        Map<String, Object> result = castToMap(initializePayload.get("result"));
        assertThat(result.get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
        assertFalse(initializeResponse.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertLaunchHttpServerWithoutAcceptHeader() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(createInitializeRequestBody()))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(200));
        assertThat(initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(""), is(MCPTransportConstants.PROTOCOL_VERSION));
        assertFalse(initializeResponse.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertAcceptFollowUpRequestWithoutProtocolHeader() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "tool-1",
                        "method", "tools/call",
                        "params", Map.of("name", "get_capabilities", "arguments", Map.of())))))
                .build();
        HttpResponse<String> actualResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actualResponse.statusCode(), is(200));
        assertTrue(actualResponse.body().contains("supportedTools"));
    }
    
    @Test
    void assertAcceptFollowUpRequestWithoutAcceptHeader() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "tool-1",
                        "method", "tools/call",
                        "params", Map.of("name", "get_capabilities", "arguments", Map.of())))))
                .build();
        HttpResponse<String> actualResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actualResponse.statusCode(), is(200));
        assertTrue(actualResponse.body().contains("supportedTools"));
    }
    
    @Test
    void assertAcceptFollowUpRequestWithBlankAcceptHeader() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", " ")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "tool-1",
                        "method", "tools/call",
                        "params", Map.of("name", "get_capabilities", "arguments", Map.of())))))
                .build();
        HttpResponse<String> actualResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actualResponse.statusCode(), is(200));
        assertTrue(actualResponse.body().contains("supportedTools"));
    }
    
    @Test
    void assertRejectDeleteWithoutSessionHeader() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(400));
        assertTrue(deleteResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertTrue(deleteResponse.body().contains("Session ID required in mcp-session-id header"));
    }
    
    @Test
    void assertRejectDeleteWithMissingSession() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", "missing-session")
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(404));
        assertTrue(deleteResponse.body().contains("Session does not exist."));
    }
    
    @Test
    void assertRejectDeleteWithProtocolVersionMismatch() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", "1900-01-01")
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(400));
        assertTrue(deleteResponse.body().contains("Protocol version mismatch."));
    }
    
    @Test
    void assertRejectOpenStreamAfterDelete() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .DELETE()
                .build();
        httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest streamRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Accept", "text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .GET()
                .build();
        HttpResponse<String> streamResponse = httpClient.send(streamRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(streamResponse.statusCode(), is(404));
        assertTrue(streamResponse.body().contains("Session does not exist."));
    }
    
    @Test
    void assertRejectInitializeWithInvalidOrigin() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Origin", "https://evil.example.com")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(createInitializeRequestBody()))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(403));
        assertTrue(initializeResponse.body().contains("Origin is not allowed"));
    }
    
    @Test
    void assertRejectFollowUpWithInvalidOrigin() throws IOException, InterruptedException, SQLException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Origin", "https://evil.example.com")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "tool-1",
                        "method", "tools/call",
                        "params", Map.of("name", "get_capabilities", "arguments", Map.of())))))
                .build();
        HttpResponse<String> actualResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actualResponse.statusCode(), is(403));
        assertTrue(actualResponse.body().contains("Origin is not allowed"));
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "streamable-http-runtime");
        try {
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    private String createInitializeRequestBody() {
        return JsonUtils.toJsonString(Map.of(
                "jsonrpc", "2.0",
                "id", "init-1",
                "method", "initialize",
                "params", Map.of(
                        "protocolVersion", MCPTransportConstants.PROTOCOL_VERSION,
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "integration-test", "version", "1.0.0"))));
    }
    
}
