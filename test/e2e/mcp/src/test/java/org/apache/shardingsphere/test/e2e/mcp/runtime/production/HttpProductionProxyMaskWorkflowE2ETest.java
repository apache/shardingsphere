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

import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

class HttpProductionProxyMaskWorkflowE2ETest extends AbstractProductionProxyWorkflowE2ETest {
    
    private static final String PLAN_TOOL_NAME = "plan_mask_rule";
    
    private static final String APPLY_TOOL_NAME = "apply_mask_rule";
    
    private static final String VALIDATE_TOOL_NAME = "validate_mask_rule";
    
    private static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/mask/algorithms";
    
    private static final String RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/%s/rules";
    
    private static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/%s/tables/%s/rules";
    
    @Test
    void assertPlanApplyAndValidateMaskCreateAlterWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
            assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualCreatePlanResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("CREATE MASK RULE orders"));
            String createPlanId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", createPlanId)).get("status")), is("completed"));
            Map<String, Object> actualCreateValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", createPlanId));
            assertValidationPassed(actualCreateValidationResponse);
            assertThat(String.valueOf(getMap(getMap(actualCreateValidationResponse.get("rule_validation")).get("evidence")).get("algorithm_type")).toUpperCase(Locale.ENGLISH),
                    is("KEEP_FIRST_N_LAST_M"));
            Map<String, Object> actualAlterPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "alter", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "2", "last-m", "2", "replace-char", "#")));
            assertThat(String.valueOf(actualAlterPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualAlterPlanResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("ALTER MASK RULE orders"));
            String alterPlanId = String.valueOf(actualAlterPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", alterPlanId)).get("status")), is("completed"));
            Map<String, Object> actualAlterValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", alterPlanId));
            assertValidationPassed(actualAlterValidationResponse);
            assertThat(String.valueOf(getMap(getMap(actualAlterValidationResponse.get("rule_validation")).get("evidence")).get("algorithm_type")).toUpperCase(Locale.ENGLISH),
                    is("KEEP_FIRST_N_LAST_M"));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateMaskDropWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createMaskRule(interactionClient);
            Map<String, Object> actualDropPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status", "operation_type", "drop"));
            assertThat(String.valueOf(actualDropPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualDropPlanResponse.get("distsql_artifacts")).get(0).get("sql")), is("DROP MASK RULE orders"));
            String planId = String.valueOf(actualDropPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId)).get("status")), is("completed"));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getMap(actualValidationResponse.get("rule_validation")).get("details")), is("Mask rule has been removed."));
        }
    }
    
    @Test
    void assertPlanKeepsSiblingMaskRulesWhenDroppingOneColumnThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createMaskRule(interactionClient);
            Map<String, Object> actualSecondCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount",
                            "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                            "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "#")));
            assertThat(String.valueOf(actualSecondCreatePlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualSecondCreatePlanResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("ALTER MASK RULE orders"));
            String secondCreatePlanId = String.valueOf(actualSecondCreatePlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", secondCreatePlanId)).get("status")), is("completed"));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", secondCreatePlanId)));
            List<Map<String, Object>> actualMaskRules = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualMaskRules.size(), is(2));
            Map<String, Object> actualDropPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount", "operation_type", "drop"));
            assertThat(String.valueOf(actualDropPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualDropPlanResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("ALTER MASK RULE orders"));
            String dropPlanId = String.valueOf(actualDropPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", dropPlanId)).get("status")), is("completed"));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", dropPlanId)));
            List<Map<String, Object>> actualRemainingMaskRules = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualRemainingMaskRules.size(), is(1));
            assertThat(String.valueOf(actualRemainingMaskRules.get(0).get("column")), is("status"));
        }
    }
    
    @Test
    void assertPlanRecommendApplyAndValidateMaskWorkflowFromNaturalLanguageThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualClarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "把 status 当作手机号做脱敏，保留前3后4"));
            assertThat(String.valueOf(actualClarifyingResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualClarifyingResponse), hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            List<Map<String, Object>> actualRecommendations = getMapList(actualClarifyingResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(1));
            assertThat(String.valueOf(actualRecommendations.get(0).get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MASK_FROM_X_TO_Y"));
            assertThat(getStringList(actualClarifyingResponse.get("pending_questions")), is(List.of("请提供属性 `from-x`。", "请提供属性 `to-y`。")));
            String planId = String.valueOf(actualClarifyingResponse.get("plan_id"));
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("plan_id", planId, "primary_algorithm_properties", Map.of("from-x", "4", "to-y", "7")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId)).get("status")), is("completed"));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getMap(getMap(actualValidationResponse.get("rule_validation")).get("evidence")).get("algorithm_type")).toUpperCase(Locale.ENGLISH),
                    is("MASK_FROM_X_TO_Y"));
        }
    }
    
    @Test
    void assertPlanApplyValidateAndReadMaskResourcesWithCustomSpiAlgorithmThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "operation_type", "create", "algorithm_type", "MCP_MASK_CUSTOM"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId)).get("status")), is("completed"));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
            List<Map<String, Object>> actualMaskPluginItems = getPayloadItems(interactionClient.readResource(ALGORITHMS_RESOURCE_URI));
            Map<String, Object> actualMaskPlugin = findItemByField(actualMaskPluginItems, "type", "MCP_MASK_CUSTOM");
            assertThat(String.valueOf(actualMaskPlugin.get("source")), is("custom-spi"));
            List<Map<String, Object>> actualMaskRules = getPayloadItems(
                    interactionClient.readResource(String.format(RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualMaskRule = findItemByField(actualMaskRules, "column", "status");
            assertThat(String.valueOf(actualMaskRule.get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MCP_MASK_CUSTOM"));
            List<Map<String, Object>> actualSingleRuleItems = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualSingleRuleItems.size(), is(1));
            assertThat(String.valueOf(actualSingleRuleItems.get(0).get("column")), is("status"));
        }
    }
    
    private void createMaskRule(final MCPInteractionClient interactionClient) throws Exception {
        Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                        "operation_type", "create", "algorithm_type", "KEEP_FIRST_N_LAST_M",
                        "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
        assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
        String planId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
        assertThat(String.valueOf(interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId)).get("status")), is("completed"));
        assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
    }
    
    private Map<String, Object> findItemByField(final List<Map<String, Object>> items, final String fieldName, final String expectedValue) {
        return items.stream().filter(each -> expectedValue.equalsIgnoreCase(String.valueOf(each.get(fieldName)))).findFirst()
                .orElseThrow(() -> new AssertionError(String.format("Failed to find item by %s=%s in %s", fieldName, expectedValue, items)));
    }
}
