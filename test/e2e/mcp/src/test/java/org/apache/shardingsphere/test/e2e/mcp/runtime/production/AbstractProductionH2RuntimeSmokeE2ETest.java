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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
    void assertReadDatabaseCapabilitiesResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource("shardingsphere://databases/logic_db/capabilities");
            assertThat(String.valueOf(actual.get("databaseType")), is("H2"));
            assertThat(String.valueOf(actual.get("supportsExplainAnalyze")), is("true"));
        }
    }
    
    @Test
    void assertServiceCapabilitiesResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertThat(interactionClient.readResource("shardingsphere://capabilities").get("supportedTools"), is(List.of("search_metadata", "execute_query")));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadSingleMetadataResourceCases")
    void assertReadSingleMetadataResource(final String name, final String resourceUri, final String key, final String expectedValue) throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(resourceUri));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get(key)), is(expectedValue));
        }
    }
    
    private static Stream<Arguments> assertReadSingleMetadataResourceCases() {
        return Stream.of(
                Arguments.of("database detail", "shardingsphere://databases/logic_db", "database", "logic_db"),
                Arguments.of("schema detail", "shardingsphere://databases/logic_db/schemas/public", "schema", "public"),
                Arguments.of("table column detail", "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/status", "column", "status"),
                Arguments.of("view detail", "shardingsphere://databases/logic_db/schemas/public/views/active_orders", "view", "active_orders"),
                Arguments.of("view column detail", "shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns/status", "column", "status"),
                Arguments.of("index detail", "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/idx_orders_status", "index", "idx_orders_status"),
                Arguments.of("sequence detail", "shardingsphere://databases/logic_db/schemas/public/sequences/order_seq", "sequence", "order_seq"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadCollectionMetadataResourceCases")
    void assertReadCollectionMetadataResource(final String name, final String resourceUri, final String key, final List<String> expectedNames) throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(resourceUri));
            assertThat(items.stream().map(each -> String.valueOf(each.get(key))).toList(), is(expectedNames));
        }
    }
    
    private static Stream<Arguments> assertReadCollectionMetadataResourceCases() {
        return Stream.of(
                Arguments.of("schemas list", "shardingsphere://databases/logic_db/schemas", "schema", List.of("public")),
                Arguments.of("tables list", "shardingsphere://databases/logic_db/schemas/public/tables", "table", List.of("order_items", "orders")),
                Arguments.of("table columns list", "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", "column", List.of("amount", "order_id", "status")),
                Arguments.of("view columns list", "shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns", "column", List.of("order_id", "status")));
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
    void assertListResourceTemplates() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<String> actualTemplates = getResourceTemplates(interactionClient.listResourceTemplates()).stream()
                    .map(each -> String.valueOf(each.get("uriTemplate"))).toList();
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}"));
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}/schemas/{schema}"));
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}"));
        }
    }
    
    @Test
    void assertRejectUnsupportedResourceUri() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource("unsupported://resource");
            assertThat(String.valueOf(actual.get("error_code")), is("json_rpc_error"));
            assertThat(String.valueOf(actual.get("message")), is("Resource not found"));
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
    void assertExecuteSelectWithTruncation() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(((List<?>) actual.get("rows")).size(), is(1));
            assertThat(String.valueOf(actual.get("truncated")), is("true"));
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
    void assertExecuteExplainAnalyze() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "EXPLAIN ANALYZE SELECT * FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(String.valueOf(actual.get("statement_type")), is("EXPLAIN ANALYZE"));
            assertTrue(!((List<?>) actual.get("rows")).isEmpty());
        }
    }
    
    @Test
    void assertExecuteQueryTimeout() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql",
                            "SELECT COUNT(*) FROM SYSTEM_RANGE(1, 200000) a CROSS JOIN SYSTEM_RANGE(1, 200000) b", "timeout_ms", 1));
            assertThat(String.valueOf(actual.get("error_code")), is("timeout"));
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
    void assertRejectUnsupportedObjectType() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "order",
                            "object_types", List.of("TABLE", "VIEW", "INDEX", "MATERIALIZED_VIEW", "SEQUENCE")));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("Unsupported object_types value `MATERIALIZED_VIEW`."));
        }
    }
    
    @Test
    void assertRejectExecuteMultiStatement() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1; SELECT 2"));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("Only one SQL statement is allowed."));
        }
    }
    
    @Test
    void assertRejectBlankSavepointName() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SAVEPOINT"));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("Savepoint name is required."));
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
    void assertExecuteReleaseSavepoint() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
            Map<String, Object> savepointResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SAVEPOINT sp_release"));
            Map<String, Object> releaseResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "RELEASE SAVEPOINT sp_release"));
            Map<String, Object> commitResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("message")), is("Savepoint created."));
            assertThat(String.valueOf(releaseResponse.get("message")), is("Savepoint released."));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
        }
    }
    
    @Test
    void assertExecuteTransactionalDdlRefreshesMetadataOnCommit() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
            interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            Map<String, Object> commitResponse = interactionClient.call("execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "COMMIT"));
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(
                    "shardingsphere://databases/logic_db/schemas/public/tables/orders_archive"));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get("table")), is("orders_archive"));
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
}
