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

package org.apache.shardingsphere.test.e2e.mcp.functionality;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
class HttpProxyMaskWorkflowE2ETest extends AbstractHttpProxyWorkflowE2ETest {
    
    private static final String PLAN_TOOL_NAME = "database_gateway_plan_mask_rule";
    
    private static final String PLAN_PROMPT_NAME = "plan_mask_rule";
    
    private static final String VALIDATE_TOOL_NAME = WorkflowToolDescriptors.VALIDATE_TOOL_NAME;
    
    private static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/mask/algorithms";
    
    private static final String RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/%s/rules";
    
    private static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/%s/tables/%s/rules";
    
    @Test
    void assertCompleteMaskAlgorithm() throws IOException, InterruptedException {
        useSharedReadOnlyRuntimeFixture();
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.complete(Map.of("type", "ref/prompt", "name", PLAN_PROMPT_NAME), "algorithm_type", "KEEP", Map.of());
            assertThat(getStringListOrEmpty(getObjectOrEmpty(actual.get("completion")).get("values")), hasItem("KEEP_FIRST_N_LAST_M"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("allTransportCases")
    void assertElicitMaskPlanning(final String name, final RuntimeTransport transport) throws IOException {
        useTransport(transport);
        List<McpSchema.ElicitRequest> actualElicitationRequests = new CopyOnWriteArrayList<>();
        try (McpSyncClient client = createElicitationClient(transport, actualElicitationRequests)) {
            client.initialize();
            McpSchema.CallToolResult actual = client.callTool(new McpSchema.CallToolRequest(PLAN_TOOL_NAME, Map.of(
                    "database", getLogicalDatabaseName(),
                    "schema", getLogicalDatabaseName(),
                    "table", "orders",
                    "column", "status",
                    "operation_type", "create",
                    "algorithm_type", "MASK_FROM_X_TO_Y")));
            Map<String, Object> actualPayload = MCPInteractionPayloads.getRequiredObjectValue(actual.structuredContent(), "structuredContent");
            if (RuntimeTransport.HTTP == transport) {
                assertThat(String.valueOf(actualPayload.get("status")), is("clarifying"));
                assertThat(String.valueOf(actualPayload.get("fallback_reason")), is("remote_identity_required"));
                assertTrue(actualElicitationRequests.isEmpty());
                return;
            }
            assertThat(String.valueOf(actualPayload.get("status")), is("planned"));
            assertThat(String.valueOf(actualPayload.get("current_step")), is("review"));
            Map<String, Object> maskedPropertyPreview = MCPInteractionPayloads.getRequiredObject(actualPayload, "masked_property_preview");
            Map<String, Object> primaryProperties = MCPInteractionPayloads.getRequiredObject(maskedPropertyPreview, "primary");
            assertThat(String.valueOf(primaryProperties.get("from-x")), is("1"));
            assertThat(String.valueOf(primaryProperties.get("to-y")), is("3"));
            assertElicitationRequest(actualElicitationRequests);
        }
    }
    
    private McpSyncClient createElicitationClient(final RuntimeTransport transport, final List<McpSchema.ElicitRequest> elicitationRequests) throws IOException {
        return MCPClientTransportFactory.createElicitationClient(createClientTransport(transport), elicitationRequests, this::createElicitationResult);
    }
    
    private McpClientTransport createClientTransport(final RuntimeTransport transport) throws IOException {
        return RuntimeTransport.HTTP == transport
                ? MCPClientTransportFactory.createHttpClientTransport(getHttpEndpointUri())
                : MCPClientTransportFactory.createStdioClientTransport(getConfigFile());
    }
    
    private McpSchema.ElicitResult createElicitationResult(final List<McpSchema.ElicitRequest> elicitationRequests,
                                                           final McpSchema.ElicitRequest request) {
        elicitationRequests.add(request);
        List<String> requiredFields = getRequiredStringList(request.requestedSchema().get("required"));
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of(
                requiredFields.getFirst(), "1",
                requiredFields.get(1), "3"));
    }
    
    private List<String> getRequiredStringList(final Object value) {
        return ((List<?>) value).stream().map(String::valueOf).toList();
    }
    
    private void assertElicitationRequest(final List<McpSchema.ElicitRequest> actualRequests) {
        assertThat(actualRequests.size(), is(1));
        McpSchema.ElicitRequest actual = actualRequests.getFirst();
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.TOOL), is(PLAN_TOOL_NAME));
        assertFalse(String.valueOf(actual.meta().get(MCPShardingSphereMetadataKeys.PLAN_ID)).isBlank());
        Map<String, Object> actualRequestedSchema = actual.requestedSchema();
        assertThat(actualRequestedSchema.get("type"), is("object"));
        assertFalse((Boolean) actualRequestedSchema.get("additionalProperties"));
        Map<String, Object> actualProperties = MCPInteractionPayloads.getRequiredObject(actualRequestedSchema, "properties");
        assertTrue(actualProperties.containsKey("field_1"));
        assertTrue(actualProperties.containsKey("field_2"));
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredObject(actualProperties, "field_1").get("description")), is("Please provide property `from-x`."));
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredObject(actualProperties, "field_2").get("description")), is("Please provide property `to-y`."));
        assertFalse(actualProperties.keySet().stream().map(String::valueOf).anyMatch(each -> each.contains("secret") || each.contains("password") || each.contains("token")));
        assertThat(getRequiredStringList(actualRequestedSchema.get("required")), hasItems("field_1", "field_2"));
    }
    
    @Test
    void assertPlanApplyAndValidateMaskWorkflow() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
            assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getObjectListOrEmpty(actualCreatePlanResponse.get("distsql_artifacts")).getFirst().get("sql")), containsString("CREATE MASK RULE `orders`"));
            String createPlanId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, createPlanId));
            Map<String, Object> actualCreateValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", createPlanId));
            assertValidationPassed(actualCreateValidationResponse);
            assertThat(
                    String.valueOf(getObjectListOrEmpty(getValidationSection(actualCreateValidationResponse, "rule").get("evidence")).getFirst().get("algorithm_type"))
                            .toUpperCase(Locale.ENGLISH),
                    is("KEEP_FIRST_N_LAST_M"));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateMaskDropWorkflow() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createMaskRule(interactionClient);
            Map<String, Object> actualDropPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status", "operation_type", "drop"));
            assertThat(String.valueOf(actualDropPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getObjectListOrEmpty(actualDropPlanResponse.get("distsql_artifacts")).getFirst().get("sql")), is("DROP MASK RULE `orders`"));
            String planId = String.valueOf(actualDropPlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getValidationSection(actualValidationResponse, "rule").get("details")), is("Mask table rule state matches the planned state."));
        }
    }
    
    @Test
    void assertPlanRejectsSecondMaskColumn() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createMaskRule(interactionClient);
            Map<String, Object> actualSecondCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "#")));
            assertThat(String.valueOf(actualSecondCreatePlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualSecondCreatePlanResponse), hasItem(WorkflowIssueCode.MASK_RULE_REWRITE_LIMITED));
            assertThat(getObjectListOrEmpty(actualSecondCreatePlanResponse.get("distsql_artifacts")).size(), is(0));
        }
    }
    
    @Test
    void assertPlanRecommendsApplyAndValidateMaskWorkflowFromNaturalLanguage() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualClarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "mask status as phone number, keep first 3 and last 4"));
            assertThat(String.valueOf(actualClarifyingResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualClarifyingResponse), hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            List<Map<String, Object>> actualRecommendations = getObjectListOrEmpty(actualClarifyingResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(1));
            assertThat(String.valueOf(actualRecommendations.getFirst().get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MASK_FROM_X_TO_Y"));
            assertThat(getClarificationMessages(actualClarifyingResponse), is(List.of("Please provide property `from-x`.", "Please provide property `to-y`.")));
            String planId = String.valueOf(actualClarifyingResponse.get("plan_id"));
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("plan_id", planId, "primary_algorithm_properties", Map.of("from-x", "4", "to-y", "7")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(
                    String.valueOf(getObjectListOrEmpty(getValidationSection(actualValidationResponse, "rule").get("evidence")).getFirst().get("algorithm_type"))
                            .toUpperCase(Locale.ENGLISH),
                    is("MASK_FROM_X_TO_Y"));
        }
    }
    
    @Test
    void assertPlanApplyValidateAndReadMaskResourcesWithCustomAlgorithm() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "create", "algorithm_type", "MCP_MASK_CUSTOM"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
            List<Map<String, Object>> actualMaskPluginItems = getPayloadItems(interactionClient.readResource(ALGORITHMS_RESOURCE_URI));
            assertThat(String.valueOf(findItemByField(actualMaskPluginItems, "type", "MCP_MASK_CUSTOM").get("type")), is("MCP_MASK_CUSTOM"));
            List<Map<String, Object>> actualMaskRules = getPayloadItems(
                    interactionClient.readResource(String.format(RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualMaskRule = findItemByField(actualMaskRules, "column", "status");
            assertThat(String.valueOf(actualMaskRule.get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MCP_MASK_CUSTOM"));
            List<Map<String, Object>> actualSingleRuleItems = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualSingleRuleItems.size(), is(1));
            assertThat(String.valueOf(actualSingleRuleItems.getFirst().get("column")), is("status"));
        }
    }
    
    private void createMaskRule(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                        "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                        "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
        assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
        String planId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
        assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
        assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
    }
    
}
