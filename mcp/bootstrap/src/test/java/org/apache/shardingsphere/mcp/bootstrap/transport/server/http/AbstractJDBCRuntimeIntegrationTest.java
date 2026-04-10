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

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractJDBCRuntimeIntegrationTest {
    
    private static final String PROTOCOL_VERSION = MCPTransportConstants.PROTOCOL_VERSION;
    
    @TempDir
    private Path tempDir;
    
    private StreamableHttpMCPServer httpServer;
    
    @AfterEach
    void tearDown() {
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }
    }
    
    protected final void launchJDBCRuntime() throws SQLException, IOException {
        prepareRuntimeFixture();
        httpServer = launchHttpServer(createRuntimeConfiguration());
    }
    
    protected final URI createEndpointUri() {
        int localPort = httpServer.getLocalPort();
        return URI.create(String.format("http://127.0.0.1:%d/gateway", localPort));
    }
    
    protected final HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }
    
    protected final RuntimeHttpSession launchRuntimeWithSession() throws SQLException, IOException, InterruptedException {
        launchJDBCRuntime();
        HttpClient httpClient = createHttpClient();
        return new RuntimeHttpSession(httpClient, initializeSession(httpClient));
    }
    
    protected final void stopRuntime() {
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpRequest initializeRequest = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", createInitializeRequestParams("jdbc-runtime-integration")))))
                .build();
        HttpResponse<String> actual = httpClient.send(initializeRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", toolName + "-1",
                        "method", "tools/call",
                        "params", Map.of("name", toolName, "arguments", arguments)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final String sessionId, final String resourceUri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "resource-read-1",
                        "method", "resources/read",
                        "params", Map.of("uri", resourceUri)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final Map<String, Object> callToolAndGetStructuredContent(final RuntimeHttpSession session, final String toolName,
                                                                        final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(session.httpClient(), session.sessionId(), toolName, arguments);
        assertThat(actual.statusCode(), is(200));
        return getStructuredContent(actual.body());
    }
    
    protected final Map<String, Object> readResourceAndGetPayload(final RuntimeHttpSession session, final String resourceUri) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendResourceReadRequest(session.httpClient(), session.sessionId(), resourceUri);
        assertThat(actual.statusCode(), is(200));
        return getResourcePayload(actual.body());
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> result = getJsonRpcResult(responseBody);
        return result.containsKey("structuredContent") ? castToMap(result.get("structuredContent")) : Map.of();
    }
    
    protected final List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return castToList(payload.get("items"));
    }
    
    protected final List<String> getStringList(final Map<String, Object> payload, final String fieldName) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(payload.get(fieldName)), new TypeReference<>() {
        });
    }
    
    protected final Map<String, Object> getResourcePayload(final String responseBody) {
        return parseJsonBody(String.valueOf(getResultContents(responseBody).get(0).get("text")));
    }
    
    protected final Map<String, Object> getJsonRpcResult(final String responseBody) {
        return castToMap(parseJsonBody(responseBody).get("result"));
    }
    
    private MCPLaunchConfiguration createRuntimeConfiguration() {
        return new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", false, 0, "/gateway"), new StdioTransportConfiguration(false), createRuntimeDatabases());
    }
    
    private StreamableHttpMCPServer launchHttpServer(final MCPLaunchConfiguration launchConfiguration) throws IOException {
        MCPRuntimeServer actual = new MCPRuntimeLauncher().launch(launchConfiguration);
        if (actual instanceof StreamableHttpMCPServer) {
            return (StreamableHttpMCPServer) actual;
        }
        actual.stop();
        throw new IllegalStateException("HTTP server must be enabled for HTTP integration tests.");
    }
    
    protected final Map<String, Object> createInitializeRequestParams(final String clientName) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", clientName, "version", "1.0.0"));
        return result;
    }
    
    protected final Map<String, Object> parseJsonBody(final String responseBody) {
        return JsonUtils.fromJsonString(normalizeJsonBody(responseBody), new TypeReference<>() {
        });
    }
    
    protected final Map<String, Object> castToMap(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private List<Map<String, Object>> getResultContents(final String responseBody) {
        return castToList(getJsonRpcResult(responseBody).get("contents"));
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
    
    protected void prepareRuntimeFixture() throws SQLException {
    }
    
    protected abstract Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases();
    
    protected final Path getTempDir() {
        return tempDir;
    }
    
    protected record RuntimeHttpSession(HttpClient httpClient, String sessionId) {
    }
}
