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

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeConfigurationTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
abstract class ProductionH2RuntimeSmokeE2ETest extends AbstractTransportParameterizedProductionRuntimeE2ETest {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-runtime-smoke", getTransport().getH2AccessMode());
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return H2RuntimeConfigurationTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    protected void assertOfficialToolNames(final List<String> actualToolNames) {
        assertThat(actualToolNames, containsInAnyOrder(OfficialMCPToolNames.getAll().toArray()));
    }
    
    protected void assertAiNativeCapabilities(final Map<String, Object> capabilities) {
        assertTrue(capabilities.containsKey("model_first_summary"));
        assertTrue(capabilities.containsKey("model_contract"));
        assertTrue(capabilities.containsKey("surface_summary"));
        assertTrue(capabilities.containsKey("field_naming_contract"));
        assertTrue(capabilities.containsKey("next_action_contract"));
        assertTrue(capabilities.containsKey("common_flows"));
        assertTrue(capabilities.containsKey("security_hints"));
        assertTrue(capabilities.containsKey("fingerprints"));
        assertFalse(getMap(capabilities.get("fingerprints")).isEmpty());
        assertFalse(((List<?>) capabilities.get("common_flows")).isEmpty());
        Map<String, Object> modelFirstSummary = getMap(capabilities.get("model_first_summary"));
        assertThat(getMap(modelFirstSummary.get("official_discovery_methods")).get("tools"), is("tools/list"));
        assertThat(modelFirstSummary.get("safe_first_resource"), is("shardingsphere://capabilities"));
        assertThat(getMap(getMap(modelFirstSummary.get("sql_tool_selection")).get("read_only")).get("tool"), is("database_gateway_execute_query"));
        assertThat(getMap(getMap(modelFirstSummary.get("workflow_rule")).get("preview_tool")).get("tool"), is("database_gateway_apply_workflow"));
        Map<String, Object> surfaceSummary = getMap(capabilities.get("surface_summary"));
        assertThat(getMap(surfaceSummary.get("first_protocol_methods")).get("resources"), is("resources/list"));
        assertThat(surfaceSummary.get("read_only_sql_tool"), is("database_gateway_execute_query"));
        assertThat(surfaceSummary.get("side_effect_sql_tool"), is("database_gateway_execute_update"));
    }
    
    protected void assertAiNativeDiscovery(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> runtimeStatus = interactionClient.readResource("shardingsphere://runtime");
        assertThat(String.valueOf(runtimeStatus.get("status")), is("available"));
        assertThat(String.valueOf(runtimeStatus.get("configured_database_count")), is("1"));
        assertTrue(getResources(interactionClient.listResources()).stream().anyMatch(each -> "shardingsphere://runtime".equals(each.get("uri"))));
        assertTrue(getResourceTemplates(interactionClient.listResourceTemplates()).stream()
                .anyMatch(each -> "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}".equals(each.get("uriTemplate"))));
        assertTrue(interactionClient.listTools().stream().anyMatch(each -> "database_gateway_execute_update".equals(each.get("name")) && getMap(each.get("outputSchema")).containsKey("properties")));
        Map<String, Object> promptPayload = interactionClient.getPrompt("inspect_metadata", Map.of("database", "logic_db", "schema", "public", "query", "orders"));
        assertTrue(String.valueOf(promptPayload).contains("Stop conditions"));
        Map<String, Object> completionPayload = interactionClient.complete(Map.of("type", "ref/prompt", "name", "inspect_metadata"), "schema", "pub", Map.of("database", "logic_db"));
        assertTrue(((List<?>) getMap(completionPayload.get("completion")).get("values")).contains("public"));
    }
    
