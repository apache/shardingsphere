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
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory;
import org.apache.shardingsphere.mcp.bootstrap.runtime.JdbcMetadataLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractProductionRuntimeE2ETest {
    
    private static final String PROTOCOL_VERSION = "2025-11-25";
    
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
    
    protected final void launchProductionRuntime() throws IOException {
        prepareRuntimeFixture();
        Path configFile = tempDir.resolve("mcp.yaml");
        Files.writeString(configFile, createConfigurationContent());
        httpServer = createStartedHttpServer(configFile);
    }
    
    protected final HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of());
        
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
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
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
        return result.containsKey("structuredContent") ? castToMap(result.get("structuredContent")) : Map.of();
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
    
    protected String getEndpointPath() {
        return "/gateway";
    }
    
    protected Map<String, Map<String, String>> getRuntimeDatabases() {
        return Map.of();
    }
    
    protected void prepareRuntimeFixture() throws IOException {
    }
    
    protected final Path getTempDir() {
        return tempDir;
    }
    
    private HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> requestHeaders) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", "init-1",
                        "method", "initialize",
                        "params", createInitializeRequestParams()))));
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
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
        return URI.create(String.format("http://127.0.0.1:%d%s", localPort, getEndpointPath()));
    }
    
    private StreamableHttpMCPServer createStartedHttpServer(final Path configFile) throws IOException {
        MCPLaunchConfiguration launchConfiguration = MCPConfigurationLoader.load(configFile.toString());
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
        MetadataCatalog metadataCatalog = metadataLoader.load(launchConfiguration.getRuntimeDatabases());
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.createDatabaseRuntime(launchConfiguration.getRuntimeDatabases(), metadataCatalog, metadataLoader);
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager, metadataCatalog, databaseRuntime);
        StreamableHttpMCPServer result = new StreamableHttpMCPServer(launchConfiguration.getTransport().getHttp(), sessionManager, runtimeServices, metadataCatalog, databaseRuntime);
        try {
            result.start();
        } catch (final IOException ex) {
            result.stop();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        return result;
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", "production-runtime-e2e", "version", "1.0.0"));
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
    
    private String createConfigurationContent() {
        StringBuilder result = new StringBuilder();
        result.append("transport:\n");
        result.append("  http:\n");
        result.append("    enabled: true\n");
        result.append("    bindHost: 127.0.0.1\n");
        result.append("    port: 0\n");
        result.append("    endpointPath: ").append(toYamlScalar(getEndpointPath())).append('\n');
        result.append("  stdio:\n");
        result.append("    enabled: false\n");
        Map<String, Map<String, String>> runtimeDatabases = getRuntimeDatabases();
        if (!runtimeDatabases.isEmpty()) {
            result.append("runtimeDatabases:\n");
            for (Entry<String, Map<String, String>> databaseEntry : runtimeDatabases.entrySet()) {
                appendDatabaseEntry(result, databaseEntry.getKey(), databaseEntry.getValue());
            }
        }
        return result.toString();
    }
    
    private void appendDatabaseEntry(final StringBuilder result, final String databaseName, final Map<String, String> properties) {
        result.append("  ").append(databaseName).append(":\n");
        appendDatabaseProperties(result, "    ", properties);
    }
    
    private void appendDatabaseProperties(final StringBuilder result, final String indent, final Map<String, String> properties) {
        for (Entry<String, String> entry : properties.entrySet()) {
            if (shouldSkipEntry(entry.getKey())) {
                continue;
            }
            result.append(indent).append(entry.getKey()).append(": ").append(toYamlScalar(entry.getValue())).append('\n');
        }
    }
    
    private boolean shouldSkipEntry(final String key) {
        return "databaseName".equals(key) || "schemaPattern".equals(key) || "defaultSchema".equals(key)
                || "supportsCrossSchemaSql".equals(key) || "supportsExplainAnalyze".equals(key);
    }
    
    private String toYamlScalar(final String value) {
        return '\'' + value.replace("'", "''") + '\'';
    }
}
