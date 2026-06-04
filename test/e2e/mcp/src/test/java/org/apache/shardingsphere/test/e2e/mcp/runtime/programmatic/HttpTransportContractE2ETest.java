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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpTransportContractE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final List<String> OFFICIAL_TOOL_NAMES = List.of(
            "database_gateway_search_metadata", "database_gateway_validate_proxy_connectivity", "database_gateway_execute_query", "database_gateway_execute_update", "database_gateway_apply_workflow",
            "database_gateway_validate_workflow", "database_gateway_plan_encrypt_rule", "database_gateway_plan_mask_rule");
    
    private static final String PLAN_MASK_TOOL_NAME = "database_gateway_plan_mask_rule";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
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
        assertTrue(((List<?>) actualCapabilities.get("resourceNavigation")).stream().map(String::valueOf).anyMatch(each -> each.contains("database_gateway_apply_workflow")));
        Map<String, Object> actualFingerprints = castToMap(actualCapabilities.get("fingerprints"));
        assertFalse(String.valueOf(actualFingerprints.get("descriptorCatalog")).isEmpty());
        assertFalse(String.valueOf(actualFingerprints.get("promptSet")).isEmpty());
        assertFalse(String.valueOf(actualFingerprints.get("resourceNavigation")).isEmpty());
        assertFalse(String.valueOf(actualFingerprints.get("modelFacingSchemas")).isEmpty());
        Map<String, Object> actualProtocolAvailability = castToMap(actualCapabilities.get("protocolAvailability"));
        assertTrue((Boolean) actualProtocolAvailability.get("prompts"));
        assertTrue((Boolean) actualProtocolAvailability.get("completions"));
        assertTrue((Boolean) actualProtocolAvailability.get("resourceNavigation"));
        assertModelFacingPayloadContract(actualCapabilities);
    }
    
    @Test
    void assertExposeSecretFreeRuntimeDiagnostics() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://runtime");
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualRuntimeStatus = getFirstResourcePayload(actual.body());
        assertThat(actualRuntimeStatus.get("status"), is("available"));
        assertThat(actualRuntimeStatus.get("active_transport"), is("http"));
        assertModelFacingPayloadContract(actualRuntimeStatus);
        Map<String, Object> actualDiagnostics = castToMap(actualRuntimeStatus.get("diagnostics"));
        assertThat(actualDiagnostics.get("current_category"), is("ready"));
        assertTrue(((List<?>) actualDiagnostics.get("safe_categories")).contains("invalid_configuration"));
        assertTrue(castToMapList(actualDiagnostics.get("operator_next_actions")).stream().anyMatch(each -> "invalid_configuration".equals(each.get("category"))));
        assertThat(castToMap(actualRuntimeStatus.get("redaction_summary")).get("marker"), is("******"));
        assertRuntimeStatusSecretSafe(actualRuntimeStatus);
    }
    
    @Test
    void assertExposeResourceAndPromptListContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> resourceListResponse = sendResourceListRequest(httpClient, sessionId);
        assertThat(resourceListResponse.statusCode(), is(200));
        Map<String, Object> resourceListPayload = getResultPayload(resourceListResponse);
        assertModelFacingPayloadContract(resourceListPayload);
        List<Map<String, Object>> actualResources = castToMapList(resourceListPayload.get("resources"));
        Map<String, Object> actualCapabilityResource = findByKey(actualResources, "uri", "shardingsphere://capabilities");
        assertThat(castToMap(actualCapabilityResource.get("_meta")).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("capability-catalog"));
        HttpResponse<String> resourceTemplateListResponse = sendResourceTemplateListRequest(httpClient, sessionId);
        assertThat(resourceTemplateListResponse.statusCode(), is(200));
        Map<String, Object> resourceTemplateListPayload = getResultPayload(resourceTemplateListResponse);
        assertModelFacingPayloadContract(resourceTemplateListPayload);
        List<Map<String, Object>> actualResourceTemplates = castToMapList(resourceTemplateListPayload.get("resourceTemplates"));
        Map<String, Object> actualDatabaseDetail = findByKey(actualResourceTemplates, "uriTemplate", "shardingsphere://databases/{database}");
        assertThat(castToMap(actualDatabaseDetail.get("_meta")).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("detail"));
        HttpResponse<String> promptListResponse = sendPromptListRequest(httpClient, sessionId);
        assertThat(promptListResponse.statusCode(), is(200));
        Map<String, Object> promptListPayload = getResultPayload(promptListResponse);
        assertModelFacingPayloadContract(promptListPayload);
        List<Map<String, Object>> actualPrompts = castToMapList(promptListPayload.get("prompts"));
        assertTrue(actualPrompts.stream().anyMatch(each -> "inspect_metadata".equals(each.get("name"))));
    }
    
    @Test
    void assertExposePromptAndCompletionContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> promptResponse = sendPromptGetRequest(httpClient, sessionId, "inspect_metadata",
                Map.of("database", "logic_db", "schema", "logic_db", "query", "orders"));
        assertThat(promptResponse.statusCode(), is(200));
        Map<String, Object> promptPayload = getResultPayload(promptResponse);
        assertTrue(String.valueOf(promptPayload).contains("Stop conditions"));
        assertTrue(String.valueOf(promptPayload).contains(MCPShardingSphereMetadataKeys.STOP_CONDITIONS));
        assertModelFacingPayloadContract(promptPayload);
        HttpResponse<String> completionResponse = sendCompletionRequest(httpClient, sessionId, Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "schema", "pub", Map.of());
        assertThat(completionResponse.statusCode(), is(200));
        Map<String, Object> completionPayload = getResultPayload(completionResponse);
        assertTrue(String.valueOf(completionPayload).contains("missing_context"));
        assertTrue(String.valueOf(completionPayload).contains(MCPShardingSphereMetadataKeys.MISSING_CONTEXT_ARGUMENTS));
        assertModelFacingPayloadContract(completionPayload);
    }
    
    @Test
    void assertExposeModelFacingOutputSchemas() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendRawPostRequest(httpClient, createSessionHeaders(sessionId),
                MCPHttpTransportTestSupport.createJsonRpcRequestBody("tools-list-1", "tools/list", Map.of()));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualResult = getResultPayload(actual);
        assertModelFacingPayloadContract(actualResult);
        Map<String, Object> actualSearchMetadataTool = castToMapList(actualResult.get("tools")).stream()
                .filter(each -> "database_gateway_search_metadata".equals(each.get("name"))).findFirst().orElseThrow(IllegalStateException::new);
        Map<String, Object> actualSearchMetadataOutputProperties = castToMap(castToMap(actualSearchMetadataTool.get("outputSchema")).get("properties"));
        Map<String, Object> actualSearchMetadataItemProperties = castToMap(castToMap(castToMap(actualSearchMetadataOutputProperties.get("items")).get("items")).get("properties"));
        assertThat(String.valueOf(castToMap(actualSearchMetadataItemProperties.get("resource")).get("type")), is("object"));
        assertThat(String.valueOf(castToMap(actualSearchMetadataItemProperties.get("next_resources")).get("type")), is("array"));
        Map<String, Object> actualTool = castToMapList(actualResult.get("tools")).stream()
                .filter(each -> "database_gateway_execute_update".equals(each.get("name"))).findFirst().orElseThrow(IllegalStateException::new);
        Map<String, Object> actualOutputProperties = castToMap(castToMap(actualTool.get("outputSchema")).get("properties"));
        assertThat(String.valueOf(castToMap(actualOutputProperties.get("next_actions")).get("type")), is("array"));
    }
    
    @Test
    void assertExposeWorkflowPayloadContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> planResponse = sendToolCallRequest(httpClient, sessionId, PLAN_MASK_TOOL_NAME,
                createMaskRulePlanArguments());
        assertThat(planResponse.statusCode(), is(200));
        Map<String, Object> planPayload = getStructuredContent(planResponse.body());
        assertThat(String.valueOf(planPayload.get("status")), is("planned"));
        assertThat(String.valueOf(castToMapList(planPayload.get("next_actions")).get(0).get("tool_name")), is(WorkflowToolDescriptors.APPLY_TOOL_NAME));
        assertModelFacingPayloadContract(planPayload);
        String planId = String.valueOf(planPayload.get("plan_id"));
        HttpResponse<String> previewResponse = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME,
                Map.of("plan_id", planId, "execution_mode", "preview"));
        assertThat(previewResponse.statusCode(), is(200));
        Map<String, Object> previewPayload = getStructuredContent(previewResponse.body());
        assertThat(String.valueOf(previewPayload.get("status")), is("preview"));
        assertFalse((Boolean) previewPayload.get("would_apply"));
        assertModelFacingPayloadContract(previewPayload);
        HttpResponse<String> manualApplyResponse = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME,
                Map.of("plan_id", planId, "execution_mode", "manual-only"));
        assertThat(manualApplyResponse.statusCode(), is(200));
        Map<String, Object> manualApplyPayload = getStructuredContent(manualApplyResponse.body());
        assertThat(String.valueOf(manualApplyPayload.get("status")), is("awaiting-manual-execution"));
        assertModelFacingPayloadContract(manualApplyPayload);
        HttpResponse<String> validationResponse = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
        assertThat(validationResponse.statusCode(), is(200));
        Map<String, Object> validationPayload = getStructuredContent(validationResponse.body());
        assertThat(String.valueOf(validationPayload.get("status")), is("failed"));
        assertFalse(String.valueOf(validationPayload.get("recovery_guidance")).isEmpty());
        assertModelFacingPayloadContract(validationPayload);
    }
    
    @Test
    void assertPreserveUtf8ToolArgumentsForWorkflowIntent() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_plan_encrypt_rule",
                Map.of("table", "orders", "column", "status", "natural_language_intent", "encrypt status with reversible encryption, no equality, no like"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> structuredContent = getStructuredContent(actual.body());
        assertThat(String.valueOf(structuredContent.get("status")), is("clarifying"));
        assertThat(castToMapList(structuredContent.get("clarification_questions")).stream().map(each -> String.valueOf(each.get("display_message"))).toList(),
                is(List.of("Please provide logical database first.")));
    }
    
    private HttpResponse<String> sendResourceListRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "resources-list-1", "resources/list", Map.of()));
    }
    
    private HttpResponse<String> sendResourceTemplateListRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "resource-templates-list-1", "resources/templates/list", Map.of()));
    }
    
    private HttpResponse<String> sendPromptListRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "prompts-list-1", "prompts/list", Map.of()));
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
    
    private Map<String, Object> createMaskRulePlanArguments() {
        return Map.of(
                "database", "logic_db",
                "schema", "logic_db",
                "table", "orders",
                "column", "status",
                "operation_type", "create",
                "algorithm_type", "KEEP_FIRST_N_LAST_M",
                "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*"));
    }
    
    private Map<String, Object> getResultPayload(final HttpResponse<String> response) {
        return castToMap(parseJsonBody(response.body()).get("result"));
    }
    
    private void assertModelFacingPayloadContract(final Map<String, Object> payload) {
        MCPModelContractAssertions.assertNoBannedPublicFields(payload);
        MCPModelContractAssertions.assertCanonicalNextActionLists(payload);
    }
    
    private void assertRuntimeStatusSecretSafe(final Map<String, Object> runtimeStatus) {
        String actualPayload = String.valueOf(runtimeStatus);
        assertFalse(actualPayload.contains("Authorization: Bearer"));
        assertFalse(actualPayload.contains("runtime-secret"));
        assertFalse(actualPayload.contains("jdbc:"));
        assertFalse(actualPayload.contains("at org.apache."));
    }
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        return ((List<?>) value).stream().map(this::castToMap).toList();
    }
    
    private Map<String, Object> findByKey(final List<Map<String, Object>> values, final String key, final String expectedValue) {
        return values.stream().filter(each -> expectedValue.equals(each.get(key))).findFirst().orElseThrow();
    }
}
