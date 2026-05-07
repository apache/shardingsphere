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
            "search_metadata", "execute_query", "execute_update", "apply_workflow", "validate_workflow", "plan_encrypt_rule", "plan_mask_rule");
    
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
        Map<String, Object> actualCapabilities = getFirstResourcePayload(actual.body());
        assertThat(((List<?>) actualCapabilities.get("supportedTools")).stream().map(String::valueOf).toList(), containsInAnyOrder(OFFICIAL_TOOL_NAMES.toArray()));
        assertTrue(((List<?>) actualCapabilities.get("prompts")).stream().map(String::valueOf).anyMatch(each -> each.contains("inspect_metadata")));
        assertTrue(((List<?>) actualCapabilities.get("completionTargets")).stream().map(String::valueOf).anyMatch(each -> each.contains("inspect_metadata")));
        assertTrue(((List<?>) actualCapabilities.get("resourceNavigation")).stream().map(String::valueOf).anyMatch(each -> each.contains("apply_workflow")));
        Map<String, Object> actualFingerprints = castToMap(actualCapabilities.get("fingerprints"));
        assertFalse(String.valueOf(actualFingerprints.get("descriptorCatalog")).isEmpty());
        assertFalse(String.valueOf(actualFingerprints.get("promptSet")).isEmpty());
        assertFalse(String.valueOf(actualFingerprints.get("resourceNavigation")).isEmpty());
        assertFalse(String.valueOf(actualFingerprints.get("modelFacingSchemas")).isEmpty());
        Map<String, Object> actualProtocolAvailability = castToMap(actualCapabilities.get("protocolAvailability"));
        assertTrue((Boolean) actualProtocolAvailability.get("prompts"));
        assertTrue((Boolean) actualProtocolAvailability.get("completions"));
        assertTrue((Boolean) actualProtocolAvailability.get("resourceNavigation"));
    }
    
    @Test
    void assertExposePromptAndCompletionContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> promptResponse = sendPromptGetRequest(httpClient, sessionId, "inspect_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "orders"));
        assertThat(promptResponse.statusCode(), is(200));
        Map<String, Object> promptPayload = castToMap(parseJsonBody(promptResponse.body()).get("result"));
        assertTrue(String.valueOf(promptPayload).contains("Stop conditions"));
        assertTrue(String.valueOf(promptPayload).contains("stopConditions"));
        HttpResponse<String> completionResponse = sendCompletionRequest(httpClient, sessionId, Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "schema", "pub", Map.of());
        assertThat(completionResponse.statusCode(), is(200));
        Map<String, Object> completionPayload = castToMap(parseJsonBody(completionResponse.body()).get("result"));
        assertTrue(String.valueOf(completionPayload).contains("missing_context"));
        assertTrue(String.valueOf(completionPayload).contains("missingContextArguments"));
    }
    
    @Test
    void assertExposeModelFacingOutputSchemas() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendRawPostRequest(httpClient, createSessionHeaders(sessionId),
                MCPHttpTransportTestSupport.createJsonRpcRequestBody("tools-list-1", "tools/list", Map.of()));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualResult = castToMap(parseJsonBody(actual.body()).get("result"));
        Map<String, Object> actualSearchMetadataTool = castToMapList(actualResult.get("tools")).stream()
                .filter(each -> "search_metadata".equals(each.get("name"))).findFirst().orElseThrow(IllegalStateException::new);
        Map<String, Object> actualSearchMetadataOutputProperties = castToMap(castToMap(actualSearchMetadataTool.get("outputSchema")).get("properties"));
        Map<String, Object> actualSearchMetadataItemProperties = castToMap(castToMap(castToMap(actualSearchMetadataOutputProperties.get("items")).get("items")).get("properties"));
        assertThat(String.valueOf(castToMap(actualSearchMetadataItemProperties.get("resource")).get("type")), is("object"));
        assertThat(String.valueOf(castToMap(actualSearchMetadataItemProperties.get("next_resources")).get("type")), is("array"));
        Map<String, Object> actualTool = castToMapList(actualResult.get("tools")).stream()
                .filter(each -> "execute_update".equals(each.get("name"))).findFirst().orElseThrow(IllegalStateException::new);
        Map<String, Object> actualOutputProperties = castToMap(castToMap(actualTool.get("outputSchema")).get("properties"));
        assertThat(String.valueOf(castToMap(actualOutputProperties.get("next_actions")).get("type")), is("array"));
    }
    
    @Test
    void assertRejectExecuteUpdateWithoutExecutionMode() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> structuredContent = getStructuredContent(actual.body());
        assertThat(String.valueOf(structuredContent.get("error_code")), is("invalid_request"));
        Map<String, Object> recovery = castToMap(structuredContent.get("recovery"));
        assertThat(recovery.get("category"), is("missing_execution_mode"));
        assertThat(recovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        Map<String, Object> retryAction = castToMapList(recovery.get("next_actions")).iterator().next();
        assertThat(String.valueOf(retryAction.get("action_kind")), is("retry_tool"));
        assertThat(castToMap(retryAction.get("required_arguments")), is(Map.of("execution_mode", "preview")));
        assertTrue((Boolean) retryAction.get("requires_user_approval"));
    }
    
    @Test
    void assertPreviewExecuteUpdateNextActions() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "preview"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> structuredContent = getStructuredContent(actual.body());
        assertThat(String.valueOf(structuredContent.get("result_kind")), is("preview"));
        assertFalse((Boolean) structuredContent.get("would_execute"));
        List<Map<String, Object>> nextActions = castToMapList(structuredContent.get("next_actions"));
        assertThat(nextActions.stream().map(each -> String.valueOf(each.get("action_kind"))).toList(), is(List.of("ask_user", "call_tool")));
        Map<String, Object> callToolAction = nextActions.get(1);
        assertThat(String.valueOf(callToolAction.get("target_tool")), is("execute_update"));
        assertThat(String.valueOf(castToMap(callToolAction.get("required_arguments")).get("execution_mode")), is("execute"));
        assertTrue((Boolean) callToolAction.get("requires_user_approval"));
    }
    
    @Test
    void assertPreserveUtf8ToolArgumentsForWorkflowIntent() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "plan_encrypt_rule",
                Map.of("table", "orders", "column", "status", "natural_language_intent", "encrypt status with reversible encryption, no equality, no like"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> structuredContent = getStructuredContent(actual.body());
        assertThat(String.valueOf(structuredContent.get("status")), is("clarifying"));
        assertThat(castToMapList(structuredContent.get("clarification_questions")).stream().map(each -> String.valueOf(each.get("display_message"))).toList(),
                is(List.of("Please provide logical database first.")));
    }
    
    private HttpResponse<String> sendPromptGetRequest(final HttpClient httpClient, final String sessionId, final String promptName,
                                                      final Map<String, Object> arguments) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "prompt-1", "prompts/get", Map.of("name", promptName, "arguments", arguments)));
    }
    
    private HttpResponse<String> sendCompletionRequest(final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                                       final String argumentName, final String argumentValue, final Map<String, String> contextArguments) throws IOException, InterruptedException {
        Map<String, Object> params = new LinkedHashMap<>(3, 1F);
        params.put("ref", reference);
        params.put("argument", Map.of("name", argumentName, "value", argumentValue));
        if (!contextArguments.isEmpty()) {
            params.put("context", Map.of("arguments", contextArguments));
        }
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "completion-1", "completion/complete", params));
    }
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        return ((List<?>) value).stream().map(this::castToMap).toList();
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