    protected void assertAiNativeSqlPreview(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "preview"));
        assertThat(String.valueOf(actual.get("response_mode")), is("preview"));
        assertThat(String.valueOf(actual.get("result_kind")), is("preview"));
        assertThat(String.valueOf(actual.get("preview_semantics")), is("classification_only"));
        assertFalse((Boolean) actual.get("would_execute"));
        List<Map<String, Object>> nextActions = getMapList(actual.get("next_actions"));
        assertThat(nextActions.stream().map(each -> String.valueOf(each.get("type"))).toList(), is(List.of("ask_user", "tool_call")));
        assertTrue((Boolean) nextActions.get(1).get("requires_user_approval"));
    }
    
    protected void assertAiNativeSqlResult(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
        assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        assertThat(String.valueOf(actual.get("row_object_status")), is("available"));
        assertThat(((List<?>) actual.get("row_objects")).size(), is(1));
        assertThat(String.valueOf(actual.get("truncated")), is("true"));
        assertThat(String.valueOf(getMapList(actual.get("next_actions")).get(0).get("type")), is("ask_user"));
    }
    
    protected Map<String, Object> getMap(final Object value) {
        return MCPPayloadAssertions.getMap(value);
    }
    
    protected List<Map<String, Object>> getMapList(final Object value) {
        return MCPPayloadAssertions.getMapList(value);
    }
    
    protected static boolean isEnabled() {
        return MCPE2ECondition.isProductionH2Enabled() || MCPE2ECondition.isProductionStdioEnabled();
    }
    
    protected static Stream<Arguments> transports() {
        return runtimeTransports().map(each -> Arguments.of(getTransportName(each), each));
    }
    
    protected static Stream<Arguments> assertReadSingleMetadataResourceCases() {
        return runtimeTransports().flatMap(each -> Stream.of(
                Arguments.of(getTransportName(each) + " database detail", each, "shardingsphere://databases/logic_db", "database", "logic_db"),
                Arguments.of(getTransportName(each) + " schema detail", each, "shardingsphere://databases/logic_db/schemas/public", "schema", "public"),
                Arguments.of(getTransportName(each) + " table column detail", each, "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/status", "column", "status"),
                Arguments.of(getTransportName(each) + " view detail", each, "shardingsphere://databases/logic_db/schemas/public/views/active_orders", "view", "active_orders"),
                Arguments.of(getTransportName(each) + " view column detail", each,
                        "shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns/status", "column", "status"),
                Arguments.of(getTransportName(each) + " index detail", each, "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/idx_orders_status", "index",
                        "idx_orders_status"),
                Arguments.of(getTransportName(each) + " sequence detail", each, "shardingsphere://databases/logic_db/schemas/public/sequences/order_seq", "sequence", "order_seq")));
    }
    
    protected static Stream<Arguments> assertReadCollectionMetadataResourceCases() {
        return runtimeTransports().flatMap(each -> Stream.of(
                Arguments.of(getTransportName(each) + " schemas list", each, "shardingsphere://databases/logic_db/schemas", "schema", List.of("public")),
                Arguments.of(getTransportName(each) + " tables list", each, "shardingsphere://databases/logic_db/schemas/public/tables", "table", List.of("order_items", "orders")),
                Arguments.of(getTransportName(each) + " table columns list", each,
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", "column", List.of("amount", "order_id", "status")),
                Arguments.of(getTransportName(each) + " view columns list", each,
                        "shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns", "column", List.of("order_id", "status"))));
    }
    
    protected static Map<String, Object> createExecuteUpdateArguments(final String sql) {
        return Map.of("database", "logic_db", "schema", "public", "sql", sql, "execution_mode", "execute", "approved_by_user", true);
    }
    
    protected String getJdbcUrl() {
        return jdbcUrl;
    }
    
    protected static Stream<RuntimeTransport> runtimeTransports() {
        Stream.Builder<RuntimeTransport> result = Stream.builder();
        if (MCPE2ECondition.isProductionH2Enabled()) {
            result.add(RuntimeTransport.HTTP);
        }
        if (MCPE2ECondition.isProductionStdioEnabled()) {
            result.add(RuntimeTransport.STDIO);
        }
        return result.build();
    }
    
    protected static String getTransportName(final RuntimeTransport transport) {
        return RuntimeTransport.HTTP == transport ? "http" : "stdio";
    }
    
    protected void assertToolDefinition(final List<Map<String, Object>> tools, final String toolName, final String expectedTitle,
                                        final String expectedRequiredField, final String expectedPropertyField, final String expectedPropertyType) {
        MCPPayloadAssertions.assertToolDefinition(tools, toolName, expectedTitle, expectedRequiredField, expectedPropertyField, expectedPropertyType);
    }
}
