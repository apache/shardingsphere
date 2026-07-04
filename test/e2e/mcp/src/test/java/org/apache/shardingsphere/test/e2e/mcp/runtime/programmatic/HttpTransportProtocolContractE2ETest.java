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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTransportProtocolContractE2ETest extends AbstractHttpProtocolOnlyE2ETest {
    
    @Test
    void assertInitializeSessionAndProtocolHeaders() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient);
        assertThat(actual.statusCode(), is(200));
        assertTrue(actual.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is(getProtocolVersion()));
        String actualSessionId = actual.headers().firstValue("MCP-Session-Id").orElse("");
        assertFalse(actualSessionId.isEmpty());
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        Map<String, Object> actualResult = castToMap(actualPayload.get("result"));
        assertThat(String.valueOf(actualPayload.get("jsonrpc")), is("2.0"));
        assertThat(String.valueOf(actualResult.get("protocolVersion")), is(getProtocolVersion()));
        assertThat(sendInitializedNotification(httpClient, actualSessionId).statusCode(), is(202));
    }
    
    @Test
    void assertRejectInitializeWithoutAcceptHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(getEndpointUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        "init-1", "initialize", MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-programmatic"))))
                .build();
        HttpResponse<String> actual = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(400));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").isPresent());
    }
    
    @Test
    void assertRejectInitializeWithUnsupportedAcceptHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Accept", "application/json"),
                MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        assertThat(actual.statusCode(), is(400));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").isPresent());
    }
    
    @Test
    void assertRejectEventStreamWithoutAcceptHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = openEventStream(httpClient, createSessionHeaders(sessionId));
        assertThat(actual.statusCode(), is(400));
    }
    
    @Test
    void assertAcceptInitializeWithUnsupportedProtocolVersion() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        Map<String, Object> initializeRequestParams = new LinkedHashMap<>(MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        initializeRequestParams.put("protocolVersion", "2024-11-05");
        HttpResponse<String> actual = sendInitializeRequest(httpClient, initializeRequestParams);
        assertThat(actual.statusCode(), is(200));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is(getProtocolVersion()));
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        assertThat(String.valueOf(castToMap(actualPayload.get("result")).get("protocolVersion")), is(getProtocolVersion()));
    }
    
    @Test
    void assertRejectFollowUpRequestWithProtocolMismatch() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, Map.of("MCP-Session-Id", sessionId, "MCP-Protocol-Version", "2024-11-05"));
        assertThat(actual.statusCode(), is(400));
    }
    
    @Test
    void assertRejectFollowUpRequestWithMissingSession() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        initializeSession(httpClient);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient,
                Map.of("MCP-Session-Id", "missing-session", "MCP-Protocol-Version", getProtocolVersion()));
        assertThat(actual.statusCode(), is(404));
        assertThat(String.valueOf(parseJsonBody(actual.body()).get("message")), is("Session not found: missing-session"));
    }
    
    @Test
    void assertRejectDeleteWithoutSessionHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendDeleteRequest(httpClient, Map.of("MCP-Protocol-Version", getProtocolVersion()));
        assertThat(actual.statusCode(), is(400));
        assertThat(String.valueOf(parseJsonBody(actual.body()).get("message")), is("Session ID required in mcp-session-id header"));
    }
    
    @Test
    void assertCloseSessionOnDelete() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, String> sessionHeaders = Map.of("MCP-Session-Id", sessionId, "MCP-Protocol-Version", getProtocolVersion());
        HttpResponse<String> deleteResponse = sendDeleteRequest(httpClient, sessionHeaders);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, sessionHeaders);
        assertThat(deleteResponse.statusCode(), is(200));
        assertThat(actual.statusCode(), is(404));
        assertThat(String.valueOf(parseJsonBody(actual.body()).get("message")), is("Session not found: " + sessionId));
    }
}
