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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamableHttpMCPServerIT {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    private static final String ACCEPT_HEADER = "Accept";
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String ACCEPTED_CONTENT_TYPES = "application/json, text/event-stream";
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    private StreamableHttpMCPServer server;
    
    private URI endpoint;
    
    @AfterEach
    void stopServer() {
        if (null != server) {
            server.stop();
        }
    }
    
    @Test
    void assertInitializeSessionLifecycle() throws IOException, InterruptedException {
        startServer();
        HttpResponse<String> initializeResponse = sendPost(createInitializePayload(), createInitializeHeaders());
        assertThat(initializeResponse.statusCode(), is(200));
        assertTrue(initializeResponse.headers().firstValue(CONTENT_TYPE_HEADER).orElse("").startsWith(JSON_CONTENT_TYPE));
        assertThat(initializeResponse.headers().firstValue(PROTOCOL_HEADER).orElse(""), is(MCPTransportConstants.PROTOCOL_VERSION));
        String sessionId = initializeResponse.headers().firstValue(SESSION_HEADER).orElseThrow();
        assertFalse(sessionId.isEmpty());
        Map<?, ?> initializePayload = parseBody(initializeResponse);
        assertThat(initializePayload.get("jsonrpc"), is("2.0"));
        Map<?, ?> result = (Map<?, ?>) initializePayload.get("result");
        assertThat(result.get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(sendInitializedNotification(sessionId, Collections.emptyMap()).statusCode(), is(202));
        HttpResponse<String> capabilitiesResponse = sendPost(createCapabilitiesPayload(), Map.of(
                CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE,
                ACCEPT_HEADER, ACCEPTED_CONTENT_TYPES,
                "mcp-session-id", sessionId,
                "mcp-protocol-version", MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(capabilitiesResponse.statusCode(), is(200));
    }
    
    @Test
    void assertRejectUnsupportedContentType() throws IOException, InterruptedException {
        startServer();
        HttpResponse<String> actual = sendPost(createInitializePayload(), Map.of(CONTENT_TYPE_HEADER, "text/plain", ACCEPT_HEADER, ACCEPTED_CONTENT_TYPES));
        assertThat(actual.statusCode(), is(415));
    }
    
    @Test
    void assertRejectMissingAcceptHeader() throws IOException, InterruptedException {
        startServer();
        HttpResponse<String> actual = sendPost(createInitializePayload(), Map.of(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE));
        assertThat(actual.statusCode(), is(400));
        assertFalse(actual.headers().firstValue(SESSION_HEADER).isPresent());
    }
    
    @Test
    void assertRejectProtocolMismatch() throws IOException, InterruptedException {
        startServer();
        String sessionId = initializeSession(Collections.emptyMap());
        Map<String, String> headers = createSessionHeaders(sessionId);
        headers.put(PROTOCOL_HEADER, "2025-06-18");
        HttpResponse<String> actual = sendPost(createCapabilitiesPayload(), headers);
        assertThat(actual.statusCode(), is(400));
    }
    
    @Test
    void assertRejectMissingSession() throws IOException, InterruptedException {
        startServer();
        HttpResponse<String> actual = sendCapabilitiesRequest("missing-session");
        assertThat(actual.statusCode(), is(404));
        assertThat(parseBody(actual).get("message"), is("Session not found: missing-session"));
    }
    
    @Test
    void assertDeleteKeepsOtherSession() throws IOException, InterruptedException {
        startServer();
        String deletedSessionId = initializeSession(Collections.emptyMap());
        String activeSessionId = initializeSession(Collections.emptyMap());
        assertThat(sendDelete(deletedSessionId).statusCode(), is(200));
        assertThat(sendCapabilitiesRequest(deletedSessionId).statusCode(), is(404));
        assertThat(sendCapabilitiesRequest(activeSessionId).statusCode(), is(200));
    }
    
    @Test
    void assertRejectRemoteOriginWithCategory() throws IOException, InterruptedException {
        startServer();
        Map<String, String> headers = createInitializeHeaders();
        headers.put("Origin", "http://example.com");
        HttpResponse<String> actual = sendPost(createInitializePayload(), headers);
        assertThat(actual.statusCode(), is(403));
        assertFalse(parseBody(actual).containsKey("id"));
        assertThat(getRecoveryCategory(actual), is("origin_not_allowed"));
    }
    
    @Test
    void assertRejectChangedSessionAttribution() throws IOException, InterruptedException {
        String subjectHeader = "X-Test-Subject";
        String sourceHeader = "X-Test-Source";
        String attributeHeaderPrefix = "X-Test-Attr-";
        startServer(new HttpTransportConfiguration("127.0.0.1", 0, "/mcp",
                new SessionAttributionSourceConfiguration(subjectHeader, sourceHeader, attributeHeaderPrefix)));
        Map<String, String> attributionHeaders = Map.of(subjectHeader, "subject", sourceHeader, "gateway", attributeHeaderPrefix + "Region", "ap-south");
        String sessionId = initializeSession(attributionHeaders);
        Map<String, String> changedHeaders = new LinkedHashMap<>(attributionHeaders);
        changedHeaders.put(subjectHeader, "other");
        HttpResponse<String> actual = sendPost(createCapabilitiesPayload(), createSessionHeaders(sessionId, changedHeaders));
        assertThat(actual.statusCode(), is(400));
        assertThat(getRecoveryCategory(actual), is("session_attribution_mismatch"));
    }
    
    private void startServer() throws IOException {
        startServer(new HttpTransportConfiguration("127.0.0.1", 0, "/mcp"));
    }
    
    private void startServer(final HttpTransportConfiguration configuration) throws IOException {
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()),
                MCPTransportType.HTTP);
        server = new StreamableHttpMCPServer(configuration, runtimeContext);
        server.start();
        endpoint = URI.create("http://127.0.0.1:" + server.getLocalPort() + configuration.getEndpointPath());
    }
    
    private String initializeSession(final Map<String, String> attributionHeaders) throws IOException, InterruptedException {
        Map<String, String> headers = createInitializeHeaders();
        headers.putAll(attributionHeaders);
        HttpResponse<String> actual = sendPost(createInitializePayload(), headers);
        assertThat(actual.statusCode(), is(200));
        String result = actual.headers().firstValue(SESSION_HEADER).orElseThrow();
        assertThat(sendInitializedNotification(result, attributionHeaders).statusCode(), is(202));
        return result;
    }
    
    private HttpResponse<String> sendInitializedNotification(final String sessionId, final Map<String, String> attributionHeaders) throws IOException, InterruptedException {
        return sendPost(Map.of("jsonrpc", "2.0", "method", "notifications/initialized", "params", Map.of()), createSessionHeaders(sessionId, attributionHeaders));
    }
    
    private HttpResponse<String> sendCapabilitiesRequest(final String sessionId) throws IOException, InterruptedException {
        return sendPost(createCapabilitiesPayload(), createSessionHeaders(sessionId));
    }
    
    private HttpResponse<String> sendDelete(final String sessionId) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpoint).DELETE();
        createSessionHeaders(sessionId).forEach(requestBuilder::header);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpResponse<String> sendPost(final Map<String, Object> payload, final Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpoint).POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload)));
        headers.forEach(requestBuilder::header);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    private Map<String, String> createInitializeHeaders() {
        return new LinkedHashMap<>(Map.of(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE, ACCEPT_HEADER, ACCEPTED_CONTENT_TYPES));
    }
    
    private Map<String, String> createSessionHeaders(final String sessionId) {
        return new LinkedHashMap<>(Map.of(
                CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE,
                ACCEPT_HEADER, ACCEPTED_CONTENT_TYPES,
                SESSION_HEADER, sessionId,
                PROTOCOL_HEADER, MCPTransportConstants.PROTOCOL_VERSION));
    }
    
    private Map<String, String> createSessionHeaders(final String sessionId, final Map<String, String> attributionHeaders) {
        Map<String, String> result = createSessionHeaders(sessionId);
        result.putAll(attributionHeaders);
        return result;
    }
    
    private Map<String, Object> createInitializePayload() {
        return Map.of(
                "jsonrpc", "2.0",
                "id", "init-1",
                "method", "initialize",
                "params", Map.of(
                        "protocolVersion", MCPTransportConstants.PROTOCOL_VERSION,
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "mcp-http-it", "version", "test")));
    }
    
    private Map<String, Object> createCapabilitiesPayload() {
        return Map.of(
                "jsonrpc", "2.0",
                "id", "resource-1",
                "method", "resources/read",
                "params", Map.of("uri", "shardingsphere://capabilities"));
    }
    
    private Map<?, ?> parseBody(final HttpResponse<String> response) throws IOException {
        return OBJECT_MAPPER.readValue(response.body(), Map.class);
    }
    
    private String getRecoveryCategory(final HttpResponse<String> response) throws IOException {
        Map<?, ?> error = (Map<?, ?>) parseBody(response).get("error");
        Map<?, ?> data = (Map<?, ?>) error.get("data");
        return String.valueOf(((Map<?, ?>) data.get("recovery")).get("category"));
    }
}
