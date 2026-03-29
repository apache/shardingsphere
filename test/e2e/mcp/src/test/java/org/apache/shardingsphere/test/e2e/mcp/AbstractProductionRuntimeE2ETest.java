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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper.YamlMCPLaunchConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
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
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(createEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    private Map<String, Object> parseJsonBody(final String responseBody) {
        return JsonUtils.fromJsonString(normalizeJsonBody(responseBody), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> getJsonRpcResult(final String responseBody) {
        return castToMap(parseJsonBody(responseBody).get("result"));
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> result = getJsonRpcResult(responseBody);
        return result.containsKey("structuredContent") ? castToMap(result.get("structuredContent")) : Map.of();
    }
    
    protected final List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return castToList(payload.get("items"));
    }
    
    protected final Map<String, Object> getNestedPayload(final Map<String, Object> payload, final String key) {
        return castToMap(payload.get(key));
    }
    
    protected abstract Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases();
    
    protected abstract void prepareRuntimeFixture() throws IOException;
    
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
        return URI.create(String.format("http://127.0.0.1:%d%s", localPort, ENDPOINT_PATH));
    }
    
    private StreamableHttpMCPServer createStartedHttpServer(final Path configFile) throws IOException {
        MCPLaunchConfiguration launchConfiguration = MCPConfigurationLoader.load(configFile.toString());
        MCPRuntimeServer server = new MCPRuntimeLauncher().launch(launchConfiguration);
        if (!(server instanceof StreamableHttpMCPServer)) {
            server.stop();
            throw new IllegalStateException("HTTP transport must be enabled for production runtime E2E tests.");
        }
        return (StreamableHttpMCPServer) server;
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
        return YamlEngine.marshal(new YamlMCPLaunchConfigurationSwapper().swapToYamlConfiguration(new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", 0, ENDPOINT_PATH), new StdioTransportConfiguration(false), getRuntimeDatabases())));
    }
}
