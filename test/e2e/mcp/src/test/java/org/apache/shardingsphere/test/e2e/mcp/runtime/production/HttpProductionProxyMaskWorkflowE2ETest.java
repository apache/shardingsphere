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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class HttpProductionProxyMaskWorkflowE2ETest extends AbstractProductionProxyWorkflowE2ETest {
    
    private static final String PLAN_TOOL_NAME = "database_gateway_plan_mask_rule";
    
    private static final String PLAN_PROMPT_NAME = "plan_mask_rule";
    
    private static final String APPLY_TOOL_NAME = WorkflowToolDescriptors.APPLY_TOOL_NAME;
    
    private static final String VALIDATE_TOOL_NAME = WorkflowToolDescriptors.VALIDATE_TOOL_NAME;
    
    private static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/mask/algorithms";
    
    private static final String RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/%s/rules";
    
    private static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/%s/tables/%s/rules";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertCompleteMaskAlgorithmThroughProxy() throws IOException, InterruptedException {
        useSharedReadOnlyRuntimeFixture();
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.complete(Map.of("type", "ref/prompt", "name", PLAN_PROMPT_NAME), "algorithm_type", "KEEP", Map.of());
            assertThat(getStringList(getMap(actual.get("completion")).get("values")), hasItem("KEEP_FIRST_N_LAST_M"));
        }
    }
    
    @Test
    void assertPlanApplyValidateAndRejectMaskAlterWorkflowThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
            assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualCreatePlanResponse.get("distsql_artifacts")).getFirst().get("sql")), containsString("CREATE MASK RULE orders"));
            String createPlanId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, createPlanId));
            Map<String, Object> actualCreateValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", createPlanId));
            assertValidationPassed(actualCreateValidationResponse);
            assertThat(String.valueOf(getMapList(getMap(actualCreateValidationResponse.get("rule_validation")).get("evidence")).getFirst().get("algorithm_type")).toUpperCase(Locale.ENGLISH),
                    is("KEEP_FIRST_N_LAST_M"));
            Map<String, Object> actualAlterPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "alter", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "2", "last-m", "2", "replace-char", "#")));
            assertThat(String.valueOf(actualAlterPlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualAlterPlanResponse), hasItem(WorkflowIssueCode.MASK_ALTER_SCOPE_LIMITED));
            assertThat(getMapList(actualAlterPlanResponse.get("distsql_artifacts")).size(), is(0));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateMaskDropWorkflowThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createMaskRule(interactionClient);
            Map<String, Object> actualDropPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status", "operation_type", "drop"));
            assertThat(String.valueOf(actualDropPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualDropPlanResponse.get("distsql_artifacts")).getFirst().get("sql")), is("DROP MASK RULE orders"));
            String planId = String.valueOf(actualDropPlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getMap(actualValidationResponse.get("rule_validation")).get("details")), is("Mask table rule state matches the planned state."));
        }
    }
    
    @Test
    void assertPlanRejectsSecondMaskColumnThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createMaskRule(interactionClient);
            Map<String, Object> actualSecondCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "#")));
            assertThat(String.valueOf(actualSecondCreatePlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualSecondCreatePlanResponse), hasItem(WorkflowIssueCode.MASK_ALTER_SCOPE_LIMITED));
            assertThat(getMapList(actualSecondCreatePlanResponse.get("distsql_artifacts")).size(), is(0));
        }
    }
    
    @Test
    void assertPlanRecommendApplyAndValidateMaskWorkflowFromNaturalLanguageThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualClarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "mask status as phone number, keep first 3 and last 4"));
            assertThat(String.valueOf(actualClarifyingResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualClarifyingResponse), hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            List<Map<String, Object>> actualRecommendations = getMapList(actualClarifyingResponse.get("algorithm_recommendations"));
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
            assertThat(String.valueOf(getMapList(getMap(actualValidationResponse.get("rule_validation")).get("evidence")).getFirst().get("algorithm_type")).toUpperCase(Locale.ENGLISH),
                    is("MASK_FROM_X_TO_Y"));
        }
    }
    
    @Test
    void assertApplySupportsApprovedStepsThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            Map<String, Object> actualPreviewResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "preview"));
            assertThat(String.valueOf(actualPreviewResponse.get("status")), is("preview"));
            assertThat(getMapList(actualPreviewResponse.get("preview_artifacts")).stream().map(each -> String.valueOf(each.get("approval_step"))).distinct().toList(),
                    is(List.of("rule_distsql")));
            Map<String, Object> actualRuleApplyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, List.of("rule_distsql")));
            assertThat(String.valueOf(actualRuleApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualRuleApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(getStringList(actualRuleApplyResponse.get("skipped_artifacts")).size(), is(0));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
        }
    }
    
    @Test
    void assertPlanApplyValidateAndReadMaskResourcesWithCustomAlgorithmThroughProxy() throws IOException, InterruptedException {
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
    
    private Map<String, Object> applyReviewedWorkflow(final MCPInteractionClient interactionClient, final String planId) throws IOException, InterruptedException {
        Map<String, Object> previewResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "preview"));
        assertThat(String.valueOf(previewResponse.get("status")), is("preview"));
        List<String> approvedSteps = getMapList(previewResponse.get("preview_artifacts")).stream().map(each -> String.valueOf(each.get("approval_step"))).distinct().toList();
        return interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, approvedSteps));
    }
    
    private Map<String, Object> createApplyArguments(final String planId, final List<String> approvedSteps) {
        return Map.of("plan_id", planId, "execution_mode", "review-then-execute", "approved_steps", approvedSteps);
    }
    
    private Map<String, Object> findItemByField(final List<Map<String, Object>> items, final String fieldName, final String expectedValue) {
        return items.stream().filter(each -> expectedValue.equalsIgnoreCase(String.valueOf(each.get(fieldName)))).findFirst()
                .orElseThrow(() -> new AssertionError(String.format("Failed to find item by %s=%s in %s", fieldName, expectedValue, items)));
    }
}
