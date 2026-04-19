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

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
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
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, MCPHttpTransportTestSupport.createInitializeRequestParams(CLIENT_NAME));
    }
    
    private HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        HttpRequest request = MCPHttpTransportTestSupport.createJsonRequestBuilder(getEndpointUri())
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody("init-1", "initialize", initializeRequestParams)))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        toolName + "-1", "tools/call", Map.of("name", toolName, "arguments", arguments))))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final String sessionId,
                                                                 final String resourceUri) throws IOException, InterruptedException {
        HttpRequest request = createJsonRequestBuilder(sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        "resource-1", "resources/read", Map.of("uri", resourceUri))))
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
    
    private HttpRequest.Builder createJsonRequestBuilder(final String sessionId) throws IOException {
        return MCPHttpTransportTestSupport.createSessionRequestBuilder(getEndpointUri(), sessionId, getProtocolVersion());
    }
    
    protected final URI getEndpointUri() throws IOException {
        return getHttpEndpointUri();
    }
    
    protected final String getProtocolVersion() {
        return MCPHttpTransportTestSupport.PROTOCOL_VERSION;
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
            runtimeDatabases = H2RuntimeTestSupport.createPreparedProgrammaticRuntimeDatabases(getTempDir(), getTransport());
        } catch (final SQLException ex) {
            throw new IOException("Failed to initialize MCP E2E runtime databases.", ex);
        }
    }
}
