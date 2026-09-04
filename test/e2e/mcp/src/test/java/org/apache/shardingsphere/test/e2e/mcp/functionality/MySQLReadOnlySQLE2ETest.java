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

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
class MySQLReadOnlySQLE2ETest extends AbstractMySQLRuntimeE2ETest {
    
    @Override
    protected boolean useSharedRuntimeFixture() {
        return true;
    }
    
    @Test
    void assertExecuteSelect() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
    
    @Test
    void assertExecuteSelectWithTruncation() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(((List<?>) actual.get("rows")).size(), is(1));
            assertThat(String.valueOf(actual.get("truncated")), is("true"));
        }
    }
    
    @Test
    void assertExecuteExplainSelect() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_explain_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT * FROM orders WHERE order_id = 1",
                            "explain_sql", "EXPLAIN SELECT * FROM orders WHERE order_id = 1", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(String.valueOf(actual.get("statement_type")), is("EXPLAIN"));
            assertFalse(((List<?>) actual.get("rows")).isEmpty());
        }
    }
    
    @Test
    void assertExecuteQueryTimeout() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT SLEEP(2)", "timeout_ms", 1));
            assertRecoveryResponse(actual);
        }
    }
    
    @Test
    void assertRejectSequenceResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/sequences", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME));
            assertThat(String.valueOf(actual.get("error_code")), is("json_rpc_error"));
            assertThat(String.valueOf(actual.get("message")), is("Sequence resources are not supported for the current database."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("allTransportCases")
    void assertAiNativeDeterministicInteractionLoop(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertAiNativeGuidance(interactionClient.readResource("shardingsphere://guidance"));
            assertAiNativeDiscovery(interactionClient);
            Map<String, Object> searchMetadataPayload = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "orders", "object_types", List.of("table")));
            Map<String, Object> tableHit = MCPPayloadAssertions.findItem(searchMetadataPayload, "name", "orders");
            String tableResourceUri = String.valueOf(getObjectOrEmpty(tableHit.get("resource")).get("uri"));
            assertThat(tableResourceUri, is("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders"));
            assertFalse(getRequiredObjectList(tableHit.get("next_resources")).isEmpty());
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(tableResourceUri), "table", "orders");
            assertAiNativeSqlPreview(interactionClient);
            assertAiNativeSqlResult(interactionClient);
        }
    }
    
    private void assertAiNativeGuidance(final Map<String, Object> guidance) {
        assertTrue(guidance.containsKey("discovery"));
        assertTrue(guidance.containsKey("model_contract"));
        assertTrue(guidance.containsKey("next_action_contract"));
        assertTrue(guidance.containsKey("common_flows"));
        assertTrue(guidance.containsKey("security_hints"));
        assertFalse(guidance.containsKey("model_first_summary"));
        assertFalse(guidance.containsKey("surface_summary"));
        assertFalse(guidance.containsKey("fingerprints"));
        assertFalse(((List<?>) guidance.get("common_flows")).isEmpty());
        Map<String, Object> discovery = getObjectOrEmpty(guidance.get("discovery"));
        assertThat(getObjectOrEmpty(discovery.get("official_discovery_methods")).get("tools"), is("tools/list"));
        assertThat(discovery.get("argument_completion_method"), is("completion/complete"));
        Map<String, Object> modelContract = getObjectOrEmpty(guidance.get("model_contract"));
        assertTrue(String.valueOf(modelContract.get("preflight_rule")).contains("database_gateway_validate_runtime_database"));
        assertTrue(String.valueOf(getObjectOrEmpty(modelContract.get("sql_tool_selection")).get("read_only")).contains("database_gateway_execute_query"));
        assertThat(modelContract.get("recovery_rule"), is("When a call fails, follow top-level next_actions before inventing a new call."));
    }
    
    private void assertAiNativeDiscovery(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> runtimeStatus = interactionClient.readResource("shardingsphere://runtime");
        assertThat(String.valueOf(runtimeStatus.get("status")), is("available"));
        assertThat(String.valueOf(runtimeStatus.get("configured_database_count")), is("1"));
        assertTrue(MCPInteractionPayloads.getRequiredObjectList(interactionClient.listResources(), "resources").stream()
                .anyMatch(each -> "shardingsphere://runtime".equals(each.get("uri"))));
        assertTrue(MCPInteractionPayloads.getRequiredObjectList(interactionClient.listResourceTemplates(), "resourceTemplates").stream()
                .anyMatch(each -> "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}".equals(each.get("uriTemplate"))));
        assertTrue(interactionClient.listTools().stream()
                .anyMatch(each -> "database_gateway_execute_update".equals(each.get("name")) && getObjectOrEmpty(each.get("outputSchema")).containsKey("properties")));
        Map<String, Object> promptPayload = interactionClient.getPrompt("inspect_metadata",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "orders"));
        assertTrue(String.valueOf(promptPayload).contains("Stop conditions"));
        Map<String, Object> completionPayload = interactionClient.complete(Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "schema", "log", Map.of("database", LOGICAL_DATABASE_NAME));
        assertTrue(((List<?>) getObjectOrEmpty(completionPayload.get("completion")).get("values")).contains(LOGICAL_DATABASE_NAME));
    }
    
    private void assertAiNativeSqlPreview(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "preview"));
        assertThat(String.valueOf(actual.get("response_mode")), is("preview"));
        assertThat(String.valueOf(actual.get("result_kind")), is("preview"));
        assertThat(String.valueOf(actual.get("preview_semantics")), is("classification_only"));
        assertFalse((Boolean) actual.get("would_execute"));
        List<Map<String, Object>> nextActions = getRequiredObjectList(actual.get("next_actions"));
        assertThat(nextActions.stream().map(each -> String.valueOf(each.get("type"))).toList(), is(List.of("ask_user", "tool_call")));
        Map<String, Object> askUserAction = nextActions.getFirst();
        assertThat(askUserAction.get("order"), is(1));
        assertThat(askUserAction.get("required_inputs"), is(List.of("execution_approved")));
        Map<String, Object> toolCallAction = nextActions.get(1);
        assertThat(toolCallAction.get("order"), is(2));
        assertThat(toolCallAction.get("tool_name"), is("database_gateway_execute_update"));
        assertThat(toolCallAction.get("depends_on"), is(List.of(1)));
        assertThat(getObjectOrEmpty(toolCallAction.get("arguments")).get("execution_mode"), is("execute"));
    }
    
    private void assertAiNativeSqlResult(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
        assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        assertThat(String.valueOf(actual.get("row_object_status")), is("available"));
        assertThat(((List<?>) actual.get("row_objects")).size(), is(1));
        assertThat(String.valueOf(actual.get("truncated")), is("true"));
        assertThat(String.valueOf(getRequiredObjectList(actual.get("next_actions")).getFirst().get("type")), is("ask_user"));
    }
}
