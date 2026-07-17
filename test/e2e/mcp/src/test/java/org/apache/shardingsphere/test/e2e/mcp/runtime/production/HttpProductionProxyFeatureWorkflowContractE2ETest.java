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
    
    private static final String READWRITE_SPLITTING_STATUS_PLAN_TOOL_NAME = "database_gateway_plan_readwrite_splitting_status";
    
    private static final String SHADOW_PLAN_TOOL_NAME = "database_gateway_plan_shadow_rule";
    
    private static final String DEFAULT_SHADOW_ALGORITHM_PLAN_TOOL_NAME = "database_gateway_plan_default_shadow_algorithm";
    
    private static final String SHADOW_ALGORITHM_CLEANUP_PLAN_TOOL_NAME = "database_gateway_plan_shadow_algorithm_cleanup";
    
    private static final String SHARDING_PLAN_TOOL_NAME = "database_gateway_plan_sharding_table_rule";
    
    private static final String SHARDING_TABLE_REFERENCE_PLAN_TOOL_NAME = "database_gateway_plan_sharding_table_reference_rule";
    
    private static final String SHARDING_DEFAULT_STRATEGY_PLAN_TOOL_NAME = "database_gateway_plan_sharding_default_strategy";
    
    private static final String SHARDING_KEY_GENERATOR_PLAN_TOOL_NAME = "database_gateway_plan_sharding_key_generator";
    
    private static final String SHARDING_KEY_GENERATE_STRATEGY_PLAN_TOOL_NAME = "database_gateway_plan_sharding_key_generate_strategy";
    
    private static final String SHARDING_COMPONENT_CLEANUP_PLAN_TOOL_NAME = "database_gateway_plan_sharding_rule_component_cleanup";
    
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
            Map<String, Object> actualPlanResponse = planWorkflow(interactionClient, scenario.toolName(), createPlanArguments(scenario));
            assertThat(String.valueOf(actualPlanResponse.get("current_step")), is("review"));
            assertTrue(String.valueOf(getObjectListOrEmpty(actualPlanResponse.get("distsql_artifacts"))).contains(scenario.expectedDistSQLToken()));
            List<String> actualResourceUris = getObjectListOrEmpty(actualPlanResponse.get("resources_to_read")).stream()
                    .map(each -> String.valueOf(each.get("uri"))).toList();
            List<String> expectedResourceUris = scenario.resourceUriTemplates().stream()
                    .map(each -> String.format(each, getLogicalDatabaseName())).toList();
            assertTrue(actualResourceUris.containsAll(expectedResourceUris));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            Map<String, Object> actualManualApplyResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "manual-only"));
            assertThat(String.valueOf(actualManualApplyResponse.get("status")), is("awaiting-manual-execution"));
            assertThat(getStringListOrEmpty(actualManualApplyResponse.get("executed_distsql")).size(), is(0));
            assertNoForbiddenArtifacts(actualManualApplyResponse);
            assertModelFacingPayloadContract(actualManualApplyResponse);
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertThat(String.valueOf(actualValidationResponse.get("status")), is("failed"));
            assertThat(String.valueOf(actualValidationResponse.get("overall_status")), is("failed"));
            assertFalse(getObjectListOrEmpty(actualValidationResponse.get("issues")).isEmpty());
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
            assertThat(getStringListOrEmpty(actualApplyResponse.get("executed_distsql")).size(), is(1));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
            List<Map<String, Object>> actualRules = getPayloadItems(interactionClient.readResource(String.format(BROADCAST_RULES_RESOURCE_URI, getLogicalDatabaseName())));
            assertThat(actualRules.stream().map(each -> String.valueOf(each.get("broadcast_table"))).toList(), hasItem("orders"));
            assertDuplicateCreateFails(interactionClient, BROADCAST_PLAN_TOOL_NAME, arguments);
            applyAndValidateWorkflow(interactionClient, BROADCAST_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "tables", "orders"));
            assertFalse(getPayloadItems(interactionClient.readResource(String.format(BROADCAST_RULES_RESOURCE_URI, getLogicalDatabaseName())))
                    .stream().anyMatch(each -> "orders".equalsIgnoreCase(String.valueOf(each.get("broadcast_table")))));
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
            assertThat(String.valueOf(actualRule.get("load_balancer_type")).toUpperCase(Locale.ENGLISH), is("ROUND_ROBIN"));
            Map<String, Object> actualStatusPlan = planWorkflow(interactionClient, READWRITE_SPLITTING_STATUS_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "rule", "readwrite_ds", "storage_unit", "ds_1", "target_status", "disable"));
            Map<String, Object> actualStatusApply = applyReviewedWorkflow(interactionClient, String.valueOf(actualStatusPlan.get("plan_id")));
            assertThat(String.valueOf(actualStatusApply.get("status")), is("failed"));
            assertThat(getIssueCodes(actualStatusApply), hasItem(WorkflowIssueCode.RULE_EXECUTION_FAILED));
            assertTrue(String.valueOf(actualStatusApply.get("issues")).contains("Mode must be 'cluster'"));
            assertThat(getStringListOrEmpty(actualStatusApply.get("executed_distsql")).size(), is(0));
            assertModelFacingPayloadContract(actualStatusApply);
        }
    }
    
    @Test
    void assertShadowWorkflowCanBeAppliedAndValidatedThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            applyAndValidateWorkflow(interactionClient, DEFAULT_SHADOW_ALGORITHM_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "algorithm_type", "SQL_HINT"));
            assertFalse(getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://features/shadow/databases/%s/default-algorithm", getLogicalDatabaseName()))).isEmpty());
            applyAndValidateWorkflow(interactionClient, DEFAULT_SHADOW_ALGORITHM_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "alter", "algorithm_type", "SQL_HINT"));
            applyAndValidateWorkflow(interactionClient, DEFAULT_SHADOW_ALGORITHM_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop"));
            assertTrue(getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://features/shadow/databases/%s/default-algorithm", getLogicalDatabaseName()))).isEmpty());
            applyAndValidateWorkflow(interactionClient, SHADOW_ALGORITHM_CLEANUP_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "algorithm_name", "fixture_unused_algorithm"));
            assertFalse(getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://features/shadow/databases/%s/algorithms", getLogicalDatabaseName())))
                    .stream().anyMatch(each -> "fixture_unused_algorithm".equalsIgnoreCase(String.valueOf(each.get("shadow_algorithm_name")))));
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
            applyAndValidateWorkflow(interactionClient, SHARDING_KEY_GENERATOR_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "key_generator", "snowflake_generator",
                            "key_generator_type", "SNOWFLAKE", "key_generator_properties", Map.of("worker-id", "1")));
            Map<String, Object> orderRuleArguments = new LinkedHashMap<>(createShardingTableRuleArguments("orders"));
            orderRuleArguments.put("key_generate_column", "order_id");
            orderRuleArguments.put("key_generator_type", "SNOWFLAKE");
            orderRuleArguments.put("key_generator_properties", Map.of("worker-id", "3"));
            planApplyAndValidateWorkflow(interactionClient, SHARDING_PLAN_TOOL_NAME, orderRuleArguments);
            applyAndValidateWorkflow(interactionClient, SHARDING_PLAN_TOOL_NAME, createShardingTableRuleArguments("order_items"));
            applyAndValidateWorkflow(interactionClient, SHARDING_TABLE_REFERENCE_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "rule", "order_reference",
                            "reference_tables", "orders,order_items"));
            applyAndValidateWorkflow(interactionClient, SHARDING_DEFAULT_STRATEGY_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "default_strategy_type", "DATABASE", "strategy_type", "none"));
            applyAndValidateWorkflow(interactionClient, SHARDING_KEY_GENERATE_STRATEGY_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "key_generate_strategy", "order_key_strategy",
                            "table", "orders", "column", "order_id", "key_generator", "snowflake_generator"));
            applyAndValidateWorkflow(interactionClient, SHARDING_KEY_GENERATE_STRATEGY_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "key_generate_strategy", "order_sequence_strategy",
                            "sequence", "order_seq", "key_generator", "snowflake_generator"));
            applyAndValidateWorkflow(interactionClient, SHARDING_KEY_GENERATOR_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "key_generator", "unused_generator",
                            "key_generator_type", "SNOWFLAKE", "key_generator_properties", Map.of("worker-id", "2")));
            List<Map<String, Object>> actualRules = getPayloadItems(interactionClient.readResource(
                    String.format(SHARDING_TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualTableRule = findItemByField(actualRules, "table", "orders");
            assertThat(String.valueOf(actualTableRule.get("actual_data_nodes")), is("ds_0.orders"));
            assertThat(String.valueOf(actualTableRule.get("table_strategy_type")).toUpperCase(Locale.ENGLISH), is("STANDARD"));
            assertThat(String.valueOf(actualTableRule.get("table_sharding_algorithm_type")).toUpperCase(Locale.ENGLISH), is("INLINE"));
            assertThat(String.valueOf(actualTableRule.get("key_generate_column")), is("order_id"));
            Map<String, Object> actualReferenceRule = findItemByField(getPayloadItems(interactionClient.readResource(String.format(
                    "shardingsphere://features/sharding/databases/%s/table-reference-rules", getLogicalDatabaseName()))), "name", "order_reference");
            assertTrue(String.valueOf(actualReferenceRule.get("sharding_table_reference")).contains("orders"));
            Map<String, Object> actualDefaultStrategy = findItemByField(getPayloadItems(interactionClient.readResource(String.format(
                    "shardingsphere://features/sharding/databases/%s/default-strategy", getLogicalDatabaseName()))), "name", "DATABASE");
            assertThat(String.valueOf(actualDefaultStrategy.get("type")).toUpperCase(Locale.ENGLISH), is("NONE"));
            Map<String, Object> actualKeyGenerator = findItemByField(getPayloadItems(interactionClient.readResource(String.format(
                    "shardingsphere://features/sharding/databases/%s/key-generators", getLogicalDatabaseName()))), "name", "snowflake_generator");
            assertThat(String.valueOf(actualKeyGenerator.get("type")).toUpperCase(Locale.ENGLISH), is("SNOWFLAKE"));
            List<Map<String, Object>> actualKeyGenerateStrategies = getPayloadItems(interactionClient.readResource(String.format(
                    "shardingsphere://features/sharding/databases/%s/key-generate-strategies", getLogicalDatabaseName())));
            Map<String, Object> actualKeyGenerateStrategy = findItemByField(actualKeyGenerateStrategies, "name", "order_key_strategy");
            assertThat(String.valueOf(actualKeyGenerateStrategy.get("column")), is("order_id"));
            Map<String, Object> actualSequenceKeyGenerateStrategy = findItemByField(actualKeyGenerateStrategies, "name", "order_sequence_strategy");
            assertThat(String.valueOf(actualSequenceKeyGenerateStrategy.get("type")).toUpperCase(Locale.ENGLISH), is("SEQUENCE"));
            assertThat(String.valueOf(actualSequenceKeyGenerateStrategy.get("sequence")), is("order_seq"));
            List<Map<String, Object>> unusedKeyGenerators = getPayloadItems(interactionClient.readResource(String.format(
                    "shardingsphere://features/sharding/databases/%s/unused-key-generators", getLogicalDatabaseName())));
            assertThat(findItemByField(unusedKeyGenerators, "name", "unused_generator").get("name"), is("unused_generator"));
            applyAndValidateWorkflow(interactionClient, SHARDING_COMPONENT_CLEANUP_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "component_type", "key-generator", "component_name", "unused_generator"));
            assertFalse(getPayloadItems(interactionClient.readResource(String.format(
                    "shardingsphere://features/sharding/databases/%s/unused-key-generators", getLogicalDatabaseName())))
                    .stream().anyMatch(each -> "unused_generator".equalsIgnoreCase(String.valueOf(each.get("name")))));
            applyAndValidateWorkflow(interactionClient, SHARDING_KEY_GENERATE_STRATEGY_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "key_generate_strategy", "order_sequence_strategy"));
            applyAndValidateWorkflow(interactionClient, SHARDING_KEY_GENERATE_STRATEGY_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "key_generate_strategy", "order_key_strategy"));
            applyAndValidateWorkflow(interactionClient, SHARDING_TABLE_REFERENCE_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "rule", "order_reference"));
            applyAndValidateWorkflow(interactionClient, SHARDING_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "table", "order_items"));
            applyAndValidateWorkflow(interactionClient, SHARDING_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "table", "orders"));
            applyAndValidateWorkflow(interactionClient, SHARDING_DEFAULT_STRATEGY_PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "operation_type", "drop", "default_strategy_type", "DATABASE"));
        }
    }
    
    private Map<String, Object> createShardingTableRuleArguments(final String tableName) {
        return Map.of("database", getLogicalDatabaseName(), "operation_type", "create", "table", tableName, "column", "order_id",
                "data_nodes", "ds_0." + tableName, "strategy_type", "standard", "algorithm_type", "INLINE",
                "algorithm_properties", Map.of("algorithm-expression", tableName));
    }
    
    private static Stream<Arguments> featureWorkflowScenarios() {
        return Stream.of(
                Arguments.of("broadcast", new FeatureWorkflowScenario(BROADCAST_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "tables", "orders"), "CREATE BROADCAST TABLE RULE", List.of())),
                Arguments.of("readwrite-splitting", new FeatureWorkflowScenario(READWRITE_SPLITTING_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "rule", "readwrite_ds", "write_storage_unit", "ds_0",
                                "read_storage_units", "ds_1", "transactional_read_query_strategy", "DYNAMIC"),
                        "CREATE READWRITE_SPLITTING RULE", List.of("shardingsphere://databases/%s/storage-units"))),
                Arguments.of("shadow", new FeatureWorkflowScenario(SHADOW_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "rule", "shadow_rule", "source_storage_unit", "ds_0",
                                "shadow_storage_unit", "ds_shadow", "table", "orders", "algorithm_type", "VALUE_MATCH",
                                "algorithm_properties", Map.of("operation", "insert", "column", "order_id", "value", "1")),
                        "CREATE SHADOW RULE", List.of("shardingsphere://databases/%s/storage-units", "shardingsphere://databases/%s/single-tables",
                                "shardingsphere://databases/%s/single-tables/orders"))),
                Arguments.of("sharding", new FeatureWorkflowScenario(SHARDING_PLAN_TOOL_NAME,
                        Map.of("operation_type", "create", "table", "orders", "column", "order_id",
                                "data_nodes", "ds_0.orders", "strategy_type", "standard", "algorithm_type", "INLINE",
                                "algorithm_properties", Map.of("algorithm-expression", "orders")),
                        "CREATE SHARDING TABLE RULE", List.of("shardingsphere://databases/%s/storage-units", "shardingsphere://databases/%s/single-tables",
                                "shardingsphere://databases/%s/single-tables/orders"))));
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
        applyAndValidateWorkflow(interactionClient, toolName, arguments);
        assertDuplicateCreateFails(interactionClient, toolName, arguments);
    }
    
    private void applyAndValidateWorkflow(final MCPInteractionClient interactionClient, final String toolName,
                                          final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> actualPlanResponse = planWorkflow(interactionClient, toolName, arguments);
        String planId = String.valueOf(actualPlanResponse.get("plan_id"));
        Map<String, Object> actualApplyResponse = applyReviewedWorkflow(interactionClient, planId);
        assertApplyCompleted(actualApplyResponse);
        assertThat(getStringListOrEmpty(actualApplyResponse.get("executed_distsql")).size(), is(1));
        assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
    }
    
    private Map<String, Object> planWorkflow(final MCPInteractionClient interactionClient, final String toolName,
                                             final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> result = interactionClient.call(toolName, arguments);
        assertThat(String.valueOf(result.get("status")), is("planned"));
        assertNoForbiddenArtifacts(result);
        assertModelFacingPayloadContract(result);
        return result;
    }
    
    private void assertDuplicateCreateFails(final MCPInteractionClient interactionClient, final String toolName,
                                            final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call(toolName, arguments);
        assertThat(String.valueOf(actual.get("status")), is("failed"));
        assertFalse(getObjectListOrEmpty(actual.get("issues")).isEmpty());
        assertModelFacingPayloadContract(actual);
    }
    
    private record FeatureWorkflowScenario(String toolName, Map<String, Object> planArguments, String expectedDistSQLToken, List<String> resourceUriTemplates) {
    }
}
