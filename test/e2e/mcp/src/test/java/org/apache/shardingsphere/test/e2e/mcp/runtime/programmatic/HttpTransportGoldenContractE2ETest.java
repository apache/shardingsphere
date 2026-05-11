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

import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPGoldenContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class HttpTransportGoldenContractE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String PLAN_MASK_TOOL_NAME = "plan_mask_rule";
    
    private static final String GOLDEN_RESOURCE_PATH = "golden/model-contract/";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertCapabilitiesGoldenContract() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://capabilities");
        assertThat(actual.statusCode(), is(200));
        MCPGoldenContractAssertions.assertMatchesNormalizedGoldenContract(GOLDEN_RESOURCE_PATH + "capabilities.yaml", createCapabilitiesContract(getFirstResourcePayload(actual.body())));
    }
    
    private Map<String, Object> createCapabilitiesContract(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(13, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("model_first_summary", payload.get("model_first_summary"));
        result.put("model_contract", payload.get("model_contract"));
        result.put("surface_summary", payload.get("surface_summary"));
        result.put("field_naming_contract", payload.get("field_naming_contract"));
        result.put("next_action_contract", payload.get("next_action_contract"));
        result.put("common_flows", payload.get("common_flows"));
        result.put("protocolAvailability", payload.get("protocolAvailability"));
        result.put("fingerprints", payload.get("fingerprints"));
        result.put("resources", summarizeCapabilityResourceIdentities(castToMapList(payload.get("resources"))));
        result.put("resourceTemplates", summarizeCapabilityResourceTemplateIdentities(castToMapList(payload.get("resourceTemplates"))));
        result.put("tools", summarizeCapabilityTools(castToMapList(payload.get("tools"))));
        result.put("prompts", summarizePrompts(castToMapList(payload.get("prompts"))));
        result.put("completionTargets", payload.get("completionTargets"));
        return result;
    }
    
    @Test
    void assertResourceAndTemplateGoldenContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> actual = new LinkedHashMap<>(2, 1F);
        HttpResponse<String> resourceListResponse = sendResourceListRequest(httpClient, sessionId);
        assertThat(resourceListResponse.statusCode(), is(200));
        actual.put("resources", summarizeProtocolResources(castToMapList(getResultPayload(resourceListResponse).get("resources"))));
        HttpResponse<String> resourceTemplatesResponse = sendResourceTemplatesListRequest(httpClient, sessionId);
        assertThat(resourceTemplatesResponse.statusCode(), is(200));
        actual.put("resourceTemplates", summarizeProtocolResourceTemplates(castToMapList(getResultPayload(resourceTemplatesResponse).get("resourceTemplates"))));
        MCPGoldenContractAssertions.assertMatchesNormalizedGoldenContract(GOLDEN_RESOURCE_PATH + "resources-and-templates.yaml", actual);
    }
    
    private HttpResponse<String> sendResourceListRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "resources-list-1", "resources/list", Map.of()));
    }
    
    private HttpResponse<String> sendResourceTemplatesListRequest(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "resource-templates-list-1", "resources/templates/list", Map.of()));
    }
    
    @Test
    void assertToolsPromptsAndCompletionGoldenContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> actual = new LinkedHashMap<>(4, 1F);
        HttpResponse<String> toolListResponse = sendRawPostRequest(httpClient, createSessionHeaders(sessionId),
                MCPHttpTransportTestSupport.createJsonRpcRequestBody("tools-list-1", "tools/list", Map.of()));
        assertThat(toolListResponse.statusCode(), is(200));
        actual.put("tools", summarizeProtocolTools(castToMapList(getResultPayload(toolListResponse).get("tools"))));
        HttpResponse<String> promptListResponse = sendPromptListRequest(httpClient, sessionId);
        assertThat(promptListResponse.statusCode(), is(200));
        actual.put("prompts", summarizePrompts(castToMapList(getResultPayload(promptListResponse).get("prompts"))));
        HttpResponse<String> promptResponse = sendPromptGetRequest(httpClient, sessionId, "inspect_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "orders"));
        assertThat(promptResponse.statusCode(), is(200));
        actual.put("inspectMetadataPrompt", summarizePromptPayload(getResultPayload(promptResponse)));
        HttpResponse<String> completionResponse = sendCompletionRequest(httpClient, sessionId, Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "schema", "pub", Map.of());
        assertThat(completionResponse.statusCode(), is(200));
        actual.put("missingContextCompletion", getResultPayload(completionResponse));
        MCPGoldenContractAssertions.assertMatchesNormalizedGoldenContract(GOLDEN_RESOURCE_PATH + "tools-prompts-completion.yaml", actual);
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
    
    @Test
    void assertRecoveryAndWorkflowGoldenContracts() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> actual = new LinkedHashMap<>(6, 1F);
        HttpResponse<String> missingExecutionModeResponse = sendToolCallRequest(httpClient, sessionId, "execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1"));
        assertThat(missingExecutionModeResponse.statusCode(), is(200));
        actual.put("executeUpdateMissingExecutionMode", summarizeRecoveryPayload(getStructuredContent(missingExecutionModeResponse.body())));
        HttpResponse<String> previewResponse = sendToolCallRequest(httpClient, sessionId, "execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "preview"));
        assertThat(previewResponse.statusCode(), is(200));
        actual.put("executeUpdatePreview", summarizeExecuteUpdatePreview(getStructuredContent(previewResponse.body())));
        HttpResponse<String> planResponse = sendToolCallRequest(httpClient, sessionId, PLAN_MASK_TOOL_NAME, createMaskRulePlanArguments());
        assertThat(planResponse.statusCode(), is(200));
        Map<String, Object> planPayload = getStructuredContent(planResponse.body());
        actual.put("workflowPlan", summarizeWorkflowPlan(planPayload));
        String planId = String.valueOf(planPayload.get("plan_id"));
        HttpResponse<String> workflowPreviewResponse = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME,
                Map.of("plan_id", planId, "execution_mode", "preview"));
        assertThat(workflowPreviewResponse.statusCode(), is(200));
        actual.put("workflowApplyPreview", summarizeWorkflowApplyPreview(getStructuredContent(workflowPreviewResponse.body())));
        HttpResponse<String> workflowManualResponse = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME,
                Map.of("plan_id", planId, "execution_mode", "manual-only"));
        assertThat(workflowManualResponse.statusCode(), is(200));
        actual.put("workflowApplyManualOnly", summarizeWorkflowApplyManualOnly(getStructuredContent(workflowManualResponse.body())));
        HttpResponse<String> validationResponse = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
        assertThat(validationResponse.statusCode(), is(200));
        actual.put("workflowValidate", summarizeWorkflowValidation(getStructuredContent(validationResponse.body())));
        MCPGoldenContractAssertions.assertMatchesNormalizedGoldenContract(GOLDEN_RESOURCE_PATH + "recovery-and-workflow.yaml", actual);
    }
    
    private Map<String, Object> createMaskRulePlanArguments() {
        return Map.of(
                "database", "logic_db",
                "schema", "public",
                "table", "orders",
                "column", "status",
                "operation_type", "create",
                "algorithm_type", "KEEP_FIRST_N_LAST_M",
                "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*"));
    }
    
    private Map<String, Object> getResultPayload(final HttpResponse<String> response) {
        return castToMap(parseJsonBody(response.body()).get("result"));
    }
    
    private List<Map<String, Object>> summarizeCapabilityResourceIdentities(final List<Map<String, Object>> resources) {
        return resources.stream().map(this::summarizeCapabilityResourceIdentity).toList();
    }
    
    private Map<String, Object> summarizeCapabilityResourceIdentity(final Map<String, Object> resource) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uri", resource.get("uri"));
        result.put("name", resource.get("name"));
        result.put("resourceKind", resource.get("resourceKind"));
        result.put("relatedTools", resource.getOrDefault("relatedTools", List.of()));
        return result;
    }
    
    private List<Map<String, Object>> summarizeCapabilityResourceTemplateIdentities(final List<Map<String, Object>> resourceTemplates) {
        return resourceTemplates.stream().map(this::summarizeCapabilityResourceTemplateIdentity).toList();
    }
    
    private Map<String, Object> summarizeCapabilityResourceTemplateIdentity(final Map<String, Object> resourceTemplate) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uriTemplate", resourceTemplate.get("uriTemplate"));
        result.put("name", resourceTemplate.get("name"));
        result.put("resourceKind", resourceTemplate.get("resourceKind"));
        result.put("parameters", summarizeParameters(castToMapList(resourceTemplate.get("parameters"))));
        return result;
    }
    
    private List<Map<String, Object>> summarizeCapabilityTools(final List<Map<String, Object>> tools) {
        return tools.stream().map(this::summarizeCapabilityTool).toList();
    }
    
    private Map<String, Object> summarizeCapabilityTool(final Map<String, Object> tool) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("name", tool.get("name"));
        result.put("annotations", tool.getOrDefault("annotations", Map.of()));
        return result;
    }
    
    private List<Map<String, Object>> summarizeProtocolResources(final List<Map<String, Object>> resources) {
        return resources.stream().sorted(Comparator.comparing(each -> String.valueOf(each.get("uri")))).map(this::summarizeProtocolResource).toList();
    }
    
    private Map<String, Object> summarizeProtocolResource(final Map<String, Object> resource) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uri", resource.get("uri"));
        result.put("name", resource.get("name"));
        result.put("title", resource.get("title"));
        result.put("mimeType", resource.get("mimeType"));
        return result;
    }
    
    private List<Map<String, Object>> summarizeProtocolResourceTemplates(final List<Map<String, Object>> resourceTemplates) {
        return resourceTemplates.stream().sorted(Comparator.comparing(each -> String.valueOf(each.get("uriTemplate")))).map(this::summarizeProtocolResourceTemplate).toList();
    }
    
    private Map<String, Object> summarizeProtocolResourceTemplate(final Map<String, Object> resourceTemplate) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("uriTemplate", resourceTemplate.get("uriTemplate"));
        result.put("name", resourceTemplate.get("name"));
        result.put("title", resourceTemplate.get("title"));
        result.put("mimeType", resourceTemplate.get("mimeType"));
        result.put("parameters", summarizeParameters(castToMapList(resourceTemplate.get("parameters"))));
        return result;
    }
    
    private List<Map<String, Object>> summarizeProtocolTools(final List<Map<String, Object>> tools) {
        return tools.stream().sorted(Comparator.comparing(each -> String.valueOf(each.get("name")))).map(this::summarizeProtocolTool).toList();
    }
    
    private Map<String, Object> summarizeProtocolTool(final Map<String, Object> tool) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        Map<String, Object> inputSchema = castToMap(tool.get("inputSchema"));
        result.put("name", tool.get("name"));
        result.put("title", tool.get("title"));
        result.put("inputProperties", propertyNames(castToMap(inputSchema.get("properties"))));
        result.put("required", inputSchema.getOrDefault("required", List.of()));
        result.put("outputProperties", propertyNames(castToMap(castToMap(tool.get("outputSchema")).get("properties"))));
        return result;
    }
    
    private List<Map<String, Object>> summarizePrompts(final List<Map<String, Object>> prompts) {
        return prompts.stream().map(this::summarizePrompt).toList();
    }
    
    private Map<String, Object> summarizePrompt(final Map<String, Object> prompt) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("name", prompt.get("name"));
        result.put("title", prompt.get("title"));
        result.put("arguments", summarizeParameters(castToMapList(prompt.get("arguments"))));
        return result;
    }
    
    private Map<String, Object> summarizePromptPayload(final Map<String, Object> promptPayload) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("description", promptPayload.get("description"));
        result.put("messages", castToMapList(promptPayload.get("messages")).stream().map(this::summarizePromptMessage).toList());
        return result;
    }
    
    private Map<String, Object> summarizePromptMessage(final Map<String, Object> message) {
        Map<String, Object> content = castToMap(message.get("content"));
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("role", message.get("role"));
        result.put("contentType", content.get("type"));
        result.put("textSha256", createSha256(String.valueOf(content.get("text"))));
        return result;
    }
    
    private String createSha256(final String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable.", ex);
        }
    }
    
    private Map<String, Object> summarizeRecoveryPayload(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("error_code", payload.get("error_code"));
        result.put("message", payload.get("message"));
        result.put("recovery", summarizeRecovery(castToMap(payload.get("recovery"))));
        result.put("next_actions", summarizeNextActions(castToMapList(payload.get("next_actions"))));
        return result;
    }
    
    private Map<String, Object> summarizeRecovery(final Map<String, Object> recovery) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("category", recovery.get("category"));
        result.put("source_tool", recovery.get("source_tool"));
        result.put("tool_name", recovery.get("tool_name"));
        result.put("suggested_arguments", recovery.get("suggested_arguments"));
        result.put("next_actions", summarizeNextActions(castToMapList(recovery.get("next_actions"))));
        return result;
    }
    
    private Map<String, Object> summarizeExecuteUpdatePreview(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("result_kind", payload.get("result_kind"));
        result.put("would_execute", payload.get("would_execute"));
        result.put("classification", payload.get("classification"));
        result.put("preview", payload.get("preview"));
        result.put("next_actions", summarizeNextActions(castToMapList(payload.get("next_actions"))));
        return result;
    }
    
    private Map<String, Object> summarizeWorkflowPlan(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("plan_id", payload.get("plan_id"));
        result.put("workflow_kind", payload.get("workflow_kind"));
        result.put("status", payload.get("status"));
        result.put("current_step", payload.get("current_step"));
        result.put("global_steps", payload.get("global_steps"));
        result.put("algorithm_recommendations", payload.get("algorithm_recommendations"));
        result.put("property_requirements", payload.get("property_requirements"));
        result.put("review_focus", payload.get("review_focus"));
        result.put("next_actions", summarizeNextActions(castToMapList(payload.get("next_actions"))));
        return result;
    }
    
    private Map<String, Object> summarizeWorkflowApplyPreview(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("plan_id", payload.get("plan_id"));
        result.put("status", payload.get("status"));
        result.put("would_apply", payload.get("would_apply"));
        result.put("execution_mode", payload.get("execution_mode"));
        result.put("execution_preview", payload.get("execution_preview"));
        result.put("requires_user_approval", payload.get("requires_user_approval"));
        result.put("next_actions", summarizeNextActions(castToMapList(payload.get("next_actions"))));
        return result;
    }
    
    private Map<String, Object> summarizeWorkflowApplyManualOnly(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("plan_id", payload.get("plan_id"));
        result.put("status", payload.get("status"));
        result.put("execution_mode", payload.get("execution_mode"));
        result.put("manual_artifact_summary", payload.get("manual_artifact_summary"));
        result.put("manual_artifact_package", payload.get("manual_artifact_package"));
        result.put("requires_user_approval", payload.get("requires_user_approval"));
        result.put("next_actions", summarizeNextActions(castToMapList(payload.get("next_actions"))));
        return result;
    }
    
    private Map<String, Object> summarizeWorkflowValidation(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(9, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("plan_id", payload.get("plan_id"));
        result.put("status", payload.get("status"));
        result.put("overall_status", payload.get("overall_status"));
        result.put("sections", payload.get("sections"));
        result.put("mismatches", payload.get("mismatches"));
        result.put("recovery_guidance", payload.get("recovery_guidance"));
        result.put("requires_user_approval", payload.get("requires_user_approval"));
        result.put("next_actions", summarizeNextActions(castToMapList(payload.get("next_actions"))));
        return result;
    }
    
    private List<Map<String, Object>> summarizeNextActions(final List<Map<String, Object>> nextActions) {
        return nextActions.stream().map(this::summarizeNextAction).toList();
    }
    
    private Map<String, Object> summarizeNextAction(final Map<String, Object> nextAction) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("order", nextAction.get("order"));
        result.put("type", nextAction.get("type"));
        result.put("title", nextAction.get("title"));
        result.put("tool_name", nextAction.get("tool_name"));
        result.put("resource_uri", nextAction.get("resource_uri"));
        result.put("arguments", nextAction.get("arguments"));
        result.put("requires_user_approval", nextAction.get("requires_user_approval"));
        result.put("question", nextAction.get("question"));
        result.put("required_inputs", nextAction.get("required_inputs"));
        result.put("reason", nextAction.get("reason"));
        return result.entrySet().stream().filter(entry -> null != entry.getValue()).collect(
                LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
    }
    
    private List<Map<String, Object>> summarizeParameters(final List<Map<String, Object>> params) {
        return params.stream().map(this::summarizeParameter).toList();
    }
    
    private Map<String, Object> summarizeParameter(final Map<String, Object> parameter) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("name", parameter.get("name"));
        result.put("required", parameter.get("required"));
        return result;
    }
    
    private List<String> propertyNames(final Map<String, Object> properties) {
        return new LinkedList<>(properties.keySet());
    }
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        if (null == value) {
            return List.of();
        }
        return ((List<?>) value).stream().map(this::castToMap).toList();
    }
}
