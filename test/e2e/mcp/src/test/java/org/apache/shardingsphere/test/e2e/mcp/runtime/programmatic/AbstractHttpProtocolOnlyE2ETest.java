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

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractHttpProtocolOnlyE2ETest {
    
    private static final String LOOPBACK_BIND_HOST = "127.0.0.1";
    
    private static final String ENDPOINT_PATH = "/gateway";
    
    private static final String CLIENT_NAME = "mcp-e2e-programmatic";
    
    private StreamableHttpMCPServer httpServer;
    
    @AfterEach
    void tearDownHttpServer() {
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }
    }
    
    protected final void launchHttpTransport() throws IOException {
        if (null != httpServer) {
            return;
        }
        httpServer = new StreamableHttpMCPServer(createHttpTransportConfiguration(), createRuntimeContext());
        httpServer.start();
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient);
        assertThat(actual.statusCode(), is(200));
        String result = actual.headers().firstValue("MCP-Session-Id").orElseThrow();
        assertThat(sendInitializedNotification(httpClient, result).statusCode(), is(202));
        return result;
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, MCPInteractionProtocolSupport.createInitializeRequestParams(CLIENT_NAME));
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, Map.of(), initializeRequestParams);
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> headers,
                                                               final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendJsonRpcRequest(httpClient, getEndpointUri(), headers, "init-1", "initialize", initializeRequestParams);
    }
    
    protected final HttpResponse<String> sendInitializedNotification(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId),
                MCPInteractionProtocolSupport.createJsonRpcNotificationBody("notifications/initialized", Map.of()));
    }
    
    protected final HttpResponse<String> sendCapabilitiesRequest(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendJsonRpcRequest(httpClient, getEndpointUri(), headers, "resource-1", "resources/read", Map.of("uri", "shardingsphere://capabilities"));
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendJsonRpcRequest(httpClient, getEndpointUri(), createSessionHeaders(sessionId), toolName + "-1", "tools/call",
                Map.of("name", toolName, "arguments", arguments));
    }
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendDeleteRequest(httpClient, getEndpointUri(), headers);
    }
    
    protected final HttpResponse<String> sendRawPostRequest(final HttpClient httpClient, final Map<String, String> headers,
                                                            final String requestBody) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendRawPostRequest(httpClient, getEndpointUri(), headers, requestBody);
    }
    
    protected final HttpResponse<String> openEventStream(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.openEventStream(httpClient, getEndpointUri(), headers);
    }
    
    protected final HttpResponse<InputStream> openEventStreamInputStream(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.openEventStreamInputStream(httpClient, getEndpointUri(), headers);
    }
    
    protected final Map<String, Object> parseJsonBody(final String responseBody) {
        return MCPInteractionPayloads.parseJsonPayload(responseBody);
    }
    
    protected final URI getEndpointUri() {
        if (null == httpServer) {
            throw new IllegalStateException("HTTP transport is not enabled for current protocol E2E test.");
        }
        return URI.create(String.format("http://%s:%d%s", LOOPBACK_BIND_HOST, httpServer.getLocalPort(), getHttpEndpointPath()));
    }
    
    protected final String getProtocolVersion() {
        return MCPInteractionProtocolSupport.PROTOCOL_VERSION;
    }
    
    protected final Map<String, String> createSessionHeaders(final String sessionId) {
        return MCPHttpTransportTestSupport.createSessionHeaders(sessionId, getProtocolVersion());
    }
    
    protected HttpTransportConfiguration createHttpTransportConfiguration() {
        return new HttpTransportConfiguration(LOOPBACK_BIND_HOST, 0, getHttpEndpointPath());
    }
    
    protected String getHttpEndpointPath() {
        return ENDPOINT_PATH;
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()), "http");
    }
}
