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

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionPostgreSQLRuntimeE2ETest extends AbstractProductionPostgreSQLRuntimeE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dualTransports")
    void assertPostgreSQLRuntimeContract(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertMetadata(interactionClient);
            assertQueries(interactionClient);
            assertTransactionRollback(interactionClient);
        }
    }
    
    private void assertMetadata(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> capabilities = interactionClient.readResource("shardingsphere://databases/postgres_db/capabilities");
        assertThat(String.valueOf(capabilities.get("databaseType")), is("PostgreSQL"));
        MCPPayloadAssertions.assertItemValues(interactionClient.readResource("shardingsphere://databases/postgres_db/schemas"), "schema", List.of("public", "tenant"));
        MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(
                "shardingsphere://databases/postgres_db/schemas/public/tables"), "table", "orders");
        MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(
                "shardingsphere://databases/postgres_db/schemas/tenant/tables"), "table", "orders");
        MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(
                "shardingsphere://databases/postgres_db/schemas/public/views"), "view", "active_orders");
        Map<String, Object> publicOrders = getPayloadItems(interactionClient.readResource(
                "shardingsphere://databases/postgres_db/schemas/public/tables/orders")).getFirst();
        assertThat(getNestedNames(publicOrders, "columns", "column"), is(List.of("order_id", "status", "tenant_code")));
        Map<String, Object> statusColumn = findNested(publicOrders, "columns", "column", "status");
        assertThat(statusColumn.get("ordinalPosition"), is(2));
        assertThat(statusColumn.get("jdbcType"), is(Types.VARCHAR));
        assertThat(statusColumn.get("nativeTypeName"), is("varchar"));
        assertThat(statusColumn.get("nullability"), is("nullable"));
        Map<String, Object> tenantColumn = findNested(publicOrders, "columns", "column", "tenant_code");
        assertThat(tenantColumn.get("nullability"), is("not_nullable"));
        Map<String, Object> compositeIndex = findNested(publicOrders, "indexes", "index", "idx_orders_tenant_order");
        assertThat(compositeIndex.get("columns"), is(List.of("tenant_code", "order_id")));
        assertTrue((Boolean) compositeIndex.get("unique"));
        assertThat(getNestedNames(getPayloadItems(interactionClient.readResource(
                "shardingsphere://databases/postgres_db/schemas/tenant/tables/orders")).getFirst(), "columns", "column"),
                is(List.of("order_id", "tenant_note")));
        assertThat(getNestedNames(getPayloadItems(interactionClient.readResource(
                "shardingsphere://databases/postgres_db/schemas/public/views/active_orders")).getFirst(), "columns", "column"),
                is(List.of("order_id", "status")));
    }
    
    private void assertQueries(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> query = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", "public", "sql", "SELECT status FROM public.orders WHERE order_id = 1", "max_rows", 1));
        assertThat(String.valueOf(query.get("result_kind")), is("result_set"));
        assertTrue(String.valueOf(query.get("row_objects")).contains("NEW"));
        Map<String, Object> tenantQuery = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", "tenant", "sql", "SELECT tenant_note FROM tenant.orders WHERE order_id = 1", "max_rows", 1));
        assertTrue(String.valueOf(tenantQuery.get("row_objects")).contains("tenant-schema"));
        Map<String, Object> explain = interactionClient.call("database_gateway_execute_explain_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", "public", "sql", "SELECT * FROM public.orders WHERE order_id = 1",
                        "explain_sql", "EXPLAIN SELECT * FROM public.orders WHERE order_id = 1", "max_rows", 10));
        assertThat(String.valueOf(explain.get("statement_type")), is("EXPLAIN"));
        assertFalse(((List<?>) explain.get("rows")).isEmpty());
    }
    
    private void assertTransactionRollback(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertThat(interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("public", "BEGIN")).get("summary"), is("Transaction started."));
        interactionClient.call("database_gateway_execute_update",
                createExecuteUpdateArguments("public", "UPDATE public.orders SET status = 'PENDING' WHERE order_id = 1"));
        assertThat(interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("public", "ROLLBACK")).get("summary"), is("Transaction rolled back."));
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", "public", "sql", "SELECT status FROM public.orders WHERE order_id = 1", "max_rows", 1));
        assertTrue(String.valueOf(actual.get("row_objects")).contains("NEW"));
    }
    
    private Map<String, Object> findNested(final Map<String, Object> payload, final String collectionName, final String fieldName, final String expectedValue) {
        return ((List<?>) payload.get(collectionName)).stream().map(each -> MCPInteractionPayloads.getRequiredObjectValue(each, collectionName))
                .filter(each -> expectedValue.equals(each.get(fieldName))).findFirst().orElseThrow();
    }
}
