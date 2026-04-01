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

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionStatementExecutor;
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
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractMCPE2ETest {
    
    private static final String PROTOCOL_VERSION = "2025-11-25";
    
    private static final String ENDPOINT_PATH = "/gateway";
    
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
    
    protected final void launchRuntime() {
        launchRuntimeInternal();
    }
    
    protected final HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRequestHeaders(), createInitializeRequestParams());
        
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    protected final String initializeSessionWithoutProtocolVersion(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRequestHeaders(), createInitializeRequestParamsWithoutProtocolVersion());
        
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final Map<String, String> requestHeaders, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(requestHeaders, sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", toolName + "-1",
                        "method", "tools/call",
                        "params", Map.of("name", toolName, "arguments", arguments)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final Map<String, String> requestHeaders, final String sessionId,
                                                                 final String resourceUri) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(requestHeaders, sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "resource-1",
                        "method", "resources/read",
                        "params", Map.of("uri", resourceUri)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final Map<String, String> requestHeaders,
                                                           final String sessionId) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .DELETE();
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    protected final Map<String, Object> parseJsonBody(final String responseBody) {
        return JsonUtils.fromJsonString(normalizeJsonBody(responseBody), new TypeReference<>() {
        });
    }
    
    protected final Map<String, Object> getJsonRpcResult(final String responseBody) {
        return castToMap(parseJsonBody(responseBody).get("result"));
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> result = getJsonRpcResult(responseBody);
        if (result.containsKey("structuredContent")) {
            return castToMap(result.get("structuredContent"));
        }
        List<Map<String, Object>> content = getResultContents(responseBody);
        return content.isEmpty() ? Map.of() : parseJsonBody(String.valueOf(content.get(0).get("text")));
    }
    
    protected final List<Map<String, Object>> getResultContents(final String responseBody) {
        return castToList(getJsonRpcResult(responseBody).get("content"));
    }
    
    protected final Map<String, Object> getFirstResourcePayload(final String responseBody) {
        List<Map<String, Object>> contents = castToList(getJsonRpcResult(responseBody).get("contents"));
        return parseJsonBody(String.valueOf(contents.get(0).get("text")));
    }
    
    protected final List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return castToList(payload.get("items"));
    }
    
    protected final Map<String, Object> getNestedPayload(final Map<String, Object> payload, final String key) {
        return castToMap(payload.get(key));
    }
    
    protected final DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, DatabaseMetadataSnapshot> databaseSnapshots = new LinkedHashMap<>();
        databaseSnapshots.put("logic_db", new DatabaseMetadataSnapshot("MySQL", "", List.of(
                new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "order_items", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.VIEW, "active_orders", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.COLUMN, "order_id", "TABLE", "orders"),
                new MetadataObject("logic_db", "public", MetadataObjectType.COLUMN, "status", "TABLE", "orders"),
                new MetadataObject("logic_db", "public", MetadataObjectType.INDEX, "idx_orders_status", "TABLE", "orders"),
                new MetadataObject("logic_db", "public", MetadataObjectType.MATERIALIZED_VIEW, "mv_orders", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.SEQUENCE, "order_seq", "", ""))));
        databaseSnapshots.put("analytics_db", new DatabaseMetadataSnapshot("PostgreSQL", "", List.of(
                new MetadataObject("analytics_db", "public", MetadataObjectType.SCHEMA, "public", "", ""),
                new MetadataObject("analytics_db", "public", MetadataObjectType.TABLE, "metrics", "", ""),
                new MetadataObject("analytics_db", "public", MetadataObjectType.COLUMN, "metric_id", "TABLE", "metrics"))));
        databaseSnapshots.put("warehouse", new DatabaseMetadataSnapshot("Hive", "", List.of(
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.SCHEMA, "warehouse", "", ""),
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.TABLE, "facts", "", ""),
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.COLUMN, "fact_id", "TABLE", "facts"))));
        return new DatabaseMetadataSnapshots(databaseSnapshots);
    }
    
    protected final Map<String, String> createRequestHeaders() {
        return Map.of();
    }
    
    private void launchRuntimeInternal() {
        DatabaseMetadataSnapshots databaseMetadataSnapshots = createDatabaseMetadataSnapshots();
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = createRuntimeDatabases();
        try {
            initializeRuntimeDatabases(runtimeDatabases);
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to initialize MCP E2E runtime databases.", ex);
        }
        StreamableHttpMCPServer httpServer = new StreamableHttpMCPServer(new HttpTransportConfiguration(true, "127.0.0.1", 0, ENDPOINT_PATH),
                createRuntimeContext(runtimeDatabases, databaseMetadataSnapshots));
        try {
            httpServer.start();
        } catch (final IOException ex) {
            httpServer.stop();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        this.httpServer = httpServer;
    }
    
    private MCPRuntimeContext createRuntimeContext(final Map<String, RuntimeDatabaseConfiguration> databaseConfigs, final DatabaseMetadataSnapshots databaseMetadataSnapshots) {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPJdbcTransactionResourceManager transactionResourceManager = new MCPJdbcTransactionResourceManager(databaseConfigs);
        MCPJdbcTransactionStatementExecutor transactionStatementExecutor = new MCPJdbcTransactionStatementExecutor(sessionManager, transactionResourceManager);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(databaseConfigs, transactionResourceManager);
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(databaseMetadataSnapshots);
        ExecuteQueryFacade executeQueryFacade = new ExecuteQueryFacade(
                capabilityBuilder, transactionStatementExecutor, statementExecutor, new MetadataRefreshCoordinator(databaseConfigs, databaseMetadataSnapshots));
        return new MCPRuntimeContext(sessionManager, transactionResourceManager, transactionStatementExecutor, statementExecutor, databaseMetadataSnapshots, capabilityBuilder, executeQueryFacade);
    }
    
    private HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> requestHeaders,
                                                       final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", initializeRequestParams))));
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpRequest.Builder createJsonRequestBuilder(final Map<String, String> requestHeaders, final String sessionId) {
        HttpRequest.Builder result = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION);
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            result.header(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private URI createEndpointUri() {
        int localPort = httpServer.getLocalPort();
        return URI.create(String.format("http://127.0.0.1:%d%s", localPort, ENDPOINT_PATH));
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", "e2e-test", "version", "1.0.0"));
        return result;
    }
    
    private Map<String, Object> createInitializeRequestParamsWithoutProtocolVersion() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", "e2e-test", "version", "1.0.0"));
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
    
    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>();
        result.put("logic_db", createRuntimeDatabaseConfiguration("abstract-mcp-e2e-logic"));
        result.put("analytics_db", createRuntimeDatabaseConfiguration("abstract-mcp-e2e-analytics"));
        result.put("warehouse", createRuntimeDatabaseConfiguration("abstract-mcp-e2e-warehouse"));
        return result;
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseName) {
        return new RuntimeDatabaseConfiguration("H2", H2RuntimeTestSupport.createJdbcUrl(tempDir, databaseName), "", "", "org.h2.Driver");
    }
    
    private void initializeRuntimeDatabases(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) throws SQLException {
        H2RuntimeTestSupport.initializeDatabase(runtimeDatabases.get("logic_db").getJdbcUrl());
        H2RuntimeTestSupport.executeStatements(runtimeDatabases.get("analytics_db").getJdbcUrl(),
                "CREATE SCHEMA IF NOT EXISTS public",
                "SET SCHEMA public",
                "CREATE TABLE IF NOT EXISTS metrics (metric_id INT PRIMARY KEY, metric_name VARCHAR(32))",
                "MERGE INTO metrics (metric_id, metric_name) KEY (metric_id) VALUES (10, 'cpu')",
                "MERGE INTO metrics (metric_id, metric_name) KEY (metric_id) VALUES (20, 'memory')");
        H2RuntimeTestSupport.executeStatements(runtimeDatabases.get("warehouse").getJdbcUrl(),
                "CREATE SCHEMA IF NOT EXISTS warehouse",
                "SET SCHEMA warehouse",
                "CREATE TABLE IF NOT EXISTS facts (fact_id INT PRIMARY KEY, total INT)",
                "MERGE INTO facts (fact_id, total) KEY (fact_id) VALUES (100, 1)",
                "MERGE INTO facts (fact_id, total) KEY (fact_id) VALUES (200, 2)");
    }
}
