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

import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpTransportContractE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final List<String> OFFICIAL_TOOL_NAMES = List.of(
            "search_metadata", "execute_query", "apply_workflow", "validate_workflow", "plan_encrypt_rule", "plan_mask_rule");
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
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
    }
    
    @Test
    void assertAcceptInitializeWithoutAcceptHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(getEndpointUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        "init-1", "initialize", MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-programmatic"))))
                .build();
        HttpResponse<String> actual = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        assertTrue(actual.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
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
    void assertAcceptInitializeWithoutProtocolVersion() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        Map<String, Object> initializeRequestParams = new LinkedHashMap<>(MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        initializeRequestParams.remove("protocolVersion");
        HttpResponse<String> actual = sendInitializeRequest(httpClient, initializeRequestParams);
        assertThat(actual.statusCode(), is(200));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is(getProtocolVersion()));
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        assertThat(String.valueOf(castToMap(actualPayload.get("result")).get("protocolVersion")), is(getProtocolVersion()));
    }
    
    @Test
    void assertAcceptFollowUpRequestWithLowercaseSessionHeaders() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = MCPHttpTransportTestSupport.createJsonRequestBuilder(getEndpointUri())
                .header("mcp-session-id", sessionId)
                .header("mcp-protocol-version", getProtocolVersion())
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        "resource-1", "resources/read", Map.of("uri", "shardingsphere://capabilities"))))
                .build();
        HttpResponse<String> actual = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        assertThat(((List<?>) getFirstResourcePayload(actual.body()).get("supportedTools")).stream().map(String::valueOf).toList(),
                containsInAnyOrder(OFFICIAL_TOOL_NAMES.toArray()));
    }
    
    @Test
    void assertPreserveUtf8ToolArgumentsForChineseWorkflowIntent() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "plan_encrypt_rule",
                Map.of("table", "orders", "column", "status", "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> structuredContent = getStructuredContent(actual.body());
        assertThat(String.valueOf(structuredContent.get("status")), is("clarifying"));
        assertThat(((List<?>) structuredContent.get("pending_questions")).stream().map(String::valueOf).toList(), is(List.of("请先提供 logical database。")));
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutProtocolHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, Map.of("MCP-Session-Id", sessionId));
        assertThat(actual.statusCode(), is(400));
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
