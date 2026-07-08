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
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpProductionProxyFeatureWorkflowContractE2ETest extends AbstractProductionProxyWorkflowE2ETest {
    
    private static final String APPLY_TOOL_NAME = WorkflowToolDescriptors.APPLY_TOOL_NAME;
    
    private static final String VALIDATE_TOOL_NAME = WorkflowToolDescriptors.VALIDATE_TOOL_NAME;
    
    private static final String BROADCAST_PLAN_TOOL_NAME = "database_gateway_plan_broadcast_rule";
    
    private static final String READWRITE_SPLITTING_PLAN_TOOL_NAME = "database_gateway_plan_readwrite_splitting_rule";
    
    private static final String SHADOW_PLAN_TOOL_NAME = "database_gateway_plan_shadow_rule";
    
    private static final String SHARDING_PLAN_TOOL_NAME = "database_gateway_plan_sharding_table_rule";
    
    private static final String BROADCAST_RULES_RESOURCE_URI = "shardingsphere://features/broadcast/databases/%s/rules";
    
    private static final String READWRITE_SPLITTING_RULES_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/databases/%s/rules";
    
    private static final String SHADOW_RULES_RESOURCE_URI = "shardingsphere://features/shadow/databases/%s/rules";
    
    private static final String SHARDING_TABLE_RULES_RESOURCE_URI = "shardingsphere://features/sharding/databases/%s/table-rules";
    
    private static final List<String> FORBIDDEN_ARTIFACT_TOKENS = List.of(
            "create table", "alter table", "drop table", "create index", "drop index", "migrate", "migration", "backfill", "data probe", "physical metadata",
            "register storage unit", "alter storage unit", "unregister storage unit");
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("featureWorkflowScenarios")
    void assertPlanManualApplyAndValidateFeatureWorkflowThroughProxy(final String scenarioName, final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertThat(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList(), hasItem(scenario.toolName()));
            Map<String, Object> actualPlanResponse = interactionClient.call(scenario.toolName(), createPlanArguments(scenario));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(actualPlanResponse.get("current_step")), is("review"));
            assertTrue(String.valueOf(getMapList(actualPlanResponse.get("distsql_artifacts"))).contains(scenario.expectedDistSQLToken()));
            assertNoForbiddenArtifacts(actualPlanResponse);
            assertModelFacingPayloadContract(actualPlanResponse);
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            Map<String, Object> actualManualApplyResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "manual-only"));
            assertThat(String.valueOf(actualManualApplyResponse.get("status")), is("awaiting-manual-execution"));
            assertThat(getStringList(actualManualApplyResponse.get("executed_distsql")).size(), is(0));
            assertNoForbiddenArtifacts(actualManualApplyResponse);
            assertModelFacingPayloadContract(actualManualApplyResponse);
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertThat(String.valueOf(actualValidationResponse.get("status")), is("failed"));
            assertThat(String.valueOf(actualValidationResponse.get("overall_status")), is("failed"));
            assertFalse(getMapList(actualValidationResponse.get("issues")).isEmpty());
            assertModelFacingPayloadContract(actualValidationResponse);
        }
    }
    
    @Test
    void assertBroadcastWorkflowCanBeAppliedAndValidatedThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> arguments = Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "tables", "orders");
            Map<String, Object> actualPlanResponse = interactionClient.call(BROADCAST_PLAN_TOOL_NAME, arguments);
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = interactionClient.call(APPLY_TOOL_NAME,
                    createReviewThenExecuteArguments(planId, getApprovedSteps(previewWorkflow(interactionClient, planId))));
            assertApplyCompleted(actualApplyResponse);
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
            List<Map<String, Object>> actualRules = getPayloadItems(interactionClient.readResource(String.format(BROADCAST_RULES_RESOURCE_URI, getLogicalDatabaseName())));
            assertThat(actualRules.stream().map(each -> String.valueOf(each.get("broadcast_table"))).toList(), hasItem("orders"));
            assertDuplicateCreateFails(interactionClient, BROADCAST_PLAN_TOOL_NAME, arguments);
        }
    }
    
    @Test
    void assertReadwriteSplittingWorkflowCanBeAppliedAndValidatedThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertStorageUnitsExposeWorkflowTopology(interactionClient);
            planApplyAndValidateWorkflow(interactionClient, READWRITE_SPLITTING_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "rule", "readwrite_ds", "write_storage_unit", "ds_0",
                            "read_storage_units", "ds_1", "transactional_read_query_strategy", "DYNAMIC", "load_balancer_type", "ROUND_ROBIN"));
            List<Map<String, Object>> actualRules = getPayloadItems(interactionClient.readResource(
                    String.format(READWRITE_SPLITTING_RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualRule = findItemByField(actualRules, "name", "readwrite_ds");
            assertThat(String.valueOf(actualRule.get("write_storage_unit_name")), is("ds_0"));
            assertTrue(String.valueOf(actualRule.get("read_storage_unit_names")).contains("ds_1"));
            assertThat(String.valueOf(actualRule.get("transactional_read_query_strategy")).toUpperCase(Locale.ENGLISH), is("DYNAMIC"));
        }
    }
    
    @Test
    void assertShadowWorkflowCanBeAppliedAndValidatedThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            planApplyAndValidateWorkflow(interactionClient, SHADOW_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "rule", "shadow_rule", "source_storage_unit", "ds_0",
                            "shadow_storage_unit", "ds_shadow", "table", "orders", "algorithm_type", "VALUE_MATCH",
                            "algorithm_properties", Map.of("operation", "insert", "column", "order_id", "value", "1")));
            List<Map<String, Object>> actualRules = getPayloadItems(interactionClient.readResource(String.format(SHADOW_RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualRule = findItemByField(actualRules, "rule_name", "shadow_rule");
            assertThat(String.valueOf(actualRule.get("source_name")), is("ds_0"));
            assertThat(String.valueOf(actualRule.get("shadow_name")), is("ds_shadow"));
            assertThat(String.valueOf(actualRule.get("shadow_table")), is("orders"));
            assertThat(String.valueOf(actualRule.get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("VALUE_MATCH"));
        }
    }
    
    @Test
    void assertShardingWorkflowCanBeAppliedAndValidatedThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            planApplyAndValidateWorkflow(interactionClient, SHARDING_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "table", "orders", "column", "order_id",
                            "data_nodes", "ds_0.orders", "strategy_type", "standard", "algorithm_type", "INLINE",
                            "algorithm_properties", Map.of("algorithm-expression", "orders")));
            List<Map<String, Object>> actualRules = getPayloadItems(interactionClient.readResource(
                    String.format(SHARDING_TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName())));
            assertThat(actualRules.stream().map(each -> String.valueOf(each.get("table"))).toList(), hasItem("orders"));
        }
    }
    
    private static Stream<Arguments> featureWorkflowScenarios() {
        return Stream.of(
                Arguments.of("broadcast", new FeatureWorkflowScenario(BROADCAST_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "tables", "orders"), "CREATE BROADCAST TABLE RULE")),
                Arguments.of("readwrite-splitting", new FeatureWorkflowScenario(READWRITE_SPLITTING_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "rule", "readwrite_ds", "write_storage_unit", "ds_0",
                                "read_storage_units", "ds_1", "transactional_read_query_strategy", "DYNAMIC"),
                        "CREATE READWRITE_SPLITTING RULE")),
                Arguments.of("shadow", new FeatureWorkflowScenario(SHADOW_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "rule", "shadow_rule", "source_storage_unit", "ds_0",
                                "shadow_storage_unit", "ds_shadow", "table", "orders", "algorithm_type", "VALUE_MATCH",
                                "algorithm_properties", Map.of("operation", "insert", "column", "order_id", "value", "1")),
                        "CREATE SHADOW RULE")),
                Arguments.of("sharding", new FeatureWorkflowScenario(SHARDING_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "table", "orders", "column", "order_id",
                                "data_nodes", "ds_0.orders", "strategy_type", "standard", "algorithm_type", "INLINE",
                                "algorithm_properties", Map.of("algorithm-expression", "orders")),
                        "CREATE SHARDING TABLE RULE")));
    }
    
    private Map<String, Object> createPlanArguments(final FeatureWorkflowScenario scenario) {
        Map<String, Object> result = new LinkedHashMap<>(scenario.planArguments());
        result.put("database", getLogicalDatabaseName());
        return result;
    }
    
    private void assertNoForbiddenArtifacts(final Map<String, Object> payload) {
        String actualPayload = String.valueOf(payload).toLowerCase(Locale.ENGLISH);
        FORBIDDEN_ARTIFACT_TOKENS.forEach(each -> assertFalse(actualPayload.contains(each)));
    }
    
    private void assertStorageUnitsExposeWorkflowTopology(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        List<Map<String, Object>> actualStorageUnits = getPayloadItems(interactionClient.readResource(
                String.format("shardingsphere://databases/%s/storage-units", getLogicalDatabaseName())));
        assertThat(actualStorageUnits.stream().map(each -> String.valueOf(each.get("name"))).toList(), hasItems("ds_0", "ds_1", "ds_shadow"));
        List<Map<String, Object>> actualStorageUnitDetail = getPayloadItems(interactionClient.readResource(
                String.format("shardingsphere://databases/%s/storage-units/ds_0", getLogicalDatabaseName())));
        assertThat(actualStorageUnitDetail.size(), is(1));
        assertThat(String.valueOf(actualStorageUnitDetail.getFirst().get("name")), is("ds_0"));
    }
    
    private void planApplyAndValidateWorkflow(final MCPInteractionClient interactionClient, final String toolName,
                                              final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> actualPlanResponse = interactionClient.call(toolName, arguments);
        assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
        assertNoForbiddenArtifacts(actualPlanResponse);
        assertModelFacingPayloadContract(actualPlanResponse);
        String planId = String.valueOf(actualPlanResponse.get("plan_id"));
        Map<String, Object> actualApplyResponse = applyReviewedWorkflow(interactionClient, planId);
        assertApplyCompleted(actualApplyResponse);
        assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
        assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
        assertDuplicateCreateFails(interactionClient, toolName, arguments);
    }
    
    private void assertDuplicateCreateFails(final MCPInteractionClient interactionClient, final String toolName,
                                            final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call(toolName, arguments);
        assertThat(String.valueOf(actual.get("status")), is("failed"));
        assertFalse(getMapList(actual.get("issues")).isEmpty());
        assertModelFacingPayloadContract(actual);
    }
    
    private record FeatureWorkflowScenario(String toolName, Map<String, Object> planArguments, String expectedDistSQLToken) {
    }
}
