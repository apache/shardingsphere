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
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class FeatureWorkflowContractE2ETest extends AbstractSharedHttpProgrammaticRuntimeE2ETest {
    
    private static final List<String> FORBIDDEN_ARTIFACT_TOKENS = List.of(
            "create table", "alter table", "drop table", "create index", "drop index", "migrate", "migration", "backfill", "data probe", "physical metadata",
            "register storage unit", "alter storage unit", "unregister storage unit");
    
    private static final List<String> REQUIRED_WORKFLOW_OUTPUT_FIELDS = List.of(
            "response_mode", "plan_id", "workflow_kind", "status", "missing_required_inputs", "resources_to_read", "next_actions");
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("featureWorkflowScenarios")
    void assertPlanFeatureWorkflowContract(final String scenarioName, final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        assertFeatureDiscovery(httpClient, sessionId, scenario);
        assertFeatureToolSchemaMatchesPlanArguments(httpClient, sessionId, scenario);
        assertRejectUnsupportedPlanArgument(httpClient, sessionId, scenario);
        assertClarifiesMissingDatabase(httpClient, sessionId, scenario);
        assertRecoversWhenDistSQLIsUnsupportedByDirectDatabase(httpClient, sessionId, scenario);
    }
    
    private static Stream<Arguments> featureWorkflowScenarios() {
        return Stream.of(
                Arguments.of("broadcast", new FeatureWorkflowScenario("database_gateway_plan_broadcast_rule", "shardingsphere://features/broadcast/databases/{database}/rules",
                        Map.of("database", "logic_db", "operation_type", "create", "tables", "broadcast_orders"))),
                Arguments.of("readwrite-splitting", new FeatureWorkflowScenario("database_gateway_plan_readwrite_splitting_rule",
                        "shardingsphere://features/readwrite-splitting/databases/{database}/rules",
                        Map.of("database", "logic_db", "operation_type", "create", "rule", "readwrite_ds", "write_storage_unit", "write_ds",
                                "read_storage_units", "read_ds_0", "transactional_read_query_strategy", "DYNAMIC"))),
                Arguments.of("readwrite-splitting status", new FeatureWorkflowScenario("database_gateway_plan_readwrite_splitting_status",
                        "shardingsphere://features/readwrite-splitting/databases/{database}/rules",
                        Map.of("database", "logic_db", "rule", "readwrite_ds", "storage_unit", "read_ds_0", "target_status", "disable"))),
                Arguments.of("shadow", new FeatureWorkflowScenario("database_gateway_plan_shadow_rule", "shardingsphere://features/shadow/databases/{database}/rules",
                        Map.of("database", "logic_db", "operation_type", "create", "rule", "shadow_rule", "source_storage_unit", "demo_ds",
                                "shadow_storage_unit", "demo_ds_shadow", "table", "orders", "algorithm_type", "VALUE_MATCH",
                                "algorithm_properties", Map.of("operation", "insert", "column", "user_id", "value", "1")))),
                Arguments.of("shadow default algorithm", new FeatureWorkflowScenario("database_gateway_plan_default_shadow_algorithm",
                        "shardingsphere://features/shadow/databases/{database}/rules",
                        Map.of("database", "logic_db", "operation_type", "create", "algorithm_type", "SQL_HINT"))),
                Arguments.of("shadow algorithm cleanup", new FeatureWorkflowScenario("database_gateway_plan_shadow_algorithm_cleanup",
                        "shardingsphere://features/shadow/databases/{database}/rules",
                        Map.of("database", "logic_db", "algorithm_name", "unused_algorithm"))),
                Arguments.of("sharding", new FeatureWorkflowScenario("database_gateway_plan_sharding_table_rule",
                        "shardingsphere://features/sharding/databases/{database}/table-rules",
                        Map.of("database", "logic_db", "operation_type", "create", "table", "t_order", "column", "order_id",
                                "data_nodes", "ds_${0..1}.t_order_${0..1}", "strategy_type", "standard", "algorithm_type", "INLINE",
                                "algorithm_properties", Map.of("algorithm-expression", "t_order_${order_id % 2}"), "key_generate_column", "id",
                                "key_generator_type", "SNOWFLAKE"))),
                Arguments.of("sharding table reference", new FeatureWorkflowScenario("database_gateway_plan_sharding_table_reference_rule",
                        "shardingsphere://features/sharding/databases/{database}/table-reference-rules",
                        Map.of("database", "logic_db", "operation_type", "create", "rule", "order_reference", "reference_tables", "t_order,t_order_item"))),
                Arguments.of("sharding default strategy", new FeatureWorkflowScenario("database_gateway_plan_sharding_default_strategy",
                        "shardingsphere://features/sharding/databases/{database}/default-strategy",
                        Map.of("database", "logic_db", "operation_type", "create", "default_strategy_type", "DATABASE", "strategy_type", "none"))),
                Arguments.of("sharding key generator", new FeatureWorkflowScenario("database_gateway_plan_sharding_key_generator",
                        "shardingsphere://features/sharding/databases/{database}/key-generators",
                        Map.of("database", "logic_db", "operation_type", "create", "key_generator", "snowflake_generator",
                                "key_generator_type", "SNOWFLAKE", "key_generator_properties", Map.of("worker-id", "1")))),
                Arguments.of("sharding key generate strategy", new FeatureWorkflowScenario("database_gateway_plan_sharding_key_generate_strategy",
                        "shardingsphere://features/sharding/databases/{database}/key-generate-strategies",
                        Map.of("database", "logic_db", "operation_type", "create", "key_generate_strategy", "order_key_strategy",
                                "table", "t_order", "column", "id", "key_generator", "snowflake_generator"))),
                Arguments.of("sharding component cleanup", new FeatureWorkflowScenario("database_gateway_plan_sharding_rule_component_cleanup",
                        "shardingsphere://features/sharding/databases/{database}/unused-algorithms",
                        Map.of("database", "logic_db", "component_type", "algorithm", "component_name", "unused_algorithm"))));
    }
    
    private void assertFeatureDiscovery(final HttpClient httpClient, final String sessionId, final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actualTools = sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPInteractionProtocolSupport.createJsonRpcRequestBody(
                scenario.toolName() + "-discovery-tools-1", "tools/list", Map.of()));
        assertThat(actualTools.statusCode(), is(200));
        Map<String, Object> toolsPayload = MCPInteractionPayloads.getRequiredJsonRpcResult(parseJsonBody(actualTools.body()));
        assertTrue(MCPInteractionPayloads.getRequiredObjectList(toolsPayload, "tools").stream().anyMatch(each -> scenario.toolName().equals(each.get("name"))));
        assertModelFacingPayloadContract(toolsPayload);
        HttpResponse<String> actualResources = sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPInteractionProtocolSupport.createJsonRpcRequestBody(
                scenario.toolName() + "-discovery-resources-1", "resources/templates/list", Map.of()));
        assertThat(actualResources.statusCode(), is(200));
        assertTrue(actualResources.body().contains(scenario.discoveryToken()));
    }
    
    private void assertFeatureToolSchemaMatchesPlanArguments(final HttpClient httpClient, final String sessionId,
                                                             final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPInteractionProtocolSupport.createJsonRpcRequestBody(
                scenario.toolName() + "-schema-1", "tools/list", Map.of()));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = MCPInteractionPayloads.getRequiredJsonRpcResult(parseJsonBody(actual.body()));
        assertModelFacingPayloadContract(payload);
        Map<String, Object> actualTool = findByKey(MCPInteractionPayloads.getRequiredObjectList(payload, "tools"), "name", scenario.toolName());
        Map<String, Object> actualInputSchema = MCPInteractionPayloads.getRequiredObject(actualTool, "inputSchema");
        assertFalse((Boolean) actualInputSchema.get("additionalProperties"));
        Map<String, Object> actualProperties = MCPInteractionPayloads.getRequiredObject(actualInputSchema, "properties");
        for (String each : scenario.planArguments().keySet()) {
            assertTrue(actualProperties.containsKey(each), each);
        }
        Map<String, Object> actualOutputSchema = MCPInteractionPayloads.getRequiredObject(actualTool, "outputSchema");
        assertThat(((List<?>) actualOutputSchema.get("required")).stream().map(String::valueOf).toList(), is(REQUIRED_WORKFLOW_OUTPUT_FIELDS));
    }
    
    private Map<String, Object> findByKey(final List<Map<String, Object>> values, final String key, final String expectedValue) {
        return values.stream().filter(each -> expectedValue.equals(each.get(key))).findFirst().orElseThrow();
    }
    
    private void assertRejectUnsupportedPlanArgument(final HttpClient httpClient, final String sessionId,
                                                     final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        Map<String, Object> arguments = new LinkedHashMap<>(scenario.planArguments());
        arguments.put("client_hint", "narrow");
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, scenario.toolName(), arguments);
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> responsePayload = parseJsonBody(actual.body());
        Map<String, Object> result = MCPInteractionPayloads.getRequiredJsonRpcResult(responsePayload);
        assertTrue((Boolean) result.get("isError"));
        Map<String, Object> payload = MCPInteractionPayloads.getToolCallPayload(responsePayload);
        Map<String, Object> recovery = getRecoveryPayload(payload, "validation");
        assertThat(recovery.get("category"), is("unknown_argument"));
        assertThat(recovery.get("field"), is("client_hint"));
        assertModelFacingPayloadContract(payload);
    }
    
    private void assertClarifiesMissingDatabase(final HttpClient httpClient, final String sessionId, final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, scenario.toolName(), Map.of("natural_language_intent", "plan DistSQL-only feature rule"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getToolCallPayload(actual.body());
        assertThat(String.valueOf(payload.get("status")), is("clarifying"));
        assertTrue(String.valueOf(payload).contains("Please provide logical database first."));
        assertModelFacingPayloadContract(payload);
    }
    
    private void assertRecoversWhenDistSQLIsUnsupportedByDirectDatabase(final HttpClient httpClient, final String sessionId,
                                                                        final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, scenario.toolName(), scenario.planArguments());
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> result = getToolCallPayload(actual.body());
        assertThat(String.valueOf(result.get("response_mode")), is("recovery"));
        assertFalse(String.valueOf(result.get("summary")).isBlank());
        assertNoForbiddenArtifacts(result);
        assertModelFacingPayloadContract(result);
    }
    
    private void assertNoForbiddenArtifacts(final Map<String, Object> payload) {
        String actualPayload = String.valueOf(payload).toLowerCase(Locale.ENGLISH);
        FORBIDDEN_ARTIFACT_TOKENS.forEach(each -> assertFalse(actualPayload.contains(each)));
    }
    
    private void assertModelFacingPayloadContract(final Map<String, Object> payload) {
        MCPModelContractAssertions.assertCanonicalNextActionLists(payload);
    }
    
    private record FeatureWorkflowScenario(String toolName, String discoveryToken, Map<String, Object> planArguments) {
    }
}
