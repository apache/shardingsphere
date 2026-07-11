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

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionMySQLReadOnlySQLRuntimeE2ETest extends AbstractProductionMySQLRuntimeE2ETest {
    
    @Override
    protected boolean useSharedRuntimeFixture() {
        return true;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteSelectWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteSelectWithTruncationWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(((List<?>) actual.get("rows")).size(), is(1));
            assertThat(String.valueOf(actual.get("truncated")), is("true"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteExplainSelectWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_explain_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT * FROM orders WHERE order_id = 1",
                            "explain_sql", "EXPLAIN SELECT * FROM orders WHERE order_id = 1", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(String.valueOf(actual.get("statement_type")), is("EXPLAIN"));
            assertFalse(((List<?>) actual.get("rows")).isEmpty());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteQueryTimeoutWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT SLEEP(2)", "timeout_ms", 1));
            assertRecoveryResponse(actual);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectExecuteMultiStatementWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT 1; SELECT 2"));
            assertRecoveryResponse(actual, "Only one SQL statement is allowed.", "multiple_sql_statements");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectExplainUpdateWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_explain_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = 'DONE' WHERE order_id = 1",
                            "explain_sql", "EXPLAIN UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
            assertRecoveryResponse(actual, "database_gateway_execute_explain_query only supports QUERY statements as the explained SQL.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectLockingReadFromReadOnlyToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT * FROM orders FOR UPDATE"));
            assertRecoveryResponse(actual, "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectLockingReadFromUpdateToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("SELECT * FROM orders FOR UPDATE"));
            assertRecoveryResponse(actual, "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dualTransports")
    void assertElicitMaskPlanningWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException {
        useTransport(transport);
        List<McpSchema.ElicitRequest> actualElicitationRequests = new CopyOnWriteArrayList<>();
        try (McpSyncClient client = createElicitationClient(transport, actualElicitationRequests)) {
            client.initialize();
            McpSchema.CallToolResult actual = client.callTool(new McpSchema.CallToolRequest(MASK_PLAN_TOOL_NAME, Map.of(
                    "database", LOGICAL_DATABASE_NAME,
                    "schema", LOGICAL_DATABASE_NAME,
                    "table", "orders",
                    "column", "status",
                    "operation_type", "create",
                    "algorithm_type", "MASK_FROM_X_TO_Y")));
            Map<String, Object> actualPayload = castStructuredContent(actual.structuredContent());
            if (RuntimeTransport.HTTP == transport) {
                assertThat(String.valueOf(actualPayload.get("status")), is("clarifying"));
                assertThat(String.valueOf(actualPayload.get("fallback_reason")), is("remote_identity_required"));
                assertTrue(actualElicitationRequests.isEmpty());
                return;
            }
            assertThat(String.valueOf(actualPayload.get("status")), is("planned"));
            assertThat(String.valueOf(actualPayload.get("current_step")), is("review"));
            assertThat(String.valueOf(castToMap(castToMap(actualPayload.get("masked_property_preview")).get("primary")).get("from-x")), is("1"));
            assertThat(String.valueOf(castToMap(castToMap(actualPayload.get("masked_property_preview")).get("primary")).get("to-y")), is("3"));
            assertElicitationRequest(actualElicitationRequests);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectSequenceResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/sequences", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME));
            assertThat(String.valueOf(actual.get("error_code")), is("json_rpc_error"));
            assertThat(String.valueOf(actual.get("message")), is("Sequence resources are not supported for the current database."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertAiNativeDeterministicInteractionLoopWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertAiNativeGuidance(interactionClient.readResource("shardingsphere://guidance"));
            assertAiNativeDiscovery(interactionClient);
            Map<String, Object> searchMetadataPayload = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "orders", "object_types", List.of("table")));
            Map<String, Object> tableHit = MCPPayloadAssertions.findItem(searchMetadataPayload, "name", "orders");
            String tableResourceUri = String.valueOf(getMap(tableHit.get("resource")).get("uri"));
            assertThat(tableResourceUri, is("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders"));
            assertFalse(getMapList(tableHit.get("next_resources")).isEmpty());
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(tableResourceUri), "table", "orders");
            assertAiNativeSqlPreview(interactionClient);
            assertAiNativeSqlResult(interactionClient);
        }
    }
}
