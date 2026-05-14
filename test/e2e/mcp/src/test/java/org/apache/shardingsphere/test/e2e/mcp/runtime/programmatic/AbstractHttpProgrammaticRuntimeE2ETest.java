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

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractHttpProgrammaticRuntimeE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String CLIENT_NAME = "mcp-e2e-programmatic";
    
    private Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    protected final void launchHttpTransport() throws IOException {
        prepareRuntime();
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient);
        assertThat(actual.statusCode(), is(200));
        String result = actual.headers().firstValue("MCP-Session-Id").orElseThrow();
        assertThat(sendInitializedNotification(httpClient, result).statusCode(), is(202));
        return result;
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, MCPHttpTransportTestSupport.createInitializeRequestParams(CLIENT_NAME));
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, Map.of(), initializeRequestParams);
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> headers,
                                                               final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        return sendJsonRpcRequest(httpClient, headers, "init-1", "initialize", initializeRequestParams);
    }
    
    protected final HttpResponse<String> sendInitializedNotification(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId),
                MCPHttpTransportTestSupport.createJsonRpcNotificationBody("notifications/initialized", Map.of()));
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return sendJsonRpcRequest(httpClient, createSessionHeaders(sessionId), toolName + "-1", "tools/call", Map.of("name", toolName, "arguments", arguments));
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final String sessionId,
                                                                 final String resourceUri) throws IOException, InterruptedException {
        return sendJsonRpcRequest(httpClient, createSessionHeaders(sessionId), "resource-1", "resources/read", Map.of("uri", resourceUri));
    }
    
    protected final HttpResponse<String> sendCapabilitiesRequest(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        return sendJsonRpcRequest(httpClient, headers, "resource-1", "resources/read", Map.of("uri", "shardingsphere://capabilities"));
    }
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(getEndpointUri()).DELETE();
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendRawPostRequest(final HttpClient httpClient, final Map<String, String> headers,
                                                            final String requestBody) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = MCPHttpTransportTestSupport.createJsonRequestBuilder(getEndpointUri())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> openEventStream(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(getEndpointUri()).GET();
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> payload = MCPInteractionPayloads.parseJsonPayload(responseBody);
        return MCPInteractionPayloads.hasJsonRpcError(payload) ? MCPInteractionPayloads.getJsonRpcErrorPayload(payload) : MCPInteractionPayloads.getStructuredContent(payload);
    }
    
    protected final Map<String, Object> getFirstResourcePayload(final String responseBody) {
        Map<String, Object> payload = MCPInteractionPayloads.parseJsonPayload(responseBody);
        return MCPInteractionPayloads.hasJsonRpcError(payload) ? MCPInteractionPayloads.getJsonRpcErrorPayload(payload) : MCPInteractionPayloads.getFirstResourcePayload(payload);
    }
    
    protected final Map<String, Object> parseJsonBody(final String responseBody) {
        return MCPInteractionPayloads.parseJsonPayload(responseBody);
    }
    
    protected final Map<String, Object> castToMap(final Object value) {
        return MCPInteractionPayloads.castToMap(value);
    }
    
    protected final String createAuthorizationHeaderValue(final String accessToken) {
        return "Bearer " + accessToken;
    }
    
    protected final URI getEndpointUri() throws IOException {
        return getHttpEndpointUri();
    }
    
    protected final String getProtocolVersion() {
        return MCPHttpTransportTestSupport.PROTOCOL_VERSION;
    }
    
    protected final Map<String, String> createSessionHeaders(final String sessionId) {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("MCP-Session-Id", sessionId);
        result.put("MCP-Protocol-Version", getProtocolVersion());
        return result;
    }
    
    @Override
    protected final RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Override
    protected final Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeDatabases;
    }
    
    @Override
    protected final void prepareRuntimeFixture() throws IOException {
        try {
            runtimeDatabases = H2RuntimeTestSupport.createPreparedProgrammaticRuntimeDatabases(getTempDir(), getTransport().getH2AccessMode());
        } catch (final SQLException ex) {
            throw new IOException("Failed to initialize MCP E2E runtime databases.", ex);
        }
    }
    
    private HttpResponse<String> sendJsonRpcRequest(final HttpClient httpClient, final Map<String, String> headers, final String requestId,
                                                    final String method, final Map<String, Object> params) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, headers, MCPHttpTransportTestSupport.createJsonRpcRequestBody(requestId, method, params));
    }
    
    private void applyHeaders(final HttpRequest.Builder requestBuilder, final Map<String, String> headers) {
        headers.forEach(requestBuilder::setHeader);
    }
}
