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
import org.apache.shardingsphere.mcp.bootstrap.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreamableHttpTransportIT {
    
    private static final String ACCESS_TOKEN = "test-access-token";
    
    private static final String DATABASE_TYPE = "MySQL";
    
    private static final String DATABASE_VERSION = "8.0.36";
    
    private static final String JDBC_URL = "jdbc:mysql://bootstrap-http/test";
    
    private static final String LOOPBACK_BIND_HOST = "127.0.0.1";
    
    private static final String ENDPOINT_PATH = "/gateway";
    
    private static final String PROTOCOL_VERSION = MCPTransportConstants.PROTOCOL_VERSION;
    
    private static final String CONTENT_TYPE = "application/json";
    
    private static final String JSON_ACCEPT = "application/json, text/event-stream";
    
    private StreamableHttpMCPServer httpServer;
    
    @AfterEach
    void tearDown() {
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }
    }
    
    @Test
    void assertLaunchHttpServerWithConfiguredEndpoint() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> initializeResponse = sendInitializeRequest(httpClient, createJsonRequestHeaders(), createInitializeRequestParams("integration-test"));
        assertThat(initializeResponse.statusCode(), is(200));
        assertTrue(initializeResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertThat(initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(""), is(MCPTransportConstants.PROTOCOL_VERSION));
        Map<String, Object> initializePayload = parseJsonBody(initializeResponse.body());
        assertThat(initializePayload.get("jsonrpc"), is("2.0"));
        Map<String, Object> result = castToMap(initializePayload.get("result"));
        assertThat(result.get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
        String sessionId = initializeResponse.headers().firstValue("MCP-Session-Id").orElse("");
        assertFalse(sessionId.isEmpty());
        HttpResponse<String> toolCallResponse = sendToolCallRequest(httpClient, sessionId, "search_metadata",
                Map.of("database", "logic_db", "query", "order", "object_types", List.of("table")));
        assertThat(toolCallResponse.statusCode(), is(200));
        assertTrue(getPayloadItems(getStructuredContent(toolCallResponse.body())).stream().anyMatch(each -> "orders".equals(each.get("name"))));
        HttpResponse<String> resourceReadResponse = sendCapabilitiesRequest(httpClient, createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", sessionId,
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)));
        assertThat(resourceReadResponse.statusCode(), is(200));
        assertTrue(getResourcePayload(resourceReadResponse.body()).containsKey("supportedResources"));
        HttpResponse<String> deleteResponse = sendDeleteRequest(httpClient, Map.of("MCP-Session-Id", sessionId, "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(deleteResponse.statusCode(), is(200));
    }
    
    @Test
    void assertAcceptFollowUpRequestWithLowercaseHeaders() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders(Map.of(
                "mcp-session-id", session.sessionId(),
                "mcp-protocol-version", MCPTransportConstants.PROTOCOL_VERSION)));
        assertSuccessfulCapabilitiesResponse(actualResponse);
    }
    
    @Test
    void assertAcceptInitializeWithAccessToken() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime("127.0.0.1", false, ACCESS_TOKEN);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actualResponse = sendInitializeRequest(httpClient,
                createJsonRequestHeaders(Map.of("Authorization", getAuthorizationHeaderValue(ACCESS_TOKEN))), createInitializeRequestParams("integration-test"));
        assertThat(actualResponse.statusCode(), is(200));
        assertFalse(actualResponse.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithoutAccessToken() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime("127.0.0.1", false, ACCESS_TOKEN);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actualResponse = sendInitializeRequest(httpClient, createJsonRequestHeaders(), createInitializeRequestParams("integration-test"));
        assertThat(actualResponse.statusCode(), is(401));
    }
    
    @Test
    void assertRejectInitializeWithWrongAccessToken() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime("127.0.0.1", false, ACCESS_TOKEN);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actualResponse = sendInitializeRequest(httpClient,
                createJsonRequestHeaders(Map.of("Authorization", getAuthorizationHeaderValue("wrong-token"))), createInitializeRequestParams("integration-test"));
        assertThat(actualResponse.statusCode(), is(401));
    }
    
    @Test
    void assertRejectOpenStreamAfterDelete() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        sendDeleteRequest(session.httpClient(), Map.of("MCP-Session-Id", session.sessionId(), "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION));
        HttpResponse<String> streamResponse = openEventStream(session.httpClient(), Map.of(
                "Accept", "text/event-stream",
                "MCP-Session-Id", session.sessionId(),
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(streamResponse.statusCode(), is(404));
    }
    
    @Test
    void assertAcceptInitializeWithIpv6LoopbackOrigin() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> initializeResponse = sendInitializeRequest(httpClient,
                createJsonRequestHeaders(Map.of("Origin", "http://[::1]:8080")), createInitializeRequestParams("integration-test"));
        assertThat(initializeResponse.statusCode(), is(200));
        assertFalse(initializeResponse.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithInvalidOrigin() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> initializeResponse = sendInitializeRequest(httpClient,
                createJsonRequestHeaders(Map.of("Origin", "https://evil.example.com")), createInitializeRequestParams("integration-test"));
        assertThat(initializeResponse.statusCode(), is(403));
    }
    
    @Test
    void assertAcceptInitializeWithUnsupportedProtocolVersion() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime();
        HttpClient httpClient = HttpClient.newHttpClient();
        Map<String, Object> requestParams = createInitializeRequestParams("integration-test");
        requestParams.put("protocolVersion", "2024-11-05");
        HttpResponse<String> initializeResponse = sendInitializeRequest(httpClient, createJsonRequestHeaders(), requestParams);
        String sessionId = initializeResponse.headers().firstValue("MCP-Session-Id").orElse("");
        String protocolVersion = initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse("");
        assertThat(initializeResponse.statusCode(), is(200));
        assertFalse(sessionId.isEmpty());
        assertThat(protocolVersion, is(MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(castToMap(parseJsonBody(initializeResponse.body()).get("result")).get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(httpClient, createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", sessionId,
                "MCP-Protocol-Version", protocolVersion)));
        assertSuccessfulCapabilitiesResponse(actualResponse);
    }
    
    @Test
    void assertAcceptInitializeWithoutProtocolVersion() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime();
        HttpClient httpClient = HttpClient.newHttpClient();
        Map<String, Object> requestParams = createInitializeRequestParams("integration-test");
        requestParams.remove("protocolVersion");
        HttpResponse<String> initializeResponse = sendInitializeRequest(httpClient, createJsonRequestHeaders(), requestParams);
        String sessionId = initializeResponse.headers().firstValue("MCP-Session-Id").orElse("");
        String protocolVersion = initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse("");
        assertThat(initializeResponse.statusCode(), is(200));
        assertFalse(sessionId.isEmpty());
        assertThat(protocolVersion, is(MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(castToMap(parseJsonBody(initializeResponse.body()).get("result")).get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(httpClient, createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", sessionId,
                "MCP-Protocol-Version", protocolVersion)));
        assertSuccessfulCapabilitiesResponse(actualResponse);
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutAccessTokenBeforeSessionValidation() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", "missing-session",
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)));
        assertThat(actualResponse.statusCode(), is(401));
    }
    
    @Test
    void assertRejectFollowUpRequestWithWrongAccessToken() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders(Map.of(
                "Authorization", getAuthorizationHeaderValue("wrong-token"),
                "MCP-Session-Id", session.sessionId(),
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)));
        assertThat(actualResponse.statusCode(), is(401));
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutProtocolHeader() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders(Map.of("MCP-Session-Id", session.sessionId())));
        assertThat(actualResponse.statusCode(), is(400));
    }
    
    @Test
    void assertRejectFollowUpRequestWithProtocolMismatch() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", session.sessionId(),
                "MCP-Protocol-Version", "2024-11-05")));
        assertThat(actualResponse.statusCode(), is(400));
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutSessionId() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders());
        assertThat(actualResponse.statusCode(), is(400));
    }
    
    @Test
    void assertRejectFollowUpRequestWithUnknownSession() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> actualResponse = sendCapabilitiesRequest(session.httpClient(), createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", "missing-session",
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)));
        assertThat(actualResponse.statusCode(), is(404));
    }
    
    @Test
    void assertRejectInitializeWithInvalidAcceptHeader() throws IOException, InterruptedException, SQLException {
        launchJDBCRuntime();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actualResponse = sendInitializeRequest(httpClient,
                createJsonRequestHeaders(Map.of("Accept", "application/json")), createInitializeRequestParams("integration-test"));
        assertThat(actualResponse.statusCode(), is(400));
    }
    
    @Test
    void assertRejectFollowUpRequestWithMalformedRequestBody() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> actualResponse = sendRawPostRequest(session.httpClient(), createJsonRequestHeaders(Map.of(
                "MCP-Session-Id", session.sessionId(),
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION)), "not-json");
        assertThat(actualResponse.statusCode(), is(400));
    }
    
    @Test
    void assertRejectDeleteForClosedSession() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntime();
        HttpResponse<String> deleteResponse = sendDeleteRequest(session.httpClient(), session.sessionId());
        HttpResponse<String> actualResponse = sendDeleteRequest(session.httpClient(), session.sessionId());
        assertThat(deleteResponse.statusCode(), is(200));
        assertThat(actualResponse.statusCode(), is(404));
    }
    
    @Test
    void assertCloseSessionWithAccessToken() throws IOException, InterruptedException, SQLException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        HttpResponse<String> actualResponse = sendDeleteRequest(session.httpClient(), Map.of(
                "Authorization", getAuthorizationHeaderValue(session.accessToken()),
                "MCP-Session-Id", session.sessionId(),
                "MCP-Protocol-Version", MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(actualResponse.statusCode(), is(200));
    }
    
    private RuntimeHttpSession launchRuntime() throws SQLException, IOException, InterruptedException {
        return launchRuntime("");
    }
    
    private RuntimeHttpSession launchRuntime(final String accessToken) throws SQLException, IOException, InterruptedException {
        launchJDBCRuntime(LOOPBACK_BIND_HOST, false, accessToken);
        HttpClient httpClient = HttpClient.newHttpClient();
        return new RuntimeHttpSession(httpClient, initializeSession(httpClient, accessToken), accessToken);
    }
    
    private RuntimeHttpSession launchRuntimeWithAccessToken() throws SQLException, IOException, InterruptedException {
        return launchRuntime(ACCESS_TOKEN);
    }
    
    private void launchJDBCRuntime() throws SQLException, IOException {
        launchJDBCRuntime(LOOPBACK_BIND_HOST, false, "");
    }
    
    private void launchJDBCRuntime(final String bindHost, final boolean allowRemoteAccess, final String accessToken) throws SQLException, IOException {
        HttpTransportConfiguration httpConfig = new HttpTransportConfiguration(true, bindHost, allowRemoteAccess, accessToken, 0, ENDPOINT_PATH);
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(httpConfig, new StdioTransportConfiguration(false),
                Map.of("logic_db", createRuntimeDatabaseConfiguration()));
        httpServer = launchHttpServer(launchConfig);
    }
    
    private StreamableHttpMCPServer launchHttpServer(final MCPLaunchConfiguration launchConfig) throws IOException {
        MCPRuntimeServer actual = new MCPRuntimeLauncher().launch(launchConfig);
        if (actual instanceof StreamableHttpMCPServer) {
            return (StreamableHttpMCPServer) actual;
        }
        actual.stop();
        throw new IllegalStateException("HTTP server must be enabled for HTTP integration tests.");
    }
    
    private String initializeSession(final HttpClient httpClient, final String accessToken) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", JSON_ACCEPT)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", createInitializeRequestParams("jdbc-runtime-integration")))));
        addAuthorizationHeader(requestBuilder, accessToken);
        HttpResponse<String> actual = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    private URI createEndpointUri() {
        return URI.create(String.format("http://%s:%d%s", LOOPBACK_BIND_HOST, httpServer.getLocalPort(), ENDPOINT_PATH));
    }
    
    private HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                     final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return sendToolCallRequest(httpClient, sessionId, "", toolName, arguments);
    }
    
    private HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId, final String accessToken,
                                                     final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", JSON_ACCEPT)
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", toolName + "-1",
                        "method", "tools/call",
                        "params", Map.of("name", toolName, "arguments", arguments)))));
        addAuthorizationHeader(requestBuilder, accessToken);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .DELETE();
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final Map<String, String> requestHeaders) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri()).DELETE();
        requestHeaders.forEach(requestBuilder::header);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> result = getJsonRpcResult(responseBody);
        return result.containsKey("structuredContent") ? castToMap(result.get("structuredContent")) : Map.of();
    }
    
    private List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return castToList(payload.get("items"));
    }
    
    private Map<String, Object> getResourcePayload(final String responseBody) {
        return parseJsonBody(String.valueOf(getResultContents(responseBody).get(0).get("text")));
    }
    
    private Map<String, Object> getJsonRpcResult(final String responseBody) {
        return castToMap(parseJsonBody(responseBody).get("result"));
    }
    
    private Map<String, Object> createInitializeRequestParams(final String clientName) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", clientName, "version", "1.0.0"));
        return result;
    }
    
    private Map<String, String> createJsonRequestHeaders() {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("Content-Type", CONTENT_TYPE);
        result.put("Accept", JSON_ACCEPT);
        return result;
    }
    
    private Map<String, String> createJsonRequestHeaders(final Map<String, String> additionalHeaders) {
        Map<String, String> result = new LinkedHashMap<>(2 + additionalHeaders.size(), 1F);
        result.putAll(createJsonRequestHeaders());
        result.putAll(additionalHeaders);
        return result;
    }
    
    private Map<String, Object> parseJsonBody(final String responseBody) {
        return JsonUtils.fromJsonString(normalizeJsonBody(responseBody), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> castToMap(final Object value) {
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
    
    private String getAuthorizationHeaderValue(final String accessToken) {
        return "Bearer " + accessToken;
    }
    
    private void addAuthorizationHeader(final HttpRequest.Builder requestBuilder, final String accessToken) {
        if (!accessToken.isEmpty()) {
            requestBuilder.header("Authorization", getAuthorizationHeaderValue(accessToken));
        }
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration() throws SQLException {
        Connection connection = createMetadataConnection();
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        when(result.getDatabaseType()).thenReturn(DATABASE_TYPE);
        when(result.openConnection(anyString())).thenReturn(connection);
        return result;
    }
    
    private Connection createMetadataConnection() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Map<String, List<String>> columns = Map.of(
                "orders", List.of("amount", "order_id", "status"),
                "order_items", List.of("item_id", "order_id", "sku"));
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(DATABASE_TYPE);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(DATABASE_VERSION);
        when(databaseMetaData.getURL()).thenReturn(JDBC_URL);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0])
                    ? mockMultiRowResultSet(List.of(Map.of("TABLE_NAME", "orders"), Map.of("TABLE_NAME", "order_items")))
                    : mockMultiRowResultSet(List.of());
        });
        when(databaseMetaData.getColumns(isNull(), isNull(), anyString(), eq("%"))).thenAnswer(invocation -> {
            String objectName = invocation.getArgument(2);
            return mockResultSet("COLUMN_NAME", columns.getOrDefault(objectName, List.of()).toArray(new String[0]));
        });
        when(databaseMetaData.getIndexInfo(isNull(), isNull(), anyString(), eq(false), eq(false))).thenAnswer(invocation -> mockResultSet("INDEX_NAME"));
        return result;
    }
    
    private ResultSet mockResultSet(final String columnName, final String... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger nextIndex = new AtomicInteger();
        AtomicInteger valueIndex = new AtomicInteger();
        when(result.next()).thenAnswer(invocation -> nextIndex.getAndIncrement() < values.length);
        when(result.getString(columnName)).thenAnswer(invocation -> values[valueIndex.getAndIncrement()]);
        return result;
    }
    
    private ResultSet mockMultiRowResultSet(final List<Map<String, String>> values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < values.size());
        when(result.getString(anyString())).thenAnswer(invocation -> {
            int currentRowIndex = rowIndex.get();
            return 0 <= currentRowIndex && currentRowIndex < values.size() ? values.get(currentRowIndex).get(invocation.getArgument(0)) : null;
        });
        return result;
    }
    
    private HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> requestHeaders,
                                                       final Map<String, Object> requestParams) throws IOException, InterruptedException {
        return sendPostRequest(httpClient, requestHeaders, Map.of("jsonrpc", "2.0", "id", "init-1", "method", "initialize", "params", requestParams));
    }
    
    private HttpResponse<String> sendCapabilitiesRequest(final HttpClient httpClient, final Map<String, String> requestHeaders) throws IOException, InterruptedException {
        return sendPostRequest(httpClient, requestHeaders, Map.of(
                "jsonrpc", "2.0",
                "id", "resource-1",
                "method", "resources/read",
                "params", Map.of("uri", "shardingsphere://capabilities")));
    }
    
    private HttpResponse<String> openEventStream(final HttpClient httpClient, final Map<String, String> requestHeaders) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri()).GET();
        requestHeaders.forEach(requestBuilder::header);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpResponse<String> sendPostRequest(final HttpClient httpClient, final Map<String, String> requestHeaders,
                                                 final Map<String, Object> requestBody) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, requestHeaders, JsonUtils.toJsonString(requestBody));
    }
    
    private HttpResponse<String> sendRawPostRequest(final HttpClient httpClient, final Map<String, String> requestHeaders,
                                                    final String requestBody) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        requestHeaders.forEach(requestBuilder::header);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private void assertSuccessfulCapabilitiesResponse(final HttpResponse<String> actualResponse) {
        assertThat(actualResponse.statusCode(), is(200));
        assertTrue(getResourcePayload(actualResponse.body()).containsKey("supportedTools"));
    }
    
    private record RuntimeHttpSession(HttpClient httpClient, String sessionId, String accessToken) {
    }
}
