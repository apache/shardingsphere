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
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class FeatureWorkflowContractE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final List<String> FORBIDDEN_ARTIFACT_TOKENS = List.of(
            "create table", "alter table", "drop table", "create index", "drop index", "migrate", "migration", "backfill", "data probe", "physical metadata",
            "register storage unit", "alter storage unit", "unregister storage unit");
    
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
                Arguments.of("shadow", new FeatureWorkflowScenario("database_gateway_plan_shadow_rule", "shardingsphere://features/shadow/databases/{database}/rules",
                        Map.of("database", "logic_db", "operation_type", "create", "rule", "shadow_rule", "source_storage_unit", "demo_ds",
                                "shadow_storage_unit", "demo_ds_shadow", "table", "orders", "algorithm_type", "VALUE_MATCH",
                                "algorithm_properties", Map.of("operation", "insert", "column", "user_id", "value", "1")))),
                Arguments.of("sharding", new FeatureWorkflowScenario("database_gateway_plan_sharding_table_rule",
                        "shardingsphere://features/sharding/databases/{database}/table-rules",
                        Map.of("database", "logic_db", "operation_type", "create", "table", "t_order", "column", "order_id",
                                "data_nodes", "ds_${0..1}.t_order_${0..1}", "strategy_type", "standard", "algorithm_type", "INLINE",
                                "algorithm_properties", Map.of("algorithm-expression", "t_order_${order_id % 2}"), "key_generate_column", "id",
                                "key_generator_type", "SNOWFLAKE"))));
    }
    
    private void assertFeatureDiscovery(final HttpClient httpClient, final String sessionId, final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://capabilities");
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        assertTrue(((List<?>) payload.get("supportedTools")).stream().map(String::valueOf).toList().contains(scenario.toolName()));
        assertTrue(String.valueOf(payload).contains(scenario.discoveryToken()));
        assertModelFacingPayloadContract(payload);
    }
    
    private void assertClarifiesMissingDatabase(final HttpClient httpClient, final String sessionId, final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, scenario.toolName(), Map.of("natural_language_intent", "plan DistSQL-only feature rule"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("status")), is("clarifying"));
        assertTrue(String.valueOf(payload).contains("Please provide logical database first."));
        assertModelFacingPayloadContract(payload);
    }
    
    private void assertRecoversWhenDistSQLIsUnsupportedByDirectDatabase(final HttpClient httpClient, final String sessionId,
                                                                        final FeatureWorkflowScenario scenario) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, scenario.toolName(), scenario.planArguments());
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> result = getStructuredContent(actual.body());
        assertThat(String.valueOf(result.get("response_mode")), is("recovery"));
        assertFalse(String.valueOf(result.get("message")).isBlank());
        assertNoForbiddenArtifacts(result);
        assertModelFacingPayloadContract(result);
    }
    
    private void assertNoForbiddenArtifacts(final Map<String, Object> payload) {
        String actualPayload = String.valueOf(payload).toLowerCase(Locale.ENGLISH);
        FORBIDDEN_ARTIFACT_TOKENS.forEach(each -> assertFalse(actualPayload.contains(each)));
    }
    
    private void assertModelFacingPayloadContract(final Map<String, Object> payload) {
        MCPModelContractAssertions.assertNoBannedPublicFields(payload);
        MCPModelContractAssertions.assertCanonicalNextActionLists(payload);
    }
    
    private record FeatureWorkflowScenario(String toolName, String discoveryToken, Map<String, Object> planArguments) {
    }
}
