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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntime;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.runtime.H2RuntimeTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamableHttpRuntimeIntegrationTest {
    
    @TempDir
    private Path tempDir;
    
    private MCPRuntime runtime;
    
    @AfterEach
    void tearDown() {
        if (null != runtime) {
            runtime.close();
            runtime = null;
        }
    }
    
    @Test
    void assertLaunchHttpServerWithConfiguredEndpoint() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(createInitializeRequestBody()))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(200));
        assertTrue(initializeResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertThat(initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(""), is("2025-11-25"));
        Map<String, Object> initializePayload = parseJsonBody(initializeResponse.body());
        assertThat(initializePayload.get("jsonrpc"), is("2.0"));
        Map<String, Object> result = castToMap(initializePayload.get("result"));
        assertThat(result.get("protocolVersion"), is("2025-11-25"));
        String sessionId = initializeResponse.headers().firstValue("MCP-Session-Id").orElse("");
        assertFalse(sessionId.isEmpty());
        HttpRequest toolCallRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", "2025-11-25")
                .POST(HttpRequest.BodyPublishers.ofString(createToolCallRequestBody()))
                .build();
        HttpResponse<String> toolCallResponse = httpClient.send(toolCallRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(toolCallResponse.statusCode(), is(200));
        assertTrue(toolCallResponse.body().contains("supportedTools"));
        assertTrue(toolCallResponse.body().contains("execute_query"));
        HttpRequest resourceReadRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", "2025-11-25")
                .POST(HttpRequest.BodyPublishers.ofString(createResourceReadRequestBody()))
                .build();
        HttpResponse<String> resourceReadResponse = httpClient.send(resourceReadRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(resourceReadResponse.statusCode(), is(200));
        assertTrue(resourceReadResponse.body().contains("supportedResources"));
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", "2025-11-25")
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(200));
    }
    
    @Test
    void assertLaunchHttpServerWithoutInitializeProtocolVersion() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(createInitializeRequestBodyWithoutProtocolVersion()))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(200));
        assertThat(initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(""), is("2025-11-25"));
        Map<String, Object> initializePayload = parseJsonBody(initializeResponse.body());
        Map<String, Object> result = castToMap(initializePayload.get("result"));
        assertThat(result.get("protocolVersion"), is("2025-11-25"));
        assertFalse(initializeResponse.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectDeleteWithoutSessionHeader() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(400));
        assertTrue(deleteResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertTrue(deleteResponse.body().contains("Session ID required in mcp-session-id header"));
    }
    
    @Test
    void assertRejectDeleteWithMissingSession() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", "missing-session")
                .header("MCP-Protocol-Version", "2025-11-25")
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(deleteResponse.statusCode(), is(404));
        assertTrue(deleteResponse.body().contains("Session does not exist."));
    }
    
    @Test
    void assertRejectDeleteWithProtocolVersionMismatch() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
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
    void assertRejectOpenStreamAfterDelete() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest deleteRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", "2025-11-25")
                .DELETE()
                .build();
        httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest streamRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Accept", "text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", "2025-11-25")
                .GET()
                .build();
        HttpResponse<String> streamResponse = httpClient.send(streamRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(streamResponse.statusCode(), is(404));
        assertTrue(streamResponse.body().contains("Session does not exist."));
    }
    
    @Test
    void assertRejectInitializeWithInvalidOrigin() throws IOException, InterruptedException {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPLaunchConfiguration runtimeConfiguration = createRuntimeConfiguration();
        runtime = runtimeLauncher.launch(runtimeConfiguration);
        HttpClient httpClient = HttpClient.newHttpClient();
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
    
    private URI createEndpointUri() {
        int localPort = runtime.getHttpServer().orElseThrow().getLocalPort();
        return URI.create(String.format("http://127.0.0.1:%d%s", localPort, "/gateway"));
    }
    
    private MCPLaunchConfiguration createRuntimeConfiguration() {
        return new MCPLaunchConfiguration(
                new MCPTransportConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", 0, "/gateway"), new StdioTransportConfiguration(false)),
                createRuntimeDatabases());
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "streamable-http-runtime");
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
                        "protocolVersion", "2025-11-25",
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "integration-test", "version", "1.0.0"))));
    }
    
    private String createInitializeRequestBodyWithoutProtocolVersion() {
        return JsonUtils.toJsonString(Map.of(
                "jsonrpc", "2.0",
                "id", "init-1",
                "method", "initialize",
                "params", Map.of(
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "integration-test", "version", "1.0.0"))));
    }
    
    private String createToolCallRequestBody() {
        return JsonUtils.toJsonString(Map.of(
                "jsonrpc", "2.0",
                "id", "tool-1",
                "method", "tools/call",
                "params", Map.of("name", "get_capabilities", "arguments", Map.of())));
    }
    
    private String createResourceReadRequestBody() {
        return JsonUtils.toJsonString(Map.of(
                "jsonrpc", "2.0",
                "id", "resource-1",
                "method", "resources/read",
                "params", Map.of("uri", "shardingsphere://capabilities")));
    }
    
    private String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(createInitializeRequestBody()))
                .build();
        HttpResponse<String> initializeResponse = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(initializeResponse.statusCode(), is(200));
        return initializeResponse.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    private Map<String, Object> castToMap(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> parseJsonBody(final String responseBody) {
        return JsonUtils.fromJsonString(responseBody, new TypeReference<>() {
        });
    }
}
