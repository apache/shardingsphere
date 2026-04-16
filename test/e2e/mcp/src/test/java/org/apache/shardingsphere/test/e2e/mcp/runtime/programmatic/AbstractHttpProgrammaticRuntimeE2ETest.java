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

package org.apache.shardingsphere.test.e2e.mcp.runtime.programmatic;

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
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

abstract class AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String PROTOCOL_VERSION = "2025-11-25";
    
    private static final String CLIENT_NAME = "mcp-e2e-programmatic";
    
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
    
    protected final void launchHttpTransport() {
        MCPDatabaseMetadataCatalog metadataCatalog = createDatabaseMetadataCatalog();
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = createRuntimeDatabases();
        try {
            initializeRuntimeDatabases(runtimeDatabases);
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to initialize MCP E2E runtime databases.", ex);
        }
        StreamableHttpMCPServer httpServer = new StreamableHttpMCPServer(
                new HttpTransportConfiguration(true, "127.0.0.1", false, "", 0, ENDPOINT_PATH),
                new MCPRuntimeContext(new MCPSessionManager(runtimeDatabases), metadataCatalog));
        try {
            httpServer.start();
        } catch (final IOException ex) {
            httpServer.stop();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        this.httpServer = httpServer;
    }
    
    private MCPDatabaseMetadataCatalog createDatabaseMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(3, 1F);
        databaseMetadataMap.put("logic_db", createLogicDatabaseMetadata());
        databaseMetadataMap.put("analytics_db", createAnalyticsDatabaseMetadata());
        databaseMetadataMap.put("warehouse", createWarehouseDatabaseMetadata());
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
    
    private MCPDatabaseMetadata createLogicDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders", List.of(
                                new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id"),
                                new MCPColumnMetadata("logic_db", "public", "orders", "", "status")),
                                List.of(new MCPIndexMetadata("logic_db", "public", "orders", "idx_orders_status"))),
                        new MCPTableMetadata("logic_db", "public", "order_items", List.of(
                                new MCPColumnMetadata("logic_db", "public", "order_items", "", "order_id")), List.of())),
                        List.of(new MCPViewMetadata("logic_db", "public", "active_orders",
                                List.of(new MCPColumnMetadata("logic_db", "public", "", "active_orders", "order_id")))))));
    }
    
    private MCPDatabaseMetadata createAnalyticsDatabaseMetadata() {
        return new MCPDatabaseMetadata("analytics_db", "PostgreSQL", "", List.of(
                new MCPSchemaMetadata("analytics_db", "public", List.of(
                        new MCPTableMetadata("analytics_db", "public", "metrics", List.of(
                                new MCPColumnMetadata("analytics_db", "public", "metrics", "", "metric_id")), List.of())),
                        List.of(), List.of(new MCPSequenceMetadata("analytics_db", "public", "metric_seq")))));
    }
    
    private MCPDatabaseMetadata createWarehouseDatabaseMetadata() {
        return new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(
                        new MCPTableMetadata("warehouse", "warehouse", "facts", List.of(
                                new MCPColumnMetadata("warehouse", "warehouse", "facts", "", "fact_id")), List.of())), List.of())));
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    private HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", initializeRequestParams))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", toolName + "-1",
                        "method", "tools/call",
                        "params", Map.of("name", toolName, "arguments", arguments)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final String sessionId,
                                                                 final String resourceUri) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "resource-1",
                        "method", "resources/read",
                        "params", Map.of("uri", resourceUri)))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> payload = MCPInteractionPayloads.parseJsonPayload(responseBody);
        return MCPInteractionPayloads.hasJsonRpcError(payload) ? MCPInteractionPayloads.getJsonRpcErrorPayload(payload) : MCPInteractionPayloads.getStructuredContent(payload);
    }
    
    protected final Map<String, Object> getFirstResourcePayload(final String responseBody) {
        Map<String, Object> payload = MCPInteractionPayloads.parseJsonPayload(responseBody);
        return MCPInteractionPayloads.hasJsonRpcError(payload) ? MCPInteractionPayloads.getJsonRpcErrorPayload(payload) : MCPInteractionPayloads.getFirstResourcePayload(payload);
    }
    
    private HttpRequest.Builder createJsonRequestBuilder(final String sessionId) {
        return HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION);
    }
    
    private URI createEndpointUri() {
        int localPort = httpServer.getLocalPort();
        return URI.create(String.format("http://127.0.0.1:%d%s", localPort, ENDPOINT_PATH));
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", CLIENT_NAME, "version", "1.0.0"));
        return result;
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(3, 1F);
        result.put("logic_db", createRuntimeDatabaseConfiguration("abstract-mcp-e2e-logic", "public"));
        result.put("analytics_db", createRuntimeDatabaseConfiguration("abstract-mcp-e2e-analytics", "public"));
        result.put("warehouse", createRuntimeDatabaseConfiguration("abstract-mcp-e2e-warehouse", "warehouse"));
        return result;
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseName, final String defaultSchema) {
        String jdbcUrl = String.format("%s;INIT=CREATE SCHEMA IF NOT EXISTS %s\\;SET SCHEMA %s",
                H2RuntimeTestSupport.createJdbcUrl(tempDir, databaseName, RuntimeTransport.HTTP), defaultSchema, defaultSchema);
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
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
