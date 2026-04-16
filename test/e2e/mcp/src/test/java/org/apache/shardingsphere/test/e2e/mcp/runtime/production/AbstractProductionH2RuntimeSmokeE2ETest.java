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

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractProductionH2RuntimeSmokeE2ETest extends AbstractProductionRuntimeE2ETest {

    private String jdbcUrl;

    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-runtime-smoke", getTransport());
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }

    @Test
    void assertReadDatabasesResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases"));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get("database")), is("logic_db"));
        }
    }

    @Test
    void assertServiceCapabilitiesResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertThat(interactionClient.readResource("shardingsphere://capabilities").get("supportedTools"), is(List.of("search_metadata", "execute_query")));
        }
    }

    @Test
    void assertTableDetailResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
            assertThat(items.size(), is(1));
            Map<String, Object> actualItem = items.get(0);
            assertThat(String.valueOf(actualItem.get("table")), is("orders"));
            assertThat(getNestedNames(actualItem, "columns", "column"), is(List.of("amount", "order_id", "status")));
            assertThat(getNestedNames(actualItem, "indexes", "index"), is(List.of("PRIMARY_KEY_C", "idx_orders_status")));
        }
    }

    @Test
    void assertReadViewsResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/views"));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get("view")), is("active_orders"));
        }
    }

    @Test
    void assertReadIndexesResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes"));
            assertThat(items.stream().map(each -> String.valueOf(each.get("index"))).toList(), is(List.of("PRIMARY_KEY_C", "idx_orders_status")));
        }
    }

    @Test
    void assertListResources() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.listResources();
            assertTrue(getResources(actual).stream().anyMatch(each -> "shardingsphere://capabilities".equals(each.get("uri"))));
        }
    }

    @Test
    void assertExecuteSelect() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }

    @Test
    void assertExecuteUpdate() throws SQLException, IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> payload = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            assertThat(String.valueOf(payload.get("result_kind")), is("update_count"));
            assertThat(String.valueOf(payload.get("affected_rows")), is("1"));
            assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("PENDING"));
        }
    }

    @Test
    void assertReadSequencesResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/sequences"));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get("sequence")), is("order_seq"));
        }
    }

    @Test
    void assertSearchSequence() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.call("search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("SEQUENCE"))));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get("name")), is("order_seq"));
        }
    }

    @Test
    void assertSearchTableAndViewMetadata() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.call("search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("TABLE", "VIEW"))));
            assertThat(items.stream().map(each -> String.valueOf(each.get("name"))).toList(), is(List.of("order_items", "orders", "active_orders")));
        }
    }

    @Test
    void assertExecuteSavepointFlow() throws SQLException, IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
            interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> savepointResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SAVEPOINT sp_1"));
            interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "ROLLBACK TO SAVEPOINT sp_1"));
            Map<String, Object> commitResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("message")), is("Savepoint created."));
            assertThat(String.valueOf(rollbackResponse.get("message")), is("Savepoint rolled back."));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
            assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("PENDING"));
        }
    }

    @Test
    void assertCloseRollsBackPendingTransaction() throws SQLException, IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
            interactionClient.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            interactionClient.close();
            assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("NEW"));
        }
    }

    private static List<String> getNestedNames(final Map<String, Object> item, final String nestedKey, final String nameKey) {
        return ((List<?>) item.get(nestedKey)).stream().map(each -> String.valueOf(((Map<?, ?>) each).get(nameKey))).toList();
    }
}
