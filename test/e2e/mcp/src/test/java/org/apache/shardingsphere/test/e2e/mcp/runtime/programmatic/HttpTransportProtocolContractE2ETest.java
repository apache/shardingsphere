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

import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        assertTrue(actualSessionId.chars().allMatch(each -> 0x21 <= each && each <= 0x7E));
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        Map<String, Object> actualResult = MCPInteractionPayloads.getRequiredJsonRpcResult(actualPayload);
        assertThat(String.valueOf(actualPayload.get("jsonrpc")), is("2.0"));
        assertThat(String.valueOf(actualResult.get("protocolVersion")), is(getProtocolVersion()));
        assertServerCapabilities(MCPInteractionPayloads.getRequiredObject(actualResult, "capabilities"));
        assertThat(sendInitializedNotification(httpClient, actualSessionId).statusCode(), is(202));
    }
    
    @Test
    void assertRejectInitializeWithoutAcceptHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(getEndpointUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MCPInteractionProtocolSupport.createJsonRpcRequestBody(
                        "init-1", "initialize", MCPInteractionProtocolSupport.createInitializeRequestParams("mcp-e2e-programmatic"))))
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
                MCPInteractionProtocolSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        assertThat(actual.statusCode(), is(400));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").isPresent());
    }
    
    @Test
    void assertAcceptInitializeWithParameterizedAcceptHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient,
                Map.of("Accept", "application/json; charset=utf-8, text/event-stream; charset=utf-8"),
                MCPInteractionProtocolSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        assertThat(actual.statusCode(), is(200));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is(getProtocolVersion()));
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
    void assertRejectUnsupportedActiveEventStream() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, String> headers = new LinkedHashMap<>(createSessionHeaders(sessionId));
        headers.put("Accept", "text/event-stream");
        HttpResponse<InputStream> actual = openEventStreamInputStream(httpClient, headers);
        try (InputStream ignored = actual.body()) {
            assertThat(actual.statusCode(), is(405));
        }
    }
    
    @Test
    void assertReturnToolErrorForInvalidIntegerArgument() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", createInvalidUpdateArguments());
        Map<String, Object> actualRecovery = assertToolErrorRecovery(actual, "invalid_integer_argument");
        assertThat(actualRecovery.get("argument_path"), is("max_rows"));
        assertThat(actualRecovery.get("minimum_value"), is(0));
        assertThat(actualRecovery.get("maximum_value"), is(5000));
        assertThat(actualRecovery.get("suggested_value"), is(100));
    }
    
    @Test
    void assertReturnToolErrorForSchemaAdditionalProperties() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_plan_readwrite_splitting_rule",
                Map.of("load_balancer_properties", Map.of("weight", 1)));
        Map<String, Object> actualRecovery = assertToolErrorRecovery(actual, "invalid_argument_type");
        assertThat(actualRecovery.get("argument_path"), is("load_balancer_properties.weight"));
        assertThat(actualRecovery.get("expected_type"), is("string"));
    }
    
    @Test
    void assertReturnJsonRpcErrorForUnsupportedTool() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPInteractionProtocolSupport.createJsonRpcRequestBody(
                "missing-tool-1", "tools/call", Map.of("name", "database_gateway_missing_tool", "arguments", Map.of())));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        assertTrue(MCPInteractionPayloads.hasJsonRpcError(actualPayload));
        assertFalse(actualPayload.containsKey("result"));
        Map<String, Object> actualError = MCPInteractionPayloads.getRequiredObject(actualPayload, "error");
        assertTrue(actualError.containsKey("code"));
        assertTrue(actualError.containsKey("message"));
    }
    
    @Test
    void assertExposeToolInputSchemaConstraints() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> actualResult = sendInitializedRequest(httpClient, sessionId, "tools-list-constraints", "tools/list", Map.of());
        Map<String, Object> actualTool = MCPInteractionPayloads.getRequiredObjectList(actualResult, "tools").stream()
                .filter(each -> "database_gateway_execute_update".equals(each.get("name"))).findFirst().orElseThrow();
        Map<String, Object> actualInputSchema = MCPInteractionPayloads.getRequiredObject(actualTool, "inputSchema");
        Map<String, Object> actualProperties = MCPInteractionPayloads.getRequiredObject(actualInputSchema, "properties");
        Map<String, Object> actualMaxRows = MCPInteractionPayloads.getRequiredObject(actualProperties, "max_rows");
        assertThat(actualMaxRows.get("minimum"), is(0));
        assertThat(actualMaxRows.get("maximum"), is(5000));
        assertThat(MCPInteractionPayloads.getRequiredObject(actualProperties, "execution_mode").get("enum"), is(List.of("execute", "preview")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("listMethodCases")
    void assertListMethodCompletesWithoutPaginationCursor(final String method, final String resultKey) throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> actualResult = sendInitializedRequest(httpClient, sessionId, method + "-1", method, Map.of());
        assertFalse(MCPInteractionPayloads.getRequiredObjectList(actualResult, resultKey).isEmpty());
        assertFalse(actualResult.containsKey("nextCursor"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("listMethodCases")
    void assertListMethodAcceptsPaginationCursorParameter(final String method, final String resultKey) throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> actualResult = sendInitializedRequest(httpClient, sessionId, method + "-cursor-1", method, Map.of("cursor", "opaque-cursor"));
        assertFalse(MCPInteractionPayloads.getRequiredObjectList(actualResult, resultKey).isEmpty());
        assertFalse(actualResult.containsKey("nextCursor"));
    }
    
    @Test
    void assertEnforceToolCallLimitPerSession() throws IOException, InterruptedException {
        String propertyName = MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY;
        String originalValue = System.getProperty(propertyName);
        System.setProperty(propertyName, "1");
        try {
            launchHttpTransport();
            HttpClient httpClient = HttpClient.newHttpClient();
            String sessionId = initializeSession(httpClient);
            assertToolErrorRecovery(sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", createInvalidUpdateArguments()), "invalid_integer_argument");
            Map<String, Object> actualRecovery = assertToolErrorRecovery(
                    sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", createInvalidUpdateArguments()), "tool_call_limit_exceeded");
            assertThat(actualRecovery.get("session_id"), is(sessionId));
            assertThat(actualRecovery.get("max_tool_calls_per_session"), is(1));
            String otherSessionId = initializeSession(httpClient);
            assertToolErrorRecovery(sendToolCallRequest(httpClient, otherSessionId, "database_gateway_execute_update", createInvalidUpdateArguments()), "invalid_integer_argument");
        } finally {
            restoreProperty(propertyName, originalValue);
        }
    }
    
    @Test
    void assertAcceptInitializeWithUnsupportedProtocolVersion() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        Map<String, Object> initializeRequestParams = new LinkedHashMap<>(MCPInteractionProtocolSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        initializeRequestParams.put("protocolVersion", "2025-06-18");
        HttpResponse<String> actual = sendInitializeRequest(httpClient, initializeRequestParams);
        assertThat(actual.statusCode(), is(200));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is(getProtocolVersion()));
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredJsonRpcResult(actualPayload).get("protocolVersion")), is(getProtocolVersion()));
    }
    
    @Test
    void assertRejectInitializeWithoutProtocolVersion() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        Map<String, Object> initializeRequestParams = new LinkedHashMap<>(MCPInteractionProtocolSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        initializeRequestParams.remove("protocolVersion");
        HttpResponse<String> actual = sendInitializeRequest(httpClient, initializeRequestParams);
        assertThat(actual.statusCode(), is(400));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").isPresent());
    }
    
    @Test
    void assertRejectFollowUpRequestWithProtocolMismatch() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, Map.of("MCP-Session-Id", sessionId, "MCP-Protocol-Version", "2025-06-18"));
        assertThat(actual.statusCode(), is(400));
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
    
    private static Stream<Arguments> listMethodCases() {
        return Stream.of(
                Arguments.of("tools/list", "tools"),
                Arguments.of("resources/list", "resources"),
                Arguments.of("resources/templates/list", "resourceTemplates"),
                Arguments.of("prompts/list", "prompts"));
    }
    
    private void assertServerCapabilities(final Map<String, Object> capabilities) {
        assertTrue(capabilities.containsKey("completions"));
        assertFalse(capabilities.containsKey("experimental"));
        assertFalse(capabilities.containsKey("tasks"));
        Map<String, Object> resources = MCPInteractionPayloads.getRequiredObject(capabilities, "resources");
        assertFalse((boolean) resources.get("subscribe"));
        assertFalse((boolean) resources.get("listChanged"));
        Map<String, Object> tools = MCPInteractionPayloads.getRequiredObject(capabilities, "tools");
        assertFalse((boolean) tools.get("listChanged"));
        Map<String, Object> prompts = MCPInteractionPayloads.getRequiredObject(capabilities, "prompts");
        assertFalse((boolean) prompts.get("listChanged"));
    }
    
    private Map<String, Object> sendInitializedRequest(final HttpClient httpClient, final String sessionId, final String requestId, final String method,
                                                       final Map<String, Object> params) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPInteractionProtocolSupport.createJsonRpcRequestBody(requestId, method, params));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        return MCPInteractionPayloads.getRequiredJsonRpcResult(actualPayload);
    }
    
    private Map<String, Object> assertToolErrorRecovery(final HttpResponse<String> response, final String expectedCategory) {
        assertThat(response.statusCode(), is(200));
        Map<String, Object> payload = parseJsonBody(response.body());
        Map<String, Object> result = MCPInteractionPayloads.getRequiredJsonRpcResult(payload);
        assertTrue((boolean) result.get("isError"));
        assertFalse(result.containsKey("structuredContent"));
        Map<String, Object> structuredContent = MCPInteractionPayloads.getToolCallPayload(payload);
        assertThat(structuredContent.get("response_mode"), is("recovery"));
        Map<String, Object> actualRecovery = MCPInteractionPayloads.getRequiredObject(structuredContent, "recovery");
        assertThat(actualRecovery.get("category"), is(expectedCategory));
        return actualRecovery;
    }
    
    private Map<String, Object> createInvalidUpdateArguments() {
        return Map.of("database", "logic_db", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "preview", "max_rows", 5001);
    }
    
    private void restoreProperty(final String propertyName, final String originalValue) {
        if (null == originalValue) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, originalValue);
        }
    }
}
